# Phản hồi phản biện chính sách preflight Class2Test

- Người gửi: `dungng2808`
- Người nhận: `CuuTroHan`
- Thời điểm: 21/09/2026, 22:46 (Asia/Ho_Chi_Minh)
- Tài liệu được phản hồi: `20260921-2219-CuuTroHan-to-dungng2808-phan-bien-preflight-class2test.md`

## Kết luận

Nhóm chấp nhận các vấn đề P0 về khóa revision, probe và định nghĩa API. Các
góp ý này cần được đưa vào đặc tả trước khi dùng preflight làm đầu vào chính
thức cho experiment. Các góp ý về build theo module, phân loại lỗi và xác minh
regression cũng được chấp nhận.

Ba điểm dưới đây cần được làm rõ để không mở rộng vai trò của preflight quá
mức hoặc làm runner không thể chạy ở quy mô lớn.

## Phạm vi của revision chưa xác minh

Đồng ý rằng một entry không có commit SHA không được diễn giải là revision gốc
của mẫu. Do đó, entry đó không được xuất hiện trong manifest dùng để báo cáo
kết quả experiment tái lập được.

Tuy nhiên, entry có thể được chạy trong chế độ chẩn đoán `DISCOVERY_ONLY` nếu
runner ghi rõ revision đã chọn, phương pháp chọn revision và trạng thái
`REVISION_UNVERIFIED`. Mục đích của chế độ này chỉ là đo khả năng truy cập,
build và cấu trúc hiện tại của repository; kết quả không được trộn vào strict
cohort, số liệu paper hoặc manifest `ELIGIBLE` chính thức. Cách tách này cho
phép kiểm tra dataset trước mà không tạo cảm giác rằng revision đã được tái lập.

## `ELIGIBLE` đo khả năng kỹ thuật, không đo giá trị logic

Đồng ý sửa `api_count`: chỉ đếm constructor và method truy cập được từ package
của generated test, xét visibility của class/member, default constructor,
inherited member và static member. Static `private` không được tính. Probe là
bắt buộc trong strict cohort; nếu không chạy probe thì trạng thái là
`PRECHECKED`, không phải `ELIGIBLE`.

Điểm cần giữ là preflight không nên dùng một ngưỡng “logic đủ phức tạp” để loại
DTO/POJO. Điều đó biến bước kiểm tra khả thi thành một bộ lọc chất lượng chủ
quan và có thể làm lệch benchmark. DTO/POJO có API truy cập được vẫn được nhận
với tag `LOW_LOGIC`; chất lượng của test sinh ra sẽ được đo ở giai đoạn coverage,
mutation và test smell.

## Dependency database và service ngoài

Đồng ý rằng `DriverManager.getConnection(...)` hoặc import JDBC không tự nó
chứng minh mọi method của CUT không unit test được. `DB_DEPENDENT` và
`EXTERNAL_DEPENDENT` sẽ là tag quan sát được; `INTEGRATION_ONLY` chỉ được gán
khi có quy tắc có thể kiểm tra hoặc bằng chứng chạy thực tế.

Runner không nên yêu cầu đánh giá thủ công cho mọi entry vì dataset có thể lớn.
Thay vào đó, trường hợp không thể kết luận bằng AST, dependency graph và probe
sẽ nhận `NEEDS_REVIEW`. Việc đánh giá thủ công chỉ áp dụng cho các entry đó hoặc
một mẫu audit đã được xác định trước.

## `VALID` thuộc pha hậu sinh test

Đồng ý baseline, discovery, regression, flaky test và timeout phải được định
nghĩa trước khi báo `MODULE_TESTS_PASSED (VALID)`. Tuy nhiên, đây là hợp đồng
của pha validation sau khi sinh test, không phải điều kiện chặn preflight
scanner. Preflight vẫn có thể tạo danh sách `ELIGIBLE` sau khi đã khóa revision,
build module và chạy probe; validation sẽ dùng baseline đã lưu để kết luận
`VALID` ở bước sau.

## Các thay đổi cam kết

1. Tạo locked input manifest, bao gồm source JSON/path hash, repository URL,
   commit SHA, CUT path/FQN, phương pháp xác minh revision và trạng thái
   `REVISION_UNVERIFIED` khi cần.
2. Chốt một định nghĩa thực thi được cho `ELIGIBLE`, `PRECHECKED`,
   `probe_status` và `api_count`.
3. Ghi working directory, module path, JDK, wrapper/build-tool version, command,
   exit code và log cho từng build attempt; bổ sung `BUILD_TIMEOUT` và
   `DEPENDENCY_UNAVAILABLE`.
4. Gộp các bảng loại/gắn nhãn theo thứ tự ưu tiên; source set là bằng chứng
   chính, còn tên `*Test`, `*Tests` và `*IT` chỉ là tag hoặc quy tắc cohort được
   thống kê riêng.
5. Xác minh `class_fqn` với package/declaration ở revision đã khóa.
6. Viết đặc tả validation riêng cho baseline, test discovery, regression, flaky
   test và timeout trước khi công bố chỉ số `VALID`.

## Kết quả

Phản biện được chấp nhận về mặt kỹ thuật. Các làm rõ nêu trên không phủ nhận
vấn đề được chỉ ra, mà giới hạn rõ trách nhiệm của preflight: xác định tính khả
thi kỹ thuật và lưu bằng chứng tái lập được; không tự kết luận chất lượng logic
của CUT hoặc thay thế pha validation sau sinh test.
