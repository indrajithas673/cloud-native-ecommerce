# DevOps and Infrastructure

This document outlines the developer workflows, CI/CD readiness, and infrastructure-as-code implementations.

## Infrastructure as Code (Terraform)
The repository includes a robust `terraform/` module for provisioning AWS infrastructure.
- **Provider**: AWS (`hashicorp/aws`).
- **Networking**: Provisions a custom VPC, Public Subnet, Internet Gateway, and Route Tables.
- **Security**: Creates an EC2 Security Group allowing inbound SSH (port 22), HTTP (80), HTTPS (443), and Kubernetes API (6443) exclusively from an admin CIDR block.
- **Compute**: Provisions an AWS EC2 instance (`t3.medium` by default) running Ubuntu 24.04.
- **Bootstrapping**: The `user_data` script automatically installs **K3s** (a lightweight Kubernetes distribution) immediately upon instance creation.
- **Integration**: Extracts the assigned public Elastic IP (`aws_eip`) and generates a `runtime-values.env` file. This file is seamlessly consumed by Kustomize to dynamically update Kubernetes Ingress hosts (via `.nip.io` wildcard DNS).

## Local Development Workflow
The repository supports dual modes for local development.

### 1. Docker Compose (`docker-compose.yml`)
- Ideal for rapid local iteration without Kubernetes overhead.
- Spins up three distinct MySQL containers, Kafka (KRaft), Keycloak, Prometheus, Grafana, and all application services.
- Pulls application images directly from local Docker registry.

### 2. Local Kubernetes (K3s / Docker Desktop K8s)
Automated PowerShell scripts drive the Kubernetes workflow:
1. `build-local-images.ps1`: 
   - Uses Maven and the **Jib Maven Plugin**.
   - Builds Distroless Java 17 container images (`gcr.io/distroless/java17-debian11`) directly to the local Docker daemon without requiring a `Dockerfile`.
2. `import-local-images.ps1`: 
   - Automates the import of daemon images into the K3s `containerd` runtime cache. (Bypassed if using Docker Desktop native K8s).
3. `deploy-k8s.ps1` & `clean-k8s.ps1`:
   - Wraps Kustomize and `kubectl apply -k k8s/` for rapid deployment/teardown.
4. `verify-k8s.ps1`:
   - Runs readiness checks on all pods and endpoints.

## CI/CD Readiness
- **Build Reproducibility**: The use of Maven Wrapper (`mvnw` is implied, though standard Maven commands are used in scripts) and Jib ensures consistent, rootless, and minimal-surface-area container builds.
- **Configuration Segregation**: Kubernetes manifests cleanly separate secrets (`secrets.yaml`), environment variables (`app-config.yaml`), and infrastructure. This maps perfectly to CI/CD tools like ArgoCD or GitHub Actions.

## Recommendations for CI/CD Pipeline
While the project is highly automatable locally via PowerShell, it currently lacks a centralized CI/CD YAML (e.g., `.github/workflows/main.yml`). 
A production pipeline should be structured as follows:
1. **Lint/Test**: Run `mvn clean test`.
2. **Build/Push**: Run `mvn compile jib:build` to push images to a remote registry (e.g., AWS ECR or Docker Hub).
3. **Deploy (Infra)**: `terraform apply -auto-approve`.
4. **Deploy (App)**: Inject actual secrets into `secrets.yaml` via HashiCorp Vault, and execute `kubectl apply -k k8s/`.
