# 04. Gọi model và prompt

## 1. Gọi model qua LiteLLM proxy

- `POST {proxy_url}/v1/chat/completions`, header `Authorization: Bearer <master_key>` (đọc từ `litellm.config.local.yaml`).
- Body: `{"model": "<name>", "messages": [...], "temperature": 0, "max_tokens": <params.max_tokens>, "seed": 42, "reasoning_effort": "none"}`; tham số lấy đúng từ cấu hình thực nghiệm, không thêm tham số khác.
- Timeout mỗi lượt gọi: 300 giây.
- Lỗi tạm thời (HTTP 429 rate limit, quá tải, 5xx, lỗi mạng, timeout): gọi lại tối đa **3 lần**, chờ **20, 60, 120 giây** (AR 4). Lần gọi lỗi không tính là một vòng sửa; số lần gọi lại ghi vào `llm_retries`. Vẫn lỗi → job `INFRA` với `reason=LLM_UNAVAILABLE`; ở bước sửa thì dừng nhánh và chạy lại nhánh ở lượt sau (`05` Mục 2.2). HTTP 400/401/403/404 → `INFRA` ngay (`LLM_CONFIG_ERROR`), không thử lại, và runner **tạm dừng** cả run (thường do khóa hoặc endpoint sai).
- Ghi `usage.json` cho mỗi lượt gọi: `model_id` do API trả về, `prompt_tokens`, `completion_tokens`, `finish_reason`, `latency_seconds`, `attempts`, `cache_hit`.
- `model_id` MUST bắt đầu bằng `expected_model_id_prefix`; sai → `INFRA` `LLM_WRONG_MODEL` và tạm dừng run.
- Giới hạn tốc độ: tối đa `workers.llm` lượt gọi đồng thời và `rpm_limit` lượt/phút trên máy.

### 1.1. Cache

Khóa cache = SHA-256 của JSON chuẩn hóa `{model, params, messages}`. Có cache thì dùng, không gọi API (`cache_hit=true`). Response đầu tiên là bản chính thức; không bao giờ ghi đè cache.

### 1.2. Chi phí

Cấu hình thực nghiệm có `models[].price_usd_per_million = {"input": …, "output": …}` (người điều phối điền theo bảng giá của provider). Chi phí = token × giá; lượt `cache_hit` không tính tiền.

## 2. Định dạng template

- File Markdown trong `prompts/`, phần đầu YAML ghi `id`, `version`, `role` (`system`/`user`). Biến dạng `{{ten_bien}}`; khối điều kiện `{{#if bien}}…{{/if}}`; danh sách `{{#each ds}}…{{/each}}`. Template engine tự viết, chỉ hỗ trợ ba cú pháp này; biến thiếu → lỗi ngay, không thay bằng rỗng.
- Hash của từng file template ghi vào `run_config.lock.json`. Sửa template = đổi `prompt_set.version` và cấu hình thực nghiệm mới.
- `prompt.json` của mỗi job lưu đúng `messages` đã gửi.

## 3. Kiểm tra và chuẩn hóa output (theo code đã chạy)

AR 8 ghi c0 được "dựng lại đúng như lúc sinh (validate, chuẩn hóa, sửa import)". Các bước dưới đây là cách spec thực hiện việc đó, dựa trên bước kiểm tra của pipeline ARROW trước đây (`ARROW-paper/ARROW/src/test_writer.py`); áp dụng **giống nhau** cho mọi quy trình prompting, mọi vòng sửa và cả hai cơ chế.

Đầu vào: response, `expected_package` (package của CUT), `expected_class_name` (`<CUT>_ArrowTest`), `testing_framework` của repository (theo manifest).

1. **Lấy mã:** bỏ markdown fence (khối ```` ```java ```` hoặc ```` ``` ````); không có fence thì lấy cả response.
2. **Từ chối** (output không hợp lệ) khi:
   - mã rỗng;
   - có dấu hiệu là diff (`--- `, `+++ `, `@@`);
   - có nội dung không phải test (chứa `pom.xml`, `build.gradle`, `settings.gradle`, `src/main/java`);
   - không cân bằng `{}`, `()`, `[]` (bỏ qua chuỗi, ký tự, comment) hoặc chuỗi/comment chưa đóng;
   - `finish_reason = length` (response bị cắt).
3. **Chuẩn hóa tên class:** tìm các `public class X`. Nếu đúng một class và `X` là biến thể quen thuộc của tên bắt buộc (`<CUT>Test`, `<CUT>Tests`, `<CUT>GeneratedTest`, `<CUT>LLMGeneratedTest`, `<CUT>_ArrowTest`) thì đổi thành `<CUT>_ArrowTest`. Sau bước này phải có **đúng một** `public class` tên `<CUT>_ArrowTest`, ngược lại từ chối ("sai class").
4. **Sửa import theo test framework của repository:**
   - `junit5`: bỏ import JUnit 4 (`org.junit.Test`, `Before`, `After`, `BeforeClass`, `AfterClass`, `Ignore`, `static org.junit.Assert.*`); đổi annotation `@Before→@BeforeEach`, `@After→@AfterEach`, `@BeforeClass→@BeforeAll`, `@AfterClass→@AfterAll`, `@Ignore→@Disabled`; thêm import `org.junit.jupiter.api.*` cho annotation đang dùng và `import static org.junit.jupiter.api.Assertions.*;` nếu có lời gọi `assertX(` mà chưa import.
   - `junit4`: đổi ngược lại (`org.junit.jupiter.api.Test→org.junit.Test`, …, `Assertions.*→org.junit.Assert.*`), bỏ import Jupiter còn lại, thêm import JUnit 4 cho annotation đang dùng và `import static org.junit.Assert.*;` nếu cần.
   - `testng`: không đổi.
5. **Thêm import JDK phổ biến** khi dùng mà thiếu: `java.util.Arrays`, `java.util.Collections`, `java.util.Comparator`, `java.util.NoSuchElementException`.
6. **Kiểm tra package:** dòng `package …;` phải đúng `expected_package`, ngược lại từ chối ("sai package").
7. Tính code hash (`03` Mục 7) trên mã đã chuẩn hóa.

Ghi `normalization.json` cạnh mã: tên class trước và sau, import đã thêm hoặc bỏ, annotation đã đổi, lý do từ chối nếu có. Báo cáo RQ1 phải nêu bước chuẩn hóa này là một phần của pipeline áp dụng cho mọi quy trình prompting.

**Output không hợp lệ:**
- Ở bước sinh test: ghi ứng viên với `state = COMPILE`, `steps.compile = "FAIL"`, `invalid_output = true`, lý do; không build, **không sinh lại** (Report 2: ứng viên rỗng hoặc lỗi vẫn tính vào mẫu số RQ1). Ứng viên này **không** được đưa vào bước sửa (`05` Mục 2.1).
- Ở bước sửa: tính một vòng, không build, lên mức (Adaptive) hoặc giữ bản mới nhất (Fixed) (`05`).

## 4. Prompt sinh test

Ba quy trình dùng chung system message và cùng phần đầu ra; chỉ khác phần thông tin đưa vào.

| | zero_shot | few_shot | zero_shot_project_aware |
| --- | --- | --- | --- |
| Mã nguồn CUT (cả file) | ✔ | ✔ | ✔ |
| Tên class test bắt buộc và package | ✔ | ✔ | ✔ |
| 2 ví dụ (CUT + test người viết) | | ✔ | |
| 6 thông tin repository | | | ✔ |

`[D3]` zero_shot và few_shot **không** được biết test framework, Java version, dependencies, cấu trúc, test path; mô hình tự suy ra từ mã CUT. Đây là điểm khác biệt mà RQ1 đo. Tên class và package được đưa vào mọi quy trình vì bước kiểm tra (Mục 3) bắt buộc đúng tên và package; package lấy từ chính CUT, không phải thông tin cấu hình repository. Lưu ý: bước sửa import theo framework (Mục 3.4) áp dụng cho mọi quy trình, nên một phần lỗi chọn sai JUnit 4/5 của zero_shot và few_shot được sửa tự động; báo cáo RQ1 phải nêu điều này.

### 4.1. `generation/system.md`

```
You are an experienced Java developer who writes unit tests.
Return exactly one Java source file containing one test class. Do not include explanations.
The test class must compile and its tests must pass against the given class without modifying any production code.
```

### 4.2. `generation/zero_shot.md`

~~~
Write a unit test class for the Java class below.

Test class name: {{test_class_name}} (package {{test_package}})

Class under test ({{cut_path}}):
```java
{{cut_source}}
```
~~~

### 4.3. `generation/few_shot.md`

~~~
Here are examples of a Java class and a unit test class written for it.

{{#each examples}}
Example {{index}} – class under test:
```java
{{cut_source}}
```
Example {{index}} – unit test:
```java
{{test_source}}
```
{{/each}}

Now write a unit test class for the Java class below.

Test class name: {{test_class_name}} (package {{test_package}})

Class under test ({{cut_path}}):
```java
{{cut_source}}
```
~~~

### 4.4. `generation/zero_shot_project_aware.md`

~~~
Write a unit test class for the Java class below. It must build and run inside the repository described here.

Repository configuration:
{{repo_context}}

Test class name: {{test_class_name}}
Write it to: {{test_file_path}} (package {{test_package}})

Class under test ({{cut_path}}):
```java
{{cut_source}}
```
~~~

### 4.5. Khối `{{repo_context}}` (chỉ dùng ở zero_shot_project_aware)

```
- Build tool: Maven 3.9.6 (wrapper: yes), module: trading-common
- Java version: 17 (maven.compiler.release=17)
- Test framework: JUnit 5.10.2 (org.junit.jupiter)
- Other test libraries available: Mockito 5.11.0, AssertJ 3.25.3
- Dependencies used by the class under test: org.springframework.data:spring-data-jpa:3.2.4, …
- Project structure: multi-module Maven project (modules: trading-common, trading-web, …);
  main sources in trading-common/src/main/java, tests in trading-common/src/test/java
- Source path of the class under test: trading-common/src/main/java/fr/…/PairService.java
- Test path for the new test: trading-common/src/test/java/fr/…/PairService_ArrowTest.java
```

Dependencies đưa vào prompt: các thư viện scope test liên quan tới test (framework, mock, assert), và các thư viện mà CUT import (khớp theo package của import với artifact trong danh sách dependencies). Tối đa 40 dòng; còn lại ghi `… and N more`.

## 5. Ví dụ few-shot `[D4]`

- Lệnh `arrow fewshot select` (người điều phối chạy một lần, commit kết quả `prompts/fewshot/examples.json`).
- Pool: `technical_eligible` + `CONTENT_MATCHED` của Classes2Test, bỏ mọi repository của tập 200 class, pilot v1, pilot v2 và 50 CUT dự bị (`--exclude` mặc định gồm 4 manifest này). Pool còn 4.805 bản ghi.
- Điều kiện: CUT 30–150 dòng, test người viết 20–120 dòng, test có ít nhất 2 test method và 2 assertion (java-analyzer), không dùng Mockito (ví dụ đơn giản). **Thêm (13/10/2026, chờ xác nhận):** test chạy độc lập, không cần dịch vụ ngoài: không chứa dấu hiệu Spring/Micronaut/Quarkus context, `@ExtendWith`, `@RunWith`, `@Inject`, Testcontainers, JDBC, SQL, `Database`, URL HTTP, socket, thư mục tạm, biến môi trường, `Thread.sleep` (danh sách đầy đủ trong `FewShotSelector.ExternalMarkers`). Lần chọn đầu không có điều kiện này lấy ví dụ JUnit 5 là test Micronaut cần cơ sở dữ liệu.
- Chọn **2 ví dụ**: một JUnit 4, một JUnit 5. Với mỗi framework: danh sách ứng viên sắp theo `task_id`, xáo bằng `System.Random(42)` (Fisher–Yates), lấy ứng viên đầu tiên đạt mọi điều kiện. Cùng 2 ví dụ dùng cho mọi CUT.
- `examples.json` lưu mã CUT và mã test (đọc bằng `git show` tại đúng commit), kèm `task_id`, `repo_url`, `checkout_sha`, hash, quy tắc và các manifest bị loại.
- **Kết quả (13/10/2026):**

| Framework | task_id | Repository | CUT | Lý do đạt |
| --- | --- | --- | --- | --- |
| JUnit 4 | `166116543_f6b6c10d343d5bb1` | shekhargulati/conduit-on-kubernetes | `DefaultJwtService` (50 dòng) | test 44 dòng, 3 test method, 5 assertion, tạo đối tượng trực tiếp, không cần Spring |
| JUnit 5 | `48880766_962a909932e57aca` | jtablesaw/tablesaw | `ReadOptions` (48 dòng) | test 32 dòng, 3 test method, 3 assertion, chỉ dùng Guava `Lists` |

## 6. Prompt sửa test

Theo AR 3.1 và AR 7. `[D12]` AR quy định **nội dung** từng mức, không quy định câu chữ; câu chữ dưới đây là của spec, được khóa bằng hash như mọi template.

System message dùng chung:

~~~
You are an experienced Java developer fixing a failing unit test class.
Only the test class may change. Do not modify production code, build files, or other tests.
~~~

### 6.1. Mức 1 (cũng là prompt duy nhất của Fixed)

Mức 1 gồm đúng ba thứ (AR 3.1): **thông báo lỗi + mã test + source focal class**. **Không** có khối thông tin repository.

~~~
The unit test class below fails verification.

Verification result: {{state_description}}
{{error_block}}

Current test class ({{test_class_name}}):
```java
{{test_source}}
```

Class under test ({{cut_path}}):
```java
{{cut_source}}
```

Return the complete corrected test class {{test_class_name}} (package {{test_package}}) as one Java file.
~~~

`{{state_description}}`:

| Trạng thái | Mô tả |
| --- | --- |
| COMPILE | `The test class does not compile.` |
| UNKNOWN | `The build failed for a reason other than a compilation error in the test class.` |
| DISCOVERY | `The test class compiles, but the test runner does not find or run any of its tests.` |
| RUNTIME | `Some tests throw an exception when they run.` |
| ASSERTION | `Some tests fail their assertions.` |
| REGRESSION | `The new tests pass, but existing tests in the module now fail.` |

`{{error_block}}` (rút gọn, tối đa 60 dòng):

| Trạng thái | Nội dung |
| --- | --- |
| COMPILE | Các lỗi javac chỉ vào file test: `line N: <message>` kèm `symbol`, `location` |
| UNKNOWN | 40 dòng `ERROR`/`FAILURE` cuối của log build |
| DISCOVERY | `No tests were executed.` |
| RUNTIME | Với mỗi method lỗi (tối đa 5): tên method, exception, thông điệp, tối đa 15 frame (bỏ frame của JDK, JUnit, Maven/Gradle) |
| ASSERTION | Với mỗi method lỗi (tối đa 5): tên method, thông điệp assertion |
| REGRESSION | Tên các test có sẵn bị hỏng mới (tối đa 10) |

Fixed: `test_source` và lỗi là của **bản mới nhất**. Adaptive: luôn là của **checkpoint**.

### 6.2. Mức 2 (Adaptive)

Mức 1, thêm trước dòng `Return …`:

~~~
Additional information:
{{level2_block}}

Previous attempts that did not fix the problem. Do not repeat them:
{{#each attempts}}
- Attempt {{round}}: {{outcome}}
{{/each}}
~~~

`attempts` là danh sách `đã_thử` của `05` (không bị xóa khi đổi checkpoint); mỗi mục ghi `outcome` là `<trạng thái> — <error signature>`, `duplicate code (same as an earlier attempt)` hoặc `invalid output (not a valid test class)`. AR không quy định cách hiển thị lần thử; spec chỉ ghi kết quả và signature, không đưa mã của lần thử (thuộc D12).

`{{level2_block}}` theo trạng thái của checkpoint (AR 3.1):

| Trạng thái | Thông tin bổ sung |
| --- | --- |
| COMPILE | Danh sách constructor và phương thức thật của CUT và các class liên quan (lỗi nhắc tới), dạng chữ ký (`java-analyzer cut_api`); dependencies của module (`02` Mục 7); Java version |
| DISCOVERY | Khung test class mẫu đúng test framework (annotation, import), package và tên file theo test path |
| RUNTIME | Mã của phương thức bị gọi (method của CUT mà method test lỗi gọi); constructor và cách khởi tạo field; gợi ý cấu hình mock (thư viện mock có trong scope test và cách khởi tạo mock theo framework) |
| ASSERTION | Giá trị expected và actual của từng assertion lỗi; mã của phương thức được kiểm tra |
| REGRESSION | Tên và lỗi của các test sẵn có bị fail; gợi ý về trạng thái dùng chung (biến static, file, cấu hình) |
| UNKNOWN | `[D6]` AR không quy định; spec dùng 4 phần: (a) khối `[ERROR] Failed to execute goal …` (Gradle: `Execution failed for task …`) kèm chuỗi `Caused by:` đã bỏ trùng, tối đa 60 dòng; (b) cấu hình bước test — Maven: phiên bản và cấu hình Surefire (includes, excludes, forkCount, argLine), compiler plugin (release, annotation processor); Gradle: cấu hình task `test` (useJUnitPlatform/useJUnit, include/exclude); (c) các test engine có trên classpath test và Java version; (d) mỗi dấu hiệu nhận ra trong log một dòng, chỉ nêu sự kiện (ví dụ `forked VM terminated`, `OutOfMemoryError`, `annotation processing failed`) |

### 6.3. Mức 3 (Adaptive; chỉ COMPILE, RUNTIME, ASSERTION khi xác định được method lỗi)

Mức 1, thay dòng `Return …` bằng:

~~~
The failing test methods are: {{#each failing_methods}}`{{name}}` {{/each}}.
Rewrite only these methods so that they compile and pass. Keep every other method unchanged.
You may add imports, fields, or helper methods if needed. If a method cannot be fixed, leave it out.
Return the complete test class {{test_class_name}} (package {{test_package}}) as one Java file.
~~~

LLM trả về **cả class**; ARROW chỉ lấy các method lỗi để ghép vào checkpoint (`05` Mục 5.3). Method lỗi: COMPILE → method chứa dòng lỗi biên dịch (`compile_errors[].method`); RUNTIME, ASSERTION → các method fail theo test report. Không xác định được method nào → dùng prompt mức 2 (ghi `requested_level=3, level=2`).

## 7. Kiểm thử

- Template: biến thiếu → lỗi; `#if`, `#each` đúng; hash ổn định.
- Kiểm tra và chuẩn hóa: có fence/không fence, diff, ngoặc trong chuỗi, `finish_reason=length`, đổi tên class biến thể, sai tên class, sai package, sửa import JUnit 4↔5, thêm import JDK.
- Client: `FakeLlmHandler` trả 429 rồi 200 (thử lại đúng), 401 (tạm dừng run), sai model id; cache hit không gọi HTTP.
