"""Portable team allocation, local shard creation and completed-result export.

Only orchestration/reporting uses Python; the preflight engine remains the .NET CLI.
"""
from __future__ import annotations
import argparse
import collections
import hashlib
import json
import os
import pathlib
import platform
import sqlite3
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
MEMBERS = {'dung':40, 'ka':20, 'may-toi':15, 'han':15, 'chinh':10}
EXPECTED = {'classes2test':85819, 'gitbug-java':202, 'catlm':119}

def read(path):
    return json.loads(pathlib.Path(path).read_text(encoding='utf-8-sig'))

def rows(path):
    with pathlib.Path(path).open(encoding='utf-8-sig') as stream:
        return [json.loads(line) for line in stream if line.strip()]

def write(path, value):
    path = pathlib.Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=True, indent=2)+'\n', encoding='utf-8')

def digest(value):
    return hashlib.sha256(json.dumps(value,ensure_ascii=True,sort_keys=True,separators=(',',':')).encode()).hexdigest()

def load_index(path):
    uri = pathlib.Path(path).resolve().as_uri()+'?mode=ro'
    with sqlite3.connect(uri,uri=True) as db:
        return [json.loads(row[0]) for row in db.execute('SELECT task_json FROM normalized_tasks ORDER BY task_id')]

def candidate(task):
    return task.get('candidate',task)

def canonical(task,dataset):
    c = candidate(task)
    record = {k:c[k] for k in ('task_id','project_id','repo_url','class_path','class_name')}
    record['test_class_paths'] = sorted(c['test_class_paths'])
    if dataset=='catlm':
        # CAT-LM evidence JSON contains absolute archive/run paths. Compare stable
        # identities, snapshot SHA and the Java archive prefix checksum instead.
        record['checkout_sha'] = task['pinned_revision']['checkout_sha']
        record['java_archive_sha256'] = task['extra']['catlm.java_archive_sha256']
        record['java_archive_bytes'] = task['extra']['catlm.java_archive_bytes']
    else:
        record['evidence'] = sorted(zip(c['source_json_paths'],c['source_json_sha256s']))
        if dataset=='gitbug-java':
            record['checkout_sha'] = task['pinned_revision']['checkout_sha']
    return record

def pool_hash(tasks,dataset):
    records = sorted((canonical(t,dataset) for t in tasks),key=lambda c:c['task_id'])
    ids = [r['task_id'] for r in records]
    if len(ids)!=len(set(ids)): raise ValueError('Duplicate task IDs')
    return digest(records)

def repo_key(url):
    value = url.rstrip('/')
    if value.endswith('.git'): value=value[:-4]
    return value.lower() if value.startswith('https://github.com/') else value

def allocate(tasks):
    groups = collections.defaultdict(list)
    for task in tasks: groups[repo_key(candidate(task)['repo_url'])].append(candidate(task)['task_id'])
    assigned = {m:[] for m in MEMBERS}
    for repo,ids in sorted(groups.items(),key=lambda item:(-len(item[1]),item[0])):
        member = min(MEMBERS,key=lambda m:((len(assigned[m])+len(ids))/MEMBERS[m],len(assigned[m])/MEMBERS[m],list(MEMBERS).index(m)))
        assigned[member].extend(ids)
    assert set().union(*(set(v) for v in assigned.values()))=={candidate(t)['task_id'] for t in tasks}
    assert sum(map(len,assigned.values()))==len(tasks)
    return {m:sorted(v) for m,v in assigned.items()}

def plan(args):
    output=pathlib.Path(args.output)
    if output.exists(): raise ValueError('Plan already exists; do not silently reallocate an experiment')
    pools=collections.defaultdict(list)
    for dataset,path in args.catalog or []: pools[dataset]+=rows(path)
    for dataset,path in args.index or []: pools[dataset]+=load_index(path)
    assert set(pools)==set(EXPECTED)
    result={'schema_version':1,'experiment_id':'team-r1','weights':MEMBERS,'datasets':{}}
    for dataset,tasks in pools.items():
        assert len(tasks)==EXPECTED[dataset],(dataset,len(tasks))
        assignments=allocate(tasks)
        result['datasets'][dataset]={'total':len(tasks),'pool_sha256':pool_hash(tasks,dataset),'assignments':assignments,
            'counts':{m:len(ids) for m,ids in assignments.items()},'repositories':len({repo_key(candidate(t)['repo_url']) for t in tasks})}
    write(output,result)
    print(json.dumps({d:v['counts'] for d,v in result['datasets'].items()}))

def assignment(task):
    c=candidate(task)
    evidence=sorted(zip(c['source_json_paths'],c['source_json_sha256s']))
    checksum=hashlib.sha256(json.dumps(evidence,ensure_ascii=True,separators=(',',':')).encode()).hexdigest()
    return {k:c[k] for k in ('task_id','repo_url','class_path','class_name')}|{'evidence_count':len(evidence),'evidence_sha256':checksum}

def shard(args):
    plan=read(args.plan); expected=plan['datasets'][args.dataset]
    tasks=load_index(args.index)
    if pool_hash(tasks,args.dataset)!=expected['pool_sha256']:
        raise ValueError('Local input differs from frozen team pool; check dataset/revision_map/archive. Do not run.')
    wanted=set(expected['assignments'][args.member])
    chosen=[t for t in tasks if candidate(t)['task_id'] in wanted]
    assert len(chosen)==len(wanted)
    if not chosen: raise ValueError('No tasks assigned; skip this dataset on this member')
    output=pathlib.Path(args.output)
    if output.exists(): raise ValueError('Shard exists; reuse it for resume')
    write(output,{'schema_version':1,'shard_id':f"team-r1-{args.member}-{args.dataset}",'class_count':len(chosen),'classes':[assignment(t) for t in chosen]})
    print(json.dumps({'member':args.member,'dataset':args.dataset,'classes':len(chosen),'output':str(output)}))

def export(args):
    run=pathlib.Path(args.run).resolve()
    plan=read(args.plan); expected=set(plan['datasets'][args.dataset]['assignments'][args.member])
    provenance=read(run/'provenance.json')
    if provenance.get('run_status')!='COMPLETED': raise ValueError('Only export COMPLETED runs')
    results=rows(run/'reports/preflight_results.jsonl')
    if len(results)!=len(expected) or {r['task_id'] for r in results}!=expected:
        raise ValueError('Result coverage differs from assigned shard')
    out=ROOT/'results/team-r1'/args.member/args.dataset
    if out.exists(): raise ValueError('Published export exists; use a new experiment ID for reruns')
    out.mkdir(parents=True)
    files=[]
    sources=[run/'reports/preflight_results.jsonl',run/'reports/summary.json',run/'reports/input_rejections.jsonl',run/'reports/repo_cleanup.jsonl',run/'provenance.json']
    sources+=sorted((run/'manifests').glob('*.jsonl'))
    for source in sources:
        if not source.exists(): continue
        name=('manifests-' if source.parent.name=='manifests' else '')+source.name
        # Preserve JSONL bytes. Chunk below GitHub's 100 MiB hard limit.
        if source.suffix=='.jsonl':
            number=1; buffer=bytearray()
            with source.open('rb') as stream:
                for line in stream:
                    if len(line)>20*1024**2: raise ValueError('Single JSONL row >20 MiB; inspect before publication')
                    if buffer and len(buffer)+len(line)>20*1024**2:
                        target=out/(pathlib.Path(name).stem+f'.part-{number:04}.jsonl');target.write_bytes(buffer);files.append(target);number+=1;buffer.clear()
                    buffer.extend(line)
            if buffer:
                target=out/(pathlib.Path(name).stem+f'.part-{number:04}.jsonl');target.write_bytes(buffer);files.append(target)
        else:
            target=out/name;target.write_bytes(source.read_bytes());files.append(target)
    passed=sum(bool(r['technical_eligible']) for r in results)
    metadata={'experiment_id':'team-r1','member':args.member,'dataset':args.dataset,'total':len(results),'technical_pass':passed,'pass_percent':round(passed/len(results)*100,3),
       'strict_pass':sum(bool(r['strict_eligible']) for r in results),'by_status':dict(collections.Counter(r['preflight_status'] for r in results)),
       'plan_sha256':digest(plan),'pool_sha256':plan['datasets'][args.dataset]['pool_sha256'],
       'os':platform.platform(),'architecture':platform.machine(),'python_for_reporting':platform.python_version(),
       'git_commit':subprocess.check_output(['git','-c','safe.directory='+ROOT.parent.as_posix(),'-C',str(ROOT.parent),'rev-parse','HEAD'],text=True).strip(),
       'files':{f.name:{'bytes':f.stat().st_size,'sha256':hashlib.sha256(f.read_bytes()).hexdigest()} for f in files}}
    write(out/'export.json',metadata)
    print(json.dumps({'export':str(out),'total':len(results),'pass_percent':metadata['pass_percent']}))

def aggregate(args):
    plan=read(args.plan); plan_sha=digest(plan)
    summary={'experiment_id':'team-r1','plan_sha256':plan_sha,'datasets':{}}
    for dataset,pool in plan['datasets'].items():
        combined=[]; members={}
        for member in MEMBERS:
            folder=ROOT/'results/team-r1'/member/dataset
            meta=read(folder/'export.json')
            if meta['plan_sha256']!=plan_sha:raise ValueError('Different team plan: '+str(folder))
            for name,info in meta['files'].items():
                path=folder/name
                if path.stat().st_size!=info['bytes'] or hashlib.sha256(path.read_bytes()).hexdigest()!=info['sha256']:
                    raise ValueError('Changed export: '+str(path))
            records=[r for path in sorted(folder.glob('preflight_results.part-*.jsonl')) for r in rows(path)]
            if len(records)!=len(pool['assignments'][member]) or {r['task_id'] for r in records}!=set(pool['assignments'][member]):
                raise ValueError('Incomplete or overlapping member result: '+str(folder))
            combined+=records;members[member]={k:meta[k] for k in ('total','technical_pass','pass_percent','os','architecture')}
        expected=set().union(*(set(v) for v in pool['assignments'].values()))
        assert len(combined)==pool['total'] and {r['task_id'] for r in combined}==expected
        passed=sum(bool(r['technical_eligible']) for r in combined)
        summary['datasets'][dataset]={'total':len(combined),'technical_pass':passed,'pass_percent':round(passed/len(combined)*100,3),'strict_pass':sum(bool(r['strict_eligible']) for r in combined),'by_status':dict(collections.Counter(r['preflight_status'] for r in combined)),'members':members}
    total=sum(d['total'] for d in summary['datasets'].values());passed=sum(d['technical_pass'] for d in summary['datasets'].values())
    summary.update(total=total,technical_pass=passed,pass_percent=round(passed/total*100,3))
    out=ROOT/'results/team-r1/_aggregate'
    write(out/'full-summary.json',summary)
    lines=['# Kết quả team-r1','',f'Tổng: {passed}/{total} technical pass ({passed/total*100:.2f}%).','', '| Dataset | Tổng CUT | Pass | % pass | Strict |','| --- | ---: | ---: | ---: | ---: |']
    for name,value in summary['datasets'].items():lines.append(f"| {name} | {value['total']} | {value['technical_pass']} | {value['pass_percent']:.2f}% | {value['strict_pass']} |")
    lines+=['','Tỷ lệ gộp theo số CUT, không lấy trung bình phần trăm của năm máy. Windows/macOS được ghi riêng trong JSON; kết quả chịu ảnh hưởng môi trường.']
    (out/'TONG_HOP.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
    print(json.dumps(summary))

def publish(args):
    plan=read(args.plan); prefix=f'Prelight_Net/results/team-r1/{args.member}/'
    for dataset,pool in plan['datasets'].items():
        folder=ROOT/'results/team-r1'/args.member/dataset
        meta=read(folder/'export.json')
        if meta['plan_sha256']!=digest(plan):raise ValueError('Export uses another plan')
        for name,info in meta['files'].items():
            path=folder/name
            if hashlib.sha256(path.read_bytes()).hexdigest()!=info['sha256']:raise ValueError('Export checksum mismatch')
        records=[r for path in sorted(folder.glob('preflight_results.part-*.jsonl')) for r in rows(path)]
        if len(records)!=len(pool['assignments'][args.member]) or {r['task_id'] for r in records}!=set(pool['assignments'][args.member]):
            raise ValueError('Do not publish incomplete member results')
    repo=ROOT.parent
    env=dict(os.environ);env['GIT_TERMINAL_PROMPT']='0';env['GCM_INTERACTIVE']='never'
    base=['git','-c','safe.directory='+repo.as_posix(),'-C',str(repo)]
    def git(*values,capture=False):
        return subprocess.run(base+list(values),check=True,env=env,capture_output=capture,text=capture)
    branch=f'experiment/team-r1-{args.member}'
    if git('branch','--show-current',capture=True).stdout.strip()!=branch:
        raise ValueError('Switch to '+branch+' before publishing')
    staged=git('diff','--cached','--name-only',capture=True).stdout.splitlines()
    if any(not p.startswith(prefix) for p in staged):
        raise ValueError('Other staged changes exist; preserve them and publish manually')
    git('add','--',prefix)
    staged=git('diff','--cached','--name-only',capture=True).stdout.splitlines()
    if any(not p.startswith(prefix) for p in staged):raise ValueError('Unexpected staged path')
    if staged:git('commit','-m','Add team-r1 results for '+args.member)
    git('push','-u','origin',branch)
    print(json.dumps({'published_branch':branch,'member':args.member}))

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    sub=parser.add_subparsers(dest='command',required=True)
    p=sub.add_parser('plan');p.add_argument('--catalog',nargs=2,action='append');p.add_argument('--index',nargs=2,action='append');p.add_argument('--output',required=True);p.set_defaults(func=plan)
    p=sub.add_parser('aggregate');p.add_argument('--plan',required=True);p.set_defaults(func=aggregate)
    p=sub.add_parser('publish');p.add_argument('--plan',required=True);p.add_argument('--member',required=True,choices=MEMBERS);p.set_defaults(func=publish)
    for command,func in [('shard',shard),('export',export)]:
        p=sub.add_parser(command);p.add_argument('--plan',required=True);p.add_argument('--dataset',required=True,choices=EXPECTED);p.add_argument('--member',required=True,choices=MEMBERS)
        if command=='shard':p.add_argument('--index',required=True);p.add_argument('--output',required=True)
        else:p.add_argument('--run',required=True)
        p.set_defaults(func=func)
    args=parser.parse_args();args.func(args)

if __name__=='__main__':main()
