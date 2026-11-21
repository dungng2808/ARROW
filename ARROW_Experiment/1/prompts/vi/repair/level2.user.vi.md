Lớp test dưới đây chưa qua bước kiểm tra của ARROW. {{failure_summary}} Yêu cầu này có thêm thông tin về lỗi và về các lần sửa trước (nếu có).

Lớp test cần trả về
- Package: {{test_package}}
- Tên lớp: {{test_class_name}}

Thông tin dự án:
<project>
{{repository_configuration}}
</project>

Lỗi:
<error>
{{error_block}}
</error>

Thông tin thêm cho lỗi này:
<extra>
{{extra_context}}
</extra>

Lớp test hiện tại:
<test>
{{test_source}}
</test>

Lớp được kiểm thử {{cut_fqn}} (file {{cut_path}}):
<source>
{{cut_source}}
</source>

Các lần sửa trước đã thất bại (mỗi mục ghi vòng sửa, mã lỗi tóm tắt, lỗi đầu tiên và phần mã đã đổi). Không lặp lại các thay đổi này:
<attempts>
{{failed_attempts}}
</attempts>

Trả về toàn bộ mã Java đã sửa của {{test_class_name}}. Câu trả lời phải khác lớp test hiện tại và khác mọi lần sửa đã thất bại ở trên; trả lại mã trùng là phí một lần sửa. Chỉ trả về mã Java, không có ``` và không giải thích.
