"""Read-only Java AST oracle. Writes only beneath Prelight_Net; never runs dataset builds."""
import argparse, dataclasses, gzip, hashlib, json, pathlib, sys, tarfile

ROOT = pathlib.Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT.parent / 'preflight_tool'))
from preflight.java_ast import parse_java_source

def canonical(value):
    if dataclasses.is_dataclass(value): return canonical(dataclasses.asdict(value))
    if isinstance(value, set): return sorted(value)
    if isinstance(value, list): return [canonical(v) for v in value]
    if isinstance(value, dict): return {k: canonical(v) for k, v in value.items()}
    return value

def main():
    p = argparse.ArgumentParser(); p.add_argument('--limit', type=int, default=5000); a = p.parse_args()
    output = ROOT / '.work/golden-full/java_ast.jsonl'; output.parent.mkdir(parents=True,exist_ok=True); count = 0
    with output.open('w', encoding='utf-8', newline='\n') as out:
        with tarfile.open(ROOT / 'Dataset/catlm/testdata.tgz', 'r|') as archive:
            for member in archive:
                if member.name.startswith('TestData/RawData/python/'): break
                if not member.isfile() or not member.name.startswith('TestData/RawData/java/') or not member.name.endswith('.java'): continue
                source = archive.extractfile(member).read().decode('utf-8', errors='replace').replace('\r\n','\n').replace('\r','\n')
                expected = pathlib.PurePosixPath(member.name).stem
                record = {'input': {'source': source, 'path': member.name, 'expected_name': expected}, 'output': canonical(parse_java_source(source, member.name, expected_name=expected))}
                out.write(json.dumps(record, ensure_ascii=False) + '\n'); count += 1
                if count >= a.limit: break
    compressed=ROOT/'tests/golden/java_ast.jsonl.gz'
    with compressed.open('wb') as file:
        with gzip.GzipFile(filename='',mode='wb',fileobj=file,mtime=0) as zipped: zipped.write(output.read_bytes())
    legacy=ROOT/'tests/golden/java_ast.jsonl'
    if legacy.exists(): legacy.unlink()
    metadata = {'count': count, 'sha256': hashlib.sha256(compressed.read_bytes()).hexdigest(), 'raw_sha256':hashlib.sha256(output.read_bytes()).hexdigest(), 'oracle': 'preflight.java_ast.parse_java_source', 'tree_sitter': '0.24.0', 'tree_sitter_java': '0.23.5', 'read_only_source': 'CAT-LM Java snapshot'}
    (ROOT / 'tests/golden/java_ast.meta.json').write_text(json.dumps(metadata, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(metadata), flush=True)
    if count < a.limit: raise SystemExit('Not enough real Java files for AST acceptance')

if __name__ == '__main__': main()
