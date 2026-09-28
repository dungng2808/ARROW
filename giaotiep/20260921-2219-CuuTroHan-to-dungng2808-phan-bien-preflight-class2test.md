# Phản biện chính sách preflight Class2Test

- Người gửi: `CuuTroHan`
- Người nhận: `dungng2808`
- Thời điểm: 21/09/2026, 22:19 (Asia/Ho_Chi_Minh)
- Tài liệu được xét: `20260921-2149-dungng2808-to-all-chinh-sach-preflight-class2test.md`, commit gốc `de15860`
- Phạm vi: rà soát tính nhất quán và khả năng triển khai của **đặc tả**, chưa chạy preflight trên toàn dataset.

## Kết luận

Tài liệu đặt đúng ranh giới giữa *entry đủ điều kiện để sinh test* (`ELIGIBLE`) và *test được sinh hợp lệ* (`VALID`). Chủ trương giữ nguyên dữ liệu đầu vào, checkout revision xác định, ghi log lỗi và không sửa dependency để ép build pass là phù hợp cho benchmark có thể kiểm tra lại. Hai lệnh mẫu `mvn -DskipTests test-compile` và `gradlew testClasses` về cơ bản biên dịch test mà không thực thi test.

Tuy nhiên, **chưa nên dùng nguyên trạng làm hợp đồng đầu vào/đầu ra cho runner**. Có một chỗ thiếu dữ liệu đầu vào và một mâu thuẫn trực tiếp trong điều kiện `ELIGIBLE`; các tiêu chí còn lại cần được định nghĩa chặt hơn để hai lần chạy cho cùng entry không đưa ra quyết định khác nhau.

## P0 — cần giải quyết trước khi triển khai

### 1. Commit SHA chưa có nguồn trong dữ liệu hiện dùng

Phần **Dữ liệu đầu vào** yêu cầu `commit` là full SHA và phần **Chính sách strict** đòi checkout đúng SHA đó. Trong một JSON Classes2Test đang có ở workspace (`DatasetClassToTest/classes2test-main/dataset/100021742/100021742_19.json`), `repository` có URL nhưng không có commit SHA. `RepoGithub/ARROW/shards/clean-samples-seed42/final/eligible_manifest.csv` cũng chưa có cột commit. Đây là bằng chứng đối chiếu ở workspace hiện tại, không phải dữ liệu được version trong repo ARROW này.

**Đề nghị:** thêm bước tạo *locked input manifest* trước preflight, ghi `task_id`, sample ID hoặc path và hash của JSON nguồn, repo URL, full commit SHA, đường dẫn CUT/test, và phương pháp chọn revision. Nếu không truy ra được revision gốc của mẫu, ghi trạng thái riêng (ví dụ `REVISION_UNVERIFIED`) và không diễn giải một commit bất kỳ có file trùng đường dẫn là revision gốc. Không để preflight tự suy ra SHA theo một thuật toán không được ghi lại.

### 2. Probe vừa là tùy chọn vừa là điều kiện bắt buộc

Bước 7 nói **có thể** compile probe, nhưng điều kiện `ELIGIBLE` yêu cầu class được resolve từ test classpath và bảng trạng thái có `PROBE_FAILED`. Nếu bỏ qua probe, đặc tả chưa nêu bằng chứng nào thay thế để thỏa điều kiện này.

**Đề nghị:** làm probe bắt buộc cho strict cohort, trên workspace tạm, đúng module và package sẽ đặt generated test; ghi command, exit code và log. Nếu muốn chế độ nhanh bỏ probe, phải ghi `probe_status=NOT_RUN` và trạng thái riêng như `PRECHECKED`, không gọi là `ELIGIBLE` theo định nghĩa strict hiện tại. Probe cần kiểm tra khả năng tham chiếu CUT, không cần biến một test rỗng thành bằng chứng hành vi có thể test.

### 3. Quy tắc chọn API tự mâu thuẫn và có false positive

Điều kiện “constructor truy cập được, method không phải `private`, **hoặc static method**” có thể nhận cả static method `private`. Một class không khai báo member nào có thể có default constructor, trong khi bảng loại trừ lại loại “empty class”. Việc có constructor/getter/setter cũng chưa chứng minh CUT có logic đáng đo hoặc hành vi quan sát được. Đây là vấn đề định nghĩa benchmark, không phải lỗi Java.

**Đề nghị:** định nghĩa `api_count` là số constructor/method *thực sự truy cập được từ package của generated test*, có xét visibility của class và member, default constructor, inherited method và static method. Quyết định riêng mức tối thiểu về hành vi: nếu DTO không có logic vẫn được nhận thì gắn `LOW_LOGIC`; nếu không, loại bằng quy tắc được viết rõ. Không dùng một danh sách member mơ hồ làm điều kiện đủ.

## P1 — cần chốt để kết quả tái lập được

### 4. `INTEGRATION_ONLY` và khả năng mock chưa có phép kiểm tra xác định

Bảng accept yêu cầu DAO hoặc external client “mock được”, còn bảng loại trừ nói class “bắt buộc” dùng database/service thật. AST và kiểu field/constructor có thể gợi ý dependency, nhưng không tự chứng minh có thể cô lập mọi side effect. Ví dụ `DriverManager.getConnection(...)` là dấu hiệu rủi ro, chưa đủ để kết luận mọi method của class đều không unit test được.

**Đề nghị:** tách `DB_DEPENDENT`/`EXTERNAL_DEPENDENT` (tag quan sát được) khỏi `INTEGRATION_ONLY` (kết luận cần bằng chứng). Nêu rõ phép kiểm tra hoặc mức đánh giá thủ công cho kết luận sau; nếu chưa chắc, dùng `NEEDS_REVIEW` thay vì ép thành eligible hay excluded.

### 5. Lệnh build cần gắn với module và phân loại lỗi

`test-compile` và `testClasses` là ví dụ hợp lệ, nhưng đặc tả chưa chỉ rõ chạy từ thư mục nào, xử lý Maven reactor nhiều module, Gradle project path, wrapper và JDK được chọn ra sao. Một lệnh `test-compile` thất bại cũng chưa đủ để phân biệt `MAIN_BUILD_FAILED` với `TEST_COMPILE_FAILED`. Build timeout và lỗi tải dependency hiện chỉ được nói là “preflight thất bại”, nhưng chưa có mã trạng thái riêng.

**Đề nghị:** ghi chính xác working directory và command theo từng module, gồm cách build các module phụ thuộc (Maven `-pl/-am` khi phù hợp; Gradle `:<module>:testClasses`). Chạy hoặc phân tích log theo hai giai đoạn `compile` và `test-compile` để phân loại lỗi, thêm `BUILD_TIMEOUT` và `DEPENDENCY_UNAVAILABLE`. Lưu phiên bản wrapper/build tool, JDK thực chạy, exit code và log cho mọi attempt.

### 6. `VALID` chưa có quy trình đối chiếu regression

Đoạn cuối yêu cầu generated test được phát hiện, chạy pass và không tạo regression mới. Cần một baseline trước khi chèn test để biết lỗi nào có sẵn. Exit code 0 của toàn module không chứng minh generated test đã chạy nếu bộ lọc discovery không nhận nó.

**Đề nghị:** lưu baseline suite ở cùng commit, cấu hình và môi trường; sau khi chèn test, xác nhận đúng test mới có số lượng test chạy > 0 và pass; so sánh danh sách lỗi trước/sau. Định nghĩa cách xử lý flaky test, baseline thất bại, skipped test và timeout trước khi gắn `MODULE_TESTS_PASSED (VALID)`.

## P2 — làm gọn đặc tả

- Hai phần **Các class bị bỏ qua** và **Chính sách loại và gắn nhãn** lặp nhiều quy tắc. Gộp thành một bảng có thứ tự ưu tiên khi một entry cùng lúc vi phạm nhiều điều kiện.
- Không loại production class chỉ vì tên `*Test`, `*Tests` hoặc `*IT`: tên là tín hiệu, còn source set và khai báo class mới là bằng chứng chính. Nếu vẫn muốn loại theo tên để giữ cohort, nêu đây là quy tắc chọn mẫu và báo số trường hợp bị loại.
- Chỉ dùng `class_fqn` sau khi xác minh nó khớp package/declaration trong source tại commit đã khóa; trường không khớp cần trạng thái riêng hoặc lý do cụ thể.

## Điều kiện để chốt bản sửa

1. Có locked input manifest với SHA đã xác minh hoặc có trạng thái minh bạch cho entry thiếu SHA.
2. Có một định nghĩa duy nhất, thực thi được, cho `ELIGIBLE`, `probe_status` và `api_count`.
3. Có lệnh build theo module cùng sơ đồ phân loại build failure, timeout và dependency failure.
4. Có quy trình baseline/discovery/regression trước khi báo `VALID`.

Nguồn kỹ thuật để đối chiếu lệnh build: [Maven Surefire — Skipping Tests](https://maven.apache.org/surefire/maven-surefire-plugin/examples/skipping-tests.html), [Gradle Java plugin — Tasks](https://docs.gradle.org/current/userguide/java_plugin.html), [Maven multi-project guide](https://maven.apache.org/guides/mini/guide-multiple-subprojects-4.html).
