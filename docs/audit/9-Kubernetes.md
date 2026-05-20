# Kubernetes Architecture

This document details the Kubernetes deployment architecture defined in the `k8s/` directory. The deployment is managed using **Kustomize** for configuration assembly.

## Namespace
- **Name**: `micro-marketplace` (`namespace.yaml`). All resources are isolated within this namespace to prevent collisions with other cluster applications.

## Kustomize Setup (`kustomization.yaml`)
- **Generator**: Uses `configMapGenerator` to create `deployment-values` from Terraform's `runtime-values.env`, and `keycloak-realm-import` from `spring-boot-microservices-realm.json`.
- **Replacements**: Dynamically injects Terraform-provisioned IP addresses and DNS records (e.g., `API_HOST`, `KEYCLOAK_ISSUER_URI`) into the Ingress rules and application configurations (`platform-config`).

## Infrastructure Resources (`infra.yaml` & `storage.yaml`)

### Storage
- **PersistentVolumeClaims (PVC)**: Two PVCs (`mysql-pvc`, `kafka-pvc`) are created with `accessModes: ReadWriteOnce` and `2Gi` capacity each. This ensures database and event stream durability across pod restarts.

### Deployments & Services
1. **MySQL**: 
   - Uses `mysql:latest`. Mounts `mysql-pvc`. 
   - Defines databases via `MYSQL_DATABASE` and sets up the root password via `platform-secrets`.
2. **Kafka**: 
   - Uses `apache/kafka:latest` in KRaft mode. 
   - Mounts `kafka-pvc`.
3. **Keycloak**: 
   - Uses `quay.io/keycloak/keycloak:latest`.
   - Mounts the `keycloak-realm-import` ConfigMap to seed the identity provider configuration on startup.

## Application Resources (`apps.yaml`)

Each Spring Boot microservice (`api-gateway`, `product-service`, `order-service`, `inventory-service`, `notification-service`) has a standard structure:

### Deployment Specs
- **Image**: `local/<service>:dev`
- **ImagePullPolicy**: `IfNotPresent` (Optimized for local Docker Desktop / K3s caches).
- **Environment Variables**: Dynamically linked to `platform-config` (ConfigMap) and `platform-secrets` (Secret) using `valueFrom`.

### Resource Management
- **Requests/Limits**: Defined for predictable scheduling.
  - *Example (`order-service`)*: Requests (CPU: 100m, Mem: 384Mi), Limits (CPU: 750m, Mem: 768Mi).
- **Probes**:
  - **Readiness Probe**: Queries `/actuator/health` to ensure the application is ready to receive traffic (delays traffic routing until true).
  - **Liveness Probe**: Restarts the pod if it becomes deadlocked.
  - **Startup Probe**: Handles slow JVM startup times (e.g., `failureThreshold: 30`, `periodSeconds: 10`).

## Advanced Kubernetes Features

### Network Policies (`network-policy.yaml`)
- Restricts ingress traffic to the MySQL pod. It exclusively permits traffic from pods labeled `app: product-service`, `app: order-service`, or `app: inventory-service`. This is a strong zero-trust security measure.

### Horizontal Pod Autoscaler (`hpa.yaml`)
- Configured for the `product-service`. 
- Scales between 1 and 3 replicas based on CPU utilization exceeding 70%.

### Ingress (`ingress.yaml`)
- Exposes the cluster to external traffic.
- Routes `api.*.nip.io` to the `api-gateway` on port 80.
- Routes `auth.*.nip.io` to `keycloak` on port 8080.

## Operational Considerations
- The architecture is highly robust. Resource limits prevent noisy-neighbor issues, Probes ensure traffic is only routed to healthy JVMs, and PVCs maintain critical state.
- **Service Discovery**: The application utilizes native Kubernetes DNS. For example, `ORDER_SERVICE_URL=http://order-service:8080`.
