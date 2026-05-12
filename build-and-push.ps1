$ErrorActionPreference = "Stop"

Write-Host "Getting ECR Password..."
$ECR_PASSWORD = aws ecr get-login-password --region eu-north-1
$REGISTRY = "035466343449.dkr.ecr.eu-north-1.amazonaws.com/ecommerce-microservices"

Write-Host "Building and pushing with Jib..."
cmd.exe /c "mvn clean compile jib:build -Ddocker.image.prefix=$REGISTRY -Ddocker.image.tag=latest -Djib.to.auth.username=AWS -Djib.to.auth.password=$ECR_PASSWORD -DskipTests"

Write-Host "Done!"
