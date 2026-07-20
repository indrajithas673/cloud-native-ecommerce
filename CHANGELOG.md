# Changelog

## [1.0.0] - 2026-07-20

### Added
- **Microservices architecture**: Initial implementation of the e-commerce domain using Spring Boot microservices (Product, Order, Inventory, Notification, API Gateway).
- **Kubernetes deployment**: Fully containerized and orchestrated deployment using Kustomize and K3s.
- **AWS infrastructure**: Cloud hosting using Amazon EC2 \	3.medium\ instances provisioned for K3s.
- **OAuth2/Keycloak security**: Edge-level API security with Keycloak JWT authentication configured at the API Gateway.
- **Kafka messaging**: Event-driven asynchronous communication for order processing and notifications.
- **Terraform infrastructure**: Infrastructure as Code setup for provisioning AWS resources.
- **GitHub Actions CI/CD**: End-to-end continuous integration and continuous deployment pipeline.
- **AWS OIDC authentication**: Keyless, secure deployment pipeline integration with AWS via OIDC identity federation.
- **Production validation**: Verified live configuration with rigorous auditing of scaling, networking, and secrets management.
- **Continuous Deployment validation**: Automated smoke testing integrated into the pipeline to prevent unhealthy deployments.
