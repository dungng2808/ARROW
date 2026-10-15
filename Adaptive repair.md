# Adaptive Repair — Thiết kế chi tiết

**Trạng thái:** Thiết kế đã chốt ngày 08/10/2026 (Đóng góp 2 của ARROW). Đã triển khai và đối chiếu với code ngày 09/10/2026, cập nhật theo ARROW Framework và các quyết định Q4–Q8 ngày 14/10/2026 (mục 13, dòng 14–19); các điểm làm rõ hoặc thay đổi so với bản 08/10 được đánh dấu *(cập nhật 09/10/2026)* và tổng hợp ở mục 13. Lỗi được mô tả theo **ba nhóm build, runtime, assertion**, mỗi nhóm gồm các trạng thái ở mục 2.1; việc gom nhóm không làm thay đổi thuật toán đã triển khai.
**Code (cập nhật 14/10/2026):** ARROW Framework (C#, `ARROW/ARROW_Framework/`, lệnh `arrow`). Thuật toán: `src/Arrow.Service/Block3/RepairPolicies.cs` (`AdaptiveRepairPolicy`, `FixedRepairPolicy`, `CheckpointComparer`), vòng lặp `RepairRunner.cs`, xác minh `Verifier.cs`, phân loại và signature `TestResultClassifier.cs`, `JavacErrorParser.cs`, `SignatureBuilder.cs`, thông tin bổ sung `ExtraContextBuilder.cs`; template `ARROW_Framework/prompts/repair/`; đếm assertion bằng java-helper (`count_assertions`); bảng RQ2 do khối 4 (`ReportBuilder.cs`) sinh. Spec chi tiết và chuẩn: `ARROW_Framework/specs/03-adaptive-repair.md` (khi tài liệu này và spec khác nhau, spec 03 là bản đã code). Code cũ `src/repair_v2/` và `pilot_tool` không còn dùng.
**Lưu ý:** Trước khi viết Report 4 (Methodology) hoặc trình bày, vẫn phải đối chiếu tài liệu này với code; nếu khác nhau, sửa code theo thiết kế hoặc sửa thiết kế theo code — không để báo cáo mô tả khác với cái đã chạy.

---

## 1. Tóm tắt

Khi một unit test do LLM sinh ra bị lỗi, ARROW cho LLM sửa qua nhiều vòng. **Fixed Repair** dùng cùng một prompt ở mọi vòng và luôn sửa tiếp từ bản mới nhất. **Adaptive Repair** sau mỗi vòng ghi lại bốn thông tin (test đang hỏng ở bước nào, lỗi thuộc dạng nào, mã đã từng sinh chưa, bản tốt nhất là bản nào) và dựa vào đó quyết định ba việc: **đổi cách sửa** (strategy switching), **quay về bản tốt hơn** (rollback) và **nhận ra khi không còn tiến triển** (stagnation detection).

Câu nói ngắn cho hội đồng:

> "Fixed Repair sửa mù: cùng một prompt, luôn sửa tiếp từ bản mới nhất. Adaptive Repair có trí nhớ: nó biết lỗi đã đổi loại hay đang lặp lại, biết bản nào tốt nhất để quay về, và biết khi nào LLM đang sinh lại mã cũ."

---

## 2. Bốn thông tin được theo dõi sau mỗi vòng

### 2.1. Failure state — test đang hỏng ở bước nào

Test phải đi qua chuỗi bước xác minh của Đóng góp 3. Hỏng ở bước nào thì nhận trạng thái đó:

```
COMPILE → DISCOVERY → RUNTIME → ASSERTION → REGRESSION → VALID
(tệ nhất)                                                (đích)
```

| Trạng thái | Ý nghĩa | Ví dụ |
|---|---|---|
| COMPILE | Không biên dịch được | Gọi phương thức không tồn tại, sai kiểu |
| DISCOVERY | Biên dịch được nhưng test runner không phát hiện test | Sai thư mục/package, thiếu annotation `@Test` |
| RUNTIME | Chạy thì văng exception | `NullPointerException`, mock dùng sai |
| ASSERTION | Chạy xong nhưng kết quả không khớp | `expected:<100> but was:<90>` |
| REGRESSION | Test mới pass nhưng làm test sẵn có của module fail | Dùng chung biến static, file, cấu hình |
| VALID | Qua tất cả các bước | ✅ |

**Quy ước:** bản đi xa hơn trong chuỗi là bản tốt hơn.

*(Cập nhật 09/10/2026)* Khi triển khai:
- **UNKNOWN** (log build không phân loại được) xếp ngay sau COMPILE. **Build timeout** và **lỗi công cụ** là lỗi hạ tầng, không thuộc chuỗi. *(Cập nhật 14/10/2026, chốt Q8)* Chạy lại đúng bước đó tối đa **3 lần thử**; vẫn lỗi thì job nhận `INFRA` (`B3_TIMEOUT`, `B3_DEPENDENCY`, `B3_TOOL_ERROR`), vào danh sách **"Cần chạy lại riêng"** kèm lệnh `arrow run --run-id <run> --tasks <task_id> --rerun-infra`; chưa tính vào RQ1/RQ2 cho tới khi chạy lại xong.
- Phân loại lỗi biên dịch chỉ dựa trên dấu hiệu thật của javac/Maven/Gradle. Trước đây dòng `java.lang.AssertionError:` bị nhận nhầm là lỗi biên dịch (chuỗi con `error:`), làm ASSERTION bị xếp thành COMPILE.
- **REGRESSION** chỉ phát hiện được khi lần chạy toàn module chạy cả test sinh ra. Tên test sinh ra `<Focal>Test_<hash>` không khớp mẫu mặc định của Maven Surefire (`*Test`, `Test*`, `*Tests`, `*TestCase`), nên lần chạy toàn module truyền thêm `-Dsurefire.includesFile` (mẫu mặc định + tên test sinh ra), không sửa `pom.xml`.

**Ba nhóm lỗi** *(cập nhật 09/10/2026)*. Các trạng thái lỗi được gom thành ba nhóm; mỗi trạng thái là một **loại con** của nhóm:

| Nhóm | Loại con (trạng thái) | Ý nghĩa |
|---|---|---|
| **BUILD** | COMPILE, UNKNOWN, DISCOVERY | Test chưa vào được bước chạy: không biên dịch được, log build không phân loại được, hoặc test runner không phát hiện test |
| **RUNTIME** | RUNTIME, REGRESSION | Lỗi khi chạy: test văng exception, hoặc test pass nhưng làm test sẵn có của module fail |
| **ASSERTION** | ASSERTION | Test chạy xong nhưng kết quả không khớp với assertion (kể cả `assertThrows` hay `@Test(expected = …)` thất bại) |

- **Prompt, mức strategy, thông tin bổ sung ở mức 2 và cách làm ở mức 3 vẫn chọn theo loại con (trạng thái)**, đúng như code (spec 03 Mục 6.5). Nhóm dùng để mô tả và báo cáo (ví dụ thống kê số ứng viên theo nhóm lỗi); nhóm suy ra trực tiếp từ trạng thái nên log không cần trường riêng.
- Đổi nhóm luôn là đổi trạng thái, nên quy tắc "trạng thái đổi → mức 1" ở mục 3.1 đã bao gồm việc đổi nhóm.
- **Thứ tự nhóm không dùng để so sánh checkpoint**: REGRESSION thuộc nhóm RUNTIME nhưng trong chuỗi xác minh đứng sau ASSERTION vì test mục tiêu đã pass. Checkpoint luôn so theo chuỗi ở trên.
- Phân loại theo nhóm là **bước nền**, không phải điểm mới của ARROW (TestART cũng phân lỗi biên dịch, runtime, assertion); điểm khác biệt nằm ở cách dùng lịch sử sửa (mục 3).

### 2.2. Normalized error signature — lỗi thuộc dạng nào

Thông báo lỗi sau khi chuẩn hóa: bỏ số dòng, đường dẫn file, tên biến, giá trị cụ thể; giữ trạng thái, loại lỗi và loại đối tượng gây lỗi.

```
Lỗi gốc:   OrderServiceTest.java:42: error: cannot find symbol  symbol: method getTotal(long)
Signature: COMPILE | cannot find symbol | method

Lỗi gốc:   OrderServiceTest.java:57: error: cannot find symbol  symbol: method calculateTotal(long)
Signature: COMPILE | cannot find symbol | method        ← TRÙNG với lỗi trên
```

Dùng để nhận ra LLM **mắc lại cùng một dạng lỗi**, dù lỗi nằm ở dòng khác hoặc tên khác.

*(Cập nhật 09/10/2026)* Dạng signature đã triển khai, lấy từ lỗi đầu tiên (ưu tiên lỗi nằm trong file test sinh ra):

| Trạng thái | Signature | Ví dụ |
|---|---|---|
| COMPILE | `COMPILE \| loại lỗi javac \| loại symbol` | `COMPILE \| cannot find symbol \| method` |
| RUNTIME | `RUNTIME \| tên exception \| Class.method` (frame đầu tiên thuộc code dự án, không phải thư viện/test) | `RUNTIME \| NullPointerException \| OrderService.totalPrice` |
| ASSERTION | `ASSERTION \| loại assertion error \| test method bị fail` | `ASSERTION \| AssertionError \| testGetOneNotFound` |
| REGRESSION | `REGRESSION \| existing tests fail \| các test class sẵn có bị fail` | `REGRESSION \| existing tests fail \| OrderServiceTest` |
| DISCOVERY | `DISCOVERY \| no tests executed \| -` | |

Loại lỗi javac được quy về danh sách cố định (cannot find symbol, incompatible types, unreported exception, method cannot be applied, package does not exist, …) để bỏ tên và giá trị cụ thể.

### 2.3. Generated-code hash — đoạn mã này đã thử chưa

`hash = SHA-256(mã test sau khi bỏ khoảng trắng thừa và comment)`. Hai đoạn mã giống hệt nhau có cùng hash. Comment được xác định qua AST (tree-sitter), nên chuỗi như `"http://…"` không bị cắt nhầm.

Dùng để nhận ra LLM **sinh lại y hệt đoạn mã đã thử**; khi đó không cần build lại.

### 2.4. Checkpoint — bản tốt nhất đến lúc này

Bản được giữ lại để quay về khi lần sửa mới làm tệ đi. Cách so sánh "tốt hơn" ở mục 3.2.

---

## 3. Ba quyết định

### 3.1. Strategy switching — đổi cách sửa

**Nguyên tắc:** không đổi LLM, mà đổi **nội dung prompt** đưa cho LLM. Mỗi loại lỗi (trạng thái ở mục 2.1, tức loại con trong ba nhóm) có **3 mức**.

| Sự kiện sau vòng t | Hành động |
|---|---|
| Loại lỗi **khác** vòng trước | Chuyển sang prompt của loại lỗi mới, bắt đầu ở **mức 1** |
| Error signature **trùng** vòng trước (lỗi lặp) | Giữ loại lỗi, **lên mức tiếp theo** (1 → 2 → 3) |
| Code hash trùng một bản đã thử | Không build, **lên mức tiếp theo** |
| VALID | Dừng, thành công |

*(Cập nhật 09/10/2026 — quyết định của nhóm)* Vì vòng nào cũng **sửa từ checkpoint**, "vòng trước" trong bảng trên được hiểu là **checkpoint** (mã đang đem đi sửa):
- Loại lỗi dùng để chọn prompt luôn là **trạng thái của checkpoint**, nên prompt luôn khớp với mã gửi đi.
- Bản mới **tốt hơn** checkpoint thì thành checkpoint. Nếu trạng thái đổi so với checkpoint cũ thì về **mức 1**; nếu cùng trạng thái (chỉ thêm method pass/assertion) thì giữ mức.
- Bản mới **tệ hơn** checkpoint (rollback) **hoặc** có signature **trùng** signature của checkpoint thì **lên mức**. Bản đó được ghi vào "cách đã thử và đã thất bại".
- Bản mới cùng mức tốt với checkpoint nhưng signature khác thì giữ mức.
- LLM trả về **không phải một test class hợp lệ** (sai class/package, không phải Java) thì xử lý như mã trùng: tính là một vòng, không build, lên mức.

**Nội dung từng mức:**

| Mức | Khi nào | Prompt |
|---|---|---|
| 1 | Lần đầu gặp loại lỗi | Thông báo lỗi + mã test + **source của focal class** *(cập nhật 09/10/2026)* |
| 2 | Lỗi lặp lại lần 1 | Mức 1 + **thông tin bổ sung đúng loại lỗi** + **"cách đã thử và đã thất bại — không lặp lại"** |
| 3 | Lỗi lặp lại lần 2 | **Như mức 2** (giữ thông tin bổ sung và danh sách đã thử, cập nhật 14/10/2026) nhưng chỉ **viết lại riêng test method bị lỗi**, giữ nguyên các method đã chạy tốt |

*(Cập nhật 14/10/2026, chốt Q4)* **Mọi mức** (và mọi vòng của Fixed) đều có **khối Repository Metadata M đầy đủ** (build tool, Java version, test framework, dependencies, cấu trúc dự án, source/test path). Vì vậy thông tin bổ sung mức 2 của COMPILE chỉ còn phần M không có: API thật của các class liên quan tới lỗi.

*(Cập nhật 14/10/2026, chốt Q5)* **Sau mức 3 là mức 2**, không đứng yên ở 3: `NextLevel(1) = 2`, `NextLevel(2) = 3`, `NextLevel(3) = 2`. Nếu giữ `min(level + 1, 3)`, khi checkpoint không đổi thì prompt mức 3 giống hệt từng chữ ở mọi vòng, model (temperature 0) trả lại cùng câu trả lời và các vòng còn lại đều là mã trùng (pilot cũ: 5 chain kẹt như vậy tới hết N). Danh sách đã thử thay đổi sau mỗi vòng nên prompt của vòng sau luôn khác.

*(Cập nhật 14/10/2026)* Mỗi lần trong **"cách đã thử và đã thất bại"** gồm: vòng, **signature** (thuật toán dùng để nhận ra lỗi lặp), **lỗi đầu tiên chưa chuẩn hóa** (tối đa 200 ký tự) và **các dòng mã đã đổi** so với checkpoint (`-`/`+`, tối đa 15 dòng). Prompt hiện đủ ba thông tin cho **3 lần gần nhất**, các lần cũ hơn chỉ một dòng vòng + signature. Mục đích: model biết đã thử cái gì để tránh lặp, kể cả bản đã bị rollback.

**Thông tin bổ sung ở mức 2 theo loại lỗi:**

| Loại lỗi | Thông tin bổ sung |
|---|---|
| COMPILE | Danh sách constructor và phương thức thật của CUT và các class liên quan; dependencies; Java version |
| DISCOVERY | Khung test class mẫu đúng test framework (annotation, import), package và tên file theo test path |
| RUNTIME | Mã của phương thức bị gọi; constructor và cách khởi tạo field; gợi ý cấu hình mock |
| ASSERTION | Giá trị expected và actual; mã của phương thức được kiểm tra |
| REGRESSION | Tên và lỗi của các test sẵn có bị fail; gợi ý về trạng thái dùng chung (biến static, file, cấu hình) |

**Vì sao không đưa hết thông tin ngay từ mức 1?** Prompt sẽ rất dài (tốn token, dễ gây nhiễu) trong khi phần lớn lỗi được sửa ngay ở mức 1. Chỉ khi lỗi lặp lại — tức đã có bằng chứng LLM thiếu thông tin — mới bổ sung.

**Vì sao mức 1 có source focal class?** *(cập nhật 09/10/2026)* Lúc sinh test, LLM đã thấy focal class. Nếu bỏ ra khi sửa, LLM gần như không thể sửa lỗi `cannot find symbol`. Khi đó chênh lệch Fixed/Adaptive có thể bị cho là do Fixed thiếu ngữ cảnh, chứ không phải do cơ chế. Mức 2 vẫn chỉ thêm thông tin riêng theo loại lỗi.

**Lưu ý mức 3:** nếu test method viết lại vẫn hỏng và bị bỏ, số test method giảm. **Phải báo cáo số method bị bỏ.**

*(Cập nhật 09/10/2026)* Cách triển khai mức 3:
- Áp dụng cho COMPILE (method chứa dòng lỗi biên dịch), RUNTIME và ASSERTION (method fail theo test report). Với DISCOVERY, REGRESSION, UNKNOWN, hoặc khi không xác định được method lỗi, dùng lại prompt **mức 2**; log ghi mức thực tế.
- LLM trả về cả class, nhưng ARROW **chỉ lấy các method bị lỗi** từ response và ghép vào checkpoint; các method khác giữ nguyên từng ký tự. Import, field và helper method mới trong response được giữ lại.
- Method bị lỗi mà response không còn thì bị bỏ khỏi test class. "Method bị bỏ" được tính là các test method có trong c0 nhưng không còn trong bản cuối.

### 3.2. Checkpoint và rollback — giữ bản tốt nhất, quay về khi tệ đi

**So sánh hai bản theo thứ tự** (chỉ xét tiêu chí sau khi tiêu chí trước bằng nhau):

| Thứ tự | Tiêu chí | Tốt hơn khi |
|---|---|---|
| 1 | Bước xác minh đạt được (mục 2.1) | Đi xa hơn |
| 2 | Số test method pass | Nhiều hơn |
| 3 | Số assertion (mục 5.2) | Nhiều hơn |
| 4 | Vòng xuất hiện | Sớm hơn |

Không dùng coverage hay mutation score để so sánh trong vòng sửa (tốn thời gian chạy).

*(Cập nhật 09/10/2026)* "Số test method pass" lấy từ test report của **lần chạy riêng test sinh ra**, không lấy từ lần chạy toàn module.

**Rollback:** nếu bản mới **tệ hơn checkpoint** → bỏ bản mới, vòng sau **sửa từ checkpoint** thay vì từ bản vừa sinh.
**Cập nhật checkpoint:** nếu bản mới **tốt hơn** checkpoint → bản mới trở thành checkpoint.

Ví dụ:
```
Vòng 1: RUNTIME    → checkpoint = vòng 1
Vòng 2: COMPILE    → tệ hơn checkpoint → ROLLBACK, vòng 3 sửa từ vòng 1
Vòng 3: ASSERTION  → tốt hơn → checkpoint = vòng 3
```

### 3.3. Stagnation detection — nhận ra khi không tiến triển

Hai dấu hiệu:

| Dấu hiệu | Phát hiện bằng | Xử lý |
|---|---|---|
| LLM sinh lại y hệt mã đã thử | Code hash trùng | Không build lại, tính là một vòng không tiến triển, lên mức strategy |
| LLM trả về thứ không dùng được *(cập nhật 09/10/2026)* | Không qua kiểm tra class/package | Như mã trùng: không build, một vòng không tiến triển, lên mức |
| Nhiều vòng liên tiếp không có bản nào tốt hơn checkpoint | Đếm số vòng không cải thiện | Dừng sớm sau P vòng, trả về checkpoint |

**Chọn P:** không chọn trước (xem mục 6.2).

---

## 4. Thuật toán

*(Cập nhật 14/10/2026 — khớp với `ARROW_Framework/src/Arrow.Service/Block3/RepairPolicies.cs`)*

```
Đầu vào: ứng viên thất bại c0 (đã xác minh), ngân sách N (mặc định 5; chọn lại bằng pilot, mục 6.1)
best ← c0 ; seen_hash ← {hash(c0)} ; level ← 1 ; đã_thử ← []

for t = 1 … N:
    type ← state(best)                            # prompt luôn theo lỗi của checkpoint
    ct   ← LLM_sửa(best, prompt(type, level, đã_thử))   # mức 3: chỉ ghép method lỗi vào best

    if ct không phải test class hợp lệ:           # sai class/package, không phải Java
        đã_thử += (t, "output không hợp lệ") ; level ← NextLevel(level) ; continue
    if hash(ct) ∈ seen_hash:                      # mã trùng
        đã_thử += (t, "mã trùng", dòng đã đổi) ; level ← NextLevel(level) ; continue
    seen_hash ← seen_hash ∪ {hash(ct)}

    build + chạy ct → state(ct), signature(ct)    # bản sao mới của snapshot; lỗi hạ tầng: 3 lần thử rồi INFRA

    if state(ct) = VALID:  return ct              # THÀNH CÔNG

    if tốt_hơn(ct, best):                         # cập nhật checkpoint
        if state(ct) ≠ state(best): level ← 1     # lỗi đổi loại
        best ← ct
    else:
        đã_thử += (t, signature(ct), lỗi đầu tiên, dòng đã đổi)   # cách đã thử và đã thất bại
        if tệ_hơn(ct, best) hoặc signature(ct) = signature(best):
            level ← NextLevel(level)              # rollback hoặc lỗi lặp → leo mức (3 → 2)
                                                  # (vòng sau vẫn sửa từ best)
return best                                       # hết N vòng, không đạt VALID
```

Khác bản 08/10: bản cũ đặt `type ← state(ct)` khi lỗi đổi loại kể cả lúc ct tệ hơn (rollback). Khi đó vòng sau sửa mã của checkpoint nhưng prompt lại mô tả lỗi của bản tệ, không còn trong mã được gửi. Nhóm chọn lấy loại lỗi theo checkpoint.

Gọi LLM bị lỗi tạm thời (quá tải, rate limit, lỗi mạng/5xx) được **gọi lại** tối đa 3 lần (chờ 20 s, 60 s, 120 s). Lần gọi lỗi chưa trả về code nên **không tính là một vòng sửa**; số lần gọi lại được ghi vào log. Vẫn lỗi thì dừng nhánh (hạ tầng) và chạy lại nhánh đó ở lượt sau.

Trong thực nghiệm, dừng sớm theo P **không bật**; P được tính lại từ log (mục 6.2).

---

## 5. Assertion failure

### 5.1. Quy tắc

- **LLM sửa tự do**, kể cả đổi giá trị expected theo actual. Không có rào chắn khi tính "sửa thành công".
- *(Tùy chọn, tạm thời không đưa vào Report1 — phương án A, 08/10/2026)* Nếu nhóm quyết định làm sau, ARROW có thể đếm cho từng nhánh Fixed và Adaptive: số lần đổi giá trị expected, số assertion bị xóa, số lần đổi loại assertion (ví dụ `assertNull` → `assertNotNull`), bằng so sánh AST giữa bản trước và sau mỗi lần sửa. Chưa cam kết trong báo cáo.

*(Tạm bỏ khỏi báo cáo, 08/10/2026)* Ghi chú hạn chế có thể dùng sau: tỷ lệ sửa thành công có thể bao gồm các test đã bị làm yếu assertion; chất lượng thật của test được phản ánh qua mutation score ở RQ3.

**Bối cảnh cần biết khi bị hỏi:**
- Đổi expected theo actual là **regression oracle** — giống cách EvoSuite sinh assertion từ hành vi quan sát được và template T3 của TestART. Phù hợp khi mã nguồn được giả định đúng, nhưng nếu mã có bug thì test vẫn pass và bug bị bỏ sót.
- Ví dụ minh họa của ChatUniTest (Hình 3) cho thấy một assertion failure phát hiện **bug thật** trong `StringUtils.equals` của Apache Commons.
- ChatTester chọn **không** sửa lỗi thực thi vì khó xác định thất bại do test hay do phương thức được kiểm thử.

### 5.2. Cách đếm assertion

| Được tính | Ví dụ |
|---|---|
| Assertion của JUnit 4/5, TestNG | `assertEquals`, `assertTrue`, `assertNull`, `assertThrows`, … |
| Thư viện assertion | `assertThat(...)` của AssertJ, Hamcrest |
| Kiểm tra exception kiểu JUnit 4 | `@Test(expected = X.class)`, `fail()` trong `try` |
| Kiểm tra lời gọi mock | `verify(mock).method(...)` của Mockito, cả `inOrder.verify(...)`; `verifyNoInteractions`, `verifyNoMoreInteractions`, `verifyZeroInteractions` *(bổ sung 15/10/2026)* |
| Kiểu BDD *(bổ sung 15/10/2026)* | `then(mock).should().save(o)` của Mockito, `then(total).isEqualTo(3)` của AssertJ (mỗi `then(x)` tính một) |
| Kiểm tra HTTP của Spring MockMvc *(bổ sung 15/10/2026)* | `.andExpect(status().isOk())` (mỗi `andExpect` tính một) |
| `ExpectedException` của JUnit 4 *(bổ sung 15/10/2026)* | `thrown.expect(X.class)`, `thrown.expectMessage("…")` |
| Kiểm tra trong chuỗi `then()`/`expect()` của RestAssured *(bổ sung 09/10/2026)* | `.then().statusCode(200).body(is("hi"))` — mỗi kiểm tra tính một assertion |

| Không được tính (assertion tầm thường) | Ví dụ |
|---|---|
| Luôn đúng | `assertTrue(true)`, `assertEquals(x, x)`, `assertNotNull(new Object())` |
| Từ nối không tham số *(bổ sung 09/10/2026)* | `.then().assertThat()` của RestAssured |
| Không phải assertion dù cùng tên *(bổ sung 15/10/2026)* | `signature.verify(bytes)` (method `verify` của class khác), `expect(mock.find()).andReturn(x)` của EasyMock (là stub) |

Đếm qua AST, không cần chạy test.

---

## 6. Tham số và cách biện minh

### 6.1. N = 5 (số vòng sửa tối đa)

| Công trình | Giới hạn | Có chứng minh không |
|---|---|---|
| ChatUniTest | 1 lần sinh + 5 lần sửa | Không — chỉ để các công cụ dùng cùng cấu hình |
| TestART | Tối đa 4 vòng | Không |
| ChatTester | Dừng sau 3 lần sửa không hiệu quả (ghi "e.g., 3") | Không |

Lập luận:
1. Cùng cấu hình đánh giá của ChatUniTest; nằm trong khoảng 3–5 của các công trình liên quan.
2. Fixed và Adaptive dùng **cùng N** — RQ2 so sánh hai cơ chế, không tìm N tối ưu.
3. Báo cáo **tỷ lệ thành công cộng dồn theo từng vòng** (vòng 1 → 5). Nếu đường cong đi ngang ở vòng 4–5 thì N = 5 là đủ.

*(Cập nhật 14/10/2026, chốt Q6)* Chỉ dựa vào tài liệu chưa đủ nên N được **kiểm tra bằng pilot**: pilot (`experiments/pilot-v1.json`, `repair.n = 10`, 50 CUT Classes2Test v3 + 9 CUT GitBug-Java, chia 6 máy theo `shards/pilot-6/`) chạy cả hai cơ chế tới 10 vòng; N = vòng k nhỏ nhất (5 ≤ k ≤ 10) mà cả vòng k−1 và vòng k không có thêm ứng viên sửa thành công trên tỷ lệ cộng dồn chung của hai cơ chế (chỉ tính Classes2Test); không vòng nào thỏa thì N = 10. Khối 4 của ARROW Framework tính quy tắc này (lớp `NRule`, kết quả in trong `report.md`). Pilot không xem kết quả so sánh hai cơ chế và không gộp vào kết quả chính.

### 6.2. P (dừng sớm)

- Thực nghiệm chạy **hết N = 5 vòng** cho Adaptive và ghi log từng vòng.
- Dừng sớm chỉ **cắt ngắn** cùng một chuỗi sửa; các vòng trước điểm dừng giống hệt nhau. Vì vậy tính lại **chính xác** từ log kết quả cho P = 1, 2, 3.
- Chọn P tiết kiệm nhiều nhất mà tỷ lệ thành công không giảm; báo cáo cả bảng như phân tích độ nhạy.
- Không tốn thêm lượt gọi LLM.

| Cấu hình | Tỷ lệ thành công | Số lần sửa TB | Thời gian TB |
|---|---|---|---|
| Không dừng sớm | (từ log) | (từ log) | (từ log) |
| P = 3 | | | |
| P = 2 | | | |
| P = 1 | | | |

---

## 7. Fixed Repair (đối chứng của RQ2)

| | Fixed Repair | Adaptive Repair |
|---|---|---|
| Prompt | Một prompt duy nhất ở mọi vòng: thông báo lỗi + mã test + source focal class (= mức 1 của Adaptive) | 3 mức theo loại lỗi, leo mức khi lỗi lặp |
| Sửa tiếp từ | Bản mới nhất (kể cả khi tệ hơn) | Checkpoint |
| Biết mình đã thử gì | Không | Có (signature, hash, "cách đã thử và thất bại") |
| Phát hiện mã trùng | Không | Có (code hash) |
| Dừng khi | VALID hoặc hết N | VALID hoặc hết N (P tính lại từ log) |
| Ngân sách N | 5 | 5 |
| Quy tắc assertion | LLM sửa tự do | Như Fixed |

Fixed **không** dùng hash, signature hay checkpoint, để mọi cơ chế dựa vào lịch sử là điểm khác biệt rõ ràng của Adaptive. **Cần kiểm tra code:** ghi chú cũ của nhóm nói paper ARROW liệt kê điều kiện dừng *"same code or error signature reappeared"* cho cả hai cơ chế — nếu code Fixed đang dừng khi mã lặp, phải sửa code hoặc sửa mô tả.

*(Cập nhật 09/10/2026 — đã kiểm tra code `run_fixed`)* Fixed **không** dừng khi mã hoặc signature lặp lại: mã trùng vẫn được build lại, chỉ dừng khi VALID hoặc hết N. Output không hợp lệ được tính là một vòng, bản mới nhất giữ nguyên. Mô tả trong paper phải sửa cho khớp: điều kiện "same code or error signature reappeared" chỉ là cơ chế của Adaptive (lên mức), không phải điều kiện dừng. Vì prompt Fixed bằng mức 1 của Adaptive, **vòng 1 của hai cơ chế dùng cùng một prompt**; khác biệt chỉ xuất hiện từ vòng 2.

---

## 8. Chỉ số của RQ2

**RQ2:** Does the Adaptive Repair mechanism improve the success rate of unit-test repair while reducing the number of repair attempts and repair time compared with a non-adaptive fixed repair mechanism?

| Chỉ số | Định nghĩa |
|---|---|
| Repair success rate | Tỷ lệ ứng viên thất bại đạt VALID trong tối đa N = 5 vòng |
| Number of repair attempts | Vòng đầu tiên đạt VALID; ứng viên không thành công tính bằng N |
| Repair time | Tổng thời gian gọi LLM + build + chạy test từ vòng 1 đến vòng thành công (hoặc hết N) |

**Điều kiện công bằng:** cùng tập ứng viên thất bại ban đầu, cùng mô hình, cùng trạng thái repository, cùng N, cùng quy tắc assertion.

*(Cập nhật 09/10/2026)* Cách đảm bảo khi chạy (`run_repair.py`):
- **Ứng viên thất bại ban đầu c0** là test do single-shot sinh ra mà chưa đạt VALID. c0 được dựng lại đúng như lúc sinh (validate, chuẩn hóa, sửa import) rồi build lại để lấy trạng thái.
- **Bị loại khỏi RQ2:** test bị loại ở single-shot vì output không hợp lệ (không có mã để sửa), baseline hỏng, hoặc lỗi pipeline. c0 build lại mà đạt VALID cũng bị loại.
- Mỗi cơ chế chạy trong **workspace sạch riêng**, copy từ cùng một bản repo, **cùng model và tham số** với lượt single-shot.
- **Thứ tự chạy xen kẽ** giữa các ứng viên (lẻ: Fixed trước, chẵn: Adaptive trước). Vòng 1 hai cơ chế cùng prompt, cơ chế chạy sau thường được nhà cung cấp trả lời từ cache nhanh hơn.
- Bảng RQ2 chỉ tính ứng viên mà **cả hai cơ chế đều chạy xong**. Nhánh dừng vì hạ tầng (build timeout, LLM không phản hồi sau khi gọi lại) được chạy lại ở lượt sau, không tính vào so sánh.

**Báo cáo kèm:**
- Tỷ lệ thành công cộng dồn theo vòng (biện minh N).
- Bảng độ nhạy P.
- *(Tùy chọn, chưa cam kết)* Số lần đổi expected / xóa / đổi loại assertion cho từng nhánh.
- Số test method bị bỏ ở mức 3.
- Thống kê mô tả các cơ chế: số lần rollback, mã trùng, lỗi lặp, đổi strategy.

---

## 9. Log cần ghi mỗi vòng

| Trường | Ví dụ |
|---|---|
| task_id, nhánh (fixed/adaptive), model, vòng t | `123_0`, `adaptive`, `gpt-6-luna`, `3` |
| Trạng thái | `RUNTIME` |
| Error signature | `RUNTIME \| NullPointerException \| OrderService.totalPrice` |
| Code hash | `sha256:…` |
| Mức strategy | `2` |
| Sửa từ bản nào | `checkpoint (vòng 1)` |
| Rollback? / Mã trùng? | `có` / `không` |
| Số test method pass, số assertion | `3`, `7` |
| *(Tùy chọn)* Thay đổi assertion so với bản trước | đổi expected: 1, xóa: 0, đổi loại: 0 |
| Thời gian LLM, thời gian build + chạy | `4,2 s`, `18,5 s` |
| Prompt và response đầy đủ | lưu file riêng (phục vụ tái lập) |

*(Cập nhật 09/10/2026)* Nơi lưu và các trường bổ sung:
- `runs/repair_exports/<mã lượt>/rounds.jsonl`: mỗi vòng một dòng, gồm các trường trên và thêm:
  - `requested_level` (mức theo thuật toán) và `level` (mức thực tế, vì mức 3 có thể lùi về mức 2);
  - `improved`, `invalid_output`;
  - `level3_replaced` / `level3_dropped` (method được viết lại hoặc bị bỏ ở mức 3);
  - `llm_retries` (số lần gọi lại LLM khi lỗi tạm thời), `llm_tokens`;
  - `attempt_id` (lần chạy của nhánh; nhánh chạy lại sau lỗi hạ tầng có `attempt_id` mới).
- `candidates.jsonl`: mỗi ứng viên × cơ chế một dòng. Gồm:
  - trạng thái c0 và trạng thái cuối, vòng thành công, thời gian, method bị bỏ;
  - bộ đếm cơ chế: rollback, mã trùng, output không hợp lệ, lỗi lặp, lên mức, đổi loại lỗi, số vòng mức 3;
  - `run_order` (cơ chế này chạy trước hay sau).
- `runs/rp/<mã lượt>/<sample>/<kỹ thuật>/<F|A>/r<t>/`: `prompt.txt`, `response.txt`, `candidate.java`, `build_output.txt` (chạy riêng test sinh ra + chạy toàn module).

---

## 10. Phạm vi chứng minh

- Adaptive Repair được đánh giá **như một tổng thể** so với Fixed Repair. **Không** làm ablation từng thành phần.
- Khi viết kết quả: **không** quy hiệu quả cho từng thành phần riêng lẻ ("nhờ rollback…", "error signature giúp…").
- Thống kê số lần mỗi cơ chế được kích hoạt chỉ là **mô tả**, không phải chứng minh nhân quả.
- Thực nghiệm chạy **một lần** cho mỗi cấu hình; kết quả có thể khác khi lặp lại do LLM không tất định.
- Hướng phát triển: ablation (checkpoint, rollback, error signature), đo tính ổn định qua nhiều lần chạy.

---

## 11. Câu hỏi hội đồng thường gặp

| Câu hỏi | Gợi ý trả lời |
|---|---|
| Adaptive khác Fixed ở đâu? | Fixed dùng một prompt và luôn sửa từ bản mới nhất. Adaptive biết lỗi đổi loại hay lặp lại để đổi prompt, giữ checkpoint để rollback, và dùng code hash để không build lại mã trùng. |
| Khác TestART ở đâu? | TestART chọn template theo loại lỗi và đưa vào ví dụ đúng (positive prompt injection), chọn test tốt nhất ở cuối. ARROW leo mức prompt khi error signature lặp lại, đưa vào cách đã thử và thất bại, có checkpoint và rollback trong quá trình sửa. |
| Strategy switching đổi khi nào? | Khi lỗi chuyển sang loại khác (đổi bảng prompt) và khi cùng một dạng lỗi lặp lại (lên mức bổ sung thông tin, rồi viết lại method). |
| Vì sao chia ba nhóm lỗi? | Ba nhóm build, runtime, assertion cho biết test hỏng trước khi chạy, khi chạy hay ở kết quả; trong mỗi nhóm, từng trạng thái có prompt và thông tin bổ sung riêng. Việc phân loại là bước nền (TestART cũng phân loại tương tự); điểm khác của ARROW là cách dùng lịch sử sửa. |
| Rollback khi nào? | Khi lần sửa mới cho ra bản đi lùi trong chuỗi xác minh so với checkpoint; vòng sau sửa từ checkpoint. |
| Nếu mọi lần sửa đều tệ hơn? | Luôn sửa từ checkpoint nên không mất bản tốt nhất; hết N vòng thì trả về checkpoint. |
| Vì sao N = …? | Tài liệu (ChatUniTest 5, TestART 4, ChatTester 3) không chứng minh con số nào, nên N được chọn bằng pilot: chạy hai cơ chế tới 10 vòng, N là vòng k nhỏ nhất (5 ≤ k ≤ 10) mà cả vòng k−1 và k không có thêm ứng viên sửa thành công trên tỷ lệ cộng dồn chung (mục 6.1); biểu đồ cộng dồn của pilot cho thấy … |
| Vì sao P = …? | Không chọn trước; tính từ log cho P = 1, 2, 3 và chọn giá trị cân bằng tốt nhất giữa chi phí và tỷ lệ thành công. |
| Dừng sớm có làm mất cơ hội thành công không? | Có thể — RQ2 báo cáo cả tỷ lệ thành công, số lần sửa và thời gian để thấy đánh đổi; bảng độ nhạy P cho thấy mức ảnh hưởng. |
| Để LLM tự do sửa assertion có thổi phồng tỷ lệ thành công không? | Có thể. Chất lượng thật của test hợp lệ được đánh giá bằng mutation score ở RQ3. *(Theo quyết định 08/10/2026, tạm không đưa ghi chú hạn chế này vào báo cáo; xem mục 5.1.)* |
| Thành phần nào đóng góp nhiều nhất? | Nghiên cứu đánh giá Adaptive Repair như một tổng thể; tách riêng từng thành phần (ablation) là hướng phát triển đã nêu. |

---

## 12. Việc cần làm trước khi viết Methodology

1. ~~Đối chiếu toàn bộ tài liệu này với code repair~~ — **xong 09/10/2026**. Các chỗ làm rõ/thay đổi đã đánh dấu trong tài liệu và tổng hợp ở mục 13.
2. ~~Xác nhận code ghi đủ log ở mục 9~~ — **xong 09/10/2026**.
3. ~~Viết hàm đếm assertion qua AST~~ — **xong 09/10/2026**; từ 14/10/2026 là lệnh `count_assertions` của java-helper (JavaParser) trong ARROW Framework. Việc phát hiện đổi expected / xóa / đổi loại assertion vẫn là tùy chọn, chưa làm.
4. Sau thực nghiệm: vẽ biểu đồ cộng dồn theo vòng, tính bảng độ nhạy P, tổng hợp thống kê assertion và mức 3. Khối 4 của ARROW Framework (`arrow report`) tạo sẵn các bảng (CSV); **còn chờ số liệu pilot/thực nghiệm**.
5. *(Mới)* Paper/Report 4: sửa mô tả điều kiện dừng của Fixed (mục 7) và nêu rõ chỉ số "pass cả module" đã đổi cách đo từ 09/10/2026 (mục 2.1).

---

## 13. Lịch sử thay đổi so với bản 08/10/2026

Ngày 09/10/2026, khi triển khai và chạy thử (smoke với Qwen2.5-Coder 7B):

| # | Mục | Thay đổi / làm rõ | Lý do |
|---|---|---|---|
| 1 | 3.1, 7 | Prompt mức 1 (và prompt duy nhất của Fixed) có thêm **source focal class** | Không có nó LLM gần như không sửa được lỗi biên dịch; tránh chênh lệch Fixed/Adaptive do thiếu ngữ cảnh |
| 2 | 3.1, 4 | Loại lỗi để chọn prompt lấy theo **checkpoint**; bản tệ hơn chỉ ghi vào "đã thử và thất bại" và làm lên mức | Bản cũ có thể mô tả lỗi không còn trong mã được gửi đi |
| 3 | 3.1, 3.3 | Output không hợp lệ xử lý như mã trùng (một vòng, không build, lên mức) | Bản cũ chưa quy định |
| 4 | 3.1 | Mức 3 chỉ áp dụng khi xác định được method lỗi (COMPILE/RUNTIME/ASSERTION); còn lại dùng mức 2. ARROW tự ghép method vào checkpoint | Bản cũ chưa quy định cách thực hiện |
| 5 | 2.1 | Thêm UNKNOWN; timeout/lỗi công cụ là lỗi hạ tầng (build lại 1 lần, vẫn lỗi thì loại khỏi RQ2) | Bản cũ chưa quy định |
| 6 | 2.1 | Sửa phân loại: `AssertionError:` không còn bị tính là lỗi biên dịch | Lỗi phân loại cũ làm ASSERTION thành COMPILE, sai checkpoint/rollback |
| 7 | 2.1 | Lần chạy toàn module chạy cả test sinh ra (`surefire.includesFile`) | Trước đây Maven bỏ qua test sinh ra nên không bao giờ phát hiện REGRESSION; **ảnh hưởng chỉ số Valid của RQ1** |
| 8 | 5.2 | Đếm kiểm tra RestAssured trong `then()`/`expect()`; bỏ `assertThat()` không tham số | Test REST (Quarkus, Spring) dùng kiểu này; trước đây đếm ra 0 |
| 9 | 3.2 | Số method pass lấy từ lần chạy riêng test sinh ra | Lần chạy toàn module có thể không có report của test sinh ra |
| 10 | 4 | LLM lỗi tạm thời được gọi lại tối đa 3 lần, không tính là vòng sửa | Model miễn phí/quá tải làm hỏng nhánh giữa chừng |
| 11 | 8 | c0 lấy từ single-shot; thứ tự Fixed/Adaptive xen kẽ; chỉ so sánh ứng viên cả hai cơ chế chạy xong | Bảo đảm điều kiện công bằng; vòng 1 cùng prompt nên cơ chế chạy sau được cache |
| 12 | 2.1, 3.1, 11 | Gom các trạng thái lỗi thành ba nhóm build (COMPILE, UNKNOWN, DISCOVERY), runtime (RUNTIME, REGRESSION), assertion (ASSERTION); prompt vẫn chọn theo trạng thái | Giữ đúng mô tả ban đầu của Đóng góp 2 ("biên dịch, runtime, assertion") mà không đổi code |
| 13 | 11 | Câu trả lời về việc LLM tự do sửa assertion không còn nói "nêu ở phần hạn chế" | Khớp quyết định 08/10/2026 tạm không đưa ghi chú này vào báo cáo (mục 5.1) |
| 14 | 3.1, 4 | *(14/10/2026, Q5)* `NextLevel(3) = 2`: sau mức 3 không tiến triển thì về mức 2 | Pilot cũ: 5 chain kẹt ở mức 3 với prompt giống hệt, mọi vòng còn lại là mã trùng |
| 15 | 3.1 | *(14/10/2026)* Mức 3 giữ thông tin bổ sung và danh sách đã thử của mức 2; mỗi lần đã thử có thêm lỗi đầu tiên chưa chuẩn hóa và các dòng mã đã đổi (3 lần gần nhất) | Xoay mức 2 ↔ 3 mà bỏ thông tin ở mức 3 thì mất đúng thông tin vừa đưa ra; model cần biết đã thử gì |
| 16 | 3.1, 7 | *(14/10/2026, Q4)* Mọi prompt sửa (mọi mức, cả Fixed) có khối Repository Metadata M đầy đủ | RQ2 công bằng: hai cơ chế nhận cùng thông tin cấu hình |
| 17 | 2.1 | *(14/10/2026, Q8)* Lỗi hạ tầng: 3 lần thử rồi INFRA, vào danh sách "Cần chạy lại riêng" kèm lệnh; không loại hẳn khỏi RQ2 | Timeout thường do máy, chạy lại riêng task đó |
| 18 | 6.1 | *(14/10/2026, Q6)* N chọn bằng pilot theo quy tắc 5 ≤ k ≤ 10 của CLAUDE.md, pilot chạy `n = 10` | Tài liệu chưa đủ để chọn N |
| 19 | đầu tài liệu | *(14/10/2026)* Code chuyển sang ARROW Framework (C#); `src/repair_v2/` và `pilot_tool` không còn dùng; spec chuẩn là `ARROW_Framework/specs/03-adaptive-repair.md` | Viết lại framework theo 4 khối |

Quan sát khi chạy thử (chưa phải số liệu):
- Qwen 7B thường trả lại **y nguyên** test cũ: Adaptive phát hiện mã trùng và bỏ qua build.

---

## Nguồn tham chiếu (đã đối chiếu toàn văn)

- **ChatUniTest** — Chen et al., FSE Companion 2024: validation cú pháp → biên dịch → runtime; sửa theo luật rồi LLM; đánh giá với 1 lần sinh + 5 lần sửa; Hình 3 là assertion failure phát hiện bug thật.
- **ChatTester** — Yuan et al., FSE 2024: chỉ sửa lỗi biên dịch; dừng khi số lần sửa không làm giảm lỗi vượt ngưỡng (e.g., 3); không sửa lỗi thực thi vì khó xác định nguyên nhân.
- **TestART** — Gu et al., arXiv 2408.03095 / TOSEM: template theo lỗi biên dịch, runtime, assertion (T2 đảo `assertNull`/`assertNotNull`, T3 thay expected bằng actual); positive prompt injection; tối đa 4 vòng; chọn test tốt nhất theo execution success, coverage, số test.
- **TestGen-LLM** — Alshahwan et al., FSE Companion 2024: lọc build → pass ổn định qua 5 lần chạy → tăng coverage.
- **Ouyang et al.**, TOSEM 2025: LLM không tất định ngay cả khi temperature = 0.
