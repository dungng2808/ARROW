# Giao việc cho AI: đưa thống kê preflight lên Git

Khi người dùng gửi file này cho AI, hãy **thực hiện đến khi push xong**, không chỉ tóm tắt hướng dẫn. Yêu cầu đó bao gồm quyền chạy script thống kê, commit và push **chỉ thư mục shard của người dùng** trong `preflight_summary/`; không cần hỏi lại quyền cho từng bước. Nếu thiếu run hợp lệ, chưa xác định được shard, script báo lỗi hoặc Git không có quyền push, báo chính xác điều còn thiếu. Không tạo hoặc sửa số liệu để hoàn thành.

## Vì sao cần

Mỗi shard mới chỉ đưa lên Git bốn manifest trong `preflight_manifest/`. Các manifest đó không cho biết **vì sao** class bị loại. Report 2 cần bảng thống kê trên toàn bộ 85.819 class (bao nhiêu class lỗi clone, checkout, build, JDK, dependency, probe...) và đặc điểm của pool `technical_eligible`. Số liệu này nằm trong `reports/` của từng run, chỉ có trên máy đã chạy shard.

## Đích đến

| Shard | Người phụ trách | Thư mục Git | Số class | Trạng thái |
| --- | --- | --- | ---: | --- |
| 01 | Dũng | `preflight_summary/01-DUNG/` | 17.164 | Chờ |
| 02 | Kiều Anh | `preflight_summary/02-KIEU_ANH/` | 17.164 | Chờ |
| 03 | Quang | `preflight_summary/03-QUANG/` | 17.164 | Chờ |
| 04 | Chinh | `preflight_summary/04-CHINH/` | 17.164 | Chờ |
| 05 | Hán | `preflight_summary/05-HAN/` | 17.163 | Đã có |

Mỗi thư mục chứa đúng hai file do script tạo ra, không sửa tay:

```text
summary.json     bản sao nguyên vẹn của <run>/reports/summary.json
run_stats.json   thống kê theo status, reason code, tag, revision status, build tool, test framework;
                 provenance của run đã bỏ đường dẫn tuyệt đối của máy
```

`preflight_summary/05-HAN/` là ví dụ đã đúng định dạng.

## Các bước AI phải làm

1. Pull `main` mới nhất của ARROW một cách an toàn (không reset, không force). Xác định người dùng phụ trách shard nào từ yêu cầu của họ, `preflight_tool_md/`, hoặc thư mục đã dùng trong `preflight_manifest/`. Nếu không chắc, hỏi người dùng; không lấy run của người khác.
2. Tìm **đúng full run** đã tạo bốn manifest của shard đó, thường ở `preflight_tool/runs/<run-id>/`. Run phải có `provenance.json` với `run_status = "COMPLETED"` và `shard_id` đúng shard, cùng `reports/summary.json` và `reports/preflight_results.jsonl`. Không dùng output `--index-only`, smoke test hoặc run dở. Nếu có nhiều run hoàn tất, chọn run có `manifests/locked_input_manifest.jsonl` trùng SHA-256 với file trong `preflight_manifest/<thu-muc>/`.
3. Từ `ARROW/preflight_tool`, dùng Python của `.venv` (hoặc Python ≥ 3.11 bất kỳ, script chỉ dùng thư viện chuẩn) chạy:

   ```text
   python scripts/summarize_run.py --run-dir runs/<run-id> --output-dir ../preflight_summary/<thu-muc-cua-minh>
   ```

   Script tự kiểm tra run đã hoàn tất, không trùng `task_id`, và số record khớp giữa `summary.json`, `preflight_results.jsonl` và `provenance.json`. Nếu script in `ERROR` và thoát mã 1, **dừng và báo lỗi nguyên văn**; không sửa report để ép khớp.
4. Kiểm tra hai file vừa tạo: `summary.json` phải giống byte-for-byte file trong run; `run_stats.json` không chứa đường dẫn tuyệt đối, tên người dùng máy, token hoặc secret. Nếu phát hiện, dừng push và báo vị trí.
5. Kiểm tra `git status` và diff. Chỉ stage hai file trong thư mục shard của mình; không đưa `preflight_tool/runs/`, log, dataset, JDK hoặc thay đổi sẵn có của người khác vào commit. Commit với thông điệp `Add preflight summary for shard <NN>` rồi push lên `origin main`. **Không force push, reset, clean hoặc ghi đè thay đổi của người khác.** Nếu remote có commit mới, pull/rebase an toàn rồi push lại.
6. Trả lời người dùng bằng tiếng Việt: shard, run nguồn, `total`, `technical_eligible`, các `by_status` lớn nhất, commit SHA và kết quả push. Nếu nội dung đã có sẵn và giống hệt trên remote, không tạo commit trống.

Ví dụ cho **shard 02** (thay `02-KIEU_ANH` và `<run-id>` bằng giá trị đúng):

```text
cd preflight_tool
python scripts/summarize_run.py --run-dir runs/<run-id> --output-dir ../preflight_summary/02-KIEU_ANH
cd ..
git status --short
git add -- preflight_summary/02-KIEU_ANH/summary.json preflight_summary/02-KIEU_ANH/run_stats.json
git diff --cached --stat
git commit -m "Add preflight summary for shard 02" -- preflight_summary/02-KIEU_ANH/summary.json preflight_summary/02-KIEU_ANH/run_stats.json
git push origin main
```

Tài liệu đối chiếu: [manifest preflight](../preflight_manifest/README.md), [phân công shard](../shards-5/README.md), [outputs của tool](../preflight_tool/README.md#outputs).
