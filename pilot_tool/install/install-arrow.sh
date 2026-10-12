#!/bin/sh
# Cài lệnh `arrow` cho macOS/Linux: tạo ~/.local/bin/arrow gọi pilot_tool bằng `dotnet run`.
# Chạy lại script này nếu chuyển thư mục ARROW sang chỗ khác. Sau git pull không cần chạy lại.
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BIN="$HOME/.local/bin"
mkdir -p "$BIN"
cat > "$BIN/arrow" <<LAUNCHER
#!/bin/sh
export ARROW_PILOT_ROOT="$ROOT"
exec dotnet run --project "$ROOT/src/Arrow.Cli" --nologo -- "\$@"
LAUNCHER
chmod +x "$BIN/arrow"

case ":$PATH:" in
  *":$BIN:"*) ;;
  *)
    for RC in "$HOME/.zshrc" "$HOME/.bashrc"; do
      if [ -f "$RC" ] || [ "$RC" = "$HOME/.zshrc" ]; then
        grep -qs 'ARROW: thêm ~/.local/bin' "$RC" || printf '\n# ARROW: thêm ~/.local/bin vào PATH cho lệnh arrow\nexport PATH="$HOME/.local/bin:$PATH"\n' >> "$RC"
      fi
    done
    echo "Đã thêm ~/.local/bin vào PATH. Mở terminal mới (hoặc chạy: source ~/.zshrc)."
    ;;
esac

echo "Đã cài lệnh arrow -> $BIN/arrow (pilot_tool: $ROOT)"
echo "Thử: arrow --help"
