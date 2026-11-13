"""Tổng hợp bằng chứng P0/P1 vào báo cáo tiếng Việt, không chạy pipeline dataset."""
from __future__ import annotations
import ast
import hashlib
import json
from pathlib import Path
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
PYTHON = ROOT.parent / "preflight_tool"


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def test_runs(prefix):
    rows = {}
    for path in sorted((ROOT / ".work/test-results").glob(prefix + "*.trx")):
        document = ET.parse(path)
        ns = {"t": "http://microsoft.com/schemas/VisualStudio/TeamTest/2010"}
        stats = document.find("t:ResultSummary/t:Counters", ns).attrib
        definition = document.find("t:TestDefinitions/t:UnitTest", ns)
        assembly = definition.attrib.get("storage", path.name) if definition is not None else path.name
        rows[assembly] = (path.name, stats)
    return list(rows.values())


def mapping(file, name):
    exact = {
        "test_each_preflight_status_is_selectable_on_its_own": "PolicyTests.EachPreflightStatusIsSelectableOnItsOwn",
        "test_primary_status_uses_specified_priority": "GoldenTests(primary_status): mọi cặp xung đột",
        "test_technical_and_strict_eligibility_formula": "GoldenTests(eligibility), PolicyTests.EligibleDiscoveryIsTechnicalButNeverStrict",
        "test_unknown_status_is_rejected_by_policy": "PolicyTests.UnknownStatusIsRejected",
        "test_jsonl_csv_round_trip_and_nested_data": "OutputTests.JsonlCsvRoundTripAndNestedData",
        "test_output_is_stably_key_sorted_and_does_not_overwrite_content": "OutputTests.OutputIsStablyKeySortedAndByteIdenticalToPython",
        "test_normalization_preserves_comment_markers_inside_java_strings": "PortedHelperTests.NormalizerPreservesEscapesAndCommentMarkersInsideStrings, GoldenTests(normalize_java)",
        "test_normalization_handles_escaped_quotes_and_unterminated_comments": "PortedHelperTests.NormalizerPreservesEscapesAndCommentMarkersInsideStrings, GoldenTests(normalize_java)",
        "test_revision_map_requires_pinned_sha_provenance_and_evidence": "GoldenTests(revision_map)",
        "test_invalid_revision_map_is_rejected": "GoldenTests(revision_map)",
        "test_load_revision_map_none_returns_empty_dict": "RevisionMapTests.NoneReturnsEmptyMap",
        "test_task_id_is_stable_and_sensitive_to_identity": "PortedHelperTests.TaskIdIsStableAndSensitiveToIdentity, GoldenTests(task_id)",
        "test_build_failure_classification": "GoldenTests(failure_status), PortedHelperTests.FailureClassifierHandlesMissingLogAndPrecedence",
        "test_gradle_java_target_normalizes_1_8": "PortedHelperTests.GradleTargetNormalizesVersions",
        "test_java_target_and_workspace_path_security": "GoldenTests(java_target/path_candidates), PortedHelperTests.UnsafePathsAreRejected",
        "test_path_candidates_reject_unsafe_paths_on_every_platform": "PortedHelperTests.UnsafePathsAreRejected",
        "test_path_candidates_normalize_separators_and_contain_symlinks": "PortedHelperTests.EmptyAndNormalizedRelativePathsMatchPython, ResolvePathContainsJunctionEscapes",
        "test_test_framework_detection": "GoldenTests(framework)",
        "test_probe_source_covers_constructor_static_and_instance_calls": "PortedHelperTests.ProbeSourceCoversConstructorStaticAndInstance, GoldenTests(probe_source)",
        "test_process_runner_covers_success_missing_tool_and_timeout": "ProcessRunnerTests.SuccessKeepsPythonLogFormatAndOriginalCommand/MissingExecutableHas127AndMarker/TimeoutStopsChildProcessAndPreservesPartialOutput",
        "test_process_runner_cancellation_kills_process_and_keeps_log": "ProcessRunnerTests.CancellationStopsTreeAndKeepsLog",
        "test_timeout_stops_child_process_on_host_platform": "ProcessRunnerTests.TimeoutStopsChildProcessAndPreservesPartialOutput; kiểm PID và < 8 giây",
        "test_class_policy_source_set_exclusions": "PolicyTests.ExcludedDeclarationsReceiveReasonCodes, GoldenTests(class_policy)",
        "test_revision_sha_must_be_exactly_40_ascii_hex": "RevisionMapTests.ShaMustBeExactly40AsciiHex, GoldenTests(revision_map)",
        "test_conflicting_duplicate_revision_rejected": "GoldenTests(revision_map)",
        "test_identical_duplicate_and_uppercase_sha_are_safe": "GoldenTests(revision_map)",
    }
    if name in exact:
        return "Đã port", exact[name]
    if file.name == "test_class_policy.py":
        target = "PolicyTests.ExcludedDeclarationsReceiveReasonCodes" if "excluded_declarations" in name else "PolicyTests.AcceptedTagsAreNotAutomaticExclusions"
        return "Một phần", target + "; quyết định policy với JavaClass giả đã kiểm; parse/gắn tag từ source thuộc P2"
    partial = {
        "test_tags_and_probe_arguments_cover_supported_types": "PortedHelperTests.ProbeArgumentsCoverAllPrimitiveTypes; AST/tag thuộc P2",
        "test_revision_map_csv_and_pinned_missing_commit": "GoldenTests(revision_map) và CSV multiline/BOM; xác minh commit Git thuộc P3",
        "test_valid_bom_json_hash_and_unicode_path_are_indexed": "PortedHelperTests.BomAndRawByteHashArePreserved; SQLite index thuộc P5",
        "test_invalid_json_and_missing_required_fields_are_reported": "GoldenTests(record_identity); bảng input_rejections thuộc P5",
        "test_unsafe_raw_paths_rejected_before_candidate_creation": "GoldenTests(record_identity), PortedHelperTests.UnsafePathsAreRejected; ghi index thuộc P5",
        "test_relative_paths_remain_accepted": "GoldenTests(record_identity/path_candidates); index thuộc P5",
        "test_gradle_subproject_command_and_java_target": "GoldenTests(java_target); BuildPlan/Gradle command thuộc P3",
    }
    if name in partial:
        return "Một phần", partial[name]
    if name == "test_process_runner_handles_timeout_before_process_creation":
        return "Chưa port", "Monkeypatch subprocess.Popen ném TimeoutExpired không có nhánh tương đương trong Process.Start; đã kiểm pre-cancel, missing executable và timeout thật"
    if name == "test_preflight_and_strict_schemas_accept_contract_record":
        return "Chưa port", "P0 kiểm đủ trường/default/enum bằng OutputTests; validator schema toàn bộ output thuộc P4, không thêm package JSON Schema ngoài phạm vi"
    if file.name == "test_java_ast.py":
        return "Chưa port", "Parser/tree-sitter thuộc P2; dừng trước P2 theo prompt"
    if file.name in {"test_checkpoint.py", "test_repo_cleanup.py", "test_shards.py"}:
        return "Chưa port", "Checkpoint/runner/cleanup/shard thuộc P4"
    if file.name == "test_split_dataset.py":
        return "Chưa port", "Lệnh split-dataset thuộc P4b"
    if file.name == "test_ingest.py":
        return "Chưa port", "Index/gom class/limit/dataset adapter thuộc P5"
    if file.name == "test_input_validation_regressions.py":
        return "Chưa port", "CLI execution boundary chưa cài ở P0/P1; luật input/map đã kiểm riêng"
    return "Chưa port", "Git/build detector/JDK execution/runner/API kế thừa thuộc P3/P4"


def main():
    full = json.loads((ROOT / ".work/golden-full/manifest.json").read_text(encoding="utf-8"))
    subset = json.loads((ROOT / "tests/golden/manifest.json").read_text(encoding="utf-8"))
    deterministic = sha(ROOT / ".work/golden-manifest-first.json") == sha(ROOT / "tests/golden/manifest.json") and sha(ROOT / ".work/golden-full-manifest-first.json") == sha(ROOT / ".work/golden-full/manifest.json")
    status = subprocess.run(["git", "-c", f"safe.directory={ROOT.parent.as_posix()}", "-C", str(ROOT.parent), "status", "--short"], capture_output=True, text=True, encoding="utf-8", check=True).stdout
    lines = ["# Báo cáo P0–P1 Preflight .NET", "", "Ngày 13/11/2026. Đã triển khai phạm vi P0/P1 và dừng trước P2. Chưa chạy preflight trên dataset.", "",
        "## 1. Code đã tạo", "", "```text", "Prelight_Net/", "  PreflightNet.sln, global.json, Directory.Build.props, nuget.config", "  preflight.config.example.json, README.md", "  src/Preflight.Models/         enums, ClassCandidate, BuildAttempt, PreflightResult, JavaClass/JavaApi, config", "  src/Preflight.DataAccess/     PythonJson, ManifestWriter, ConfigLoader, RevisionMapReader", "  src/Preflight.Service/        Core/Common, PyCompat, Policy, Probe, Jdk, Build; Classes2TestRecordReader", "                               External/ProcessRunner + WindowsProcessJob", "  src/Preflight.Cli/            usage, chưa có logic lệnh", "  tests/Preflight.Service.Tests/", "  tests/Preflight.DataAccess.Tests/", "  tests/golden/                tập con cố định, manifest/checksum", "  tools/                       dotnet-local.ps1, process_fixture.py, golden/export_*.py, report_p0_p1.py", "  .work/                       golden đầy đủ, fixture, test results, verification", "  .nuget/                      package/cache cục bộ", "  file.md/BAO_CAO_P0_P1.md", "```", "",
        "Sáu project đúng hướng Cli → Service → DataAccess và các tầng dùng Models. Target net8.0, SDK 10.0.403 qua rollForward, nullable và warnings-as-errors. Package chỉ Microsoft.Data.Sqlite 8.0.22, xUnit 2.9.2, runner VSTest 2.8.2 và Microsoft.NET.Test.Sdk 17.11.1. Chưa cài parser/native DLL, SQLite index hay checkpoint.", "",
        "## 2. Build và test", "", "Build Release cuối: **thành công, 0 warning, 0 error**. CLI `--help` chạy được, không gọi pipeline dataset.", "", "| Bộ kiểm | Artifact | Pass | Fail | Skip |", "| --- | --- | ---: | ---: | ---: |"]
    for prefix in ("final-subset", "final-full"):
        for name, stats in test_runs(prefix):
            lines.append(f"| {prefix} | `.work/test-results/{name}` | {stats.get('passed', 0)} | {stats.get('failed', 0)} | {int(stats.get('total', 0)) - int(stats.get('executed', 0))} |")
    lines += ["", "Các test tiến trình dùng fixture Python của tool, không dùng repo Java/dataset. Timeout/hủy 1 giây có assert tổng thời gian < 8 giây và kiểm PID con đã dừng; `.cmd` có đường dẫn Unicode/khoảng trắng và tham số `&` cũng được kiểm.", "",
        "Trong quá trình kiểm có lỗi đã xử lý:", "", "- Restore trong sandbox: `error NU1301: Unable to load the service index for source https://api.nuget.org/v3/index.json.`; tải package lại bằng quyền mạng đã được duyệt, cache nằm trong Prelight_Net.", "- Test process trước khi bổ sung Job Object: `Kill tree took 00:00:30.2307793`. Cả Kill(entireProcessTree) và taskkill riêng lẻ không dừng hết chuỗi launcher trên máy này. Bản cuối dùng Job Object Windows, không giữ cách taskkill thử nghiệm; test cuối pass quanh 1 giây.", "- Có lần build gặp DLL đang bị testhost trước giữ; đã đợi testhost kết thúc và build lại sạch. Không đổi setting để bỏ qua warning/error.", "",
        "## 3. Golden và độ trùng", "", f"Hai lần xuất cùng seed 42: **{'SHA-256 giống hệt' if deterministic else 'CHƯA KHỚP — cần xử lý'}** cho manifest tập đầy đủ và tập con; tất cả file được ràng buộc bằng SHA-256 trong manifest.", "", "| Hàm | Tổng mẫu đầy đủ | Trùng / tổng | Mẫu giữ trong tests/golden |", "| --- | ---: | ---: | ---: |"]
    for filename, item in sorted(full["files"].items()):
        name = filename.removesuffix(".jsonl")
        evidence_path = ROOT / ".work/verification/full" / (name + ".json")
        evidence = json.loads(evidence_path.read_text(encoding="utf-8")) if evidence_path.exists() else {}
        matched = evidence.get("matched", 0)
        valid = evidence.get("golden_sha256") == item["sha256"]
        lines.append(f"| `{name}` | {item['count']:,} | {matched if valid else 'CHƯA KIỂM'}/{item['count']:,} | {subset['files'][filename]['count']:,} |")
    total = sum(item["count"] for item in full["files"].values())
    lines += ["", f"Tổng **{total:,}** cặp input/output hoặc loại ngoại lệ. Không có mismatch còn mở trong các mẫu đã kiểm. Task ID gồm toàn bộ 85.819 CUT ở năm manifest gốc và 3 ca biên. Failure classifier đọc {full['real_logs']:,} log thật, xét hai stage; cộng các ca marker/timeout sinh tự động.", "",
        f"**Thiếu dữ liệu kiểm chứng Java target:** chỉ đọc được **{full['real_build_files']} file build thật** từ các mirror/FETCH_HEAD và workspaces còn sót; có 22 cây build tổng hợp, tạo 60 ca v1/v2 và 30 ca release. Chưa đạt mục tiêu 3.000 file build thật của plan. Không nhân bản mẫu để giả đạt mốc, không clone/fetch thêm. Đây là giới hạn bằng chứng, không được coi như đã đạt gate 3.000 mẫu.", "",
        f"Tập đầy đủ khoảng {sum(v['bytes'] for v in full['files'].values()) / 1024**2:.1f} MiB nằm tại `.work/golden-full/`; tập con tại `tests/golden/` khoảng {sum(v['bytes'] for v in subset['files'].values()) / 1024**2:.1f} MiB, dưới 50 MB. SHA và số dòng đầy đủ được giữ trong manifest tập con. Script import hàm Python gốc; không sao chép logic làm oracle. Python 3.11.9, tree-sitter 0.24.0, tree-sitter-java 0.23.5; không gọi parser P2.", "",
        "`PythonUnicodeData.Generated.cs` sinh từ re của Python 3.11, khóa tập `\\w`/`\\d` cả BMP và supplementary; có golden riêng cho Deseret, ký tự toán học, CJK ngoài BMP và ký tự Unicode mới mà Python 3.11 chưa coi là word. Không dùng tập Unicode của .NET để suy đoán parity.", "",
        "## 4. Ánh xạ toàn bộ test unit/business Python", "", "Số test .NET là số case sau tham số hóa, không tương đương số hàm test Python. Các test cần parser hoặc pipeline chưa được khai là đã port toàn phần.", "", "| File Python / hàm | Trạng thái | Test C# / lý do |", "| --- | --- | --- |"]
    for folder in ("unit", "business"):
        for path in sorted((PYTHON / "tests" / folder).glob("test_*.py")):
            for node in ast.parse(path.read_text(encoding="utf-8")).body:
                if isinstance(node, ast.FunctionDef) and node.name.startswith("test_"):
                    state, target = mapping(path, node.name)
                    lines.append(f"| `{folder}/{path.name}::{node.name}` | {state} | {target} |")
    lines += ["", "Bổ sung riêng cho .NET: config JSON/default/khóa cũ; JSON float/Unicode/CSV multiline; process environment/stdin; pre-cancel; .cmd; Job Object; golden checksum; probe v2 overload/import/nested/type-variable; release v2 và giới hạn ba cha.", "",
        "## 5. Quirk giữ nguyên và điểm plan khác code", "", "- normalize_java vẫn bỏ whitespace trong literal, vẫn giữ escape/comment marker trong chuỗi như Python, gồm U+001C..U+001F.", "- Probe v1 vẫn dùng regex tách generic cũ; v2 giữ erase type, nested type, cast null, type-variable regex `[A-Z][A-Z0-9_]?`, import-line regex cũ và `throws Throwable`. Không tự mở rộng import static ngoài hành vi regex gốc.", "- `_erased_type` với generic không đóng có thể lặp vô hạn ở Python; giữ thuật toán, không truyền các input gây treo vào golden.", "- Maven target chưa resolve `${java.version}`; Java 8 + release chỉ đổi sang 11 trong v2. Không thêm target/framework detector mới.", "- ClassPolicy giữ thứ tự test source → non-production → non-top-level → interface/annotation → enum/record → abstract → generated. Tag DIRECT_CONNECTION không tự là exclusion ở helper này.", "- Failure classifier giữ ưu tiên timeout → missing tool → dependency → JDK → main/test, kể cả marker `no such file or directory` rộng quá mức.", "- Revision map giữ full 40 ASCII hex, lowercase SHA, nguồn/evidence bắt buộc, identical duplicate hợp lệ và conflicting duplicate bị loại.", "- Plan ghi build_tool_version luôn stdout nhưng Python thật lấy dòng thứ ba của log; khi lệnh timeout/missing executable thì marker khác. P0 giữ format log; chưa hard-code giá trị version vì runner thuộc P3.", "- Schema locked_input cũ yêu cầu revision_sha/revision_status/project_id/class_name khác output cli._finalize; không đổi schema hoặc output Python trong P0/P1. Gate schema pipeline còn ở giai đoạn sau.", "- Plan còn mâu thuẫn normalize không clone với GitBug cần mirror, và origin CAT-LM snapshot/GitHub. Chưa chạm các phần đó trong lượt P0/P1.", "- Hai quirk parser (constructor ảo khi mọi constructor private, annotation có chữ private) vẫn là yêu cầu P2; P0/P1 chưa có parser nên không tuyên bố đã kiểm chúng.", "",
        "## 6. Việc chưa làm / giới hạn", "", "- Dừng trước P2: không parser tree-sitter, không native DLL, không AST/tag extraction/API kế thừa. Không tạo IntegrationTests/ParityTests của P3.", "- Không Git mirror/worktree mới, JDK selector thực thi, Maven/Gradle compile, runner/scheduler, checkpoint/resume, adapter đầy đủ hoặc S0. Không chạy preflight/pipeline dataset.", "- Chưa có đủ 3.000 file build thật; cần bổ sung cache/bằng chứng ở lượt sau. Không cần người dùng chốt quyết định CAT-LM/P8 để tiếp tục đánh giá code P0/P1 này.", "- Helpers PyRegex chỉ hỗ trợ các pattern Python được dùng trong phạm vi P0/P1, không phải bản thay thế toàn bộ module re; negated Unicode word/digit class chưa dùng sẽ báo NotSupportedException.", "- Windows .cmd dùng quote từng argument; argument có dấu quote, phần trăm, NUL hoặc xuống dòng bị từ chối thay vì diễn giải qua cmd. Các build command bình thường và đường dẫn có dấu/khoảng trắng đã được kiểm bằng fixture.", "- Chưa chứng nhận Linux/macOS; native Job Object chỉ kích hoạt ở Windows. Không đổi chính sách chọn mẫu hay ghi đè kết quả đã công bố.", "",
        "## 7. Phạm vi ghi file và Git", "", "Mọi code, test, golden, fixture, package, cache và artifact do các lệnh của lượt này chỉ định đều nằm trong Prelight_Net. Python chạy -B; không phát hiện pyc mới trong source Python kể từ lúc bắt đầu. Không sửa .gitignore, dataset, Python gốc, manifest hay kết quả cũ; không commit/push.", "", "Phát sinh ngoài thư mục được quan sát: lần dotnet khởi tạo đầu tiên báo `Installed an ASP.NET Core HTTPS development certificate.`. Đây là tác dụng phụ của SDK ở certificate store người dùng, không phải file ứng dụng; các lần gọi sau đã đặt DOTNET_GENERATE_ASPNET_CERTIFICATE=false và DOTNET_ADD_GLOBAL_TOOLS_TO_PATH=false. Không tự sửa/xóa certificate store. Không khẳng định đã quét toàn bộ filesystem ngoài workspace.", "", "Git status cuối (giữ nguyên các mục ngoài Prelight_Net đã tồn tại trước lượt này):", "", "```text", status.rstrip(), "```", "", "Các mục .gitignore, ARROW_Framework/.../.gradle, bin, build_cache, file.md, pilot_tool đã có trong status ban đầu; lượt này không tạo/sửa chúng. Prelight_Net vẫn chưa được theo dõi bằng Git.", "", "Nên thêm ignore sau khi người dùng quyết định: `.work/`, `.nuget/`, `.cache/`, `bin/`, `obj/`, `.vs/`, `TestResults/`. Không tự sửa .gitignore. `file.md/` và mọi `*.md` đang bị ignore nên README và báo cáo không tự vào Git.", "", "## 8. Lệnh tái lập", "", "Xem README.md. Chạy build/test qua tools/dotnet-local.ps1 để giữ toàn bộ cache/TEMP trong Prelight_Net; exporter dùng venv Python cũ với -B. Muốn chạy full golden đặt PREFLIGHT_GOLDEN_ROOT=.work/golden-full trước dotnet test, sau đó bỏ biến môi trường."]
    (ROOT / "file.md/BAO_CAO_P0_P1.md").write_text("\n".join(lines) + "\n", encoding="utf-8", newline="\n")
    print(json.dumps({"golden_cases": total, "deterministic": deterministic, "report": "file.md/BAO_CAO_P0_P1.md"}))


if __name__ == "__main__":
    main()
