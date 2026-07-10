"# Workspace ARROW

Khoá workspace này chứa pipeline ARROW để phân tích repository, sinh test bằng LLM và chạy build Maven/Gradle tự động.

## Chuẩn bị dữ liệu classes2test

Thư mục classes2test không được cung cấp sẵn trong repo này. Bạn có thể lấy bộ dữ liệu từ repository chính thức:

- GitHub: [lopsandrea/classes2test](https://github.com/lopsandrea/classes2test)
- Tải ZIP trực tiếp: [classes2test-main.zip](https://github.com/lopsandrea/classes2test/archive/refs/heads/main.zip)

Để clone trực tiếp vào workspace với đúng tên thư mục, chạy:

```bash
git clone https://github.com/lopsandrea/classes2test.git
```

Nếu bạn tải về dưới dạng zip hoặc folder khác, hãy giải nén và đổi tên thư mục thành:

```text
classes2test
```

Pipeline sẽ đọc dữ liệu từ thư mục này để chạy. Nếu thư mục không tồn tại hoặc tên khác, chương trình sẽ không hoạt động đúng như mong đợi.

