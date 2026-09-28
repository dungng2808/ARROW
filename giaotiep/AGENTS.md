# Quy ước giao tiếp và đặt tên file trong `giaotiep/`

> **Phạm vi:** Quy tắc này chỉ áp dụng cho các file trong `giaotiep/` và các thư mục con. Các file mã nguồn và tài liệu ngoài thư mục này không chịu quy tắc này.

## Cấu trúc tên file

Mọi file trao đổi, phản biện, báo cáo hoặc tài liệu trong thư mục này dùng dạng:

```text
YYYYMMDD-HHMM-<nguoi_gui>-to-<nguoi_nhan>-<mo_ta_ngan>.<ext>
```

- `YYYYMMDD-HHMM`: ngày, giờ và phút tạo file theo múi giờ Việt Nam (`Asia/Ho_Chi_Minh`), dùng giờ 24 tiếng.
- `nguoi_gui`: GitHub username của người tạo hoặc gửi file.
- `to`: từ khóa cố định nối người gửi với người nhận.
- `nguoi_nhan`: GitHub username của người nhận; dùng `all` cho tài liệu gửi cả nhóm.
- `mo_ta_ngan`: chữ thường không dấu, các từ cách nhau bằng dấu `-`.
- `ext`: định dạng gốc, ví dụ `.md` hoặc `.pdf`.

Ví dụ:

```text
20260921-2149-dungng2808-to-all-chinh-sach-preflight-class2test.md
20260921-2220-CuuTroHan-to-dungng2808-phan-bien-preflight-class2test.md
```

Khi chuyển tài liệu đã tồn tại vào `giaotiep/`, giữ người tạo và thời điểm tạo có thể xác minh từ lịch sử Git; không ghi người thực hiện thao tác đổi tên thành tác giả. Không sửa hồi tố nội dung trao đổi cũ khi chỉ đổi tên file.

`AGENTS.md` là file quy tắc nên được miễn quy tắc đặt tên trên.
