# Technical Interview Guide

This guide prepares you to confidently discuss the `ecommerce-microservices-master` project in senior engineering interviews.

## 2-Minute Elevator Pitch
"I built a cloud-native, microservices-based e-commerce backend using Java 17 and Spring Boot 3. The architecture decomposes the domain into isolated Product, Order, and Inventory services, all shielded by a Spring Cloud API Gateway with Keycloak handling OAuth2 authentication. I implemented a hybrid communication model: synchronous HTTP via WebClient with Resilience4J circuit breakers for strict stock validation, and asynchronous event-driven notifications via Kafka (KRaft mode). The entire stack is containerized with Jib, deployed to a Kubernetes cluster using Kustomize, and provisioned on AWS using Terraform."

## 5-Minute Architecture Deep Dive
- **Entrypoint**: Traffic hits an Nginx Ingress Controller mapped via wildcard DNS (`nip.io`), forwarding to the API Gateway.
- **Security**: The Gateway validates the JWT signatures against Keycloak.
- **Data Persistence**: I adhered to the Database-per-Service pattern. Each Spring Boot pod connects to its own isolated MySQL instance (backed by K8s PVCs).
- **Service Mesh / Orchestration**: No heavy service mesh. Relying purely on Kubernetes internal DNS (`http://inventory-service:8080`) for discovery, omitting Eureka.
- **Resilience**: The critical path (Order -> Inventory) is protected by `@CircuitBreaker`, `@Retry`, and `@TimeLimiter`. If Inventory is down, Orders fail fast rather than exhausting connection pools.

## Major Design Decisions & Trade-offs

1. **Why Kafka in KRaft mode?**
   - *Reason*: ZooKeeper adds operational complexity, requires separate JVMs, and slows down partition leader elections. KRaft embeds the Raft consensus protocol directly in Kafka brokers.
   - *Trade-off*: KRaft is newer, but stable enough for modern deployments, significantly reducing the container footprint in my K3s cluster.

2. **Why WebClient instead of RestTemplate/Feign?**
   - *Reason*: `RestTemplate` is in maintenance mode. `WebClient` provides a modern, non-blocking, reactive API. Even though the Order Service isn't fully reactive (WebFlux), `WebClient` is the Spring-recommended standard for HTTP calls.

3. **Why Kustomize instead of Helm?**
   - *Reason*: For this project size, Helm's Go-templating can be overkill. Kustomize allows me to keep raw, readable Kubernetes YAMLs and patch them dynamically based on environments.

## Top 5 Project-Specific Interview Questions

### Q1: How do you handle a scenario where the Inventory Service goes down during high traffic?
**Answer**: I implemented the Circuit Breaker pattern using Resilience4J in the `OrderController`. If the Inventory Service times out or throws continuous errors, the circuit trips to "OPEN" state. Subsequent requests immediately execute the `fallbackMethod` returning a friendly error string without even attempting the network call, protecting the Order Service's thread pool from exhaustion.

### Q2: How do you ensure data consistency between the MySQL database and Kafka?
**Answer**: *Be honest about current technical debt.* "Currently, `OrderService.java` performs a dual-write: it calls `orderRepository.save()` and then `kafkaTemplate.send()`. This is a known risk. If the JVM crashes between those two lines, the order exists but the event is lost. To fix this for production, I plan to implement the Transactional Outbox pattern, where the event is saved to an `outbox` table in the same DB transaction, and a CDC tool like Debezium streams it to Kafka."

### Q3: Why did you drop Netflix Eureka from the architecture?
**Answer**: "In a Kubernetes environment, Eureka is redundant. Kubernetes provides native service discovery via CoreDNS (`ClusterIP` services). By dropping Eureka, I removed an entire infrastructure component, reduced the memory footprint, and simplified the architecture."

### Q4: Explain how security is enforced. Can someone bypass the Gateway and access the Order Service directly?
**Answer**: "Externally, no. The API Gateway acts as an OAuth2 Resource Server and enforces JWT validation. Internally, if an attacker gains access to the Kubernetes cluster, they *could* call the Order Service directly because the downstream services don't currently validate the token. However, I mitigated internal lateral movement by implementing Kubernetes Network Policies (`network-policy.yaml`) that strictly restrict which pods can communicate with the databases."

### Q5: How do you manage database schemas across these microservices?
**Answer**: "Currently, the project leverages Hibernate's `ddl-auto=update` for rapid prototyping. However, I understand this is an anti-pattern for production. My roadmap includes migrating all services to Flyway to ensure deterministic, version-controlled schema migrations."
