$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot 'backend')
mvn spring-boot:run '-Dspring-boot.run.profiles=demo'
