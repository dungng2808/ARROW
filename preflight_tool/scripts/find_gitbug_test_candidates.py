"""For each NO_PAIRED_TEST CUT, find test files (Java/Groovy/Kotlin) at the fixed commit that reference the CUT."""
import csv, json, re, subprocess, shutil, sys
from pathlib import Path
A = Path("/Users/dungng/FPT/Ki 9/SET490/DoAnARROW/ARROW")
MIRRORS = A / "preflight_tool/runs/gitbug_mirrors"
WORK = Path(sys.argv[1])
rows = [r for r in csv.DictReader(open(A / "gitbug_input/candidates.csv")) if r["reason"] == "NO_PAIRED_TEST"]
def git(args, cwd=None):
    return subprocess.run(["git", *args], cwd=cwd, capture_output=True, text=True, errors="replace")
out = []
by_bug = {}
for r in rows: by_bug.setdefault((r["repository"], r["checkout_sha"]), []).append(r)
for n, ((repo, sha), cuts) in enumerate(sorted(by_bug.items()), 1):
    mirror = MIRRORS / (repo.replace("/", "-") + ".git")
    wt = WORK / f"{repo.replace('/', '-')}-{sha[:12]}"
    if not wt.exists():
        git(["worktree", "prune"], cwd=mirror)
        res = git(["worktree", "add", "--no-checkout", "--detach", str(wt), sha], cwd=mirror)
        if res.returncode: print("WT FAIL", repo, sha, res.stderr[-300:], file=sys.stderr); continue
        git(["sparse-checkout", "set", "--no-cone", "/**/src/test/**", "/test/**", "/src/test/**"], cwd=wt)
        res = git(["checkout", "--quiet", "--force", sha], cwd=wt)
        if res.returncode: print("CHECKOUT FAIL", repo, sha, res.stderr[-300:], file=sys.stderr)
    tests = [p for p in wt.rglob("*") if p.is_file() and p.suffix in (".java", ".groovy", ".kt") and "/test/" in p.as_posix()]
    texts = {p: p.read_text(errors="replace") for p in tests}
    print(f"[{n}/{len(by_bug)}] {repo}@{sha[:8]}: {len(tests)} test files", file=sys.stderr)
    for r in cuts:
        cut_path = r["class_path"]; name = cut_path.rsplit("/", 1)[1][:-5]
        pkg = cut_path.split("src/main/java/", 1)[1].rsplit("/", 1)[0].replace("/", ".") if "/" in cut_path.split("src/main/java/", 1)[1] else ""
        fqn = f"{pkg}.{name}" if pkg else name
        word = re.compile(rf"\b{re.escape(name)}\b")
        cands = []
        for p, t in texts.items():
            body = re.sub(r"(?m)^\s*(import|package)\s.*$", "", t)
            hits = len(word.findall(body))
            imported = bool(re.search(rf"(?m)^\s*import\s+(static\s+)?{re.escape(fqn)}\b", t))
            rel = p.relative_to(wt).as_posix()
            same_pkg = bool(pkg) and f"/{pkg.replace('.', '/')}/" in f"/{rel}"
            if hits == 0 and not imported: continue
            uses = len(re.findall(rf"new\s+{re.escape(name)}\b|\b{re.escape(name)}\s*\.\s*\w+\s*\(|\b{re.escape(name)}\s*::", body))
            if not same_pkg and not imported and not re.search(rf"(?m)^\s*import\s+{re.escape(pkg)}\.\*\s*;", t):
                # simple name may belong to another class with the same name
                continue
            line = next((l.strip() for l in body.splitlines() if word.search(l)), "")
            score = uses * 3 + hits + (5 if name in p.stem else 0) + (2 if same_pkg else 0)
            cands.append({"test_path": rel, "lang": p.suffix[1:], "mentions": hits, "direct_uses": uses,
                          "name_contains_cut": name in p.stem, "same_package": same_pkg, "score": score, "example": line[:200]})
        cands.sort(key=lambda c: (-c["score"], c["test_path"]))
        out.append({**{k: r[k] for k in ("task_id", "project_id", "repository", "checkout_sha", "class_path")},
                    "cut_fqn": fqn, "test_files_scanned": len(tests), "candidates": cands})
    git(["worktree", "remove", "--force", str(wt)], cwd=mirror)
(WORK / "test_candidates.json").write_text(json.dumps(out, indent=1, ensure_ascii=False))
print("done", len(out), file=sys.stderr)
