# Azure Automated Deployment Script for Community Service (Spring Boot + MySQL Flexible Server)
param (
    [string]$ResourceGroupName = "ams-community-rg",
    [string]$Location = "southeastasia",
    [string]$AppServiceName = "ams-community-service-$(Get-Random -Minimum 1000 -Maximum 9999)",
    [string]$AppServicePlanName = "ams-community-plan",
    [string]$MySqlServerName = "ams-mysql-$(Get-Random -Minimum 1000 -Maximum 9999)",
    [string]$DbAdminUser = "amsadmin",
    [string]$DbAdminPassword = "AmsPassword$(Get-Random -Minimum 10000 -Maximum 99999)!",
    [string]$DbName = "community_db",
    [string]$JarPath = "target/community-service-0.0.1-SNAPSHOT.jar"
)

$ErrorActionPreference = "Stop"
$az = "C:\Program Files\Microsoft SDKs\Azure\CLI2\wbin\az.cmd"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "🚀 Starting Automated Azure Provisioning & Deployment" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Check Azure Login
Write-Host "▶ Step 1: Checking Azure Account..." -ForegroundColor Yellow
$account = & $az account show --output json 2>$null | ConvertFrom-Json
if (-not $account) {
    Write-Host "Please run 'az login' first to authenticate." -ForegroundColor Red
    exit 1
}
Write-Host "✔ Authenticated as: $($account.user.name) (Subscription: $($account.name) [$($account.id)])" -ForegroundColor Green

# 2. Create Resource Group
Write-Host "`n▶ Step 2: Creating Resource Group '$ResourceGroupName' in $Location..." -ForegroundColor Yellow
& $az group create --name $ResourceGroupName --location $Location --output table
Write-Host "✔ Resource Group ready." -ForegroundColor Green

# 3. Create Azure Database for MySQL Flexible Server
Write-Host "`n▶ Step 3: Provisioning Azure Database for MySQL Flexible Server '$MySqlServerName'..." -ForegroundColor Yellow
& $az mysql flexible-server create `
    --resource-group $ResourceGroupName `
    --name $MySqlServerName `
    --location $Location `
    --admin-user $DbAdminUser `
    --admin-password $DbAdminPassword `
    --sku-name Standard_B1ms `
    --tier Burstable `
    --storage-size 20 `
    --version 8.0.21 `
    --public-access 0.0.0.0 `
    --yes `
    --output table

Write-Host "✔ MySQL Server provisioned." -ForegroundColor Green

# 4. Open Firewall for Azure Services
Write-Host "`n▶ Step 4: Configuring MySQL Firewall for Azure Internal Services..." -ForegroundColor Yellow
& $az mysql flexible-server firewall-rule create `
    --resource-group $ResourceGroupName `
    --name $MySqlServerName `
    --rule-name AllowAllAzureIPs `
    --start-ip-address 0.0.0.0 `
    --end-ip-address 0.0.0.0 `
    --output table
Write-Host "✔ Firewall rule configured." -ForegroundColor Green

# 5. Create Database
Write-Host "`n▶ Step 5: Creating database '$DbName'..." -ForegroundColor Yellow
& $az mysql flexible-server db create `
    --resource-group $ResourceGroupName `
    --server-name $MySqlServerName `
    --database-name $DbName `
    --output table
Write-Host "✔ Database created." -ForegroundColor Green

# 6. Create App Service Plan (Linux)
Write-Host "`n▶ Step 6: Creating App Service Plan '$AppServicePlanName'..." -ForegroundColor Yellow
& $az appservice plan create `
    --resource-group $ResourceGroupName `
    --name $AppServicePlanName `
    --is-linux `
    --sku B1 `
    --location $Location `
    --output table
Write-Host "✔ App Service Plan created." -ForegroundColor Green

# 7. Create Web App (Java 21)
Write-Host "`n▶ Step 7: Creating Linux Web App '$AppServiceName' (Java 21)..." -ForegroundColor Yellow
& $az webapp create `
    --resource-group $ResourceGroupName `
    --name $AppServiceName `
    --plan $AppServicePlanName `
    --runtime "JAVA:21-java21" `
    --output table
Write-Host "✔ Web App created." -ForegroundColor Green

# 8. Configure Environment Variables
Write-Host "`n▶ Step 8: Configuring Application Settings..." -ForegroundColor Yellow
$jdbcUrl = "jdbc:mysql://$MySqlServerName.mysql.database.azure.com:3306/$DbName`?useSSL=true&requireSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
& $az webapp config appsettings set `
    --resource-group $ResourceGroupName `
    --name $AppServiceName `
    --settings `
        SPRING_PROFILES_ACTIVE="mysql" `
        SPRING_DATASOURCE_URL="$jdbcUrl" `
        SPRING_DATASOURCE_USERNAME="$DbAdminUser" `
        SPRING_DATASOURCE_PASSWORD="$DbAdminPassword" `
        PORT="8085" `
        SERVER_PORT="8085" `
    --output table
Write-Host "✔ Environment variables configured." -ForegroundColor Green

# 9. Verify JAR existence
if (-not (Test-Path $JarPath)) {
    Write-Host "`nBuilding JAR package..." -ForegroundColor Yellow
    & .\mvnw.cmd clean package -DskipTests
}

# 10. Deploy JAR
Write-Host "`n▶ Step 9: Deploying Spring Boot JAR to Azure App Service..." -ForegroundColor Yellow
& $az webapp deploy `
    --resource-group $ResourceGroupName `
    --name $AppServiceName `
    --src-path $JarPath `
    --type jar `
    --output table

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host "🎉 DEPLOYMENT COMPLETE!" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "App URL: https://$AppServiceName.azurewebsites.net" -ForegroundColor Cyan
Write-Host "Health Check: https://$AppServiceName.azurewebsites.net/actuator/health" -ForegroundColor Cyan
Write-Host "Swagger / API Base: https://$AppServiceName.azurewebsites.net/api/v1/facilities" -ForegroundColor Cyan
Write-Host "MySQL Host: $MySqlServerName.mysql.database.azure.com" -ForegroundColor Cyan
Write-Host "Database Name: $DbName" -ForegroundColor Cyan
