"""Read-only original Python adapter/helper oracle; output is confined to Prelight_Net."""
import dataclasses, hashlib, json, pathlib, sqlite3, sys
ROOT = pathlib.Path(__file__).resolve().parents[2]
sys.path[:0] = [str(ROOT.parent/'preflight_tool'),str(ROOT.parent/'preflight_tool/scripts')]
from preflight.ingest import build_index, candidates
from normalize_gitbug import changed_java_files, pair_test
from export_shards import export
WORK = ROOT/'.work/acceptance'; report = {}
database = WORK/'gitbug-python.sqlite'
if database.exists(): database.unlink()
raw, count = build_index(ROOT.parent/'gitbug_input/dataset', database)
expected = {item.task_id:dataclasses.asdict(item) for item in candidates(database)}
actual = {r['task_id']:r for r in map(json.loads,(WORK/'gitbug-normalize/manifests/class_candidates.jsonl').read_text().splitlines())}
assert expected.keys()==actual.keys()
for task,row in expected.items():
    for key,value in row.items():
        if isinstance(value,tuple): value=list(value)
        assert actual[task][key]==value,(task,key)
report['gitbug_normalized_replay']={'raw':raw,'classes':count,'matched':len(expected)}
output = WORK/'shards-python'
if not output.exists(): export(database, output, 5)
net = WORK/'shards-net'
assert net.exists(), 'Run .NET export-shards first'
for path in output.glob('shard-*.json'): assert path.read_bytes()==(net/path.name).read_bytes(),path.name
report['shard_bytes_matched']=len(list(output.glob('shard-*.json')))
golden = ROOT/'tests/golden/gitbug_rules.jsonl'; bugs=0
with golden.open('w',encoding='utf-8',newline='\n') as out:
    for file in sorted((ROOT/'Dataset/gitbug-java/data/bugs').glob('*.json')):
        for line in file.read_text(encoding='utf-8').splitlines():
            if not line.strip(): continue
            bug=json.loads(line); bugs+=1
            out.write(json.dumps({'operation':'changed_java','input':bug['bug_patch'],'output':changed_java_files(bug['bug_patch'])},ensure_ascii=False)+'\n')
    # Same package, unique, ambiguous and missing paths with real Java path names.
    for row in actual.values():
        path=row['class_path']; tests=row['test_class_paths']
        for paths in (tests,[],tests+['src/test/java/other/'+path.rsplit('/',1)[-1][:-5]+'Test.java']):
            out.write(json.dumps({'operation':'pair','input':{'path':path,'paths':paths},'output':pair_test(path,set(paths))},ensure_ascii=False)+'\n')
assert bugs==199,bugs
report['raw_gitbug_bugs']=bugs; report['gitbug_rules_sha256']=hashlib.sha256(golden.read_bytes()).hexdigest()
(ROOT/'tests/golden/gitbug_rules.meta.json').write_text(json.dumps({'sha256':report['gitbug_rules_sha256'],'bugs':bugs,'pairs':606},indent=2)+'\n',encoding='utf-8')
with sqlite3.connect(WORK/'catlm-normalize/state/class_index.sqlite') as db:
    catlm=[json.loads(row[0]) for row in db.execute('select task_json from normalized_tasks')]
assert len(catlm)==119 and len({t['repo']['repo_url'] for t in catlm})==98
evals=sum(t['extra']['catlm_eval_subset']=='true' for t in catlm); assert evals==27,evals
report['catlm']={'pairs':119,'repositories':98,'eval_pairs':evals,'rejections':0}
(ROOT/'.work/verification/adapters.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
print(json.dumps(report))
