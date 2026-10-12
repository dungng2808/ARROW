# Thống kê preflight GitBug-Java

| File / thư mục | Nội dung |
| --- | --- |
| `GitBug-Java_preflight_ket_qua.xlsx` | Bảng tổng hợp: phễu lọc, trạng thái theo từng lần chạy, theo repository, chi tiết 202 CUT, file bị loại trước preflight |
| `merged_summary.json` | Tóm tắt gộp ba lần chạy (202 CUT, 168 ELIGIBLE, 32 repository, 9 repository có ≥ 2 bug ELIGIBLE) |
| `gitbug-full-20261011/`, `gitbug-rerun-20261011/`, `gitbug-labeled-20261012/` | `summary.json` và `run_stats.json` của từng lần chạy, tạo bằng `scripts/summarize_run.py` |

Manifest gộp và quy tắc gộp: `preflight_manifest/gitbug-java/`.
