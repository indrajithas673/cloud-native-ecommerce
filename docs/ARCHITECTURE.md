# Architecture Overview

## 1. System Architecture

The following diagram illustrates the overarching system architecture, displaying the internal communication between Spring Boot microservices, infrastructure boundaries, and the external Gateway.

```mermaid
graph TD
    Client[Client / Browser]
    
    subgraph AWS K3s Cluster
        Ingress[Ingress Controller]
        API_Gateway[API Gateway]
        Keycloak[Auth Server - Keycloak]
        
        subgraph Microservices
            Product[Product Service]
            Order[Order Service]
            Inventory[Inventory Service]
            Notification[Notification Service]
        end
        
        Kafka[(Apache Kafka)]
        Prometheus[Prometheus]
        Grafana[Grafana]
        Zipkin[Zipkin Tracing]
    end
    
    subgraph AWS Managed Services
        RDS[(Amazon RDS - MySQL 8.4)]
    end

    Client -->|HTTPS| Ingress
    Ingress -->|Route /api/*| API_Gateway
    Ingress -->|Route /auth/*| Keycloak
    
    API_Gateway -->|Authenticate| Keycloak
    API_Gateway -->|Forward| Product
    API_Gateway -->|Forward| Order
    
    Order -->|Sync REST| Inventory
    Order -.->|Async Event| Kafka
    Kafka -.->|Consume| Notification
    
    Product --> RDS
    Order --> RDS
    Inventory --> RDS
    Keycloak --> RDS
    
    Product -.->|Metrics/Traces| Zipkin
    Order -.->|Metrics/Traces| Zipkin
    Inventory -.->|Metrics/Traces| Zipkin
    API_Gateway -.->|Metrics/Traces| Zipkin
```

## 2. Request Flow

This diagram details the sequence of a typical API request as it traverses the API Gateway, retrieves authorization, and traces execution across downstream microservices.

```mermaid
sequenceDiagram
    participant Client
    participant Gateway as API Gateway
    participant Keycloak as Auth Server
    participant Microservice as Target Service (e.g. Product)
    participant DB as Amazon RDS
    participant Zipkin as Zipkin Tracing

    Client->>Keycloak: 1. Request OAuth2 Token
    Keycloak-->>Client: 2. Return JWT Token
    
    Client->>Gateway: 3. API Request + Bearer Token
    Gateway->>Keycloak: 4. Validate Token Signature
    Keycloak-->>Gateway: 5. Token Valid
    
    Gateway->>Zipkin: 6. Start Trace Span
    Gateway->>Microservice: 7. Forward Request (with Trace ID)
    Microservice->>Zipkin: 8. Start Child Span
    
    Microservice->>DB: 9. Execute Query
    DB-->>Microservice: 10. Return Data
    
    Microservice-->>Gateway: 11. Return Response
    Gateway-->>Client: 12. Return Final Response
```

## 3. Order Processing Flow

The Order processing flow involves synchronous interaction for inventory deduction and asynchronous messaging for notification delivery. It utilizes Circuit Breaker resilience to handle downstream service failures gracefully.

```mermaid
sequenceDiagram
    participant Client
    participant OrderService as Order Service
    participant InventoryService as Inventory Service
    participant Kafka as Apache Kafka
    participant NotificationService as Notification Service

    Client->>OrderService: POST /api/order
    activate OrderService
    
    OrderService->>InventoryService: HTTP POST /api/inventory/deduct
    activate InventoryService
    
    alt Inventory Deduction Success
        InventoryService-->>OrderService: Success Response
        OrderService->>Kafka: Publish OrderPlacedEvent
        OrderService-->>Client: 201 Created (Order Placed)
        Kafka-->>NotificationService: Consume OrderPlacedEvent
        NotificationService->>NotificationService: Send Email/SMS Logic
    else Inventory Deduction Failure / Timeout
        InventoryService-->>OrderService: Timeout / 500 Error
        deactivate InventoryService
        OrderService->>OrderService: Circuit Breaker triggers Fallback
        OrderService-->>Client: 200 OK (Fallback: "Oops! Something went wrong")
    end
    
    deactivate OrderService
```

## 4. Authentication Flow (Keycloak)

```mermaid
graph TD
    User[Client Application] -->|1. Client Credentials Grant| Keycloak[Keycloak]
    Keycloak -->|2. Verify Credentials| RDS[(RDS Auth DB)]
    RDS --> Keycloak
    Keycloak -->|3. Issue JWT Token| User
    
    User -->|4. Request with Bearer Token| API[API Gateway]
    API -->|5. Verify Signature| Keycloak
    Keycloak --> API
    API -->|6. Forward if valid| Downstream[Microservices]
```
