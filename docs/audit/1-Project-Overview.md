# Project Overview

## Project Objective
The objective of this project is to implement a scalable, cloud-native backend for an e-commerce platform using a modern microservices architecture. It demonstrates best practices in inter-service communication, event-driven architecture, and containerized deployment.

## Problem Statement
Traditional monolithic e-commerce applications struggle with scalability, fault isolation, and agile deployment. This project solves these issues by decoupling the platform into distinct domains (Products, Orders, Inventory, Notifications) that can scale and fail independently.

## Intended Users
- **End Users**: Customers browsing products and placing orders.
- **Administrators**: Users managing inventory and product catalogs.
- **System Operators**: DevOps engineers monitoring and scaling the platform.

## Business Domain
The core business domain is E-Commerce. The system handles:
1. **Product Catalog Management**
2. **Inventory Tracking**
3. **Order Placement and Validation**
4. **Asynchronous Notifications**

## Major Features
- API Gateway for unified routing and authentication.
- Secure, OAuth2-based authentication using Keycloak.
- Product creation and retrieval.
- Inventory validation before order placement.
- Asynchronous order notifications via Apache Kafka.
- Centralized monitoring and observability (Prometheus & Grafana).

## Current Maturity
The project is in a **Production-Ready** architectural state but requires further feature completeness (e.g., payment processing, user management UI) to be a fully functional product. The infrastructure (Kubernetes, Terraform) is mature.

## Overall Technology Stack
- **Languages**: Java 17
- **Framework**: Spring Boot 3.1.3, Spring Cloud 2022.0.4
- **Databases**: MySQL 8 (per-service databases)
- **Message Broker**: Apache Kafka (KRaft mode)
- **Security**: Keycloak (OAuth2 / OIDC)
- **Resilience**: Resilience4J (Circuit Breaker, Retry, TimeLimiter)
- **Containerization**: Docker, Jib Maven Plugin
- **Orchestration**: Kubernetes (Kustomize, K3s)
- **Infrastructure as Code**: Terraform (AWS EC2, VPC)
- **Observability**: Micrometer Tracing (OTel), Prometheus, Grafana

## High-Level Architecture
Client requests enter through an **API Gateway**, which validates JWT tokens against **Keycloak**. 
Requests are routed to either the **Product Service** or **Order Service**. 
The **Order Service** communicates synchronously (via WebClient) with the **Inventory Service** to verify stock. Upon successful order creation, an event is published to **Kafka**, which is consumed asynchronously by the **Notification Service**.

## End-to-End Request Lifecycle (Order Placement)
1. Client POSTs to `/api/order` with a Bearer Token.
2. API Gateway validates the token.
3. Request routes to Order Service.
4. Order Service performs a synchronous HTTP GET to Inventory Service to verify SKU stock.
5. If in stock, Order Service saves the order to MySQL.
6. Order Service publishes `OrderPlacedEvent` to Kafka `notificationTopic`.
7. Order Service returns a 201 Created response.
8. Notification Service consumes the Kafka event and processes the notification.

## Overall Strengths
- **Decoupled Data**: Each microservice manages its own MySQL database, enforcing strong boundaries.
- **Modern Message Broker**: Kafka uses KRaft mode, eliminating ZooKeeper dependency.
- **Resiliency**: Order Service uses Resilience4J circuit breakers to handle Inventory Service downtime.
- **Infrastructure Automation**: Comprehensive K8s manifests and Terraform code for AWS deployment.

## Overall Weaknesses
- **Missing Features**: No payment gateway integration or shipping module.
- **Security Scope**: Keycloak is implemented, but granular Role-Based Access Control (RBAC) at the method level (`@PreAuthorize`) is missing in the services.
- **Data Consistency**: The order process lacks a distributed transaction coordinator (e.g., Saga pattern). If Kafka fails after the database commit, the notification is lost.
