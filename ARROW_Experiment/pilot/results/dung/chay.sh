#!/bin/zsh
# Pilot máy Dung: gen rồi repair (phần 4/4, cả 2 mô hình). Chạy lại file này là làm tiếp phần còn thiếu.
cd "$(dirname "$0")/../.."
python3 -u pilot_run.py gen --machine dung --models gpt6luna,qwen3coder-or --shard 4/4 --workers 4 > results/dung/gen.log 2>&1
code=$?
if [ $code -ne 0 ]; then echo "GEN DỪNG, mã $code" >> results/dung/gen.log; exit $code; fi
python3 -u pilot_run.py repair --machine dung --models gpt6luna,qwen3coder-or --workers 4 --arrow ~/.local/share/arrow-dev/build-tn2/arrow > results/dung/repair.log 2>&1
code=$?
echo "REPAIR KẾT THÚC, mã $code" >> results/dung/repair.log
exit $code
