# start-paystream.ps1
# 1) Dev altyapıyı kaldır (Postgres, Kafka, Redis, Keycloak, Weaviate)
Write-Host ">> Bringing up dev infra with Docker Compose..." -ForegroundColor Cyan
docker compose -f infra\docker-compose.dev.yml up -d

# 2) Eureka (service-registry)
Write-Host ">> Starting service-registry (Eureka)..." -ForegroundColor Cyan
Start-Process -NoNewWindow -PassThru powershell -ArgumentList ' -NoLogo -NoProfile -Command "cd ''C:\Users\kayah\Desktop\PayStream''; .\gradlew.bat :platform:service-registry:bootRun"'

Start-Sleep -Seconds 8

# 3) Config Server
Write-Host ">> Starting config-server..." -ForegroundColor Cyan
Start-Process -NoNewWindow -PassThru powershell -ArgumentList ' -NoLogo -NoProfile -Command "cd ''C:\Users\kayah\Desktop\PayStream''; .\gradlew.bat :platform:config-server:bootRun"'

Start-Sleep -Seconds 6

# 4) API Gateway
Write-Host ">> Starting api-gateway..." -ForegroundColor Cyan
Start-Process -NoNewWindow -PassThru powershell -ArgumentList ' -NoLogo -NoProfile -Command "cd ''C:\Users\kayah\Desktop\PayStream''; .\gradlew.bat :platform:api-gateway:bootRun"'

Write-Host "`nAll started. Open:" -ForegroundColor Green
Write-Host "  - Eureka:    http://localhost:8761"
Write-Host "  - Config:    http://localhost:8888/actuator/health"
Write-Host "  - Gateway:   http://localhost:8080/actuator/health"
