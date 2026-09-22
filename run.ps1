$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "==========================================================" -ForegroundColor Green
Write-Host "Starting Flowra Spring Boot Backend..." -ForegroundColor Green
Write-Host "JAVA_HOME = $env:JAVA_HOME" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Green

Set-Location $PSScriptRoot
& .\mvnw.cmd spring-boot:run
