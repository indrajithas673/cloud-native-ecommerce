# Security Overview

This document outlines the security architecture and best practices implemented in Version 1.1.0 of the Micro Marketplace application.

## 1. Authentication & Authorization (Keycloak)

Micro Marketplace relies on **Keycloak** as its centralized Identity and Access Management (IAM) provider.

### OAuth2 / JWT Flow
All external incoming requests must be authenticated via the Spring Cloud API Gateway.
1. Clients authenticate against the Keycloak server using the `client_credentials` grant type (or standard authorization code flow for UI clients).
2. Keycloak issues an asymmetric signed JSON Web Token (JWT).
3. The API Gateway serves as a Resource Server, intercepting requests and validating the JWT signature using the Keycloak public key (fetched via JWKS).
4. If the token is valid, the API Gateway forwards the request to the downstream microservices.

### Security Boundaries
- Downstream microservices (Product, Order, Inventory, Notification) **do not** validate the JWT themselves. They exist within a private Kubernetes network and trust the API Gateway to enforce edge security.
- Internal service-to-service communication relies on Kubernetes network isolation.

## 2. Infrastructure Security (AWS OIDC)

To adhere to the principle of least privilege and eliminate long-lived static credentials, the GitHub Actions CI/CD pipeline authenticates with AWS using OpenID Connect (OIDC).

- **Federated Trust**: GitHub Actions is configured as a trusted OIDC Identity Provider in AWS IAM.
- **Temporary Credentials**: The CI/CD pipeline assumes an IAM Role (`arn:aws:iam::[ACCOUNT_ID]:role/github-actions-ecommerce-role`), which grants short-lived, scoped access specifically for managing Amazon ECR and reading SSM parameters.
- **No Hardcoded Secrets**: `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` are strictly prohibited in GitHub Secrets.

## 3. Secret Management (Kubernetes)

Sensitive application properties (e.g., database passwords) are injected into the microservices at runtime using Kubernetes Secrets.

### `cloud-db-secrets`
- Generated dynamically during the CI/CD pipeline execution.
- Contains the Amazon RDS connection credentials (`DB_HOST`, `DB_USERNAME`, `DB_PASSWORD`).
- Mounted as environment variables into the Spring Boot Pods and Keycloak.
- Eliminates the need to commit plain-text passwords into the repository.

## 4. Network Security (Kubernetes)
- The cluster runs on AWS EC2 behind a strictly configured Security Group that only permits inbound traffic on ports `80` (HTTP), `443` (HTTPS), and `22` (SSH for admins).
- The internal Kubernetes overlay network uses Flannel/Calico. Services communicate via internal ClusterIPs, masking them from external exposure.

## 5. Security Best Practices
- **No root users**: Containers are built using the `maven-jib-plugin` utilizing distroless/slim base images running as non-root users where possible.
- **Vulnerability Scanning**: The CI pipeline includes OWASP Dependency-Check to prevent deploying known vulnerable Java dependencies.

## 6. Known Limitations (Version 1.1.0)
- **HTTPS Termination**: Currently, TLS termination is expected to be handled by an external load balancer or the Kubernetes Ingress controller. Internally, services communicate over plaintext HTTP.
- **Service Mesh**: Mutual TLS (mTLS) between microservices is not implemented in this version.
