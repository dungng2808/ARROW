#!/bin/sh
# Cài LiteLLM proxy vào ARROW/litellm (môi trường Python riêng, không commit) cho macOS/Linux.
# Phiên bản theo Report 2: LiteLLM 1.91.0. Chạy lại an toàn (tạo lại môi trường trong cùng thư mục, cần khi đổi tên/chuyển ARROW).
set -e
LITELLM_VERSION="${LITELLM_VERSION:-1.91.0}"
ARROW_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
TARGET="$ARROW_ROOT/litellm"

PY=""
for candidate in python3.13 python3.12 python3.11 python3.10 /opt/homebrew/bin/python3.12 /opt/homebrew/bin/python3.13 python3; do
  if command -v "$candidate" >/dev/null 2>&1; then
    if "$candidate" -c 'import sys; sys.exit(0 if sys.version_info >= (3, 10) else 1)' 2>/dev/null; then
      PY="$(command -v "$candidate")"; break
    fi
  fi
done
if [ -z "$PY" ]; then
  echo "Cần Python 3.10 trở lên (macOS: brew install python@3.12)." >&2
  exit 1
fi

echo "Dùng $PY ($("$PY" --version)) để tạo $TARGET"
# --clear: venv ghi cứng đường dẫn; chạy lại sau khi chuyển/đổi tên thư mục ARROW thì phải tạo lại từ đầu.
"$PY" -m venv --clear "$TARGET"
"$TARGET/bin/python" -m pip install --upgrade pip >/dev/null
"$TARGET/bin/python" -m pip install "litellm[proxy]==$LITELLM_VERSION"
echo "Đã cài LiteLLM $("$TARGET/bin/python" -c "import importlib.metadata as m; print(m.version('litellm'))")"
echo "Lệnh: $TARGET/bin/litellm  (pilot_tool tự tìm ở đây; chạy lại: arrow doctor)"
