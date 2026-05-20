# Backend Services

This document details the architecture, design, and implementation of every backend microservice in the repository.

---

## 1. API Gateway (`api-gateway`)

- **Purpose**: Acts as the single entry point for all client requests, providing unified routing and authentication enforcement.
- **Responsibilities**: Route matching, token validation, edge security.
- **Folder Structure**:
  - `src/main/java/com/ibatulanand/apigateway/`
  - `config/SecurityConfig.java`
- **Configuration**: `application.properties` defines routes for `/api/product` and `/api/order`. Defines Keycloak as the JWT issuer.
- **Security**: Implements Spring Security WebFlux (`@EnableWebFluxSecurity`). Configures `oauth2ResourceServer` to enforce JWT authentication on all routes except `/actuator/health`.
- **Dependencies**: `spring-cloud-starter-gateway`, `spring-boot-starter-oauth2-resource-server`, `micrometer-tracing-bridge-otel`.
- **Important Classes**: `SecurityConfig.java` (handles token validation).

---

## 2. Product Service (`product-service`)

- **Purpose**: Manages the product catalog.
- **Folder Structure**: standard MVC (`controller`, `service`, `repository`, `model`, `dto`).
- **Controllers**: `ProductController` handles POST and GET to `/api/product`.
- **Services**: `ProductService` maps between `ProductRequest` DTOs and `Product` entities and delegates to the repository.
- **Repositories**: `ProductRepository` (Spring Data JPA).
- **Entities**: `Product` (id, name, description, price). Uses `@PrePersist` to auto-generate a UUID.
- **Configuration**: Uses MySQL (`com.mysql.cj.jdbc.Driver`) via JPA.
- **Validation**: No strict `jakarta.validation` annotations (e.g., `@NotBlank`) exist on `ProductRequest`.
- **Exception Handling**: Relies on default Spring Boot exception mapping.
- **Dependencies**: `spring-boot-starter-data-jpa`, `spring-boot-starter-web`, `mysql-connector-j`.

---

## 3. Order Service (`order-service`)

- **Purpose**: Core transactional domain for placing user orders.
- **Folder Structure**: standard MVC plus `config` (WebClient) and `event` (Kafka DTOs).
- **Controllers**: `OrderController` exposes `/api/order`. It utilizes `@CircuitBreaker`, `@TimeLimiter`, and `@Retry` from Resilience4J for the `placeOrder` endpoint, wrapping it in a `CompletableFuture`.
- **Services**: `OrderService` handles the complex orchestration:
  1. Maps `OrderRequest` to `Order` entity.
  2. Extracts SKU codes.
  3. Makes a synchronous HTTP call to `InventoryService` via `WebClient`.
  4. Checks if all products are in stock.
  5. Saves the `Order`.
  6. Publishes an `OrderPlacedEvent` to Kafka.
- **Repositories**: `OrderRepository`.
- **Entities**: `Order` (has a One-To-Many relationship with `OrderLineItems`).
- **Configuration**: Connects to `order_service` MySQL schema and `broker:29092` Kafka broker.
- **Business Logic**: Order creation strictly fails if any item is out of stock. If `InventoryService` times out, Resilience4J falls back to a graceful error message: *"Oops! Something went wrong, please order after some time!"*
- **Important Classes**: `OrderService.java`.

---

## 4. Inventory Service (`inventory-service`)

- **Purpose**: Maintains and queries stock levels.
- **Controllers**: `InventoryController` handles GET `/api/inventory?skuCode=...`.
- **Services**: `InventoryService` queries the DB and maps results to `InventoryResponse`.
- **Repositories**: `InventoryRepository` uses a custom derived query: `findBySkuCodeIn(List<String> skuCode)`.
- **Entities**: `Inventory` (id, skuCode, quantity).
- **Business Logic**: Determines an item is in stock if `quantity > 0`.
- **Note**: The codebase contains commented-out thread-sleep code in `InventoryService.java` which was originally used to test Resilience4J timeouts.

---

## 5. Notification Service (`notification-service`)

- **Purpose**: Processes side-effects resulting from placed orders.
- **Controllers**: None (Headless service).
- **Configuration**: Configures Spring Kafka consumer properties in `application.properties`. Requires JSON deserialization trust for `OrderPlacedEvent`.
- **Event Handling**: `NotificationServiceApplication` acts as the listener.
  - `@KafkaListener` subscribes to `notificationTopic`.
  - `@RetryableTopic` automatically creates a `-retry` topic and retries failures up to 3 times with a 2-second backoff.
  - `@DltHandler` captures messages that fail all retries and logs them.
- **Business Logic**: Currently a stub. It logs *"Received Notification for Order - {orderId}"*. No actual email client (e.g., JavaMailSender) is implemented.
