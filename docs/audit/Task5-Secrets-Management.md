# Task 5: Secrets Management & Configuration Guide

## Overview
This repository has been fully decoupled from hardcoded secrets. All sensitive configurations (database passwords, Keycloak credentials) have been externalized and are injected via Environment Variables.

## 1. Local Development Setup (IntelliJ / VSCode / Maven)
To run microservices locally using `mvn spring-boot:run` or your IDE, you must provide the required environment variables.
We strongly recommend creating a `.env` file in the root directory.

### `.env` File Requirements
Copy the provided `.env.example` to `.env` and fill in your values:
```bash
cp .env.example .env
```
Ensure your IDE is configured to load this `.env` file during startup (e.g., using the EnvFile plugin for IntelliJ).

**Required Variables for Microservices**:
- `MYSQL_USER`: The MySQL username (e.g., `root` or `ibatulanand`).
- `MYSQL_PASSWORD`: The MySQL password.

## 2. Docker Compose Setup
Docker Compose natively supports the `.env` file. 

To start the infrastructure:
1. Ensure your `.env` file exists in the root directory.
2. Run `docker-compose up -d`.

Docker will automatically inject the `MYSQL_ROOT_PASSWORD`, `KC_DB_PASSWORD`, and `KEYCLOAK_ADMIN_PASSWORD` into the respective MySQL and Keycloak containers, and propagate `MYSQL_PASSWORD` to the Spring Boot microservices.

## 3. Kubernetes Deployment
Kubernetes deployments **do not** use the `.env` file. 
Instead, the `k8s/apps.yaml` and `k8s/infra.yaml` manifests load credentials from a Kubernetes `Secret` named `platform-secrets`.

To deploy to Kubernetes:
1. Review `k8s/secrets.example.yaml` for the required keys.
2. Create the secret manually in your cluster using `kubectl`:
```bash
kubectl -n micro-marketplace create secret generic platform-secrets \
  --from-literal=mysql-root-password="<YOUR_ROOT_PWD>" \
  --from-literal=mysql-app-password="<YOUR_APP_PWD>" \
  --from-literal=keycloak-db-password="<YOUR_KC_DB_PWD>" \
  --from-literal=keycloak-admin-password="<YOUR_KC_ADMIN_PWD>"
```
*(Note: Our GitHub Actions CI/CD pipeline automatically creates this secret during deployment using GitHub Secrets).*

## 4. Future AWS Integration
To move to AWS Secrets Manager:
- Deploy the **External Secrets Operator** (ESO) to the Kubernetes cluster.
- Configure an ESO `SecretStore` pointing to AWS Secrets Manager.
- Create an `ExternalSecret` manifest that automatically syncs the AWS Secret into the local `platform-secrets` K8s Secret. 
- No application code or deployment manifest changes will be required.
