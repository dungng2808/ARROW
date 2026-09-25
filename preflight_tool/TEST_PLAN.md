# Kế hoạch kiểm thử hệ thống Class2Test Preflight

> Bổ sung dọn mirror theo repository: xem `TEST_REPORT_REPO_CLEANUP_20260923.md`.
> Suite mới nhất sau tính năng cleanup: 250 pass trên macOS ARM64. Gate cleanup:
> không xóa khi còn class chạy/chờ; xóa sau class cuối; giữ khi debug hoặc workspace
> còn sót; chặn path ngoài run/symlink; ghi lỗi dọn; giữ nguyên source và evidence.
> Test: `tests/unit/test_repo_cleanup.py` và `tests/integration/test_git_and_runner.py`.

> Cập nhật sau sửa F-01..04 ngày 23/09/2026: validation raw path và revision map
> đã sửa, có 48 regression case mới trong `tests/unit/test_input_validation_regressions.py`
> (gồm 4 case marker e2e). Suite hiện tại 237 pass; xem `TEST_REPORT_FIX_FOUR_20260923.md`.
> Các số liệu baseline bên dưới là lịch sử; các hạng mục chưa kiểm chứng khác vẫn giữ nguyên.

## 1. Thông tin tài liệu

| Thuộc tính | Giá trị |
| --- | --- |
| Hệ thống | Class2Test Preflight của ARROW |
| Phiên bản mục tiêu | `0.1.0` |
| Đặc tả nguồn | `../preflight.md` |
| Phạm vi code | `preflight_tool/preflight/` |
| Framework kiểm thử | `pytest`, `pytest-cov`, `hypothesis`, `jsonschema`, `mutmut` |
| Baseline mã đã đối chiếu | HEAD `f8dd5d3`; bản sửa runner đa nền tảng `2057cbe` |
| Lần rà soát kế hoạch | 2026-09-23 |
| Trạng thái | Có thể chạy suite hiện hữu; chưa chứng nhận đầy đủ đặc tả hoặc full experiment |

### 1.1. Bằng chứng rà soát baseline

Kết quả lần rà soát trước trên cùng HEAD: macOS, Python 3.12.13, đủ JDK local
8/11/17/21, `RUN_PERFORMANCE=1`, `PREFLIGHT_JDK_*` được nạp từ config và JDK 17
được thêm vào PATH của tiến trình test: **175 passed**, lines **96.32%**, branches
**89.69%**, policy branch **100%**. Đây là bằng chứng của suite hiện có, không
phải toàn bộ ca dự kiến trong tài liệu này; lần sửa tài liệu không tạo một kết
quả thực thi mới.

Nếu không cấu hình Java cho tiến trình test, ca
`test_fast_run_is_prechecked_and_never_strict` có thể nhận `JDK_UNSUPPORTED`:
fixture mock build nhưng vẫn gọi `_java_env()` thật với mapping rỗng. Bốn ca
`test_real_jdk_version_matrix_when_configured` chỉ đọc `PREFLIGHT_JDK_*`, không
tự đọc config TOML; thiếu biến thì skip. Xem mục 19 để chạy đúng.

Commit `2057cbe` đã sửa helper path, chọn wrapper theo OS, kiểm java/javac và
major version, kill process group POSIX, lock worktree add/remove và giới hạn
future chờ ở `2 × workers`. Các test tương ứng đã pass trên macOS; không suy ra
Windows/Linux đã pass. Lỗi helper `C:/secret.java` cũ không còn là blocker mở.

Kết quả dataset lịch sử ở baseline cũ: `362,414` JSON → `85,819` CUT, `0`
input rejection trong `--index-only`. Chưa phải bằng chứng build/probe toàn bộ
dataset trên HEAD này. Kiểm java/javac thật cũng chưa thay thế Maven/Gradle
compile thật với JDK tương ứng.

### 1.2. Cách đọc trạng thái kiểm thử

- **Có test**: tìm được hàm test trong source; không có nghĩa đã pass trên mọi OS.
- **Một phần**: chỉ mock process hoặc kiểm một biến thể, chưa đủ yêu cầu.
- **Cần bổ sung**: test dự kiến chưa có implementation; không tính là pass/skip.
- **GAP**: code chưa có chức năng hoặc khác đặc tả; giữ expected theo đặc tả,
  không sửa expected để hợp thức hóa hành vi hiện tại.

Các bảng ca kiểm thử mục 8–13 là danh mục yêu cầu. Trạng thái bao phủ thực tế
nằm ở mục 18. ID trong tài liệu là ID kế hoạch, không phải pytest node ID.
`tests/traceability.md` dùng hệ ID cũ; không dùng số ID ở đó để suy số test.

### 1.3. Truy vết đặc tả → nhóm test

| Mục trong `preflight.md` | Nhóm ca trong kế hoạch | Nguồn test hiện có / phần cần bổ sung |
| --- | --- | --- |
| 1, 4: chỉ đọc, cache/workspace riêng | IT-001..003, IT-010, E2E-005 | `tests/integration/test_git_and_runner.py`; bổ sung hash trước/sau và probe cleanup khi lỗi |
| 2, 8: trạng thái/eligibility | UT-POL-001..005, BT-001..004, BT-012 | `tests/business/test_policy.py`; thêm xung đột trạng thái qua runner thật |
| 3: SHA, provenance, locked input | UT-ING-012, UT-REV-001..012, UT-REV-013..014, E2E-003 | `tests/unit/test_ingest.py`, `test_revision.py`; locked input là GAP |
| 5: module/build/JDK | UT-BLD-001..016, IT-004..009 | `test_runner_helpers.py`, `test_preflight_status_paths.py`; compile thật cần bổ sung |
| 6: class/DAO/SQL | UT-AST-001..017, BT-005..011, BT-015..016 | `tests/business/test_class_policy.py`, `test_java_ast.py`; thêm non-Java và dependency của CUT |
| 7: API/probe | UT-AST-010..012, UT-PRB-001..007, IT-004..007 | helper có test sinh chuỗi; cần compile probe thật |
| 9: output/audit | UT-OUT-001..007, E2E-001..006 | `test_output.py`, `test_cli_portfolio.py`; strict CLI hiện mock runner |
| 10: ví dụ quyết định | BT-001..016, E2E-005 | fixture mix theo mục 11, không dùng kết quả sinh ngẫu nhiên làm oracle |
| 11: ranh giới validation | UT-PRB-007, E2E-005 | xác nhận compile-only; không sinh LLM hoặc gán `VALID` |
| 12: tái lập | UT-ING-009, NF-PERF-004, mục 13.2 | `tests/nonfunctional/test_stability.py`; mở rộng nhiều worker và full runner |

## 2. Mục tiêu kiểm thử

Kế hoạch này xác minh rằng Preflight:

1. Đọc đúng dataset Classes2Test và gộp nhiều test case về đúng một CUT.
2. Giữ được bằng chứng nguồn, hash nội dung và provenance của revision.
3. Không diễn giải revision chưa xác minh thành revision gốc.
4. Clone/cache/checkout repository an toàn và không làm bẩn source repository.
5. Phát hiện đúng module, Maven/Gradle, JDK và test framework.
6. Phân loại đúng CUT được accept, bị loại, cần review hoặc integration-only.
7. Tính đúng API có thể truy cập và compile probe đúng package/module.
8. Suy ra nhất quán `technical_eligible` và `strict_eligible`.
9. Sinh đầy đủ JSONL, CSV, summary, locked manifest và eligible manifests.
10. Cho cùng input và môi trường thì kết quả có thể tái lập, không phụ thuộc thứ
    tự file hoặc số worker.

## 3. Phạm vi

### 3.1. Trong phạm vi

- CLI `class2test-preflight` và cấu hình TOML.
- Ingest, kiểm tra schema tối thiểu, hash và SQLite index.
- Deduplicate theo project, repository, class path và class identity.
- Revision map, Git mirror, tìm revision theo content và detached worktree.
- Java AST, source set, FQN, API count, dependency tags và nhận diện generated
  annotation dạng simple name/FQN.
- Maven/Gradle/JDK detection, compile stages và failure classification.
- Compile probe cho JUnit 4, JUnit 5 và TestNG.
- Policy trạng thái, eligibility và strict manifest.
- Output contract, JSON schemas, log và cleanup.
- Hiệu năng ingest, tính ổn định, đồng thời và an toàn đường dẫn.

### 3.2. Ngoài phạm vi

- Gọi LLM và chất lượng nội dung unit test do LLM sinh.
- Coverage, mutation score và test smell của generated test.
- Pha validation sau generation: baseline suite, discovery, target-test pass và
  module regression. Pha này cần kế hoạch riêng khi implementation được thêm.
- Sửa source, dependency hoặc build file của repository bên ngoài.

## 4. Rủi ro chất lượng chính

| ID | Rủi ro | Mức độ | Kiểm soát kiểm thử |
| --- | --- | --- | --- |
| R-01 | Chọn sai revision nhưng vẫn đưa vào strict experiment | Nghiêm trọng | Unit revision map, business eligibility, strict E2E |
| R-02 | Gộp nhầm hai CUT hoặc sinh task ID không ổn định | Cao | Unit ingest, property-based và determinism |
| R-03 | Chạy build ở sai module/JDK | Cao | Unit build-plan và toolchain integration |
| R-04 | Interface/abstract/generated/test source được accept | Cao | Business decision table và AST tests |
| R-05 | DAO/JDBC bị loại chỉ vì import hoặc tên file | Cao | Business dependency-tag tests |
| R-06 | Probe compile sai package/API hoặc làm bẩn repository | Cao | Unit probe và Git/worktree integration |
| R-07 | Lỗi network/dependency bị ghi nhầm thành lỗi source | Trung bình | Failure-classification tests |
| R-08 | Output thiếu bằng chứng hoặc không khớp schema | Cao | Contract/schema/E2E tests |
| R-09 | Race condition khi nhiều CUT dùng cùng mirror | Cao | Concurrency/integration tests |
| R-10 | Build script độc hại đọc secret hoặc sửa máy chạy | Nghiêm trọng | Sandbox VM, không secrets, security tests |
| R-11 | 362.414 JSON gây OOM hoặc chạy không kết thúc | Cao | Performance/full-dataset soak test |
| R-12 | Kết quả thay đổi giữa hai lần chạy | Cao | Determinism và golden-output tests |

## 5. Chiến lược kiểm thử

```text
Static checks
  -> Unit tests
  -> Business-policy tests
  -> Local integration tests
  -> Local end-to-end portfolio
  -> Toolchain compatibility
  -> Pinned remote smoke
  -> Full-dataset performance/audit
```

Unit và business tests là gate bắt buộc cho mọi commit. Integration và local
E2E cũng là gate bắt buộc nhưng chỉ dùng repository tạm cục bộ. Remote smoke
không chặn CI vì phụ thuộc mạng; kết quả của nó phải được lưu riêng. Full
dataset chạy theo lịch hoặc trước experiment chính thức.

## 6. Môi trường kiểm thử

### 6.1. Ma trận nền tảng

| Thành phần | Gate bắt buộc | Tương thích mở rộng |
| --- | --- | --- |
| OS | Windows 11 x64 và macOS hiện hành | Ubuntu LTS |
| Python | 3.11, 3.12 | 3.13 sau khi dependency hỗ trợ |
| Git | Bản stable hiện hành, long-path bật trên Windows | Bản stable Linux/macOS |
| Maven | Wrapper dự án và Maven 3.8/3.9 | Historical Maven nếu project yêu cầu |
| Gradle | Wrapper dự án | Gradle host chỉ khi không có wrapper |
| JDK | 8, 11, 17, 21, từng full version/build được ghi lại | Tool không hard-code whitelist, nhưng task chuẩn bị/chạy này không tự tải hay cấu hình major ngoài ma trận; ghi status và bằng chứng thực tế |
| Filesystem | NTFS, đường dẫn có dấu và khoảng trắng | Case-sensitive filesystem |

### 6.2. An toàn môi trường

- Remote/toolchain tests chạy trong VM hoặc container dùng một lần.
- Không gắn API key, SSH key, cloud credential hoặc thư mục cá nhân vào VM.
- Repository bên ngoài được coi là mã không tin cậy vì Maven/Gradle có thể chạy
  arbitrary build plugin.
- Network outbound chỉ mở cho Git và dependency registries cần thiết; ghi log
  hostname truy cập nếu môi trường hỗ trợ.
- Giới hạn CPU, RAM, dung lượng đĩa và timeout cho từng build.

### 6.3. Điều kiện máy thành viên sau khi tải Java

1. Python >=3.11, Git và dependency `.[test]` của tool đã cài trong venv.
2. `Java-version/runtime/jdk-{8,11,17,21}/` có JDK đúng OS/CPU, java/javac
   đã verify; config `Java-version/config.local.toml` map tới JAVA_HOME thực
   (macOS có thể có `Contents/Home`). CLI không tự tìm runtime này.
3. Dataset có cấu trúc `dataset/<project-id>/*.json`; cấu hình input trỏ đúng
   máy. Lệnh khởi tạo dùng output mới; full run có checkpoint có thể tiếp tục
   bằng `--resume --output-dir <output-cũ>` khi code/schema và các đường dẫn
   dataset/JDK tuyệt đối vẫn giữ nguyên.
4. Muốn build cần wrapper trong repo hoặc Maven/Gradle trên máy, phiên bản
   tương thích với JDK và project lịch sử. Không ép một Gradle mới chạy với
   mọi JDK cũ; phiên bản build tool là thành phần của fixture.
5. Chạy index-only trước, rồi smoke discovery trong môi trường build cô lập.
   Chỉ chạy strict khi có revision map audit; Java đầy đủ không tạo provenance.

## 7. Dữ liệu kiểm thử

| Bộ dữ liệu | Mục đích |
| --- | --- |
| Synthetic JSON tối thiểu | Unit ingest/schema/hash |
| JSON BOM, Unicode, malformed, thiếu field | Robustness và input rejection |
| Nhiều JSON cùng CUT | Deduplicate/evidence aggregation |
| Local Git Maven đơn module | Integration cơ bản |
| Local Git Maven multi-module | `-pl/-am`, module root và fallback |
| Local Git Gradle multi-project | Project path và wrapper |
| Revision map JSONL/CSV hợp lệ và không hợp lệ | Provenance/business policy |
| Java source portfolio | interface, enum, record, abstract, nested, generated, DTO, utility, DAO |
| Path security portfolio | POSIX absolute, `..`, Windows drive/UNC/device path, slash ngược, symlink escape |
| Ba remote repo đã audit và pin full SHA | Remote smoke |
| 10.000 synthetic JSON | Performance gate nhanh |
| Toàn bộ 362.414 Classes2Test JSON | Full-dataset audit |

Mọi remote fixture phải pin full SHA và lưu expected result. Không dùng branch
name hoặc HEAD động làm expected baseline.

## 8. Kiểm thử unit

### 8.1. Ingest và index

| ID | Ca kiểm thử | Kết quả mong đợi | Ưu tiên |
| --- | --- | --- | --- |
| UT-ING-001 | Input root chứa `dataset/` | Resolve đúng `dataset/` | P0 |
| UT-ING-002 | Input root chính là dataset | Dùng trực tiếp root | P1 |
| UT-ING-003 | JSON UTF-8 BOM và đường dẫn Unicode | Parse/hash/index thành công | P0 |
| UT-ING-004 | JSON sai cú pháp | Ghi `INPUT_JSON_INVALID`, không dừng toàn run | P0 |
| UT-ING-005 | Encoding không hợp lệ | Ghi `INPUT_ENCODING_INVALID` | P1 |
| UT-ING-006 | Thiếu repo URL/class path/class name | Ghi `INPUT_SCHEMA_INVALID` | P0 |
| UT-ING-007 | Nhiều JSON cùng CUT | Một candidate, nhiều evidence/hash/test path | P0 |
| UT-ING-008 | Cùng tên class nhưng khác path/repo | Không gộp nhầm | P0 |
| UT-ING-009 | Thứ tự file thay đổi | Candidate/task ID/order không đổi | P0 |
| UT-ING-010 | `--limit` tại ranh giới 0/1/N | Đếm JSON và class chính xác | P1 |
| UT-ING-011 | Chạy index lại trên DB hiện có | Không nhân đôi class/evidence | P1 |
| UT-ING-012 | Hash source JSON | Khớp SHA-256 của raw bytes | P0 |

### 8.2. Revision và Git

| ID | Ca kiểm thử | Kết quả mong đợi | Ưu tiên |
| --- | --- | --- | --- |
| UT-REV-001 | Revision map JSONL/CSV hợp lệ | Load đúng `UPSTREAM_PINNED` | P0 |
| UT-REV-002 | SHA không đủ 40 ký tự | Reject map trước khi clone | P0 |
| UT-REV-003 | Provenance `manual/git_history` nhưng khai pinned | Reject | P0 |
| UT-REV-004 | Thiếu `evidence_ref` | Reject | P0 |
| UT-REV-005 | Pinned commit không tồn tại | `COMMIT_MISSING` | P0 |
| UT-REV-006 | Focal/test path cùng tồn tại tại nhiều commit | Chọn theo content score và tie-break ổn định | P0 |
| UT-REV-007 | Có path nhưng snippet không khớp | `UNVERIFIED`, không strict | P0 |
| UT-REV-008 | Focal body và test body khớp | `CONTENT_MATCHED`, discovery-only | P0 |
| UT-REV-009 | Comment marker trong string/text block | Normalize không làm hỏng literal | P1 |
| UT-REV-010 | `max_revision_candidates` đạt giới hạn | Không đọc quá giới hạn, ghi evidence đúng | P1 |
| UT-REV-011 | Mirror đã tồn tại | Remote update/prune, không clone lại | P1 |
| UT-REV-012 | Git command/network lỗi | Lỗi cô lập theo candidate và có log | P0 |
| UT-REV-013 | SHA dài 40 nhưng không phải hex; task_id trùng với hai SHA | Reject input mâu thuẫn/sai trước build; đã có regression JSONL/CSV | P0 |
| UT-REV-014 | Locked input có hash sai hoặc file nguồn bị đổi | Không chạy strict với evidence sai; cần triển khai boundary nhận/validate locked manifest | P0 |

### 8.3. Java AST và class policy

| ID | Ca kiểm thử | Kết quả mong đợi | Ưu tiên |
| --- | --- | --- | --- |
| UT-AST-001 | Concrete/final/package-private class | Parse và accept theo visibility | P0 |
| UT-AST-002 | Interface/annotation | `EXCLUDE_UNSUPPORTED_DECLARATION` | P0 |
| UT-AST-003 | Enum/record | `EXCLUDE_STRICT_COHORT` | P0 |
| UT-AST-004 | Abstract class | `EXCLUDE_ABSTRACT_CLASS` | P0 |
| UT-AST-005 | Nested/local/anonymous class làm target | Exclude | P0 |
| UT-AST-006 | `@Generated`, `@javax.annotation.Generated`, `@jakarta.annotation.Generated`, import `*.Generated`, generated comment/path | `EXCLUDE_GENERATED_SOURCE` | P0 |
| UT-AST-007 | Source dưới `src/test` | `EXCLUDE_TEST_SOURCE` | P0 |
| UT-AST-008 | Source set không xác định | `EXCLUDE_NON_PRODUCTION_SOURCE` | P1 |
| UT-AST-009 | Package + declaration khác provisional FQN | Nâng cấp FQN theo source; mismatch có reason | P0 |
| UT-AST-010 | Default/private/protected/package constructor | Count đúng constructor truy cập được | P0 |
| UT-AST-011 | Static/private/inherited/Object methods | Count đúng; không dùng Object làm API duy nhất | P0 |
| UT-AST-012 | Generics, array, varargs, nested type, annotation | Parse signature và primitive arguments đúng | P1 |
| UT-AST-013 | JDBC/JPA/HTTP/file-system imports | Chỉ gắn tag, không tự loại | P0 |
| UT-AST-014 | `DriverManager.getConnection` | Gắn `DIRECT_CONNECTION`, chưa tự integration-only | P0 |
| UT-AST-015 | Target `.sql`, XML, YAML; kể cả nội dung có chuỗi giống Java | `EXCLUDED` + `EXCLUDE_NOT_JAVA_SOURCE` theo đặc tả 6.3 | P0 |
| UT-AST-016 | `@NotGenerated`, import Generated không được sử dụng | Không tự kết luận generated chỉ vì substring/import; cần oracle có bằng chứng annotation thực | P1 |
| UT-AST-017 | Inherited overload cùng tên, parent khác package | Count theo signature và accessibility thực, không bỏ overload hoặc tính member không truy cập được | P0 |

### 8.4. Build, process và probe

| ID | Ca kiểm thử | Kết quả mong đợi | Ưu tiên |
| --- | --- | --- | --- |
| UT-BLD-001 | Maven single/multi-module | Đúng root, module, `-pl/-am` và fallback | P0 |
| UT-BLD-002 | Gradle root/subproject | Đúng `classes`/`testClasses` task | P0 |
| UT-BLD-003 | Có `.cmd`/shell wrapper | Chọn wrapper đúng OS trước host tool | P0 |
| UT-BLD-004 | JDK release 8/11/17/21 | Chọn đúng home và ghi version thực chạy | P0 |
| UT-BLD-005 | Không có executable | `BUILD_TOOL_UNSUPPORTED` | P0 |
| UT-BLD-006 | Main fail/test compile fail | Phân biệt hai trạng thái | P0 |
| UT-BLD-007 | Dependency resolve fail | `DEPENDENCY_UNAVAILABLE` | P1 |
| UT-BLD-008 | Invalid target release | `JDK_UNSUPPORTED` | P1 |
| UT-BLD-009 | Process timeout | Kill process tree, `BUILD_TIMEOUT`, giữ log | P0 |
| UT-BLD-010 | POSIX absolute hoặc `..` trong path CUT/test | Reject trước khi tạo candidate; không đọc/ghi ngoài workspace | P0 |
| UT-BLD-011 | `C:/...`, `C:\\...`, `\\\\server\\share`, `//server/share`, `\\\\?\\...` và slash ngược có `..` | Reject theo **cú pháp Windows trên cả Windows, macOS và Linux**, không phụ thuộc `Path.is_absolute()` của host | P0 |
| UT-BLD-012 | Symlink trong workspace trỏ ra ngoài; path hợp lệ dùng `/` và `\\` | Resolve canonical path rồi reject symlink escape; hai cách phân tách path hợp lệ cùng cho kết quả đúng | P0 |
| UT-BLD-013 | Thiếu java/javac, launcher exit !=0, version không parse được, major mismatch | `JDK_UNSUPPORTED` kèm reason `JDK_JAVA_MISSING`, `JDK_JAVAC_MISSING`, `JDK_VERSION_COMMAND_FAILED`, `JDK_VERSION_UNPARSEABLE` hoặc `JDK_VERSION_MISMATCH` | P0 |
| UT-BLD-014 | Có wrapper đúng OS ở ngoài workspace | Không sử dụng wrapper ngoài workspace | P0 |
| UT-BLD-015 | N=20/100 candidate, workers=1/2/4 | Outstanding future <=2×workers; không mất/lặp task; output sort theo task_id | P0 |
| UT-BLD-016 | Target JDK từ parent POM/property/toolchain Gradle | Chọn theo effective configuration hoặc ghi rõ chưa hỗ trợ; parser regex hiện chỉ bao phủ một phần | P1 |
| UT-PRB-001 | JUnit4/JUnit5/TestNG | Probe dùng annotation/import đúng | P0 |
| UT-PRB-002 | Constructor/static/instance API | Probe compile đúng loại lời gọi | P0 |
| UT-PRB-003 | Primitive, reference, array, varargs arguments | Sinh argument compile được | P1 |
| UT-PRB-004 | Method ném checked exception | Probe compile với khai báo phù hợp | P1 |
| UT-PRB-005 | Framework unknown | Không báo probe pass giả | P0 |
| UT-PRB-006 | `--fast` | `PRECHECKED`, `NOT_RUN`, không technical/strict eligible | P0 |
| UT-PRB-007 | Probe nếu chạy sẽ ghi sentinel hoặc ném lỗi | Chỉ compile, sentinel không xuất hiện, không chạy test; probe bị xóa cả khi compile lỗi | P0 |

### 8.5. Policy, CLI và output

| ID | Ca kiểm thử | Kết quả mong đợi | Ưu tiên |
| --- | --- | --- | --- |
| UT-POL-001 | Mọi `PreflightStatus` riêng lẻ | Chọn được đúng status | P0 |
| UT-POL-002 | Mọi cặp và tổ hợp đại diện qua 8 nhóm priority | Status chính theo priority, giữ mọi reason; có test intra-group | P0 |
| UT-POL-003 | Strict + pinned + eligible | Technical và strict cùng true | P0 |
| UT-POL-004 | Discovery/content-matched + eligible | Technical true, strict false | P0 |
| UT-POL-005 | Prechecked/needs-review/integration-only | Cả hai eligibility false | P0 |
| UT-OUT-001 | JSONL/CSV chứa list/nested object | Round-trip không mất dữ liệu | P0 |
| UT-OUT-002 | Record theo schema | Validate thành công | P0 |
| UT-OUT-003 | Strict manifest sai điều kiện | Schema reject | P0 |
| UT-OUT-004 | Output path đã tồn tại | Không ghi đè bằng chứng cũ | P0 |
| UT-OUT-005 | Đường dẫn có dấu/khoảng trắng | CLI và output hoạt động đúng | P1 |
| UT-OUT-006 | Config thiếu/sai kiểu/giá trị âm | Fail-fast với thông báo rõ | P1 |
| UT-OUT-007 | Negative schema: eligible nhưng unpinned/discovery, thiếu field, api_count sai | Schema/business validator reject; test schema hiện chủ yếu ca hợp lệ | P0 |

## 9. Kiểm thử nghiệp vụ

Kiểm thử nghiệp vụ xác nhận quyết định cuối cùng, không chỉ hành vi của từng
hàm. Mỗi rule dưới đây cần test theo decision table và ít nhất một ca phủ định.

| ID | Tình huống nghiệp vụ | Kết quả mong đợi | Ưu tiên |
| --- | --- | --- | --- |
| BT-001 | `STRICT + UPSTREAM_PINNED + ELIGIBLE` | `strict_eligible=true` | P0 |
| BT-002 | `DISCOVERY_ONLY` dù build/probe pass | Technical true, strict false | P0 |
| BT-003 | `CONTENT_MATCHED` dù build/probe pass | Technical true, strict false | P0 |
| BT-004 | Probe bị bỏ qua trong fast mode | Không được gọi `ELIGIBLE` | P0 |
| BT-005 | DTO chỉ có accessor/constructor | Accept với `LOW_LOGIC`/`CONSTRUCTOR_ONLY` | P1 |
| BT-006 | Static utility có private constructor | Accept nếu có static API truy cập được | P0 |
| BT-007 | Target là interface nhưng dependency của CUT khác | Interface target bị loại; dependency không làm CUT khác bị loại | P0 |
| BT-008 | Concrete DAO inject `DataSource` | Accept + `DB_DEPENDENT`/`REQUIRES_MOCK` | P0 |
| BT-009 | DAO tự mở connection nhưng chưa đủ bằng chứng | `NEEDS_REVIEW`, không strict | P0 |
| BT-010 | Có bằng chứng cần hạ tầng thật theo quy tắc đã công bố | `INTEGRATION_ONLY`; runner chưa triển khai cơ chế áp dụng rule, xem GAP-02 | P1 |
| BT-011 | Tên production class kết thúc `Test` | Chỉ gắn `SUSPECT_TEST_NAME`; source set quyết định | P1 |
| BT-012 | Nhiều lỗi: build timeout + source invalid | Status chính timeout, giữ cả reason codes | P0 |
| BT-013 | Pinned SHA hợp lệ nhưng source path không tồn tại | `SOURCE_INVALID`, không eligible | P0 |
| BT-014 | FQN trong input khác package tại SHA | Ghi FQN thực tế hoặc reject theo contract; không silent pass | P0 |
| BT-015 | Annotation generated viết bằng FQN thay vì import/simple name | Loại như generated source, không lọt vào strict cohort | P0 |
| BT-016 | Cùng CUT có class/test path absolute kiểu Windows (drive, UNC hoặc device path) trên host POSIX | Bị từ chối nhất quán như input không an toàn, không âm thầm diễn giải thành tên file hợp lệ | P0 |

## 10. Kiểm thử integration

| ID | Luồng | Bằng chứng pass | Ưu tiên |
| --- | --- | --- | --- |
| IT-001 | Local Git -> mirror -> revision -> worktree -> cleanup | Source repo sạch; worktree tạm bị xóa | P0 |
| IT-002 | Hai candidate cùng repository chạy song song | Không race mirror, mỗi task một kết quả | P0 |
| IT-003 | Nhiều JSON cùng CUT | Chỉ một build/probe, giữ toàn bộ evidence | P0 |
| IT-004 | Maven single-module thực | Main/test compile và probe pass | P0 |
| IT-005 | Maven multi-module thực | Build đúng module và module phụ thuộc | P0 |
| IT-006 | Gradle multi-project thực | Đúng project task, wrapper và probe source set | P0 |
| IT-007 | JDK matrix với Maven/Gradle fixture tương thích | Compile thật đúng JDK hoặc unsupported có bằng chứng; không chỉ java/javac -version | P0 |
| IT-008 | Build timeout | Process con bị dừng; log/status/output vẫn được ghi | P0 |
| IT-009 | Mirror update/prune | Commit mới fetch được, stale ref không ảnh hưởng pinned SHA | P1 |
| IT-010 | `keep_workspaces=true/false` | Giữ/xóa đúng policy, không làm bẩn mirror | P1 |
| IT-011 | Candidate lỗi giữa run nhiều candidate | Candidate khác vẫn hoàn tất | P0 |
| IT-012 | Cấu hình integration_rule_file có/không/sai | Khi cơ chế được chốt, áp dụng đúng hoặc reject; hiện config bị bỏ qua, không tính pass | P1 |

## 11. Kiểm thử end-to-end

| ID | Kịch bản | Kết quả bắt buộc | Ưu tiên |
| --- | --- | --- | --- |
| E2E-001 | `--index-only` | Candidate manifest/index/rejection đúng, không clone/build | P0 |
| E2E-002 | Discovery run không revision map | Locked manifest + technical manifest; strict manifest rỗng | P0 |
| E2E-003 | Strict run với revision map audit | Chỉ pinned+eligible xuất hiện trong strict manifest | P0 |
| E2E-004 | Fast run | Không có strict eligible; probe `NOT_RUN` | P0 |
| E2E-005 | Mixed portfolio theo ma trận class/path | Summary/count/reason code đúng decision table, gồm generated FQN và Windows absolute/UNC path độc hại | P0 |
| E2E-006 | Output directory có dữ liệu cũ | Từ chối ghi đè và giữ nguyên evidence cũ | P0 |
| E2E-007 | Đường dẫn Unicode/khoảng trắng trên Windows | Toàn luồng hoàn tất | P1 |
| E2E-008 | Ba remote repo pinned: Maven, multi-module, Gradle | Expected status khớp; không dùng branch động | P1, non-blocking |

Portfolio E2E tối thiểu gồm: concrete class, DTO, constructor-only, static
utility, interface/annotation, abstract, enum/record, nested, generated
simple-name, generated FQN, test source, DAO inject dependency, DAO direct
connection và class không có API truy cập được. Path độc hại là fixture tách
biệt: không được tạo worktree/build cho fixture đó.

## 12. Kiểm thử hồi quy

- Mỗi defect phải có test tái hiện trước khi sửa và được giữ lâu dài.
- Golden fixtures phải pin SHA, config và expected output; không snapshot log có
  timestamp hoặc absolute temp path không ổn định.
- Khi thay policy/schema, cập nhật version, migration note, traceability và
  test dữ liệu cũ.
- Mọi thay đổi Java AST phải có ca hồi quy theo từng syntax đã hỗ trợ. Riêng
  thay đổi `Generated` phải kiểm tra simple annotation, FQN annotation, import
  và comment/path generated để tránh chỉ sửa một spelling.
- Mọi thay đổi path phải kiểm tra cú pháp POSIX **và** Windows trên cả ba OS;
  không mock hoặc dựa vào riêng `Path.is_absolute()` của host.
- So sánh summary trước/sau trên một portfolio cố định; mọi thay đổi số lượng
  status phải được review.
- Chạy mutation test riêng cho `revision.py` và `policy.py`; mutation sống sót
  trong logic P0 phải được kill hoặc ghi lý do equivalent rõ ràng.

## 13. Kiểm thử phi chức năng

### 13.1. Hiệu năng và tài nguyên

| ID | Tải | Ngưỡng pass |
| --- | --- | --- |
| NF-PERF-001 | Index 10.000 JSON thành một CUT | <= 120 giây, RAM tăng < 512 MiB |
| NF-PERF-002 | Index 10.000 JSON nhiều project/class | Không OOM; order ổn định; ghi thời gian/RAM |
| NF-PERF-003 | Full 362.414 JSON | Hoàn tất, count khớp, không corruption/OOM; lần đầu lập baseline |
| NF-PERF-004 | Workers 1/2/4/8 trên cùng portfolio | Kết quả giống nhau; speedup và contention được ghi |
| NF-SOAK-001 | Run discovery dài với tối thiểu 100 CUT | Không leak worktree/process/file handle |

Sau full run đầu tiên, lấy median của ba lần làm baseline và đặt gate hồi quy
thời gian ở mức không chậm hơn 20%, trên cùng máy và cache state.

### 13.2. Tính ổn định và tái lập

- Chạy index 20 lần phải tạo cùng task ID, ordering và evidence hash.
- Cùng checkout SHA/config/JDK/timeout phải cho cùng status/reason/tag.
- Thay số worker không được thay nội dung output sau khi bỏ field thời gian/log
  path mang tính môi trường.
- Kết quả CSV và JSONL phải có số record/eligibility giống nhau.

### 13.3. Bảo mật

| ID | Ca kiểm thử | Kết quả mong đợi |
| --- | --- | --- |
| NF-SEC-001 | `../`, absolute POSIX, drive/UNC/device Windows path, slash ngược và symlink escape | Không đọc/ghi ngoài workspace; quyết định giống nhau trên Windows/macOS/Linux |
| NF-SEC-002 | Repo URL/class path chứa shell metacharacter | Không shell interpolation; argument giữ nguyên |
| NF-SEC-003 | Symlink Git checkout/probe destination trỏ ra ngoài | Không đọc/ghi ngoài workspace; kiểm archive JDK thuộc setup Java, ngoài runner |
| NF-SEC-004 | Build script đọc environment | VM không chứa secrets; biến nhạy cảm không được truyền |
| NF-SEC-005 | Build sinh process con treo | Timeout dừng toàn process tree |
| NF-SEC-006 | Log chứa credential/token | Redact trước khi lưu artifact |

### 13.4. Khả dụng và quan sát

- CLI lỗi phải nêu task, stage, command và log path; không chỉ in stack trace.
- Đối soát tổng JSON/candidate trong `provenance.json`, rejection trong
  `reports/input_rejections.jsonl`, status/eligibility trong `summary.json`.
  `--index-only` hiện không sinh summary hay preflight_results; không test đòi
  các output này ở index-only.
- Ctrl+C hoặc lỗi hệ thống phải cleanup worktree và đóng SQLite an toàn.
- Log dùng UTF-8 và đọc được đường dẫn/tên class Unicode.

## 14. Coverage và quality gate

Trước khi merge hoặc chạy experiment chính thức:

- 100% test P0 pass; không flaky test chưa xử lý.
- Unit/business security gate phải pass trên Windows và macOS; không chấp nhận
  xfail cho parser path đa nền tảng.
- Tối thiểu 90% statement coverage và 85% branch coverage toàn package.
- 100% branch coverage cho `preflight.policy`.
- `check_coverage.py` chỉ kiểm tỷ lệ toàn package; kiểm policy branch riêng
  trong `coverage.json`. Skip toolchain vì thiếu JDK là chưa kiểm, không pass.
- JSON schemas validate mọi E2E output.
- Mutation gate cho revision/policy không còn mutation P0 sống sót chưa review.
- Local Git integration và E2E portfolio pass.
- Remote smoke pinned được chạy trên VM sạch và có báo cáo, nhưng không chặn
  local CI khi mạng ngoài lỗi.
- Không còn defect severity Critical/High mở.

## 15. Điều kiện bắt đầu và kết thúc

### 15.1. Entry criteria

- `preflight.md`, schemas và config contract đã version hóa.
- Dependency test cài được trên máy/CI sạch.
- Local fixtures không phụ thuộc network.
- Remote fixtures có full SHA, license phù hợp và expected status đã audit.
- JDK paths và resource limits được cấu hình.

### 15.2. Exit criteria

- Hoàn thành toàn bộ gate mục 14.
- Traceability từ mỗi rule P0 đến ít nhất một automated test.
- Full-dataset index đối chiếu được tổng input và rejection.
- Có danh sách known limitations và manual review queue.
- Test report lưu command, commit, OS, Python/JDK/build-tool version, duration,
  coverage, mutation và artifact hashes.

## 16. Mức độ lỗi và xử lý

| Severity | Ví dụ | Quy tắc |
| --- | --- | --- |
| Critical | Entry unpinned vào strict manifest; path escape; source repo bị sửa | Dừng release/experiment |
| High | Sai status/API count/module/JDK; mất evidence; output sai schema | Phải sửa trước merge |
| Medium | Error message khó hiểu; tag phụ sai; performance regression có workaround | Sửa trước full run hoặc có waiver |
| Low | Typo/tài liệu/format không ảnh hưởng quyết định | Đưa backlog |

Mỗi bug report cần: test ID, task ID, input hash, checkout SHA, config, command,
expected/actual, status/reason codes, log path, môi trường và khả năng tái hiện.

## 17. Thứ tự triển khai test

1. Hoàn thiện unit tests P0 cho ingest, revision, AST, build, probe và policy.
2. Hoàn thiện business decision table và schema contract.
3. Thêm Maven/Gradle/JDK local integration fixtures.
4. Chạy local E2E mixed portfolio và determinism.
5. Chạy coverage + mutation gates, xử lý mutation P0.
6. Chạy remote smoke pinned trong VM sạch.
7. Chạy performance 10.000 JSON.
8. Chạy index/preflight trên toàn Classes2Test theo từng batch, lưu audit.

## 18. Trạng thái bao phủ ở HEAD `f8dd5d3`

### 18.1. Đã có test tự động trong source tree

| Nhóm kế hoạch | Pytest file/hàm làm bằng chứng | Mức bao phủ |
| --- | --- | --- |
| UT-ING-001..004, 006..010, 012 | `tests/unit/test_ingest.py` | Có test các ca cơ bản; chưa đủ mọi boundary/property-based |
| UT-REV-001..005, 008..009 | `tests/unit/test_revision.py`, integration local Git | Một phần; chưa đủ text block, multi-commit tie-break và locked input |
| UT-AST, BT-005..009, 011, 015 | `tests/unit/test_java_ast.py`, `tests/business/test_class_policy.py` | Có declaration/tag/API cơ bản; tag REQUIRES_MOCK không chứng minh mock thành công |
| UT-POL, BT-001..004 | `tests/business/test_policy.py` | Có bảng priority và eligibility cho helper; chưa chứng minh runner giữ mọi reason |
| UT-BLD-003, 010..014 | `tests/unit/test_runner_helpers.py`: `test_detect_build_prefers_platform_wrapper_and_module_commands`, `test_path_candidates_reject_unsafe_paths_on_every_platform`, `test_path_candidates_normalize_separators_and_contain_symlinks`, `test_java_environment_validates_both_tools_and_major_version` | Có test, phải chạy trên mỗi OS; không dùng mock OS làm chứng nhận Windows |
| UT-BLD-009, IT-008 | `test_timeout_stops_child_process_on_host_platform` trong file helper | Process thật trên host đang chạy; chưa chứng nhận mọi build tool/OS |
| UT-BLD-015, IT-002 | `test_run_all_limits_submissions_between_completion_waits`; `tests/integration/test_git_and_runner.py::test_parallel_workers_have_stable_nonduplicated_results` | Bounded queue bằng test helper; hai CUT thật chung repo nhưng fixture không build |
| IT-001, fast mode | `tests/integration/test_git_and_runner.py` | Git/worktree thật; fast build được mock, còn Java môi trường chưa được cô lập |
| JDK launcher matrix | `test_real_jdk_version_matrix_when_configured` trong file helper, marker `toolchain` | Bốn launcher thật nếu PREFLIGHT_JDK_* được set; chưa compile project |
| UT-PRB-001..003 | `test_probe_source_covers_constructor_static_and_instance_calls` | Kiểm chuỗi source; chưa chứng minh compile thật |
| UT-OUT, E2E-001/006 | `tests/unit/test_output.py`, `tests/e2e/test_cli_portfolio.py` | CLI index-only thật và output contract; test strict output mock `cli.run_all` |
| E2E-002..005, IT-004..007 | Chưa có portfolio compile thật tương ứng | Cần bổ sung |
| NF-PERF-001, determinism index | `tests/nonfunctional/test_stability.py` | Có test; RAM hiện đo RSS trước/sau, không phải peak RSS |

### 18.2. Sai khác đặc tả cần test và xử lý

Các mục dưới là phát hiện đọc code, không gán mọi mục thành bug đã tái hiện.
Khi triển khai test, dùng expected của đặc tả và báo actual riêng.

| ID | Đặc tả / ca liên quan | Hiện trạng và điều kiện đóng |
| --- | --- | --- |
| GAP-01 | §3–4, UT-REV-014 | CLI nhận raw dataset và revision map, rồi ghi locked manifest; chưa có input locked-manifest/kiểm hash lại trước execution. Cần triển khai hoặc chốt chính thức adapter boundary, có test tampered hash |
| GAP-02 | §6.4, BT-010/IT-012 | `integration_rule_file` có trong config mẫu nhưng CLI/runner không đọc; chưa có nhánh quyết định INTEGRATION_ONLY theo evidence. Không dựng CLI flag giả để test |
| GAP-03 | §8, BT-012 | Helper priority có test nhưng runner return sớm, không gọi select_primary_status để tổng hợp mọi lỗi. Thêm fixture source invalid + build lỗi và kiểm đủ reason; không kết luận pass từ helper |
| GAP-04 | §6.3, UT-AST-015 | Chưa thấy guard đuôi file/reason EXCLUDE_NOT_JAVA_SOURCE trong runner. Thêm .sql chứa Java hợp lệ để phát hiện accept sai |
| GAP-05 | Security, UT-BLD-010..012/BT-016 | Đã sửa ingest: kiểm raw focal/test path trước strip; reject absolute/UNC/drive/device/traversal/NUL. Regression và CLI test pass trên macOS; chưa chứng nhận Windows native |
| GAP-06 | §3, UT-REV-013 | Đã sửa loader: fullmatch 40 hex, reject duplicate khác SHA/provenance/evidence; chấp nhận duplicate giống hệt sau lowercase SHA. Regression JSONL/CSV và CLI pass |
| GAP-07 | Test independence | Fast integration mock `_run` nhưng gọi Java thật với mapping rỗng. Cần mock JDK cho local deterministic gate hoặc đánh dấu/cấu hình toolchain đúng; hiện dùng PATH tạm theo mục 19 |
| GAP-08 | §5, build provenance | Lệnh build-tool --version chạy trước `_java_env`, chưa dùng env JDK được chọn. Kiểm version/log và compile cùng env, JDK inherited/property/toolchain trong UT-BLD-016 |

Lỗi helper Windows-drive path trước đây đã sửa bằng `2057cbe`; giữ test hồi
quy thay vì ghi còn fail. Không vì sửa helper mà đóng GAP-05 ở ingest.

### 18.3. Các khoảng trống còn lại trước full experiment

1. Maven/Gradle **thật** cho single-module và multi-module, không chỉ mock process.
2. Build fixture trên JDK 8/11/17/21 và OS matrix; launcher matrix macOS đã pass ở lần rà soát trước.
3. Content-match nhiều commit, tie-break và text block/string edge cases.
4. Probe compile thật cho JUnit4/JUnit5/TestNG với complex signatures.
5. Chạy lại process-tree test trên Windows/Linux; mirror recovery sau network failure.
6. Strict E2E dùng revision map pinned thực.
7. Full dataset trên baseline mới và long-run resource leak; số đếm cũ chỉ làm mốc đối chiếu.
8. Security sandbox/redaction tests cho repository không tin cậy.
9. Property-based tests cho task identity, path normalization và Java arguments.
10. Mutation gate thực tế cho `revision.py` và `policy.py`, cùng report về mutation sống sót.

## 19. Lệnh gate tham chiếu

### 19.1. Cài môi trường

Chạy từ root ARROW. Chỉ tạo venv nếu chưa có; cần Python >=3.11.
Setup Java theo `../Java-version/AGENTS.md` trước; không commit config local.

macOS (bash/zsh):

```bash
python3 -m venv preflight_tool/.venv
source preflight_tool/.venv/bin/activate
python -m pip install -e 'preflight_tool[test]'
cd preflight_tool
```

Windows PowerShell:

```powershell
py -3.12 -m venv preflight_tool/.venv
.\preflight_tool\.venv\Scripts\Activate.ps1
python -m pip install -e "preflight_tool[test]"
cd preflight_tool
```

Nếu PowerShell chặn Activate.ps1, dùng đường dẫn `.venv\Scripts\python.exe`
thay cho `python` ở các lệnh sau; không cần thay execution policy toàn máy.

### 19.2. Chạy từng lớp trước gate tổng

Sau khi venv được kích hoạt và cwd là `preflight_tool`, các lệnh dùng chung:

```text
python -m pytest --collect-only -q
python -m pytest -m unit -ra
python -m pytest -m business -ra
python -m pytest -m e2e -ra
```

Marker integration/toolchain cần Java của tiến trình test được cấu hình.
Gate tổng dưới đây nạp cả bốn PREFLIGHT_JDK_* và JDK 17 vào PATH chỉ cho process
con; không đổi Java toàn máy. Không dùng suite marker riêng làm coverage gate.

### 19.3. Gate tổng gồm integration, JDK thật và performance

Chạy cùng lệnh này trên macOS hoặc PowerShell với Python >=3.11 của venv.
Các path JDK lấy trực tiếp từ `Java-version/config.local.toml`, không hard-code
`Contents/Home` cho Windows. Coverage và JUnit XML nằm trong thư mục run mới.

```text
python -c 'import os,pathlib,subprocess,sys,tomllib; c=tomllib.loads(pathlib.Path("../Java-version/config.local.toml").read_text(encoding="utf-8")); homes=c["java"]["homes"]; e=os.environ.copy(); e.update({"PREFLIGHT_JDK_"+v:homes[v] for v in ("8","11","17","21")}); e["JAVA_HOME"]=homes["17"]; e["PATH"]=str(pathlib.Path(homes["17"])/"bin")+os.pathsep+e.get("PATH",""); e["RUN_PERFORMANCE"]="1"; out=pathlib.Path("runs/qa-20260923-01"); out.mkdir(parents=True,exist_ok=False); raise SystemExit(subprocess.call([sys.executable,"-m","pytest","--cov=preflight","--cov-branch","--cov-report=term-missing","--cov-report=json:"+str(out/"coverage.json"),"--junitxml="+str(out/"pytest.xml"),"-ra"],env=e))'
python scripts/check_coverage.py runs/qa-20260923-01/coverage.json
python -c 'import json,pathlib; c=json.loads(pathlib.Path("runs/qa-20260923-01/coverage.json").read_text()); s=next(v["summary"] for k,v in c["files"].items() if k.replace(chr(92),"/").endswith("preflight/policy.py")); assert s["missing_branches"]==0, s; print("policy branch gate PASS")'
```

Mỗi lần chạy thay `qa-20260923-01` bằng ID mới ở cả ba lệnh. Nếu shell Windows
cũ không truyền nguyên dấu nháy trong `python -c`, lưu nội dung Python bên trong
vào file tạm cục bộ rồi chạy bằng Python; không thay các path bằng đường dẫn máy khác.
Lỗi pytest phải giữ là FAIL dù coverage đạt. JUnit XML có skipped nghĩa là
phải ghi lý do; không chứng nhận JDK matrix khi thiếu một major.

Để chẩn đoán riêng path/timeout, chạy sau gate tổng (không ghi đè coverage):

```text
python -m pytest tests/unit/test_runner_helpers.py -k 'path or wrapper or timeout' -ra
```

### 19.4. Smoke CLI theo config từng máy

Từ `preflight_tool` với venv đã kích hoạt; giả sử dataset ở `../classes2test`.
Thay input path nếu khác và luôn dùng output mới.

```text
class2test-preflight --config ../Java-version/config.local.toml --input-root ../classes2test --limit 20 --index-only --output-dir runs/index-smoke-01
```

Pass nếu `provenance.json` ghi đúng raw/candidate count, candidate manifest parse
được, mọi hash khớp input, rejection có lý do; không có clone/build/workspace.
Không đòi đủ 20 candidate vì `--limit` đếm JSON trước dedup và trước rejection.

Chỉ trong môi trường build đã chuẩn bị, chạy discovery nhỏ:

```text
class2test-preflight --config ../Java-version/config.local.toml --input-root ../classes2test --limit 20 --max-classes 5 --workers 2 --output-dir runs/discovery-smoke-01
```

Không có revision map thì strict manifest phải rỗng. Exit code 0 của CLI chỉ
cho biết run kết thúc; kiểm từng status/log, không coi mọi repo đã build pass.
Strict E2E dùng cùng CLI thêm `--revision-map <map-fixture-da-audit.jsonl>`;
không tự gắn upstream provenance cho SHA suy từ Git history. Muốn đánh giá
probe không dùng `--fast`.

### 19.5. Mutation gate (cần hoàn thiện cấu hình)

Dependency là `mutmut>=3,<4`; lệnh `--paths-to-mutate` trong plan cũ chưa được
xác minh tương thích và không phải gate đã chạy. Trước khi chạy cần cấu hình
phạm vi `preflight/revision.py`, `preflight/policy.py` theo version mutmut thực
cài, kiểm `mutmut run --help`, rồi lưu command/version cùng report. Thực hiện
trên bản sao làm việc cô lập, ưu tiên POSIX/WSL, không sửa code production để
kill mutation. Chưa có cấu hình/report hợp lệ thì ghi NOT_RUN.

## 20. Quy trình chi tiết cho ca chưa tự động hóa

Các fixture dưới cần được tạo trong test temp directory, Git local có commit
SHA cố định trong từng lần chạy và expected được định nghĩa trước khi run.
Không cần repository công cộng để test logic này.

### 20.1. IT-004..007 — Build và probe thật

1. Tạo Maven single-module, Maven reactor `api` + `service`, Gradle subproject;
   mỗi fixture pin build tool/dependency tương thích từng JDK cần test.
2. Đặt CUT trong production source; test framework lần lượt JUnit4, JUnit5,
   TestNG với dependency thực. JUnit5 dùng bản còn hỗ trợ JDK fixture.
3. Chuẩn bị biến thể main syntax error, test syntax error, dependency không
   resolve, API private, checked exception và probe static/instance/constructor.
4. Chạy preflight với config đã verify. Assert command/cwd/module/JDK ở từng
   attempt, main trước test, rồi probe; assert trạng thái và lý do dự kiến.
5. Đặt sentinel trong body test/probe: file này không được tạo vì chỉ compile.
   Kiểm không còn probe sau pass/fail, source Git và dataset hash không đổi.
6. Chạy lại cùng SHA/config/cache state; so output sau khi bỏ duration/log path
   và timestamp. Ghi rõ cold/warm dependency cache để phân biệt lỗi network.

Expected: main lỗi → MAIN_BUILD_FAILED; main pass/test lỗi → TEST_COMPILE_FAILED;
chỉ probe lỗi → PROBE_FAILED; tất cả pass + pinned fixture → ELIGIBLE và strict.
Fixture tổng hợp chỉ chứng minh strict logic, không phải bằng chứng upstream
của dataset experiment.

### 20.2. BT-007..010 — CUT và dependency

1. Cùng một concrete service lần lượt phụ thuộc interface, enum, DAO inject,
   HTTP client; giữ source set/API/build/probe hợp lệ để cô lập policy.
2. So sánh với việc chọn chính interface/enum làm target: dependency không được
   kéo theo loại service; target interface/enum phải bị loại đúng reason.
3. DTO có getter/setter nhận LOW_LOGIC; class chỉ constructor mới nhận thêm
   CONSTRUCTOR_ONLY. DAO inject có DB_DEPENDENT/REQUIRES_MOCK là tag quan sát.
4. DAO DriverManager chưa có evidence infrastructure → NEEDS_REVIEW; không
   đưa strict. Ca INTEGRATION_ONLY theo rule ghi BLOCKED_IMPLEMENTATION đến khi
   cơ chế GAP-02 được triển khai và schema rule được chốt.

### 20.3. E2E-003/005 — Portfolio và strict manifest

1. Tạo các class tại mục 11, gán expected_status/reasons/tags/probe/eligibility
   theo từng fixture; thêm content-matched và unverified dù build được.
2. Run CLI subprocess thật, không monkeypatch run_all/_compile, output mới.
3. JSONL/CSV cùng số record, task ID không trùng; strict manifest đúng tập
   STRICT + UPSTREAM_PINNED + ELIGIBLE. Loại tất cả PRECHECKED/NEEDS_REVIEW.
4. Validate schema kết quả và strict schema; kiểm negative record cố ý sai
   bị reject. Đối soát api_count=constructor_count+method_count và log attempts.
5. Run lại workers=1/2/4 với >=2 CUT khác nhau chung mirror; so canonical output
   và tính toàn vẹn worktree, không chỉ so count JSON dedup.

### 20.4. NF-SEC-001 — Raw input đến workspace

Tạo JSON focal/test path là `/etc/...`, `//server/share/...`, `C:/...`,
`C:relative.java`, traversal slash thuận/ngược và symlink escape. Chạy từ ingest
đến runner để phát hiện normalization làm mất prefix nguy hiểm. Expected reject
trước khi đọc/ghi source ngoài workspace; yêu cầu reject trước clone/worktree
là mục tiêu hardening bổ sung, chưa phải hành vi hiện có. Không dùng file bí
mật thật; dùng sentinel vô hại ngoài temp workspace và kiểm hash trước/sau.

## 21. Bàn giao và quyết định cho phép chạy

Mỗi ca có: ID kế hoạch, pytest node ID hoặc bước manual, commit/spec hash,
OS/CPU/Python/JDK/build tool, fixture SHA/input hash/config hash, command, expected,
actual, PASS/FAIL/SKIP/NOT_RUN/BLOCKED_IMPLEMENTATION, duration và artifact path.

- **Được chạy index khảo sát:** dependency Python + input/config hợp lệ;
  không cần JDK cho index-only.
- **Được smoke discovery:** JDK/build tool đúng, môi trường build cô lập, run
  nhỏ có log; kết quả không dùng thay strict experiment.
- **Được chạy strict experiment:** mọi P0 theo đặc tả được thực thi và pass,
  GAP ảnh hưởng strict đã đóng, build/probe thật trên OS mục tiêu có bằng chứng,
  revision map upstream đã audit và tất cả gate mục 14 đạt. Suite 175 pass
  hiện tại chưa tự thỏa các điều kiện này.

Người triển khai bổ sung test; người chạy test giữ artifact; người phụ trách
đặc tả/experiment duyệt oracle và sai khác trước khi chốt. Trong lần cập nhật
này chỉ sửa kế hoạch, không đổi đặc tả hoặc implementation để ép kết quả pass.
