# Development Guide

This document provides instructions for setting up the Micro Marketplace repository for local development.

## 1. Prerequisites
- **Java 17+** (JDK)
- **Maven 3.8+**
- **Docker Desktop** (or equivalent container runtime)
- **Git**

## 2. Project Structure
The repository is built as a multi-module Maven project (though each microservice can be opened individually).
- `product-service`: Manages product lifecycle.
- `order-service`: Manages customer orders and Circuit Breaker resilience.
- `inventory-service`: Manages product stock.
- `notification-service`: Kafka event consumer for order notifications.
- `api-gateway`: Spring Cloud Gateway with Keycloak Resource Server config.

## 3. Local Development Setup

To run the services locally without Kubernetes, you can use the provided Docker Compose configuration which boots up all necessary infrastructure dependencies.

### Step 1: Environment Variables
Create a `.env` file from the provided example:
```bash
cp .env.example .env
```
*(You may leave the default placeholder passwords for local development).*

### Step 2: Boot Infrastructure
Start the supporting services (MySQL, Kafka, Keycloak, Prometheus, Zipkin, Grafana):
```bash
docker-compose up -d
```
*Note: The actual Spring Boot microservices are configured to wait until these dependencies are healthy. You can either let docker-compose boot the Java services, or you can run them manually via your IDE for debugging.*

### Step 3: Run Microservices manually (Optional, for Debugging)
If you wish to debug a service in your IDE (e.g., IntelliJ or Eclipse), stop the specific docker container:
```bash
docker stop order-service
```
Then, run the main application class `OrderServiceApplication.java`.
*Ensure your IDE runner includes the environment variables defined in the `.env` file.*

## 4. Building and Testing

### Compile and Unit Test
To build all modules and run unit tests (uses Testcontainers):
```bash
mvn clean verify
```

### Build Docker Images (Local Jib)
To compile the code and build Docker images into your local Docker daemon, run this for each service:
```bash
mvn compile jib:dockerBuild -Dimage=local/<service>:dev -pl <service>
```

## 5. Helpful Commands
- **Check Kafka Topics**: Connect to the Kafka container to verify messages are being produced.
- **Keycloak Admin**: Access the Keycloak UI at `http://localhost:8080` (or `8181` depending on compose mapping) using the `keycloak-admin-password` from your `.env`.
- **Zipkin UI**: Traces can be viewed locally at `http://localhost:9411`.
