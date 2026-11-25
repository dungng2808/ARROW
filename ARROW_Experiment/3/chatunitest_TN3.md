# TN3, nhánh ChatUniTest: sinh bộ test ChatUniTest cho RQ3

Nguồn thiết kế: `Sua/ban_phan_bien/cac_thuc_nghiem.md`, mục "Thực nghiệm 3" (phần "Trước khi chạy" mục 6–7,
"Chạy ChatUniTest", "Xác minh chung", "Đo trên bộ test VALID") và bảng "Chỗ thiết kế chưa khớp thực tế".
Tổng quan TN3: [README.md](README.md).

Ký hiệu: ✅ đã chốt trong thiết kế. **Mọi điểm của nhánh ChatUniTest đã chốt ngày 26/11/2026** (bảng "Quyết định chốt 26/11/2026" trong `cac_thuc_nghiem.md`).

## 1. Mục đích

ChatUniTest 2.1.1 là hệ thống **sinh test bằng LLM có vòng sửa** ("Generate-Validate-Fix"), cùng loại với ARROW. Nhánh
này chạy ChatUniTest **với cùng hai mô hình và cùng tham số suy luận như ARROW** trên các lớp Maven, đưa bộ test qua
**cùng 5 bước xác minh** và **cùng phép đo**, để so sánh:

- **Chính 2: ARROW – ChatUniTest, cùng mô hình**: ARROW-GPT với ChatUniTest-GPT, ARROW-Qwen với ChatUniTest-Qwen.
- **Phụ:** tỷ lệ qua từng bước (McNemar trên lớp Maven); chi phí (lượt gọi, token, thời gian mỗi lớp).
- **Phát hiện lỗi thật** trên GitBug-Java.

ChatUniTest được thêm ngày 17/11/2026, đổi quyết định 14/11 ("chỉ so với EvoSuite").

## 2. Phạm vi ✅

- Chỉ lớp **Maven** (ChatUniTest chỉ có plugin Maven): **191 / 36 / 25 = 252 lớp**.
- Lớp Gradle (9 Classes2Test + 7 GitBug-Java): ghi `NOT_APPLICABLE`, **không** tính là thất bại. Báo cáo số lớp bị loại
  vì Gradle theo từng dataset.
- Mỗi lớp, mỗi mô hình chạy **1 lần** (như ARROW): 252 × 2 = **504 lần**.
- Chạy xong mà không cho ra bộ test (lỗi, quá giờ): ghi theo mã lỗi, **tính vào mẫu số** tỷ lệ qua từng bước, giống
  INVALID_OUTPUT của ARROW.

## 3. Chạy khói trước (làm trước TN3) ✅ chốt 26/11/2026: không chạy 50 lớp pilot

**Chạy khói trên 2 lớp Maven** (một JUnit 4, một JUnit 5), **cả hai mô hình**, để:

- thấy pipeline chạy thông từ đầu tới cuối: sinh → gộp → 5 bước xác minh → đo → ghi kết quả và token;
- kiểm: sau khi thêm starter, test có sẵn còn đạt không (nhất là Surefire cũ); `selectClass` nhận tên ngắn hay đầy đủ;
  bộ test gộp đặt tên và đặt ở đâu.

**Không có tiêu chí đạt/không đạt.** Chạy khói chỉ để sửa lỗi pipeline trước khi chạy thật; kết quả không dùng trong báo cáo.

**Đã bỏ (26/11/2026):** bước chạy 50 lớp pilot, tiêu chí công bố trước (≥ 70% lớp Maven cho ra bộ test, chi phí trong
ngân sách Giai đoạn 4), và phương án thay bằng Randoop 4.3.3 đi kèm tiêu chí đó. Thời gian, lượt gọi và token của
ChatUniTest đo ngay trong TN3.

Bản dựng thử đã chạy khói 1 lớp JUnit 4 với GPT-6 Luna (mục 12); còn thiếu 1 lớp JUnit 5 và mô hình Qwen.

## 4. Cấu hình công bố trước ✅

| Mục | Giá trị |
|---|---|
| Phiên bản | 2.1.1 |
| `phaseType` | mặc định |
| `testNumber` | 5 |
| `maxRounds` | 5 |
| `merge` | true |
| Mô hình | GPT-6 Luna, Qwen3-Coder-30B; cùng tham số suy luận như ARROW (temperature 0, tắt suy luận, không top-p) |
| Timeout mỗi lớp | ✅ **60 phút** mỗi lớp, mỗi mô hình (chốt 26/11/2026) |
| Giới hạn câu trả lời | ✅ giữ `maxResponseTokens` mặc định của ChatUniTest (**1.024**); ARROW dùng 16.384; ghi rõ khác biệt trong RP2 (chốt 26/11/2026) |
| Starter | ✅ 1.4.0 (chốt 26/11/2026) |
| JDK | JDK của dự án |

✅ Có thêm `-Dtemperature=0` và thư mục `-DtestOutput`, `-DtmpOutput` riêng cho mỗi lần chạy (mặc định
ChatUniTest dùng chung `/tmp`, nhiều worker sẽ giẫm lên nhau).

## 5. Chuẩn bị trên mỗi máy

| Việc | Chi tiết |
|---|---|
| Plugin | ✅ `io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1`. **groupId viết thường**; bản viết hoa `io.github.ZJU-ACES-ISE` trên Maven Central chỉ tới 1.4.1. Tải sẵn vào `~/.m2` (việc 15 của thiết kế) |
| Starter | ✅ `io.github.ZJU-ACES-ISE:chatunitest-starter:1.4.0` (type pom). **Không dùng 1.5.0**: bản đó kéo Mockito 5, cần Java 11, dự án JDK 8 không biên dịch được |
| JDK chạy plugin | Plugin biên dịch cho Java 11 → dùng JDK dự án nếu ≥ 11, không thì JDK 11 |
| Proxy mô hình | mục 6.2 |
| Maven | `~/.m2/settings.xml` (bản sao Google của Maven Central) như mọi thực nghiệm |
| arrow | cùng commit với nhánh ARROW; `arrow tools install` |
| Tiền | ✅ Chốt 26/11: **nạp thêm khoảng $30** vào OpenRouter trước phần Qwen (số dư 26/11 khoảng $5,5, không đủ cho ChatUniTest-Qwen; mục 11) |

## 6. Quy trình cho mỗi lớp Maven, mỗi mô hình (lấy repo theo cách A, chốt 26/11)

Mỗi lớp tự tải repo, chạy xong thì xóa ([cach_lay_repo_TN3.md](cach_lay_repo_TN3.md)).

```
1. arrow inspect repo gốc        → JDK, lệnh build, BASELINE GỐC
2. mỗi mô hình, chép 2 bản từ mirror của arrow, cả hai thêm starter (scope test):
     gen : để ChatUniTest sinh test
     ver : để arrow xác minh bộ test
3. gen: mvn install -DskipTests (module của lớp + các module nó cần; plugin cần chúng trong ~/.m2)
4. gen: chạy plugin ChatUniTest, gọi mô hình qua proxy (mục 6.2), timeout 60 phút
5. lấy các lớp test sinh ra → gộp thành 1 lớp (mục 6.3)
6. ver: arrow xác minh 5 bước, so baseline gốc với baseline bản ver, đo (mục 7, 8)
7. xóa bản chép, mirror, thư mục làm việc
```

### 6.1. Lệnh chạy plugin

Thiết kế ghi: thêm khối `<plugin>` vào `pom.xml` của bản chép, rồi chạy `mvn chatunitest:class`.
✅ Chốt 26/11/2026: gọi thẳng tọa độ đầy đủ, khỏi sửa khối `<plugin>`. Chạy bằng **Maven của máy** (≥ 3.9, không dùng `mvnw` cũ của dự án), từ **thư mục gốc** dự án với `-pl <module> -am -fn` (dự án nhiều module); phát hiện khi chạy khói 26/11. Lệnh chạy trong thư mục module, với JDK của plugin:

```bash
mvn -B -ntp io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:class \
    -DselectClass=<FQN> -Dmodel=gpt-4o -DapiKeys=proxy \
    -Durl=http://127.0.0.1:<cổng>/<nhãn>/<mô hình>/v1/chat/completions \
    -DtestNumber=5 -DmaxRounds=5 -Dmerge=true -Dtemperature=0 \
    -DtestOutput=<thư mục ra> -DtmpOutput=<thư mục tạm>
```

### 6.2. Proxy mô hình ✅ (chốt 26/11/2026: proxy tự viết, không dùng LiteLLM)

Thiết kế cũ ghi "LiteLLM proxy cục bộ với `-Dmodel=<mô hình>`". **Đã bỏ LiteLLM**: nhóm tự viết proxy (một file Python,
không cần cài thêm gì). Lý do: ChatUniTest 2.1.1 **chỉ nhận một danh sách tên mô hình cố định** (`gpt-3.5-turbo`,
`gpt-4o`, `gpt-4o-mini`, `code-llama`…), nên dù dùng proxy nào cũng phải đổi tên mô hình; proxy tự viết còn đọc thẳng
cấu hình và khóa của arrow, nên tham số gửi đi chắc chắn giống ARROW. Proxy làm:

- ChatUniTest khai `-Dmodel=gpt-4o` (ngân sách ngữ cảnh 122.880 token);
- proxy nhận đường dẫn `/<nhãn>/<mô hình arrow>/v1/chat/completions` và đổi `gpt-4o` sang `model_id` thật;
- proxy chèn tham số của mô hình trong cấu hình arrow (temperature, seed, reasoning_effort, provider, ví dụ ghim Amazon
  Bedrock cho Qwen); ✅ **không chèn `max_tokens`**, giữ giới hạn câu trả lời của ChatUniTest
  (`maxResponseTokens`, mặc định 1.024), vì ChatUniTest dùng nó để tính phần ngữ cảnh còn cho prompt (chốt 26/11/2026;
  ARROW dùng 16.384, ghi rõ khác biệt trong RP2);
- proxy đọc khóa giống arrow (biến `ARROW_SECRET_<REF>`, `secrets/<ref>.key`, Windows DPAPI);
- proxy ghi mỗi lượt gọi một dòng: nhãn, mô hình, mã trạng thái, token vào/ra, thời gian, tham số đã gửi.

Nhãn mỗi lần chạy có thời điểm (`<task_id>__<mô hình>__<giờ>`), để token
không bị cộng dồn khi chạy lại.

### 6.3. Gộp bộ test

Thiết kế giả định `merge=true` cho ra một bộ gộp. ✅ Thực tế `merge=true` chỉ sinh **một lớp `@Suite`**
(`@SelectClasses`) trỏ tới các lớp test, **không gộp mã**, mà `arrow gen --c0` chỉ nhận một lớp. Bản dựng thử làm:

- **1 lớp test**: dùng nguyên lớp đó, không bọc gì;
- **nhiều lớp** (✅ chốt 26/11/2026, cách A): **gộp phẳng** (`gop_scaffolding.merge_flat`): chép mọi thành viên (trường,
  phương thức, lớp con, khối khởi tạo) vào một lớp thường `<Lớp>_ChatUniTest`; tên trùng ở lớp sau thêm `_c<số thứ tự>`;
  constructor thành khối khởi tạo; chú thích cấp lớp giữ một cái cho mỗi loại. Ghi `merge_mode`, `merge_info`.
- Không dùng `@Nested` nữa: khi chạy khói, gộp `@Nested` ra **0 test** (DISCOVERY) trên dự án chạy JUnit 5 bằng Surefire
  cũ (2.22), vì arrow chạy `-Dtest=<lớp>` và Surefire lọc mất lớp lồng. Gộp phẳng thì đủ test (đã kiểm trên bxbot).
- Ngoại lệ ghi RP2: `@BeforeEach`/`@AfterEach` của lớp này cũng chạy cho test của lớp khác; chú thích cấp lớp áp cho mọi test.
- ✅ **`@RunWith` theo provider của Surefire** (chạy khói 26/11): dự án mà Surefire chạy bằng provider JUnit 4 (starter kéo
  `junit-platform-runner`) thì lớp JUnit 5 không có `@RunWith(JUnitPlatform.class)` bị bỏ qua (0 test); dự án dùng provider
  JUnit Platform thì ngược lại, `@RunWith` làm lớp bị bỏ qua. Quy tắc: xác minh lần đầu; ra DISCOVERY, log có `JUnit4Provider`
  và lớp chưa có `@RunWith` → thêm `@RunWith(JUnitPlatform.class)` (như lớp `@Suite` của ChatUniTest), xác minh lại một lần,
  ghi `runwith_added`, `first_verify_result`.

## 7. Xác minh ✅

- **5 bước, cùng arrow với ARROW:** `arrow gen --project <bản ver> --c0 <bộ đã gộp> --no-repair`.
  Các bước: biên dịch → được phát hiện → thực thi ≥ 1 phương thức → test đích đạt → không hồi quy trong module.
- **Kiểm lại trước khi ghi REGRESSION:** chạy riêng mỗi test có sẵn bị fail 3 lần trên bản không có test mới.
- ✅ **So baseline gốc với baseline bản `ver`:** starter kéo JUnit 5, vintage, Mockito vào dự án và có thể tự làm hỏng
  test có sẵn. Test đạt ở bản gốc mà mất ở bản `ver` → tính **REGRESSION**.
- ✅ Test cần JUnit 5 hoặc Mockito mà dự án không có: cho phép qua starter 1.4.0 (phạm vi test, chỉ bản chép của
  ChatUniTest), ghi là ngoại lệ (chốt 26/11/2026).

## 8. Đo trên bộ VALID

| Chỉ số | Cách đo |
|---|---|
| Dòng, nhánh (JaCoCo 0.8.12), mật độ smell (tsDetect), số test, số assertion | arrow đo khi xác minh |
| Điểm đột biến, test strength (PIT 1.17.4, `DEFAULTS`) | arrow đo với dự án JUnit 5. ✅ Dự án **không phải JUnit 5** (JUnit 4, TestNG): arrow thiếu `pitest-junit5-plugin` nên PIT báo OK mà không chạy được test (mutation = 0, thấy khi chạy khói 26/11) → script **luôn** đo lại với plugin này, ghi `pit_source = script-junit5` và giữ số của arrow ở `arrow_pit_mutation` Script đo lại cũng lỗi (vd. dự án ép JUnit Platform 1.3 mà `pitest-junit5-plugin` 1.2.1 cần ≥ 1.5) thì để trống mutation (không đo được, rơi ở lớp lọc 3), ghi `pit_source = script-junit5-failed`. |
| Chi phí ✅ | Lượt gọi mô hình, token prompt, token đầu ra, thời gian gọi: lấy từ log proxy theo nhãn; thời gian chạy plugin |

## 9. Ghi kết quả: mỗi (lớp, mô hình) một dòng

| Nhóm | Trường |
|---|---|
| Định danh | `task_id`, `dataset`, `class_fqn`, `model`, `machine`, `tool_commit`, tên mô hình khai cho ChatUniTest (`gpt-4o`) |
| Môi trường | `build_tool`, `jdk`, JDK chạy plugin, `framework`, `vintage` |
| Sinh | `gen_seconds`, `gen_exit`, `gen_timed_out`, số lớp test sinh ra, lý do nếu `NO_OUTPUT` / `NOT_APPLICABLE` |
| Chi phí | `cut_llm_calls`, `cut_prompt_tokens`, `cut_completion_tokens`, `cut_llm_errors`, `cut_llm_seconds`, nhãn proxy |
| Xác minh | `final_state`, các bước, số test, số assertion, `regressions`, so baseline |
| Chất lượng | `line`, `branch`, `method`, `mutation`, `test_strength`, `smell_density`, `pit_source` |
| Lưu bộ test | các file ChatUniTest sinh ra và bộ đã gộp (kèm sha256); log proxy `proxy_calls.jsonl` |

Hết tiền hoặc mô hình không dùng được: dừng cả lô (như TN1, TN2), ghi `STOPPED`, chạy lại sau.

## 10. Chia máy (10 phần: Dung 4, 6 phần còn lại cho 4 máy)

Chia 268 lớp thành **10 phần: Dung 4 phần (107 lớp), 6 phần còn lại (161 lớp) chia cho 4 máy, Chính ít hơn một chút: Chính 38, Kiều Anh, Quang, Hán mỗi máy 41** (chốt 26/11/2026). Xếp lớp theo thứ tự `sha256` cố định rồi giao lần lượt cho máy đang thiếu nhiều nhất so với tỷ lệ, nên mỗi máy nhận đủ ba bộ dữ liệu. Cả ba hệ thống của một lớp chạy trên cùng máy.

| Máy | Lớp | Trong đó Maven (chạy ChatUniTest) |
|---|---:|---:|
| Dung | 107 | 102 |
| Chính | 38 | 34 |
| Kiều Anh | 41 | 38 |
| Quang | 41 | 39 |
| Hán | 41 | 39 |
| **Tổng** | **268** | **252** |

## 11. Chi phí

- Mỗi lớp gọi mô hình nhiều hơn ARROW nhiều: 5 test mỗi phương thức × tối đa 5 vòng sửa, prompt dài. Số thật đo ở chạy
  khói (2 lớp) và các lớp đầu của TN3.
- Ước thô cho Qwen (giá Amazon Bedrock $0,15 / $0,60 mỗi triệu token vào / ra): 252 lớp có thể
  **$15–20 trở lên**. Số dư OpenRouter ngày 26/11 khoảng $5,5 → **phải nạp thêm** trước khi chạy ChatUniTest-Qwen.
- Qwen phải gọi qua OpenRouter, **ghim Amazon Bedrock** như TN1, TN2. Không dùng SiliconFlow: rẻ hơn nhưng khác máy
  chủ, khác cách triển khai.

## 12. Tham khảo từ bản dựng thử (đã xóa 26/11/2026)

Chạy khói 1 lớp, chỉ để tham khảo, **không dùng làm kết quả**: `BooleanParser` (Classes2Test, JUnit 4, JDK 8),
GPT-6 Luna.

| Mục | Giá trị |
|---|---|
| Kết quả | VALID, baseline bản `ver` giống bản gốc |
| Sinh | 1 lớp test, 3 phương thức, 11,6 giây, plugin chạy bằng JDK 11 |
| Chi phí | 3 lượt gọi, 1.029 token vào, 905 token ra, 13,6 giây gọi mô hình |
| Chất lượng | line 0,56; branch 1,0; mutation 0,75 (`pit_source = script-junit5`) |

Script: `~/ARROW_backups/tn3-ban-dung-truoc-20261126/` (`tn3_chatunitest.py`, `llm_proxy.py`, `gop_scaffolding.py`
hàm `merge_nested`).

## 13. Đã chốt (26/11/2026)

| # | Việc | Quyết định |
|---|---|---|
| 1 | Timeout mỗi lớp | 60 phút mỗi lớp, mỗi mô hình |
| 2 | Starter | 1.4.0 (không dùng 1.5.0); ghi ngoại lệ RP2 |
| 3 | Gọi plugin; proxy | Tọa độ đầy đủ, không sửa `<plugin>`; proxy tự viết, không LiteLLM |
| 3a | Giới hạn câu trả lời | Giữ `maxResponseTokens` mặc định 1.024 của ChatUniTest; ghi rõ khác ARROW (16.384) |
| 4 | Gộp bộ test | 1 lớp dùng nguyên; nhiều lớp gộp phẳng (đổi từ `@Nested` sau chạy khói); ghi ngoại lệ RP2 |
| 5 | So baseline bản vá; PIT dự án JUnit 4 | So baseline, mất test đạt → REGRESSION (cờ `DEP_BREAKS_BASELINE`); PIT bằng script có `pitest-junit5-plugin`; ghi ngoại lệ RP2 |
| 6 | Bước thử trước | Chỉ chạy khói 2 lớp × 2 mô hình; bỏ 50 lớp pilot, tiêu chí 70%, Randoop |
| 7 | Tiền OpenRouter | Nạp thêm khoảng $30 trước phần Qwen |
