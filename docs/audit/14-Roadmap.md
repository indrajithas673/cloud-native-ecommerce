# Future Development Roadmap

This roadmap outlines a prioritized plan for evolving the repository based on identified technical debt and missing features.

## Phase 1: Critical (Immediate Action Required)

1. **Fix Inventory Deduction Bug**
   - **Why**: Currently, stock is validated but never decremented.
   - **Impacted Files**: `OrderService.java`, `InventoryController.java`, `InventoryService.java`
   - **Complexity**: Medium. Requires creating a new POST/PUT endpoint in Inventory to decrement stock after validation.

2. **Database Migrations (Flyway/Liquibase)**
   - **Why**: `spring.jpa.hibernate.ddl-auto=update` is unsafe for production.
   - **Impacted Files**: Add `flyway-core` to all domain `pom.xml` files. Create `V1__init.sql` scripts in `src/main/resources/db/migration`.
   - **Complexity**: Low.

3. **Global Exception Handling**
   - **Why**: Prevent stack traces from leaking to API clients.
   - **Impacted Files**: Create `GlobalExceptionHandler.java` (using `@RestControllerAdvice`) in all domain services.
   - **Complexity**: Low.

## Phase 2: High (Data Integrity & Validation)

4. **Transactional Outbox Pattern**
   - **Why**: Fixes the Dual-Write vulnerability where a Kafka outage causes lost notifications despite a successful order DB commit.
   - **Impacted Files**: `OrderService.java`, `Order.java` (add `outbox` table), new `OutboxScheduler.java`.
   - **Complexity**: High.

5. **Input Validation**
   - **Why**: Prevent bad data (negative prices, empty strings).
   - **Impacted Files**: Add `spring-boot-starter-validation`. Update `ProductRequest.java`, `OrderRequest.java`, and add `@Valid` to Controllers.
   - **Complexity**: Low.

## Phase 3: Medium (Security & Operations)

6. **User Identity Context**
   - **Why**: Orders are currently anonymous.
   - **Impacted Files**: `SecurityConfig.java`, `OrderService.java`. Extract the `sub` (subject/userId) claim from the JWT at the API Gateway, pass it via an HTTP header (e.g., `X-User-Id`), and save it to the `Order` entity.
   - **Complexity**: Medium.

7. **Secret Management**
   - **Why**: Hardcoded base64 secrets in Kustomize.
   - **Impacted Files**: Remove `secrets.yaml`. Integrate HashiCorp Vault or AWS Secrets Manager Operator into the Kubernetes cluster.
   - **Complexity**: High.

## Phase 4: Optional (Advanced Features)

8. **Distributed Tracing UI (Zipkin/Jaeger)**
   - **Why**: Micrometer is producing trace IDs, but there is no UI to visualize the request span across the gateway, order, and inventory services.
   - **Impacted Files**: `docker-compose.yml`, `k8s/apps.yaml`.
   - **Complexity**: Medium.

9. **Service Mesh (Istio)**
   - **Why**: To provide mTLS encryption for intra-cluster traffic between the API Gateway and backend services.
   - **Impacted Files**: `k8s/` manifests (add Istio sidecar injection labels).
   - **Complexity**: High.
