"""Render exact per-member dataset assignments; never allocates or resamples tasks."""
import argparse
import collections
import csv
import hashlib
import json
import pathlib
import team

FIELDS=['assigned_row','population_row','task_id','project_id','repo_url','class_path','test_class_paths','checkout_sha','evidence_count','source_json_path']

def render(plan,pools,output):
    output=pathlib.Path(output)
    manifest={'experiment_id':plan['experiment_id'],'assignment_revision':plan['assignment_revision'],
              'plan_sha256':team.digest(plan),'members':{m:{} for m in plan['weights']}}
    for dataset,pool in plan['datasets'].items():
        tasks=pools[dataset]
        if team.pool_hash(tasks,dataset)!=pool['pool_sha256']:
            raise ValueError('Catalog differs from frozen input pool: '+dataset)
        ordered=sorted(tasks,key=lambda t:team.candidate(t)['task_id'])
        indexed={team.candidate(t)['task_id']:(i+1,t) for i,t in enumerate(ordered)}
        seen=set()
        for member,ids in pool['assignments'].items():
            target=output/'assignments'/member/(dataset+'.csv')
            target.parent.mkdir(parents=True,exist_ok=True)
            repositories=set();preview=[]
            with target.open('w',encoding='utf-8',newline='') as stream:
                writer=csv.DictWriter(stream,fieldnames=FIELDS,lineterminator='\n');writer.writeheader()
                for local,id in enumerate(ids,1):
                    if id in seen:raise ValueError('Task assigned twice: '+id)
                    seen.add(id);ordinal,t=indexed[id];c=team.candidate(t)
                    repositories.add(team.repo_key(c['repo_url']))
                    source='' if dataset=='catlm' else c['source_json_paths'][0]
                    row={'assigned_row':local,'population_row':ordinal,'task_id':id,'project_id':c['project_id'],
                         'repo_url':c['repo_url'],'class_path':c['class_path'],
                         'test_class_paths':json.dumps(c['test_class_paths'],ensure_ascii=False,separators=(',',':')),
                         'checkout_sha':(t.get('pinned_revision') or {}).get('checkout_sha',''),
                         'evidence_count':len(c['source_json_paths']),'source_json_path':source}
                    writer.writerow(row)
                    if len(preview)<1:preview.append({k:row[k] for k in ('task_id','repo_url','class_path')})
            manifest['members'][member][dataset]={'path':f'Shards/_shared/assignments/{member}/{dataset}.csv',
                'task_count':len(ids),'repository_count':len(repositories),'bytes':target.stat().st_size,
                'sha256':hashlib.sha256(target.read_bytes()).hexdigest(),'preview':preview}
        if seen!=set(indexed) or len(seen)!=pool['total']:raise ValueError('Assignment coverage incomplete: '+dataset)
    team.write(output/'assignment-files.json',manifest)
    return manifest

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--plan',required=True);parser.add_argument('--output',required=True)
    parser.add_argument('--catalog',nargs=2,action='append',default=[])
    parser.add_argument('--index',nargs=2,action='append',default=[])
    args=parser.parse_args();pools=collections.defaultdict(list)
    for dataset,path in args.catalog:pools[dataset]+=team.rows(path)
    for dataset,path in args.index:pools[dataset]+=team.load_index(path)
    result=render(team.read(args.plan),pools,args.output)
    print(json.dumps({m:{d:v['task_count'] for d,v in datasets.items()} for m,datasets in result['members'].items()}))

if __name__=='__main__':main()
