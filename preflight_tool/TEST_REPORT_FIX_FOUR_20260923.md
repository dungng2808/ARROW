# Kết quả sửa và kiểm thử lại F-01..04 — 23/09/2026

## Kết luận

**Đã sửa cả bốn lỗi được tái hiện trong báo cáo trước.** Suite cuối: **237 passed, 0 failed, 0 skipped**, 5,93 giây. Không suy rộng thành đã đạt mọi hạng mục TEST_PLAN.

Baseline: HEAD `f8dd5d3` cộng thay đổi local cho shard và bản sửa lần này; chưa commit/push. Môi trường macOS ARM64, Python 3.12.13; đủ JDK 8/11/17/21 qua config local; JDK 17 vào JAVA_HOME/PATH chỉ cho subprocess; `RUN_PERFORMANCE=1`.

## Bản sửa

| Lỗi | Thay đổi | Kiểm chứng |
| --- | --- | --- |
| F-01 POSIX absolute | `ingest.py` kiểm raw path trước khi strip slash; ghi `INPUT_PATH_INVALID`, không tạo candidate | Cả focal/test path và CLI pass |
| F-02 UNC Windows | Nhận diện cả slash/backslash trên mọi host; chặn UNC, root-relative, drive absolute/relative, device path; đồng thời chặn `..` và NUL | Parameterized fixture pass trên macOS; không tuyên bố Windows native đã chạy |
| F-03 SHA non-hex | `revision.py` dùng fullmatch `[0-9a-fA-F]{40}`, chuyển SHA hợp lệ sang lowercase | JSONL/CSV: non-hex, thiếu/thừa ký tự, newline, Unicode giả hex đều reject |
| F-04 task map mâu thuẫn | Reject cùng task nhưng khác SHA/provenance/evidence; không âm thầm ghi đè. Duplicate giống hệt được chấp nhận sau chuẩn hóa hoa/thường SHA | JSONL/CSV và CLI chặn trước runner pass |

Input path lỗi chỉ loại bản ghi đó, không dừng các JSON hợp lệ khác. Relative path dùng `/` hoặc `\\` vẫn được nhận. Revision map lỗi dừng trước clone/build. Không sửa source dataset, Java config hoặc 5 manifest shard.

## Test bổ sung và kết quả

- File chính thức: `tests/unit/test_input_validation_regressions.py` — **48 case**, gồm 44 unit và 4 CLI E2E dùng runner giả để xác minh không gửi input sai sang runner.
- Đã chạy test mới trước bản sửa để xác nhận phát hiện lỗi; sau sửa toàn bộ pass.
- Chạy lại **nguyên 4 ca lỗi cũ** trong `runs/qa-20260923-02/test_plan_gaps.py`: **4 passed, 2 deselected**. Không thay assertion của test cũ.
- Suite chính thức (bao gồm test shard và regression mới): **237 passed**, không skip, không xfail.
- Line coverage **96,24%**, branch coverage **90,12%**; policy branch **100%**. Gate lines ≥90%, branches ≥85% đạt.

## Đối soát dataset và shard sau sửa

Chạy index toàn bộ dataset bằng code mới:

```text
.venv/bin/python -m preflight.cli --config ../Java-version/config.local.toml --input-root ../classes2test --index-only --output-dir runs/index-after-fix-four-20260923
```

Kết quả: **362.414 JSON → 85.819 class; 0 input rejection**. Đối chiếu cả 5 file trong `ARROW/shards-5/` bằng `select_shard` với index mới: metadata/evidence hash khớp; không trùng task; hợp của 5 shard đúng tập class nguồn. Số class lần lượt 17.164 / 17.164 / 17.164 / 17.164 / 17.163. **Không cần tạo lại shard**.

## Bằng chứng

- [JUnit suite cuối](runs/qa-20260923-fix-four-final/pytest.xml).
- [Coverage suite cuối](runs/qa-20260923-fix-four-final/coverage.json).
- [JUnit 4 ca lỗi gốc sau sửa](runs/qa-20260923-fix-four/original-four.xml).
- [Provenance index mới](runs/index-after-fix-four-20260923/provenance.json).

Chạy regression từ `preflight_tool`:

```text
python -m pytest tests/unit/test_input_validation_regressions.py -ra
```

Suite tổng dùng môi trường và lệnh coverage tại TEST_PLAN mục 19.3, chọn run ID mới; không ghi đè evidence cũ. Artifact `runs/` vẫn bị Git ignore, còn file test mới có thể commit.

## Giới hạn còn lại

Chưa chạy Windows native/Linux, full Maven/Gradle build/probe portfolio, mutation testing hoặc full strict experiment. Các GAP khác trong TEST_PLAN không được tự động đóng bởi việc sửa bốn ca này. Không chạy clone/build repository từ dataset trong lần kiểm thử này.
