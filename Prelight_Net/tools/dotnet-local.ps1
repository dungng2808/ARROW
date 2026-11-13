# Giữ cache, log và thư mục tạm trong Prelight_Net.
$ErrorActionPreference = 'Stop'
$preflightRoot = Split-Path -Parent $PSScriptRoot
$env:DOTNET_CLI_HOME = Join-Path $preflightRoot '.work/dotnet-home'
$env:DOTNET_CLI_TELEMETRY_OPTOUT = '1'
$env:DOTNET_SKIP_FIRST_TIME_EXPERIENCE = '1'
$env:DOTNET_GENERATE_ASPNET_CERTIFICATE = 'false'
$env:DOTNET_ADD_GLOBAL_TOOLS_TO_PATH = 'false'
$env:DOTNET_CLI_WORKLOAD_UPDATE_NOTIFY_DISABLE = 'true'
$env:NUGET_PACKAGES = Join-Path $preflightRoot '.nuget/packages'
$env:NUGET_HTTP_CACHE_PATH = Join-Path $preflightRoot '.nuget/http-cache'
$env:NUGET_PLUGINS_CACHE_PATH = Join-Path $preflightRoot '.nuget/plugins-cache'
$env:TEMP = Join-Path $preflightRoot '.work/tmp'
$env:TMP = $env:TEMP
[IO.Directory]::CreateDirectory($env:TEMP) | Out-Null
Push-Location $preflightRoot
try { & dotnet @args; $preflightExit = $LASTEXITCODE } finally { Pop-Location }
exit $preflightExit
