<div align="center">
  <img src="docs/images/banner.png" alt="Cloud-Native Micro Marketplace Banner" width="100%"/>
  
  # 🛒 Cloud-Native E-Commerce Platform
  
  **A production-ready, event-driven microservices backend built for scale.**

  [![CI Pipeline](https://github.com/indrajithas673/cloud-native-ecommerce/actions/workflows/ci.yml/badge.svg)](https://github.com/indrajithas673/cloud-native-ecommerce/actions/workflows/ci.yml)
  [![Java 17](https://img.shields.io/badge/Java-17-007396.svg?logo=java&logoColor=white)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
  [![Spring Boot 3.x](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F.svg?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
  [![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Driven-231F20.svg?logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
  [![Kubernetes](https://img.shields.io/badge/Kubernetes-K3s-326ce5?logo=kubernetes&logoColor=white)](https://k3s.io/)
  [![AWS Infrastructure](https://img.shields.io/badge/AWS-Cloud-FF9900?logo=amazonaws&logoColor=white)](https://aws.amazon.com/)

  *Demonstrating distributed systems, asynchronous messaging, and modern cloud deployments.*
</div>

---

## 📖 Project Overview

This project is a comprehensive backend for a modern e-commerce platform. It transitions away from traditional monolithic design to a robust **Microservices Architecture**, handling core functionalities like product cataloging, order processing, inventory management, and asynchronous user notifications.

> **Goal:** To engineer a scalable, highly available distributed system with real-world complexities—including inter-service communication, distributed tracing, and infrastructure-as-code (IaC).

### 🏗️ Core Services
- 🚪 **API Gateway**: Single entry point routing traffic and enforcing security.
- 📦 **Product Service**: Manages the product catalog.
- 🛒 **Order Service**: Handles transactions and triggers stock verification.
- 🧮 **Inventory Service**: Maintains stock levels and reserves items.
- 📬 **Notification Service**: Asynchronously emails users upon successful orders.
- 🔐 **Keycloak**: Issues JWTs and manages user identity.

---

## ✨ Key Features

- **Event-Driven Architecture**: Uses **Apache Kafka** to decouple services. The Order Service publishes events without waiting, preventing cascading failures.
- **Secure by Default**: End-to-end security using **OAuth2** and **JWT** via Keycloak.
- **Distributed Observability**: Integrated with **Zipkin**, **Prometheus**, and **Grafana** for tracing requests across multiple microservices.
- **Container Orchestration**: Deployed to a **K3s Kubernetes** cluster, ensuring high availability and automatic restarts.
- **Automated Infrastructure**: 100% of the AWS infrastructure (VPC, EC2, RDS, IAM) is provisioned via **Terraform**.
- **Fully Automated CI/CD**: **GitHub Actions** pipeline builds images, pushes to Amazon ECR, and deploys manifests directly to Kubernetes.

---

## 🏛️ Architecture & Request Flow

<div align="center">
  <img src="docs/images/architecture/SolutionArchitecture.png" alt="Solution Architecture" width="800"/>
</div>

1. **Client** sends an HTTP request (e.g., "Place Order") with a valid JWT.
2. **API Gateway** intercepts, validates the JWT with **Keycloak**, and forwards to the target service.
3. The **Order Service** makes a synchronous call to the **Inventory Service** to verify stock.
4. If valid, the **Order Service** saves the order to **Amazon RDS** and publishes an `OrderPlacedEvent` to **Kafka**.
5. The **Notification Service** asynchronously consumes the event and processes the notification.

---

## 🚀 Quick Start (Local Development)

Want to run the entire microservices stack on your local machine? It's as simple as one command.

```bash
# 1. Clone the repository
git clone https://github.com/indrajithas673/cloud-native-ecommerce.git
cd cloud-native-ecommerce

# 2. Start the infrastructure (MySQL, Keycloak, Kafka, Zipkin)
docker-compose up -d

# 3. Build and run the microservices (requires Java 17 and Maven)
./mvnw clean package -DskipTests
# Run individual services via your IDE or java -jar
```
*For detailed instructions, see the [Local Development Setup](docs/DEVELOPMENT.md).*

---

## 🛠️ Technology Stack

| Category | Technology |
| :--- | :--- |
| **Backend** | Java 17, Spring Boot 3.1, Spring Cloud |
| **Security** | Keycloak, OAuth2, JWT |
| **Messaging** | Apache Kafka (KRaft mode) |
| **Database** | Amazon RDS (MySQL 8.4), Spring Data JPA |
| **Observability** | Micrometer, Zipkin, Prometheus, Grafana |
| **Infrastructure** | Terraform, AWS EC2, K3s (Kubernetes) |
| **DevOps** | GitHub Actions, Jib, Kustomize |

---

## 💡 Engineering Decisions

- **Why Kafka?** Direct HTTP calls create tight coupling. Kafka allows services to operate independently, absorbing traffic spikes (e.g., Black Friday) without crashing downstream services.
- **Why Keycloak?** Avoids reinventing the wheel for IAM. Provides enterprise-grade security and standard OAuth2 flows out of the box.
- **Why K3s?** AWS EKS is expensive and complex. K3s provides a fully CNCF-compliant Kubernetes distribution that is lightweight and perfect for this scale.
- **Why Terraform?** Ensures infrastructure is reproducible, version-controlled, and eliminates "click-ops" errors.

---

## 📚 Documentation

Dive deeper into the implementation:
- 📐 [System Architecture](docs/ARCHITECTURE.md)
- ☁️ [Infrastructure & Deployment Guide](docs/DEPLOYMENT.md)
- 🔌 [API Reference](docs/API.md)
- 💻 [Local Development Setup](docs/DEVELOPMENT.md)

---
<div align="center">
  <i>A robust demonstration of practical cloud-native backend development.</i>
</div>
