# Phản biện phản hồi về chính sách preflight Class2Test

- Người gửi: `CuuTroHan`
- Người nhận: `dungng2808`
- Thời điểm: 21/09/2026, 23:07 (Asia/Ho_Chi_Minh)
- Tài liệu được xét: `20260921-2246-dungng2808-to-CuuTroHan-phan-hoi-phan-bien-preflight-class2test.md`, commit `c546ffd`
- Đối chiếu với: chính sách gốc `20260921-2149-dungng2808-to-all-chinh-sach-preflight-class2test.md` và phản biện `20260921-2219-CuuTroHan-to-dungng2808-phan-bien-preflight-class2test.md`.

## Kết luận

**Đồng ý với hướng giải quyết của phản hồi.** `DISCOVERY_ONLY` là cách dùng hợp lý cho entry chưa xác minh revision nếu đầu ra được tách khỏi strict cohort và báo cáo experiment. Không nên loại DTO/POJO chỉ vì ít logic; gắn `LOW_LOGIC` giúp giữ tính khả thi làm tiêu chí preflight. Việc dùng tag cho dấu hiệu database/service, giới hạn đánh giá thủ công ở các ca bất định và tách `VALID` sang pha validation cũng đúng phạm vi.

Phản hồi hiện là **cam kết sửa đặc tả**, chưa phải bản đặc tả đã sửa hay bằng chứng runner làm đúng. Trước khi đóng trao đổi, cần chốt bốn điểm dưới đây. Chúng làm rõ cách thực thi những quyết định đã thống nhất, không yêu cầu preflight đo coverage, mutation hoặc test smell.

## 1. Phân biệt “khóa commit để tái chạy” với “xác minh revision gốc của mẫu”

Phản hồi nêu `commit SHA` và “phương pháp xác minh revision” trong locked manifest. Một SHA đầy đủ xác định chính xác mã sẽ build, nhưng tự nó không chứng minh đó là revision đã sinh ra JSON Classes2Test. Việc tìm commit đầu tiên còn tồn tại cả focal path và test path cũng chỉ chứng minh hai file có mặt. Nhiều commit có thể thỏa điều kiện ấy.

**Đề nghị:** khai báo riêng `checkout_sha`, `revision_provenance` và `revision_verification_status`. `revision_provenance` nêu nguồn SHA (metadata upstream, bản ghi dataset, hoặc lịch sử Git); `revision_verification_status` phân biệt ít nhất `UPSTREAM_PINNED`, `CONTENT_MATCHED`, `UNVERIFIED`. Khi đối chiếu nội dung, ghi rõ những đoạn của JSON được so với source/test tại SHA nào, phép chuẩn hóa và mức trùng khớp. `CONTENT_MATCHED` vẫn không được tự gọi là “original revision” nếu không có chứng cứ upstream. Locked manifest cần hash **nội dung file JSON nguồn**, không chỉ hash chuỗi path.

`DISCOVERY_ONLY` có thể ghi `checkout_sha` đã chọn dù `revision_verification_status=UNVERIFIED`; bản ghi này phục vụ chẩn đoán và `eligible=false`. Strict cohort chỉ dùng những mức xác minh đã được định nghĩa trước và công bố.

## 2. Probe phải chứng minh đúng điều kiện mà `api_count` dùng

Phản hồi đồng ý probe bắt buộc và sửa `api_count`; đây là quyết định đúng. Nhưng probe chỉ import hoặc khai báo biến kiểu CUT có thể compile dù constructor/method mà generator cần gọi không truy cập được. Ngược lại, class chỉ có static API truy cập được không cần constructor public.

**Đề nghị:** probe compile lời gọi tới ít nhất một API được `api_count` tính là truy cập được, trong package/module dự kiến cho generated test, và lưu API nào đã được probe. Tách `constructor_count` và `method_count`; chỉ tính member inherited thực sự gọi được, không lấy các method phổ quát của `java.lang.Object` làm bằng chứng duy nhất cho mọi class. Với DTO hoặc class chỉ có constructor mà không có hành vi khác, chấp nhận kèm `LOW_LOGIC`/`CONSTRUCTOR_ONLY` nếu đó là chính sách benchmark; sửa quy tắc “empty class” trong bản gốc để không mâu thuẫn với quyết định này. Probe chỉ chứng minh **khả năng compile lời gọi**, không chứng minh test sẽ pass hoặc dependency có thể mock.

## 3. Các nhãn hiện mô tả những trục khác nhau

`DISCOVERY_ONLY` là chế độ chạy; `REVISION_UNVERIFIED` là tình trạng provenance; `PRECHECKED`, `ELIGIBLE`, `NEEDS_REVIEW` là kết quả kiểm tra; `DB_DEPENDENT` là tag. Nếu dùng tất cả như một cột trạng thái duy nhất, runner dễ ghi đè mất thông tin hoặc để một entry vừa “unverified” vừa “eligible” không rõ nghĩa.

**Đề nghị:** định nghĩa tối thiểu `run_mode`, `revision_verification_status`, `preflight_status`, `tags`, `eligible` và `reason_codes` thành các trường riêng. Ghi quy tắc suy ra `eligible=true` và quy tắc ưu tiên khi có nhiều lỗi. Ví dụ, `run_mode=DISCOVERY_ONLY` luôn cho `eligible=false`, dù build/probe thành công; `NEEDS_REVIEW` không được âm thầm đưa vào strict manifest. Phần output CSV/JSONL và các bảng trạng thái trong chính sách gốc phải dùng cùng schema này.

## 4. `NEEDS_REVIEW` và baseline cần đúng pha

AST, dependency graph và **compile probe** có thể phát hiện dấu hiệu phụ thuộc ngoài, nhưng compile probe không xác nhận database/service có chạy thật hoặc được mock trong test sinh ra. Vì vậy, `INTEGRATION_ONLY` cần một quy tắc tĩnh có bằng chứng rõ hoặc một kiểm tra hành vi phù hợp; khi chưa đủ bằng chứng thì giữ `NEEDS_REVIEW` theo đúng phản hồi. Quy định này cho phép audit có chọn mẫu, không đòi đọc tay mọi entry.

Tôi đồng ý `VALID` thuộc pha hậu sinh test. Cụm “validation sẽ dùng baseline đã lưu” nên được nói rõ: baseline được chạy **trước khi chèn generated test** tại cùng `checkout_sha`, module, JDK và cấu hình, có thể thực hiện ở đầu pha validation. Preflight không cần chờ suite chạy pass để xuất `ELIGIBLE`. Nếu muốn preflight lưu baseline để tái dùng, ghi dấu vết cấu hình và tuổi thọ cache; không dùng baseline từ môi trường khác làm chứng cứ không có regression.

## Quyết định đề xuất

Chấp nhận phản hồi `c546ffd` làm hướng sửa chính sách. Bản `preflight` kế tiếp nên đưa bốn quy tắc trên vào schema và flow cụ thể, kèm một vài ví dụ entry: SHA chưa xác minh, DTO chỉ có accessor, static utility có private constructor, DAO có dependency chưa kết luận được. Sau đó mới xem đặc tả đủ chặt để triển khai runner và báo số lượng strict cohort.
