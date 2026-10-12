# Tiến độ pilot_tool

Bằng chứng cho từng mốc của `ARCHITECTURE.md` Mục 16: lệnh đã chạy, kết quả, run, commit. Máy chạy: Dung-mac (macOS arm64, 14 nhân, 48 GB, JDK 8/11/17/21, Maven 3.9.16, Gradle 9.6.1). Mỗi mục ghi theo thứ tự thời gian; commit hash của một mục nằm ở mục ngay sau (commit chứa chính file này).

## 0. Dữ liệu pilot v2 (13/10/2026)

- Rút 50 CUT dự bị (`evaluation_manifest/pilot/classes2test_pilot_reserve_50_seed42.*`, sha256 `5a76e632…`), chạy S0 cho 50 CUT pilot v1 + 50 dự bị + 9 GitBug-Java: run `pilot-screening` (109 task: 101 READY, 8 INFRA) và `pilot-screening-fix` (4 task chạy lại sau khi sửa lỗi đọc `classname` của Surefire).
- `python scripts/select_pilot_after_s0.py … --name classes2test_pilot_50_seed42_v2` → `OK 50 CUTs: kept 39, replaced 11, reserve left 27; sha256=b44a9d9a…`. Tiêu chí và danh sách thay: `evaluation_manifest/pilot/README.md`, `classes2test_pilot_50_seed42_v2.meta.json`.
- `experiments/pilot-v1.json` trỏ sang v2; `arrow shard plan --experiment experiments/pilot-v1.json --owners "Dung-mac=Dũng-mac,…,Han=Hán" --plan-id pilot-6 --out ../shards/pilot-6 --coordinator Dung-mac -y` → 6 shard, 59 CUT (50 Classes2Test, 9 GitBug-Java).
- Sửa trong lúc sàng lọc: JVM của mọi bước build chạy với `-Duser.language=en -Duser.country=US -Duser.timezone=UTC` (specs/02 Mục 3 bước 6; jitterted-ensembler hết 3 test fail giả); `TestReportParser` lấy tên class từ `testsuite` khi `classname` là tên method (amforeas, cucable); tên test JUnit 5 của Gradle bỏ `()` cuối.

## 1. Nợ M2: fixtures và integration test

- `tests/fixtures/`: `sample-maven-junit4` (1 module, Java 8), `sample-maven-multimodule-junit5` (core + app, Java 17, `-pl app -am`, module app có `<includes>` riêng), `sample-gradle-junit5` (Java 17). Mỗi fixture có 1 test luôn fail (`BrokenTest`) và 1 test fail ở lần chạy đầu của workspace (`FlakyTest`).
- `tests/Arrow.Infrastructure.Tests/PrepareIntegrationTests.cs`: S0 thật trên 3 fixture (fixture được commit thành repository git tạm, tải qua `file://`).
- `dotnet build ArrowRunner.sln` → 0 warning, 0 error. `dotnet test ArrowRunner.sln` → Core 50/50, Infrastructure 3/3 pass.

Commit: `79315db` (M1, M2, nợ M2, pilot v2), đã push lên `main`.

## 2. M3: xác minh 5 bước

- Code: `Arrow.Core/Verify` (`VerificationState`, `VerifyResult` = `verify.json`, `FailureClassifier`: lỗi javac Maven/Gradle, loại lỗi javac cố định, RUNTIME/ASSERTION, regression, signature theo specs/03 Mục 6, D11; `CodeHasher`), `Arrow.Infrastructure/Verify` (`FiveStepVerifier`, `CandidateRunner`: workspace sạch mỗi ứng viên, build lại 1 lần khi timeout/lỗi công cụ/lỗi tải dependency rồi INFRA, `-Dsurefire.includesFile` hoặc init script Gradle khi module không chạy test sinh ra), `Arrow.Infrastructure/Java/JavaAnalyzer` + `tools/java-analyzer` (JavaParser: bỏ comment theo AST, đếm assertion, method chứa dòng lỗi). S0 thêm hậu tố `_ArrowTest2` khi tên trùng file sẵn có.
- `dotnet test tests/Arrow.Infrastructure.Tests` → 14/14 pass (`VerifierIntegrationTests`: lỗi compile trong method → `COMPILE | cannot find symbol | method` + method lỗi; lỗi import → `COMPILE | package does not exist | -`; không có `@Test` và mọi method `@Ignore` → DISCOVERY; NPE từ CUT → `RUNTIME | NullPointerException | Calculator.labelLength`; assertion sai giá trị → ASSERTION (không phải COMPILE); `assertThrows` không ném → ASSERTION; ghi file dùng chung → `REGRESSION | existing tests fail | SharedStoreTest`; test đúng → VALID với 2 assertion; module có `<includes>` riêng → VALID, `module_run_includes_target=false`; Gradle VALID và COMPILE).
- `dotnet test tests/Arrow.Core.Tests` → 76/76 pass (thêm `FailureClassifierTests`, `CodeHasherTests`).
- `dotnet build ArrowRunner.sln` → 0 warning, 0 error.

## Việc còn mở

- Tiêu chí M1 trên Windows (tạm dừng/tiếp tục trên Windows): chưa kiểm được trên máy này.
- `evaluation_manifest/gitbug_java_32_seed42.*` (tập đánh giá GitBug-Java, nguồn rút pilot GitBug-Java) và thay đổi trong `evaluation_manifest/README.md` chưa commit: ngoài phạm vi goal, để người dùng commit.
- Tập đánh giá 200 class chưa sàng lọc S0 theo tiêu chí B (người dùng sẽ chọn lại sau).
