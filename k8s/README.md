# Kubernetes Deployment Notes

These manifests target a single-node k3s cluster on one AWS EC2 instance.

## Before you apply

1. Run Terraform first so it generates [generated/runtime-values.env](./generated/runtime-values.env) with the EC2 public IP and ingress hostnames.
2. Update the placeholder passwords in [secrets.yaml](./secrets.yaml).
3. Ensure the k3s default Traefik ingress controller is enabled.

Using `nip.io` keeps the setup simple for a single node because hostnames like `api.<public-ip>.nip.io` resolve automatically to that IP.

## Apply order

Apply these manifests with kustomize so the Keycloak realm ConfigMap is generated from the checked-in realm file:

```bash
kubectl kustomize --load-restrictor LoadRestrictionsNone k8s | kubectl apply -f -
```

The rollout order should still be:

1. Namespace, secrets, config, and storage
2. Infrastructure: `mysql`, `kafka`, `keycloak`
3. Spring services: `api-gateway`, `product-service`, `order-service`, `inventory-service`, `notification-service`
4. Ingress

## Why this order matters

- `Namespace`, `Secrets`, `ConfigMaps`, and `PVCs` must exist before workloads reference them.
- The generated `deployment-values` ConfigMap must exist so Kustomize can inject the EC2-backed ingress hosts and Keycloak issuer URI automatically.
- `MySQL`, `MongoDB`, and `Kafka` must be ready before the Spring services can connect successfully.
- `Keycloak` should be running before `api-gateway` because the gateway validates JWTs using the Keycloak issuer URI.
- `Ingress` comes last so external traffic only starts once backends are present.
- Service-to-service communication now relies on Kubernetes Service DNS instead of a separate discovery server.
