# GitHub Actions CI/CD

## Workflow

The pipeline is defined in [`.github/workflows/ci-cd.yml`](../.github/workflows/ci-cd.yml).

It does the following:

1. Builds all Maven modules and runs tests with `mvn clean verify`
2. Builds and pushes Docker images for:
   - `product-service`
   - `order-service`
   - `inventory-service`
   - `api-gateway`
   - `notification-service`
3. Generates the runtime Kustomize values file from the EC2 public IP
4. Copies the `k8s/` manifests to the EC2 server
5. Connects over SSH
6. Creates or updates Kubernetes secrets from GitHub Secrets
7. Applies the k3s manifests
8. Updates the Spring service deployment images to the current Git SHA tag
9. Waits for rollout completion

## Required GitHub Secrets

Repository or environment secrets required by the workflow:

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
- `EC2_PUBLIC_IP`
- `EC2_SSH_USER`
- `EC2_SSH_PRIVATE_KEY`
- `MYSQL_ROOT_PASSWORD`
- `MYSQL_APP_PASSWORD`
- `KEYCLOAK_DB_PASSWORD`
- `KEYCLOAK_ADMIN_PASSWORD`

Recommended values:

- `EC2_SSH_USER`: `ubuntu`
- `EC2_PUBLIC_IP`: the Elastic IP of the Terraform-managed EC2 instance
- `EC2_SSH_PRIVATE_KEY`: the private key matching the EC2 key pair

## Deployment instructions

1. Create the Docker Hub repository namespace under your Docker Hub account.
2. Add the GitHub Secrets listed above.
3. Make sure the EC2 instance already has k3s installed and is reachable over SSH.
4. Push to `main`, or run the workflow manually from the GitHub Actions tab.
5. Watch the `deploy` job until all rollout checks pass.

After deployment, validate:

```bash
kubectl -n micro-marketplace get pods
kubectl -n micro-marketplace get ingress
```

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

Then verify:

```bash
kubectl -n micro-marketplace rollout status deployment/api-gateway
kubectl -n micro-marketplace rollout status deployment/product-service
kubectl -n micro-marketplace rollout status deployment/order-service
kubectl -n micro-marketplace rollout status deployment/inventory-service
kubectl -n micro-marketplace rollout status deployment/notification-service
```
