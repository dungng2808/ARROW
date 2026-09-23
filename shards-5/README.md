# Phân công preflight cho 5 máy

Folder này được đưa lên Git: mỗi file JSON là **danh sách class được giao**, không phải bản sao dataset, không chứa JDK hoặc đường dẫn tuyệt đối của máy tạo.

| Máy | File phân công | Class candidate | Repository |
| --- | --- | ---: | ---: |
| 1 | `shard-01.json` | 17.164 | 1.882 |
| 2 | `shard-02.json` | 17.164 | 1.882 |
| 3 | `shard-03.json` | 17.164 | 1.882 |
| 4 | `shard-04.json` | 17.164 | 1.882 |
| 5 | `shard-05.json` | 17.163 | 1.882 |

Tổng: **85.819 class candidate**, **9.410 repository**, từ **362.414 JSON** của dataset hiện tại. Các class cùng repository nằm trên cùng một máy; không có task ID trùng giữa các shard. Phân công cân bằng theo số class, không cam kết thời gian chạy bằng nhau. Đây là candidate **cần kiểm tra**, chưa phải class đã đạt preflight.

## Chuẩn bị trên mỗi máy

1. Pull cùng phiên bản ARROW **có hỗ trợ `--shard`** và folder này.
2. Có dataset Classes2Test cùng snapshot ở `ARROW/classes2test/dataset/` (hoặc đường dẫn riêng truyền bằng `--input-root`). Pull Git ARROW không tải dataset gốc do dataset đang bị ignore. Có thể dùng toàn dataset hoặc bản con chứa đầy đủ mọi evidence JSON của shard, giữ nguyên `dataset/<project-id>/<file>.json`.
3. Cài Python environment/dependency của `preflight_tool`; chuẩn bị JDK đúng hệ điều hành theo `Java-version/README.md`. `Java-version/config.local.toml` là config riêng của mỗi máy, không sao chép đường dẫn Java của người khác.
4. Chuẩn bị Git, Maven/Gradle theo repository; build code bên ngoài trong môi trường cô lập phù hợp, không cấp secrets. Windows vẫn cần kiểm thử thực tế; hỗ trợ cú pháp CLI không đồng nghĩa mọi build đã được chứng nhận trên Windows.

## Lệnh dùng chung cho macOS / Windows

Kích hoạt virtualenv, đứng tại `ARROW/preflight_tool`. Các lệnh dưới viết một dòng để dùng với cả PowerShell và shell macOS.

Máy 1 kiểm tra dataset và phân công trước, **chưa clone/build**:

```text
python -m preflight.cli --config ../Java-version/config.local.toml --input-root ../classes2test --shard ../shards-5/shard-01.json --index-only --output-dir runs/machine-01-check
```

Kết quả phải có **17.164 class selected**. `raw_json_indexed` và `deduplicated_classes` tính toàn bộ input local; `classes_selected` mới là số class của shard.

Khi môi trường build đã sẵn sàng, chạy phần được giao:

```text
python -m preflight.cli --config ../Java-version/config.local.toml --input-root ../classes2test --shard ../shards-5/shard-01.json --workers 2 --output-dir runs/machine-01
```

Máy 2–5 thay `shard-01.json` bằng file tương ứng và đổi tên output. Máy 5 có 17.163 class. Output phải mới/rỗng; không dùng chung thư mục output giữa các máy. Điều chỉnh workers theo tài nguyên.

Tool hiện index input local trước, sau đó chọn shard; chỉ class được chọn mới được chuyển đến runner clone/checkout/build/probe. `--shard` không cho kết hợp `--limit` hoặc `--max-classes`, tránh chạy thiếu mà tưởng đã xong cả phần.

Mặc định runner tự xóa cache mirror của mỗi repo sau khi toàn bộ class được chọn thuộc repo đó đã kết thúc, giữ nguyên log/report/manifest. Xem `reports/repo_cleanup.jsonl` và `summary.repo_cleanup` để xác minh DELETED/ABSENT/KEPT/FAILED. `--keep-repo-cache` hoặc `[run].keep_repo_cache=true` giữ mirror để debug; giữ workspace cũng kéo theo giữ mirror. Nếu workspace dọn chưa sạch, mirror được giữ và ghi lý do. Không dọn cache của run cũ khi process bị kill. Xóa cache không đưa vào thùng rác; tái chạy cần clone lại.

Thiếu class, task ID lặp, metadata khác hoặc evidence hash không khớp sẽ dừng **trước khi chạy runner**. Nếu dừng, lấy đúng dataset snapshot và dùng output mới; không sửa hash trong shard để bỏ qua lỗi. Shard không tự tải dataset và không tự tải JDK.

## Nội dung file JSON và bằng chứng

Mỗi file có `schema_version`, `shard_id`, `class_count`, `classes`. Mỗi class ghi `task_id`, `repo_url`, `class_path`, `class_name`, `evidence_count`, `evidence_sha256`.

`evidence_sha256` là SHA-256 của JSON compact ASCII biểu diễn các cặp `[source_json_path, SHA256(raw bytes)]` đã sắp xếp. Nó kiểm snapshot evidence mà không chép nội dung raw JSON vào Git. Các phép chuẩn hóa/task ID tuân theo ingest hiện tại; package/FQN chính xác vẫn do preflight xác định từ source checkout.

`summary.json` chứa số lượng và SHA-256 của từng file phân công. Output `provenance.json` ghi shard ID, hash của file shard và số class được chọn.

Đây **không phải locked revision manifest**: shard không gán commit hoặc khai `UPSTREAM_PINNED`. Không có `--revision-map` đã audit thì kết quả vẫn discovery, không được coi là strict experiment. Bốn lỗi acceptance trước đây đã được sửa riêng và kiểm thử lại; xem `preflight_tool/TEST_REPORT_FIX_FOUR_20260923.md`. Các hạng mục chưa kiểm chứng trong test plan vẫn còn.

## Tạo lại phân công

Từ `preflight_tool`, dùng index nguồn mới được kiểm chứng và folder output chưa tồn tại:

```text
python scripts/export_shards.py --index <source_index.sqlite> --output-dir <folder-moi> --parts 5
```

Index có thể lấy từ một lượt `--index-only` không giới hạn trên toàn dataset. Không dùng index smoke, cũ hoặc thiếu dữ liệu để tạo lại phân công toàn bộ. Thuật toán sắp repo theo số class giảm dần rồi giao cho phần ít class nhất; tie-break cố định để tái lập.

Folder `ARROW/classes2test/shards-5/` là các bản sao JSON local của cách chia trước, **khác** folder manifest `ARROW/shards-5/` này. Không cần gửi bản sao local nếu mọi người đã có dataset đúng snapshot. Chưa có script tự động gộp kết quả trong tính năng này; giữ nguyên toàn bộ output từng máy để đối soát sau.
