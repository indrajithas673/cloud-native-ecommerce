$ErrorActionPreference = "Stop"

$PUBLIC_IP = "13.61.61.209"
$KEY_PATH = "./terraform/ecommerce-key.pem"
$KUBECONFIG_AWS = "./kubeconfig-aws.yaml"

Write-Host "Fetching kubeconfig from EC2 instance ($PUBLIC_IP)..."
# In a real environment, you would use SSH/SCP:
# scp -i $KEY_PATH -o StrictHostKeyChecking=no ubuntu@${PUBLIC_IP}:/etc/rancher/k3s/k3s.yaml $KUBECONFIG_AWS

# If SCP is blocked but the kubeconfig is exposed temporarily via user-data script:
# Invoke-WebRequest -Uri "http://${PUBLIC_IP}/config" -OutFile $KUBECONFIG_AWS -UseBasicParsing

Write-Host "Updating kubeconfig to use public IP..."
$configContent = Get-Content $KUBECONFIG_AWS
$configContent = $configContent -replace "127.0.0.1", $PUBLIC_IP
Set-Content $KUBECONFIG_AWS $configContent

Write-Host "Validating K3s connection..."
kubectl --kubeconfig $KUBECONFIG_AWS get nodes

Write-Host "Creating platform-secrets from .env file..."
if (-not (Test-Path .env)) {
    Write-Host "WARNING: .env not found! Generating a local one from .env.example for manual testing."
    Copy-Item .env.example .env
}
kubectl --kubeconfig $KUBECONFIG_AWS create namespace micro-marketplace --dry-run=client -o yaml | kubectl --kubeconfig $KUBECONFIG_AWS apply -f -
kubectl --kubeconfig $KUBECONFIG_AWS create secret generic platform-secrets -n micro-marketplace --from-env-file=.env --dry-run=client -o yaml | kubectl --kubeconfig $KUBECONFIG_AWS apply -f -

Write-Host "Deploying infrastructure to AWS K3s..."
kubectl --kubeconfig $KUBECONFIG_AWS apply -k k8s/overlays/aws

Write-Host "Deploying Observability Stack (Helm)..."
.\helm.exe --kubeconfig $KUBECONFIG_AWS repo add prometheus-community https://prometheus-community.github.io/helm-charts
.\helm.exe --kubeconfig $KUBECONFIG_AWS repo update
.\helm.exe --kubeconfig $KUBECONFIG_AWS upgrade --install prometheus prometheus-community/kube-prometheus-stack -n micro-marketplace `
    --set grafana.adminPassword=admin `
    --set grafana.ingress.enabled=true `
    --set grafana.ingress.hosts[0]=grafana.${PUBLIC_IP}.nip.io `
    --set prometheus.ingress.enabled=true `
    --set prometheus.ingress.hosts[0]=prometheus.${PUBLIC_IP}.nip.io `
    --set prometheus-node-exporter.enabled=false `
    --timeout 5m

Write-Host "Waiting for pods to be ready..."
kubectl --kubeconfig $KUBECONFIG_AWS wait --for=condition=Ready pods --all -n micro-marketplace --timeout=300s

Write-Host "Deployment to AWS completed successfully!"
Write-Host "API Gateway: http://api.${PUBLIC_IP}.nip.io"
Write-Host "Keycloak: http://auth.${PUBLIC_IP}.nip.io"
Write-Host "Prometheus: http://prometheus.${PUBLIC_IP}.nip.io"
Write-Host "Grafana: http://grafana.${PUBLIC_IP}.nip.io"
