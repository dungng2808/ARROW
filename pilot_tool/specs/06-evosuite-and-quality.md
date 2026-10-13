# 06. EvoSuite và đo chất lượng

## 1. EvoSuite (S3)

Công cụ: `ARROW/tools/evosuite/evosuite-1.2.0.jar`, `evosuite-standalone-runtime-1.2.0.jar` (tải từ release chính thức của EvoSuite, kiểm SHA-256, ghi vào `run_config.lock.json`). EvoSuite 1.2.0 chỉ chạy trên Java ≤ 11: chạy EvoSuite bằng **JDK 11** khi task dùng JDK 8 hoặc 11; task dùng JDK 17/21 → vẫn thử bằng JDK 11 với classpath đã biên dịch; lỗi do phiên bản class (`Unsupported class file major version`) được ghi `evosuite_status=UNSUPPORTED_JDK`, không phải lỗi hạ tầng của tool.

Các bước cho mỗi task:

1. Workspace sạch tại `checkout_sha`; `main_compile` và `test_compile`.
2. Lấy classpath của module: Maven `org.apache.maven.plugins:maven-dependency-plugin:3.8.1:build-classpath -Dmdep.outputFile=target/arrow-cp-test.txt -Dmdep.includeScope=test`; Gradle: init script in `sourceSets.test.runtimeClasspath`. Ghép với `target/classes` (`build/classes/java/main`); `-projectCP` bỏ thư mục class test.
3. Sinh test `[D9]`:

```
java -jar evosuite-1.2.0.jar -class <CUT FQN> -projectCP <classpath> -seed 42 \
     -Dtest_dir=<out> -Dreport_dir=<out>/report \
     -Doutput_variables=TARGET_CLASS,criterion,Coverage,Total_Goals,Covered_Goals,Size,Length,Total_Time
```

   Không đổi `search_budget`, `criterion` hay tham số khác (cấu hình mặc định); ghi lại giá trị mặc định thực tế từ log. Timeout cho cả lệnh: 20 phút.
4. EvoSuite sinh `<CUT>_ESTest.java` và `<CUT>_ESTest_scaffolding.java`. Hai file được ghi vào test path của module, cùng package.
5. **Ngoại lệ đã khai báo:** thêm `evosuite-standalone-runtime-1.2.0.jar` vào scope test, chỉ trong workspace của job EvoSuite: Maven thêm một `<dependency>` scope `system` (`systemPath` trỏ tới jar) vào POM của module; Gradle thêm `testImplementation files('<jar>')` qua init script. Ghi thay đổi này vào `evosuite_stats.json`.
6. Xác minh bằng `FiveStepVerifier` với class test `<CUT>_ESTest` (giống ARROW). EvoSuite sinh test dạng JUnit 4; module dùng JUnit 5 cần `junit-vintage-engine` mới chạy được — **không** thêm; test không được phát hiện thì kết quả là `DISCOVERY` như mọi hệ thống khác.
7. Ghi `evosuite/verify.json`, `evosuite_stats.json` (thời gian sinh, coverage nội bộ của EvoSuite — không dùng thay JaCoCo).

## 2. Đo chất lượng (S4)

Chỉ đo trên bộ test có `state = VALID` (của ARROW: ứng viên VALID ở bước sinh hoặc sau sửa; của EvoSuite). Mỗi lần đo dùng workspace sạch có đúng một file test sinh ra (cộng scaffolding với EvoSuite).

### 2.1. JaCoCo 0.8.12

- Maven: `org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent` + lệnh `target_test` (chỉ chạy class test sinh ra) + `org.jacoco:jacoco-maven-plugin:0.8.12:report`; đọc `target/site/jacoco/jacoco.csv`.
- Gradle: init script áp plugin `jacoco` phiên bản 0.8.12, chạy `<p>:test --tests <T>` rồi `<p>:jacocoTestReport` với CSV bật (`build/reports/jacoco/test/jacocoTestReport.csv`).
- Lấy dòng của CUT (khớp package và class, gồm cả inner class `CUT$…`): line, branch, method coverage = covered / (covered + missed). Mẫu số 0 (ví dụ không có nhánh) → ghi `null`, không ghi 0.
- Cờ `-Djacoco.skip=true` của bước xác minh **không** dùng ở bước này.

### 2.2. PIT 1.17.4

- **Cách chạy (13/10/2026, 00-INDEX P7):** PIT **bản dòng lệnh** (`org.pitest.mutationtest.commandline.MutationCoverageReport`, jar trong `ARROW/tools/pit/`) cho cả Maven và Gradle, không sửa POM, không dùng plugin build. Classpath test lấy từ build tool trên chính workspace đo: Maven `maven-dependency-plugin:3.8.1:build-classpath -Dmdep.includeScope=test` (cộng `target/test-classes`, `target/classes`); Gradle init script task `arrowClasspath` in `sourceSets.test.runtimeClasspath`. Lý do: plugin Gradle `info.solidsoft.pitest` 1.15.0 không chạy trên Gradle 9 (`reporting.baseDir` đã bị bỏ), và một cách chạy cho mọi build tool thì kết quả so sánh được.
- Tham số: `--targetClasses <CUT FQN> --targetTests <T> --sourceDirs <module>/src/main/java --outputFormats CSV,XML --timestampedReports=false --threads 2 --timeoutConst 8000`, chạy bằng JDK của task.
- Module JUnit 5 (classpath có JUnit Platform): thêm `pitest-junit5-plugin` 1.2.1; nếu classpath không có `junit-platform-launcher` (Maven: Surefire tự mang khi chạy test) thì thêm launcher **cùng phiên bản** với `junit-platform-engine` trên classpath (tải từ Maven Central vào `tools/pit/launcher/`). Không thêm plugin JUnit 5 cho module JUnit 4 (plugin đòi JUnit Platform). TestNG cần `pitest-testng-plugin`: chưa cài, pilot không có module TestNG; gặp thì `pit_status=FAILED` có lý do.
- Mutation score theo định nghĩa của PIT: số mutant bị phát hiện (`KILLED`, `TIMED_OUT`, `MEMORY_ERROR`, `RUN_ERROR` như PIT tính) chia tổng số mutant sinh ra cho CUT (bỏ `NON_VIABLE`). Ghi thêm "test strength" = bị phát hiện / số mutant được test phủ (bỏ `NO_COVERAGE`), để tham khảo. Timeout cả lệnh: 30 phút. Lỗi → `pit_status=FAILED` với lý do, không ghi 0. Trên fixture JUnit 4, bản dòng lệnh cho cùng kết quả với `pitest-maven:1.17.4:mutationCoverage` (7 mutant, diệt 5).

### 2.3. tsDetect 2.2

- Chạy `java -jar TestSmellDetector.jar <input.csv>`; `input.csv` mỗi dòng: `app,<đường dẫn test>,<đường dẫn CUT>`.
- Đọc CSV kết quả: mỗi cột smell là số lần phát hiện trong class test. Ghi tổng số smell và theo từng loại.
- Mật độ smell = tổng smell / 1 bộ test (Report 2); ghi kèm số test method và số dòng của file test.

## 3. Đầu ra

`quality/<system>/{jacoco.json, pit.json, tsdetect.json}`:

```json
{"status": "OK", "tool_version": "0.8.12", "line": 0.82, "branch": 0.61, "method": 0.9,
 "seconds": 48.2, "log": "logs/quality_jacoco.log"}
```

Thiếu số đo nào thì `status` khác `OK` kèm `reason`; báo cáo **không** coi thiếu là 0 (Report 2 Mục 7.1).

## 4. Kiểm thử

Fixture Maven JUnit 4, Maven JUnit 5, Gradle JUnit 5: có một test VALID; kiểm JaCoCo ra coverage của đúng CUT, PIT ra mutation score, tsDetect đọc được kết quả. EvoSuite: một fixture JUnit 4 với CUT đơn giản sinh được test và đạt VALID.
