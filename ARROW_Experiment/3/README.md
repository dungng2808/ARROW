# Thực nghiệm 3 (RQ3): ARROW so với EvoSuite và ChatUniTest

Nguồn: `Sua/ban_phan_bien/cac_thuc_nghiem.md`, các mục "Adaptive Repair cho TN3", "Thực nghiệm 3" và "Quy tắc chung
cho mọi thực nghiệm" (cập nhật 26/11/2026). File này chép lại thiết kế cho dễ làm theo, **không thêm quyết định mới**.
**Thiết kế TN3 đã chốt toàn bộ ngày 26/11/2026** (bảng "Quyết định chốt 26/11/2026" trong `cac_thuc_nghiem.md`, chép ở
mục 17). Mục 18 ghi những chỗ thiết kế cũ khác thực tế, phát hiện khi dựng thử (25–26/11/2026), và đã được sửa.

Ký hiệu: ✅ đã chốt · 🆕 mới theo phản biện, cần cài hoặc chạy. Làm bằng **script** quanh arrow (cùng một commit trên mọi
máy), không sửa tool.

**Bản dễ hiểu: [dac_ta_TN3.md](dac_ta_TN3.md).**

Chi tiết từng nhánh: [arrow_TN3.md](arrow_TN3.md) (ARROW), [evosuite_TN3.md](evosuite_TN3.md) (EvoSuite),
[chatunitest_TN3.md](chatunitest_TN3.md) (ChatUniTest). Cách lấy repo, chạy, xóa giữa các nhánh:
[cach_lay_repo_TN3.md](cach_lay_repo_TN3.md) (chốt 26/11: cách A, mỗi nhánh tự tải repo, xong việc thì xóa).

---

## 1. Câu hỏi

**RQ3:** chất lượng bộ test hợp lệ của ARROW so với **EvoSuite 1.2.0** và **ChatUniTest 2.1.1** thế nào, khi cả ba hệ
thống qua **cùng 5 bước xác minh** và **cùng phép đo**?

- Chỉ số chính khai báo trước: **điểm đột biến** (mutation score). Các chỉ số còn lại là kết quả phụ.
- ChatUniTest được thêm ngày 17/11/2026, đổi quyết định 14/11 ("chỉ so với EvoSuite"). Lý do: RQ3 cần một hệ thống sinh
  test bằng LLM có vòng sửa để so; EvoSuite không dùng LLM và chỉ chạy được trên ít lớp.

## 2. Vị trí trong chuỗi thực nghiệm

```
dataset-v1 → chạy khói → Pilot (chọn N) → TN1 → { TN2 ; TN3 } → Phân tích
```

- TN3 **chỉ lấy đầu vào từ TN1**, không lấy gì của TN2 (chốt 17/11/2026). TN2 và TN3 chạy độc lập.
- TN3 chạy trên **toàn bộ tập đánh giá**: 200 lớp Classes2Test + 36 CAT-LM + 32 GitBug-Java = **268 lớp**. Không rút tập con.

## 3. Ba hệ thống

| Hệ thống | Loại | Phiên bản | Chạy trên | Mô hình |
|---|---|---|---|---|
| **ARROW** | LLM + Adaptive Repair | bản tool dùng khi chạy chính (giống nhau trên mọi máy) | mọi lớp: 200 / 36 / 32 | GPT-6 Luna, Qwen3-Coder-30B |
| **EvoSuite** | Tìm kiếm (SBST), không dùng LLM | 1.2.0 | **mọi lớp**; "chạy được" = JDK ≤ 11: 188 / 33 / 25; lớp JDK 17 vẫn chạy và tính là thất bại ở bước tương ứng | — |
| **ChatUniTest** | LLM có vòng sửa ("Generate-Validate-Fix") | 2.1.1 (`chatunitest-maven-plugin`, MIT) | chỉ lớp **Maven**: 191 / 36 / 25; lớp Gradle ghi `NOT_APPLICABLE` | **cùng** GPT-6 Luna và Qwen3-Coder-30B, cùng tham số suy luận như ARROW |

Tham số chung khi gọi mô hình: temperature 0, max_tokens 16.384, tắt suy luận, không đặt top-p; Qwen gọi qua
**OpenRouter, ghim Amazon Bedrock** (như TN1, TN2); mọi tham số ghi vào file experiment và file lock của run.

## 4. Đầu vào

| # | Đầu vào | Lấy từ đâu | Số lượng |
|---|---|---|---|
| 1 | Danh sách lớp | Ba manifest cuối `dataset-v1` (`ARROW_dataset/final/`), không rút mẫu thêm | 268 lớp |
| 2 | Bộ test ARROW (TN3 không sinh mới) | Mỗi lớp, mỗi mô hình 1 bộ **Zero-shot-project-aware** của TN1: c0 nếu VALID ngay; nếu không thì bộ đã sửa bằng Adaptive Repair trong TN3 (mục 5) | 2 × 268 = 536 bộ |
| 3 | Snapshot từng lớp, để EvoSuite và ChatUniTest tự sinh test | Snapshot đã build mà ARROW dùng ở TN1 (cùng revision, cùng JDK) | 268 |
| 4 | Bản lỗi GitBug-Java (chỉ cho phép phát hiện lỗi thật) | Bước 0.6: bản sửa + `git apply -R bug.patch` (`bug_patch` trong JSON của bug) | 32 |

```
dataset-v1 (268 lớp) ──→ TN1: build snapshot, sinh 3 chiến lược × 2 mô hình
                                    │
                 chỉ giữ bộ Zero-shot-project-aware (mỗi lớp, mỗi mô hình)
                                    │
                 ┌──────────────────┴──────────────────┐
             VALID ngay                            chưa VALID
                 │                                      ↓
                 │                Adaptive Repair (cùng mô hình), trong TN3
                 └──────────────────┬──────────────────┘
                                    ↓
                          BỘ TEST ARROW (536)

GitBug-Java JSON (bug_patch) ──→ git apply -R ──→ BẢN LỖI (32)
```

## 5. Bước 1: Adaptive Repair cho bộ ARROW

- Nhánh **A (Adaptive)**, **N vòng** (N chọn ở pilot: **N = 10**), **cùng mô hình đã sinh c0**.
- Chạy cho **mọi** candidate Zero-shot-project-aware chưa VALID của TN1: tối đa 400 (Classes2Test) + 72 (CAT-LM) +
  64 (GitBug-Java) = 536. Candidate thuộc tập con 180 của TN2 **cũng sửa lại ở đây** (không lấy kết quả TN2).
- Candidate Zero-shot và Few-shot **không** sửa ở bước này.
- Mỗi bộ ARROW ghi cột `source`:

```
TN1: c0 VALID ngay?
 ├─ có    → dùng c0                                       (source = initial)
 └─ không → Adaptive Repair cùng mô hình, chạy trong TN3  (source = adaptive)
```

- Số thật từ TN1: **195 VALID ngay**, **335 cần sửa**, **6 INVALID_OUTPUT** (Qwen; không có file test → không sinh lại,
  không sửa, tính là thất bại).
- ✅ Làm bằng script: `arrow gen --c0 … --rounds 10` cho từng bộ, không sửa `rq3_only`. Chi tiết: [arrow_TN3.md](arrow_TN3.md).

## 6. Bước 2: Chạy EvoSuite 🆕

| Cấu hình | Cách chạy | Dùng để |
|---|---|---|
| **Chính** | Cấu hình mặc định, **5 seed khác nhau**, timeout **20 phút** mỗi lần, cả ba dataset | So sánh chính. Mỗi lớp: chỉ số = **trung vị qua các seed cho bộ VALID**, kèm số seed VALID trên 5 |
| **Phụ: cùng ngân sách** | Một lần, `-Dsearch_budget=<giây>` = thời gian trung vị ARROW dùng cho cùng lớp (sinh + sửa) | Phân tích phụ về hiệu quả theo thời gian; ghi nhãn riêng |

- ✅ Thư viện thêm vào bản chép (phạm vi test, ngoại lệ được công bố): runtime 1.2.0; `junit:junit:4.12` khi module thiếu
  JUnit 4 hoặc JUnit 4 < 4.12; `junit-vintage-engine` khi module chạy JUnit 5; listener `InitializingListener` trong
  Surefire (Maven, provider JUnit 4). Gộp scaffolding vào lớp test. Chi tiết: [evosuite_TN3.md](evosuite_TN3.md).
- ✅ Seed: **1, 2, 3, 4, 5** (seed chỉ dùng cho lần chạy EvoSuite, không dùng để chọn mẫu).
- Khối lượng: 268 lớp × 6 = **1.608 lần sinh**, cộng xác minh và PIT/JaCoCo/tsDetect cho mọi bộ VALID.

## 7. Bước 3: Chạy ChatUniTest 🆕

### 7.1. Chạy khói trước (làm trước TN3) — chốt 26/11/2026: không chạy 50 lớp pilot

- **Đã biết từ mã nguồn:**
  - Prompt mặc định (`initial_system.ftl`) cố định *"create a whole unit test using JUnit 5"*: test luôn là JUnit 5.
  - README yêu cầu thêm `chatunitest-starter` vào `pom`; starter 1.5.0 kéo JUnit Jupiter 5.7.2, junit-vintage-engine,
    junit-platform-launcher, Mockito 5, ByteBuddy.
- **Chạy khói trên 2 lớp Maven** (một JUnit 4, một JUnit 5), cả hai mô hình: pipeline chạy thông từ đầu tới cuối (sinh →
  gộp → 5 bước xác minh → đo → ghi kết quả, token); sau khi thêm starter, test có sẵn còn đạt không (nhất là Surefire
  cũ); `selectClass` nhận tên ngắn hay đầy đủ; bộ test gộp đặt tên và đặt ở đâu.
- **Không có tiêu chí đạt/không đạt**; kết quả chạy khói không dùng trong báo cáo.
- **Đã bỏ (26/11/2026):** bước chạy 50 lớp pilot, tiêu chí ≥ 70% và phương án thay bằng Randoop 4.3.3 đi kèm. Thời gian,
  lượt gọi và token của ChatUniTest đo ngay trong TN3.

### 7.2. Cấu hình công bố trước

Phiên bản 2.1.1; `phaseType` mặc định; `testNumber` = 5; `maxRounds` = 5; `merge` = true; cùng hai mô hình và cùng tham
số suy luận như ARROW, **trừ giới hạn câu trả lời** (giữ `maxResponseTokens` mặc định 1.024 của ChatUniTest; ARROW
16.384; ghi rõ trong RP2); timeout **60 phút**; starter **1.4.0**; JDK chạy plugin = JDK dự án nếu ≥ 11, không thì JDK 11. ✅

### 7.3. Cách chạy (mỗi lớp Maven, mỗi mô hình, 1 lần)

1. Chép snapshot đã build của ARROW sang thư mục làm việc riêng.
2. Thêm `chatunitest-starter` **1.4.0** (scope test) vào `pom.xml` **của bản chép** (ngoại lệ được công bố); **không** sửa khối `<plugin>`; `mvn install -DskipTests`.
3. Chạy, với JDK chạy plugin, timeout 60 phút; ghi mã thoát và thời gian:
   ```
   mvn -B io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:class -DselectClass=<lớp> -Durl=<proxy tự viết>/<nhãn>/<mô hình>/v1/chat/completions -Dmodel=gpt-4o -DapiKeys=<khóa giả>
       -DtestNumber=5 -DmaxRounds=5 -Dmerge=true -Dtemperature=0 -DtestOutput=<riêng> -DtmpOutput=<riêng>
   ```
4. 1 lớp test thì dùng nguyên; nhiều lớp thì **gộp phẳng** thành một lớp `<Lớp>_ChatUniTest` (tên trùng thêm `_c<số>`;
   `merge=true` chỉ sinh lớp `@Suite`, không gộp mã), rồi đưa qua 5 bước xác minh và phép đo của ARROW. Plugin chạy bằng
   Maven của máy, từ thư mục gốc với `-pl <module> -am -fn`.

### 7.4. Quy định

- Gọi mô hình **qua proxy cục bộ do nhóm tự viết, không dùng LiteLLM** (chốt 26/11/2026): ChatUniTest khai `gpt-4o`,
  proxy đổi sang mô hình thật và chèn cùng tham số như ARROW; khóa thật chỉ nằm ở proxy, không ghi vào `pom.xml`; proxy
  ghi lượt gọi và token theo cùng cách với ARROW. Chi tiết: [chatunitest_TN3.md](chatunitest_TN3.md) mục 6.2.
- Repo Gradle: `NOT_APPLICABLE`, **không** tính là thất bại; báo cáo số lớp bị loại vì Gradle theo từng dataset.
- Chạy xong mà không cho ra bộ test (lỗi, timeout): ghi theo mã lỗi; **tính vào mẫu số** tỷ lệ qua từng bước, giống
  INVALID_OUTPUT của ARROW.
- ✅ Test cần JUnit 5 hoặc Mockito mà dự án không có: cho phép qua starter 1.4.0 (phạm vi test, chỉ bản chép của
  ChatUniTest), ghi là ngoại lệ.
- Khối lượng: 252 lớp Maven × 2 mô hình = **504 lần**. Mỗi lần gọi mô hình nhiều hơn ARROW nhiều (5 test mỗi phương
  thức × tối đa 5 vòng sửa); đo ngay trong TN3. ✅ Nạp thêm khoảng $30 vào OpenRouter trước phần Qwen.

## 8. Bước 4: Xác minh chung

Bộ test của **cả ba hệ thống** qua cùng 5 bước:

```
biên dịch → được phát hiện → thực thi ≥ 1 phương thức → test đích đạt → không hồi quy trong module
```

Thứ tự trạng thái khi báo cáo: COMPILE < UNKNOWN < DISCOVERY < RUNTIME < ASSERTION < REGRESSION < VALID.

**Kiểm tra lại trước khi ghi REGRESSION** 🆕 (áp cho cả ARROW, EvoSuite, ChatUniTest):
1. Mỗi test có sẵn bị fail: chạy riêng **3 lần** trên bản sao không có test mới. Fail ≥ 1 lần → `suspected_flaky`, không tính hồi quy.
2. Cả 3 lần đạt: chạy lại module 1 lần kèm test mới. Vẫn fail → REGRESSION.
3. Ghi `regression_recheck` và `suspected_flaky` vào `verify.json`.

✅ **So baseline bản gốc với bản đã thêm thư viện** (EvoSuite, ChatUniTest): test đạt ở bản gốc mà mất ở bản đã thêm thư
viện → tính REGRESSION, kèm cờ `DEP_BREAKS_BASELINE`.

✅ **REGRESSION do classloader của EvoSuite** (`separateClassLoader = true` rò sang test có sẵn: Spring `not assignable`,
Mockito `ClassCastException`): tính REGRESSION trong số chính; tách một dòng mô tả phụ theo loại lỗi.

## 9. Bước 5: Đo trên bộ test VALID

- Độ bao phủ dòng và nhánh: **JaCoCo** (với EvoSuite đo trên bản sao `separateClassLoader = false`) ✅.
- Điểm đột biến và test strength: **PIT 1.17.4**, `DEFAULTS`, cùng cấu hình cho mọi bộ của cả ba hệ thống.
  ✅ Đo bằng script, cùng tham số PIT của ARROW, ghi `pit_source`: (a) EvoSuite: JaCoCo trên bản `false` (JUnitCore, JDK
  dự án), PIT trên bộ gốc (`true`) với minion JDK của dự án; (b) ChatUniTest trên dự án JUnit 4: thêm `pitest-junit5-plugin`.
- Mật độ test smell: **tsDetect** (số smell / số phương thức test).
- Kích thước bộ test, số phương thức test, số assertion (`count_assertions` của java-helper).
- Số và tỷ lệ bộ test qua từng bước xác minh của mỗi hệ thống.
- Với ARROW và ChatUniTest: số lượt gọi mô hình, token prompt, token đầu ra, thời gian mỗi lớp 🆕.

## 10. Cỡ mẫu: ba lớp lọc và ngưỡng 10 cặp

Cỡ mẫu của từng phép so sánh **không chốt trước**; nó là số lớp còn lại sau ba lớp lọc (làm riêng cho từng dataset,
từng mô hình, từng chỉ số):

1. **Chạy được:** ✅ EvoSuite cần **JDK ≤ 11** (framework không còn là điều kiện, vì đã thêm JUnit 4.12 và vintage); ChatUniTest cần Maven; ARROW chạy mọi lớp.
2. **Có bộ VALID:** qua đủ 5 bước. Với EvoSuite: lớp có ít nhất 1 seed VALID.
3. **Đo được chỉ số:** PIT, JaCoCo hoặc tsDetect chạy xong trên bộ đó.

```
n_cặp(A, B, dataset, mô hình, chỉ số) = số lớp mà A và B cùng qua cả ba lớp lọc
n_cặp < 10  →  phép so sánh đó chỉ báo cáo mô tả
```

Lớp 1 theo **JDK thật ARROW dùng ở TN1** (✅ tính lại 26/11/2026; bảng 18/11 cũ lấy JDK từ preflight và sai):

| | Classes2Test (200) | CAT-LM (36) | GitBug-Java (32) |
|---|---:|---:|---:|
| Maven / Gradle | 191 / 9 | 36 / 0 | 25 / 7 |
| JDK 8 / 11 / 17 | 166 / 22 / 12 | 30 / 3 / 3 | 16 / 9 / 7 |
| → EvoSuite chạy được (JDK ≤ 11) | **188** | **33** | **25** |
| → ChatUniTest chạy được (Maven) | 191 | 36 | 25 |

Hệ quả dự kiến: ARROW – ChatUniTest kiểm định được trên cả ba dataset; ARROW – EvoSuite trên Classes2Test trên ngưỡng
thoải mái; CAT-LM và GitBug-Java có thể dưới 10 cặp sau khi lọc bộ VALID. ✅ Giữ ngưỡng 10; RP2 ghi rằng so với EvoSuite
chủ yếu dựa vào Classes2Test. Báo cáo n_cặp ở mọi bảng RQ3.

## 11. So sánh và thống kê

| Phép so sánh | Cách làm |
|---|---|
| **Chính 1: ARROW – EvoSuite** | Từng cặp trên các lớp cả hai cùng có bộ VALID và cùng đo được chỉ số; báo cáo cỡ mẫu từng chỉ số; **Wilcoxon hạng có dấu + A12** |
| **Chính 2: ARROW – ChatUniTest** | Như trên, **cùng mô hình** (ARROW-GPT với ChatUniTest-GPT; ARROW-Qwen với ChatUniTest-Qwen), trên lớp Maven cả hai cùng có bộ VALID |
| Hiệu chỉnh | **Holm** cho hai phép so sánh chính, theo từng chỉ số, trong từng dataset |
| Phụ: tỷ lệ qua từng bước | Số và tỷ lệ bộ test qua từng bước của ba hệ thống trên cùng tập lớp (ARROW với ChatUniTest: **McNemar** trên lớp Maven) |
| Phụ: chi phí | ARROW với ChatUniTest: lượt gọi mô hình, token, thời gian mỗi lớp; chỉ mô tả |
| Phụ: EvoSuite cùng ngân sách | ARROW so với cấu hình EvoSuite cùng ngân sách, cùng cách |
| Phụ: toàn bộ lớp | Lớp không có bộ VALID gán 0 cho độ bao phủ và điểm đột biến (bỏ smell), cho cả ba hệ thống; trung bình, trung vị, Wilcoxon ghép cặp; nhãn "theo cách tính của AgoneTest". Mẫu số: ARROW–EvoSuite = toàn bộ 200/36/32 lớp; ARROW–ChatUniTest = lớp Maven |
| Đối chiếu khung đánh giá | Số bộ được chấp nhận nếu chỉ yêu cầu biên dịch so với khi yêu cầu đủ 5 bước, cho cả ba hệ thống |
| Test người viết | Chỉ là tham chiếu mô tả |
| EvoSuite – ChatUniTest | Không phải câu hỏi nghiên cứu; chỉ đặt cạnh nhau trong bảng mô tả |

Mỗi dataset kiểm định riêng, không gộp. Dataset nhỏ báo cáo kèm khoảng tin cậy 95% và độ lớn hiệu ứng; không viết
"không có ý nghĩa thống kê" thành "không có khác biệt". Công cụ: Wilson và Holm của `statsmodels`.

## 12. Bước 6: Phát hiện lỗi thật (chỉ GitBug-Java)

- Chép mỗi bộ VALID (sinh trên bản sửa) vào **bản lỗi**, build, chạy riêng bộ đó.
- Có test fail = **phát hiện**; mọi test đạt = **không phát hiện**; không biên dịch được = báo cáo riêng, không tính.
- Báo cáo số phát hiện / số bộ VALID kèm khoảng Wilson, theo hệ thống (cả ba); thêm hàng **test người viết** làm tham chiếu.
- Cài thành lệnh riêng, chạy sau TN3. CAT-LM không có bản lỗi nên không làm.
- ✅ EvoSuite: mỗi lớp một bộ, của **seed nhỏ nhất cho bộ VALID**; mọi seed VALID chỉ để mô tả phụ.

## 13. Đầu ra

- Bảng RQ3 theo từng dataset và từng mô hình: ba hệ thống × (qua từng bước, chỉ số chất lượng, số cặp; số seed VALID với EvoSuite); có cột `source` của bộ ARROW.
- Bảng chi phí ARROW – ChatUniTest.
- Bảng EvoSuite cùng ngân sách.
- Bảng phân tích phụ trên toàn bộ lớp.
- Bảng phát hiện lỗi thật trên GitBug-Java.
- Bảng đối chiếu khung đánh giá.
- Bảng JDK × framework × build tool.

## 14. Chốt trước khi chạy (ghi vào RP2)

1. 🆕 **Bảng JDK × framework × build tool** của từng dataset (từ `block1/metadata.json`): hàng JDK 8/11/17/21; cột JUnit 4 / JUnit 5 + vintage / JUnit 5 không vintage / TestNG; tách Maven và Gradle. Cột vintage đọc từ `pom.xml`/`build.gradle`.
2. 🆕 **Ngưỡng** 10 cặp (mục 10).
3. ✅ **Cấu hình ARROW đem so**: bộ Zero-shot-project-aware, bộ ban đầu nếu VALID ngay, nếu không thì bộ sau Adaptive; báo cáo riêng từng mô hình và từng dataset.
4. 🆕 **Chỉ số chính:** điểm đột biến.
5. ✅ **Seed EvoSuite: 1, 2, 3, 4, 5.**
6. 🆕 **Cấu hình ChatUniTest** (mục 7.2).
7. 🆕 **Chạy khói ChatUniTest trước** (mục 7.1; không chạy 50 lớp pilot, chốt 26/11).

## 15. Log bắt buộc

| Mức | Trường |
|---|---|
| Mỗi lần gọi mô hình | mô hình, `model_id` trả về, token prompt, token đầu ra, thời gian gọi (`usage.json`) |
| Mỗi run | phiên bản tool (commit, **giống nhau trên mọi máy**), sha256 manifest (`run.lock.json`) |
| Mỗi lô | máy, `--jobs`, tải máy |
| Lỗi hạ tầng | thử lại ≤ 3 lần, cách 30 s; sau đó ghi INFRA, đưa vào danh sách chạy lại, không tính kết quả cho tới khi chạy lại xong |

## 16. Việc tool / script cần làm cho TN3 (theo bảng việc của thiết kế)

| # | Việc | Loại |
|---|---|---|
| 6 | EvoSuite 5 seed + cấu hình `search_budget` theo thời gian ARROW; gộp trung vị qua seed | Script |
| 7 | Kiểm tra lại 3 lần trước REGRESSION (cũng dùng ở TN1, TN2) | Đã có trong arrow (bản dùng cho TN2) |
| 8 | Lệnh chạy bộ test trên bản lỗi GitBug-Java | Script |
| 11 | Script bảng JDK × framework | Script |
| 12 | Script phân tích phụ trên toàn bộ lớp, gán 0 | Script |
| 14 | Runner ChatUniTest: chép snapshot, thêm plugin vào `pom` bản chép, chạy `chatunitest:class` qua proxy tự viết (không dùng LiteLLM), lấy bộ gộp, đổi tên, đưa vào xác minh của ARROW; `NOT_APPLICABLE` cho Gradle | Script |
| 15 | Tải sẵn plugin ChatUniTest 2.1.1 và phụ thuộc vào cache Maven trên mọi máy | Chuẩn bị |
| 17 | Adaptive cho TN3 chạy trên **mọi** candidate Zero-shot-project-aware chưa VALID | Script (`arrow gen --c0 … --rounds 10`), không sửa `rq3_only` |

## 17. Quyết định chốt 26/11/2026 ✅

| # | Điểm | Quyết định |
|---|---|---|
| 1 | Thư viện cho bộ EvoSuite | runtime 1.2.0 + `junit:junit:4.12` (khi thiếu hoặc JUnit 4 < 4.12) + `junit-vintage-engine` (module JUnit 5) + listener `InitializingListener` (Maven, provider JUnit 4) |
| 2 | Starter ChatUniTest | 1.4.0 (không dùng 1.5.0) |
| 3 | Thư viện thêm vào làm hỏng test có sẵn | So baseline bản gốc với bản đã thêm thư viện; mất test đạt → REGRESSION, cờ `DEP_BREAKS_BASELINE` |
| 4 | Gọi plugin ChatUniTest | Tọa độ đầy đủ `io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:class`, không sửa `<plugin>` |
| 5 | Giới hạn câu trả lời của ChatUniTest | Giữ `maxResponseTokens` mặc định 1.024; ghi rõ khác ARROW (16.384) |
| 6 | Gộp bộ test thành một lớp | EvoSuite: chép scaffolding vào lớp test; ChatUniTest: 1 lớp dùng nguyên, nhiều lớp gộp phẳng (đổi từ `@Nested` sau chạy khói 26/11) |
| 7 | JaCoCo, PIT cho EvoSuite | JaCoCo trên bản `false`; PIT trên bộ gốc, minion JDK dự án; cả hai bằng script |
| 8 | PIT cho ChatUniTest trên dự án JUnit 4 | Script đo lại có `pitest-junit5-plugin`; ghi `pit_source` |
| 9 | Timeout ChatUniTest | 60 phút mỗi lớp, mỗi mô hình |
| 10 | Seed EvoSuite | 1, 2, 3, 4, 5 |
| 11 | "EvoSuite chạy được" | JDK ≤ 11 |
| 12 | Số lớp EvoSuite chạy được | 188 / 33 / 25 (theo JDK thật của TN1) |
| 13 | REGRESSION do classloader EvoSuite | Tính REGRESSION; tách mô tả phụ theo loại lỗi |
| 14 | Phát hiện lỗi thật, EvoSuite | Seed nhỏ nhất cho bộ VALID; mọi seed VALID chỉ mô tả phụ |
| 15 | Ngưỡng cặp | Giữ 10 cặp; RP2 ghi so với EvoSuite chủ yếu dựa vào Classes2Test |
| 16 | Tiền OpenRouter | Nạp thêm khoảng $30 trước phần Qwen |
| 17 | Sửa chữ | Qwen qua OpenRouter ghim Amazon Bedrock; 6 INVALID_OUTPUT tính thất bại; làm bằng script |
| — | Cùng ngày | Lấy repo cách A; proxy tự viết, không LiteLLM; ChatUniTest chỉ chạy khói (bỏ 50 lớp pilot, tiêu chí 70%, Randoop) |
| — | Chia máy | 10 phần: Dung 4 phần (107 lớp); 6 phần còn lại cho 4 máy: Chính 38, Kiều Anh 41, Quang 41, Hán 41 |

---

## 18. Những chỗ thiết kế cũ chưa khớp thực tế ✅ (phát hiện khi dựng thử 25–26/11/2026, đã chốt 26/11)

Bản dựng thử đã xóa ngày 26/11/2026; bản sao đầy đủ (code + kết quả: ARROW-GPT 123 dòng, EvoSuite 244 dòng của máy
Dung) ở `~/ARROW_backups/tn3-ban-dung-truoc-20261126/`. Cả 14 điểm dưới đây **đã chốt 26/11/2026** (mục 17) và đã sửa
vào các mục trên; bảng giữ lại để biết vì sao thiết kế đổi:

| # | Thiết kế ghi | Thực tế |
|---|---|---|
| 1 | Qwen3-Coder chạy qua Ollama, `num_ctx` 40.960 (mục tham số chung) | TN1, TN2 gọi Qwen qua **OpenRouter, ghim Amazon Bedrock** (100% lượt gọi). TN3 phải giữ đúng như vậy |
| 2 | EvoSuite chạy được 30–34 / 9–28 / 7–12 lớp | Theo JDK thật của TN1 (`block1`): **188 / 33 / 25** lớp theo định nghĩa đã chốt (JDK ≤ 11); 159 / 16 / 14 theo định nghĩa cũ. Ước lượng cũ lấy JDK từ preflight |
| 3 | Gọi `mvn chatunitest:class` sau khi thêm `<plugin>` | Gọi thẳng tọa độ đầy đủ `io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:class`, khỏi sửa `<plugin>`; groupId viết thường (bản viết hoa trên Maven Central chỉ tới 1.4.1) |
| 4 | Starter 1.5.0 | 1.5.0 kéo Mockito 5, cần Java 11 → dự án JDK 8 không biên dịch được. Bản chạy được: **1.4.0** (Mockito 3.8) |
| 5 | LiteLLM proxy với `-Dmodel=<mô hình>` | ChatUniTest 2.1.1 chỉ nhận một danh sách tên mô hình cố định → cần proxy đổi tên (khai `gpt-4o`, proxy đổi sang mô hình thật); ✅ chốt 26/11: proxy tự viết, không dùng LiteLLM |
| 6 | `merge=true` cho ra một bộ gộp | `merge=true` chỉ sinh một lớp `@Suite` (`@SelectClasses`) trỏ tới các lớp test, không gộp mã. ARROW (`--c0`) chỉ nhận một lớp → phải tự gộp. ✅ 1 lớp dùng nguyên; nhiều lớp gộp phẳng |
| 7 | Chỉ thêm runtime EvoSuite | EvoSuite chỉ sinh JUnit 4 và runtime không kèm JUnit → cần thêm `junit:junit` 4.12 khi module thiếu (hoặc JUnit 4 < 4.12, ví dụ 4.5 chưa có `@Rule`), thêm `junit-vintage-engine` khi module chạy JUnit 5. Hướng dẫn EvoSuite còn khuyên listener `InitializingListener` trong Surefire (chỉ hợp provider JUnit 4; với TestNG làm JVM test sập) |
| 8 | Lớp EvoSuite cũng có 2 file | EvoSuite sinh `X_ESTest` + `X_ESTest_scaffolding` → phải gộp scaffolding vào lớp test để ARROW nhận một lớp |
| 9 | JaCoCo đo trên bản `separateClassLoader = false` | Đúng, nhưng bản `false` làm PIT không thấy test → PIT phải đo trên bản gốc (`true`). Runtime EvoSuite 1.2.0 không chạy trên JDK 11 với lớp JDK 8 → minion PIT phải dùng JDK của dự án |
| 10 | REGRESSION của EvoSuite là ngoại lệ nhỏ (pilot cũ 6 bộ) | Chạy thật: khoảng 30% lớp EvoSuite ra REGRESSION do classloader riêng của EvoSuite rò sang test có sẵn (Spring `not assignable`, Mockito `ClassCastException`); listener không cứu được. Nên tách nhóm này khi phân tích |
| 11 | PIT cùng cấu hình cho ba hệ thống | Với dự án JUnit 4, ARROW chọn plugin PIT theo framework của dự án → bộ ChatUniTest (JUnit 5) thiếu `pitest-junit5-plugin` |
| 12 | Thêm thư viện test vào bản chép là đủ | Thư viện thêm vào có thể làm hỏng test có sẵn → cần so **baseline bản gốc** với **baseline bản đã vá**; test đạt ở bản gốc mà mất ở bản vá → tính REGRESSION |
| 13 | ARROW: 536 bộ | Thực tế TN1: 195 VALID ngay, 335 cần sửa, **6 INVALID_OUTPUT** (Qwen: 5 bị cắt do chạm 16.384 token đầu ra, 1 có hai lớp ngoài cùng) → không có file test nào để sửa, tính là thất bại |
| 14 | — | 25/11/2026 OpenRouter từ chối mọi lệnh sinh ("Request path cannot be relayed", sự cố chung); 26/11 đã chạy lại. Số dư OpenRouter còn khoảng $5,5, không đủ cho ChatUniTest-Qwen |
