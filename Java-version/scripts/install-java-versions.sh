#!/usr/bin/env bash
# ==============================================================================
# install-java-versions.sh
#
# Idempotent installer for historical JDKs (8, 11, 17, 21) on macOS
# Targets: darwin-aarch64 (Apple Silicon) & darwin-x64 (Intel)
# Compatible with macOS default Bash 3.2+ and zsh
#
# Adheres strictly to Java-version/AGENTS.md:
# 1. Auto-detects OS and CPU architecture.
# 2. Downloads only artifacts matching the current machine from java-versions.lock.json.
# 3. Verifies SHA-256 BEFORE extraction.
# 4. Verifies java -version and javac -version (both must exit 0 and match major version).
# 5. Generates/updates preflight_tool/config.local.toml with java.homes mappings.
# 6. Deletes raw archive in .cache/ upon successful verification; retains on failure.
# 7. Operates idempotently (skips download/extraction if already correctly installed).
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAVA_VERSION_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
REPO_ROOT="$(cd "$JAVA_VERSION_DIR/.." && pwd)"

LOCK_FILE="$JAVA_VERSION_DIR/java-versions.lock.json"
CACHE_DIR="$JAVA_VERSION_DIR/.cache"
RUNTIME_BASE_DIR="$JAVA_VERSION_DIR/runtime"
REPORT_FILE="$JAVA_VERSION_DIR/installed-report.json"
STATE_FILE="$CACHE_DIR/installed_jdks.tmp"
PREFLIGHT_CONFIG="$REPO_ROOT/preflight_tool/config.local.toml"
PREFLIGHT_EXAMPLE="$REPO_ROOT/preflight_tool/config.example.toml"

BOLD="\033[1m"
GREEN="\033[0;32m"
BLUE="\033[0;34m"
YELLOW="\033[0;33m"
RED="\033[0;31m"
RESET="\033[0m"

log_info() {
    echo -e "${BLUE}[INFO]${RESET} $*"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${RESET} $*"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${RESET} $*"
}

log_error() {
    echo -e "${RED}[ERROR]${RESET} $*" >&2
}

# 1. Platform detection
OS="$(uname -s)"
if [ "$OS" != "Darwin" ]; then
    log_error "This script is designed for macOS. Detected OS: $OS"
    log_error "On Windows, please run: powershell -ExecutionPolicy Bypass -File Java-version/scripts/install-java-versions.ps1"
    exit 1
fi

ARCH="$(uname -m)"
case "$ARCH" in
    arm64)
        PLATFORM="darwin-aarch64"
        ;;
    x86_64)
        PLATFORM="darwin-x64"
        ;;
    *)
        log_error "Unsupported CPU architecture: $ARCH"
        exit 1
        ;;
esac

log_info "Detected platform: ${BOLD}$PLATFORM${RESET} (OS=$OS, ARCH=$ARCH)"

# 2. Check lock file
if [ ! -f "$LOCK_FILE" ]; then
    log_error "Lock manifest not found: $LOCK_FILE"
    exit 1
fi

# 3. Verify dependencies
for cmd in python3 curl tar shasum; do
    if ! command -v "$cmd" &>/dev/null; then
        log_error "Required tool '$cmd' is not installed or not in PATH."
        exit 1
    fi
done

mkdir -p "$CACHE_DIR"
mkdir -p "$RUNTIME_BASE_DIR/$PLATFORM"
rm -f "$STATE_FILE"
touch "$STATE_FILE"

# Parse target artifacts from lock file for current platform
ARTIFACT_JSON="$(python3 -c "
import json, sys
with open('$LOCK_FILE', 'r', encoding='utf-8') as f:
    data = json.load(f)
platforms = data.get('platforms', {})
if '$PLATFORM' not in platforms:
    sys.stderr.write('Platform $PLATFORM not found in lock file\n')
    sys.exit(1)
print(json.dumps(platforms['$PLATFORM']))
")"

MAJORS=("8" "11" "17" "21")

verify_jdk() {
    local candidate_home="$1"
    local expected_major="$2"

    local java_bin="$candidate_home/bin/java"
    local javac_bin="$candidate_home/bin/javac"

    if [ ! -x "$java_bin" ] || [ ! -x "$javac_bin" ]; then
        return 1
    fi

    local java_out
    if ! java_out="$("$java_bin" -version 2>&1)"; then
        return 1
    fi

    local javac_out
    if ! javac_out="$("$javac_bin" -version 2>&1)"; then
        return 1
    fi

    # Extract major version
    local parsed_java_major
    local parsed_javac_major
    parsed_java_major="$(echo "$java_out" | python3 -c "
import re, sys
m = re.search(r'(?:version\s+\"|javac\s+)(?:1\.)?(\d+)', sys.stdin.read())
print(m.group(1) if m else '')
")"
    parsed_javac_major="$(echo "$javac_out" | python3 -c "
import re, sys
m = re.search(r'(?:version\s+\"|javac\s+)(?:1\.)?(\d+)', sys.stdin.read())
print(m.group(1) if m else '')
")"

    if [ "$parsed_java_major" != "$expected_major" ] || [ "$parsed_javac_major" != "$expected_major" ]; then
        return 1
    fi

    local first_line
    first_line="$(echo "$java_out" | head -n 1)"
    echo "$first_line"
    return 0
}

log_info "Preparing JDKs 8, 11, 17, 21 for ${BOLD}$PLATFORM${RESET}..."

for major in "${MAJORS[@]}"; do
    TARGET_DIR="$RUNTIME_BASE_DIR/$PLATFORM/jdk-$major"

    # Extract artifact metadata for this major version
    METADATA="$(python3 -c "
import json
data = json.loads('''$ARTIFACT_JSON''')
item = data.get('$major')
if not item:
    print('MISSING')
else:
    print(f\"{item['download_url']}\t{item['sha256']}\t{item['java_home_relative_path']}\t{item['version']}\t{item['vendor']}\")
")"

    if [ "$METADATA" = "MISSING" ]; then
        log_error "No artifact configuration found for JDK $major on $PLATFORM in lock manifest!"
        exit 1
    fi

    IFS=$'\t' read -r DOWNLOAD_URL EXPECTED_SHA256 REL_PATH VERSION VENDOR <<< "$METADATA"

    if [ -n "$REL_PATH" ]; then
        JAVA_HOME_PATH="$TARGET_DIR/$REL_PATH"
    else
        JAVA_HOME_PATH="$TARGET_DIR"
    fi

    # Check if already installed and valid (Idempotent check)
    if [ -d "$JAVA_HOME_PATH" ]; then
        if VERIFIED_VER="$(verify_jdk "$JAVA_HOME_PATH" "$major")"; then
            log_success "JDK $major is already installed and verified at: $JAVA_HOME_PATH ($VERIFIED_VER)"
            printf "%s\t%s\t%s\n" "$major" "$JAVA_HOME_PATH" "$VERIFIED_VER" >> "$STATE_FILE"
            continue
        else
            log_warn "Existing JDK $major at $TARGET_DIR failed validation. Reinstalling..."
        fi
    fi

    # Need to download and extract
    ARCHIVE_NAME="$(basename "$DOWNLOAD_URL")"
    ARCHIVE_PATH="$CACHE_DIR/$ARCHIVE_NAME"

    log_info "Downloading JDK $major ($VERSION, $VENDOR)..."
    log_info "URL: $DOWNLOAD_URL"

    # Check if cached archive exists and check its hash
    DOWNLOAD_NEEDED=1
    if [ -f "$ARCHIVE_PATH" ]; then
        CURRENT_HASH="$(shasum -a 256 "$ARCHIVE_PATH" | awk '{print $1}')"
        if [ "$CURRENT_HASH" = "$EXPECTED_SHA256" ]; then
            log_info "Found valid cached archive: $ARCHIVE_NAME"
            DOWNLOAD_NEEDED=0
        else
            log_warn "Cached archive hash mismatch. Removing stale file: $ARCHIVE_NAME"
            rm -f "$ARCHIVE_PATH"
        fi
    fi

    if [ "$DOWNLOAD_NEEDED" -eq 1 ]; then
        curl -fL --progress-bar -o "$ARCHIVE_PATH.tmp" "$DOWNLOAD_URL"
        mv "$ARCHIVE_PATH.tmp" "$ARCHIVE_PATH"
    fi

    # Verify SHA-256 BEFORE extraction
    log_info "Verifying SHA-256 for $ARCHIVE_NAME..."
    ACTUAL_SHA256="$(shasum -a 256 "$ARCHIVE_PATH" | awk '{print $1}')"
    if [ "$ACTUAL_SHA256" != "$EXPECTED_SHA256" ]; then
        log_error "SHA-256 verification failed for $ARCHIVE_NAME!"
        log_error "Expected: $EXPECTED_SHA256"
        log_error "Actual:   $ACTUAL_SHA256"
        log_error "Retaining raw archive in $CACHE_DIR for diagnosis as required by AGENTS.md."
        exit 1
    fi
    log_success "SHA-256 verified successfully: $ACTUAL_SHA256"

    # Extract to target directory
    log_info "Extracting $ARCHIVE_NAME to $TARGET_DIR..."
    rm -rf "$TARGET_DIR"
    mkdir -p "$TARGET_DIR"

    tar -xzf "$ARCHIVE_PATH" --strip-components=1 -C "$TARGET_DIR"

    # Remove macOS quarantine flags if present
    xattr -dr com.apple.quarantine "$TARGET_DIR" 2>/dev/null || true

    # Verify extracted JDK
    log_info "Verifying extracted JDK $major binaries (java and javac)..."
    if VERIFIED_VER="$(verify_jdk "$JAVA_HOME_PATH" "$major")"; then
        log_success "Verified JDK $major successfully ($VERIFIED_VER)"
        printf "%s\t%s\t%s\n" "$major" "$JAVA_HOME_PATH" "$VERIFIED_VER" >> "$STATE_FILE"

        # Delete raw archive from .cache/ on successful verification (Rule 6)
        log_info "Deleting raw archive from cache: $ARCHIVE_PATH"
        rm -f "$ARCHIVE_PATH"
    else
        log_error "Verification failed for JDK $major after extraction!"
        log_error "Java binaries at $JAVA_HOME_PATH did not pass version check."
        log_error "Retaining raw archive at $ARCHIVE_PATH for diagnosis."
        exit 1
    fi
done

# 4. Generate or update preflight_tool/config.local.toml
log_info "Configuring preflight_tool local settings..."
python3 -c "
import sys, re
from pathlib import Path

config_path = Path('$PREFLIGHT_CONFIG')
example_path = Path('$PREFLIGHT_EXAMPLE')
state_file = Path('$STATE_FILE')

homes = {}
for line in state_file.read_text(encoding='utf-8').splitlines():
    if not line.strip():
        continue
    parts = line.split('\t')
    homes[parts[0]] = parts[1]

homes_toml = '[java.homes]\n' + '\n'.join(f'\"{k}\" = \"{v}\"' for k, v in sorted(homes.items(), key=lambda x: int(x[0]))) + '\n'

if config_path.is_file():
    content = config_path.read_text(encoding='utf-8')
    if '[java.homes]' in content:
        content = re.sub(r'\[java\.homes\][^\[]*', homes_toml + '\n', content)
    elif '[java]' in content:
        content = re.sub(r'(\[java\][^\n]*\n(?:[^\n\[]*\n)*)', r'\1\n' + homes_toml + '\n', content)
    else:
        content += '\n[java]\ndefault_home = \"\"\n\n' + homes_toml
    config_path.write_text(content, encoding='utf-8')
    print('Updated existing ' + str(config_path))
else:
    if example_path.is_file():
        content = example_path.read_text(encoding='utf-8')
        pattern = r'\[java\.homes\](?:\s*#[^\n]*)*'
        if re.search(pattern, content):
            content = re.sub(pattern, homes_toml.strip(), content)
        else:
            content += '\n' + homes_toml
        config_path.write_text(content, encoding='utf-8')
        print('Created ' + str(config_path) + ' from template')
    else:
        content = '[run]\nid = \"auto\"\nworkers = 2\n\n[java]\ndefault_home = \"\"\n\n' + homes_toml
        config_path.write_text(content, encoding='utf-8')
        print('Created ' + str(config_path))
"

# 5. Write local verification report (report.local.json)
python3 -c "
import json
from datetime import datetime, timezone
from pathlib import Path

state_file = Path('$STATE_FILE')
report_file = Path('$REPORT_FILE')

verified = {}
for line in state_file.read_text(encoding='utf-8').splitlines():
    if not line.strip():
        continue
    parts = line.split('\t')
    major = parts[0]
    verified[major] = {
        'java_home': parts[1],
        'version_string': parts[2] if len(parts) > 2 else '',
        'status': 'VERIFIED'
    }

report = {
    'platform': '$PLATFORM',
    'timestamp': datetime.now(timezone.utc).isoformat(),
    'verified_jdks': verified
}

with open(report_file, 'w', encoding='utf-8') as f:
    json.dump(report, f, indent=2)
"

rm -f "$STATE_FILE"

echo ""
echo -e "${BOLD}${GREEN}======================================================${RESET}"
echo -e "${BOLD}${GREEN}       JDK SETUP COMPLETED AND VERIFIED${RESET}"
echo -e "${BOLD}${GREEN}======================================================${RESET}"
echo -e "Platform: ${BOLD}$PLATFORM${RESET}"
python3 -c "
import json
with open('$REPORT_FILE', 'r', encoding='utf-8') as f:
    rep = json.load(f)
for k, v in sorted(rep.get('verified_jdks', {}).items(), key=lambda x: int(x[0])):
    print(f\"  - JDK {k}: {v['java_home']}\")
    print(f\"    Version: {v['version_string']}\")
"
echo -e "Generated config: ${BOLD}$PREFLIGHT_CONFIG${RESET}"
echo -e "Verification report: ${BOLD}$REPORT_FILE${RESET}"
echo ""
