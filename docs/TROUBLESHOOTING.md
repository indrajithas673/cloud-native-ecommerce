# Troubleshooting Guide

This guide covers common issues and resolutions when running or deploying the Micro Marketplace application.

## 1. Kubernetes Issues

### ImagePullBackOff or ErrImagePull
**Symptom**: Pods fail to start with `ImagePullBackOff`.
**Cause**: The Kubernetes worker node cannot authenticate with the Amazon ECR registry, or the image tag does not exist.
**Resolution**:
1. Check the CI/CD pipeline to ensure the image was successfully built and pushed.
2. Verify that the ECR Secret (`ecr-secret`) exists in the target namespace: `kubectl get secret ecr-secret`.
3. Check the IAM Instance Profile on the AWS EC2 instance to ensure it has `ecr:GetDownloadUrlForLayer` permissions.

### CrashLoopBackOff (Database Connection Failed)
**Symptom**: Pods repeatedly crash. Logs show `Communications link failure`.
**Cause**: The service cannot reach the Amazon RDS database, or the `cloud-db-secrets` are missing/incorrect.
**Resolution**:
1. Verify the `cloud-db-secrets` exist: `kubectl get secret cloud-db-secrets -o yaml`.
2. Ensure the RDS Security Group allows inbound traffic on port `3306` from the K3s EC2 instance's IP.
3. Check the init-db job logs: `kubectl logs job/init-db`.

## 2. Authentication (Keycloak) Issues

### Keycloak Fails to Start (Access Denied)
**Symptom**: Keycloak logs show `Access denied for user 'ibatulanand'`.
**Cause**: Keycloak was configured to use the application user rather than the admin user to run database migrations, or the init-db script failed to create the correct user.
**Resolution**:
Ensure `KC_DB_USERNAME=keycloak` is set in `infra.yaml` and that the `init-db` job created the `keycloak` user on the RDS instance.

### 401 Unauthorized at API Gateway
**Symptom**: API requests return `401 Unauthorized` despite providing a token.
**Cause**: The API Gateway cannot reach Keycloak to download the JWKS public keys.
**Resolution**:
Verify the `spring.security.oauth2.resourceserver.jwt.issuer-uri` in `api-gateway` configuration points to a resolvable Keycloak URL (e.g., internal Kubernetes service name `http://keycloak/realms/spring-boot-microservices-realm`).

## 3. Resilience / Circuit Breaker Issues

### "Oops! Something went wrong" returned immediately
**Symptom**: The Order API instantly returns the fallback message without attempting to wait for a timeout.
**Cause**: The Resilience4j Circuit Breaker is in the `OPEN` state.
**Resolution**:
This is expected behavior if the downstream `inventory-service` has been failing. Check the health of `inventory-service`. Once it becomes healthy, Resilience4j will transition to `HALF_OPEN` and eventually `CLOSED` to resume normal operations.

### Silent 500 Errors instead of Fallback
**Symptom**: The Order API returns a generic 500 server error instead of the friendly fallback message when inventory is down.
**Cause**: The fallback method signature doesn't match the primary method, or the AOP proxy wasn't triggered (e.g., calling an internal method using `this.method()`).
**Resolution**:
Ensure `OrderService` uses self-injection (`@Lazy private OrderService self`) to invoke `@CircuitBreaker` annotated methods internally.

## 4. Distributed Tracing Issues

### Traces not showing in Zipkin
**Symptom**: Zipkin UI is empty or missing downstream spans.
**Cause**: The `ZIPKIN_HOST` environment variable is not set correctly in the Pods.
**Resolution**:
Verify the `platform-config` ConfigMap contains the correct `ZIPKIN_HOST` and that the Spring Boot applications have restarted to pick up the changes.
