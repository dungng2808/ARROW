# Pilot manifests

Mẫu pilot tách khỏi tập đánh giá. Kết quả pilot không gộp vào kết quả chính.

| File | Số CUT | Dùng cho |
| --- | ---: | --- |
| `classes2test_pilot_50_seed42_v2.*` | 50 (50 repository) | **Bản đang dùng** (`pilot_tool/experiments/pilot-v1.json`). Pilot phần 1 (môi trường, công cụ, chi phí) và phần 2 (chọn số vòng sửa tối đa N) |
| `classes2test_pilot_50_seed42.*` | 50 (50 repository) | Bản v1, giữ để đối chiếu; 11 CUT không qua sàng lọc S0 |
| `classes2test_pilot_reserve_50_seed42.*` | 50 (50 repository) | CUT dự bị để thay CUT không qua sàng lọc S0 |
| `gitbug_java_pilot_9_seed42.*` | 9 (9 repository) | Pilot phần 1 (môi trường, công cụ, chi phí); không dùng để chọn N |

Mỗi bộ có `.jsonl` (bản ghi preflight giữ nguyên), `.meta.json` (seed, quy tắc, checksum) và `.csv` (bản phẳng, cùng cột với `classes2test_200_seed42.csv`; bản GitBug-Java thêm `bug_id`, `test_class_path`, `test_pairing`).

## Quy tắc chọn

**Classes2Test.** Pool `technical_eligible` + `CONTENT_MATCHED` (6.342 class / 1.324 repository), bỏ mọi repository của `classes2test_200_seed42.jsonl`, còn 5.566 class / 1.124 repository. Rút ngẫu nhiên 50 repository không lặp, mỗi repository một class, cùng thuật toán với tập đánh giá. Không trùng repository nào với tập đánh giá.

**Classes2Test v2 (13/10/2026): sàng lọc bằng S0 của pilot_tool.** Preflight chỉ biên dịch, không chạy test, nên một số CUT có test người viết không chạy được trên máy thường (cần cơ sở dữ liệu, Redis, Docker, server ngoài; test treo; test bị `@Ignore`). Bước sàng lọc: (1) rút 50 CUT dự bị cùng quy tắc, seed 42, bỏ thêm 50 repository của bản v1 (`classes2test_pilot_reserve_50_seed42.*`); (2) chạy S0 của pilot_tool (baseline 2 lần, JVM `en_US`/UTC) cho cả 100 CUT (run `pilot-screening`, 4 task chạy lại trong `pilot-screening-fix` sau khi sửa lỗi đọc tên class trong báo cáo Surefire); (3) **tiêu chí B**: CUT đạt khi S0 READY và test class người viết ghép với CUT có ít nhất một test chạy, không test nào của class đó fail ở cả hai lần hoặc flaky (test của class khác được phép fail sẵn vì đã bị loại khỏi so sánh regression); (4) CUT v1 đạt thì giữ nguyên vị trí, CUT không đạt được thay bằng CUT dự bị đạt tiếp theo theo thứ tự rút. Kết quả: giữ 39, thay 11 (danh sách và lý do trong `classes2test_pilot_50_seed42_v2.meta.json`, mục `replacements`). Script: `preflight_tool/scripts/select_pilot_after_s0.py`. Tập đánh giá 200 class chưa áp dụng bước này (sẽ chọn lại sau); khi áp dụng phải dùng cùng tiêu chí để pilot đại diện cho tập đánh giá.

**GitBug-Java.** Chỉ các repository của `gitbug_java_32_seed42.jsonl` có CUT ELIGIBLE ở bug (commit) khác với CUT đánh giá: 9 repository. Mỗi repository rút một CUT ở bug khác; ưu tiên class khác CUT đánh giá (7 repository). `LMAX-Exchange/Simple-DSL` và `jitterted/ensembler` chỉ có một class qua preflight nên CUT pilot là cùng class ở bug khác (`same_class_other_bug` trong `.meta.json`). Không trùng task hay bug nào với tập đánh giá. Tập đánh giá được rút trước, pilot rút sau từ phần còn lại.

## Lệnh tái tạo (từ `preflight_tool/`)

```text
python scripts/sample_manifest.py --pool-glob "../preflight_manifest/0*/technical_eligible_manifest.jsonl" --size 50 --seed 42 --exclude-repos-from ../evaluation_manifest/classes2test_200_seed42.jsonl --name classes2test_pilot_50_seed42 --output-dir ../evaluation_manifest/pilot
python scripts/sample_gitbug_pilot.py --pool ../preflight_manifest/gitbug-java/technical_eligible_manifest.jsonl --evaluation ../evaluation_manifest/gitbug_java_32_seed42.jsonl --seed 42 --output-dir ../evaluation_manifest/pilot
python scripts/export_manifest_csv.py ../evaluation_manifest/pilot/classes2test_pilot_50_seed42.jsonl
python scripts/sample_manifest.py --pool-glob "../preflight_manifest/0*/technical_eligible_manifest.jsonl" --size 50 --seed 42 --exclude-repos-from ../evaluation_manifest/classes2test_200_seed42.jsonl --exclude-repos-from ../evaluation_manifest/pilot/classes2test_pilot_50_seed42.jsonl --name classes2test_pilot_reserve_50_seed42 --output-dir ../evaluation_manifest/pilot
python scripts/select_pilot_after_s0.py --pilot ../evaluation_manifest/pilot/classes2test_pilot_50_seed42.jsonl --reserve ../evaluation_manifest/pilot/classes2test_pilot_reserve_50_seed42.jsonl --run-dir ../pilot_tool/runs/pilot-screening --run-dir ../pilot_tool/runs/pilot-screening-fix --name classes2test_pilot_50_seed42_v2 --output-dir ../evaluation_manifest/pilot
python scripts/export_manifest_csv.py ../evaluation_manifest/pilot/gitbug_java_pilot_9_seed42.jsonl --gitbug-candidates ../gitbug_input/candidates.csv
```

Checksum: Classes2Test v2 `b44a9d9a…`, dự bị `5a76e632…`, v1 `687fab7c…`, GitBug-Java `6754b1a5…` (đầy đủ trong `.meta.json`).
