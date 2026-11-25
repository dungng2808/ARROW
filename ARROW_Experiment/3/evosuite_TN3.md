# TN3, nhánh EvoSuite: sinh bộ test EvoSuite cho RQ3

Nguồn thiết kế: `Sua/ban_phan_bien/cac_thuc_nghiem.md`, mục "Thực nghiệm 3" (phần "Chạy EvoSuite", "Xác minh chung",
"Đo trên bộ test VALID") và bảng "Chỗ thiết kế chưa khớp thực tế". Tổng quan TN3: [README.md](README.md).

Ký hiệu: ✅ đã chốt trong thiết kế. **Mọi điểm của nhánh EvoSuite đã chốt ngày 26/11/2026** (bảng "Quyết định chốt 26/11/2026" trong `cac_thuc_nghiem.md`).

## 1. Mục đích

EvoSuite 1.2.0 là hệ thống **sinh test bằng tìm kiếm (SBST), không dùng LLM**. Nhánh này sinh bộ test EvoSuite trên
**cùng 268 lớp** với ARROW, đưa qua **cùng 5 bước xác minh** và **cùng phép đo**, để so sánh:

- **Chính 1: ARROW – EvoSuite**, mỗi mô hình của ARROW, trong từng dataset.
- **Phụ: EvoSuite cùng ngân sách**: EvoSuite được cho đúng bằng thời gian ARROW đã dùng cho lớp đó.
- **Phát hiện lỗi thật** trên GitBug-Java.

## 2. Phạm vi ✅

- Chạy **mọi lớp** (200 / 36 / 32 = 268), không chỉ lớp "chạy được". Lớp không hợp JDK hoặc framework vẫn chạy; EvoSuite
  hỏng ở bước nào thì tính là thất bại ở bước đó.
- Lớp "chạy được" (lớp lọc 1 của cỡ mẫu): ✅ **JDK ≤ 11** (chốt 26/11/2026). Framework không còn là điều kiện, vì script thêm JUnit 4.12 và vintage khi module thiếu (mục 6).

Số lớp chạy được, tính theo **JDK thật của TN1** (`block1`), khác ước lượng 30–34 / 9–28 / 7–12 của thiết kế (ước lượng
cũ lấy JDK từ preflight):

| | Classes2Test | CAT-LM | GitBug-Java |
|---|---:|---:|---:|
| JDK 8 / 11 / 17 | 166 / 22 / 12 | 30 / 3 / 3 | 16 / 9 / 7 |
| JUnit 4 / JUnit 5 (có vintage) / JUnit 5 (không vintage) / TestNG | 163 / 5 / 24 / 8 | 16 / 2 / 18 / 0 | 15 / 0 / 17 / 0 |
| **EvoSuite chạy được** (JDK ≤ 11) | **188** | **33** | **25** |
| (định nghĩa cũ: JDK ≤ 11 và JUnit 4 hoặc JUnit 5 + vintage) | (159) | (16) | (14) |

Lớp JDK 17 (12 / 3 / 7) vẫn chạy EvoSuite và được tính là thất bại ở bước tương ứng.

## 3. Hai cấu hình ✅

| Cấu hình | Cách chạy | Mỗi lớp | Dùng để |
|---|---|---|---|
| **Chính** | Mặc định của EvoSuite, **5 seed khác nhau**, timeout **20 phút** mỗi lần, JDK của dự án | 5 lần | So sánh chính. Chỉ số của lớp = **trung vị qua các seed cho bộ VALID**, kèm số seed VALID trên 5 |
| **Phụ: cùng ngân sách** | 1 lần, `-Dsearch_budget=<giây>` = thời gian trung vị ARROW dùng cho cùng lớp (sinh + sửa) | 1 lần | Phân tích phụ về hiệu quả theo thời gian; ghi nhãn riêng |

- Khối lượng: 268 × 6 = **1.608 lần sinh**, cộng xác minh và đo cho mọi bộ VALID.
- ✅ Seed: **1, 2, 3, 4, 5** (chốt 26/11/2026), ghi trong file cấu hình.
- ✅ Ngân sách của cấu hình phụ: trung vị qua 2 mô hình của (thời gian sinh ở TN1 `total_s` + thời gian sửa ở TN3),
  tối thiểu 10 giây. Vì vậy cấu hình phụ chỉ chạy được **sau khi nhánh ARROW của lớp đó xong cả hai mô hình**.

## 4. Chuẩn bị trên mỗi máy

| Việc | Chi tiết |
|---|---|
| EvoSuite 1.2.0 | Tải từ GitHub release, kiểm sha256:<br>`evosuite-1.2.0.jar`: `0bc0062fdec70c35089ad736c9b88ccf87308c2f0aaaafe43176f3c4a89ddeba`<br>`evosuite-standalone-runtime-1.2.0.jar`: `cc856a6d02b391367d2a4516346c699c67e4b4a370203471df1f3d78b46b74dc` |
| Runtime vào `~/.m2` | `evosuite-standalone-runtime` 1.2.0 **không có trên Maven Central** → cài jar đúng sha256 bằng `maven-install-plugin:install-file`. Gradle dùng thẳng file jar (`files('<jar>')`) |
| JDK | 8, 11, 17 khai báo trong `java.jdk_homes` của arrow (EvoSuite chạy bằng JDK của dự án; PIT cần JDK ≥ 11) |
| Maven, Gradle | `~/.m2/settings.xml` và `~/.gradle/init.d/google-mirror.gradle` (bản sao Google của Maven Central) như mọi thực nghiệm |
| arrow | cùng commit với nhánh ARROW; `arrow tools install` để có JaCoCo 0.8.12, PIT 1.17.4, tsDetect |

## 5. Quy trình cho mỗi lớp (lấy repo theo cách A, chốt 26/11)

Mỗi lớp tự tải repo, chạy hết 5 seed, rồi xóa ([cach_lay_repo_TN3.md](cach_lay_repo_TN3.md)).

```
1. arrow inspect repo gốc          → JDK, lệnh build, classpath, BASELINE GỐC (test đạt / flaky / hỏng)
2. chép 2 bản từ mirror của arrow:
     orig : giữ nguyên, build bằng lệnh compile của arrow → class production + classpath cho EvoSuite
     evo  : thêm thư viện test EvoSuite cần (mục 6)
3. mỗi seed:
     a. EvoSuite sinh test trên orig
     b. gộp scaffolding vào lớp test (mục 6.2)
     c. arrow xác minh 5 bước trên evo (mục 7)
     d. so baseline gốc với baseline của evo (mục 7)
     e. nếu VALID: đo JaCoCo, PIT, tsDetect (mục 8)
4. xóa bản chép, mirror, thư mục làm việc
```

**Lệnh EvoSuite** (JDK của dự án, timeout 20 phút):

```bash
java -cp evosuite-1.2.0.jar org.evosuite.EvoSuite -class <FQN> -projectCP <class production>:<classpath module> \
     -seed <k> -Dtest_dir=<thư mục ra>/tests -Dreport_dir=<thư mục ra>/report   [-Dsearch_budget=<giây>]
```

- `-projectCP` chỉ gồm đường dẫn có thật; EvoSuite dừng nếu có mục không tồn tại.
- Thư mục class production lấy theo dự án: thường `target/classes`, nhưng có pom đặt `<outputDirectory>` khác
  (ví dụ `src/main/webapp/WEB-INF/classes`), phải đọc từ log build.
- Kết quả: `<Lớp>_ESTest.java` và `<Lớp>_ESTest_scaffolding.java`. Không có file test → `NO_OUTPUT`.

## 6. Môi trường cho bộ EvoSuite ✅ (chốt 26/11/2026, ghi là ngoại lệ trong RP2)

Thiết kế cũ chỉ cho thêm runtime EvoSuite ở phạm vi test; chạy thật cho thấy thiếu, nên ngày 26/11 chốt thêm JUnit 4.12, vintage và listener theo hướng dẫn chính thức của EvoSuite.

### 6.1. Thư viện thêm vào bản chép `evo` (scope test, chỉ bản chép này)

| Thư viện | Khi nào | Vì sao |
|---|---|---|
| `org.evosuite:evosuite-standalone-runtime:1.2.0` | luôn | ✅ thiết kế đã cho phép |
| `junit:junit:4.12` | module không có JUnit 4, hoặc JUnit 4 < 4.12 | ✅ EvoSuite **chỉ sinh JUnit 4** và runtime không kèm JUnit. Thiếu → `package org.junit does not exist` (10/68 lớp ở lần chạy đầu). JUnit 4.5 chưa có `@Rule` mà bộ EvoSuite dùng |
| `org.junit.vintage:junit-vintage-engine` cùng phiên bản platform (1.x.y → 5.x.y) | module chạy JUnit Platform (JUnit 5) mà chưa có vintage | ✅ thiếu thì Surefire không chạy test JUnit 4 |
| Listener `org.evosuite.runtime.InitializingListener` trong Surefire | chỉ Maven + provider JUnit 4 | ✅ khuyến nghị của EvoSuite khi chạy chung với test có sẵn. **Không** thêm cho TestNG (Surefire đưa nó cho TestNG → JVM test sập), JUnit 5, Gradle |

### 6.2. Gộp scaffolding

`arrow gen --c0` chỉ nhận **một lớp**. ✅ Chép thân lớp `<Lớp>_ESTest_scaffolding` vào lớp `<Lớp>_ESTest`, bỏ
`extends …_scaffolding`, thêm import còn thiếu. Arrow đổi tên lớp thành `<Lớp>_ArrowTest` khi xác minh.

## 7. Xác minh ✅

- **5 bước, cùng arrow với ARROW:** `arrow gen --project <bản evo> --c0 <bộ đã gộp> --no-repair`.
  Các bước: biên dịch → được phát hiện → thực thi ≥ 1 phương thức → test đích đạt → không hồi quy trong module.
- **Kiểm lại trước khi ghi REGRESSION:** chạy riêng mỗi test có sẵn bị fail 3 lần trên bản không có test mới.
- ✅ **So baseline gốc với baseline bản `evo`:** thư viện thêm vào có thể tự làm hỏng test có sẵn. Test đạt ở bản gốc mà
  mất ở bản `evo` → `DEP_BREAKS_BASELINE` → tính **REGRESSION**.

**Lưu ý về REGRESSION của EvoSuite:** ở bản dựng thử, khoảng **30% lớp** ra REGRESSION, không phải ngoại lệ nhỏ như
pilot cũ (6 bộ). Nguyên nhân: bộ EvoSuite chạy với `separateClassLoader = true`, và classloader riêng này rò sang các
test có sẵn chạy sau trong cùng JVM:

- Spring: `… is not assignable to interface ApplicationContextInitializer` (`Failed to load ApplicationContext`);
- Mockito: `ClassCastException occurred while creating the mockito mock`.

Test có sẵn chạy riêng thì đạt. Thêm listener cũng không hết lỗi. Đây là hành vi thật của EvoSuite, giữ là REGRESSION.
✅ Chốt 26/11/2026: vẫn tính là REGRESSION trong số chính; thêm một dòng mô tả phụ tách nhóm này theo loại lỗi trong `regressions` và giải thích nguyên nhân.

## 8. Đo trên bộ VALID

| Chỉ số | Cách đo |
|---|---|
| Dòng, nhánh, phương thức (JaCoCo 0.8.12) | ✅ Thiết kế: đo trên bản sao `separateClassLoader = false`. Với `true`, agent JaCoCo không thấy lớp (đo ra 0%). ✅ Chạy riêng bộ đó bằng JUnitCore với JDK dự án, cùng option agent (`includes=<FQN>:<FQN>$*`) và cách đọc CSV như arrow |
| Điểm đột biến, test strength (PIT 1.17.4, `DEFAULTS`) | ✅ Đo trên **bộ gốc** (`true`), vì bản `false` làm PIT báo "0 test". Cùng tham số PIT của arrow, nhưng **minion chạy bằng JDK của dự án** (`--jvmPath`): runtime EvoSuite 1.2.0 không chạy trên JDK 11 với lớp JDK 8 (`Cannot load/analyze class java.util.SubList`), mà arrow luôn chạy PIT bằng JDK ≥ 11 |
| Mật độ smell (tsDetect) | arrow đo khi xác minh |
| Số phương thức test, số assertion | arrow (`count_assertions` của java-helper) |

Chỉ số của một lớp = **trung vị qua các seed cho bộ VALID**; ghi kèm số seed VALID trên 5.

## 9. Ghi kết quả: mỗi (lớp, cấu hình, seed) một dòng

| Nhóm | Trường |
|---|---|
| Định danh | `task_id`, `dataset`, `class_fqn`, `config` (default / budget), `seed`, `machine`, `tool_commit` |
| Môi trường | `build_tool`, `jdk`, `framework`, `vintage`, thư viện đã thêm, `listener`, file đã vá |
| Sinh | `gen_seconds`, `gen_exit`, `gen_timed_out`, `search_budget`, lý do nếu `NO_OUTPUT` |
| Xác minh | `final_state`, các bước, số test, số assertion, `regressions`, so baseline (`SAME` / `DEP_BREAKS_BASELINE`) |
| Chất lượng | `line`, `branch`, `method`, `mutation`, `test_strength`, `smell_density` |
| Lưu bộ test | 2 file gốc của EvoSuite và bộ đã gộp (kèm sha256) |

## 10. Chia máy (10 phần: Dung 4, 6 phần còn lại cho 4 máy)

Chia 268 lớp thành **10 phần: Dung 4 phần (107 lớp), 6 phần còn lại (161 lớp) chia cho 4 máy, Chính ít hơn một chút: Chính 38, Kiều Anh, Quang, Hán mỗi máy 41** (chốt 26/11/2026). Xếp lớp theo thứ tự `sha256` cố định rồi giao lần lượt cho máy đang thiếu nhiều nhất so với tỷ lệ, nên mỗi máy nhận đủ ba bộ dữ liệu. Cả ba hệ thống của một lớp chạy trên cùng máy.

| Máy | Lớp | Trong đó EvoSuite chạy được (JDK ≤ 11) |
|---|---:|---:|
| Dung | 107 | 100 |
| Chính | 38 | 32 |
| Kiều Anh | 41 | 39 |
| Quang | 41 | 38 |
| Hán | 41 | 37 |
| **Tổng** | **268** | **246** |

Không chạy EvoSuite cùng lúc với TN2 trên cùng máy: EvoSuite tốn CPU, còn TN2 đo thời gian sửa.

## 11. Phát hiện lỗi thật (GitBug-Java) ✅

Chép bộ EvoSuite VALID (sinh trên bản sửa) vào bản lỗi (bản sửa + `git apply -R bug.patch`), build, chạy riêng bộ đó:
có test fail = phát hiện; mọi test đạt = không phát hiện; không biên dịch được = báo cáo riêng.
✅ Chốt 26/11/2026: mỗi lớp **một bộ, của seed nhỏ nhất cho bộ VALID**; kết quả trên mọi seed VALID chỉ để mô tả phụ. Bản lỗi phải nằm ngoài repo git khác (ví dụ `git init` tạm),
nếu không `git apply -R` có thể lặng lẽ không làm gì.

## 12. Tham khảo từ bản dựng thử (đã xóa 26/11/2026)

48 lớp đủ 5 seed trên máy Dung (cách dựng v2/v3), chỉ để tham khảo, **không dùng làm kết quả**:

| Kết quả cả 5 seed | Số lớp |
|---|---:|
| VALID | 21 |
| REGRESSION | 15 |
| NO_OUTPUT (EvoSuite không sinh được: JDK 17, lỗi ASM, lỗi khởi tạo static của lớp, lỗi nội bộ EvoSuite) | 6 |
| RUNTIME (ví dụ runtime không gắn được agent ByteBuddy) | 2 |
| Khác / kết quả khác nhau giữa các seed | 4 |

23/48 lớp có ít nhất 1 seed VALID. Ở 3 lớp chạy khói, mutation 0,72–1,0.
Script và kết quả: `~/ARROW_backups/tn3-ban-dung-truoc-20261126/` (`tn3_evosuite.py`, `gop_scaffolding.py`,
`tn3_common.py`, `results/dung/evosuite_runs.jsonl`).

## 13. Đã chốt (26/11/2026)

| # | Việc | Quyết định |
|---|---|---|
| 1 | Thư viện thêm vào bản chép (mục 6.1) | runtime + JUnit 4.12 + vintage + listener; ghi ngoại lệ RP2 |
| 2 | "EvoSuite chạy được" (lớp lọc 1) | JDK ≤ 11 → 188 / 33 / 25 lớp |
| 3 | Đo JaCoCo/PIT (mục 8), so baseline bản vá (mục 7) | Như mục 7, 8; ghi ngoại lệ RP2 |
| 4 | REGRESSION do classloader của EvoSuite | Tính REGRESSION; tách mô tả phụ |
| 5 | Seed | 1, 2, 3, 4, 5 |
| 6 | Phát hiện lỗi thật | Seed nhỏ nhất cho bộ VALID |
