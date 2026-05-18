$ErrorActionPreference = "Continue"

$namespace = "micro-marketplace"

Write-Host "=== Kubernetes Verification for $namespace ==="

Write-Host "`n1. Pod Status:"
kubectl get pods -n $namespace

Write-Host "`n2. Service Status:"
kubectl get svc -n $namespace

Write-Host "`n3. Checking Image Imports (K3s via sudo k3s ctr images ls, adapt if using different backend):"
Try {
    $images = docker images --format "{{.Repository}}:{{.Tag}}" | Select-String "local"
    Write-Host "Local Docker Images built:"
    $images
} Catch {
    Write-Host "Could not query docker images."
}

Write-Host "`n4. Waiting for all app pods to become ready (Timeout: 180s)..."
kubectl wait --for=condition=ready pod -l app -n $namespace --timeout=180s 

Write-Host "`n5. Displaying Ingress resources:"
kubectl get ingress -n $namespace

Write-Host "`nVerification complete."
Write-Host "If any pods failed, use 'kubectl describe pod <pod-name> -n $namespace' or 'kubectl logs <pod-name> -n $namespace' for detailed troubleshooting."
