# Prompt của ARROW Framework

**Trạng thái:** bản nháp (14/10/2026). Ba system prompt sinh test do nhóm viết (`v2-draft2`, `few_shot` `v2-draft3`); user message sinh test và prompt sửa viết theo cùng phong cách (`v2-draft1`/`v2-draft2`), đã sửa theo review prompt.

## 1. Danh sách file

| File | Vai trò | Dùng cho |
| --- | --- | --- |
| `generation/system/zero_shot.md` | system | Sinh test, quy trình `zero_shot` |
| `generation/system/zero_shot_project_aware.md` | system | Sinh test, quy trình `zero_shot_project_aware` |
| `generation/system/few_shot.md` | system | Sinh test, quy trình `few_shot` (2 ví dụ nằm sẵn trong file) |
| `generation/user/default.md` | user | Sinh test, `zero_shot` và `few_shot` |
| `generation/user/project_aware.md` | user | Sinh test, `zero_shot_project_aware` |
| `repair/system.md` | system | Sửa test, Fixed (mọi vòng) và Adaptive (mọi mức) |
| `repair/user/level1.md` | user | Fixed (mọi vòng) và Adaptive mức 1 |
| `repair/user/level2.md` | user | Adaptive mức 2 (và khi mức 3 không áp dụng) |
| `repair/user/level3.md` | user | Adaptive mức 3 (chỉ COMPILE, RUNTIME, ASSERTION khi biết method lỗi) |
| `repair/failure_summaries.md` | dữ liệu | Câu mô tả lỗi theo trạng thái, điền vào `{{failure_summary}}` |
| `generation/fewshot_provenance.json` | dữ liệu | Nguồn gốc 2 ví dụ few-shot (repository, commit, đường dẫn, sha256, quy tắc chọn) và kết quả xác minh (`PENDING` tới bước C3) |

Mỗi lượt gọi model gồm đúng 2 message: **system** + **user**.

| Lượt | System | User |
| --- | --- | --- |
| Sinh, `zero_shot` | `generation/system/zero_shot.md` | `generation/user/default.md` |
| Sinh, `zero_shot_project_aware` | `generation/system/zero_shot_project_aware.md` | `generation/user/project_aware.md` |
| Sinh, `few_shot` | `generation/system/few_shot.md` | `generation/user/default.md` |
| Sửa, Fixed, mọi vòng | `repair/system.md` | `repair/user/level1.md` |
| Sửa, Adaptive mức 1 / 2 / 3 | `repair/system.md` | `repair/user/level1.md` / `level2.md` / `level3.md` |

## 2. Bảo đảm so sánh công bằng

- **RQ1 (3 quy trình sinh):** phần "Shared requirements" và "Output contract" của 3 system prompt giống nhau từng chữ (đã kiểm bằng script); chỉ đoạn "Technique" khác nhau. Hai user message giống nhau từng chữ, chỉ `project_aware.md` có thêm khối "Repository configuration". Cả 3 quy trình đều được nêu package và tên class test (để mọi quy trình cùng điều kiện đặt file).
- **Chất lượng khi sửa:** `repair/system.md` chép **nguyên văn** phần "Shared requirements" và "Output contract" của prompt sinh test (framework kiểm tra khi khởi động), rồi thêm mục "Repair requirements" R1–R5 riêng cho việc sửa.
- **RQ2 (Fixed vs Adaptive):** cùng system prompt sửa; Fixed dùng đúng mẫu mức 1 của Adaptive, nên vòng 1 của hai cơ chế có prompt giống hệt nhau. Mọi prompt sửa đều có khối cấu hình repository đầy đủ (chốt Q4 = C). Khác biệt chỉ ở mức 2 (thông tin bổ sung theo loại lỗi + danh sách đã thử) và mức 3 (như mức 2 nhưng chỉ viết lại method lỗi), đúng thiết kế Adaptive Repair.

## 3. Cách điền chỗ trống

- Phần đầu file giữa hai dòng `---` (id, version, role, used_by, placeholders) **không** gửi cho model; framework đọc nó để kiểm tra và ghi `id`, `version`, sha256 của từng file vào `run.lock.json`.
- Chỗ trống viết `{{tên}}`. Framework thay bằng chuỗi đã dựng sẵn; thiếu giá trị cho chỗ trống nào thì báo lỗi, không gửi.

| Chỗ trống | Nguồn | Định dạng |
| --- | --- | --- |
| `cut_fqn`, `cut_path`, `cut_source` | M (khối 1), file CUT | FQN; đường dẫn từ gốc repository; mã nguồn nguyên văn (spec 02 Mục 2.1) |
| `test_package`, `test_class_name` | M | Package của CUT; `<CUT>_ArrowTest` |
| `repository_configuration` | M | Khối dưới đây (spec 02 Mục 2.2) |
| `failure_summary` | trạng thái của bản đang sửa | Một câu trong `repair/failure_summaries.md` |
| `error_block` | khối 3, Diagnosis | Tối đa 60 dòng (spec 03 Mục 5) |
| `extra_context` | khối 3, ExtraContextBuilder | Theo trạng thái, tối đa 200 dòng (spec 03 Mục 6.5) |
| `failed_attempts` | khối 3, danh sách đã thử | 3 lần gần nhất có đủ signature, lỗi đầu tiên, dòng mã đã đổi; lần cũ hơn chỉ một dòng (dưới; spec 03 Mục 6.4) |
| `failing_methods` | khối 3, Diagnosis | Tên method cách nhau dấu phẩy, mỗi tên trong dấu `` ` `` |
| `test_source` | bản đang sửa (checkpoint của Adaptive, bản mới nhất của Fixed) | Mã nguyên văn |

**`repository_configuration`** (sinh bằng code từ M):

```
- Build tool: Maven 3.9.6 (module shop-core, multi-module project, build root .)
- Java version: 8 (compile with JDK 8; do not use language features or APIs newer than Java 8)
- Test framework: JUnit 4 (4.13.2); mocking: Mockito 3.12.4; assertions: none beyond JUnit
- Dependencies (direct): com.google.guava:guava:30.1-jre, … (tối đa 25 dòng, ưu tiên thư viện test)
- Project structure: modules shop-api, shop-core; CUT package com.acme.shop
- Source path: shop-core/src/main/java
- Test path: shop-core/src/test/java/com/acme/shop/OrderService_ArrowTest.java
```

**`failed_attempts`** (ví dụ 4 lần thử; 3 lần gần nhất có chi tiết):

```
- Round 1 · COMPILE | cannot find symbol | method
- Round 2 · COMPILE | cannot find symbol | method
  First error: cannot find symbol: method calcTotal(long) in class com.acme.shop.OrderService
  Changed code:
    -        long total = service.getTotal(1L);
    +        long total = service.calcTotal(1L);
- Round 3 · duplicate code (same as round 2)
  Changed code:
    -        long total = service.getTotal(1L);
    +        long total = service.calcTotal(1L);
- Round 4 · invalid output: no Java class found
```

## 4. Ví dụ một prompt sửa mức 2 sau khi điền

```
[system] (repair/system.md, bỏ phần đầu file)

[user]
The test class below failed ARROW's verification. It does not compile. Earlier repairs of this failure did not succeed, so additional information is provided.

Required test class
- Package: com.acme.shop
- Class name: OrderService_ArrowTest

Repository configuration (compatibility constraints):
- Build tool: Maven 3.9.6 (module shop-core, multi-module project, build root .)
- Java version: 8 (…)
- …

Failure output:
OrderService_ArrowTest.java:[14,30] cannot find symbol
  symbol:   method getTotal(long)
  location: class com.acme.shop.OrderService
    long total = service.getTotal(1L);

Additional information for this failure:
Constructors, methods and fields of com.acme.shop.OrderService:
  public OrderService(com.acme.shop.repo.OrderRepository repo, com.acme.shop.TaxPolicy tax)
  public long totalPrice(long orderId)
  public void cancel(long orderId)

Approaches that already failed (do not repeat them; each entry shows the round, the failure signature, the first error, and the code that was changed):
- Round 1 · COMPILE | cannot find symbol | method
  First error: cannot find symbol: method getTotal(long) in class com.acme.shop.OrderService
  Changed code:
    -        long total = service.getTotal(1L);
    +        long total = service.getTotal(1L, true);

Current test class:
```java
package com.acme.shop;
…
```

Target CUT com.acme.shop.OrderService (shop-core/src/main/java/com/acme/shop/OrderService.java):
```java
package com.acme.shop;
…
```

Return only the complete corrected Java source of OrderService_ArrowTest, according to the output contract.
```

## 5. Điểm cần bạn duyệt

| # | Điểm | Đang viết |
| --- | --- | --- |
| P2 | Mức 3: model được bỏ method lỗi không | Không nói gì về việc bỏ method (không mời bỏ). Nếu câu trả lời thiếu method trong danh sách thì method đó bị bỏ và được đếm (AR Mục 3.1) |
| P3 | Mã CUT trong user message đặt trong ```` ```java ```` | Giữ fence ở **đầu vào** để tách rõ mã nguồn; output contract yêu cầu **đầu ra** không có fence, framework bóc fence nếu model vẫn thêm |
| P4 | Version | Trước thực nghiệm chính đổi tất cả thành một version chốt (ví dụ `v2`) |

(P1 về assertion khi sửa đã giải quyết: `repair/system.md` dùng nguyên văn quy tắc chung 4, 5, 7 của prompt sinh và R4 cho phép đổi expected khi mã CUT cho thấy expected sai.)

## 6. Độ dài prompt (đo 14/10/2026)

Đo bằng tokenizer `o200k_base` (họ GPT-4o/GPT-5; tokenizer của GPT-6 Luna không công bố nên là xấp xỉ), trên **mã CUT thật** của 59 CUT pilot (đọc từ mirror ở đúng commit).

| Phần cố định | Token |
| --- | ---: |
| System sinh `zero_shot` / `zero_shot_project_aware` / `few_shot` | 655 / 755 / 2.019 |
| System sửa | 743 |
| Khung user (không tính chỗ trống): sinh 69–77; sửa mức 1 / 2 / 3 | 69 / 123 / 192 |
| Khối cấu hình repository (M) | 173 (ít dependency) – 536 (25 dependency) |
| Mã CUT (59 CUT pilot) | min 74 · trung vị 1.044 · p90 3.198 · max 15.406 |

| Prompt đầu vào cả lượt | Trung vị | p90 | Lớn nhất |
| --- | ---: | ---: | ---: |
| Sinh `zero_shot` | 1.808 | 3.962 | 16.170 |
| Sinh `zero_shot_project_aware` | 2.089 | 4.243 | 16.451 |
| Sinh `few_shot` | 3.172 | 5.326 | 17.534 |
| Sửa mức 1 (= Fixed), ước tính | ~3.500 | ~7.800 | |
| Sửa mức 2 / 3, ước tính | ~4.300 | ~10.700 | ~22.900 (CUT lớn nhất, mọi khối tối đa) |

Phần sửa là ước tính: mã test lấy theo độ dài test sinh ra ở pilot cũ (trung vị 1.146, p90 2.260 token), khối lỗi 300–900, thông tin bổ sung 500–2.500, mỗi lần thử có chi tiết khoảng 75 token. CUT lớn nhất (15.406 token, khoảng 60.000 ký tự) vượt ngưỡng 48.000 ký tự của spec 02 Mục 2.1 nên khi chạy thật sẽ bị bỏ comment hoặc rút còn chữ ký, tức thấp hơn số trên.

**Trần đầu ra và cửa sổ ngữ cảnh** (chốt 14/10/2026): `max_tokens = 8192` cho mọi model (trần, không phải số token phải trả; pilot cũ lớn nhất 2.944, không lượt nào bị cắt). Prompt dài nhất khoảng 23.000 token + 8.192 token đầu ra nên Qwen qua Ollama cần `num_ctx` ≥ 32.768 (spec 02 Mục 4.2).
