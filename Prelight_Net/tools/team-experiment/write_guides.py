"""Keep the five standalone handoff documents consistent with the common guide."""
import json
import pathlib

ROOT=pathlib.Path(__file__).resolve().parents[2]
DOC=ROOT/'docs/team-experiment'
COMMON=(DOC/'README.md').read_text(encoding='utf-8')
PLAN=json.loads((DOC/'team-plan.json').read_text(encoding='utf-8'))
MEMBERS=[('may-toi','01_MAY_CUA_BAN.md','Máy của bạn',3,4),('han','02_HAN.md','Hán',3,4),('ka','03_KA.md','KA',4,5),('chinh','04_CHINH.md','Chính',2,3),('dung','05_DUNG_MAC.md','Dũng — Mac M4 Pro',8,10)]
WINDOWS=r'''
```powershell
$member = '{member}'
$workers = {workers}
$cli = 'src/Preflight.Cli/bin/Release/net8.0/preflight-net.dll'
$plan = 'docs/team-experiment/team-plan.json'
$sessionConfig = Get-Content preflight.local.json -Raw | ConvertFrom-Json
$env:JAVA_HOME = $sessionConfig.java.default_home
$env:PATH = (Join-Path $env:JAVA_HOME 'bin') + [IO.Path]::PathSeparator + $env:PATH
foreach ($dataset in @('gitbug-java','catlm','classes2test')) {{
    $run = "Dataset/_team-r1/$member/$dataset"
    $shard = "Dataset/_team-r1/$member/$dataset-shard.json"
    $profile = if ($dataset -eq 'classes2test') {{ 'v1' }} else {{ 'v2' }}
    if (!(Test-Path "$run/state/normalize.json")) {{
        dotnet $cli normalize --dataset $dataset --config preflight.local.json --output-dir $run --probe-profile $profile
        if ($LASTEXITCODE -ne 0) {{ throw "Normalize failed: $dataset" }}
    }}
    if (!(Test-Path $shard)) {{
        python -B tools/team-experiment/team.py shard --plan $plan --dataset $dataset --member $member --index "$run/state/class_index.sqlite" --output $shard
        if ($LASTEXITCODE -ne 0) {{ throw "Pool mismatch: $dataset" }}
    }}
    $complete = (Get-Content "$run/provenance.json" -Raw | ConvertFrom-Json).run_status -eq 'COMPLETED'
    if (!$complete) {{
        if (Test-Path "$run/state/run_state.sqlite") {{
            dotnet $cli resume --output-dir $run --workers $workers
        }} else {{
            dotnet $cli run --output-dir $run --shard $shard --workers $workers
        }}
        if ($LASTEXITCODE -ne 0) {{ throw "Run interrupted: $dataset; inspect and resume." }}
    }}
    if (!(Test-Path "results/team-r1/$member/$dataset/export.json")) {{
        python -B tools/team-experiment/team.py export --plan $plan --dataset $dataset --member $member --run $run
        if ($LASTEXITCODE -ne 0) {{ throw "Export failed: $dataset" }}
    }}
}}
python -B tools/team-experiment/team.py publish --plan $plan --member $member
if ($LASTEXITCODE -ne 0) {{ throw 'Results preserved locally; inspect Git authentication/staging/branch before publishing again.' }}
```
'''
MAC=r'''
```bash
set -e
member=dung
workers=8
cli=src/Preflight.Cli/bin/Release/net8.0/preflight-net.dll
plan=docs/team-experiment/team-plan.json
export JAVA_HOME="$(python3 -c 'import json;print(json.load(open("preflight.local.json"))["java"]["default_home"])')"
export PATH="$JAVA_HOME/bin:$PATH"
for dataset in gitbug-java catlm classes2test; do
  run="Dataset/_team-r1/$member/$dataset"
  shard="Dataset/_team-r1/$member/$dataset-shard.json"
  profile=v2
  if [ "$dataset" = classes2test ]; then profile=v1; fi
  if [ ! -f "$run/state/normalize.json" ]; then
    dotnet "$cli" normalize --dataset "$dataset" --config preflight.local.json --output-dir "$run" --probe-profile "$profile" || break
  fi
  if [ ! -f "$shard" ]; then
    python3 -B tools/team-experiment/team.py shard --plan "$plan" --dataset "$dataset" --member "$member" --index "$run/state/class_index.sqlite" --output "$shard" || break
  fi
  complete=$(python3 -c 'import json,sys;print(json.load(open(sys.argv[1]))["run_status"])' "$run/provenance.json")
  if [ "$complete" != COMPLETED ]; then
    if [ -f "$run/state/run_state.sqlite" ]; then
      caffeinate -i dotnet "$cli" resume --output-dir "$run" --workers "$workers" || break
    else
      caffeinate -i dotnet "$cli" run --output-dir "$run" --shard "$shard" --workers "$workers" || break
    fi
  fi
  if [ ! -f "results/team-r1/$member/$dataset/export.json" ]; then
    python3 -B tools/team-experiment/team.py export --plan "$plan" --dataset "$dataset" --member "$member" --run "$run" || break
  fi
done
python3 -B tools/team-experiment/team.py publish --plan "$plan" --member "$member"
```
'''
for member,file,name,workers,ceiling in MEMBERS:
    counts={d:v['counts'][member] for d,v in PLAN['datasets'].items()}
    header=f'''# Bàn giao riêng cho {name} — slug `{member}`

Gửi nguyên file này cho agent trên máy {name}. Agent chưa cần biết lịch sử chat;
quy trình chung đầy đủ đã được chép bên dưới. Repository:
https://github.com/dungng2808/ARROW.git. Tìm clone hiện có trước, nếu chưa có mới
clone. Giữ source, dataset và output cũ cho tới khi người dùng cho phép dọn.

**Phần riêng:** C2T **{counts['classes2test']}** CUT, GitBug **{counts['gitbug-java']}**,
CAT-LM **{counts['catlm']}**. Bắt đầu **{workers} workers**, trần hiệu chỉnh
**{ceiling}**; đây là số repo song song, không phải cam kết tối ưu cho mọi build.
Làm các bước 1–7 bên dưới trước khi chạy khối lệnh riêng ở cuối file.
Mọi đường dẫn thao tác bắt đầu từ `ARROW/Prelight_Net`, phải tìm đường dẫn thực tế
và cấu hình `preflight.local.json` trên **máy này** trước. Không sử dụng config
có đường dẫn của máy khác. Nhắc dọn pilot cũ trước chạy thật; chưa có chỉ dẫn xóa
thì giữ pilot và dùng thư mục run mới tách biệt.

'''
    tail=f'''

## Lệnh riêng cho {name}: chạy/resume và xuất kết quả

Sau khi setup và gate đã qua, tạo branch `experiment/team-r1-{member}` từ bản
source chung đã chốt: `git switch -c experiment/team-r1-{member}`. Nếu branch
đã có thì kiểm trạng thái rồi dùng branch đó, không ghi đè thay đổi người dùng.
Workers trong config là {workers}; thay `<SLUG>` ở cache bằng `{member}`.
Lưu `results/team-r1/{member}/machine.json` với CPU/RAM/OS/core thực,
workers thực tế, SDK/runtime/JDK/Maven/Gradle full version, SHA native và thời điểm.
Không copy config local vào Git.

Khối lệnh dưới chạy lần lượt GitBug → CAT-LM → C2T; gọi lại để resume khi đã
kiểm tra nguyên nhân ngắt và không còn CLI khác của cùng run. Thay `python`
bằng Python executable đã tìm được nếu máy không dùng tên đó.
Khối lệnh tự commit/push **chỉ output thành viên** sau khi đủ ba dataset và
checksum hợp lệ. Kiểm quyền GitHub, branch và staging trước khi chạy nền.
Nếu push lỗi, output/commit local vẫn giữ; không tự force-push hay bỏ qua secret scan.
'''
    tail+=MAC if member=='dung' else WINDOWS.format(member=member,workers=workers)
    if member=='dung':
        tail+='''
Mac: phải dùng terminal ARM64, clang và dylib ở mục 7, chạy smoke parser + SQLite
trước. Không dùng DLL Windows hoặc build_native Windows. Khởi đầu 8 workers;
chỉ nâng lên min(10, số physical core thực) sau 15–30 phút ổn định và đủ RAM.
Sửa `workers` trong shell để resume, không rebuild/sửa config khi run đang dở.
Nếu gate Mac chưa qua thì báo nhóm, chưa chạy để biến lỗi platform thành lỗi mẫu.
'''
    else:
        tail+=f'''
Ctrl+C giữ checkpoint. Nếu cần đổi workers, đổi `$workers` trong khối lệnh và
gọi lại sau khi CLI cũ dừng, tối đa {ceiling} sau calibration. Không sửa config
hoặc rebuild binary giữa run. Khối lệnh dừng khi có lỗi/thiếu disk, không bỏ qua
mẫu và không chuyển dataset nếu dataset trước chưa hoàn tất.
'''
    tail+=f'''
Ba run phải COMPLETED; kiểm đủ {counts['classes2test']}/{counts['gitbug-java']}/{counts['catlm']}
CUT tương ứng C2T/GitBug/CAT-LM. Tỷ lệ pass lấy trên đủ mẫu đã giao, không lọc lỗi.
Sau khi đã kiểm các file export, từ **root ARROW**:

```text
git add -- Prelight_Net/results/team-r1/{member}/
git diff --cached --stat
git commit -m "Add team-r1 results for {member}"
git push -u origin experiment/team-r1-{member}
```

Chỉ stage thư mục results của mình; không `git add .`, không force push.
Tạo PR vào main bằng web hoặc GitHub CLI đã đăng nhập. Nếu chưa có quyền push,
giữ commit local và báo chủ repo cấp collaborator. Không yêu cầu người dùng
đưa token vào chat. Khi xong báo counts, pass%, thời gian, lỗi hạ tầng, link PR;
không xóa output/checkpoint/pilot nếu chưa có yêu cầu.
'''
    (DOC/file).write_bytes((header+COMMON+tail).encode('utf-8'))
print('Wrote five standalone Vietnamese handoff documents.')
