# Feature Audit

This document identifies every logical feature in the repository and categorizes its current implementation state based purely on repository evidence.

## Completed Features

- **Microservice Scaffolding**: 5 distinct Spring Boot 3 applications successfully run and communicate.
- **Gateway Routing**: API Gateway successfully forwards requests to Product and Order domains.
- **Authentication**: Keycloak issues JWTs. API Gateway successfully intercepts and validates these tokens.
- **Product Catalog**: Creation and retrieval of products via MySQL persistence.
- **Stock Validation**: Inventory Service successfully queries the MySQL database to evaluate stock using `findBySkuCodeIn`.
- **Order Placement**: Order Service accepts orders, maps DTOs, and executes synchronous stock validation against Inventory Service.
- **Event Publishing**: Order Service successfully constructs and fires an `OrderPlacedEvent` into the Kafka broker.
- **Resiliency**: Circuit breaker interrupts long-running Inventory requests.
- **Observability**: Micrometer, Prometheus, and Grafana endpoints are fully integrated.
- **Containerization**: Jib Maven plugin seamlessly builds Docker images.
- **Infrastructure Automation**: Terraform successfully deploys an AWS K3s node and maps dynamic DNS.
- **Kubernetes Orchestration**: Complete Kustomize deployment topology including HPA, Ingress, and PVCs.

## Partially Implemented Features

- **Notification Dispatch**: The `NotificationService` successfully consumes the Kafka message and processes retries/DLQs, but it only *logs* the event. It does not integrate with an SMTP server or SMS gateway.
- **Inventory Deduction**: The `OrderService` *checks* if an item is in stock (`inventoryResponseArray[].isInStock()`), but there is no API call or event emitted to actually *reduce* the stock count in the Inventory Service. Stock remains static forever.

## Missing Features

- **Payment Processing**: No payment gateway (e.g., Stripe, PayPal) integration exists. Orders are assumed to be "paid" instantly upon placement.
- **Order State Machine**: Orders do not have statuses (e.g., `PENDING`, `PAID`, `SHIPPED`).
- **User Identity Context**: While Keycloak handles authentication, the actual user ID or email is not extracted from the JWT and saved with the Order. Orders are entirely anonymous.
- **Service Security**: The downstream APIs (`product-service`, `order-service`) do not validate JWTs, relying entirely on the API Gateway firewalling them.
- **Input Validation**: DTOs lack strict Java Bean Validation (`@NotNull`, `@Min`).

## Deprecated Features

- **Service Registry (Eureka)**: Early versions of Spring Cloud microservices commonly used Eureka. This repository explicitly dropped Eureka in favor of Kubernetes-native DNS, keeping the architecture modern.
