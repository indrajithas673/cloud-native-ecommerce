# Micro Marketplace: An E-commerce Microservices Application

[![CI](https://github.com/USER/REPO/actions/workflows/ci.yml/badge.svg)](https://github.com/USER/REPO/actions/workflows/ci.yml)
## Solution Overview

Micro Marketplace is a robust e-commerce application built on a microservices architecture using Spring technologies and other open-source tools. 
- This platform leverages the power of **Spring Boot**, **Spring Cloud Gateway**, and **KeyCloak** for service development, gateway management, and security, respectively. 
- It incorporates **Resilience4j** for resilient synchronous communication, and **Apache Kafka** for seamless event-driven asynchronous communication between services.
- It offers observability using **Micrometer**, **OpenTelemetry-based tracing**, **Prometheus**, and **Grafana** for metrics, dashboards, and trace context correlation. 

With a focus on scalability, resilience, and real-time interaction, Micro Marketplace provides a robust foundation for creating feature-rich online marketplaces.


### Solution Architecture
![Solution Architecture](docs/images/architecture/SolutionArchitecture.png)

### Services
- **Product Service:** Responsible for managing product information, including creation, retrieval, and updates. It uses a MySQL database.
- **Order Service:** Handles order management, including creating and retrieving orders. It uses a MySQL database.
- **Inventory Service:** Manages products inventory. It also uses a MySQL database.
- **Notification Service:** A stateless service responsible for sending notifications to users regarding their orders or other relevant updates.

### Major Components
- **API Gateway:** Spring Cloud Gateway is deployed to serve as the entry point for all external requests, effectively routing traffic to the appropriate microservices.
- **Auth Server:** For robust authentication and authorization mechanisms, KeyCloak is used to secure the microservices and protect sensitive data.
- **Circuit Breaker:** Resilience4j is used to maintain system reliability by preventing cascading failures in microservices through circuit-breaking mechanisms.
- **Message Broker:** Apache Kafka in **KRaft mode** forms the backbone of Micro Marketplace's event-driven architecture, facilitating asynchronous notification for orders.
- **Observability Stack:** Distributed tracing is implemented using Micrometer with an OpenTelemetry bridge, while Prometheus collects metrics and Grafana provides dashboards. 
   Moreover, Prometheus is used for collecting metrics, and Grafana for providing a rich dashboard for visualizing and analyzing application performance data.

### Tech Stack Used
<div>
    <table>
        <tr>
            <td>
                <strong>Languages & Frameworks</strong>
            </td>
            <td>
                <a href="https://www.java.com/en/">
                    <img alt="Java" src="https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white"/>
                </a>
                &emsp;
                <a href="https://spring.io/projects/spring-boot" target="_blank">
                    <img alt="Spring Boot" src="https://img.shields.io/badge/Spring Boot-6DB33F?style=flat&logo=springboot&logoColor=white">
                </a>
                &emsp;
                <a href="https://spring.io/projects/spring-cloud" target="_blank">
                    <img alt="Spring Cloud" src="https://img.shields.io/badge/Spring Cloud-6DB33F?style=flat&logo=spring&logoColor=white">
                </a>
                &emsp;
            </td>
        </tr>
        <tr>
            <td>
                <strong>Databases & Message Queue</strong>
            </td>
            <td>
                <a href="https://mysql.com/" target="_blank"> 
                    <img alt="MySQL" src="https://img.shields.io/badge/MySQL-00000F?style=flat&logo=mysql&logoColor=white"/>
                </a>
                &emsp;
                <a href="https://kafka.apache.org/" target="_blank"> 
                    <img alt="Apache Kafka" src="https://img.shields.io/badge/Apache%20Kafka-000?style=flat&logo=apachekafka"/>
                </a>
                &emsp;
            </td>
        </tr>
        <tr>
            <td>
                <strong>API Gateway</strong>
            </td>
            <td>
                <a href="https://spring.io/projects/spring-cloud-gateway" target="_blank"> 
                    <img alt="Spring Cloud Gateway" src="https://img.shields.io/badge/Spring Cloud Gateway-6DB33F.svg?&style=flat&logo=spring&logoColor=white"/>
                </a>
                &emsp;
            </td>
        </tr>
        <tr>
            <td>
                <strong>Circuit Breaker</strong>
            </td>
            <td>
                <a href="https://resilience4j.readme.io/" target="_blank"> 
                    <img alt="Resilience4J" src="https://img.shields.io/badge/Resilience4J-121212.svg?&style=flat&logo=resilience4j&logoColor=white"/>
                </a>
                &emsp;
            </td>
        </tr>
        <tr>
            <td>
                <strong>Security</strong>
            </td>
            <td>
                <a href="https://www.keycloak.org/" target="_blank"> 
                    <img alt="KeyCloak" src="https://img.shields.io/badge/KeyCloak-00B8E3.svg?&style=flat&logo=keycloak&logoColor=white"/>
                </a>
                &emsp;
            </td>
        </tr>
        <tr>
            <td>
                <strong>Observability</strong>
            </td>
            <td>
                <a href="https://micrometer.io/" target="_blank"> 
                    <img alt="Micrometer" src="https://img.shields.io/badge/Micrometer-117A71.svg?&style=flat&logo=micrometer&logoColor=white"/>
                </a>
                &emsp;
                <a href="https://opentelemetry.io/" target="_blank"> 
                    <img alt="OpenTelemetry" src="https://img.shields.io/badge/OpenTelemetry-425CC7.svg?&style=flat&logo=opentelemetry&logoColor=white"/>
                </a>
                &emsp;
                <a href="https://prometheus.io/" target="_blank"> 
                    <img alt="Prometheus" src="https://img.shields.io/badge/Prometheus-E6522C.svg?&style=flat&logo=prometheus&logoColor=white"/>
                </a>
                &emsp;
                <a href="https://grafana.com/" target="_blank"> 
                    <img alt="Grafana" src="https://img.shields.io/badge/Grafana-F79A2F.svg?&style=flat&logo=grafana&logoColor=white"/>
                </a>
                &emsp;
            </td>
        </tr>
        <tr>
            <td>
                <strong>Build & Containerization</strong>
            </td>
            <td>
                <a href="https://maven.apache.org/" target="_blank"> 
                    <img alt="Maven" src="https://img.shields.io/badge/Maven-C02748?style=flat&logo=apachemaven&logoColor=white"/>
                </a>
                &emsp;
                <a href="https://www.docker.com/" target="_blank"> 
                    <img alt="Docker" src="https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white"/>
                </a>
                &emsp;
                <a href="https://github.com/GoogleContainerTools/jib" target="_blank"> 
                    <img alt="Jib" src="https://img.shields.io/badge/Jib-FF6444?style=flat&logo=googlecloud&logoColor=white"/>
                </a>
                &emsp;
            </td>
        </tr>
    </table>
</div>


## Getting Started

### Prerequisites
1. **Docker Desktop** (or equivalent) should be running.
2. **K3s** (or another Kubernetes distribution) should be running locally.
3. **kubectl** installed and configured to connect to your local K3s cluster (if using Kubernetes).
4. **Maven** and **JDK 17+** installed.

### Local Setup & Environment Variables
Before running the application, you must initialize your environment variables:
```shell
cp .env.example .env
```
Ensure that `.env` contains secure passwords before deploying.

### Testcontainers Troubleshooting (Windows)
When running `mvn clean test`, Testcontainers attempts to spin up MySQL and Kafka containers.
If you encounter `Could not find a valid Docker environment` on Windows:
1. **Standard Docker Desktop + WSL2 (Recommended)**: Ensure Docker Desktop is installed, running, and the WSL2 integration is enabled for your default distro.
2. **Fallback TCP Workaround**: If you cannot use named pipes (`npipe:////./pipe/docker_engine`), expose the Docker daemon without TLS on `tcp://localhost:2375` (via Docker Desktop settings) and set `DOCKER_HOST=tcp://localhost:2375` in your environment before running Maven.

### Build and Import Images

We use locally built Docker images for this deployment. 

1. **Build Images**
   Navigate to the project root and run the provided script to build all images via Maven Jib plugin:
   ```shell
   ./build-local-images.ps1
   ```
   *(This script essentially runs `mvn compile jib:dockerBuild -Dimage=local/<service>:dev -pl <service>` for all applications.)*

2. **Import Images to K3s**
   Run the provided script to save the Docker images and import them into the k3s containerd runtime:
   ```shell
   ./import-local-images.ps1
   ```
   *(This automates `docker save local/<service>:dev | k3s ctr images import -`)*

### Local Development with Docker Compose

The easiest way to run the entire stack locally is via Docker Compose.

1. **Start the Stack**:
   ```shell
   docker-compose up -d
   ```
   *Note: The `docker-compose.yml` includes health checks for MySQL, Kafka, and Keycloak. The Spring Boot microservices will wait in the `Created` state and only boot once their infrastructure dependencies are fully healthy.*

2. **MySQL Volume / Password Recovery**:
   If you change the MySQL passwords in your `.env` file after initially starting the stack, the existing Docker volumes will retain the old credentials, and Spring Boot will fail to connect with `Access denied`.
   To recover and re-initialize MySQL with the new passwords, you must destroy the old volumes:
   ```shell
   docker-compose down -v
   docker-compose up -d
   ```
   **Warning:** This will erase all local developer data in the databases!

## Deployment Guide

### Option 1: Local Deployment (Docker Desktop / k3d)

To validate the deployment locally before pushing to production:

1. Ensure Docker Desktop is running and Kubernetes is enabled.
2. Run the deployment script to build images and apply Kubernetes manifests:
   ```bash
   .\deploy-k8s.ps1
   ```
3. Wait for all pods to be ready:
   ```bash
   kubectl wait --for=condition=Ready pods --all -n default --timeout=300s
   ```
4. Access local services:
   - API Gateway: `http://api.127.0.0.1.nip.io`
   - Keycloak: `http://auth.127.0.0.1.nip.io`
   - Prometheus: `http://prometheus.127.0.0.1.nip.io`
   - Grafana: `http://grafana.127.0.0.1.nip.io`

### Option 2: AWS Production Deployment (EC2 + K3s)

The AWS environment is provisioned using Terraform and configured for a single-node K3s cluster.

1. Provision the infrastructure:
   ```bash
   cd terraform
   terraform apply -auto-approve
   cd ..
   ```
2. Deploy the application to the AWS cluster:
   ```bash
   .\deploy-aws.ps1
   ```
   *(This script securely retrieves the K3s kubeconfig from the EC2 instance, configures kubectl, and applies the Kustomize manifests to the remote cluster.)*

3. Verify production endpoints using the outputted `PUBLIC_IP` (e.g., `http://api.<PUBLIC_IP>.nip.io`).

### Validation

1. **Check Pods**:
   Ensure all pods are in the `Running` state:
   ```shell
   kubectl get pods -n micro-marketplace
   ```

2. **Check Services**:
   Verify cluster IPs are assigned:
   ```shell
   kubectl get svc -n micro-marketplace
   ```

3. **Check Logs**:
   If any pod is failing, inspect the logs:
   ```shell
   kubectl logs <pod-name> -n micro-marketplace
   ```

## Usage

### Interacting with Application

- **Getting Credentials from KeyCloak**
  - Access the KeyCloak Admin UI by navigating to the ingress domain (`http://auth.invalid`). Ensure you add `127.0.0.1 auth.invalid api.invalid` to your `hosts` file.
  - Go to the Realm `spring-boot-microservices-realm`
  - Go to the Client `spring-cloud-client`
  - Go the the 'Credentials' section, and get the 'Client Secret'

- **Accessing API Endpoints**
  The API is accessible via the API Gateway Ingress domain (e.g. `http://api.invalid`). Make sure you fetch an OAuth2 token from Keycloak using Client Credentials as configured above.

## Continuous Integration (CI)

This repository uses GitHub Actions for continuous integration. The CI pipeline is defined in `.github/workflows/ci.yml` and is triggered on Pull Requests and commits to `main`. 

The pipeline ensures repository stability through parallelized quality gates:
1. **Validate Infrastructure**: Checks `docker-compose.yml` for syntax errors and validates Kubernetes manifests using `kubectl apply --dry-run=client -k k8s/`.
2. **Build, Test & Package Docker**: 
   - Uses Temurin JDK 17 with Maven caching.
   - Runs `mvn clean verify` executing unit tests and integration tests against real infrastructure provisioned on-the-fly via Testcontainers.
   - Scans dependencies for known vulnerabilities using the OWASP Dependency Check plugin.
   - Verifies the integrity of Docker images by building them into the local runner daemon using Jib (`mvn jib:dockerBuild`).

**Required Permissions:**
The workflow strictly uses `permissions: contents: read` to maintain least privilege.

**Local Verification:**
To verify the CI pipeline locally before pushing, run:
```shell
docker compose config
kubectl apply --dry-run=client -k k8s/
mvn clean verify
```

## Environment Cleanup

To completely remove the deployed resources from your Kubernetes cluster, run:
```shell
kubectl delete -k k8s/
```
