#!/usr/bin/env python3
"""Install repo-local Codex harness; default dry-run, never overwrite conflicts."""
import argparse
import json
from pathlib import Path
import subprocess
import sys
from harness import HOME, repo_root

IGNORE = '\n# Codex harness local runtime\nharness/state/\nharness/profiles/local.json\n__pycache__/\n*.pyc\n.codex/config.local.toml\n'


def destination_ok(root, path):
    current = path
    while current != root:
        if current.is_symlink():
            raise ValueError('Refusing symlink destination: ' + str(current))
        if current != path and current.exists() and not current.is_dir():
            raise ValueError('Destination parent is not a directory: ' + str(current))
        current = current.parent
    if path.exists() and not path.is_file():
        raise ValueError('Destination is not a regular file: ' + str(path))


def plan_install(target):
    supplied = Path(target).absolute()
    if supplied.is_symlink():
        raise ValueError('Refusing symlink target')
    root = repo_root(target)
    if Path(target).resolve() != root:
        raise ValueError('Install target must be the Git root')
    if root == HOME:
        raise ValueError('Source repository already contains the harness')
    sources = [HOME / 'AGENTS.md', HOME / 'harness/capabilities.json', HOME / 'harness/parity.json', HOME / 'harness/source-inventory.json']
    folders = ['.agents/skills', '.codex', 'harness/scripts', 'harness/schemas',
               'harness/references', 'harness/rules', 'harness/docs', 'harness/graph',
               'harness/commands', 'harness/refactor', 'harness/archive', 'harness/examples']
    # Honor the source checkout's ignore rules, including new distributable files.
    candidates = subprocess.check_output(['git', '-C', str(HOME), 'ls-files',
                                         '--cached', '--others', '--exclude-standard', '-z'])
    for name in candidates.decode().split('\0'):
        rel = Path(name)
        if not name or not any(rel.is_relative_to(folder) for folder in folders):
            continue
        # Also exclude local-only files if accidentally tracked in the source.
        if '__pycache__' in rel.parts or rel.suffix == '.pyc' or '.local.' in rel.name or rel.name == 'local.json' or rel.name == '.env' or rel.name.startswith('.env.'):
            continue
        path = HOME / rel
        if path.is_symlink():
            raise ValueError('Refusing symlink source: ' + name)
        if path.is_file():
            sources.append(path)
    planned = []
    for source in sorted(sources):
        rel = source.relative_to(HOME)
        content = source.read_bytes()
        if rel == Path('AGENTS.md'):
            content = (HOME / 'harness/examples/AGENTS.md').read_bytes()
        planned.append((root / rel, content))
    starter = {'name': root.name, 'verification': {'commands': [], 'allow_no_tests': False},
               'vcs': {'review_tool': None}, 'tracker': None}
    planned.append((root / 'harness/profiles/default.json', (json.dumps(starter, indent=2) + '\n').encode()))
    planned.append((root / 'harness/profiles/default.md', (HOME / 'harness/profiles/default.md').read_bytes()))
    ignore = root / '.gitignore'
    destination_ok(root, ignore)
    old = ignore.read_bytes() if ignore.exists() else b''
    missing = [line for line in IGNORE.splitlines() if line and line not in old.decode().splitlines()]
    separator = b'\n' if missing and old and not old.endswith(b'\n') else b''
    planned.append((ignore, old + separator + ''.join(line + '\n' for line in missing).encode()))
    result = []
    for path, content in planned:
        destination_ok(root, path)
        if path.exists() and path.read_bytes() == content:
            action = 'skip'
        elif path == ignore:
            action = 'update'
        elif path.exists():
            raise ValueError('Existing file conflicts; merge manually: ' + str(path.relative_to(root)))
        else:
            action = 'create'
        result.append((path, content, action))
    return root, result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('target')
    parser.add_argument('--apply', action='store_true')
    args = parser.parse_args()
    try:
        root, plan = plan_install(args.target)
        for path, _, action in plan:
            print(action.upper() + ' ' + str(path.relative_to(root)))
        if args.apply:
            # Recheck the entire plan immediately before writing.
            _, checked = plan_install(args.target)
            if checked != plan:
                raise ValueError('Destination changed during preflight')
            for path, content, action in plan:
                if action != 'skip':
                    path.parent.mkdir(parents=True, exist_ok=True)
                    path.write_bytes(content)
            print('Installed. Configure harness/profiles/local.json with real test commands.')
        else:
            print('DRY RUN: no files changed. Use --apply to install.')
        return 0
    except (ValueError, OSError, subprocess.SubprocessError) as exc:
        print('ERROR: ' + str(exc), file=sys.stderr)
        return 2


if __name__ == '__main__':
    sys.exit(main())
