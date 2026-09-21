# Preflight ARROW cho Class2Test

**Trạng thái:** Đặc tả chốt để triển khai runner  
**Phạm vi:** Java Class2Test và unit-test generation của ARROW

## 1. Mục đích và ranh giới

Preflight là pha chỉ đọc, chạy trước khi ARROW gọi LLM. Nó xác định entry nào
có revision được khóa, build được ở đúng module và có Java class-under-test
(CUT) đủ điều kiện kỹ thuật để ARROW sinh unit test.

Preflight không sửa source repository, dependency, build file hoặc manifest
Class2Test gốc. Preflight cũng không kết luận generated test tốt, đúng ngữ
nghĩa hoặc không tạo regression. Các kết luận đó thuộc pha validation sau khi
sinh test.

```text
Locked input -> Preflight -> Strict eligible manifest -> Generate -> Validate
```

## 2. Thuật ngữ và các trục trạng thái

Không dùng một cột trạng thái cho các khái niệm khác nhau. Mỗi kết quả phải có
các trường độc lập sau:

| Trường | Giá trị | Ý nghĩa |
| --- | --- | --- |
| `run_mode` | `STRICT`, `DISCOVERY_ONLY` | Chế độ chạy của entry. |
| `revision_verification_status` | `UPSTREAM_PINNED`, `CONTENT_MATCHED`, `UNVERIFIED` | Mức bằng chứng cho revision. |
| `preflight_status` | Xem mục 8 | Kết quả kiểm tra kỹ thuật. |
| `tags` | Mảng nhãn | Đặc điểm quan sát được, không tự là lỗi. |
| `technical_eligible` | `true` hoặc `false` | Entry có qua build, AST và probe hay không. |
| `strict_eligible` | `true` hoặc `false` | Entry có được đưa vào strict experiment hay không. |
| `reason_codes` | Mảng mã | Mọi lý do loại, cảnh báo hoặc lỗi. |

`ELIGIBLE` là `preflight_status` kỹ thuật. Chỉ `strict_eligible=true` mới cho
phép entry xuất hiện trong manifest dùng cho số liệu experiment.

## 3. Locked input manifest và provenance revision

Runner chỉ nhận **locked input manifest**; không nhận trực tiếp một JSON
Class2Test chưa được chuẩn hóa. Mỗi entry cần tối thiểu:

```json
{
  "task_id": "repo-id_sample-id",
  "source_json_path": "classes2test/dataset/123/123_0.json",
  "source_json_sha256": "sha256-cua-noi-dung-file-json",
  "repo_url": "https://github.com/owner/repository.git",
  "checkout_sha": "full-40-character-git-sha",
  "revision_provenance": "upstream_metadata | dataset_record | git_history | manual",
  "revision_verification_status": "UPSTREAM_PINNED | CONTENT_MATCHED | UNVERIFIED",
  "class_path": "src/main/java/com/example/OrderService.java",
  "class_fqn": "com.example.OrderService",
  "test_class_path": "src/test/java/com/example/OrderServiceTest.java",
  "module_path": "."
}
```

`source_json_sha256` là hash của nội dung file JSON nguồn, không phải hash của
đường dẫn. `checkout_sha` luôn là full SHA đã được `git rev-parse` xác nhận.

### 3.1. Mức xác minh revision

- `UPSTREAM_PINNED`: SHA có trong metadata upstream hoặc bản ghi dataset có
  thể kiểm tra; đây là mức duy nhất được dùng cho strict cohort.
- `CONTENT_MATCHED`: source/test tại `checkout_sha` khớp các trường trích xuất
  từ JSON sau quy tắc chuẩn hóa được lưu lại. Đây là bằng chứng đối chiếu nội
  dung, không được gọi là revision gốc khi không có chứng cứ upstream.
- `UNVERIFIED`: không có đủ bằng chứng cho hai mức trên.

Mỗi entry `CONTENT_MATCHED` phải ghi `content_match` gồm các trường đã so
sánh, cách bỏ comment/chuẩn hóa whitespace và kết quả khớp. Mỗi entry
`UNVERIFIED` phải ghi phương pháp đã chọn `checkout_sha`.

### 3.2. Chế độ `DISCOVERY_ONLY`

Entry thiếu SHA hoặc chưa xác minh revision có thể chạy chẩn đoán với
`run_mode=DISCOVERY_ONLY`. Runner vẫn ghi rõ `checkout_sha` đã dùng, nhưng
luôn đặt `strict_eligible=false`. Kết quả chỉ dùng để khảo sát khả năng clone,
build và cấu trúc repository; không được trộn vào strict cohort, bảng kết quả
paper hoặc kết quả tái lập.

## 4. Quy trình preflight

1. Kiểm tra schema, hash JSON nguồn và revision provenance của locked manifest.
2. Clone/cache repository theo `repo_url`; fetch và checkout detached HEAD tại
   `checkout_sha`.
3. Phát hiện source set, module chứa CUT, build tool, wrapper, JDK và test
   framework.
4. Chạy main compile, sau đó test compile, tại đúng module và cùng JDK.
5. Parse CUT bằng Java AST; xác minh file path, package, declaration và FQN.
6. Tính API truy cập được và gắn tag dependency quan sát được.
7. Sinh compile probe trong workspace tạm, đúng package/module của generated
   test, rồi chạy test compile.
8. Suy ra các trường trạng thái, lưu log bất biến và tạo manifest strict.

Repository cache và workspace probe phải tách nhau. Probe hay build không được
để lại source/generated test trong working tree của repository đã checkout.

## 5. Build theo module

Runner phải lưu `working_directory`, `module_path`, command, JDK thực chạy,
wrapper/build-tool version, exit code, thời gian và đường dẫn log cho mọi
attempt.

### Maven

Từ Maven reactor root, module đơn dùng `.`; module con dùng đường dẫn Maven
project tương ứng:

```bash
# Main compile
mvn -q -pl <module> -am -DskipTests compile

# Test compile, không chạy test
mvn -q -pl <module> -am -DskipTests test-compile
```

Với project không phải multi-module, runner có thể chạy hai lệnh trên mà không
có `-pl/-am` từ thư mục chứa `pom.xml`.

### Gradle

Runner ưu tiên wrapper đã checkout và Gradle project path đã phát hiện:

```bash
# Main compile
./gradlew --no-daemon :<module>:classes

# Test compile, không chạy test
./gradlew --no-daemon :<module>:testClasses
```

Với root project, dùng `classes` và `testClasses` không có prefix module.

### Phân loại build failure

Main compile phải chạy trước test compile. Không suy `MAIN_BUILD_FAILED` từ
một lệnh test compile duy nhất.

| Điều kiện | `preflight_status` hoặc `reason_code` |
| --- | --- |
| Không xác định được Maven/Gradle hoặc wrapper phù hợp | `BUILD_TOOL_UNSUPPORTED` |
| Main compile fail | `MAIN_BUILD_FAILED` |
| Main compile pass, test compile fail | `TEST_COMPILE_FAILED` |
| Vượt timeout đã cấu hình | `BUILD_TIMEOUT` |
| Không tải/resolve được dependency | `DEPENDENCY_UNAVAILABLE` |
| Sai JDK hoặc không tìm được JDK yêu cầu | `JDK_UNSUPPORTED` |

Không sửa dependency, source hay build configuration để ép build pass.

## 6. CUT được accept, loại và gắn nhãn

### 6.1. Điều kiện CUT kỹ thuật

CUT phải là file `.java` thuộc production source set đã phát hiện, có
`class_fqn` khớp package và top-level declaration tại `checkout_sha`. CUT là
top-level concrete `class`, không phải generated/test source, và có ít nhất
một API thực sự truy cập được từ package dự kiến đặt generated test.

Generated test mặc định được đặt trong cùng package với CUT. Vì vậy,
package-private class/member có thể được test nếu module test source set cho
phép cùng package đó.

### 6.2. Class được accept

| Nhóm | Kết quả | Tag khi cần |
| --- | --- | --- |
| Concrete service, domain hoặc utility class có API gọi được | Accept | Không bắt buộc |
| `final` class | Accept | Không bắt buộc |
| Static utility class có public/protected/package static API | Accept | `STATIC_UTILITY` |
| Package-private class gọi được trong cùng package | Accept | `PACKAGE_PRIVATE` |
| DTO/POJO | Accept | `LOW_LOGIC`; thêm `CONSTRUCTOR_ONLY` khi chỉ có constructor truy cập được |
| Concrete DAO/repository với dependency inject/mock được | Accept | `DB_DEPENDENT`, `REQUIRES_MOCK` |
| Class dùng file system, HTTP hoặc external client có collaborator mock được | Accept | `EXTERNAL_DEPENDENT`, `REQUIRES_MOCK` |

DTO/POJO không bị loại vì ít logic: preflight đo tính khả thi kỹ thuật, còn
quality của generated test được đo sau bằng coverage, mutation và test smell.

### 6.3. Class/file bị loại khỏi strict cohort

| Nhóm | `reason_code` |
| --- | --- |
| `interface` hoặc `@interface` | `EXCLUDE_UNSUPPORTED_DECLARATION` |
| `enum` hoặc `record` | `EXCLUDE_STRICT_COHORT` |
| Abstract class | `EXCLUDE_ABSTRACT_CLASS` |
| Anonymous, local hoặc inner class | `EXCLUDE_UNSUPPORTED_DECLARATION` |
| `.sql`, XML, YAML hoặc file không phải `.java` | `EXCLUDE_NOT_JAVA_SOURCE` |
| File không ở production source set | `EXCLUDE_NON_PRODUCTION_SOURCE` |
| Existing test source | `EXCLUDE_TEST_SOURCE` |
| Generated source hoặc class có `@Generated` | `EXCLUDE_GENERATED_SOURCE` |
| Không có API truy cập được | `EXCLUDE_NO_TESTABLE_API` |

Tên `*Test`, `*Tests` hoặc `*IT` chỉ là tín hiệu `SUSPECT_TEST_NAME`, không là
bằng chứng đủ để loại class. Source set và Java AST là bằng chứng chính.

### 6.4. DAO, SQL và dependency ngoài

Không phân loại chỉ theo tên file như `UserDao.java` hoặc `SqlHelper.java`.

- File `.sql` luôn bị loại vì không phải Java CUT.
- `UserDao` là interface thì bị loại với tư cách CUT, nhưng có thể là dependency
  được mock của một CUT khác.
- `JdbcUserDao` là concrete class có `DataSource`/`JdbcTemplate`/`EntityManager`
  inject được thì được accept với tag database.
- Import JDBC/JPA, SQL string hoặc `DriverManager.getConnection(...)` chỉ tạo
  tag quan sát được như `DB_DEPENDENT` hoặc `DIRECT_CONNECTION`; chúng không
  tự chứng minh class là `INTEGRATION_ONLY`.
- `INTEGRATION_ONLY` chỉ được gán khi có quy tắc tĩnh được ghi trước hoặc bằng
  chứng chạy cho thấy target API cần infrastructure thật. Trường hợp còn
  mơ hồ là `NEEDS_REVIEW`, không vào strict manifest.

## 7. API count và compile probe

### 7.1. `api_count`

`api_count` là tổng của `constructor_count` và `method_count`.

- `constructor_count`: constructor của CUT mà generated test cùng package có
  thể gọi. Nếu CUT không khai báo constructor, default constructor được tính
  theo visibility thật của class.
- `method_count`: instance/static method có thể gọi từ generated test; chỉ tính
  inherited method khi method đó thực sự gọi được và không tính method phổ quát
  chỉ kế thừa từ `java.lang.Object` làm bằng chứng duy nhất.
- Method/constructor `private` không được tính. Static method `private` cũng
  không được tính.

Một class chỉ có constructor truy cập được vẫn có thể accept kèm
`CONSTRUCTOR_ONLY`; empty class không có API truy cập được bị loại.

### 7.2. Probe bắt buộc cho strict cohort

Probe là bắt buộc với `run_mode=STRICT`. Runner tạo probe test tạm bằng test
framework đã phát hiện, trong package/module dự kiến đặt generated test, rồi
chạy **test compile**, không chạy test.

Probe phải compile được một lời gọi tới ít nhất một API đã tính trong
`api_count` và lưu `probed_api`:

- Static API: gọi static method với argument chỉ để compile.
- Instance API: khai báo hoặc tạo instance theo API constructor truy cập được,
  rồi compile lời gọi instance method. Nếu không thể khởi tạo an toàn, probe có
  thể dùng reference `null` vì probe không được thực thi.
- Constructor-only CUT: compile lời gọi constructor với argument chỉ để compile.

Probe chứng minh khả năng compile và khả năng truy cập API, không chứng minh
behavior đúng, dependency mock được hoặc generated test sẽ pass.

Nếu probe không chạy trong chế độ nhanh, đặt `preflight_status=PRECHECKED`,
`probe_status=NOT_RUN` và `strict_eligible=false`.

## 8. Trạng thái preflight và suy ra eligibility

`preflight_status` nhận một giá trị chính. Khi có nhiều lỗi, `reason_codes`
giữ toàn bộ mã và status chính theo ưu tiên sau: revision/checkout -> build ->
source/FQN -> declaration/API -> probe -> review.

| `preflight_status` | Ý nghĩa |
| --- | --- |
| `ELIGIBLE` | Build, AST và probe bắt buộc đều pass. |
| `PRECHECKED` | Kiểm tra nhanh pass nhưng probe không chạy. |
| `NEEDS_REVIEW` | Có dependency/side effect chưa thể kết luận theo quy tắc. |
| `CLONE_FAILED` | Không clone được repository. |
| `COMMIT_MISSING` | Không fetch/resolve được `checkout_sha`. |
| `CHECKOUT_FAILED` | Không checkout detached HEAD được. |
| `BUILD_TOOL_UNSUPPORTED` | Không nhận diện build tool/wrapper phù hợp. |
| `JDK_UNSUPPORTED` | JDK yêu cầu không khả dụng hoặc không phù hợp. |
| `MAIN_BUILD_FAILED` | Main compile fail. |
| `TEST_COMPILE_FAILED` | Test compile fail sau khi main compile pass. |
| `BUILD_TIMEOUT` | Build vượt timeout. |
| `DEPENDENCY_UNAVAILABLE` | Dependency không resolve/tải được. |
| `SOURCE_INVALID` | Path, package hoặc FQN không khớp source tại SHA. |
| `PROBE_FAILED` | Probe không compile được. |
| `EXCLUDED` | CUT vi phạm chính sách strict; chi tiết ở `reason_codes`. |
| `INTEGRATION_ONLY` | Có bằng chứng theo quy tắc đã công bố là cần infrastructure thật. |

Quy tắc suy ra:

```text
technical_eligible =
  preflight_status == ELIGIBLE

strict_eligible =
  run_mode == STRICT
  AND revision_verification_status == UPSTREAM_PINNED
  AND technical_eligible == true
```

`run_mode=DISCOVERY_ONLY` luôn có `strict_eligible=false`, kể cả khi build và
probe pass. Entry `NEEDS_REVIEW` không được âm thầm đưa vào strict manifest.

## 9. Dữ liệu đầu ra

Preflight giữ nguyên source manifest và tạo:

```text
manifests/locked_input_manifest.jsonl
reports/preflight_results.jsonl
reports/preflight_results.csv
manifests/strict_eligible_manifest.jsonl
```

JSONL là định dạng chuẩn vì giữ được `tags`, `reason_codes`, `probed_api` và
build attempt có cấu trúc. CSV là bản phẳng để tổng hợp.

Mỗi kết quả cần tối thiểu có:

```text
task_id, source_json_sha256, repo_url, checkout_sha, revision_provenance,
revision_verification_status, run_mode, class_path, class_fqn, module_path,
working_directory, build_tool, build_tool_version, java_version,
constructor_count, method_count, api_count, probed_api, probe_status,
preflight_status, technical_eligible, strict_eligible, tags, reason_codes,
duration_seconds, log_paths
```

## 10. Ví dụ quyết định

| Tình huống | Quyết định |
| --- | --- |
| JSON không có SHA, checkout branch hiện tại để khảo sát | `DISCOVERY_ONLY`, `UNVERIFIED`, `strict_eligible=false` |
| DTO có constructor/getter public, SHA upstream | `ELIGIBLE`, `LOW_LOGIC`, có thể strict eligible |
| Static utility có private constructor và public static API | `ELIGIBLE`, `STATIC_UTILITY`; probe gọi static API |
| DAO inject `DataSource` | `ELIGIBLE`, `DB_DEPENDENT`, `REQUIRES_MOCK` |
| DAO tự mở connection, chưa biết mọi API có cần DB thật không | `NEEDS_REVIEW`, `DIRECT_CONNECTION`, không strict eligible |
| CUT là interface | `EXCLUDED`, `EXCLUDE_UNSUPPORTED_DECLARATION` |

## 11. Validation sau khi sinh test

Preflight kết thúc tại strict eligible manifest. Sau khi ARROW sinh test, pha
validation mới được phép báo `VALID` theo chuỗi sau:

```text
GENERATED
  -> TEST_COMPILES
  -> TEST_DISCOVERED
  -> TARGET_TEST_PASSED
  -> MODULE_TESTS_PASSED (VALID)
```

Validation chạy baseline **trước khi chèn generated test**, với cùng
`checkout_sha`, module, JDK, command và cấu hình. Sau khi chèn test, runner
phải:

1. Xác minh generated test class được test framework discover và có ít nhất một
   test được thực thi.
2. Xác minh target test pass.
3. Chạy module suite và so sánh lỗi trước/sau baseline; không được có lỗi mới.
4. Ghi skipped test, timeout và failure signature.
5. Rerun các kết quả không ổn định theo chính sách đã cấu hình; trạng thái
   pass/fail không nhất quán là `FLAKY` và không được gọi `VALID`.

Baseline fail không tự động chặn preflight. Trong validation, baseline failure
được lưu để chỉ kết luận regression khi generated test tạo failure mới hoặc
làm thay đổi failure signature đã có.

## 12. Điều kiện hoàn tất preflight

Đặc tả này sẵn sàng triển khai khi runner có thể tạo locked manifest, thực thi
đúng schema và sinh lại cùng kết quả cho cùng input, container/JDK, timeout và
checkout SHA. Mọi thay đổi chính sách sau này phải có version, migration note
và không ghi đè kết quả preflight đã phát hành.
