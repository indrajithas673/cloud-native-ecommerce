# Deployment Overview

## 1. CI/CD Pipeline

Micro Marketplace uses GitHub Actions for an automated CI/CD pipeline targeting AWS K3s. 

```mermaid
graph TD
    Developer[Developer] -->|Git Push| GitHub[(GitHub Repository)]
    
    subgraph GitHub Actions Pipeline
        Trigger{Trigger: Push to main}
        Trigger --> Job_Validate[Validate Infrastructure]
        Trigger --> Job_Build[Build & Test]
        
        Job_Validate -->|Dry Run Manifests| Validated[Validated K8s YAML]
        
        Job_Build --> UnitTests[Run Unit Tests]
        Job_Build --> Jib[Jib Docker Build]
        Jib --> ECR[(Amazon ECR)]
    end
    
    subgraph AWS Production Environment
        ECR -->|Pull Image| Node_K3s[AWS EC2 / K3s Node]
        Node_K3s --> Kustomize[Kustomize Apply]
        Kustomize --> Deployments[Kubernetes Deployments]
    end

    Job_Build --> Node_K3s
```

## 2. AWS + K3s Deployment Architecture

The application is hosted on a single Amazon EC2 instance running the K3s Kubernetes distribution. Storage, secrets, and image registry are integrated natively with AWS managed services.

```mermaid
graph TD
    Internet((Internet)) -->|Port 80/443| SecurityGroup[AWS Security Group]
    
    subgraph AWS VPC
        SecurityGroup --> EC2[EC2 Instance - t3.medium]
        
        subgraph EC2 Instance
            K3s[K3s Kubernetes Cluster]
            Traefik[Traefik Ingress]
            
            subgraph Micro-Marketplace Namespace
                API[API Gateway Pod]
                Keycloak[Keycloak Pod]
                AppPods[Microservice Pods]
                Kafka[Kafka Pod]
            end
            
            K3s --> Traefik
            Traefik --> API
            Traefik --> Keycloak
            API --> AppPods
            AppPods --> Kafka
        end
    end
    
    EC2 -->|EBS CSI Driver| EBS[(AWS EBS Volumes)]
    EC2 -->|IAM Instance Role| ECR[(Amazon ECR)]
    EC2 -->|Secrets Rotation| SecretsManager[(AWS Secrets Manager)]
    
    AppPods -->|VPC Peering/Routing| RDS[(Amazon RDS MySQL 8.4)]
    Keycloak --> RDS
```
