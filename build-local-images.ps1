$ErrorActionPreference = "Stop"

Write-Host "Building api-gateway image..."
mvn compile jib:dockerBuild -Dimage=local/api-gateway:dev -pl api-gateway
if ($LASTEXITCODE -ne 0) { throw "Build failed for api-gateway" }

Write-Host "Building product-service image..."
mvn compile jib:dockerBuild -Dimage=local/product-service:dev -pl product-service
if ($LASTEXITCODE -ne 0) { throw "Build failed for product-service" }

Write-Host "Building order-service image..."
mvn compile jib:dockerBuild -Dimage=local/order-service:dev -pl order-service
if ($LASTEXITCODE -ne 0) { throw "Build failed for order-service" }

Write-Host "Building inventory-service image..."
mvn compile jib:dockerBuild -Dimage=local/inventory-service:dev -pl inventory-service
if ($LASTEXITCODE -ne 0) { throw "Build failed for inventory-service" }

Write-Host "Building notification-service image..."
mvn compile jib:dockerBuild -Dimage=local/notification-service:dev -pl notification-service
if ($LASTEXITCODE -ne 0) { throw "Build failed for notification-service" }

Write-Host "All images built successfully."
