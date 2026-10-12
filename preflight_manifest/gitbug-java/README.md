# Manifest preflight GitBug-Java

Bốn manifest cùng định dạng với các shard Classes2Test, nhưng **gộp từ ba lần chạy hoàn tất** thay vì một full run:

| Lần chạy | Số CUT | Nội dung |
| --- | ---: | --- |
| `gitbug-full-20261011` | 180 | Full run trên CUT có test ghép theo tên (`gitbug_input/archive/v1_name_paired/`) |
| `gitbug-rerun-20261011` | 71 | Chạy lại các CUT không ELIGIBLE/EXCLUDED bằng `scripts/rerun_fixed_probe.py` (probe khai báo `throws Throwable`, ép kiểu `null` khi overload, bỏ qua plugin định dạng khi compile probe; JDK 11 cho module nhắm Java 8 nhưng dùng `--release`) |
| `gitbug-labeled-20261012` | 22 | CUT thêm từ label test tham chiếu (`gitbug_input/manual_labels/`), chạy với cùng bản sửa (`--new-only`) |

Quy tắc gộp (`scripts/export_gitbug_manifests.py`): với mỗi `task_id`, giữ bản ghi của lần chạy mới nhất đã chạy task đó; không sửa bản ghi nào. `merge_provenance.json` ghi SHA-256 của từng manifest nguồn, mô tả bản sửa, số bản ghi và lần chạy nguồn của từng nhóm bản ghi.

| Manifest | Số bản ghi |
| --- | ---: |
| `class_candidates.jsonl` | 202 |
| `locked_input_manifest.jsonl` | 202 |
| `technical_eligible_manifest.jsonl` | 168 (32 repository) |
| `strict_eligible_manifest.jsonl` | 168 (mọi CUT là `UPSTREAM_PINNED`) |

Lệnh tái tạo (từ `preflight_tool/`):

```text
python scripts/export_gitbug_manifests.py --run runs/gitbug-full-20261011 --run runs/gitbug-rerun-20261011 --run runs/gitbug-labeled-20261012 --output-dir ../preflight_manifest/gitbug-java
```

Ghi chú: các bản ghi giữ nguyên đường dẫn máy chạy (`working_directory`, `log_path`) như manifest Classes2Test. Label test tham chiếu chưa qua người kiểm tra thứ hai; nếu label thay đổi, chạy lại chuẩn hóa, preflight `--new-only` và lệnh trên.
