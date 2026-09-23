from importlib.util import module_from_spec, spec_from_file_location
from pathlib import Path

import pytest

from tests.conftest import sample_payload


spec = spec_from_file_location("split_dataset", Path(__file__).parents[2] / "scripts/split_dataset.py")
module = module_from_spec(spec)
spec.loader.exec_module(module)


@pytest.mark.unit
def test_split_preserves_bytes_groups_repositories_and_covers_input(dataset_factory, tmp_path):
    records = []
    for i in range(7):
        records.append(sample_payload(repo_url=f"https://github.com/acme/repo{i}", class_name=f"Thing{i}"))
    records.append(records[0])
    records.append(sample_payload(repo_url="https://github.com/ACME/repo0.git", class_name="Alias"))
    source = dataset_factory(records)
    originals = {p.relative_to(source): p.read_bytes() for p in source.glob("*/*.json")}
    output = tmp_path / "shards"
    report = module.split(source, output, 5)
    assert report["verified"]
    assert report["raw_json"] == 9
    assert report["classes"] == 8
    assert report["repositories"] == 7
    assert all(row["classes"] for row in report["shards"])
    all_paths = []
    repo_shards = {}
    import json
    for shard in output.glob("shard-*"):
        for path in (shard / "dataset").glob("*/*.json"):
            relative = path.relative_to(shard / "dataset")
            assert path.read_bytes() == originals[relative]
            assert path.stat().st_ino != (source / relative).stat().st_ino
            all_paths.append(relative)
            repo = module.repo_key(json.loads(path.read_bytes())["repository"]["url"])
            repo_shards.setdefault(repo, set()).add(shard.name)
    assert len(all_paths) == len(set(all_paths)) == len(originals)
    assert set(all_paths) == set(originals)
    assert all(len(shards) == 1 for shards in repo_shards.values())
    assert all((source / path).read_bytes() == content for path, content in originals.items())
    with pytest.raises(FileExistsError):
        module.split(source, output, 5)


@pytest.mark.unit
def test_split_rejects_output_inside_source(dataset_factory):
    source = dataset_factory([sample_payload()])
    with pytest.raises(ValueError, match="outside source"):
        module.split(source, source / "shards", 5)
