"""Check out CUT + tests per bug (sparse, read-only) and measure, per test file, how many test methods call the CUT directly."""
import csv, json, re, subprocess, sys
from pathlib import Path
A = Path("/Users/dungng/FPT/Ki 9/SET490/DoAnARROW/ARROW")
MIRRORS = A / "preflight_tool/runs/gitbug_mirrors"
WORK = Path(sys.argv[1]); WORK.mkdir(exist_ok=True)
rows = [r for r in csv.DictReader(open(A / "gitbug_input/candidates.csv")) if r["reason"] == "NO_PAIRED_TEST"]
def git(args, cwd=None): return subprocess.run(["git", *args], cwd=cwd, capture_output=True, text=True, errors="replace")
TEST_ANN = re.compile(r"@(Test|ParameterizedTest|RepeatedTest|TestFactory|TestTemplate)\b")
def methods(text):
    """Split a Java test file into (annotated_test, body) chunks by method headers."""
    out = []
    for m in re.finditer(r"((?:@\w+(?:\([^)]*\))?\s*)*)(?:public|protected|private|static|final|\s)*[\w<>\[\],\s]+\s+(\w+)\s*\([^)]*\)\s*(?:throws[\w.,\s]+)?\{", text):
        start = m.end(); depth = 1; i = start
        while i < len(text) and depth:
            depth += {"{": 1, "}": -1}.get(text[i], 0); i += 1
        out.append((bool(TEST_ANN.search(m.group(1) or "")), m.group(2), text[start:i]))
    return out
by_bug = {}
for r in rows: by_bug.setdefault((r["repository"], r["checkout_sha"]), []).append(r)
result = []
for n, ((repo, sha), cuts) in enumerate(sorted(by_bug.items()), 1):
    mirror = MIRRORS / (repo.replace("/", "-") + ".git")
    wt = WORK / f"{repo.replace('/', '-')}-{sha[:12]}"
    if not wt.exists():
        git(["worktree", "prune"], cwd=mirror)
        git(["worktree", "add", "--no-checkout", "--detach", str(wt), sha], cwd=mirror)
        pats = ["/**/src/test/**", "/src/test/**"] + ["/" + c["class_path"] for c in cuts]
        git(["sparse-checkout", "set", "--no-cone", *pats], cwd=wt)
        git(["checkout", "--quiet", "--force", sha], cwd=wt)
    tests = {p: p.read_text(errors="replace") for p in wt.rglob("*") if p.is_file() and p.suffix in (".java", ".groovy", ".kt") and "/test/" in p.as_posix()}
    print(f"[{n}/{len(by_bug)}] {repo} {len(tests)}", file=sys.stderr)
    for r in cuts:
        cp = r["class_path"]; name = cp.rsplit("/", 1)[1][:-5]
        rel = cp.split("src/main/java/", 1)[1]; pkg = rel.rsplit("/", 1)[0].replace("/", ".") if "/" in rel else ""
        fqn = f"{pkg}.{name}" if pkg else name
        src = (wt / cp).read_text(errors="replace") if (wt / cp).exists() else ""
        kind = (re.search(r"\b(abstract\s+class|class|interface|enum|record)\s+" + re.escape(name) + r"\b", src) or [None, "?"])[1]
        cands = []
        for p, t in tests.items():
            relp = p.relative_to(wt).as_posix()
            same_pkg = bool(pkg) and f"/{pkg.replace('.', '/')}/" in "/" + relp
            visible = same_pkg or re.search(rf"(?m)^\s*import\s+(static\s+)?{re.escape(fqn)}\b", t) or re.search(rf"(?m)^\s*import\s+{re.escape(pkg)}\.\*\s*;", t) or fqn in t
            if not visible or not re.search(rf"\b{re.escape(name)}\b", re.sub(r"(?m)^\s*(import|package)\s.*$", "", t)): continue
            vars_ = set(re.findall(rf"\b{re.escape(name)}(?:<[^>]*>)?\s+(\w+)\s*[=;,)]", t))
            direct = re.compile(rf"new\s+{re.escape(name)}\b|\b{re.escape(name)}\s*\.\s*\w+\s*\(|\b{re.escape(name)}\s*::" + "".join(rf"|\b{re.escape(v)}\s*\.\s*\w+\s*\(" for v in vars_))
            ms = methods(t) if p.suffix == ".java" else []
            test_ms = [m for m in ms if m[0]]
            direct_ms = [m[1] for m in test_ms if direct.search(m[2])]
            ex = next((l.strip() for m in test_ms if direct.search(m[2]) for l in m[2].splitlines() if direct.search(l)), "")
            cands.append({"test_path": relp, "lang": p.suffix[1:], "test_methods": len(test_ms), "direct_test_methods": len(direct_ms),
                          "direct_method_names": direct_ms[:6], "example": ex[:180], "same_package": same_pkg})
        cands.sort(key=lambda c: (-c["direct_test_methods"], -c["test_methods"], c["test_path"]))
        result.append({"task_id": r["task_id"], "project_id": r["project_id"], "repository": repo, "checkout_sha": sha, "class_path": cp,
                       "cut_kind": kind, "cut_lines": src.count("\n"), "tests_scanned": len(tests), "candidates": cands})
(WORK / "analysis.json").write_text(json.dumps(result, indent=1, ensure_ascii=False))
print("done", len(result), file=sys.stderr)
