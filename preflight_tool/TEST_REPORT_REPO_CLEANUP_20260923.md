# Kiểm thử tự dọn cache repository — 23/09/2026

## Hành vi đã triển khai

- Runner đếm toàn bộ class được chọn theo repo_url trước khi submit; chỉ coordinator dọn mirror khi mọi future của repo đã trả kết quả, bao gồm việc dọn workspace trong finally. Class chưa submit hoặc đang chờ vẫn được tính, không chỉ class đang chạy.
- Mặc định tự xóa đúng `runs/<run-id>/cache/mirrors/<sha256-repo-url>.git` sau class cuối của repo, kể cả kết quả class là failed/excluded. Không cần chờ toàn bộ shard kết thúc.
- Chỉ dọn cache thuộc run hiện tại, không xóa source repo, dataset, JDK, dependency cache Maven/Gradle, log/report/manifest hoặc cache của lượt trước.
- `--keep-repo-cache` hoặc `[run].keep_repo_cache=true` giữ cache; `--keep-workspaces`/config tương ứng cũng giữ mirror để không làm hỏng workspace debug.
- Workspace còn sót thì giữ mirror với lý do `WORKSPACE_CLEANUP_INCOMPLETE`. Cache path không đúng vị trí thuộc run hoặc là symlink bị từ chối xóa. Permission error được ghi nhận; có retry cho file readonly, chưa chứng nhận Windows native.
- Ghi từng repo vào `reports/repo_cleanup.jsonl`: DELETED, ABSENT, KEPT, FAILED. `summary.repo_cleanup` tổng hợp số lượng; provenance ghi hai tùy chọn keep thực dùng.
- Xóa không đưa vào thùng rác. Muốn chạy/debug lại mirror đã xóa phải clone lại. Kill process/tắt máy có thể để lại cache; chưa có resume hoặc sweep cache cũ.
- Lifecycle cleanup này thuộc `run_all`/CLI; gọi riêng `preflight_one` không đủ thông tin các class còn lại nên không tự xóa mirror.

## Kết quả kiểm thử

**250 passed, 0 failed, 0 skipped**, 6,21 giây. Môi trường macOS ARM64; Python 3.12.13; JDK 8/11/17/21 thật; performance được bật. JAVA_HOME/PATH JDK 17 chỉ áp dụng subprocess test.

- Line coverage: **96,32%**; branch coverage: **90,33%**. Gate 90%/85% đạt.
- 10 case mới trong `tests/unit/test_repo_cleanup.py`: workers 1/3; nhiều class đồng thời cùng repo và class chưa submit; giữ cache/workspace; workspace sót; clone thiếu/partial; external path; symlink; deletion error không làm mất kết quả; readonly callback.
- Mở rộng test integration nhiều CUT cùng repo Git local: mirror bị xóa, source repo không đổi.
- 3 case CLI integration mới với Git thật: default delete, keep-repo-cache, keep-workspaces. Đối soát summary cleanup và file evidence còn nguyên.
- Test bounded submission vẫn đạt giới hạn 2 × workers.

Chỉ xóa mirror fixture trong thư mục test tạm của lượt này. Không chạy cleanup trên cache dataset cũ của người dùng. Không chạy build remote hoặc chứng nhận Windows/Linux.

## Bằng chứng và chạy lại

- [JUnit](runs/qa-repo-cleanup-20260923-final/pytest.xml).
- [Coverage](runs/qa-repo-cleanup-20260923-final/coverage.json).

Từ `preflight_tool` có thể chạy các test không cần JDK:

```text
python -m pytest tests/unit/test_repo_cleanup.py tests/integration/test_git_and_runner.py -k "not fast_run" -ra
```

Suite tổng dùng cách nạp JDK/performance tại TEST_PLAN mục 19.3, chọn output mới. Báo cáo này không thay kết luận về những hạng mục plan còn chưa kiểm chứng.

Đã cập nhật `config.example.toml`, `shards-5/README.md` và cả 5 tài liệu trong `preflight_tool_md/` để kiểm tra cleanup và ghi cache còn sót trong HANDOFF. Chưa commit/push.
