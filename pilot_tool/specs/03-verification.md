# 03. Xác minh 5 bước

Một bộ xác minh duy nhất (`FiveStepVerifier`) dùng cho mọi ứng viên: sinh ban đầu, mỗi vòng sửa, test của EvoSuite.

## 1. Ghi test vào repository `[D1]`

- Tên class: `<TênCUT>_ArrowTest`, cùng package với CUT. File: `<thư mục test của module>/<đường dẫn package>/<TênCUT>_ArrowTest.java`. Thư mục test của module: `src/test/java` cạnh `src/main/java` chứa CUT.
- Không bao giờ ghi đè file đã tồn tại trong repository; nếu tên trùng (hiếm) thì thêm hậu tố số `_ArrowTest2` và ghi lại.
- Chỉ ghi đúng một file; không đụng file nào khác của repository.
- Ghi bằng UTF-8, LF.

Mã do model trả về đi qua bước **kiểm tra và chuẩn hóa** trước khi ghi (giống nhau cho mọi prompt và mọi vòng sửa; chi tiết ở `04` Mục 3, theo code đã chạy: validate, chuẩn hóa tên class, sửa import theo test framework của repository, bắt buộc đúng package). Không qua kiểm tra → output không hợp lệ, không ghi file, không build.

## 2. Các bước và trạng thái

```
compile ─► discovery ─► execution ─► target pass ─► module regression ─► VALID
```

| Bước | Lệnh (`02` Mục 4.4) | Đạt khi | Không đạt → trạng thái |
| --- | --- | --- | --- |
| 1. Compile | `test_compile` | Mã thoát 0 | `COMPILE` nếu log có lỗi javac/kotlinc chỉ vào file test sinh ra; `UNKNOWN` nếu lỗi không chỉ vào file đó (plugin, dependency, lỗi không rõ) |
| 2. Discovery | `target_test` | Báo cáo có ít nhất một `<testcase>` thuộc class `<T>` | `DISCOVERY` |
| 3. Execution | (cùng lần chạy) | Ít nhất một test method của `<T>` thật sự chạy (không phải toàn `SKIP`) | `DISCOVERY` (mọi method bị skip hoặc không có `@Test` hợp lệ) |
| 4. Target pass | (cùng lần chạy) | Mọi test method của `<T>` là `PASS` | `RUNTIME` hoặc `ASSERTION` (Mục 3) |
| 5. Module regression | `module_test` | Không có test nào của module (ngoài `<T>`) fail mới so với baseline | `REGRESSION` |

Thứ tự trạng thái (dùng so sánh checkpoint): `COMPILE < UNKNOWN < DISCOVERY < RUNTIME < ASSERTION < REGRESSION < VALID`. `INFRA` nằm ngoài chuỗi.

**Phân loại lỗi biên dịch chỉ dựa trên dấu hiệu thật của javac/Maven/Gradle** (AR 2.1): dòng lỗi javac dạng `<file>.java:<dòng>: error:` hoặc `[ERROR] <file>.java:[<dòng>,<cột>]`, `COMPILATION ERROR`, `Compilation failure`, `Execution failed for task ':…:compileTestJava'`. Không dùng chuỗi con `error:` đơn thuần: dòng `java.lang.AssertionError:` trong log chạy test **không** phải lỗi biên dịch.

Quy tắc bổ sung:

- **Build timeout và lỗi công cụ là lỗi hạ tầng, không thuộc chuỗi** (AR 2.1): bất kỳ lệnh nào (bước 1, 2–4, 5) quá `step_timeout_seconds`, hoặc công cụ lỗi (tiến trình không khởi động được, JVM crash của Maven/Gradle), hoặc log cho thấy lỗi tải dependency (`Could not resolve dependencies`, `Could not transfer artifact`, `Could not resolve all files`) trong khi baseline đã build được → build lại **1 lần**; vẫn lỗi thì `INFRA` (`reason=BUILD_TIMEOUT` / `TOOL_ERROR` / `DEPENDENCY_DOWNLOAD`). `[D10]` AR không nói rõ timeout khi **chạy test** (bước 2–4) có tính là build timeout không; spec coi là có (lỗi hạ tầng).
- Test method bị timeout do annotation của chính test (`@Timeout`, `@Test(timeout=…)`) không phải lỗi hạ tầng: JUnit báo method đó lỗi, phân loại theo Mục 3.
- Bước 5 chỉ chạy khi bước 4 đạt.
- Báo cáo bước 2 không có file XML (`report_source = none`): đọc log; Maven `Tests run: 0` hoặc không có dòng `Running <T>` → `DISCOVERY`.

## 3. Phân biệt RUNTIME và ASSERTION ở bước 4

Xét các method `FAIL`/`ERROR` của `<T>`:

- Một method là **assertion failure** khi `type` (hoặc dòng đầu stack) thuộc: `java.lang.AssertionError`, `org.opentest4j.AssertionFailedError`, `org.opentest4j.MultipleFailuresError`, `junit.framework.AssertionFailedError`, `junit.framework.ComparisonFailure`, `org.junit.ComparisonFailure`, `org.assertj.core.error.*`, `org.mockito.exceptions.verification.*`, và lỗi của `assertThrows` (`Expected … to be thrown, but nothing was thrown`), `@Test(expected=…)` không ném (`Expected exception: …`). Test bị timeout (`TestTimedOutException`, `TimeoutException` của JUnit) **không** phải assertion failure mà là RUNTIME.
- Mọi method lỗi đều là assertion failure → `ASSERTION`. Có ít nhất một method lỗi khác (exception) → `RUNTIME`.

## 4. Regression

- Tập so sánh = test của module trừ `<T>`, trừ `baseline.failing_in_all_runs`, trừ `baseline.flaky`.
- Regression khi một test trong tập so sánh là `PASS` ở baseline mà `FAIL`/`ERROR` lần này, hoặc biến mất khỏi báo cáo trong khi baseline có (ví dụ crash fork).
- Ghi `new_failures: [{id, type, message}]`.
- **Lần chạy toàn module phải chạy cả test sinh ra** (AR 2.1). Tool MUST kiểm tra báo cáo của `<T>` có trong lần chạy `module_test`. Tên `<CUT>_ArrowTest` khớp mẫu mặc định của Surefire (`**/*Test.java`), nhưng module có thể tự cấu hình `<includes>`/`<excludes>`. Nếu `<T>` không có trong báo cáo: chạy lại `module_test` với `-Dsurefire.includesFile=<file>` (file liệt kê các mẫu mặc định `**/Test*.java`, `**/*Test.java`, `**/*Tests.java`, `**/*TestCase.java` và đường dẫn của `<T>`), không sửa `pom.xml`; Gradle dùng init script thêm `include` cho task `test`. Ghi `module_run_includes_target`.

## 5. `verify.json`

```json
{
  "state": "ASSERTION", "infra_reason": null,
  "steps": {"compile": "PASS", "discovery": "PASS", "execution": "PASS", "target_pass": "FAIL", "regression": "NOT_RUN"},
  "test_methods": {"total": 5, "passed": 3, "failed": 2, "skipped": 0},
  "failures": [{"method": "testTotal", "kind": "assertion", "type": "org.opentest4j.AssertionFailedError",
                "message": "expected: <100> but was: <90>", "trace_head": ["…"]}],
  "compile_errors": [{"line": 42, "column": 15, "message": "cannot find symbol", "symbol": "method getTotal(long)",
                      "location": "class PairService", "method": "testTotal"}],
  "new_failures": [], "module_run_includes_target": null,
  "group": "ASSERTION", "error_signature": "ASSERTION | AssertionFailedError | testTotal",
  "assertion_count": 7, "code_hash": "sha256:…",
  "build_seconds": {"test_compile": 12.1, "target_test": 9.4, "module_test": null},
  "logs": ["logs/test_compile_3.log", "logs/target_test_3.log"]
}
```

`group`: `BUILD` cho `COMPILE`/`UNKNOWN`/`DISCOVERY`; `RUNTIME` cho `RUNTIME`/`REGRESSION`; `ASSERTION` cho `ASSERTION`; `null` cho `VALID`/`INFRA`.

`compile_errors` chỉ lấy lỗi chỉ vào file test sinh ra (đường dẫn kết thúc bằng `<Tsimple>.java`); `method` là method chứa dòng lỗi (do `java-analyzer` xác định, `null` nếu lỗi ngoài method, ví dụ import).

## 6. Error signature

Theo AR 2.2. Chuỗi `<TRẠNG THÁI> | <loại lỗi> | <đối tượng>` (có dấu cách hai bên `|`), lấy từ **lỗi đầu tiên**, ưu tiên lỗi nằm trong file test sinh ra:

| Trạng thái | Signature | Ví dụ |
| --- | --- | --- |
| COMPILE | `COMPILE \| loại lỗi javac \| loại symbol` | `COMPILE \| cannot find symbol \| method` |
| RUNTIME | `RUNTIME \| tên exception \| Class.method` (frame đầu tiên thuộc code dự án, không phải thư viện hay test) | `RUNTIME \| NullPointerException \| OrderService.totalPrice` |
| ASSERTION | `ASSERTION \| loại assertion error \| test method bị fail` | `ASSERTION \| AssertionError \| testGetOneNotFound` |
| REGRESSION | `REGRESSION \| existing tests fail \| các test class sẵn có bị fail` | `REGRESSION \| existing tests fail \| OrderServiceTest` |
| DISCOVERY | `DISCOVERY \| no tests executed \| -` | |
| UNKNOWN | `[D11]` AR không quy định; spec dùng `UNKNOWN \| <plugin id trong "Failed to execute goal", hoặc dòng ERROR đầu tiên đã chuẩn hóa> \| -` | `UNKNOWN \| maven-surefire-plugin \| -` |

- **Loại lỗi javac** được quy về danh sách cố định: `cannot find symbol`, `incompatible types`, `unreported exception`, `method cannot be applied`, `constructor cannot be applied`, `package does not exist`, `has private access`, `is not public`, `cannot be referenced from a static context`, `already defined`, `missing return statement`, `unreachable statement`, `class is abstract`, `does not override`, `cannot be accessed`, `other`. **Loại symbol** lấy từ dòng `symbol:` (`method`, `variable`, `class`, `constructor`, `package`), không có thì `-`.
- RUNTIME: "code dự án" là frame có class thuộc `src/main/java` của repository; không có frame nào như vậy thì dùng `-`.
- REGRESSION: tên đơn giản các test class bị hỏng mới, sắp xếp, nối bằng dấu phẩy.
- Tên và giá trị cụ thể không có trong signature (trừ tên method/class ở cột đối tượng như trên). Ví dụ: `OrderServiceTest.java:42: error: cannot find symbol  symbol: method getTotal(long)` và `…:57: … symbol: method calculateTotal(long)` cùng cho `COMPILE | cannot find symbol | method`.

## 7. Code hash

Theo AR 2.3. `sha256` của mã test sau khi: chuẩn hóa xuống dòng về LF, **bỏ comment xác định qua AST** (`java-analyzer strip_comments`, để chuỗi như `"http://…"` không bị cắt nhầm), gộp mọi khoảng trắng liên tiếp thành một dấu cách, bỏ khoảng trắng đầu/cuối. Ghi dạng `sha256:<hex>`. Hash tính trên mã **sau** bước kiểm tra và chuẩn hóa (`04` Mục 3) và, ở mức 3, sau khi ghép method.

## 8. Đếm assertion (`java-analyzer count_assertions`)

Đếm theo AST của file test (mục 10 bảng Adaptive Repair trong CLAUDE.md):

Theo AR 5.2.

- Được tính: lời gọi `assert*` của JUnit 4/5 và TestNG (`Assert.*`, `Assertions.*`, static import), `assertThat(...)` có tham số (AssertJ, Hamcrest), `assertThrows`/`assertDoesNotThrow`, `@Test(expected = …)` (tính 1), `fail(…)` nằm trong khối `try` (tính 1), `verify(…)` của Mockito, **mỗi kiểm tra trong chuỗi `then()`/`expect()` của RestAssured** (ví dụ `.then().statusCode(200).body(is("hi"))` tính 2).
- Không tính (tầm thường): `assertTrue(true)`, `assertFalse(false)`, `assertEquals(x, x)` hoặc `assertSame(x, x)` với hai biểu thức giống hệt nhau, `assertNotNull(new …)`, `assertNull(null)`, **từ nối không tham số** `.then().assertThat()` của RestAssured.
- Kết quả: tổng số và số theo từng test method.

## 9. Kiểm thử

Fixture có test viết sẵn cho từng trạng thái: lỗi compile trong method, lỗi import (ngoài method), không có `@Test`, mọi method `@Disabled`, NPE từ CUT, assertion sai giá trị (log có dòng `java.lang.AssertionError:` — MUST ra ASSERTION, không phải COMPILE), `assertThrows` không ném, test làm hỏng test khác (ghi file dùng chung), module có `<includes>` riêng loại trừ `*_ArrowTest` (MUST vẫn chạy được test sinh ra trong lần chạy module), test VALID. Mỗi fixture MUST cho ra đúng `state`, `error_signature`, `assertion_count`. Thêm test đếm assertion cho RestAssured.
