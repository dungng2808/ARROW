# Thuật toán Adaptive Repair

Tài liệu này giải thích cách module Adaptive Repair trong `ARROW`
hoạt động. Mục tiêu của repair là sửa **generated test file** để test compile
và chạy được, nhưng không sửa production code, focal class, build file,
dependency hay existing human-written tests.

## 1. Nguyên tắc chính

Adaptive Repair là một vòng lặp **verification-driven**.

Nghĩa là pipeline không hỏi LLM sửa liên tục theo cảm tính. Mỗi candidate mới
phải được verify bằng Maven hoặc Gradle thật, sau đó state machine mới quyết
định:

- giữ candidate;
- rollback về best candidate;
- đổi repair prompt;
- dừng repair;
- hoặc regenerate test mới.

Repair **không dùng numeric error score**.

Các field sau chỉ dùng để report/telemetry, không được dùng để quyết định
progress, rollback hay prompt switch:

- `error_count`
- `compile_errors`
- `test_failures`
- `test_errors`
- `initial_error_count`
- `final_error_count`
- `best_error_count`

## 2. File nào được sửa

Adaptive Repair chỉ được thay thế generated test file của experiment hiện tại.

Được sửa:

```text
runs/<repo_name>/<sample_id>/<agent>/<prompt>/workspace/.../<GeneratedTest>.java
```

Không được sửa:

- focal class;
- production code;
- existing human-written tests;
- `pom.xml`;
- `build.gradle`;
- `settings.gradle`;
- wrapper files;
- dependency configuration.

Nếu LLM trả về diff, build-file change, nhiều file, sai package, sai class name
hoặc output không phải Java test class hợp lệ, candidate bị reject.

## 3. Failure states

Sau mỗi lần Maven/Gradle verify, log parser trả về `VerificationResult`.

State có thể là:

```text
COMPILE_FAILED
TEST_DISCOVERY_FAILED
RUNTIME_FAILED
ASSERTION_FAILED
TARGET_TEST_PASSED
MODULE_TESTS_FAILED
MODULE_TESTS_PASSED
BUILD_TIMEOUT
TOOL_ERROR
UNKNOWN_FAILED
```

Ý nghĩa ngắn gọn:

- `COMPILE_FAILED`: generated test hoặc module compile fail.
- `TEST_DISCOVERY_FAILED`: build tool không tìm/chạy được generated test.
- `RUNTIME_FAILED`: test crash runtime, ví dụ exception ngoài assertion.
- `ASSERTION_FAILED`: test chạy được nhưng assertion fail.
- `TARGET_TEST_PASSED`: generated target test đã pass.
- `MODULE_TESTS_FAILED`: target pass nhưng module suite có failure mới.
- `MODULE_TESTS_PASSED`: target pass và module suite pass.
- `BUILD_TIMEOUT`: Maven/Gradle timeout.
- `TOOL_ERROR`: lỗi tool/build runner, không phải lỗi Java repair được.
- `UNKNOWN_FAILED`: fail nhưng parser không phân loại chắc chắn.

## 4. Baseline verification

Trước khi thêm generated test, pipeline chạy baseline module tests.

Mục đích:

- biết project/module đã fail sẵn hay chưa;
- phân biệt lỗi do generated test gây ra với lỗi có sẵn;
- tránh gọi LLM repair khi lỗi không thuộc generated test.

Nếu target generated test pass nhưng module suite fail, pipeline so sánh với
baseline:

- failure đã có trong baseline: attribution là `EXISTING_PROJECT`;
- failure mới sau khi thêm generated test: attribution là `GENERATED_TEST`;
- không phân loại được: attribution là `UNKNOWN`.

Repair chỉ nên chạy bình thường khi failure origin là:

```text
GENERATED_TEST
UNKNOWN
```

Nếu origin là existing project/build configuration/infrastructure thì pipeline
dừng và ghi report, không retry LLM mù.

## 5. Best candidate

`best_candidate` không có nghĩa là candidate có ít compile errors nhất.

`best_candidate` là generated test mới nhất đã được verify là có tiến bộ thật.

Candidate được cập nhật thành best khi:

- state transition là progress;
- same-state nhưng có partial progress;
- target test pass;
- module tests pass.

Candidate không bao giờ thành best khi:

- regression;
- repeated error;
- repeated code;
- invalid LLM output;
- timeout;
- tool error;
- no progress sau khi hết patience;
- chưa verify bằng build tool.

Best candidate được lưu trên disk:

```text
repair/best_candidate.java
repair/best_verification.json
repair/best_candidate_metadata.json
```

Mỗi repair prompt mới luôn bắt đầu từ best candidate, không bắt đầu từ candidate
vừa làm xấu đi.

## 6. State transition rules

### Progress

Những transition sau được coi là tiến bộ:

```text
COMPILE_FAILED -> TEST_DISCOVERY_FAILED
COMPILE_FAILED -> RUNTIME_FAILED
COMPILE_FAILED -> ASSERTION_FAILED
COMPILE_FAILED -> TARGET_TEST_PASSED

TEST_DISCOVERY_FAILED -> RUNTIME_FAILED
TEST_DISCOVERY_FAILED -> ASSERTION_FAILED
TEST_DISCOVERY_FAILED -> TARGET_TEST_PASSED

RUNTIME_FAILED -> ASSERTION_FAILED
RUNTIME_FAILED -> TARGET_TEST_PASSED

ASSERTION_FAILED -> TARGET_TEST_PASSED

TARGET_TEST_PASSED -> MODULE_TESTS_PASSED
MODULE_TESTS_FAILED -> MODULE_TESTS_PASSED
```

Ví dụ:

```text
COMPILE_FAILED -> ASSERTION_FAILED
```

Nghĩa là code đã compile được và test đã chạy tới assertion. Đây là tiến bộ,
dù assertion vẫn fail.

### Regression

Những transition sau là xấu đi:

```text
TEST_DISCOVERY_FAILED -> COMPILE_FAILED

RUNTIME_FAILED -> COMPILE_FAILED
RUNTIME_FAILED -> TEST_DISCOVERY_FAILED

ASSERTION_FAILED -> COMPILE_FAILED
ASSERTION_FAILED -> TEST_DISCOVERY_FAILED
ASSERTION_FAILED -> RUNTIME_FAILED

TARGET_TEST_PASSED -> COMPILE_FAILED
TARGET_TEST_PASSED -> TEST_DISCOVERY_FAILED
TARGET_TEST_PASSED -> RUNTIME_FAILED
TARGET_TEST_PASSED -> ASSERTION_FAILED

MODULE_TESTS_FAILED -> target-test failure state
MODULE_TESTS_PASSED -> any failure state
```

Nếu regression xảy ra:

1. rollback về best candidate;
2. không cập nhật best candidate;
3. đổi repair prompt ngay.

## 7. Same-state rules

Nếu state trước và state sau giống nhau, repair không dùng số lượng lỗi để
quyết định. Nó dùng signature và code hash.

### REPEATED_CODE

Nếu generated Java code hash đã từng thử:

- không chạy Maven/Gradle nữa;
- tính là một LLM attempt;
- không tính là build attempt;
- rollback về best candidate;
- switch prompt.

### REPEATED_ERROR

Nếu normalized primary error signature không đổi:

- rollback về best candidate;
- switch prompt;
- ghi `repeated_error_signature=true`.

### PARTIAL_PROGRESS

Cùng state nhưng được coi là tiến bộ một phần khi:

- primary signature cũ không còn trong danh sách signature mới;
- primary signature mới khác primary signature cũ;
- code không bị lặp;
- không có regression rõ ràng.

Lúc này candidate mới được giữ và cập nhật best candidate.

### NO_PROGRESS

Nếu cùng state, code khác, nhưng lỗi có ý nghĩa vẫn còn:

- tăng `no_progress_count`;
- khi đạt `no_progress_patience`, rollback về best candidate;
- switch prompt.

## 8. Repair prompt selection

Prompt order được cấu hình trong `config/pipeline.yaml`.

Ví dụ:

```yaml
adaptive_repair:
  prompt_order_by_state:
    COMPILE_FAILED:
      - compile-error-focused
      - dependency-aware
      - minimal-test
    ASSERTION_FAILED:
      - minimal-test
      - dependency-aware
      - compile-error-focused
```

Prompt ban đầu phụ thuộc failure state.

Pipeline switch prompt khi:

- regression;
- repeated error signature;
- repeated generated-code hash;
- invalid LLM output;
- no-progress patience reached;
- current prompt đã dùng hết `max_attempts_per_prompt`.

Prompt mới nhận best candidate đã được restore, không nhận candidate fail vừa
gây regression.

## 9. Checkpoint

Trước mỗi repair attempt, pipeline tạo checkpoint:

```text
runs/<repo_name>/<sample_id>/<agent>/<prompt>/repair/checkpoints/attempt_<N>/
```

Mỗi checkpoint gồm:

```text
generated_test_before.java
verification_before.json
build_output_before.txt
repair_prompt.txt
llm_response.txt
generated_test_after.java
verification_after.json
build_output_after.txt
decision.json
```

`decision.json` ghi:

- attempt number;
- previous state;
- new state;
- previous/new signature;
- candidate hash before/after;
- best candidate hash;
- decision;
- rollback có xảy ra không;
- prompt có switch không;
- build có bị skip không;
- failure origin;
- reason;
- timestamp.

Rollback dùng Python copy file từ:

```text
repair/best_candidate.java
```

Không dùng Git restore/reset.

## 10. Repair loop

Luôn chạy theo thứ tự:

1. Generated test ban đầu đã được tạo và ghi vào workspace.
2. Chạy target generated test.
3. Nếu target pass, chạy module test suite.
4. Nếu module pass, dừng với success.
5. Nếu fail do generated test, bắt đầu repair.
6. Lấy best candidate hiện tại.
7. Build repair prompt theo failure state và context liên quan.
8. Gọi LiteLLM.
9. Validate Java output.
10. Nếu code hash đã thử, skip build và switch prompt.
11. Ghi candidate vào generated test file.
12. Chạy Maven/Gradle target test.
13. Nếu target pass, chạy module tests.
14. Classify transition bằng state machine.
15. Progress/partial progress thì cập nhật best candidate.
16. Regression/repeated/no-progress thì rollback.
17. Lặp tiếp cho đến khi pass hoặc hết budget.

## 11. Regeneration fallback

Nếu tất cả repair prompts fail và config cho phép:

```yaml
allow_regenerate_after_prompt_fallback_fail: true
max_regenerate_attempts: 1
```

Pipeline gọi LLM để generate lại test mới.

Regeneration khác repair:

- không dùng broken candidate làm template chính;
- dùng focal/project context;
- kèm danh sách failed signatures ngắn gọn;
- yêu cầu model không lặp lại API/pattern đã fail.

Regenerated candidate vẫn phải qua validation và Maven/Gradle verification như
bình thường.

## 12. Counters

Các counter quan trọng:

- `repair_attempts`: số lần gọi LLM bằng repair prompt.
- `regeneration_attempts`: số lần generate lại từ đầu.
- `total_llm_attempts`: repair + regeneration.
- `build_attempts`: số lần chạy Maven/Gradle trong repair.
- `prompt_switch_count`: số lần đổi repair prompt.
- `rollback_count`: số lần rollback về best candidate.
- `no_progress_count`: số lần không có tiến bộ.

Repeated code:

- tính vào LLM attempt;
- không tính vào build attempt vì build bị skip.

Timeout/tool error:

- không gọi LLM để repair Java;
- áp dụng retry policy của build/tool;
- hết retry thì stop.

## 13. Context gửi cho LLM

Repair prompt chỉ nhận context liên quan:

- focal class source;
- current best generated test;
- package name;
- required generated class name;
- Java version;
- detected test framework;
- module path;
- relevant dependencies;
- normalized error signature;
- primary error lines;
- public constructors/methods;
- một số existing tests liên quan nếu có;
- compact failed signature history.

Không gửi toàn bộ repository cho LLM.

## 14. Output report của repair

Trong `result.json` và `experiments.jsonl`, các field repair quan trọng gồm:

```text
initial_failure_state
final_failure_state
best_candidate_state
initial_failure_origin
final_failure_origin
repair_attempts
regeneration_attempts
total_llm_attempts
build_attempts
repair_status
initial_repair_prompt_strategy
final_repair_prompt_strategy
prompt_switch_count
rollback_count
repeated_error_signature
repeated_code_detected
no_progress_count
repair_stopped_reason
regenerated_after_repair_fail
best_candidate_hash
checkpoint_directory
```

`repair_status` có thể là:

```text
NOT_NEEDED
REPAIRED
REGENERATED
FAILED
TIMEOUT
TOOL_ERROR
INVALID_OUTPUT
EXISTING_PROJECT_FAILURE
ATTRIBUTION_FAILED
```

## 15. Ví dụ ngắn

### Case 1: Compile fail thành assertion fail

```text
Attempt 0: COMPILE_FAILED
Attempt 1: ASSERTION_FAILED
Decision: PROGRESS
Action: keep candidate, update best_candidate
```

### Case 2: Assertion fail thành compile fail

```text
Attempt 0: ASSERTION_FAILED
Attempt 1: COMPILE_FAILED
Decision: REGRESSION
Action: rollback best_candidate, switch prompt
```

### Case 3: LLM trả lại code y hệt

```text
Attempt 1: candidate_hash already seen
Decision: REPEATED_CODE
Action: skip Maven/Gradle, rollback, switch prompt
```

### Case 4: Target pass nhưng module fail do lỗi có sẵn

```text
Target: TARGET_TEST_PASSED
Module: MODULE_TESTS_FAILED
Baseline comparison: failure existed before generated test
Origin: EXISTING_PROJECT
Action: stop, no LLM repair
```
