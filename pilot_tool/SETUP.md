# Cài đặt máy, chia shard và gom kết quả

**Trạng thái:** thiết kế (12/10/2026). Pilot được chia cho **6 máy**; mỗi máy chạy một phần (shard), cuối cùng gom lại để tính chi phí và chọn N. Mọi màn hình chạy trong terminal (Spectre.Console). Số liệu trong các màn hình là ví dụ.

## 1. Hai loại cấu hình

Để 6 máy chạy **cùng một thực nghiệm**, cấu hình được tách làm hai:

| | Cấu hình thực nghiệm | Cấu hình máy |
| --- | --- | --- |
| File | `pilot_tool/experiments/pilot-20261015.json` | `pilot_tool/machine.local.json` |
| Commit lên Git | ✅ (mọi máy dùng chung) | ❌ (`*.local.*`, mỗi máy một bản) |
| Nội dung | Manifest và checksum, model và tham số suy luận, version và hash prompt, `N_max`, quy tắc chọn N, timeout, cờ build, seed | Người chạy, shard được giao, thư mục làm việc, JDK, Maven/Gradle, cách xử lý repo, số worker, địa chỉ LiteLLM proxy |
| Ai sửa | Người điều phối, trước khi chia shard | Từng người qua `setup` |

Màn hình `setup` chỉ **hiển thị** cấu hình thực nghiệm, không cho sửa. Khi chạy, tool so hash cấu hình thực nghiệm với hash ghi trong file phân công shard; lệch thì từ chối chạy. Như vậy không máy nào chạy với model, prompt hay N khác máy khác.

Khóa API và endpoint của provider là của **từng máy** (mỗi người một khóa, chốt 12/10/2026), lưu trong `litellm.config.local.yaml` (Mục 5, bước 6), không nằm trong hai file trên.

**6 máy (chốt 12/10/2026):** Dũng-mac (người điều phối), Dũng-win, Kiều Anh, Quang, Chinh, Hán.

## 2. Menu chính

Chạy `arrow` (hoặc `dotnet run --project src/Arrow.Cli`) không kèm lệnh sẽ mở menu:

```
╭─ ARROW pilot ─ máy: Dũng (macOS arm64) ─ shard: pilot-6/shard-01 ─────────────────────╮
│ Cấu hình máy: ✔ đã có     Kiểm tra môi trường: ✔ 15/10 08:55     LiteLLM: ● đang chạy   │
╰────────────────────────────────────────────────────────────────────────────────────────╯
 Chọn việc (↑/↓, Enter):
 > Chạy shard của tôi            (S0 → S5, tiếp tục nếu đã chạy dở)
   Xem trạng thái run
   Tạm dừng / tiếp tục run đang chạy
   Cài đặt máy (setup)
   Kiểm tra môi trường (doctor)
   Xuất kết quả để gửi người điều phối (export)
   ── Người điều phối ──
   Tạo phân công shard (shard plan)
   Gom kết quả các máy (merge)
   Báo cáo pilot (report)
   Thoát
```

Mọi mục trong menu đều có lệnh tương ứng (Mục 8), để chạy không cần menu hoặc ghi vào hướng dẫn.

## 3. Phân công shard

### 3.1. Thư mục `shards/pilot-6/` (commit lên Git)

```
shards/pilot-6/
  README.md          bảng phân công: máy, người, số CUT, số repository
  plan.json          hash cấu hình thực nghiệm, hash 2 manifest, danh sách shard, hash từng shard
  shard-01.json … shard-06.json
```

Mỗi `shard-XX.json` chỉ chứa danh sách task được giao (giống `shards-5/`), không chứa dữ liệu hay đường dẫn máy:

```json
{
  "schema_version": 1,
  "plan_id": "pilot-6",
  "shard_id": "shard-01",
  "owner": "Dũng",
  "experiment_sha256": "…",
  "tasks": [
    {"dataset": "classes2test", "task_id": "51700398_fc0e38f96f8a8eb5", "repo_url": "https://github.com/…", "manifest_sha256": "…"},
    {"dataset": "gitbug-java",  "task_id": "AuthMe-ConfigMe-7bf10c51347958ead6d2_…", "repo_url": "…", "manifest_sha256": "…"}
  ]
}
```

### 3.2. Quy tắc chia (`arrow shard plan`)

1. Gộp 2 manifest pilot (50 Classes2Test + 9 GitBug-Java = 59 CUT).
2. Mỗi repository nằm trọn trong một shard (để clone một lần).
3. Chia đều số CUT và **cân bằng thời gian ước tính** (dựa trên thời gian build của preflight `duration_seconds`), mỗi shard có cả Classes2Test lẫn GitBug-Java. Với 59 CUT và 6 máy: 5 shard 10 CUT, 1 shard 9 CUT.
4. Thuật toán tất định (sắp xếp rồi chia tham lam theo thời gian), cùng đầu vào cho cùng kết quả; `plan.json` ghi hash mọi thứ.

```
arrow shard plan --experiment experiments/pilot-20261015.json --machines 6 \
  --owners "Dũng-mac,Dũng-win,Kiều Anh,Quang,Chinh,Hán" --out shards/pilot-6
```

```
 Shard      Người       CUT  Classes2Test  GitBug-Java  Repository  Thời gian build ước tính
 shard-01   Dũng-mac     10             8            2          10  00:06:40
 shard-02   Dũng-win     10             8            2          10  00:06:35
 shard-03   Kiều Anh     10             9            1          10  00:06:38
 shard-04   Quang        10             9            1          10  00:06:31
 shard-05   Chinh        10             8            2          10  00:06:36
 shard-06   Hán           9             8            1           9  00:06:29
 Tổng                    59            50            9          59
 Ghi shards/pilot-6/ ? [y/n]
```

## 4. Dữ liệu và repository: cần gì trên máy

- **Không cần tải dataset Classes2Test hay GitBug-Java.** Pilot chỉ cần 2 manifest trong `evaluation_manifest/pilot/` (đã có trong Git). Mỗi bản ghi đã có `repo_url` và `checkout_sha`.
- Repository được **tải từ GitHub khi chạy**, đúng commit trong manifest, rồi xóa theo cách xử lý chọn ở `setup`:

| Cách xử lý | Tải | Xóa | Dùng khi |
| --- | --- | --- | --- |
| **Commit cần thiết, xóa sau khi xong** (mặc định) | `git fetch --depth 1 origin <sha>` (chỉ đúng commit, nhỏ và nhanh) | Xóa ngay khi mọi job của repository đó xong | Máy ít dung lượng; pilot |
| Commit cần thiết, giữ lại | Như trên | Không xóa | Muốn chạy lại hoặc gỡ lỗi |
| Toàn bộ lịch sử, giữ lại | `git clone --mirror` | Không xóa | Repository cần lịch sử khi build (ví dụ build gọi `git describe`) |

Repository nào build cần lịch sử git mà bản `--depth 1` không có thì tool tự tải thêm lịch sử cho riêng repository đó và ghi lại.

- Workspace của từng job luôn bị xóa khi job xong; tùy chọn **giữ workspace của job lỗi** để gỡ lỗi.
- Cache Maven (`~/.m2`) và Gradle (`~/.gradle`) dùng chung, không xóa.

## 5. Màn hình cài đặt (`arrow setup`)

Trình cài đặt đi qua 8 bước, có thể quay lại bước trước; lưu ở cuối. Chạy lại `setup` thì các giá trị cũ được điền sẵn.

**Bước 1: Máy này**

```
╭─ Cài đặt 1/8 · Máy này ───────────────────────────────────────────────╮
│ Hệ điều hành: macOS 15.2 · arm64 · 14 nhân · RAM 48 GB                 │
│ Thư mục ARROW: /Users/dungng/FPT/Ki 9/SET490/DoAnARROW/ARROW  ✔ git   │
╰───────────────────────────────────────────────────────────────────────╯
 Máy này là: > Dũng-mac   Dũng-win   Kiều Anh   Quang   Chinh   Hán
```

**Bước 2: Shard**

```
╭─ Cài đặt 2/8 · Shard ─────────────────────────────────────────────────╮
│ Phân công: shards/pilot-6 (6 shard, 59 CUT)  ✔ hash khớp plan.json     │
│ Cấu hình thực nghiệm: experiments/pilot-20261015.json ✔ hash khớp      │
╰───────────────────────────────────────────────────────────────────────╯
 Shard của Dũng: shard-01 (10 CUT: 8 Classes2Test, 2 GitBug-Java)
 Dùng shard này? [Y/n]
```

Chọn mặc định theo tên người ở bước 1; muốn chạy thay người khác thì chọn shard khác (tool ghi lại người chạy thật).

**Bước 3: Thư mục làm việc**

```
╭─ Cài đặt 3/8 · Thư mục ───────────────────────────────────────────────╮
│ Repository và workspace:  …/ARROW/repo      còn trống 53 GB         │
│ Kết quả (runs):           pilot_tool/runs                               │
│ Manifest pilot:           evaluation_manifest/pilot/   ✔ 2 file, hash khớp│
╰───────────────────────────────────────────────────────────────────────╯
 Thư mục repository và workspace (Enter để giữ): …/ARROW/repo
```

Trên Windows nên chọn đường dẫn ngắn (ví dụ `D:\aw`) vì giới hạn độ dài đường dẫn. Tool cảnh báo khi còn dưới 20 GB.

**Bước 4: Repository**

```
 Cách tải và xóa repository:
 > Chỉ commit cần thiết, xóa sau khi xong     (khuyến nghị)
   Chỉ commit cần thiết, giữ lại
   Toàn bộ lịch sử, giữ lại
 Giữ workspace của job lỗi để gỡ lỗi? [y/N]
```

**Bước 5: JDK và build tool**

```
╭─ Cài đặt 5/8 · JDK, Maven, Gradle ────────────────────────────────────╮
│ Tìm trong Java-version/runtime, JAVA_HOME, thư mục JDK hệ thống         │
│  JDK 8   ✔ 1.8.0_504   Java-version/runtime/jdk-8/Contents/Home         │
│  JDK 11  ✔ 11.0.32.1   Java-version/runtime/jdk-11/Contents/Home        │
│  JDK 17  ✔ 17.0.20.1   Java-version/runtime/jdk-17/Contents/Home        │
│  JDK 21  ✔ 21.0.12.1   Java-version/runtime/jdk-21/Contents/Home        │
│  Maven   ✔ 3.9.16      /opt/homebrew/bin/mvn  (dùng khi repo không có mvnw)│
│  Gradle  ✔ 9.1         /opt/homebrew/bin/gradle (dùng khi không có gradlew)│
│  Git     ✔ 2.55.0      core.longpaths: không cần trên macOS             │
╰───────────────────────────────────────────────────────────────────────╯
 Mọi mục đều ✔. Sửa đường dẫn mục nào? (Enter để bỏ qua)
```

Mục nào ✘ thì cho nhập đường dẫn hoặc chỉ dẫn cài (theo `Java-version/README.md`); tool **không tự tải JDK**. Trên Windows có thêm dòng kiểm tra LongPathsEnabled.

**Bước 6: API và LiteLLM**

```
╭─ Cài đặt 6/8 · Gọi model ─────────────────────────────────────────────╮
│ Model của thực nghiệm (không sửa ở đây): gpt-6-luna, reasoning_effort: none│
│ Snapshot phải trả về: gpt-6-luna (đối chiếu model id khi gọi thử)      │
╰───────────────────────────────────────────────────────────────────────╯
 LiteLLM proxy:
 > Tool tự bật proxy khi chạy   (cần Python + litellm; đã tìm thấy litellm 1.91.0)
   Dùng proxy đang chạy sẵn ở địa chỉ khác
 Endpoint của provider (api_base): https://api.openai.com/v1   (Enter để giữ, hoặc nhập địa chỉ khác)
 Khóa API của bạn: ****************************  (đã lưu; Enter để giữ)
 Tên gợi nhớ của khóa (ghi vào kết quả, không ghi khóa): dung-mac
 Thử gọi model một lần? (tốn khoảng 50 token) [Y/n]
   ✔ trả lời sau 1.8s · model id: gpt-6-luna · endpoint: api.openai.com
```

- **Mỗi người một khóa và có thể đổi endpoint** (ví dụ dùng cổng trung gian tương thích OpenAI). Khóa và endpoint ghi vào `pilot_tool/litellm.config.local.yaml` của máy đó (không commit). Màn hình chỉ hiện `****`.
- Model, tham số suy luận và prompt **không** đổi theo máy: chúng nằm trong cấu hình thực nghiệm. Endpoint khác vẫn phải phục vụ đúng model đó: khi gọi thử, `doctor` và mỗi lượt gọi đều đối chiếu **model id do API trả về**; sai model thì báo ✘ và không chạy.
- Kết quả chạy ghi **tên gợi nhớ của khóa** và **tên máy chủ của endpoint** (không ghi khóa, không ghi đường dẫn có chứa token), để biết máy nào gọi qua đâu. Khi gom kết quả, `merge` kiểm tra model id của mọi máy giống nhau; khác nhau thì báo để người điều phối quyết định.
- Mỗi người một khóa nên mỗi máy dùng trọn hạn mức của khóa đó; bước 7 gợi ý số luồng gọi model theo hạn mức nhập vào.

**Bước 7: Tài nguyên**

```
 Số luồng build (gợi ý theo 14 nhân, 48 GB): 3
 Số luồng gọi model (gợi ý: hạn mức chung chia 6 máy): 2
 Timeout build mỗi bước (theo cấu hình thực nghiệm, không sửa): 900s
```

**Bước 8: Xem lại và lưu**

```
╭─ Cài đặt 8/8 · Xem lại ───────────────────────────────────────────────╮
│ Máy          Dũng · macOS arm64                                         │
│ Shard        pilot-6/shard-01 (10 CUT)                                  │
│ Thư mục      repository: ../repo · runs: pilot_tool/runs             │
│ Repository   commit cần thiết, xóa sau khi xong · không giữ workspace lỗi│
│ JDK          8 / 11 / 17 / 21 ✔ · Maven 3.9.16 ✔ · Gradle 9.1 ✔          │
│ Model        gpt-6-luna qua LiteLLM (tự bật) · khóa: khoa-nhom-1         │
│ Worker       build 3 · model 2                                          │
│ Thực nghiệm  pilot-20261015 · N_max 10 · quy tắc N: share95 · prompt v1  │
╰───────────────────────────────────────────────────────────────────────╯
 Lưu vào machine.local.json và chạy kiểm tra môi trường? [Y/n]
```

## 6. Kiểm tra môi trường (`arrow doctor`)

```
 Kiểm tra                                          Kết quả
 Thư mục ARROW, quyền ghi runs/                    ✔
 Cấu hình máy machine.local.json                   ✔
 Cấu hình thực nghiệm, hash khớp plan.json         ✔
 Shard shard-01, hash khớp, task có trong manifest ✔ 10/10
 JDK 8/11/17/21: java và javac đúng phiên bản      ✔
 Maven, Gradle, Git                                ✔
 Dung lượng trống thư mục repository                ✔ 53 GB
 Đường dẫn thư mục repository (không dấu cách)      ✔
 Kết nối GitHub (git ls-remote 1 repository)       ✔
 LiteLLM proxy và gọi thử gpt-6-luna               ✔ 1.8s
 Windows: LongPathsEnabled, core.longpaths         – (macOS)
 Trạng thái: READY
```

`Chạy shard của tôi` chỉ bắt đầu khi `doctor` ra `READY`. Mục nào ✘ thì ghi rõ nguyên nhân và cách sửa, không tự bỏ qua.

Mục ✘ cần nhiều bước (đường dẫn repository có dấu cách/chữ có dấu/ký tự đặc biệt, thư mục ARROW đã đổi tên hoặc chuyển chỗ) được in thành khung "cần làm" dưới bảng: thư mục nào sai, vì sao, từng bước sửa (specs/02 §3b).

## 7. Gom kết quả

Kết quả được chia sẻ qua **một thư mục riêng trên GitHub** (chốt 12/10/2026): `pilot_results/pilot-6/` trong repository ARROW.

1. Mỗi máy chạy xong: `arrow export` tạo `pilot_results/pilot-6/shard-01-Dung-mac.zip`, gồm `runs/<run_id>/` (trừ workspace), `run_config.lock.json` và hash của từng file. Không chứa khóa API hay endpoint đầy đủ. Log build chỉ giữ đầy đủ cho bước lỗi, bước đạt chỉ giữ phần cuối, để file nhỏ.
2. Tool hỏi `Commit và push file này lên GitHub? [Y/n]`: chỉ `git add` đúng file zip của máy mình, commit, `git pull --rebase` rồi push. Không đụng file khác trong repository. GitHub chặn file trên 100 MB: tool báo trước nếu file zip quá 50 MB.
3. Người điều phối (Dũng-mac): `git pull`, rồi `arrow merge --plan shards/pilot-6 --bundles pilot_results/pilot-6/*.zip --out runs/pilot-6-merged`. Tool kiểm tra:
   - cùng hash cấu hình thực nghiệm và cùng hash prompt trên mọi máy;
   - cùng model id do API trả về trên mọi máy;
   - đủ 6 shard, không thiếu task, không trùng task;
   - mọi job đã xong hoặc có lý do (lỗi hạ tầng).
4. `arrow report --run-id pilot-6-merged`: bảng chi phí và thời gian (phần 1) và N (phần 2, tính trên ứng viên thất bại Classes2Test của **cả 6 máy**). N chỉ được tính sau khi gom đủ 6 shard.
5. Commit lên Git: `shards/pilot-6/`, báo cáo `pilot_report.md` và các CSV tổng hợp (cùng các file zip ở bước 2). Không commit `runs/`.

## 8. Lệnh tương ứng

| Lệnh | Việc |
| --- | --- |
| `arrow` | Menu chính |
| `arrow setup` | Cài đặt máy (Mục 5); `--from <file>` để cài không tương tác |
| `arrow doctor` | Kiểm tra môi trường (Mục 6) |
| `arrow shard plan …` | Tạo phân công (người điều phối) |
| `arrow run` | Chạy shard của máy này theo `machine.local.json` |
| `arrow status`, `arrow control …` | Xem trạng thái, tạm dừng, tiếp tục, dừng (xem `CLI_DASHBOARD.md`) |
| `arrow export` | Đóng gói kết quả của máy |
| `arrow merge …` | Gom kết quả (người điều phối) |
| `arrow report …` | Báo cáo pilot |

## 9. Đã chốt (12/10/2026)

| # | Việc | Quyết định |
| --- | --- | --- |
| 1 | 6 máy | Dũng-mac, Dũng-win, Kiều Anh, Quang, Chinh, Hán |
| 2 | Khóa API | Mỗi người một khóa; endpoint của provider sửa được theo máy, model phải đúng snapshot |
| 3 | Người điều phối | Dũng-mac |
| 4 | Nơi chia sẻ kết quả | Thư mục `pilot_results/pilot-6/` trên GitHub (repository ARROW) |
| 5 | Thư mục repository (13/10/2026) | Không dấu cách, không chữ có dấu; mặc định `ARROW/repo`, nếu đường dẫn ARROW có dấu cách thì `~/arrow-repo` (Mac), `C:\arrow-repo` (Windows); hoặc đổi tên thư mục cha cho hết dấu cách. Đường dẫn có dấu cách làm test fail vì môi trường (specs/02 §3b) |
| 6 | Chọn JDK (13/10/2026) | Theo mức Java khai trong file build, quay về JDK của manifest nếu không biên dịch được (specs/02 §3) |

## Ghi chú LiteLLM (13/10/2026)

`litellm.config.local.yaml` phải có dòng `allowed_openai_params: ["reasoning_effort"]` trong `litellm_params` của model (bản do `arrow setup` ghi từ 13/10/2026 đã có). Thiếu dòng này thì LiteLLM 1.91.0 trả HTTP 400 `UnsupportedParamsError` cho `reasoning_effort` và run tự tạm dừng. Máy đã cài trước ngày này: chạy lại bước khóa API của `arrow setup`, hoặc thêm dòng đó dưới `api_key:` (cùng thụt lề). Không dùng `drop_params: true` vì LiteLLM sẽ âm thầm bỏ `reasoning_effort` của cấu hình thực nghiệm.
