<div align="center">
  <img src="docs/images/banner.png" alt="Cloud-Native E-Commerce Platform Banner" width="100%"/>
  
  # 🛒 Cloud-Native E-Commerce Platform
  
  **Event-driven e-commerce backend built using Spring Boot, Kafka, Kubernetes, Terraform, and AWS.**

  [![CI Pipeline](https://github.com/indrajithas673/cloud-native-ecommerce/actions/workflows/ci.yml/badge.svg)](https://github.com/indrajithas673/cloud-native-ecommerce/actions/workflows/ci.yml)
  [![Java 17](https://img.shields.io/badge/Java-17-007396.svg?logo=java&logoColor=white)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
  [![Spring Boot 3.x](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F.svg?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
  [![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Driven-231F20.svg?logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
  [![Kubernetes](https://img.shields.io/badge/Kubernetes-K3s-326ce5?logo=kubernetes&logoColor=white)](https://k3s.io/)
  [![AWS Infrastructure](https://img.shields.io/badge/AWS-Cloud-FF9900?logo=amazonaws&logoColor=white)](https://aws.amazon.com/)
</div>

---

### ✨ Project Highlights
* **Event-Driven Architecture**: Decoupled services using Apache Kafka.
* **Transactional Outbox**: Guaranteed data consistency without dual-write issues.
* **Circuit Breakers**: Prevented cascading failures using Resilience4j.
* **Secure API Gateway**: Centralized OAuth2/JWT validation via Keycloak.
* **Infrastructure as Code**: 100% automated AWS provisioning with Terraform.
* **Automated CI/CD**: Seamless GitHub Actions deployments using secure AWS OIDC.

---

## 🎯 Motivation

I built this project to transition from writing simple monolithic applications to tackling the real-world complexities of distributed systems. E-commerce platforms handle unpredictable traffic and require strict data consistency. This project demonstrates how to decouple services to prevent cascading failures, how to safely publish events without losing data, and how to automate the entire infrastructure and deployment lifecycle in the cloud.

---

## 🏛️ System Architecture

<div align="center">
  <img src="docs/images/architecture/high-level-architecture.png" alt="High Level Architecture" width="100%"/>
</div>

### Request Flow
1. **Client** sends an HTTP request with a valid JWT.
2. **API Gateway** intercepts, validates the JWT signature with Keycloak, and routes the request.
3. The **Order Service** receives the request and makes a synchronous call to the **Inventory Service** to verify stock.
4. The **Order Service** saves the order to **Amazon RDS** and atomically saves an event to its Outbox table.
5. The Outbox scheduler publishes the event to **Kafka**.
6. The **Notification Service** asynchronously consumes the event and processes the user notification.

---

## 💡 Key Engineering Decisions

**Transactional Outbox**  
Writing to a database and publishing to a message broker are two separate operations; if Kafka crashes after the database saves, the event is permanently lost. I implemented the Transactional Outbox pattern to guarantee consistency by saving the order and the Kafka event in a single database transaction. 

**Circuit Breaker**  
If the Inventory Service goes down, the Order Service could run out of threads waiting for a response, causing the entire system to crash. I used Resilience4j to fail fast and return a clean fallback response ("Inventory service is currently unavailable") to preserve overall system stability.

**Kafka**  
Synchronous HTTP calls create tight coupling. Kafka was used to decouple the critical path (placing an order) from non-critical tasks (sending emails), allowing the Notification Service to process events at its own pace or catch up if it restarts.

**Terraform**  
Clicking through the AWS console is error-prone. I provisioned the AWS VPC, EC2 instance, and Amazon RDS database using Terraform so the environment is perfectly reproducible and version-controlled.

**GitHub Actions + AWS OIDC**  
Storing long-lived AWS IAM access keys in GitHub Secrets is a security risk. I secured the CI/CD pipeline by using OpenID Connect (OIDC) to issue temporary, strictly scoped credentials just in time for deployment.

**Kubernetes (K3s)**  
AWS EKS is expensive and complex for a learning project. I used K3s to orchestrate containers, manage secrets, and ensure automatic service restarts, providing standard Kubernetes primitives on a single affordable EC2 instance.

---

## 🛠️ Technology Stack

| Category | Technology |
| :--- | :--- |
| **Backend** | Java 17, Spring Boot 3.1, Spring Cloud Gateway |
| **Security** | Keycloak, OAuth2, JWT |
| **Messaging** | Apache Kafka (KRaft mode) |
| **Database** | Amazon RDS (MySQL 8.0), Spring Data JPA, Flyway |
| **Observability** | Micrometer, Zipkin, Prometheus, Grafana |
| **Infrastructure** | Terraform, AWS EC2, K3s (Kubernetes) |
| **DevOps** | GitHub Actions, AWS OIDC, Jib, Kustomize |

---

## 📁 Repository Structure
- `api-gateway`: Spring Cloud Gateway application serving as the single entry point.
- `product-service`: Manages the product catalog.
- `order-service`: Core service handling transactions and Outbox publishing.
- `inventory-service`: Manages stock levels atomically.
- `notification-service`: Kafka consumer that processes asynchronous events.
- `terraform`: IaC files provisioning the AWS environment.
- `k8s`: Kubernetes manifests managed with Kustomize.
- `.github/workflows`: Automated CI/CD pipeline definitions.

---

## 🚀 Features
- Centralized API routing with JWT validation.
- Synchronous inter-service communication with Circuit Breakers.
- Asynchronous messaging for event-driven workflows.
- Distributed tracing across HTTP and messaging boundaries.
- Fully automated IaC provisioning and CI/CD pipelines.

---

## 📊 Observability

Because distributed systems are difficult to debug, I implemented comprehensive tracing and metrics. Every request is tagged with a trace ID that flows through the Gateway, into the internal services, and across Kafka.

<div align="center">
  <img src="docs/images/outputs/grafana_dashboard_collapsed.png" alt="Grafana Metrics" width="48%"/>
  <img src="docs/images/outputs/zipkin_ui.png" alt="Zipkin Distributed Tracing" width="48%"/>
</div>

---

## 🔄 CI/CD Pipeline

<div align="center">
  <img src="docs/images/architecture/ci-cd_pipeline.png" alt="CI/CD Pipeline" width="100%"/>
</div>

- **Build:** Maven compiles the source code.
- **Test:** Runs unit and integration tests (Testcontainers).
- **Jib:** Builds optimized, distroless Docker images without requiring a Docker daemon.
- **Amazon ECR:** Pushes images to AWS using OIDC authentication.
- **Kubernetes Deployment:** Uses Kustomize to update image tags and applies manifests to K3s.
- **Smoke Tests:** Verifies the deployment health.

---

## 🧠 Lessons Learned
- **Distributed Systems:** Handling partial failures is harder than writing the happy path. Circuit breakers are mandatory, not optional.
- **Data Consistency:** The Dual-Write problem is a real threat; relying on simple sequential execution without an Outbox pattern guarantees eventual data loss.
- **Kubernetes:** Application startup order cannot be guaranteed. Liveness and readiness probes are required to prevent crash loops when databases take longer to boot than the application.
- **DevOps:** Automating infrastructure and pipelines upfront saves countless hours of manual debugging and server configuration.

---

## 🔮 Future Improvements
- Horizontal pod autoscaling (HPA) for individual services based on CPU load.
- Centralized logging stack (ELK/EFK) to aggregate logs from all Kubernetes pods.
- Implement an automated performance testing suite (e.g., Gatling or JMeter).

---

## ⚡ Quick Start

```bash
# 1. Clone the repository
git clone https://github.com/indrajithas673/cloud-native-ecommerce.git
cd cloud-native-ecommerce

# 2. Start the backing infrastructure (MySQL, Keycloak, Kafka, Zipkin, Prometheus)
docker-compose up -d

# 3. Start a service locally
cd product-service
./mvnw spring-boot:run
```

---

## 📚 Documentation
- 📐 [System Architecture](docs/ARCHITECTURE.md)
- ☁️ [Infrastructure & Deployment Guide](docs/DEPLOYMENT.md)
- 🔌 [API Reference](docs/API.md)
- 💻 [Local Development Setup](docs/DEVELOPMENT.md)
