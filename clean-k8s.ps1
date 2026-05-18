$ErrorActionPreference = "Stop"

Write-Host "Deleting Kubernetes resources for micro-marketplace..."
kubectl delete -k k8s/ --ignore-not-found

Write-Host "Cleanup complete."
