# TN3: lấy repo, chạy, xóa thế nào

Câu hỏi: mỗi nhánh (ARROW, EvoSuite, ChatUniTest) có tự tải repo về, chạy xong xóa, rồi nhánh sau lại tải lại không?

**Trả lời ngắn:** bản dựng thử (25–26/11/2026, đã xóa, sao lưu ở `~/ARROW_backups/tn3-ban-dung-truoc-20261126/`)
làm đúng như vậy.

**Chốt 26/11/2026: cách A (giữ như cũ).** Mỗi nhánh tự tải repo, xong việc thì xóa `mirrors/` và `work/`; các nhánh độc
lập, không dùng chung mirror hay cache baseline (mục 3).

---

## 1. Bản dựng thử đã làm thế nào

Mỗi script có các ARROW_HOME riêng cho từng worker:

| Script | Home của worker |
|---|---|
| `tn3_arrow.py` | `homes/arrow-w1…` |
| `tn3_evosuite.py` | `homes/evo-w1…` |
| `tn3_chatunitest.py` | `homes/cut-w1…` |

Với **mỗi việc**, script làm theo vòng:

1. `arrow` clone repo (bản nông, chỉ đúng commit) vào `<home>/mirrors/github.com/<owner>/<repo>.git`.
2. Chép mã ra `<home>/work/` (EvoSuite và ChatUniTest thì chép thêm ra `work/evo-…`, `work/cut-…` bằng `git archive` từ mirror),
   build, chạy baseline, xác minh 5 bước, đo.
3. Xong việc, trả home về pool → `clean_home` **xóa `mirrors/` và `work/`**, chỉ giữ:
   - `runs/`: kết quả của arrow;
   - `cache/`: baseline đã tính;
   - `tools/`: JaCoCo, PIT, tsDetect.
4. Việc sau, hoặc nhánh sau, lại bắt đầu từ bước 1.

### Một lớp bị tải và build mấy lần

| Nhánh | Một "việc" là | Số lần clone mỗi lớp |
|---|---|---:|
| ARROW | (lớp, mô hình) | 2 |
| EvoSuite | lớp (5 seed dùng chung bản chép) | 1 |
| ChatUniTest | lớp (2 mô hình dùng chung) | 1 |
| Phát hiện lỗi (chỉ GitBug-Java) | lớp | 1 |
| **Tổng** | | **4–5** |

### Cái gì dùng chung, cái gì làm lại

| Dùng chung, không tải lại | Làm lại mỗi lần |
|---|---|
| Thư viện Maven/Gradle (`~/.m2`, `~/.gradle`) | **Clone repo**: thường vài giây đến khoảng 1 phút. Không đáng kể |
| JDK | **Baseline** (build + chạy toàn bộ test của module 2 lần): có thể vài phút mỗi lớp. Cache baseline nằm trong từng home, mà home của các script khác nhau, nên mỗi nhánh lại chạy baseline gốc từ đầu |
| JaCoCo, PIT, tsDetect | |

Thời gian chính vẫn là EvoSuite (tối đa 20 phút × 5 seed) và các vòng sửa/xác minh, nên phần làm lại chiếm khoảng
**5–10%** tổng thời gian.

---

## 2. Vì sao EvoSuite và ChatUniTest cần nhiều bản chép

| Bản chép | Dùng để |
|---|---|
| `orig` (EvoSuite) | build nguyên bản dự án, lấy class và classpath cho EvoSuite sinh test |
| `evo` (EvoSuite) | thêm thư viện EvoSuite cần (runtime, JUnit 4.12…), rồi arrow xác minh bộ EvoSuite trên bản này |
| `gen` (ChatUniTest) | thêm starter, chạy plugin ChatUniTest sinh test |
| `ver` (ChatUniTest) | thêm starter, rồi arrow xác minh bộ ChatUniTest trên bản này |

Bản đã thêm thư viện có thể làm hỏng test có sẵn, nên arrow chạy **thêm một baseline trên bản đó** để so với baseline gốc.
Baseline thứ hai này **không bỏ được**, dù chọn cách nào ở mục 3.

---

## 3. Ba cách làm cho bản dựng lại

| Cách | Làm gì | Được | Mất |
|---|---|---|---|
| **A. Giữ như cũ** | mỗi nhánh tự tải, xong việc thì xóa | đơn giản; ít tốn đĩa (quan trọng với máy Windows); các nhánh độc lập, nhánh này hỏng không kéo nhánh kia | tải và chạy baseline gốc 4–5 lần mỗi lớp |
| **B. Một lượt cho mỗi lớp** | tải 1 lần → ARROW → EvoSuite → ChatUniTest → xóa | tải và build 1 lần | các nhánh dính vào nhau: OpenRouter lỗi (như 25/11) là kẹt cả EvoSuite; ChatUniTest còn phải chờ thử 50 lớp pilot; chạy tiếp khi bị ngắt phức tạp hơn |
| **C. Giữ chung mirror và cache** | các nhánh vẫn chạy riêng, nhưng **dùng chung `mirrors/` và `cache/`**; chỉ xóa `work/` | không tải lại repo; baseline gốc chỉ chạy 1 lần mỗi lớp | tốn thêm đĩa cho mirror (bản nông, nhỏ); phải xóa mirror khi cả lớp xong mọi nhánh |

### Đã chốt: cách A (26/11/2026)

- Mỗi nhánh, mỗi việc tự clone repo vào home của worker, chạy, rồi xóa `mirrors/` và `work/` (giữ `runs/`, `cache/`, `tools/`).
- Các nhánh **độc lập hoàn toàn**: Qwen hay OpenRouter lỗi thì EvoSuite vẫn chạy được; ChatUniTest chờ chạy khói
  không làm chậm hai nhánh kia; mỗi nhánh chạy tiếp khi bị ngắt theo file kết quả của riêng nó.
- Chấp nhận tải và chạy baseline gốc 4–5 lần mỗi lớp (khoảng 5–10% tổng thời gian).
- Ít tốn đĩa, hợp với các máy Windows.
- Đã ghi vào `cac_thuc_nghiem.md`, phần Thực nghiệm 3.
