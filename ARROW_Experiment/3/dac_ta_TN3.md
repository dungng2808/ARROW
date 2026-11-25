# Đặc tả Thực nghiệm 3 (TN3), bản dễ hiểu

Bản tóm tắt dễ đọc của thiết kế đã chốt ngày 26/11/2026 (`Sua/ban_phan_bien/cac_thuc_nghiem.md`, phần Thực nghiệm 3).
Muốn xem chi tiết kỹ thuật thì mở [README.md](README.md) và các file của từng nhánh: [arrow_TN3.md](arrow_TN3.md),
[evosuite_TN3.md](evosuite_TN3.md), [chatunitest_TN3.md](chatunitest_TN3.md).

---

## 1. TN3 là gì

TN3 trả lời câu hỏi: **bộ test do ARROW sinh ra có tốt hơn bộ test do hai công cụ khác sinh ra không?**

Hai công cụ đem so:
- **EvoSuite**: công cụ cũ, nổi tiếng, **không dùng AI**. Nó thử ngẫu nhiên rồi chọn lọc dần để ra test.
- **ChatUniTest**: công cụ **dùng AI** giống ARROW, cũng có vòng tự sửa lỗi.

Để công bằng, ba hệ thống sinh test cho **cùng các lớp Java**, rồi bộ test của cả ba đi qua **cùng một bộ kiểm tra** và
**cùng một cách chấm điểm**. Bộ kiểm tra và chấm điểm đều do tool `arrow` làm.

**Câu hỏi nghiên cứu (RQ3):** chất lượng bộ test hợp lệ của ARROW so với EvoSuite 1.2.0 và ChatUniTest 2.1.1 thế nào,
khi cả ba cùng qua 5 bước xác minh?

**Chỉ số chính:** **điểm đột biến** (mutation score), giải thích ở bước 5.

---

## 2. Ba hệ thống

| | ARROW | EvoSuite | ChatUniTest |
|---|---|---|---|
| Là gì | Công cụ của nhóm: AI sinh test + tự sửa | Sinh test bằng tìm kiếm, không AI | AI sinh test + tự sửa (của nhóm khác) |
| Phiên bản | tool `arrow`, một commit dùng chung mọi máy | 1.2.0 | 2.1.1 |
| Mô hình AI | GPT-6 Luna, Qwen3-Coder-30B | không dùng | **cùng** GPT-6 Luna, Qwen3-Coder-30B |
| Chạy trên | cả 268 lớp | cả 268 lớp | chỉ lớp dùng Maven (252 lớp) |
| TN3 có sinh test mới không | **Không**: lấy bộ test đã sinh ở TN1 | Có | Có |
| Mỗi lớp chạy mấy lần | 1 lần mỗi mô hình | 5 lần (5 seed) + 1 lần "cùng thời gian" | 1 lần mỗi mô hình |

---

## 3. Chạy trên những lớp nào

**268 lớp Java** của bộ dữ liệu đã chốt (`dataset-v1`):

| Bộ dữ liệu | Số lớp | Ghi chú |
|---|---:|---|
| Classes2Test | 200 | bộ chính |
| CAT-LM | 36 | |
| GitBug-Java | 32 | có lỗi thật, dùng thêm cho phép "phát hiện lỗi thật" |

- Không chọn bớt lớp nào. Lớp nào công cụ không chạy được thì vẫn chạy, và **tính là thất bại**.
- Riêng ChatUniTest chỉ chạy được trên dự án Maven. 16 lớp dùng Gradle ghi là `NOT_APPLICABLE` (không áp dụng),
  **không** tính là thất bại.

---

## 4. Các bước của TN3

```
Bước 0  Chuẩn bị máy
Bước 1  ARROW:       lấy bộ test TN1 → bộ nào chưa đạt thì sửa
Bước 2  EvoSuite:    sinh test (5 seed) + 1 lần cùng thời gian với ARROW
Bước 3  ChatUniTest: chạy khói 2 lớp → sinh test trên mọi lớp Maven
            ↓   (cả ba hệ thống)
Bước 4  Kiểm tra 5 bước bằng arrow
Bước 5  Chấm điểm bộ test đạt
Bước 6  Phát hiện lỗi thật (GitBug-Java)
Bước 7  Gom kết quả 5 máy, so sánh, lập bảng
```

Ba nhánh ARROW, EvoSuite, ChatUniTest **chạy độc lập**: nhánh này hỏng (ví dụ mất mạng gọi AI) thì nhánh khác vẫn chạy.
Mỗi nhánh tự tải mã dự án về, chạy xong thì xóa (cách A, chốt 26/11).

### Bước 0. Chuẩn bị máy

- Cài tool `arrow` (cùng một bản trên mọi máy), JDK 8, 11, 17, Maven.
- Tải EvoSuite 1.2.0 (kiểm sha256).
- Có khóa gọi AI. Qwen gọi qua OpenRouter, ghim nhà cung cấp Amazon Bedrock, giống TN1 và TN2.
- **Nạp thêm khoảng $30 vào OpenRouter** trước khi chạy phần Qwen (số dư ngày 26/11 chỉ còn khoảng $5,5).

### Bước 1. ARROW

ARROW **không sinh test mới**. Mỗi lớp, mỗi mô hình lấy bộ test sinh bằng cách viết prompt **Zero-shot-project-aware (PA)**
ở TN1:

| Bộ test TN1 | Số bộ | Làm gì |
|---|---:|---|
| Đã đạt ngay | 195 | Chấm điểm lại |
| Chưa đạt | 335 | **Sửa** bằng cơ chế Adaptive, tối đa **10 vòng**, cùng mô hình đã sinh |
| Không thành file test (INVALID_OUTPUT) | 6 | Không sửa được → **tính là thất bại** |

6 bộ INVALID_OUTPUT đều của Qwen: 5 bộ viết quá dài nên bị cắt ngang, 1 bộ có hai lớp trong một file. Không có file test
thì không có gì để sửa.

### Bước 2. EvoSuite

- **Cấu hình chính:** mỗi lớp chạy **5 lần** với seed 1, 2, 3, 4, 5, mỗi lần tối đa **20 phút**. Seed là con số khởi đầu
  cho phần ngẫu nhiên; khác seed thì ra bộ test khác.
- **Cấu hình phụ:** 1 lần, cho EvoSuite đúng bằng thời gian ARROW đã dùng cho lớp đó. Phải chạy sau khi nhánh ARROW
  của lớp đó xong.
- EvoSuite cần vài thư viện mà dự án có thể không có, nên được **thêm vào bản sao** của dự án (không sửa dự án gốc):
  thư viện chạy của EvoSuite, JUnit 4.12, `junit-vintage-engine`, và một "listener" theo hướng dẫn của EvoSuite.
- EvoSuite sinh **2 file** cho mỗi lớp, mà `arrow` chỉ nhận 1 lớp, nên script **gộp 2 file thành 1**.
- Điểm của một lớp = **trung vị** điểm của các seed đạt, kèm số seed đạt trên 5.

### Bước 3. ChatUniTest

- **Chạy khói trước:** 2 lớp Maven (một JUnit 4, một JUnit 5) × 2 mô hình, chỉ để chắc chắn mọi khâu chạy thông. Không
  có tiêu chí đạt/không đạt, kết quả không đưa vào báo cáo.
- **Chạy thật:** mỗi lớp Maven, mỗi mô hình chạy **1 lần**, tối đa **60 phút**. Cấu hình mặc định: 5 test mỗi phương
  thức, tối đa 5 vòng sửa.
- ChatUniTest gọi AI qua một **proxy do nhóm tự viết**. ChatUniTest chỉ nhận vài tên mô hình cố định, nên nó khai
  `gpt-4o`; proxy đổi sang đúng mô hình của ARROW, gửi cùng tham số như ARROW, và ghi lại số token để tính chi phí.
- Thêm thư viện `chatunitest-starter` 1.4.0 vào **bản sao** dự án (cung cấp JUnit 5, Mockito cho test của ChatUniTest).
- ChatUniTest sinh nhiều lớp test cho một lớp, nên script **gộp thành 1 lớp**.
- Khác ARROW một điểm, ghi rõ trong báo cáo: ChatUniTest giữ giới hạn câu trả lời mặc định của nó (1.024 token), ARROW
  dùng 16.384 token.

### Bước 4. Kiểm tra 5 bước (giống nhau cho cả ba)

Bộ test phải qua lần lượt 5 bước; trượt ở bước nào thì dừng ở trạng thái đó:

| Bước | Hỏi | Trượt thì ghi |
|---|---|---|
| 1 | Biên dịch được không? | COMPILE |
| 2 | Công cụ chạy test có **tìm thấy** các test không? | DISCOVERY |
| 3 | Chạy được ít nhất 1 test mà không bị lỗi văng ra không? | RUNTIME |
| 4 | Các test có **đạt** không (không sai kết quả mong đợi)? | ASSERTION |
| 5 | Thêm bộ test vào dự án có làm **hỏng test có sẵn** không? | REGRESSION |
| ✓ | Qua cả 5 bước | **VALID** (đạt) |

Hai quy tắc thêm:
- **Kiểm lại trước khi ghi REGRESSION:** test có sẵn bị hỏng thì chạy riêng nó 3 lần. Nếu tự nó cũng lúc đạt lúc hỏng
  (flaky) thì không đổ lỗi cho bộ test mới.
- **Thư viện thêm vào cũng có thể làm hỏng dự án.** Vì vậy với EvoSuite và ChatUniTest, so kết quả test có sẵn của dự án
  gốc với bản đã thêm thư viện. Thêm thư viện mà mất test đạt thì tính REGRESSION.

Lưu ý về EvoSuite: khá nhiều lớp (khoảng 30% ở lần chạy thử) ra REGRESSION, vì cơ chế chạy riêng của EvoSuite làm hỏng
test Spring và Mockito có sẵn của dự án. Vẫn tính là REGRESSION, nhưng báo cáo tách riêng nhóm này và giải thích nguyên nhân.

### Bước 5. Chấm điểm bộ test đạt (VALID)

Chỉ bộ VALID mới được chấm điểm:

| Chỉ số | Nghĩa đơn giản | Công cụ |
|---|---|---|
| **Điểm đột biến** (chỉ số chính) | Cố tình cài lỗi nhỏ vào mã (đổi `>` thành `>=`, `+` thành `-`…), xem bộ test **bắt được bao nhiêu % lỗi cài vào**. Càng cao càng tốt | PIT 1.17.4 |
| Test strength | Như trên, nhưng chỉ tính trên phần mã mà test có chạy qua | PIT |
| Độ bao phủ dòng / nhánh | Bộ test chạy qua bao nhiêu % **dòng lệnh** / **nhánh if-else** của lớp | JaCoCo |
| Mật độ test smell | Số "mẫu viết test xấu" trên mỗi phương thức test. Càng thấp càng tốt | tsDetect |
| Số test, số assertion | Bộ test to cỡ nào | arrow |
| Chi phí (ARROW, ChatUniTest) | Số lần gọi AI, số token, thời gian mỗi lớp | log |

Vài trường hợp đặc biệt phải đo bằng script (cùng tham số với `arrow`) vì `arrow` không đo được, ví dụ PIT cho bộ
EvoSuite, hoặc PIT cho bộ ChatUniTest trên dự án JUnit 4. Các trường hợp này đều ghi trong báo cáo.

### Bước 6. Phát hiện lỗi thật (chỉ GitBug-Java)

GitBug-Java có **bản lỗi thật** của từng dự án. Test được sinh trên bản đã sửa lỗi, rồi đem chạy trên bản còn lỗi:

| Kết quả trên bản lỗi | Nghĩa |
|---|---|
| Có test hỏng | **Phát hiện được lỗi** |
| Mọi test vẫn đạt | Không phát hiện |
| Không biên dịch được | Báo cáo riêng, không tính |

- Làm cho cả ba hệ thống, thêm **test do người viết** làm mốc tham chiếu.
- EvoSuite có 5 seed: lấy bộ của **seed nhỏ nhất ra VALID**.

### Bước 7. So sánh và lập bảng

**Hai phép so sánh chính**, làm riêng cho từng bộ dữ liệu, từng mô hình, từng chỉ số:

1. **ARROW với EvoSuite**
2. **ARROW với ChatUniTest, cùng mô hình**: ARROW-GPT với ChatUniTest-GPT, ARROW-Qwen với ChatUniTest-Qwen.

Cách so: chỉ lấy những lớp mà **cả hai hệ thống đều có bộ VALID** và đều chấm được điểm (gọi là "cặp"), rồi:
- dùng **kiểm định Wilcoxon** để xem chênh lệch có ý nghĩa thống kê không;
- dùng **A12** để xem chênh nhiều hay ít;
- dùng **Holm** để hiệu chỉnh khi làm nhiều phép kiểm định cùng lúc.

**Quy tắc 10 cặp:** phép so sánh nào có **dưới 10 cặp** thì chỉ mô tả số liệu, không kiểm định. Dự kiến với EvoSuite,
Classes2Test đủ cặp; CAT-LM và GitBug-Java có thể thiếu.

**Các phân tích phụ:**
- Tỷ lệ qua từng bước kiểm tra của ba hệ thống.
- Chi phí ARROW với ChatUniTest: số lần gọi AI, token, thời gian.
- ARROW với EvoSuite khi EvoSuite có **cùng thời gian**.
- Tính trên **mọi lớp**: lớp không có bộ VALID tính điểm 0 (cách tính của AgoneTest).
- Nếu chỉ đòi hỏi "biên dịch được" thì mỗi hệ thống được chấp nhận bao nhiêu bộ, so với khi đòi đủ 5 bước.

---

## 5. Cái gì ghi lại

Mỗi lần chạy ghi **một dòng** vào file kết quả của máy, gồm: lớp nào, hệ thống nào, mô hình hay seed nào, dừng ở trạng
thái nào, điểm các chỉ số, thời gian, token. Bộ test sinh ra cũng được lưu lại.

| Hệ thống | Một dòng là |
|---|---|
| ARROW | (lớp, mô hình) |
| EvoSuite | (lớp, cấu hình, seed) |
| ChatUniTest | (lớp, mô hình) |
| Phát hiện lỗi | (lớp, hệ thống, bộ test) |

Bị ngắt giữa chừng thì chạy lại đúng lệnh cũ; phần đã xong được bỏ qua.

---

## 6. Ai chạy gì

Chia 268 lớp thành **10 phần: Dung 4 phần, 6 phần còn lại chia cho 4 máy, Chính ít hơn một chút** (chốt 26/11/2026).
Mỗi máy nhận đủ cả ba bộ dữ liệu. Cả ba hệ thống của một lớp chạy trên cùng một máy.

| Máy | Số lớp | Classes2Test / CAT-LM / GitBug-Java | EvoSuite chạy được (JDK ≤ 11) | ChatUniTest (Maven) | Bộ ARROW cần sửa |
|---|---:|---|---:|---:|---:|
| Dung | 107 | 80 / 14 / 13 | 100 | 102 | 128 |
| Chính | 38 | 28 / 6 / 4 | 32 | 34 | 47 |
| Kiều Anh | 41 | 31 / 5 / 5 | 39 | 38 | 50 |
| Quang | 41 | 31 / 5 / 5 | 38 | 39 | 54 |
| Hán | 41 | 30 / 6 / 5 | 37 | 39 | 56 |
| **Tổng** | **268** | **200 / 36 / 32** | **246** | **252** | **335** |

Cách chia: xếp 268 lớp theo thứ tự cố định (`sha256` của mã lớp, như TN2), rồi giao lần lượt cho máy đang thiếu nhiều
nhất so với tỷ lệ. 4/10 của 268 là 107,2 nên Dung lấy 107; 161 lớp còn lại chia 38 / 41 / 41 / 41.

Thời gian ước tính (theo số đo của bản dựng thử, 3 luồng song song): **Dung khoảng 20–27 giờ**, Kiều Anh / Quang / Hán
khoảng 8–10 giờ, Chính khoảng 7–9 giờ. Gần một nửa là ChatUniTest, phần khó đoán nhất; sẽ tính lại sau chạy khói.

**Thứ tự trên mỗi máy:**
1. ARROW và EvoSuite (cấu hình chính) chạy song song.
2. Sau khi ARROW xong: EvoSuite cấu hình "cùng thời gian".
3. ChatUniTest: chạy khói xong mới chạy thật; phần Qwen chạy sau khi đã nạp tiền.
4. Cuối cùng: phát hiện lỗi thật (chỉ các lớp GitBug-Java của máy).

Không chạy TN3 cùng lúc với TN2 trên cùng máy: EvoSuite ăn nhiều CPU, còn TN2 đo thời gian.

---

## 7. Những điều phải nói rõ trong báo cáo (ngoại lệ)

Để người đọc biết cuộc so sánh có công bằng không, báo cáo (RP2) phải ghi:

1. **Thư viện thêm vào bản sao dự án:** cho EvoSuite (thư viện chạy của EvoSuite, JUnit 4.12, vintage, listener); cho
   ChatUniTest (starter 1.4.0). Chỉ dùng khi chạy test, chỉ trên bản sao.
2. **So kết quả test có sẵn trước và sau khi thêm thư viện;** thêm thư viện mà làm hỏng test có sẵn thì tính REGRESSION.
3. **Gộp nhiều file test thành một lớp** cho EvoSuite và ChatUniTest.
4. **Đo bằng script** ở vài chỗ arrow không đo được (JaCoCo/PIT cho EvoSuite, PIT cho ChatUniTest trên dự án JUnit 4),
   cùng tham số với arrow.
5. **ChatUniTest giữ giới hạn câu trả lời 1.024 token** của nó; ARROW dùng 16.384.
6. **ChatUniTest gọi AI qua proxy tự viết** để dùng đúng mô hình và tham số của ARROW.
7. **6 bộ ARROW-Qwen INVALID_OUTPUT** tính là thất bại.
8. **REGRESSION của EvoSuite** do cơ chế chạy riêng của nó, tách riêng khi trình bày.

---

## 8. Kết quả cuối cùng (các bảng cho báo cáo)

- Bảng RQ3 cho từng bộ dữ liệu và từng mô hình: ba hệ thống × (tỷ lệ qua từng bước, điểm các chỉ số, số cặp; số seed đạt
  của EvoSuite); có cột cho biết bộ ARROW **đạt ngay** hay **phải sửa**.
- Bảng chi phí ARROW với ChatUniTest.
- Bảng EvoSuite cùng thời gian.
- Bảng tính trên mọi lớp (gán 0).
- Bảng phát hiện lỗi thật trên GitBug-Java.
- Bảng "chỉ biên dịch" so với "đủ 5 bước".
- Bảng JDK × framework × công cụ build của từng bộ dữ liệu.

---

## 9. Thuật ngữ

| Từ | Nghĩa |
|---|---|
| Lớp (CUT) | Lớp Java cần sinh test |
| Bộ test | File test sinh cho một lớp |
| VALID | Bộ test qua đủ 5 bước kiểm tra |
| Seed | Con số khởi đầu cho phần ngẫu nhiên của EvoSuite; khác seed thì ra bộ test khác |
| Baseline | Kết quả chạy test có sẵn của dự án khi chưa thêm gì |
| Flaky | Test lúc đạt lúc hỏng dù không đổi gì |
| Bản sao | Bản chép mã dự án để chạy, không đụng vào dự án gốc |
| Proxy | Chương trình nhỏ đứng giữa ChatUniTest và nhà cung cấp AI, để đổi tên mô hình và ghi token |
| Trung vị | Giá trị ở giữa khi xếp các số từ nhỏ đến lớn |
| Cặp | Một lớp mà cả hai hệ thống đem so đều có bộ VALID và đều chấm được điểm |
| PA (Zero-shot-project-aware) | Cách viết prompt của ARROW có kèm thông tin về dự án; TN3 chỉ dùng bộ test của cách này |
