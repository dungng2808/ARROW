# ARROW Project Rules

## Tổng quan

Chúng tôi đề xuất ARROW (An Adaptive Repository-Aware Repair Workflow for LLM-Based Java Unit Test Generation) với ba đóng góp chính.

---

## Đóng góp 1: Repository-Aware Prompting Strategy

**Quy trình repository-aware prompting** (zero-shot-project-aware) phân tích và tích hợp các thông tin cấu hình dự án vào prompt để sinh kiểm thử phù hợp với repository đích. Cụ thể, ARROW trích xuất và sử dụng 6 thông tin:

- **Build Tool**: Maven hoặc Gradle
- **Java Version**: Phiên bản JDK cụ thể của dự án
- **Dependencies**: Các thư viện phụ thuộc
- **Test Framework**: JUnit 4, JUnit 5, TestNG...
- **Project Structure**: Cấu trúc thư mục của dự án
- **Source / Test Paths**: Đường dẫn chứa mã nguồn và test file

**Source / Test Paths**: source path là nơi chứa CUT; **test path là thư mục đích mà test do LLM sinh ra được ghi vào** (ví dụ CUT `src/main/java/com/example/OrderService.java` thì test ghi vào `src/test/java/com/example/OrderServiceTest.java`). Test path được đưa vào prompt để LLM khai báo đúng package và tên class, và được ARROW dùng để ghi file test vào đúng module trước khi build. Test path **không** dùng để đọc test cũ gửi cho LLM. ARROW **không** phân tích Existing Test Style (naming convention, cấu trúc AAA, assertion style); ý này đã bị bỏ vì chưa có cách chứng minh, không đưa lại vào báo cáo.

Thông tin này được đưa vào prompt xuyên suốt các bước sinh (generation), xác minh (verification) và sửa (repair) kiểm thử, giúp LLM tạo mã test tương thích với cấu hình thực tế của repository.

---

## Đóng góp 2: Adaptive Repair Mechanism

**Cơ chế Adaptive Repair** là cơ chế sửa kiểm thử thích ứng, sử dụng thông tin về trạng thái và lịch sử của ứng viên để điều khiển quá trình sửa một cách hiệu quả. ARROW theo dõi:

- **Failure state classification**: Phân loại lỗi thành **3 nhóm**: build (COMPILE, UNKNOWN, DISCOVERY), runtime (RUNTIME, REGRESSION) và assertion (ASSERTION). Prompt và mức strategy chọn theo trạng thái (loại con) như code `src/repair_v2`; nhóm dùng để mô tả và báo cáo. Việc phân loại là bước nền, không phải điểm mới (chốt 09/10/2026)
- **Normalized error signature**: Chữ ký lỗi đã được chuẩn hóa
- **Generated-code hash**: Mã hash của mã đã sinh để phát hiện mã lặp
- **Checkpoint**: Lưu các phiên bản tốt nhất

Dựa trên thông tin này, ARROW quyết định:

- **Strategy switching**: Chuyển đổi chiến lược sửa khi gặp loại lỗi khác
- **Rollback**: Khôi phục phiên bản tốt hơn khi lần sửa mới gây regression
- **Stagnation detection**: Phát hiện khi quá trình sửa không tiến triển

So với Fixed Repair (dùng cùng một prompt sửa ở mọi vòng), Adaptive Repair giúp giảm số vòng sửa không hiệu quả và tránh mất các phiên bản có kết quả tốt hơn.

**Phạm vi chứng minh (chốt 08/10/2026):**
- Adaptive Repair được đánh giá **như một tổng thể** so với Fixed Repair (RQ2). **Không** làm ablation từng thành phần; ablation (checkpoint, rollback, error signature) và đo tính ổn định qua nhiều lần chạy là **hướng phát triển**, ghi ở phần hạn chế và Mục 6.
- Hệ quả khi viết báo cáo: **không** quy hiệu quả cho từng thành phần riêng lẻ (không viết "nhờ rollback…", "error signature giúp…").
- Nên ghi log số lần mỗi cơ chế được kích hoạt (rollback, mã trùng theo hash, lỗi lặp theo signature, đổi strategy, dừng sớm) để báo cáo mô tả, không phải chứng minh nhân quả.
- Thực nghiệm chạy **một lần** cho mỗi cấu hình. Không dùng lý do "giá rẻ cho phép lặp lại thực nghiệm" khi giải thích việc chọn GPT-6 Luna.
**Thiết kế chi tiết (chốt 08/10/2026, cần đối chiếu với code repair khi có).** Bản đầy đủ (thuật toán, ví dụ, log, câu hỏi hội đồng): `ARROW/Adaptive repair.md`.

| # | Hạng mục | Quyết định | Cách biện minh |
|---|----------|------------|----------------|
| 1 | Trạng thái | **Chuỗi xác minh** COMPILE → UNKNOWN → DISCOVERY → RUNTIME → ASSERTION → REGRESSION → VALID, dùng để so sánh checkpoint (bản đi xa hơn là bản tốt hơn); timeout và lỗi công cụ là lỗi hạ tầng, không thuộc chuỗi. **Ba nhóm lỗi**: BUILD (COMPILE, UNKNOWN, DISCOVERY), RUNTIME (RUNTIME, REGRESSION), ASSERTION (ASSERTION); thứ tự nhóm không dùng để so sánh checkpoint (cập nhật 09/10/2026) | Chuỗi xác minh theo Đóng góp 3; ba nhóm giữ đúng mô tả ban đầu của Đóng góp 2 |
| 2 | Số vòng sửa tối đa N | **N mặc định = 5**, giống nhau cho Fixed và Adaptive. **Pilot** (20 class Classes2Test và 10 class GitBug-Java thuộc 10 repository khác nhau, đều ngoài tập đánh giá) gồm hai phần: phần 1 (Giai đoạn 2) kiểm tra môi trường, công cụ, chi phí; phần 2 (cuối Giai đoạn 3, trước RQ2) chạy hai cơ chế tới 10 vòng trên ứng viên thất bại của Classes2Test; N = vòng k nhỏ nhất (5 ≤ k ≤ 10) mà cả vòng k−1 và vòng k không có thêm ứng viên sửa thành công trên tỷ lệ cộng dồn chung của hai cơ chế; không có vòng nào thỏa thì N = 10. Pilot không xem kết quả so sánh hai cơ chế, không gộp vào kết quả chính (chốt 09/10/2026) | Chỉ dựa vào tài liệu (ChatUniTest 5, TestART 4, ChatTester dừng sau 3 lần sửa không hiệu quả) chưa đủ thuyết phục nên N được kiểm tra bằng pilot; ChatUniTest **không** chứng minh vì sao chọn 5 |
| 3 | Fixed Repair | Một prompt duy nhất ở mọi vòng (lỗi + mã test + source focal class, bằng mức 1 của Adaptive), luôn sửa tiếp từ bản mới nhất, **chỉ dừng khi VALID hoặc hết N**; không dùng hash, signature, checkpoint; mã trùng vẫn build lại | Để mọi cơ chế dùng lịch sử là điểm khác biệt rõ ràng của Adaptive |
| 4 | Strategy switching | Mỗi trạng thái lỗi có **3 mức**; prompt luôn theo lỗi của **checkpoint** (bản đang được sửa). Bản mới **tốt hơn** checkpoint → thành checkpoint; trạng thái đổi → mức 1, cùng trạng thái → giữ mức. Bản mới **tệ hơn** (rollback) hoặc **trùng signature** với checkpoint → lên mức; cùng mức tốt nhưng signature khác → giữ mức. Mã trùng hoặc output không hợp lệ → tính một vòng, không build, lên mức. Mức 1: thông báo lỗi + mã test + source focal class. Mức 2: thêm thông tin đúng trạng thái (COMPILE: API thật của CUT và class liên quan, dependencies, Java version; DISCOVERY: khung test class đúng framework và test path; RUNTIME/ASSERTION: mã phương thức bị gọi, cách khởi tạo; REGRESSION: test sẵn có bị fail, trạng thái dùng chung) + **"cách đã thử và thất bại"**. Mức 3: chỉ cho COMPILE, RUNTIME, ASSERTION khi xác định được method lỗi, ARROW ghép method viết lại vào checkpoint; trường hợp khác dùng mức 2 (cập nhật 09/10/2026, khớp code `src/repair_v2`) | Khác TestART: TestART chọn template theo loại lỗi và dùng positive prompt injection (ví dụ đúng); ARROW leo mức khi lỗi lặp và đưa ví dụ đã thất bại. Mức 3 có thể làm giảm số test method → phải báo cáo số method bị bỏ |
| 5 | Checkpoint | So sánh theo thứ tự: bước xác minh đạt được → số test method pass → số assertion → bản xuất hiện sớm hơn. Không dùng coverage hay mutation score | Rẻ, chỉ cần kết quả chạy và AST |
| 6 | Rollback | Bản mới **tệ hơn** checkpoint → bỏ bản mới, vòng sau sửa từ checkpoint | |
| 7 | Code hash | Mã trùng một bản đã thử → không build lại, tính là một vòng không tiến triển, lên mức strategy tiếp theo | |
| 8 | Dừng sớm P | **Không chọn trước.** Thực nghiệm chạy hết N vòng và ghi log từng vòng; vì dừng sớm chỉ cắt ngắn chuỗi sửa, tính lại kết quả cho P = 1, 2, 3 từ log và chọn theo dữ liệu (phân tích độ nhạy) | Không tốn thêm lượt gọi LLM; tránh phải biện minh một con số chọn tùy ý |
| 9 | Assertion failure | **Để LLM sửa tự do** (được đổi expected, không có rào chắn khi tính "sửa thành công") | Quyết định của nhóm. Tạm thời (08/10/2026) không đưa vào báo cáo việc đếm thay đổi assertion hay ghi chú hạn chế liên quan |
| 10 | Đếm assertion | Tính `assert*` của JUnit/TestNG, `assertThat` (AssertJ/Hamcrest), `@Test(expected=…)`, `fail()` trong try, và `verify()` của Mockito; **không** tính assertion tầm thường (`assertTrue(true)`, `assertEquals(x, x)`) | Đếm qua AST, không cần chạy test |
| 11 | Log cần ghi | Mỗi vòng: trạng thái, error signature, code hash, mức strategy, có rollback không, thời gian LLM và build; tổng hợp số lần rollback, mã trùng, lỗi lặp, đổi strategy, dừng sớm (tính lại) | Phục vụ biểu đồ cộng dồn, phân tích P và thống kê mô tả các cơ chế |

---

## Đóng góp 3: Multi-Dimensional Evaluation Framework

**Khung đánh giá đa chiều** cung cấp cái nhìn toàn diện về chất lượng kiểm thử được sinh. ARROW đánh giá trên nhiều khía cạnh:

### Verification Metrics (Tính hợp lệ)

Báo cáo chi tiết từng bước xác minh:
- **Compilation success rate**: Tỷ lệ biên dịch thành công
- **Test discovery rate**: Tỷ lệ được test runner phát hiện
- **Execution success rate**: Tỷ lệ thực thi thành công
- **Target test pass rate**: Tỷ lệ test mục tiêu pass
- **Module regression**: Không gây regression cho bộ test hiện có

### Quality Metrics (Chất lượng kiểm thử)

Đo lường trên tập test hợp lệ:
- **Code coverage**: Mức độ bao phủ mã (line, branch, method)
- **Mutation score**: Khả năng phát hiện lỗi qua kiểm thử đột biến
- **Test smell density**: Mật độ test smell trong bộ kiểm thử

### Repair Effectiveness (Hiệu quả sửa)

So sánh Adaptive Repair với Fixed Repair:
- **Repair success rate**: Tỷ lệ sửa thành công
- **Number of repair rounds**: Số vòng sửa cần thiết
- **Repair time**: Thời gian sửa

---

## Câu hỏi nghiên cứu (RQ)

Nguồn: Report 2 (Research Proposal and Plan), trang 10, bản 07/10/2026. Khi viết báo cáo, dùng đúng nội dung dưới đây.

| RQ | Bản tiếng Anh (giữ nguyên) | Cách viết tiếng Việt trong báo cáo | Gắn với |
|----|----------------------------|-------------------------------------|---------|
| **RQ1** | Does repository-aware prompt construction improve the compilation and execution success of generated unit tests compared with conventional prompting strategies? | Việc xây dựng prompt dựa trên cấu hình repository có cải thiện khả năng biên dịch và thực thi thành công của unit test được sinh so với các quy trình prompting thông thường hay không? | Đóng góp 1; chỉ số chính: compilation success rate, execution success rate |
| **RQ2** | Does the Adaptive Repair mechanism improve the success rate of unit-test repair while reducing the number of repair attempts and repair time compared with a non-adaptive fixed repair mechanism? | Cơ chế Adaptive Repair có cải thiện tỷ lệ sửa unit test thành công, đồng thời giảm số lần sửa và thời gian sửa so với cơ chế Fixed Repair không thích ứng hay không? | Đóng góp 2; chỉ số: repair success rate, number of repair attempts, repair time |
| **RQ3** | How does the quality of valid Java unit-test suites generated by ARROW compare with that of suites generated by other automated test-generation systems, including EvoSuite, in terms of code coverage, mutation score, and test-smell density? | Chất lượng của các bộ unit test Java hợp lệ do ARROW sinh ra so với các bộ test do những hệ thống sinh test tự động khác, bao gồm EvoSuite, như thế nào xét theo code coverage, mutation score và test smell density? | Đóng góp 3; chỉ đo trên test hợp lệ |

- RQ1 so sánh với "các quy trình prompting thông thường" (không viết "chiến lược prompting" trong văn bản tiếng Việt).
- RQ3 so sánh với **các hệ thống sinh test tự động khác, gồm EvoSuite**; không còn ghi so sánh với test do lập trình viên viết như bản RQ cũ.
- "Sửa thành công" của RQ2 = ứng viên đạt VALID trong tối đa N = 5 vòng.

---

## Cấu hình thực nghiệm (chốt ngày 07/10/2026)

### Model

| Vai trò | Model | Cấu hình |
|---------|-------|----------|
| Model thương mại qua API | **GPT-6 Luna** (snapshot `gpt-6-luna`) | `reasoning_effort: none`; gọi qua LiteLLM |
| Model trọng số mở tự host | **Qwen3-Coder-30B-A3B-Instruct** | Chỉ có non-thinking mode; phục vụ qua Ollama, gọi qua LiteLLM |

- **Không fine-tune** cả hai model (đã bỏ hẳn). Lý do: cô lập tác động của ARROW, đúng tinh thần zero-shot. Không viết lại nội dung fine-tune vào báo cáo.
- Không dùng nữa: GPT-4, Qwen2.5-Coder-32B, GPT-5.x.
- Lý do chọn và trích dẫn: GPT-6 Luna dựa trên guideline của Baltes et al. (cần model mở làm đối chứng), chi phí thấp phù hợp với số lượng lớn lượt gọi trong các vòng sinh và sửa (không dùng lý do "cho phép lặp lại thực nghiệm" vì chỉ chạy một lần; Ouyang et al. TOSEM 2025 chỉ trích ở phần độ tin cậy và hạn chế), Haroon et al. 2026 (đã có tiền lệ đánh giá model hạng tiết kiệm khi sinh test Java). Qwen3-Coder-30B dựa trên Wang, Briand et al. 2026 (sinh unit test Java qua Ollama) và Wirdemann, Borg et al. 2026 (JUnit 5, mutation score); kiến trúc MoE 30,5B tổng, 3,3B kích hoạt; giấy phép Apache 2.0. Hai model đối xứng: đều không có reasoning ẩn.
- Phải nêu thẳng trong báo cáo: GPT-6 Luna chưa có paper nào dùng trực tiếp; model card Qwen3-Coder-30B **có** bảng benchmark chính thức dạng hình ảnh ("Qwen3-Coder-Flash"): SWE-bench Verified 51,6% (OpenHands, 500 lượt), Aider-Polyglot 33,3%, SWE-bench Multilingual 34,7%, Terminal-Bench 31,3%; các paper tiền lệ năm 2026 là preprint arXiv.
- Chi tiết đầy đủ: Mục 4.2 và Bảng 2 trong `Report1_Sections2_3_Revised_VI.md`.

### Dataset

- **Tập chính: Classes2Test** (Lops et al., 9.410 repository GitHub, ghép cặp class Java với test do người viết). Tập đánh giá gồm **200 class từ 200 repository khác nhau** (mỗi repository một class). Class không dùng được được thay bằng class rút ngẫu nhiên từ pool đã qua preflight theo cùng quy tắc, kèm phiên bản manifest mới; **không** dùng 50 mẫu dự phòng chọn sẵn (bỏ ngày 09/10/2026).
- **Revision của Classes2Test dùng content-matched (chốt 09/10/2026).** Classes2Test không lưu commit SHA nên preflight tìm trong lịch sử repository commit có phần thân focal method và test case khớp với bản ghi (sau khi bỏ chú thích, chuẩn hóa khoảng trắng) và gắn `CONTENT_MATCHED`. Pool sau preflight: 6.346 class qua kiểm tra kỹ thuật, trong đó **6.342 class content-matched từ 1.324 repository** (pool rút mẫu), 4 class `UNVERIFIED` bị loại, 0 class `UPSTREAM_PINNED`. Mẫu vẫn phải build được ở đúng module và qua compile probe.
- Đây là **sai lệch đã khai báo** so với chính sách strict của `ARROW/preflight.md` (strict chỉ nhận `UPSTREAM_PINNED`). Khi viết báo cáo: commit content-matched tái lập được nhưng **không bảo đảm** là đúng commit tác giả dataset đã dùng, các file khác (cấu hình build, dependencies) có thể khác; kết quả Classes2Test gắn nhãn content-matched, báo cáo **tách riêng** với GitBug-Java (`UPSTREAM_PINNED`), không trình bày như upstream-pinned. Không viết "chỉ mẫu UPSTREAM_PINNED được đưa vào số liệu".
- Lý do chọn Classes2Test: dữ liệu ở cấp class; repository đa dạng về build tool, Java version và test framework, là điều kiện để đánh giá Đóng góp 1; có test sẵn để kiểm tra module regression; AgoneTest đã dùng với cùng hướng đánh giá coverage, mutation và test smell. Không dùng Defects4J làm tập chính vì chỉ khoảng 17 project chạy qua cùng một bộ công cụ build.
- **Tập bổ sung: GitBug-Java** (Silva, Saavedra, Monperrus, MSR 2024): 199 bug-fix từ 55 repository Java dùng Maven/Gradle, commit năm 2023, mỗi mẫu có cặp commit buggy/fixed và môi trường tái lập qua GitHub Actions. Phải chuẩn hóa sang định dạng JSON của Classes2Test cộng `revision_map.csv` (`UPSTREAM_PINNED`, provenance `dataset_record`) để preflight dùng lại không cần sửa: checkout `commit_hash` (bản đã fix); CUT là file `.java` trong `src/main/java` của `bug_patch`; test tham chiếu ghép theo tên (`<Tên>Test`, `Test<Tên>`, `<Tên>Tests`). **Không** dùng test fail làm test tham chiếu, vì dữ liệu thật cho thấy test fail thường thuộc class khác (ví dụ CUT `Language.java`, test fail `WhatsappBusinessCloudApiTest`). Mỗi repository một class. Đã đếm lại ngày 09/10/2026 trên nhánh chính của `gitbugactions/gitbug-java`: `data/bugs/` có **55 file, mỗi file một repository, tổng 199 bug**, khớp với paper (con số 70 file ghi trước đây là sai). Phân bố lệch: `traccar-traccar` có 81 bug, 33 repository chỉ có 1 bug. **Bắt buộc có test ghép (chốt 11/10/2026):** CUT không ghép được test người viết bị loại (`NO_PAIRED_TEST`), để mọi CUT GitBug-Java có test tham chiếu cho RQ3 (test người viết vẫn chỉ là tham chiếu mô tả, không phải đối tượng so sánh). Kết quả chuẩn hóa (`preflight_tool/scripts/normalize_gitbug.py` → `ARROW/gitbug_input/`): 263 file CUT ứng viên, loại 9 file ngoài `src/main/java` (`aws/event-ruler`, `iipc/jwarc`) và 83 file không có test ghép; còn **180 CUT từ 156 bug, 35 repository** trước preflight; 14 trong 35 repository có ≥ 2 bug (nguồn cho pilot). Không sửa preflight.
- Lý do chọn GitBug-Java: commit đã khóa sẵn và tái lập được (bù cho việc Classes2Test thiếu commit SHA); nguồn repository khác Classes2Test nên kiểm tra được khả năng khái quát. **Không** dùng lý do "giảm data leakage", vì commit năm 2023 nằm trước thời điểm huấn luyện của cả hai model.
- Không dùng SF110: build bằng Ant và phân phối dạng file nén, không có git; ARROW chỉ hỗ trợ Maven/Gradle.
- Báo cáo đang viết: `draft/Report1.md` (template `1_SET490/Report1_Project Introduction.docx`, đã đưa vào `DOCX/Report1_Project Introduction.docx`) và `draft/Report4.md` (template `1_SET490/Report4_Methodology.docx`, đã đưa vào `DOCX/Report4_Methodology.docx`). Bản `.md` là nguồn chính; sửa `.md` trước rồi mới cập nhật `.docx`.

---

## Format viết báo cáo

- Viết đoạn văn liền mạch, không bullet points
- Đóng góp 1: tên tiếng Anh giữ là "Repository-Aware Prompting Strategy", nhưng trong văn bản tiếng Việt gọi là "quy trình repository-aware prompting" (không dịch thành "chiến lược"); khi so sánh các cách prompt thì viết "các quy trình prompting". "Strategy switching" của Adaptive Repair giữ nguyên
- Thuật ngữ Anh giữ nguyên (không dịch): LLM, API, unit test, assertion, coverage, mutation, test smell...
- Trích dẫn: [số] theo IEEE/ACM format
- Viết cho người đọc dễ hiểu (tiếng Việt + thuật ngữ Anh)

---

## Tóm tắt

| Đóng góp | Mô tả | Điểm nổi bật |
|-----------|--------|----------------|
| **Repository-Aware Prompting** | Tích hợp metadata dự án vào prompt | Build tool, Java version, dependencies, test framework, project structure, source/test paths |
| **Adaptive Repair** | Sửa thích ứng dựa trên trạng thái và lịch sử | Error signature, checkpoint, rollback, strategy switching |
| **Multi-Dimensional Evaluation** | Đánh giá toàn diện tính hợp lệ và chất lượng | Verification metrics, quality metrics, repair effectiveness |
