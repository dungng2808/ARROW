# ==============================================================================
# install-java-versions.ps1
#
# Idempotent installer for historical JDKs (8, 11, 17, 21) on Windows x64
# Targets: windows-x64
#
# Adheres strictly to Java-version/AGENTS.md:
# 1. Auto-detects OS and architecture (Windows 64-bit).
# 2. Downloads only artifacts matching windows-x64 from java-versions.lock.json.
# 3. Verifies SHA-256 BEFORE extraction.
# 4. Verifies java -version and javac -version (exit code 0 and correct major version).
# 5. Generates/updates preflight_tool/config.local.toml with java.homes mappings (forward slashes).
# 6. Deletes raw archive in .cache/ upon successful verification; retains on failure.
# 7. Operates idempotently (skips download/extraction if already correctly installed).
# ==============================================================================

[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"

# Helper logging functions
function Write-LogInfo($msg) {
    Write-Host "[INFO] $msg" -ForegroundColor Cyan
}

function Write-LogSuccess($msg) {
    Write-Host "[SUCCESS] $msg" -ForegroundColor Green
}

function Write-LogWarn($msg) {
    Write-Host "[WARN] $msg" -ForegroundColor Yellow
}

function Write-LogError($msg) {
    Write-Host "[ERROR] $msg" -ForegroundColor Red
}

# 1. Platform detection
$isWin = $false
if ($PSVersionTable.PSEdition -eq "Core") {
    $isWin = $IsWindows
} else {
    $isWin = ([System.Environment]::OSVersion.Platform -eq [System.PlatformID]::Win32NT)
}

if (-not $isWin) {
    Write-LogError "This script is designed for Windows. On macOS, please run: bash Java-version/scripts/install-java-versions.sh"
    exit 1
}

$is64Bit = [System.Environment]::Is64BitOperatingSystem
if (-not $is64Bit) {
    Write-LogError "Only 64-bit Windows is supported (windows-x64)."
    exit 1
}

$Platform = "windows-x64"
Write-LogInfo "Detected platform: $Platform"

# 2. Locate paths
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$JavaVersionDir = (Get-Item (Join-Path $ScriptDir "..")).FullName
$RepoRoot = (Get-Item (Join-Path $JavaVersionDir "..")).FullName

$LockFile = Join-Path $JavaVersionDir "java-versions.lock.json"
$CacheDir = Join-Path $JavaVersionDir ".cache"
$RuntimeBaseDir = Join-Path $JavaVersionDir "runtime\windows-x64"
$ReportFile = Join-Path $JavaVersionDir "installed-report.json"
$PreflightConfig = Join-Path $RepoRoot "preflight_tool\config.local.toml"
$PreflightExample = Join-Path $RepoRoot "preflight_tool\config.example.toml"

if (-not (Test-Path -LiteralPath $LockFile)) {
    Write-LogError "Lock manifest not found: $LockFile"
    exit 1
}

if (-not (Test-Path -LiteralPath $CacheDir)) {
    New-Item -ItemType Directory -Path $CacheDir -Force | Out-Null
}
if (-not (Test-Path -LiteralPath $RuntimeBaseDir)) {
    New-Item -ItemType Directory -Path $RuntimeBaseDir -Force | Out-Null
}

# 3. Read lock manifest
$LockJson = Get-Content -LiteralPath $LockFile -Raw -Encoding UTF8 | ConvertFrom-Json
$PlatArtifacts = $LockJson.platforms."$Platform"
if ($null -eq $PlatArtifacts) {
    Write-LogError "Platform $Platform configuration not found in lock manifest!"
    exit 1
}

# Function to parse major version from output text
function Get-MajorVersion([string]$text) {
    if ($text -match '(?:version\s+"?|javac\s+)(?:1\.)?(\d+)') {
        return $Matches[1]
    }
    return $null
}

# Function to verify JDK installation
function Test-JdkInstallation([string]$candidateHome, [string]$expectedMajor) {
    $javaBin = Join-Path $candidateHome "bin\java.exe"
    $javacBin = Join-Path $candidateHome "bin\javac.exe"

    if (-not (Test-Path -LiteralPath $javaBin) -or -not (Test-Path -LiteralPath $javacBin)) {
        return $null
    }

    $javaOut = & $javaBin -version 2>&1 | Out-String
    if ($LASTEXITCODE -ne 0) {
        return $null
    }

    $javacOut = & $javacBin -version 2>&1 | Out-String
    if ($LASTEXITCODE -ne 0) {
        return $null
    }

    $parsedJava = Get-MajorVersion $javaOut
    $parsedJavac = Get-MajorVersion $javacOut

    if ($parsedJava -ne $expectedMajor -or $parsedJavac -ne $expectedMajor) {
        return $null
    }

    $firstLine = ($javaOut -split "`r?`n")[0].Trim()
    return $firstLine
}

$Majors = @("8", "11", "17", "21")
$VerifiedHomes = @{}
$VerifiedStrings = @{}

Write-LogInfo "Preparing JDKs 8, 11, 17, 21 for $Platform..."

foreach ($major in $Majors) {
    $artifact = $PlatArtifacts."$major"
    if ($null -eq $artifact) {
        Write-LogError "Artifact for JDK $major not defined in lock manifest!"
        exit 1
    }

    $targetDir = Join-Path $RuntimeBaseDir "jdk-$major"
    $relPath = $artifact.java_home_relative_path
    if ([string]::IsNullOrWhiteSpace($relPath)) {
        $javaHome = $targetDir
    } else {
        $javaHome = Join-Path $targetDir $relPath
    }

    # Check if already installed and valid (Idempotency)
    if (Test-Path -LiteralPath $javaHome) {
        $verString = Test-JdkInstallation $javaHome $major
        if ($null -ne $verString) {
            Write-LogSuccess "JDK $major is already installed and verified at: $javaHome ($verString)"
            $VerifiedHomes[$major] = $javaHome
            $VerifiedStrings[$major] = $verString
            continue
        } else {
            Write-LogWarn "Existing JDK $major at $targetDir failed verification. Reinstalling..."
        }
    }

    # Need download and extraction
    $downloadUrl = $artifact.download_url
    $expectedSha256 = $artifact.sha256.ToLowerInvariant()
    $archiveName = [System.IO.Path]::GetFileName($downloadUrl)
    $archivePath = Join-Path $CacheDir $archiveName

    Write-LogInfo "Downloading JDK $major ($($artifact.version), $($artifact.vendor))..."
    Write-LogInfo "URL: $downloadUrl"

    $needDownload = $true
    if (Test-Path -LiteralPath $archivePath) {
        $existingHash = (Get-FileHash -LiteralPath $archivePath -Algorithm SHA256).Hash.ToLowerInvariant()
        if ($existingHash -eq $expectedSha256) {
            Write-LogInfo "Valid cached archive found: $archiveName"
            $needDownload = $false
        } else {
            Write-LogWarn "Cached archive hash mismatch. Removing stale file: $archiveName"
            Remove-Item -LiteralPath $archivePath -Force
        }
    }

    if ($needDownload) {
        $tempDownload = "$archivePath.tmp"
        if (Test-Path -LiteralPath $tempDownload) {
            Remove-Item -LiteralPath $tempDownload -Force
        }
        # Use System.Net.WebClient for reliable progress and download on PS 5.1/7
        $webClient = New-Object System.Net.WebClient
        try {
            $webClient.DownloadFile($downloadUrl, $tempDownload)
            Move-Item -LiteralPath $tempDownload -Destination $archivePath -Force
        } finally {
            $webClient.Dispose()
        }
    }

    # Verify SHA-256 BEFORE extraction
    Write-LogInfo "Verifying SHA-256 for $archiveName..."
    $actualSha256 = (Get-FileHash -LiteralPath $archivePath -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actualSha256 -ne $expectedSha256) {
        Write-LogError "SHA-256 verification failed for $archiveName!"
        Write-LogError "Expected: $expectedSha256"
        Write-LogError "Actual:   $actualSha256"
        Write-LogError "Retaining raw archive in $CacheDir for diagnosis as required by AGENTS.md."
        exit 1
    }
    Write-LogSuccess "SHA-256 verified successfully: $actualSha256"

    # Extract to target directory
    Write-LogInfo "Extracting $archiveName to $targetDir..."
    $tempExtractDir = Join-Path $CacheDir "tmp_extract_jdk_$major"
    if (Test-Path -LiteralPath $tempExtractDir) {
        Remove-Item -LiteralPath $tempExtractDir -Recurse -Force
    }
    New-Item -ItemType Directory -Path $tempExtractDir -Force | Out-Null

    # Extract zip archive
    Expand-Archive -LiteralPath $archivePath -DestinationPath $tempExtractDir -Force

    # Find the top-level directory in the extracted archive
    $extractedItems = Get-ChildItem -LiteralPath $tempExtractDir
    $sourceDir = $tempExtractDir
    if ($extractedItems.Count -eq 1 -and $extractedItems[0].PSIsContainer) {
        $sourceDir = $extractedItems[0].FullName
    }

    if (Test-Path -LiteralPath $targetDir) {
        Remove-Item -LiteralPath $targetDir -Recurse -Force
    }
    New-Item -ItemType Directory -Path $targetDir -Force | Out-Null

    # Move contents into target directory
    Get-ChildItem -LiteralPath $sourceDir | ForEach-Object {
        Move-Item -LiteralPath $_.FullName -Destination $targetDir -Force
    }

    # Clean up temp extract folder
    Remove-Item -LiteralPath $tempExtractDir -Recurse -Force -ErrorAction SilentlyContinue

    # Verify extracted binaries
    Write-LogInfo "Verifying extracted JDK $major binaries (java.exe and javac.exe)..."
    $verString = Test-JdkInstallation $javaHome $major
    if ($null -ne $verString) {
        Write-LogSuccess "Verified JDK $major successfully ($verString)"
        $VerifiedHomes[$major] = $javaHome
        $VerifiedStrings[$major] = $verString

        # Delete raw archive from .cache/ on successful verification (Rule 6)
        Write-LogInfo "Deleting raw archive from cache: $archivePath"
        Remove-Item -LiteralPath $archivePath -Force
    } else {
        Write-LogError "Verification failed for JDK $major after extraction!"
        Write-LogError "Java binaries at $javaHome did not pass version check."
        Write-LogError "Retaining raw archive at $archivePath for diagnosis."
        exit 1
    }
}

# 4. Generate or update preflight_tool/config.local.toml
Write-LogInfo "Configuring preflight_tool local settings..."

# Format paths with forward slashes as required for TOML compatibility
$homesTomlLines = @("[java.homes]")
foreach ($m in ("8", "11", "17", "21")) {
    $normPath = $VerifiedHomes[$m] -replace '\\', '/'
    $homesTomlLines += "`"$m`" = `"$normPath`""
}
$homesToml = ($homesTomlLines -join "`n") + "`n"

if (Test-Path -LiteralPath $PreflightConfig) {
    $content = Get-Content -LiteralPath $PreflightConfig -Raw -Encoding UTF8
    if ($content -match '\[java\.homes\]') {
        $content = [regex]::Replace($content, '\[java\.homes\][^\[]*', "$homesToml`n")
    } elseif ($content -match '\[java\]') {
        $content = [regex]::Replace($content, '(\[java\][^\n]*\r?\n(?:[^\n\[]*\r?\n)*)', "`$1`n$homesToml`n")
    } else {
        $content += "`n[java]`ndefault_home = `"`"`n`n$homesToml"
    }
    Set-Content -LiteralPath $PreflightConfig -Value $content -Encoding UTF8
    Write-LogInfo "Updated existing $PreflightConfig"
} else {
    if (Test-Path -LiteralPath $PreflightExample) {
        $content = Get-Content -LiteralPath $PreflightExample -Raw -Encoding UTF8
        $pattern = '\[java\.homes\](?:\s*#[^\r\n]*)*'
        if ($content -match $pattern) {
            $content = [regex]::Replace($content, $pattern, $homesToml.Trim())
        } else {
            $content += "`n$homesToml"
        }
        Set-Content -LiteralPath $PreflightConfig -Value $content -Encoding UTF8
        Write-LogInfo "Created $PreflightConfig from template"
    } else {
        $content = "[run]`nid = `"auto`"`nworkers = 2`n`n[java]`ndefault_home = `"`"`n`n$homesToml"
        Set-Content -LiteralPath $PreflightConfig -Value $content -Encoding UTF8
        Write-LogInfo "Created $PreflightConfig"
    }
}

# 5. Write local verification report
$reportObj = [PSCustomObject]@{
    platform = $Platform
    timestamp = (Get-Date).ToUniversalTime().ToString("o")
    verified_jdks = [PSCustomObject]@{
        "8" = [PSCustomObject]@{ java_home = ($VerifiedHomes["8"] -replace '\\', '/'); version_string = $VerifiedStrings["8"]; status = "VERIFIED" }
        "11" = [PSCustomObject]@{ java_home = ($VerifiedHomes["11"] -replace '\\', '/'); version_string = $VerifiedStrings["11"]; status = "VERIFIED" }
        "17" = [PSCustomObject]@{ java_home = ($VerifiedHomes["17"] -replace '\\', '/'); version_string = $VerifiedStrings["17"]; status = "VERIFIED" }
        "21" = [PSCustomObject]@{ java_home = ($VerifiedHomes["21"] -replace '\\', '/'); version_string = $VerifiedStrings["21"]; status = "VERIFIED" }
    }
}
$reportObj | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $ReportFile -Encoding UTF8

Write-Host ""
Write-Host "======================================================" -ForegroundColor Green
Write-Host "       JDK SETUP COMPLETED AND VERIFIED" -ForegroundColor Green
Write-Host "======================================================" -ForegroundColor Green
Write-Host "Platform: $Platform"
foreach ($m in $Majors) {
    Write-Host "  - JDK $m : $($VerifiedHomes[$m])"
    Write-Host "    Version: $($VerifiedStrings[$m])"
}
Write-Host "Generated config: $PreflightConfig"
Write-Host "Verification report: $ReportFile"
Write-Host ""
