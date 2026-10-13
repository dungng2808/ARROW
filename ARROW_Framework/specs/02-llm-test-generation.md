# 02. Khối 2: LLM-Based Test Generation

**Mục đích:** dựng prompt từ mã nguồn CUT, metadata M và chỉ dẫn; gọi model đã chọn qua một lớp trừu tượng chung; tách mã test từ câu trả lời thành **Candidate Unit Test (T)**.

Khối 2 có **hai chế độ**, dùng chung mọi bước trừ phần chỉ dẫn:

| Chế độ | Gọi từ | Đầu vào riêng | Chỉ dẫn (Instruction Strategy) |
| --- | --- | --- | --- |
| `GENERATE` | Pipeline, sau khối 1 | quy trình prompting (`strategy`) | template sinh test theo quy trình |
| `REPAIR` | Khối 3 (vòng lặp nét đứt) | `RepairRequest` | template sửa test theo cơ chế và mức |

```
Repository-Aware Prompt Construction        Prompting Strategy          Candidate Test Generation
[Source Code Context][Repository Metadata]  zero_shot                   LLM Abstraction Layer
[Instruction Strategy]                ──►   zero_shot_project_aware ──► (chuẩn hóa input, tham số,  ──► Candidate Unit Test (T)
                                            few_shot                     định tuyến model, tách mã)
```

## 1. Input

### 1.1. Chế độ GENERATE

| Input | Nguồn | Ghi chú |
| --- | --- | --- |
| `TaskRecord` | manifest | `class_fqn` |
| `RepositoryMetadata` M | khối 1 `metadata.json` | và `metadata_prompt.txt` |
| Mã nguồn CUT | workspace gốc, `paths.cut_path` | đọc nguyên văn |
| `strategy` | experiment `strategies` | `zero_shot` \| `zero_shot_project_aware` \| `few_shot` |
| `ModelConfig` | experiment `models[i]` | tên, tham số, giá |
| Ví dụ few-shot | `prompts/fewshot/examples.json` | chỉ với `few_shot` |

### 1.2. Chế độ REPAIR: `RepairRequest` (khối 3 tạo)

```json
{
  "chain": { "task_id": "…", "model": "gpt-6-luna", "strategy": "zero_shot", "mechanism": "adaptive" },
  "round": 3,
  "level": 2,
  "state": "COMPILE",
  "base_code": "package …; class FileResolver_ArrowTest { … }",
  "base_round": 1,
  "error_block": "FileResolver_ArrowTest.java:[13,6] cannot find symbol\n  symbol: class TempDir\n…",
  "extra_context": "Constructors and methods of com.x.FileResolver:\n  public FileResolver(Path root)\n…",
  "failed_attempts": [ { "round": 2, "outcome": "COMPILE | cannot find symbol | method" } ],
  "rewrite_methods": []
}
```

Ý nghĩa từng trường và cách khối 3 tính chúng ở spec 03 Mục 6. Khối 2 **không** quyết định mức hay nội dung bổ sung, chỉ dựng prompt từ request.

## 2. Flow 2.1: Repository-Aware Prompt Construction

Prompt gồm 3 thành phần. Mỗi thành phần được dựng bằng code từ dữ liệu, rồi điền vào template trong `prompts/`.

### 2.1. Source Code Context

1. Mã nguồn CUT **nguyên văn** (kể cả comment), đặt trong khối ```` ```java ````, kèm dòng `File: <cut_path>`.
2. CUT dài hơn 48.000 ký tự: bỏ comment (bằng java-helper); vẫn dài hơn thì giữ chữ ký của mọi constructor và method công khai, thân method chỉ giữ cho tới khi đủ giới hạn. Ghi `source_truncation` (`none` / `comments_removed` / `signatures_only`) vào `prompt.json`.
3. Không đưa class khác của repository vào prompt sinh test (cùng một ngữ cảnh mã cho mọi quy trình, để RQ1 chỉ khác ở thông tin repository).

### 2.2. Repository Metadata

Khối M rút gọn, sinh bằng code từ `metadata.json` (không phụ thuộc chế độ):

```
Repository configuration:
- Build tool: Maven 3.9.6 (module jpa2ddl-core, multi-module project)
- Java version: 8 (compile with JDK 8; do not use APIs newer than Java 8)
- Test framework: JUnit 5 (5.9.2); mocking: Mockito 4.8.0; assertions: AssertJ 3.24.2
- Dependencies (direct): org.hibernate:hibernate-core:5.4.2.Final, … (tối đa 25 dòng, ưu tiên thư viện test)
- Project structure: modules jpa2ddl-core, jpa2ddl-maven, jpa2ddl-gradle; CUT package com.devskiller.jpa2ddl
- Source path: jpa2ddl-core/src/main/java
- Test path: write the test to jpa2ddl-core/src/test/java/com/devskiller/jpa2ddl/FileResolver_ArrowTest.java
```

Khi nào đưa vào prompt *(chốt Q4 = C, 14/10/2026)*:
- Chế độ GENERATE: **chỉ** với `zero_shot_project_aware` (đây là điểm khác biệt của RQ1).
- Chế độ REPAIR: khối M **đầy đủ** như trên trong **mọi** prompt sửa, cả Fixed và Adaptive, mọi mức, bất kể candidate được sinh bằng quy trình nào. RQ1 đo trên candidate ban đầu nên không bị ảnh hưởng; RQ2 công bằng vì hai cơ chế nhận cùng khối M.

### 2.3. Instruction Strategy

Template chỉ dẫn, mỗi template một file `.md` có version, tính sha256 khi khởi động (ghi vào `run.lock.json`):

| Template | Dùng khi |
| --- | --- |
| `generation/system.md` | Mọi lượt sinh |
| `generation/zero_shot.md`, `generation/zero_shot_project_aware.md`, `generation/few_shot.md` | Chế độ GENERATE theo quy trình |
| `repair/system.md` | Mọi lượt sửa |
| `repair/fixed.md` | Fixed Repair, mọi vòng (= mức 1 của Adaptive) |
| `repair/level1.md`, `repair/level2.md`, `repair/level3.md` | Adaptive Repair theo mức |

Yêu cầu chung của mọi template sinh: trả về **một** test class Java hoàn chỉnh trong một khối mã, đúng package của CUT, tên class `<CUT>_ArrowTest`, chỉ dùng thư viện đã có trong dự án. Quy trình nào cũng nêu tên class và package (để mọi quy trình cùng điều kiện đặt file); chỉ `zero_shot_project_aware` có thêm khối M.

## 3. Flow 2.2: Prompting Strategy

| Quy trình | System | User message |
| --- | --- | --- |
| `zero_shot` | `generation/system.md` | chỉ dẫn + Source Code Context |
| `zero_shot_project_aware` | như trên | chỉ dẫn + **khối M** (Mục 2.2) + Source Code Context |
| `few_shot` | như trên | 2 ví dụ (CUT → test) + chỉ dẫn + Source Code Context |

**Ví dụ few-shot** (`prompts/fewshot/examples.json`):
1. Chọn **một lần** trước khi chạy, giống nhau cho mọi CUT: một ví dụ JUnit 4, một ví dụ JUnit 5.
2. Nguồn: cặp CUT – test người viết của Classes2Test, **ngoài** tập đánh giá và tập pilot, khác repository với mọi task.
3. Điều kiện: test người viết pass trên commit đã dựng lại; CUT dưới 150 dòng; test có ít nhất 3 test method và 3 assertion; chọn bằng seed 42 trong các cặp đạt điều kiện.
4. Lệnh `arrow-fw fewshot select` ghi file và sha256; file được commit.

Chế độ REPAIR không dùng Prompting Strategy (quy trình sinh của candidate chỉ là một phần định danh chain).

**Test framework trong prompt sinh:** template sinh **không** nêu tên test framework (JUnit 4/5, TestNG); thông tin này chỉ đến với model qua khối M của `zero_shot_project_aware` (đúng điểm khác biệt của RQ1). `zero_shot` và `few_shot` để model tự chọn; chọn sai framework so với dự án thì lỗi biên dịch hoặc DISCOVERY là kết quả thật của quy trình đó. Ví dụ few-shot là JUnit 4 và JUnit 5, không có TestNG (2/50 CUT pilot dùng TestNG).

## 4. Flow 2.3: Candidate Test Generation, LLM Abstraction Layer

Lớp này là cách duy nhất framework gọi model (`ILlmClient`), gồm 4 việc theo hình. Model được gọi là **Selected LLM (commercial API / open-weight model)**: GPT-6 Luna qua API và Qwen3-Coder-30B tự host qua Ollama; **không fine-tune** (chốt Q1).

### 4.1. Standardize Input Format

Mọi lượt gọi là một `LlmRequest`:

```json
{ "model": "gpt-6-luna",
  "messages": [ { "role": "system", "content": "…" }, { "role": "user", "content": "…" } ],
  "params": { "reasoning_effort": "none", "temperature": 0, "max_tokens": 8192, "seed": 42 } }
```

Chỉ hai vai `system` và `user`, một lượt (không hội thoại nhiều lượt; lượt sửa gửi lại đủ ngữ cảnh). Gửi tới proxy theo chuẩn OpenAI `POST /v1/chat/completions`.

### 4.2. Configure Inference Hyperparameters

- Tham số lấy **nguyên văn** từ `models[i].params` của experiment, giống nhau cho mọi quy trình, mọi cơ chế, mọi vòng. Không có tham số nào thay đổi theo task.
- Không có `drop_params`: proxy từ chối tham số không hỗ trợ thì báo lỗi cấu hình (dừng run), không âm thầm bỏ qua.

### 4.3. Route Request to Selected Model

1. Tên model trong request là `models[i].name`. Proxy LiteLLM ánh xạ tên này tới provider theo `litellm.config.local.yaml`:

   ```yaml
   model_list:
     - model_name: gpt-6-luna
       litellm_params: { model: openai/gpt-6-luna, api_base: <endpoint>, api_key: <khóa>, allowed_openai_params: ["reasoning_effort"] }
     - model_name: qwen3-coder-30b
       litellm_params: { model: ollama_chat/qwen3-coder:30b, api_base: http://localhost:11434 }
   general_settings: { master_key: <khóa bảo vệ proxy> }
   ```

2. `proxy_mode = auto_start`: framework bật proxy khi cần gọi model và tắt khi xong; `external`: dùng proxy đang chạy ở `proxy_url`.
3. **Kiểm tra đúng model:** `model` trong response phải bắt đầu bằng `expected_model_id_prefix` (hoặc chứa `/<prefix>`). Sai → lỗi cấu hình `B2_WRONG_MODEL`, run tạm dừng.
4. **Giới hạn:** tối đa `workers.llm` lượt cùng lúc, `rpm_limit` lượt mỗi phút; `--max-llm-calls` (tùy chọn) chặn tổng số lượt của run.
5. **Gọi lại khi lỗi tạm thời** (429, 5xx, lỗi mạng, timeout 300 s): tối đa 3 lần, chờ 20 s, 60 s, 120 s. Lần gọi lỗi **không** tính là vòng sửa; số lần gọi lại ghi vào `usage.json`. Hết lượt → job `INFRA` (`B2_LLM_UNAVAILABLE`).
6. **Lỗi cấu hình** (400, 401, 403, 404, hết số dư): không gọi lại, run tạm dừng với mã `B2_LLM_CONFIG` để người chạy sửa khóa/endpoint rồi chạy tiếp.

### 4.4. Cache

Cache **chỉ** dùng để **phát lại một lượt gọi đã hoàn thành khi chạy tiếp** sau khi run dừng (Ctrl+C, mất điện, chạy lại job INFRA), để không trả tiền hai lần cho cùng một lượt. Cache **không** được dùng để thay cho một lượt gọi mới.

- **Khóa cache = định danh lượt gọi + nội dung request**: SHA-256 của JSON chuẩn hóa `{call_id, model, params, messages}`, trong đó `call_id` xác định duy nhất lượt gọi trong run:
  - chế độ GENERATE: `gen/<task_id>/<model>/<strategy>`;
  - chế độ REPAIR: `repair/<task_id>/<model>/<strategy>/<mechanism>/round_<k>`.
  Vì vậy hai cơ chế, hai chain, hay hai vòng khác nhau **không bao giờ** dùng chung một mục cache, kể cả khi prompt giống hệt nhau từng chữ.
- **Phát lại giữ số liệu gốc:** mục cache lưu cả `latency_seconds`, `prompt_tokens`, `completion_tokens`, `cost_usd`, `model_id` của lần gọi thật. Khi phát lại, `usage.json` ghi lại **đúng các số gốc** đó (cộng `replayed: true`), để thời gian sửa và chi phí của RQ2 không phụ thuộc việc run có bị dừng giữa chừng hay không.
- Hệ quả (sửa so với bản spec trước):
  - Vòng 1 của Fixed và Adaptive có cùng prompt nhưng là hai lượt gọi riêng, **mỗi cơ chế gọi model thật**; không bên nào được thời gian 0.
  - Fixed gặp output không hợp lệ thì vòng sau gửi lại cùng prompt (bản mới nhất không đổi) nhưng là lượt gọi **mới**, model được hỏi lại thật; nếu model vẫn trả lỗi đó là hành vi thật của model (temperature 0), không phải do cache.
  - Adaptive ở mức 3 gặp mã trùng: vòng sau đã khác prompt nhờ quyết định Q5 (về mức 2, danh sách đã thử dài thêm).
- Thứ tự Fixed/Adaptive vẫn xen kẽ theo task (AR Mục 8) để cân bằng hiệu ứng cache phía nhà cung cấp (nếu provider có cache riêng).

### 4.5. Parse Generated Code

Từ nội dung response tới mã Java dùng được:

1. **Tách khối mã:** lấy khối ```` ```java ```` (hoặc ```` ``` ````) đầu tiên có khai báo `class`; không có khối mã thì lấy toàn bộ văn bản nếu nó bắt đầu bằng `package`/`import`/`class`/`public`.
2. **Kiểm tra cú pháp** bằng java-helper (JavaParser): phải parse được và có đúng một class top-level. Không đạt → `INVALID_OUTPUT` với lý do (`no_code`, `parse_error`, `multiple_classes`).
3. **Chuẩn hóa tên** (ghi từng thao tác vào `normalization`):
   - `package` = package của CUT (thêm hoặc sửa);
   - tên class = `<CUT>_ArrowTest` (đổi tên class và các tham chiếu tới tên cũ trong file);
   - bỏ khai báo `package`/`import` bị lặp.
   Không thêm hay sửa import, không sửa logic test: lỗi do model là lỗi của candidate.
4. **Chế độ REPAIR, mức 3** (`rewrite_methods` khác rỗng): lấy từ class trả về **chỉ các method có tên trong `rewrite_methods`**, ghép vào `base_code` thay cho method cùng tên (java-helper `splice`). Method khác của `base_code` giữ nguyên từng ký tự; import, field và helper method mới trong response được thêm vào. Method trong `rewrite_methods` mà response không có thì bị bỏ khỏi class (ghi `dropped_methods`).
5. Tính `code_hash` = SHA-256 của mã sau khi bỏ comment và khoảng trắng thừa (java-helper), để khối 3 phát hiện mã trùng.

## 5. Output: Candidate Unit Test (T)

Thư mục `block2/<model>/<strategy>/` (GENERATE) hoặc `block3/…/<mechanism>/round_<k>/` (REPAIR):

| File | Nội dung |
| --- | --- |
| `prompt.json` | messages, params, template và sha256 của template, `source_truncation` |
| `response.json` | body gốc của API |
| `usage.json` | `call_id`, `model_id`, `prompt_tokens`, `completion_tokens`, `finish_reason`, `latency_seconds`, `attempts`, `cost_usd`, `replayed` (true khi phát lại từ cache lúc chạy tiếp; các số vẫn là số của lần gọi thật) |
| `candidate.java` | mã đã chuẩn hóa (không có khi output không hợp lệ) |
| `candidate.json` | xem dưới |

```json
{
  "task_id": "…", "model": "gpt-6-luna", "strategy": "zero_shot", "mode": "GENERATE",
  "valid_output": true, "invalid_reason": null,
  "test_fqn": "com.x.FileResolver_ArrowTest", "test_path": "jpa2ddl-core/src/test/java/com/x/FileResolver_ArrowTest.java",
  "code_hash": "sha256:…", "test_method_count": 6, "assertion_count": 11,
  "normalization": [ "package: added", "class: FileResolverTest → FileResolver_ArrowTest" ],
  "dropped_methods": [], "finish_reason": "stop"
}
```

`finish_reason = "length"` (output bị cắt) vẫn được xử lý như bình thường, nhưng được đếm riêng trong báo cáo để biết `max_tokens` có đủ không.

## 6. Mã lỗi

| Mã | Kết quả | Khi nào |
| --- | --- | --- |
| `B2_INVALID_OUTPUT` | Candidate không hợp lệ (không phải INFRA) | Không có mã, không parse được, nhiều class |
| `B2_LLM_UNAVAILABLE` | INFRA | Hết lượt gọi lại |
| `B2_LLM_CONFIG` | Run tạm dừng | 400/401/403/404, hết số dư, thiếu cấu hình model |
| `B2_WRONG_MODEL` | Run tạm dừng | Model id trả về không khớp |
| `B2_CALL_LIMIT` | Run dừng | Vượt `--max-llm-calls` |

Candidate `INVALID_OUTPUT` ở chế độ GENERATE vẫn được ghi vào kết quả RQ1 (tính là không biên dịch được) nhưng **không** vào khối 3 sửa (không có mã để sửa), như `Adaptive repair.md` Mục 8.

## 7. Interface C#

```csharp
public interface ITestGenerator
{
    Task<CandidateTest> GenerateAsync(GenerateRequest request, CancellationToken ct);  // chế độ GENERATE
    Task<CandidateTest> RepairAsync(RepairRequest request, CancellationToken ct);      // chế độ REPAIR
}

public interface ILlmClient
{
    Task<LlmResponse> CompleteAsync(LlmRequest request, CancellationToken ct);
}
// Triển khai: LiteLlmClient (HTTP tới proxy), ReplayingLlmClient (phát lại khi chạy tiếp, Mục 4.4), LlmGateway (giới hạn, kiểm tra model),
// FakeLlmClient (test: chọn câu trả lời theo luật khớp nội dung prompt).
```

## 8. Kiểm thử

- Unit: dựng prompt cho 3 quy trình (chỉ `zero_shot_project_aware` có khối M); khối M rút gọn từ M mẫu; tách mã từ response có/không có khối mã, nhiều class; chuẩn hóa package và tên class; ghép method mức 3; khóa cache ổn định khi đổi thứ tự tham số và **khác nhau** giữa hai `call_id` có cùng prompt; phát lại giữ thời gian và chi phí gốc.
- Integration: `LiteLlmClient` với HTTP giả (200, 429 rồi 200, 401, sai model id).

## 9. Ví dụ (CUT minh họa của spec 01 Mục 10)

### 9.1. Input (chế độ GENERATE, quy trình `zero_shot_project_aware`)

- M: `block1/metadata.json` ở spec 01 Mục 10.3.
- CUT: `OrderService.java` (180 dòng), model `gpt-6-luna`, `strategy = zero_shot_project_aware`.

### 9.2. Flow chạy từng bước

| Bước | Việc framework làm | Kết quả ví dụ |
| --- | --- | --- |
| 2.1 Source Code Context | Đọc nguyên văn `OrderService.java` (7.400 ký tự < 48.000) | `source_truncation = none` |
| 2.2 Repository Metadata | Dựng khối M rút gọn từ `metadata.json` | 7 dòng (dưới) |
| 2.3 Instruction Strategy | Chọn `generation/system.md` + `generation/zero_shot_project_aware.md` | sha256 của 2 template ghi vào `prompt.json` |
| 3 Prompting Strategy | `zero_shot_project_aware` = chỉ dẫn + khối M + mã CUT | 2 message |
| 4.1 Chuẩn hóa input | `LlmRequest { model, messages, params }` | |
| 4.2 Tham số | `reasoning_effort: none, temperature: 0, max_tokens: 8192, seed: 42` | |
| 4.4 Cache | Khóa `sha256(call_id + model + params + messages)`, `call_id = gen/555001_…/gpt-6-luna/zero_shot_project_aware`; chưa có (run mới) | Gọi model thật |
| 4.3 Định tuyến | `POST http://127.0.0.1:4000/v1/chat/completions`, LiteLLM chuyển tới provider của `gpt-6-luna` | 200 OK sau 8,7 s; model id `gpt-6-luna` ✔ |
| 4.5 Tách mã | Lấy khối ```` ```java ```` đầu tiên; parse được, 1 class | Class tên `OrderServiceTest` |
| 4.5 Chuẩn hóa | package đúng; đổi tên class thành `OrderService_ArrowTest` | `normalization = ["class: OrderServiceTest → OrderService_ArrowTest"]` |
| 4.5 Hash | Bỏ comment, khoảng trắng → SHA-256 | `sha256:9f3c…` |

Prompt gửi đi (rút gọn):

```
[system]  You are an expert Java developer who writes unit tests. Reply with one complete Java test class in a single ```java code block.

[user]    Write a unit test class for the class below.
          The test class must be in package com.acme.shop and named OrderService_ArrowTest.

          Repository configuration:
          - Build tool: Maven 3.9.6 (module shop-core, multi-module project)
          - Java version: 8 (compile with JDK 8; do not use APIs newer than Java 8)
          - Test framework: JUnit 4 (4.13.2); mocking: Mockito 3.12.4
          - Dependencies (direct): com.google.guava:guava:30.1-jre, …
          - Project structure: modules shop-core, shop-api; CUT package com.acme.shop
          - Source path: shop-core/src/main/java
          - Test path: write the test to shop-core/src/test/java/com/acme/shop/OrderService_ArrowTest.java

          File: shop-core/src/main/java/com/acme/shop/OrderService.java
          ```java
          package com.acme.shop;
          public class OrderService { … }
          ```
```

Với `zero_shot`: cùng prompt nhưng **không có** khối "Repository configuration". Với `few_shot`: thêm 2 ví dụ (CUT → test) trước chỉ dẫn, không có khối M.

### 9.3. Output

`candidate.java` (rút gọn):

```java
package com.acme.shop;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Test;

public class OrderService_ArrowTest {
    @Test
    public void totalPriceAddsTax() {
        OrderRepository repo = mock(OrderRepository.class);
        when(repo.items(1L)).thenReturn(java.util.Arrays.asList(new Item("a", 100)));
        OrderService service = new OrderService(repo, new TaxPolicy(10));
        assertEquals(110, service.totalPrice(1L));
    }
    @Test(expected = IllegalStateException.class)
    public void cancelTwiceFails() { … }
}
```

`candidate.json`: `valid_output: true`, `test_method_count: 5`, `assertion_count: 7`, `code_hash: "sha256:9f3c…"`, `finish_reason: "stop"`. `usage.json`: 2.410 token vào, 980 token ra, 8,7 s, `cost_usd` = 2410/1e6 × 0,05 + 980/1e6 × 0,25 = 0,00037.

### 9.4. Ví dụ output không hợp lệ

| Model trả về | Framework xử lý | Kết quả |
| --- | --- | --- |
| Chỉ có lời giải thích, không có mã | Bước tách mã không tìm thấy class | `INVALID_OUTPUT`, `invalid_reason = no_code` |
| Hai class top-level trong một khối mã | Parse được nhưng 2 class | `INVALID_OUTPUT`, `multiple_classes` |
| Mã bị cắt giữa chừng (`finish_reason = length`) | Parse lỗi | `INVALID_OUTPUT`, `parse_error`; đếm vào số lượt bị cắt |
| Class tên `TestOrderService`, thiếu `package` | Thêm `package com.acme.shop;`, đổi tên class | Hợp lệ; `normalization` ghi 2 thao tác |

### 9.5. Ví dụ chế độ REPAIR (khối 3 gửi sang)

Khối 3 gửi `RepairRequest` mức 2 cho trạng thái COMPILE (chain Adaptive, vòng 2). Khối 2 dựng prompt:

```
[system]  (repair/system.md) You fix failing Java unit tests. Reply with the complete fixed test class in one ```java block.
[user]    (repair/level2.md)
          The test class below does not compile.
          Repository configuration:                         (khối M đầy đủ, có ở mọi prompt sửa)
          - Build tool: Maven 3.9.6 (module shop-core, multi-module project)
          - Java version: 8 (compile with JDK 8; do not use APIs newer than Java 8)
          - Test framework: JUnit 4 (4.13.2); mocking: Mockito 3.12.4
          - Dependencies (direct): com.google.guava:guava:30.1-jre, …
          - Project structure: modules shop-core, shop-api; CUT package com.acme.shop
          - Source path: shop-core/src/main/java
          - Test path: shop-core/src/test/java/com/acme/shop/OrderService_ArrowTest.java
          Errors:
            OrderService_ArrowTest.java:[14,30] cannot find symbol  symbol: method getTotal(long)  location: class OrderService
          Additional information:
            Constructors and methods of com.acme.shop.OrderService:
              public OrderService(OrderRepository repo, TaxPolicy tax)
              public long totalPrice(long orderId)
              public void cancel(long orderId)
          Attempts that already failed (do not repeat them):
            - round 1: COMPILE | cannot find symbol | method
          Test class: ```java … ```
          Class under test: ```java … ```
```

Output: `candidate.java` mới của vòng 2, đi lại vào khối 3 Flow 3.1.
