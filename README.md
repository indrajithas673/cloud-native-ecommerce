<div align="center">
  <img src="docs/images/banner.png" alt="Cloud-Native Micro Marketplace Banner" width="100%"/>
  
# 🛒 Cloud-Native E-Commerce Platform
**A Microservices-based E-Commerce Backend built for scalability and learning.**

[![CI Pipeline](https://github.com/indrajithas673/cloud-native-ecommerce/actions/workflows/ci.yml/badge.svg)](https://github.com/indrajithas673/cloud-native-ecommerce/actions/workflows/ci.yml)
[![Java 17](https://img.shields.io/badge/Java-17-007396.svg?logo=java&logoColor=white)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot 3.x](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F.svg?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Driven-231F20.svg?logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-K3s-326ce5?logo=kubernetes&logoColor=white)](https://k3s.io/)
[![AWS Infrastructure](https://img.shields.io/badge/AWS-Cloud-FF9900?logo=amazonaws&logoColor=white)](https://aws.amazon.com/)

*Built to demonstrate microservices architecture, event-driven communication, and cloud deployments.*
</div>

---

## 📖 Project Overview

### What is this?
This is a backend for an e-commerce platform built using a microservices architecture. It handles core e-commerce functionalities like managing products, processing orders, updating inventory, and sending notifications. 

### Why was it built?
I built this project to transition from building simple monolithic applications to understanding how large-scale distributed systems work in the real world. The goal was to get hands-on experience with inter-service communication, asynchronous messaging, and container orchestration.

### How does it work?
The system is divided into focused microservices:
- **API Gateway**: The single entry point for all client requests. It routes traffic and enforces security.
- **Product Service**: Manages the catalog of products.
- **Order Service**: Handles customer orders and triggers inventory checks.
- **Inventory Service**: Manages stock levels and ensures products are available before confirming an order.
- **Notification Service**: Listens for successful orders and simulates sending email/SMS confirmations.
- **Keycloak**: Handles user authentication and issues JSON Web Tokens (JWT).
- **Apache Kafka**: Acts as the message broker for asynchronous communication between services.
- **Amazon RDS**: Hosts the relational database for persistent storage.
- **Kubernetes (K3s)**: Orchestrates the Docker containers on an AWS EC2 instance.

---

## ✨ Features

- **Product Management**: APIs to create, read, update, and delete products.
- **Order Processing**: Securely place orders with automatic inventory validation.
- **Inventory Updates**: Real-time stock deduction upon order placement.
- **Event-Driven Notifications**: Asynchronous processing using Kafka so the Order Service doesn't wait for notifications to send.
- **JWT Authentication**: Secured endpoints using OAuth2 and Keycloak.
- **REST APIs**: Standardized communication between the frontend (client) and the gateway.
- **Containerized Deployment**: Every service is packaged as a Docker image.
- **Infrastructure as Code**: AWS infrastructure provisioned entirely via Terraform.
- **CI/CD**: Automated testing and deployment to Kubernetes using GitHub Actions.

---

## 📸 Screenshots & Observability

*(Distributed tracing and metrics dashboards implemented in this project)*

<div align="center">
  <img src="docs/images/outputs/zipkin_ui.png" alt="Zipkin Distributed Tracing" width="48%"/>
  <img src="docs/images/outputs/grafana_dashboard_collapsed.png" alt="Grafana Metrics" width="48%"/>
</div>

---

## 🏗️ Architecture & Request Flow

<div align="center">
  <img src="docs/images/architecture/SolutionArchitecture.png" alt="Solution Architecture" width="800"/>
</div>

### How a Request Flows Through the System:
1. **Client** sends an HTTP request (e.g., "Place Order") with a JWT token.
2. **API Gateway** intercepts the request, validates the JWT with **Keycloak**, and forwards it.
3. The **Order Service** receives the request and makes a synchronous HTTP call to the **Inventory Service** to check stock.
4. If in stock, the **Order Service** saves the order to **Amazon RDS** and publishes an "OrderPlacedEvent" to **Kafka**.
5. The **Notification Service** consumes the event from **Kafka** and processes the notification asynchronously.

---

## 🛠️ Technology Stack

| Category | Technology |
| :--- | :--- |
| **Backend Framework** | Java 17, Spring Boot 3.1, Spring Cloud |
| **Security** | Keycloak, OAuth2, JWT |
| **Message Broker** | Apache Kafka (KRaft mode) |
| **Database** | Amazon RDS (MySQL 8.4), Spring Data JPA |
| **Observability** | Micrometer, Zipkin, Prometheus, Grafana |
| **Infrastructure** | Terraform, AWS EC2, K3s Kubernetes |
| **CI/CD** | GitHub Actions, Jib, Kustomize |

---

## 🤔 Why These Technologies?

- **Why Spring Boot?** It provides excellent out-of-the-box support for microservices (Spring Cloud) and dependency injection, making development fast and structured.
- **Why Kafka?** Direct HTTP calls between all services create tight coupling and cascading failures. Kafka allows the Order Service to just say "Order Placed" and move on, while the Notification service handles it at its own pace.
- **Why Keycloak?** Instead of writing custom login and token generation logic from scratch, Keycloak provides an industry-standard Identity and Access Management solution.
- **Why Amazon RDS?** A managed database takes away the headache of manual backups and scaling, allowing me to focus on application logic.
- **Why K3s instead of EKS?** AWS EKS is expensive and complex for a student project. K3s is a lightweight, fully compliant Kubernetes distribution that runs perfectly on a single EC2 instance.
- **Why Terraform?** Creating AWS resources manually via the console is error-prone and hard to replicate. Terraform allows me to define my infrastructure in code so it can be spun up or destroyed with one command.

---

## 🚧 Challenges Faced & Lessons Learned

1. **Kafka Configuration**: Initially, I struggled with Zookeeper setup. I resolved this by moving to Kafka's newer KRaft mode, which removes the Zookeeper dependency and simplifies the architecture.
2. **Kubernetes `CrashLoopBackOff`**: My services kept crashing in K8s because they were trying to connect to the database before RDS was fully ready. I solved this by adding readiness and liveness probes in my deployment manifests.
3. **Keycloak Integration**: Configuring Spring Security to act as an OAuth2 Resource Server and properly decode Keycloak JWTs took significant debugging of the `application.yml` issuer URIs.
4. **CI/CD Pipeline**: Automating the deployment meant dealing with AWS credentials. I learned how to set up AWS OIDC (OpenID Connect) so GitHub Actions could securely deploy to my EC2 instance without storing long-lived secret keys.

---

## 📚 Detailed Documentation

If you want to dive deeper into the implementation details, check out these docs:
- [System Architecture](docs/ARCHITECTURE.md)
- [Infrastructure & Deployment Guide](docs/DEPLOYMENT.md)
- [API Reference](docs/API.md)
- [Local Development Setup](docs/DEVELOPMENT.md)

---
<div align="center">
  <i>A final-year engineering project demonstrating practical cloud-native backend development.</i>
</div>
