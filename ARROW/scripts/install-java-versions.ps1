param(
  [int[]]$Versions = @(8, 11, 17, 21),
  [string]$Destination = "",
  [switch]$Force
)

$ErrorActionPreference = "Stop"

function Write-Step {
  param([string]$Message)
  Write-Host ""
  Write-Host "==> $Message" -ForegroundColor Cyan
}

function Resolve-RepoRoot {
  $scriptPath = $PSCommandPath
  if (-not $scriptPath) {
    return (Get-Location).Path
  }
  return (Resolve-Path (Join-Path (Split-Path -Parent $scriptPath) "..")).Path
}

function Test-JavaHome {
  param([string]$Path)
  return (Test-Path -LiteralPath (Join-Path $Path "bin\java.exe"))
}

function Get-ArchiveName {
  param([int]$Version)
  return "temurin-jdk-$Version-windows-x64.zip"
}

function Get-AdoptiumUrl {
  param([int]$Version)
  return "https://api.adoptium.net/v3/binary/latest/$Version/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk"
}

function Install-JdkVersion {
  param(
    [int]$Version,
    [string]$Root,
    [string]$Cache
  )

  $target = Join-Path $Root "jdk-$Version"
  if ((Test-JavaHome $target) -and -not $Force) {
    Write-Host "JDK $Version already exists: $target" -ForegroundColor Green
    return $target
  }

  if ((Test-Path -LiteralPath $target) -and $Force) {
    Write-Step "Removing existing JDK $Version because -Force was set"
    Remove-Item -LiteralPath $target -Recurse -Force
  }

  $archive = Join-Path $Cache (Get-ArchiveName $Version)
  $url = Get-AdoptiumUrl $Version

  if (-not (Test-Path -LiteralPath $archive)) {
    Write-Step "Downloading JDK $Version"
    Write-Host $url
    Invoke-WebRequest -Uri $url -OutFile $archive -MaximumRedirection 10
  } else {
    Write-Host "Using cached archive: $archive" -ForegroundColor DarkGray
  }

  $extractRoot = Join-Path $Cache "extract-$Version"
  if (Test-Path -LiteralPath $extractRoot) {
    Remove-Item -LiteralPath $extractRoot -Recurse -Force
  }
  New-Item -ItemType Directory -Path $extractRoot | Out-Null

  Write-Step "Extracting JDK $Version"
  Expand-Archive -LiteralPath $archive -DestinationPath $extractRoot -Force

  $jdkHome = Get-ChildItem -LiteralPath $extractRoot -Directory -Recurse |
    Where-Object { Test-JavaHome $_.FullName } |
    Select-Object -First 1

  if (-not $jdkHome) {
    throw "Cannot find bin\java.exe after extracting JDK $Version from $archive"
  }

  Move-Item -LiteralPath $jdkHome.FullName -Destination $target
  Remove-Item -LiteralPath $extractRoot -Recurse -Force

  if (-not (Test-JavaHome $target)) {
    throw "Installed JDK $Version but bin\java.exe is missing: $target"
  }

  Write-Host "Installed JDK $Version -> $target" -ForegroundColor Green
  return $target
}

$repoRoot = Resolve-RepoRoot
if (-not $Destination) {
  $Destination = Join-Path $repoRoot "Java version"
}

$Destination = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($Destination)
$cacheDir = Join-Path $Destination ".cache"

Write-Step "Preparing destination"
New-Item -ItemType Directory -Path $Destination -Force | Out-Null
New-Item -ItemType Directory -Path $cacheDir -Force | Out-Null
Write-Host "Destination: $Destination"

[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$installed = @{}
foreach ($version in $Versions) {
  if ($version -lt 8) {
    Write-Warning "Skipping Java $version. This script only downloads modern Temurin JDK versions from 8 upward."
    continue
  }
  $installed[$version] = Install-JdkVersion -Version $version -Root $Destination -Cache $cacheDir
}

$mapPath = Join-Path $Destination "java-version-map.txt"
$lines = foreach ($version in ($installed.Keys | Sort-Object)) {
  "java-$version`: $($installed[$version])"
}
$lines | Set-Content -LiteralPath $mapPath -Encoding UTF8

Write-Step "Verifying installed Java versions"
foreach ($version in ($installed.Keys | Sort-Object)) {
  $javaExe = Join-Path $installed[$version] "bin\java.exe"
  Write-Host ""
  Write-Host "java-$version -> $($installed[$version])" -ForegroundColor Yellow
  & $javaExe -version
}

Write-Step "Dashboard JDK version map"
Get-Content -LiteralPath $mapPath

Write-Host ""
Write-Host "Copy the block above into Dashboard -> Tùy chọn nâng cao -> JDK version map." -ForegroundColor Green
Write-Host "If you want this folder as fallback too, set Java default to one of these paths, for example jdk-17." -ForegroundColor Green
