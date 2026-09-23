# Báo cáo kiểm thử preflight_tool — 23/09/2026

> Đây là báo cáo **trước sửa**. F-01..04 đã được sửa và chạy lại thành công;
> kết quả mới trong [TEST_REPORT_FIX_FOUR_20260923.md](TEST_REPORT_FIX_FOUR_20260923.md).
> Giữ nguyên số liệu dưới đây làm bằng chứng lịch sử.

## 1. Kết luận

**Chưa đạt toàn bộ TEST_PLAN.md; chưa đủ căn cứ chốt nghiệm thu strict experiment.**

- Suite hiện có: **175 passed, 0 failed, 0 skipped**, 5,74 giây.
- Kiểm tra acceptance bổ sung theo plan: **2 passed, 4 failed**, 0,59 giây.
- Tổng hai lượt pytest độc lập: **177 passed, 4 failed** (181 lần kiểm tra; không phải 181 case ID trong plan).
- CLI index smoke: exit 0, **20 JSON → 20 candidate**, 0 rejection; kiểm tra lại toàn bộ hash evidence đều khớp.
- Không sửa code production, không commit/push; không chạy build repository bên ngoài.

175 test pass chỉ chứng minh các assertion hiện có. Các test dùng mock không thay thế build/probe thật hoặc chứng nhận chạy được trên Windows.

## 2. Baseline và môi trường

- Commit: `f8dd5d35da4f98da217bc8f2fb79fa2ba7821bbf`.
- Kế hoạch: bản local `preflight_tool/TEST_PLAN.md`; đặc tả `preflight.md`.
- Host: macOS 15.2, ARM64; Python 3.12.13 trong `.venv`.
- Đọc JDK 8/11/17/21 từ `../Java-version/config.local.toml`.
- Cấp `PREFLIGHT_JDK_8/11/17/21`; đặt JAVA_HOME và PATH sang JDK 17 **chỉ cho tiến trình chạy suite**. Không đổi Java toàn máy.
- `RUN_PERFORMANCE=1`, do đó không bỏ qua test hiệu năng.
- Output mới: `runs/qa-20260923-02/`, `runs/index-smoke-20260923-02/`.

## 3. Các lớp kiểm thử đã thực thi

Thống kê theo đường dẫn module trong JUnit XML, không theo marker (marker có thể giao nhau).

| Nhóm | Số test pass | Phạm vi chứng minh |
| --- | ---: | --- |
| Unit: ingest | 6 | Các assertion index/dedup/hash/input hiện có |
| Unit: AST | 10 | Phân tích Java theo fixture hiện có |
| Unit: output | 3 | Các output/schema fixture hiện có |
| Unit: status paths | 11 | Luồng trạng thái với dependency được mock |
| Unit: revision | 10 | Map/revision theo fixture hiện có |
| Unit: runner helpers | 54 | Path, wrapper, timeout, JDK, probe source, giới hạn submit… |
| Nghiệp vụ: class policy | 17 | Quy tắc class theo fixture |
| Nghiệp vụ: decision policy | 57 | Eligibility và bảng ưu tiên trạng thái |
| Integration | 3 | Git/worktree local, fast flow và cleanup; không phải full build thật |
| E2E trong thư mục e2e | 2 | CLI artifact với runner mock; index-only/output guard |
| Phi chức năng | 2 | Index 10.000 JSON và kiểm tra tính xác định qua 20 lượt |
| **Tổng** | **175** | **Không skip** |

Test JDK thật cho cả 8/11/17/21 nằm trong runner helpers: kiểm `java`/`javac` và major version. Đây **không phải** matrix Maven/Gradle compile trên bốn JDK.

Test performance đạt assertion hiện tại: index 10.000 JSON trong 120 giây, chênh RSS trước/sau dưới 512 MiB. Không đo peak RSS và không tương đương benchmark toàn dataset. Test timeout/diệt process con chạy trên macOS thực tế; nhánh Windows chưa được thực thi trên Windows thật.

### Coverage gate

| Gate | Thực tế | Kết quả |
| --- | ---: | --- |
| Line ≥90% | 96,32% | PASS |
| Branch ≥85% | 89,69% | PASS |
| Policy branch 100% | 100% | PASS |

Coverage chỉ lấy từ suite 175 test; lượt acceptance bổ sung không ghi đè coverage. Coverage đạt không xóa các lỗi acceptance.

## 4. Acceptance bổ sung: lỗi tái hiện được

File tái hiện: [test_plan_gaps.py](runs/qa-20260923-02/test_plan_gaps.py). Chỉ dùng dữ liệu giả và temp directory; URL `example.invalid` không được clone hay truy cập mạng.

| ID báo cáo / mapping plan | Input và kỳ vọng | Thực tế | Kết quả |
| --- | --- | --- | --- |
| F-01 / UT-BLD-010 | Raw focal path `/src/main/java/Thing.java`; từ chối trước khi tạo candidate | Index nhận thành `src/main/java/Thing.java` | FAIL |
| F-02 / BT-016 | Raw UNC path `\\\\server\\share\\Thing.java`; từ chối đầu vào tuyệt đối | Index nhận thành `server/share/Thing.java` | FAIL |
| F-03 / UT-REV-013 | SHA gồm 40 chữ `z`; loader phải reject non-hex | Không raise ValueError | FAIL |
| F-04 / UT-REV-013 | Hai dòng cùng task_id, SHA lần lượt 40 chữ `a` và 40 chữ `b`; reject mâu thuẫn | Không raise ValueError; loader ghi đè entry trước | FAIL |

F-01/F-02: `preflight/ingest.py::_record_identity` chuẩn hóa slash rồi `.strip("/")`, làm mất dấu nhận diện absolute/UNC trước khi helper runner kiểm tra. Đây là lỗi validation theo plan; phép thử này **không chứng minh đã đọc/ghi được bên ngoài workspace**. Focal path đã được tái hiện; chưa mở rộng acceptance sang mọi biến thể test path/device path.

F-03/F-04: `preflight/revision.py::load_revision_map` chỉ kiểm chiều dài SHA và gán dictionary theo task ID. Git có thể reject SHA non-hex ở giai đoạn sau; lỗi được chứng minh ở đây là loader chưa reject sớm đúng yêu cầu plan, không phải chứng minh SHA đó vào được strict manifest.

Cả bốn ca thuộc yêu cầu P0 trong plan. Hai test bổ sung pass là kiểm evidence/count của index smoke và policy branch gate.

## 5. CLI smoke với dataset thật

Đã chạy từ `preflight_tool`:

```sh
.venv/bin/class2test-preflight --config ../Java-version/config.local.toml --input-root ../classes2test --limit 20 --index-only --output-dir runs/index-smoke-20260923-02
```

- Provenance: raw=20, deduplicated=20, selected=20.
- Manifest parse được; 20 task ID duy nhất.
- Mọi cặp `source_json_paths`/`source_json_sha256s` đều khớp SHA-256 raw bytes hiện tại.
- Rejection file rỗng; không phát hiện `.git` trong output smoke.
- Chỉ chạy index-only: không có kết luận repository build được, CUT eligible hoặc strict eligible. Không yêu cầu `summary.json` ở chế độ này.

## 6. Phần chưa kiểm chứng / chưa đạt điều kiện chạy

| Hạng mục trong plan | Trạng thái | Lý do / bước tiếp theo |
| --- | --- | --- |
| IT-004..007: Maven/Gradle single/multi-module, build + probe thật | NOT_RUN | Chưa có portfolio fixture pin tool/dependency và expected đầy đủ; test mock không thay thế |
| Full strict E2E, upstream-pinned + probe thật | NOT_RUN | Cần portfolio và revision map đã audit; không tự khai provenance upstream |
| Windows native, Linux, macOS Intel | NOT_RUN | Lượt này chỉ có host macOS ARM64 |
| Remote discovery/build | NOT_RUN | Chưa có repo audit và môi trường build cô lập; không chạy build code bên ngoài trên host |
| Mutation revision/policy | NOT_RUN | Chưa có cấu hình mutation/report hợp lệ theo mục 19.5 |
| Peak RSS, full dataset throughput, full runner nhiều worker | PARTIAL | Chỉ chạy bài performance/index và kiểm giới hạn submission hiện có |
| Locked manifest input/hash sai, integration rule, nhiều lỗi đồng thời, non-Java suffix, effective JDK config | NOT_RUN acceptance đầy đủ | Các GAP đã ghi trong mục 18 của plan; không tính là pass từ suite hiện tại |

Không tuyên bố toàn bộ UT/BT/IT/E2E ID đã pass: nhiều ID là yêu cầu cần mở rộng fixture. Ưu tiên xử lý F-01..04 rồi chạy lại regression; tiếp theo bổ sung build/probe portfolio và matrix Windows/macOS trước khi chốt nghiệm thu.

## 7. Bằng chứng và chạy lại

- [JUnit suite 175 test](runs/qa-20260923-02/pytest.xml).
- [Coverage JSON](runs/qa-20260923-02/coverage.json).
- [JUnit acceptance, gồm 4 ca fail](runs/qa-20260923-02/acceptance.xml).
- [Provenance index smoke](runs/index-smoke-20260923-02/provenance.json).
- [Candidate manifest](runs/index-smoke-20260923-02/manifests/class_candidates.jsonl).

Suite tổng dùng đúng cách nạp env và lệnh tại TEST_PLAN mục 19.3, đổi run ID thành `qa-20260923-02`. Kiểm tra bổ sung có thể chạy lại từ `preflight_tool`:

```sh
.venv/bin/python -m pytest runs/qa-20260923-02/test_plan_gaps.py -ra
.venv/bin/python scripts/check_coverage.py runs/qa-20260923-02/coverage.json
```

Lệnh acceptance dự kiến exit 1 với baseline hiện tại do bốn lỗi trên. `runs/` bị Git ignore: script và bằng chứng vẫn có trên máy này nhưng không tự được gửi theo commit báo cáo; cần chủ động chọn cách chia sẻ nếu gửi cho máy khác.
