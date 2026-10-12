"""Re-run failed preflight classes with the probe and JDK-selection fixes, without editing preflight/.

Usage (from ARROW/preflight_tool):
    python scripts/rerun_fixed_probe.py --previous-run runs/gitbug-full-20261011 \
        --config config.local.toml --input-root ../gitbug_input \
        --revision-map ../gitbug_input/revision_map.csv \
        --output-dir runs/gitbug-rerun-20261011 --workers 4

    # Resume an interrupted re-run (the fixes are applied again):
    python scripts/rerun_fixed_probe.py --resume --output-dir runs/gitbug-rerun-20261011

The script patches three functions of preflight.runner at runtime, then calls the
unchanged preflight CLI with a --shard file that holds only the classes to re-run:

1. Probe source: the probe method declares `throws Throwable`, so calling an API that
   throws a checked exception compiles; `null` arguments are cast to the erased
   parameter type when the class has another overload with the same name and arity
   (the probe then copies the CUT's import lines, and nested types are qualified).
2. JDK selection: when the module targets Java 8 but compiles with `--release`
   (`options.release`, `<release>`, `maven.compiler.release`), JDK 11 is used,
   because the javac of JDK 8 has no --release flag.
3. Probe compile (Maven only): code-format and coverage plugins are skipped
   (checkstyle, spring-javaformat, spotless, formatter, jacoco), because they reject
   the generated probe file for reasons unrelated to whether the API is callable.

Classes selected by default: every status except ELIGIBLE and EXCLUDED in the previous
run; with --new-only, only classes of --input-root that the previous run never saw (for
example CUTs added by manual test labels). When --previous-run was itself made by this
script, its merged/ view is used as the previous results. After the run, <output-dir>/merged/ combines the previous ELIGIBLE rows with the
re-run rows; <output-dir>.patches.json (written before the run) records what was patched.
"""
from __future__ import annotations

import argparse
import collections
import hashlib
import json
import re
import sys
import tempfile
import threading
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from preflight import cli, runner  # noqa: E402
from preflight.ingest import build_index, candidates, resolve_dataset_dir  # noqa: E402
from preflight.java_ast import primitive_arguments  # noqa: E402
from preflight.shards import assignment_record  # noqa: E402

PATCH_VERSION = "2026-10-11.1"
KEEP_STATUSES = {"ELIGIBLE", "EXCLUDED"}
PROBE_SKIP_FLAGS = ["-Dcheckstyle.skip=true", "-Dspring-javaformat.skip=true", "-Dspotless.check.skip=true",
                    "-Dspotless.apply.skip=true", "-Dformatter.skip=true", "-Djacoco.skip=true"]
RELEASE_PATTERN = re.compile(r"options\.release|\brelease\.set\(|--release|<release>|maven\.compiler\.release")
TYPE_VARIABLE = re.compile(r"[A-Z][A-Z0-9_]?")
_probe_phase = threading.local()


# --- 1. Probe source -------------------------------------------------------------------

def _split_parameters(parameters: str) -> list[str]:
    content = parameters.strip().removeprefix("(").removesuffix(")").strip()
    if not content:
        return []
    chunks, depth, current = [], 0, ""
    for char in content:
        depth += (char == "<") - (char == ">")
        if char == "," and depth == 0:
            chunks.append(current.strip()); current = ""
        else:
            current += char
    chunks.append(current.strip())
    return chunks


def _erased_type(chunk: str) -> str:
    compact = re.sub(r"@[\w.]+(?:\([^)]*\))?", "", chunk).replace("final ", "").strip()
    type_part = compact.rsplit(" ", 1)[0] if " " in compact else compact
    type_part = type_part.replace("...", "[]")
    while "<" in type_part:
        type_part = re.sub(r"<[^<>]*>", "", type_part)
    return type_part.replace(" ", "")


def _cast_arguments(parameters: str, outer: str, nested: set[str]) -> str:
    values = []
    for chunk in _split_parameters(parameters):
        erased = _erased_type(chunk)
        if erased.split(".", 1)[0].removesuffix("[]") in nested:
            erased = f"{outer}.{erased}"
        primitive = primitive_arguments(f"({erased} x)")
        if primitive != "null":
            values.append(primitive)
        elif TYPE_VARIABLE.fullmatch(erased.removesuffix("[]")):
            values.append("(Object[]) null" if erased.endswith("[]") else "(Object) null")
        else:
            values.append(f"({erased}) null")
    return ", ".join(values)


_original_parse_java_source = runner.parse_java_source


def _parse_java_source(source, *args, **kwargs):
    # Keep the CUT source of this worker thread so the probe can reuse its imports.
    if "expected_name" in kwargs:  # the CUT itself, not a parent class
        _probe_phase.source = source
    return _original_parse_java_source(source, *args, **kwargs)


def _probe_source(java, api, class_name: str, framework: str) -> str:
    package = f"package {java.package};\n\n" if java.package else ""
    source = getattr(_probe_phase, "source", "")
    header = {"junit5": ("import org.junit.jupiter.api.Test;\n\n", "@Test\n    void probe() throws Throwable"),
              "junit4": ("import org.junit.Test;\n\n", "@Test\n    public void probe() throws Throwable"),
              "testng": ("import org.testng.annotations.Test;\n\n", "@Test\n    public void probe() throws Throwable")
              }.get(framework, ("", "void probe() throws Throwable"))
    arity = len(_split_parameters(api.parameters))
    overloaded = sum(1 for other in java.apis
                     if other.kind == api.kind and other.name == api.name
                     and len(_split_parameters(other.parameters)) == arity) > 1
    if overloaded:
        imports = "".join(f"{line.strip()}\n" for line in source.splitlines() if re.match(r"\s*import\s+[\w.*]+\s*;", line))
        package += imports + ("\n" if imports else "")
        nested = set(re.findall(r"\b(?:class|interface|enum|record)\s+(\w+)", source)) - {java.name}
        args = _cast_arguments(api.parameters, java.name, nested)
    else:
        args = primitive_arguments(api.parameters)
    if api.kind == "constructor":
        call = f"new {java.name}({args});"
    elif api.is_static:
        call = f"{java.name}.{api.name}({args});"
    else:
        call = f"{java.name} value = null;\n        value.{api.name}({args});"
    return f"{package}{header[0]}final class {class_name} {{\n    {header[1]} {{\n        {call}\n    }}\n}}\n"


# --- 2. JDK selection ------------------------------------------------------------------

_original_java_target = runner._java_target


def _uses_release_flag(module_root: Path) -> bool:
    for directory in (module_root, *module_root.parents[:3]):
        for name in ("pom.xml", "build.gradle", "build.gradle.kts"):
            path = directory / name
            if path.is_file() and RELEASE_PATTERN.search(path.read_text(encoding="utf-8", errors="replace")):
                return True
    return False


def _java_target(module_root: Path, tool: str) -> str | None:
    target = _original_java_target(module_root, tool)
    if target == "8" and _uses_release_flag(module_root):
        return "11"
    return target


# --- 3. Probe compile flags ------------------------------------------------------------

_original_compile = runner._compile
_original_maven_commands = runner._maven_commands


def _compile(plan, phase, config, task_id, env, attempt_number=1):
    _probe_phase.active = phase == "probe_compile"
    try:
        return _original_compile(plan, phase, config, task_id, env, attempt_number)
    finally:
        _probe_phase.active = False


def _maven_commands(plan, phase):
    primary, primary_cwd, fallback, fallback_cwd = _original_maven_commands(plan, phase)
    if getattr(_probe_phase, "active", False):
        primary = [primary[0], *PROBE_SKIP_FLAGS, *primary[1:]]
        fallback = [fallback[0], *PROBE_SKIP_FLAGS, *fallback[1:]]
    return primary, primary_cwd, fallback, fallback_cwd


def apply_patches() -> None:
    runner._probe_source = _probe_source
    runner.parse_java_source = _parse_java_source
    runner._java_target = _java_target
    runner._compile = _compile
    runner._maven_commands = _maven_commands


# --- Selection, run, merge -------------------------------------------------------------

def _read_jsonl(path: Path) -> list[dict]:
    return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]


def _previous_rows(previous_run: Path) -> list[dict]:
    # A run made by this script keeps its combined view in merged/; a plain preflight run in reports/.
    merged = previous_run / "merged" / "preflight_results.jsonl"
    return _read_jsonl(merged if merged.is_file() else previous_run / "reports" / "preflight_results.jsonl")


def write_shard(previous_run: Path, input_root: Path, statuses: set[str] | None, shard_path: Path,
                new_only: bool = False) -> list[str]:
    previous = _previous_rows(previous_run)
    with tempfile.TemporaryDirectory() as tmp:
        database = Path(tmp) / "index.sqlite"
        build_index(resolve_dataset_dir(input_root), database)
        items = list(candidates(database))
    if new_only:
        known = {row["task_id"] for row in previous}
        wanted = {item.task_id for item in items if item.task_id not in known}
    else:
        wanted = {row["task_id"] for row in previous
                  if (row["preflight_status"] in statuses if statuses else row["preflight_status"] not in KEEP_STATUSES)}
    rows = [assignment_record(item) for item in items if item.task_id in wanted]
    if not rows:
        raise SystemExit("nothing to run: no class matches the selection")
    missing = wanted - {row["task_id"] for row in rows}
    if missing:
        raise SystemExit(f"{len(missing)} task(s) of the previous run are not in --input-root, e.g. {sorted(missing)[0]}")
    shard_path.parent.mkdir(parents=True, exist_ok=True)
    shard_path.write_text(json.dumps({"schema_version": 1, "shard_id": f"rerun-fixed-probe-{previous_run.name}",
                                      "class_count": len(rows), "classes": rows}, indent=2) + "\n", encoding="utf-8")
    return sorted(wanted)


def merge(previous_run: Path, rerun: Path) -> dict:
    previous = _previous_rows(previous_run)
    rerun_rows = _read_jsonl(rerun / "reports" / "preflight_results.jsonl")
    by_task = {row["task_id"]: row for row in previous}
    by_task.update({row["task_id"]: row for row in rerun_rows})
    merged = sorted(by_task.values(), key=lambda row: row["task_id"])
    out = rerun / "merged"
    out.mkdir(exist_ok=True)
    for name, rows in (("preflight_results.jsonl", merged),
                       ("technical_eligible_manifest.jsonl", [r for r in merged if r.get("technical_eligible")]),
                       ("strict_eligible_manifest.jsonl", [r for r in merged if r.get("strict_eligible")])):
        (out / name).write_text("".join(json.dumps(r, ensure_ascii=False) + "\n" for r in rows), encoding="utf-8")
    repo = lambda row: row["repo_url"].split("github.com/")[-1]  # noqa: E731
    eligible = [r for r in merged if r["preflight_status"] == "ELIGIBLE"]
    bugs = collections.defaultdict(set)
    for row in eligible:
        bugs[repo(row)].add(row["checkout_sha"])
    summary = {
        "total": len(merged),
        "eligible_classes": len(eligible),
        "eligible_repositories": len(bugs),
        "repositories_with_two_or_more_eligible_bugs": sum(1 for v in bugs.values() if len(v) >= 2),
        "by_status": dict(collections.Counter(r["preflight_status"] for r in merged).most_common()),
        "rerun": {"classes": len(rerun_rows),
                  "recovered": sum(1 for r in rerun_rows if r["preflight_status"] == "ELIGIBLE"),
                  "by_status": dict(collections.Counter(r["preflight_status"] for r in rerun_rows).most_common())},
        "eligible_classes_by_repository": {k: sum(1 for r in eligible if repo(r) == k) for k in sorted(bugs)},
    }
    (out / "summary.json").write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")
    return summary


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--previous-run", type=Path)
    parser.add_argument("--config", type=Path)
    parser.add_argument("--input-root", type=Path)
    parser.add_argument("--revision-map", type=Path)
    parser.add_argument("--output-dir", required=True, type=Path)
    parser.add_argument("--workers", type=int)
    parser.add_argument("--status", action="append", help="re-run only this status (repeatable); default: all but ELIGIBLE/EXCLUDED")
    parser.add_argument("--new-only", action="store_true", help="run only classes of --input-root that the previous run never saw")
    parser.add_argument("--resume", action="store_true")
    args = parser.parse_args()

    output_dir = args.output_dir.resolve()
    shard_path = output_dir.parent / f"{output_dir.name}.shard.json"
    forwarded = ["preflight", "--output-dir", str(output_dir)]
    if args.workers:
        forwarded += ["--workers", str(args.workers)]
    if args.resume:
        forwarded.append("--resume")
    else:
        if not (args.previous_run and args.input_root):
            parser.error("--previous-run and --input-root are required unless --resume")
        selected = write_shard(args.previous_run.resolve(), args.input_root.resolve(), set(args.status or []), shard_path, args.new_only)
        print(f"Re-running {len(selected)} classes; shard: {shard_path}", file=sys.stderr)
        forwarded += ["--input-root", str(args.input_root.resolve()), "--shard", str(shard_path)]
        if args.config:
            forwarded += ["--config", str(args.config.resolve())]
        if args.revision_map:
            forwarded += ["--revision-map", str(args.revision_map.resolve())]

    patches_path = output_dir.parent / f"{output_dir.name}.patches.json"
    if args.resume:
        if not patches_path.is_file():
            parser.error(f"{patches_path} not found; was this output dir created by this script?")
        previous_run = Path(json.loads(patches_path.read_text(encoding="utf-8"))["previous_run"])
    else:
        previous_run = args.previous_run.resolve()
        patches_path.write_text(json.dumps({
            "patch_version": PATCH_VERSION,
            "script": str(Path(__file__).resolve()),
            "script_sha256": hashlib.sha256(Path(__file__).read_bytes()).hexdigest(),
            "previous_run": str(previous_run),
            "patched_functions": ["preflight.runner._probe_source", "preflight.runner.parse_java_source", "preflight.runner._java_target",
                                  "preflight.runner._compile", "preflight.runner._maven_commands"],
            "probe_skip_flags": PROBE_SKIP_FLAGS,
        }, indent=2) + "\n", encoding="utf-8")

    apply_patches()
    sys.argv = forwarded
    cli.main()
    print(json.dumps(merge(previous_run.resolve(), output_dir), indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
