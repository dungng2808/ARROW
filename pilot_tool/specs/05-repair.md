# 05. Fixed Repair và Adaptive Repair

## 1. Nguồn

Spec này theo **`ARROW/Adaptive repair.md`** (viết tắt AR), khớp với bảng Adaptive Repair trong `CLAUDE.md`. Số mục trong ngoặc, ví dụ "(AR 3.1)", là mục của tài liệu đó. Nhóm không dùng lại code cũ: tool .NET là bản triển khai mới theo AR và spec. Chỗ AR không nói rõ, spec tự quyết định và ghi thành điểm chờ xác nhận ở `00-INDEX.md`.

## 2. Đầu vào

### 2.1. Ứng viên ban đầu c0 (AR 8)

- c0 là ứng viên của bước sinh test (single-shot) chưa đạt VALID.
- c0 được **dựng lại đúng như lúc sinh** (cùng bước kiểm tra và chuẩn hóa của `04` Mục 3) rồi **build lại** để lấy trạng thái trước khi sửa.
- **Loại khỏi bước sửa** (ghi lý do vào `repair/…/excluded.json`):
  - output không hợp lệ ở bước sinh (không có mã để sửa);
  - baseline của task hỏng;
  - lỗi pipeline (lỗi hạ tầng ở bước sinh chưa giải quyết);
  - c0 build lại mà đạt VALID.

### 2.2. Điều kiện công bằng (AR 8)

- Mỗi cơ chế chạy trong **workspace sạch riêng**, tạo từ cùng một commit, cùng model và tham số với bước sinh.
- **Thứ tự chạy xen kẽ**: sắp ứng viên theo `(task_id, model, strategy)`; ứng viên thứ lẻ chạy Fixed trước, thứ chẵn chạy Adaptive trước. Ghi `run_order` (`first`/`second`). Lý do: vòng 1 của hai cơ chế dùng cùng prompt, cơ chế chạy sau thường được cache của provider trả lời nhanh hơn.
- Một nhánh (ứng viên × cơ chế) dừng vì lỗi hạ tầng thì được **chạy lại từ đầu** ở lượt sau với `attempt_id` mới; các vòng của lần chạy dở vẫn lưu nhưng không dùng.
- Bảng so sánh (thực nghiệm chính) chỉ tính ứng viên mà **cả hai cơ chế đều chạy xong**.

### 2.3. Tham số

- `n_max`: pilot 10 (chọn N); thực nghiệm chính N (mặc định 5) như nhau cho hai cơ chế.
- **Không dừng sớm** (`early_stop=false`); P được tính lại từ log (AR 6.2).
- Mỗi vòng là một job trong ledger (`stage=repair`, `round=k`); trạng thái nhánh được dựng lại từ các `decision.json` đã ghi, nên tạm dừng/tiếp tục giữa các vòng được.

## 3. So sánh hai bản (AR 3.2)

Theo thứ tự, chỉ xét tiêu chí sau khi tiêu chí trước bằng nhau:

1. Bước xác minh đạt được: `COMPILE < UNKNOWN < DISCOVERY < RUNTIME < ASSERTION < REGRESSION < VALID`.
2. Số test method pass, lấy từ báo cáo của **lần chạy riêng test sinh ra** (`target_test`), không lấy từ lần chạy toàn module.
3. Số assertion (`03` Mục 8).
4. Vòng xuất hiện sớm hơn.

`compare(mới, checkpoint)` trả `Better` (mới hơn ở tiêu chí 1–3), `Worse` (kém hơn ở tiêu chí 1–3) hoặc `Tie` (bằng ở cả 1–3; theo tiêu chí 4, checkpoint là bản sớm hơn nên được giữ).

Thứ tự **nhóm** lỗi không dùng để so sánh.

## 4. Fixed Repair (AR 7)

```
current ← c0
for t = 1 … n_max:
    ct ← LLM(prompt_level1(current, lỗi(current)))
    if ct không hợp lệ:                       # 04 Mục 3
        ghi vòng t (invalid_output); current giữ nguyên; continue    # vẫn tính là một vòng
    build + chạy ct                            # mã trùng vẫn build lại
    ghi vòng t
    if state(ct) = VALID: return thành công ở vòng t
    current ← ct                               # luôn sửa tiếp từ bản mới nhất, kể cả khi tệ hơn
return thất bại
```

- Prompt duy nhất = prompt mức 1 của Adaptive (lỗi + mã test + source focal class). **Vòng 1 của hai cơ chế dùng cùng một prompt.**
- Fixed **không** dừng khi mã hoặc signature lặp lại; không dùng hash, signature, checkpoint để quyết định. Hash, signature vẫn được tính và ghi vào log.

## 5. Adaptive Repair (AR 3, AR 4)

```
best ← c0 ; seen ← {hash(c0)} ; level ← 1 ; đã_thử ← []
for t = 1 … n_max:
    type ← state(best)                                   # prompt luôn theo lỗi của checkpoint
    ct ← LLM(prompt(type, level, best, đã_thử))          # mức 3: ghép method lỗi vào best (Mục 5.3)
    if ct không hợp lệ:
        đã_thử += (t, "output không hợp lệ"); level ← min(level+1, 3); continue
    if hash(ct) ∈ seen:
        đã_thử += (t, "mã trùng");            level ← min(level+1, 3); continue
    seen ← seen ∪ {hash(ct)}
    build + chạy ct → state(ct), signature(ct)
    if state(ct) = VALID: return thành công ở vòng t
    if compare(ct, best) = Better:
        if state(ct) ≠ state(best): level ← 1             # lỗi đổi loại
        best ← ct                                         # đã_thử KHÔNG bị xóa
    else:
        đã_thử += (t, state(ct), signature(ct))
        if compare(ct, best) = Worse hoặc signature(ct) = signature(best):
            level ← min(level+1, 3)                       # rollback hoặc lỗi lặp → leo mức
        # Tie và khác signature → giữ mức
return thất bại (trả về best)
```

### 5.1. Bảng quyết định

| Sự kiện sau vòng t | Checkpoint | Mức vòng sau | Ghi vào `đã_thử` |
| --- | --- | --- | --- |
| VALID | – | – | – |
| Tốt hơn, trạng thái đổi | Bản mới | 1 | Không |
| Tốt hơn, cùng trạng thái | Bản mới | Giữ | Không |
| Ngang, khác signature | Giữ | Giữ | Có |
| Ngang, trùng signature | Giữ | +1 | Có |
| Tệ hơn (rollback) | Giữ | +1 | Có |
| Mã trùng | Giữ | +1, không build | Có ("mã trùng") |
| Output không hợp lệ | Giữ | +1, không build | Có ("output không hợp lệ") |

Mức tối đa 3 và ở lại 3. Đổi nhóm lỗi luôn là đổi trạng thái, nên quy tắc "trạng thái đổi → mức 1" đã bao gồm đổi nhóm.

### 5.2. Prompt theo trạng thái và mức (AR 3.1)

| Trạng thái của checkpoint | Mức 1 | Mức 2 | Mức 3 |
| --- | --- | --- | --- |
| COMPILE | lỗi + mã test + source focal class | mức 1 + thông tin bổ sung + đã thử | viết lại method chứa lỗi biên dịch; không xác định được method → prompt mức 2 |
| UNKNOWN | như trên | như trên (thông tin bổ sung: `[D6]`) | prompt mức 2 |
| DISCOVERY | như trên | như trên | prompt mức 2 |
| RUNTIME | như trên | như trên | viết lại các method fail (theo test report); không xác định được → mức 2 |
| ASSERTION | như trên | như trên | như RUNTIME |
| REGRESSION | như trên | như trên | prompt mức 2 |

Log ghi `requested_level` (theo thuật toán) và `level` (mức của prompt thực sự gửi; mức 3 lùi về 2 thì `level = 2`). Nội dung prompt: `04-llm-and-prompts.md` Mục 6.

### 5.3. Mức 3: ghép method (AR 3.1)

1. Prompt mức 3 đưa toàn bộ checkpoint và tên các method lỗi; yêu cầu viết lại các method đó. **LLM trả về cả class.**
2. Response qua kiểm tra và chuẩn hóa như mọi output (`04` Mục 3); không hợp lệ → output không hợp lệ.
3. ARROW lấy **chỉ các method lỗi** từ response và thay vào checkpoint; **mọi method khác giữ nguyên từng ký tự** như checkpoint.
4. **Import, field và helper method mới** có trong response mà checkpoint chưa có được thêm vào kết quả.
5. Method lỗi không còn trong response → bỏ method đó khỏi kết quả.
6. Ghi `level3_replaced` (tên các method đã thay) và `level3_dropped` (tên các method đã bỏ).
7. Hash, kiểm tra mã trùng và xác minh làm trên **kết quả sau khi ghép**.

**Method bị bỏ** của một nhánh = các test method có trong c0 mà không còn trong bản cuối của nhánh. Phải báo cáo.

## 6. Gọi LLM lỗi tạm thời (AR 4)

Quá tải, rate limit, lỗi mạng, HTTP 5xx: gọi lại tối đa **3 lần**, chờ **20, 60, 120 giây**. Lần gọi lỗi **không tính là một vòng sửa**; số lần gọi lại ghi vào `llm_retries`. Vẫn lỗi thì dừng nhánh (lỗi hạ tầng), chạy lại nhánh ở lượt sau (Mục 2.2).

Build timeout và lỗi công cụ ở một vòng: build lại 1 lần; vẫn lỗi thì dừng nhánh (lỗi hạ tầng) (AR 2.1).

## 7. Log

### 7.1. `round_<t>/decision.json` (một vòng)

```json
{"attempt_id": "a1", "round": 3, "mechanism": "adaptive", "run_order": "first",
 "base_round": 1, "requested_level": 3, "level": 2, "prompt_kind": "level2.discovery",
 "state": "ASSERTION", "group": "ASSERTION", "error_signature": "ASSERTION | AssertionError | testGetOneNotFound",
 "code_hash": "sha256:…", "duplicate_code": false, "invalid_output": false,
 "comparison": "Better", "improved": true, "rollback": false, "repeated_signature": false,
 "level3_replaced": [], "level3_dropped": [], "level_after": 1, "checkpoint_round": 3,
 "tests_passed": 4, "assertion_count": 9, "llm_seconds": 8.7, "build_seconds": 41.2,
 "llm_retries": 0, "llm_tokens": {"prompt": 6120, "completion": 1450}}
```

Fixed: `base_round = t−1` (vòng hợp lệ gần nhất), `level = requested_level = 1`, `prompt_kind = "level1"`, `checkpoint_round = null`; `comparison` vẫn tính so với bản trước để báo cáo.

Mỗi vòng còn lưu `prompt.json`, `response.json`, `cand.java`, log build của lần chạy riêng test sinh ra và lần chạy toàn module.

### 7.2. `<mechanism>/summary.json` (một nhánh)

`attempt_id`, `run_order`, `c0_state`, `final_state`, `success`, `success_round`, `rounds_used`, `repair_seconds` (tổng `llm_seconds + build_seconds`, không gồm tạm dừng), `dropped_methods` (Mục 5.3), bộ đếm: `rollbacks`, `duplicate_code`, `invalid_output`, `repeated_signature`, `level_ups`, `state_changes`, `level3_rounds`.

## 8. Chỉ số RQ2 (AR 8, thực nghiệm chính)

- Repair success rate: tỷ lệ ứng viên đạt VALID trong tối đa N vòng.
- Number of repair attempts: vòng đầu tiên đạt VALID; không thành công tính bằng N.
- Repair time: tổng thời gian gọi LLM + build + chạy test từ vòng 1 tới vòng thành công (hoặc hết N).
- Kèm: tỷ lệ thành công cộng dồn theo vòng, bảng độ nhạy P, số method bị bỏ ở mức 3, thống kê mô tả các cơ chế.

Pilot chỉ dùng số liệu **chung hai cơ chế** (`07`).

## 9. Kiểm thử (unit, không cần build)

Dựng `VerificationResult` giả và `FakeLlm`:

| Vòng | Kết quả | Mong đợi (Adaptive) |
| --- | --- | --- |
| c0 | RUNTIME | checkpoint = c0, mức 1 |
| 1 | COMPILE | Worse → rollback, `đã_thử` +1, mức 2; vòng 2 dùng prompt RUNTIME mức 2, sửa từ c0 |
| 2 | ASSERTION | Better, trạng thái đổi → checkpoint = 2, mức 1, `đã_thử` giữ nguyên |
| 3 | mã trùng vòng 2 | không build, `đã_thử` +1 ("mã trùng"), mức 2 |
| 4 | ASSERTION, cùng signature, cùng số pass và assertion | Tie + trùng signature → mức 3 |
| 5 | (mức 3, ghép method) VALID | thành công ở vòng 5 |

Thêm:
- Tie khác signature giữ mức; output không hợp lệ tính một vòng.
- Mức 3 với DISCOVERY dùng prompt mức 2 và ghi `requested_level=3, level=2`.
- Ghép method: các method khác giữ nguyên từng ký tự; import, field, helper mới được thêm; method lỗi không có trong response bị bỏ.
- Fixed: sửa từ bản mới nhất kể cả khi tệ hơn; build lại mã trùng; output không hợp lệ giữ nguyên bản mới nhất.
- Gọi lại LLM 3 lần không tăng số vòng.
- Dựng lại trạng thái nhánh từ `decision.json` sau khi dừng giữa chừng cho kết quả giống chạy liền.
