---
Version: 1.0.0
Status: FINAL
Release State: Frozen
Last Validation Date: 2026-07-20
---

# GitHub Actions CI/CD

## Workflow

The pipeline is defined in [`.github/workflows/ci.yml`](../.github/workflows/ci.yml).

It does the following:

1. **Validate Infrastructure**: Runs `kubectl kustomize` to validate manifests and validates Docker Compose config.
2. **Build & Test**: Builds all Maven modules and runs tests with `mvn clean verify`.
3. **Build & Push Docker Images**: 
   - Authenticates to AWS via **OIDC (OpenID Connect)**.
   - Builds and pushes Docker images to Amazon ECR for:
     - `product-service`
     - `order-service`
     - `inventory-service`
     - `api-gateway`
     - `notification-service`
4. **Deploy to K3s**:
   - Uses `KUBECONFIG_DATA` to authenticate to the remote K3s cluster.
   - Updates the Spring service deployment images to the current Git SHA tag using Kustomize.
   - Applies the k3s manifests via `kubectl apply -k`.
   - Waits for rollout completion.
5. **Post-Deployment Smoke Test**:
   - Generates an OAuth2 token from Keycloak.
   - Hits the `api-gateway` endpoint to verify the deployment is functional.

## AWS OIDC Authentication Model

To maximize security, this pipeline does **not** use long-lived AWS IAM Access Keys. Instead, it uses **OpenID Connect (OIDC)** to request short-lived, temporary credentials from AWS during the workflow run.

### IAM Role
The workflow assumes the AWS IAM role: `arn:aws:iam::889812257815:role/github-actions-deploy-role`.

This role has a strict **Trust Policy** that restricts access to:
- `audience`: `sts.amazonaws.com`
- `repository`: `indrajithas673/cloud-native-ecommerce`
- `branch`: `refs/heads/main`

This ensures that only pushes to the `main` branch of this specific repository can trigger deployments.

### IAM Policy
The IAM role is attached to a custom, least-privilege policy (`github-actions-ecr-least-privilege`) that only grants the specific `ecr:` permissions required to authenticate and push images to Amazon ECR.

## Required GitHub Secrets

Repository secrets required by the workflow:

- `KUBECONFIG_DATA`: Base64-encoded kubeconfig file for authenticating to the K3s cluster.
- `KEYCLOAK_CLIENT_SECRET`: Client secret for the `spring-cloud-client` used in smoke tests.
- `AWS_REGION`: The AWS region (e.g., `ap-south-1`).
- `AWS_ECR_REGISTRY`: The ECR registry URI (e.g., `889812257815.dkr.ecr.ap-south-1.amazonaws.com`).

*(Note: `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` are explicitly not used and should not be stored in GitHub Secrets.)*

## Rollback strategy

Primary rollback:

1. Find the previously successful workflow run and its commit SHA.
2. Re-run deployment from that commit, or push/redeploy that commit.
3. The workflow will publish images with that SHA and set the deployments back to those image tags.

Fast Kubernetes rollback:

```bash
kubectl -n micro-marketplace rollout undo deployment/api-gateway
kubectl -n micro-marketplace rollout undo deployment/product-service
kubectl -n micro-marketplace rollout undo deployment/order-service
kubectl -n micro-marketplace rollout undo deployment/inventory-service
kubectl -n micro-marketplace rollout undo deployment/notification-service
```

