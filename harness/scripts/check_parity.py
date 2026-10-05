#!/usr/bin/env python3
"""Validate the independent source inventory, mappings and native artifacts."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess
import sys
from harness import HOME


def contained_file(root, relative):
    path = Path(relative)
    if path.is_absolute() or '..' in path.parts:
        raise ValueError('Invalid artifact path: ' + relative)
    resolved = (root / path).resolve()
    return resolved if root.resolve() in resolved.parents and resolved.is_file() else None


def source_inventory(source):
    source = source.resolve()
    actual_root = Path(subprocess.check_output(
        ['git', '-C', str(source), 'rev-parse', '--show-toplevel'], text=True).strip()).resolve()
    if actual_root != source:
        raise ValueError('--source must be the source Git root')
    paths = subprocess.check_output(['git', '-C', str(source), 'ls-files', '-z']).decode().split('\0')
    return {p: hashlib.sha256((source / p).read_bytes()).hexdigest()
            for p in paths if p and contained_file(source, p)}


def check(root=HOME, installed=False, source=None):
    root = root.resolve()
    manifest = json.loads((root / 'harness/parity.json').read_text())
    baseline = json.loads((root / 'harness/source-inventory.json').read_text())['files']
    entries = manifest['entries']
    if not baseline or not entries:
        raise ValueError('Source inventory and parity entries must be non-empty')
    failures = []
    mapped = {}
    for entry in entries:
        name = entry['source']
        if name in mapped:
            failures.append('Duplicate source mapping: ' + name)
        mapped[name] = entry['source_sha256']
        if baseline.get(name) != entry['source_sha256']:
            failures.append('Source digest differs from inventory: ' + name)
        if installed and (entry['target'].startswith(('.github/', 'harness/tests/')) or entry['target'] in {'README.md', 'CHANGELOG.md', 'VERSION'}):
            continue
        if not contained_file(root, entry['target']):
            failures.append(name + ' -> missing/invalid ' + entry['target'])
    for name in sorted(set(baseline) - set(mapped)):
        failures.append('Unmapped source: ' + name)
    if source is not None:
        actual = source_inventory(source)
        for name in sorted(set(actual) | set(baseline)):
            if actual.get(name) != baseline.get(name):
                failures.append('Live source inventory/digest differs: ' + name)
    capabilities = json.loads((root / 'harness/capabilities.json').read_text())
    actual_sets = {
        'skills': {p.parent.name for p in (root / '.agents/skills').glob('*/SKILL.md')},
        'agents': {p.stem for p in (root / '.codex/agents').glob('*.toml')},
        'workflows': {p.stem for p in (root / 'harness/graph/workflows').glob('*.json')},
    }
    for kind, actual in actual_sets.items():
        declared = capabilities[kind]
        if len(declared) != len(set(declared)) or actual != set(declared):
            failures.append(kind + ' discovery and capabilities manifest differ')
    return {'ok': not failures, 'mapped_source_artifacts': len(entries),
            'native_skills': len(actual_sets['skills']), 'native_agents': len(actual_sets['agents']),
            'workflows': len(actual_sets['workflows']), 'live_source_checked': source is not None,
            'failures': failures,
            'note': 'Artifact coverage and source digests are checked; LLM decisions and live client/connector behavior need separate validation.'}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--root', type=Path, default=HOME)
    parser.add_argument('--installed', action='store_true')
    parser.add_argument('--source', type=Path, help='Also compare the current source Git checkout')
    args = parser.parse_args()
    try:
        result = check(args.root, args.installed, args.source)
        print(json.dumps(result, indent=2))
        return int(not result['ok'])
    except (ValueError, OSError, KeyError, TypeError, subprocess.SubprocessError) as exc:
        print('ERROR: ' + str(exc), file=sys.stderr)
        return 2


if __name__ == '__main__':
    sys.exit(main())
