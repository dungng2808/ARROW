# Preflight ARROW cho Class2Test

## Mục đích

Preflight là giai đoạn kiểm tra chạy trước khi ARROW sinh unit test. Giai đoạn
này xác định các entry Class2Test có thể checkout đúng commit, build được và
có thể dùng làm Java class-under-test (CUT). Preflight không sửa manifest
Class2Test gốc và không gọi LLM.

Entry có trạng thái `ELIGIBLE` phù hợp để bắt đầu sinh test, nhưng chưa phải
là một test hợp lệ. Test được sinh chỉ đạt `VALID` khi compile thành công,
được test framework phát hiện, chạy pass và không tạo regression trong module.

## Dữ liệu đầu vào

Mỗi entry trong manifest cần tối thiểu các trường sau:

```json
{
  "task_id": "unique-task-id",
  "repo_url": "https://github.com/owner/repository.git",
  "commit": "full-commit-sha",
  "class_path": "src/main/java/com/example/OrderService.java",
  "class_fqn": "com.example.OrderService",
  "module": "optional-module-path"
}
```

## Quy trình preflight

1. Clone từng repository vào cache cục bộ và fetch commit được yêu cầu.
2. Checkout commit đó ở chế độ detached HEAD.
3. Phát hiện module, build tool, Java version và test framework.
4. Compile module mục tiêu mà không thực thi test.
5. Phân tích source mục tiêu bằng Java AST parser.
6. Áp dụng chính sách xác định CUT hợp lệ.
7. Có thể compile một probe test tối thiểu, theo đúng test framework, để tham
   chiếu đến class mục tiêu.
8. Ghi kết quả cho mọi entry đầu vào và tạo manifest chỉ chứa các entry hợp lệ.

## Kiểm tra build

Với Maven module, chạy:

```bash
mvn -q -DskipTests test-compile
```

Với Gradle module, chạy:

```bash
./gradlew --no-daemon testClasses
```

Runner phải lưu chính xác command, Java version, build-tool version, exit
code, thời gian chạy và đường dẫn log. Build timeout hoặc không tải được
historical dependency được ghi nhận là preflight thất bại; không được âm thầm
thay đổi repository hay dependency để biến kết quả thành pass.

## Chính sách strict để chọn CUT

Một entry chỉ có trạng thái `ELIGIBLE` khi tất cả điều kiện dưới đây đều đúng:

- Repository clone thành công và checkout đúng full commit SHA được yêu cầu.
- Module chứa class mục tiêu chạy xong `test-compile` hoặc `testClasses`.
- File mục tiêu tồn tại, có đuôi `.java` và thuộc production source set, ví dụ
  `src/main/java`.
- AST xác nhận khai báo mục tiêu là top-level concrete `class`.
- Class không abstract, anonymous, local, generated và không phải test class.
- Class có ít nhất một member có thể test: constructor truy cập được, method
  không phải `private`, hoặc static method.
- Class được resolve từ module test classpath.

Chính sách strict tạo một benchmark đồng nhất gồm các Java class thông thường.
`enum`, `record` và class chỉ có default interface method nên được giữ ở cohort
tùy chọn riêng, không trộn vào strict cohort.

## Các class được accept

Preflight accept một class vào `eligible_manifest.jsonl` khi class đó là Java
production class, build được tại đúng commit và thỏa toàn bộ chính sách strict.
Các nhóm sau được accept:

| Nhóm class | Quyết định | Điều kiện |
| --- | --- | --- |
| Concrete service, utility hoặc domain class | `ACCEPT` | Là top-level concrete class và có API có thể gọi. `final class` vẫn được accept. |
| Static utility class | `ACCEPT` | Có ít nhất một static method không phải `private`. |
| Package-private class | `ACCEPT` | Generated test được đặt trong cùng package để truy cập class. |
| DTO/POJO | `ACCEPT_WITH_TAG` | Có constructor, getter, setter hoặc behavior truy cập được; gắn `LOW_LOGIC` nếu chỉ có dữ liệu đơn giản. |
| Concrete DAO/repository | `ACCEPT_WITH_TAG` | Dependency database được inject hoặc mock được, ví dụ `DataSource`, `JdbcTemplate` hoặc `EntityManager`; gắn `DB_DEPENDENT` và `REQUIRES_MOCK`. |
| Class dùng HTTP, file system hoặc external client | `ACCEPT_WITH_TAG` | Collaborator được inject hoặc mock được; gắn nhãn dependency phù hợp. |

`ACCEPT_WITH_TAG` vẫn là `ELIGIBLE` và được đưa vào manifest. Tag chỉ giúp
ARROW chọn prompt, mock strategy và phân tích kết quả sau này.

## Các class bị bỏ qua

Preflight không đưa các nhóm dưới đây vào strict `eligible_manifest.jsonl`:

| Nhóm class hoặc file | Trạng thái | Lý do |
| --- | --- | --- |
| `interface` và `@interface` | `EXCLUDE_UNSUPPORTED_DECLARATION` | Không có implementation body để unit test. |
| `enum` và `record` | `EXCLUDE_STRICT_COHORT` | Giữ riêng để benchmark strict chỉ gồm ordinary class. |
| Abstract class | `EXCLUDE_ABSTRACT_CLASS` | Không thể khởi tạo trực tiếp; static behavior có thể được đánh giá trong cohort khác. |
| Anonymous, local hoặc inner class | `EXCLUDE_UNSUPPORTED_DECLARATION` | Không phải CUT độc lập, ổn định. |
| File `.sql`, XML, YAML hoặc file không phải `.java` | `EXCLUDE_NOT_JAVA_SOURCE` | Không phải Java class để sinh unit test. |
| Class ở `src/test`, tên `*Test`, `*Tests` hoặc `*IT` | `EXCLUDE_TEST_SOURCE` | Đây là test source, không phải production CUT. |
| Generated source hoặc class có `@Generated` | `EXCLUDE_GENERATED_SOURCE` | Không trộn generated code với handwritten source. |
| Empty class hoặc class chỉ có member `private` | `EXCLUDE_NO_TESTABLE_API` | Generated test không có API hợp lệ để gọi. |
| DAO tự mở database connection hoặc bắt buộc có database thật | `INTEGRATION_ONLY` | Không phải strict unit test nếu không thể mock dependency. |
| Class yêu cầu external service thật và không mock được | `INTEGRATION_ONLY` | Phải chạy integration test thay vì unit test. |

Không quyết định chỉ dựa trên tên file như `UserDao.java`, `SqlHelper.java` hay
`Repository.java`. Runner phải kiểm tra AST, source set và dependency của class.
Ví dụ, `UserDao.java` là interface thì bỏ qua; `JdbcUserDao.java` là concrete
class có `DataSource` được inject thì accept với tag `DB_DEPENDENT`; còn class
tự gọi `DriverManager.getConnection(...)` và cần database thật thì đánh dấu
`INTEGRATION_ONLY`.

## Chính sách loại và gắn nhãn

| Loại mục tiêu | Kết quả preflight | Ghi chú |
| --- | --- | --- |
| `interface` hoặc `@interface` | `EXCLUDE_UNSUPPORTED_DECLARATION` | Không có implementation body để test. Interface có default method có thể tạo cohort riêng. |
| File `.sql` | `EXCLUDE_NOT_JAVA_SOURCE` | SQL script không phải Java CUT. |
| `enum` hoặc `record` | `EXCLUDE_STRICT_COHORT` | Có thể đánh giá sau trong cohort riêng. |
| Abstract class | `EXCLUDE_ABSTRACT_CLASS` | Chỉ đưa vào chính sách riêng khi nhắm đến static behavior. |
| Mục tiêu trong `src/test` hoặc có tên test | `EXCLUDE_TEST_SOURCE` | Test có sẵn không phải CUT. |
| Generated source hoặc class có `@Generated` | `EXCLUDE_GENERATED_SOURCE` | Không trộn generated code với handwritten source. |
| Java class dùng JDBC, JPA hoặc SQL string | `TAG_DB_DEPENDENT` | Không loại chỉ vì dùng SQL; class có thể unit test bằng mock. |
| Java class bắt buộc cần database hoặc external service thật | `INTEGRATION_ONLY` | Loại khỏi strict unit-test cohort nếu dependency không mock được. |

Không xác định SQL usage chỉ dựa vào tên file. Một Java DAO hoặc repository
vẫn là CUT tiềm năng khi các database collaborator của nó có thể mock.

## Các trạng thái kết quả

| Trạng thái | Ý nghĩa |
| --- | --- |
| `ELIGIBLE` | Tất cả kiểm tra trước khi sinh test đều pass. |
| `CLONE_FAILED` | Không thể clone repository. |
| `COMMIT_MISSING` | Không fetch hoặc resolve được commit yêu cầu. |
| `CHECKOUT_FAILED` | Commit tồn tại nhưng không checkout được. |
| `BUILD_TOOL_UNSUPPORTED` | Không nhận diện được Maven hoặc Gradle. |
| `MAIN_BUILD_FAILED` | Production source không compile được. |
| `TEST_COMPILE_FAILED` | Test environment không compile được. |
| `SOURCE_INVALID` | File mục tiêu thiếu, không phải Java hoặc ngoài production source. |
| `EXCLUDE_*` | Mục tiêu vi phạm một quy tắc strict-cohort rõ ràng. |
| `PROBE_FAILED` | Không thể tham chiếu class mục tiêu từ generated-test classpath. |
| `INTEGRATION_ONLY` | Mục tiêu cần infrastructure ngoài strict unit-test scope. |

## Dữ liệu đầu ra

Preflight tạo hai artifact mới và giữ nguyên source manifest:

```text
reports/preflight_results.csv
manifests/eligible_manifest.jsonl
```

Mỗi dòng kết quả phải có các trường:

```text
task_id, repo_url, commit, class_path, class_fqn, module, build_tool,
java_version, checkout_status, build_status, declaration_kind, api_count,
tags, probe_status, eligible, reason, duration_seconds, log_path
```

## Định nghĩa test được sinh hợp lệ

Preflight chỉ là cổng kiểm tra. Sau khi sinh test, ARROW theo dõi quá trình:

```text
ELIGIBLE
  -> GENERATED
  -> TEST_COMPILES
  -> TEST_DISCOVERED
  -> TARGET_TEST_PASSED
  -> MODULE_TESTS_PASSED (VALID)
```

Chỉ `MODULE_TESTS_PASSED` được báo cáo là generated test hợp lệ hoàn toàn.
Trạng thái này yêu cầu target test được sinh phải pass và toàn bộ module test
suite chạy xong mà không xuất hiện regression mới.
