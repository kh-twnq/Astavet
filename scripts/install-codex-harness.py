#!/usr/bin/env python3
"""Install portable user-level guidance without changing model or approvals.
Python 3.11+. Preview by default; --apply performs the reviewed installation.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import tempfile
import sys
try:
    import tomllib
except ImportError:
    sys.exit('Python 3.11+ is required; run this installer with a compatible Python executable.')

ROOT = Path(__file__).resolve().parents[1]
BEGIN = '# BEGIN ECC PORTABLE HARNESS v1'
END = '# END ECC PORTABLE HARNESS v1'


def digest(data):
    return hashlib.sha256(data).hexdigest()


def split_managed(text):
    if BEGIN not in text and END not in text:
        return text, None
    if text.count(BEGIN) != 1 or text.count(END) != 1:
        raise ValueError('Malformed or duplicate managed section; stop and review the file.')
    start, end = text.index(BEGIN), text.index(END) + len(END)
    if text.index(END) < start:
        raise ValueError('Invalid managed section order')
    if text[end:end + 1] == '\n':
        end += 1
    return text[:start] + text[end:], text[start:end]


def validate_path(home, path):
    # No writes through symlinks, including the managed destination tree.
    current = path
    while current != home.parent:
        if current.is_symlink():
            raise ValueError('Refusing symlink destination: ' + str(current))
        if current == home:
            break
        current = current.parent
    if path.exists() and not path.is_file():
        raise ValueError('Destination is not a regular file: ' + str(path))


def atomic_write(path, data, mode):
    path.parent.mkdir(parents=True, exist_ok=True)
    handle, name = tempfile.mkstemp(prefix='.ecc-write-', dir=str(path.parent))
    try:
        with os.fdopen(handle, 'wb') as output:
            output.write(data)
        os.chmod(name, mode)
        os.replace(name, path)
    finally:
        if os.path.exists(name):
            os.unlink(name)


def plan(home):
    package = home / 'ecc-harness'
    manifest_path = package / 'install-manifest.json'
    validate_path(home, manifest_path)
    old = json.loads(manifest_path.read_text()) if manifest_path.exists() else {}
    if old and old.get('version') != 1:
        raise ValueError('Unsupported installed manifest version')
    if (home / 'AGENTS.override.md').exists():
        raise ValueError('User AGENTS.override.md shadows AGENTS.md; resolve that precedence before installing.')
    source = ROOT / 'config/ecc/portable'
    files = {'route.mjs': (ROOT / 'scripts/codex-harness.mjs').read_bytes(),
             'LICENSE': (ROOT / 'tools/ecc/LICENSE').read_bytes(),
             'audit-codex.py': (ROOT / 'scripts/audit-codex.py').read_bytes(),
             'node-runtime.sh': (source / 'node-runtime.sh').read_bytes(),
             'bin/route': (source / 'route.sh').read_bytes(),
             'bin/mcp': (source / 'mcp.sh').read_bytes()}
    for role in ['explorer', 'reviewer', 'build-resolver']:
        files['agents/' + role + '.toml'] = (source / (role + '.toml')).read_bytes()
    for language in ['common', 'java', 'typescript']:
        for name in ['coding-style', 'security', 'testing']:
            files['rules/' + language + '/' + name + '.md'] = (ROOT / 'docs/ecc/rules' / language / (name + '.md')).read_bytes()
    writes = {}
    for name, data in files.items():
        path = package / name
        validate_path(home, path)
        if path.exists() and digest(path.read_bytes()) != old.get('files', {}).get(name):
            raise ValueError('Unmanaged or locally modified file; refusing overwrite: ' + str(path))
        writes[path] = data
    # Conservative parser-based conflict detection: no regex merge of personal TOML.
    config_path = home / 'config.toml'
    agents_path = home / 'AGENTS.md'
    texts = {}
    for path in [config_path, agents_path]:
        validate_path(home, path)
        text = path.read_text() if path.exists() else ''
        outside, managed = split_managed(text)
        if managed and digest(managed.encode()) != old.get('sections', {}).get(path.name):
            raise ValueError('Modified managed section; refusing overwrite: ' + str(path))
        texts[path] = (text, outside, managed)
    cfg = tomllib.loads(texts[config_path][1])
    agent_names = ['ecc_explorer', 'ecc_reviewer', 'ecc_build_resolver']
    if any(name in cfg.get('agents', {}) for name in agent_names):
        raise ValueError('Existing portable agent name conflicts with installation')
    servers = cfg.get('mcp_servers', {})
    plugin_servers = cfg.get('plugins', {}).get('ecc@ecc', {}).get('mcp_servers', {})
    if 'ecc-chrome-devtools' in servers or 'chrome-devtools' in plugin_servers:
        raise ValueError('Existing MCP override conflicts with installation; review instead of replacing it')
    quote = lambda value: json.dumps(str(value), ensure_ascii=False)
    config = ''
    for name, filename, description in [
        ('ecc_explorer', 'explorer', 'Read-only evidence gathering for the target project.'),
        ('ecc_reviewer', 'reviewer', 'Correctness, security, compatibility and test review.'),
        ('ecc_build_resolver', 'build-resolver', 'Resolve build and test failures with project tooling.')]:
        config += '\n[agents.' + name + ']\ndescription = ' + quote(description) + '\nconfig_file = ' + quote(package / 'agents' / (filename + '.toml')) + '\n'
    config += '\n[plugins."ecc@ecc".mcp_servers.chrome-devtools]\nenabled = false\n'
    config += '\n[mcp_servers.ecc-chrome-devtools]\ncommand = "bash"\nargs = [' + quote(package / 'bin/mcp') + ']\nstartup_timeout_sec = 30\n'
    sections = {}
    for path, content in [(config_path, config.lstrip()), (agents_path, (source / 'AGENTS.md').read_text())]:
        section = BEGIN + '\n' + content.rstrip() + '\n' + END + '\n'
        text, outside, previous = texts[path]
        # Keep personal bytes outside the managed section, including table layout.
        if previous:
            updated = text.replace(previous, section, 1)
        else:
            updated = outside + ('\n' if outside and not outside.endswith('\n') else '') + section
        if path == config_path:
            tomllib.loads(updated)
        writes[path] = updated.encode()
        sections[path.name] = digest(section.encode())
    manifest = {'version': 1, 'files': {name: digest(data) for name, data in files.items()}, 'sections': sections,
                'upstream': json.loads((ROOT / 'tools/ecc/manifest.json').read_text())['commit']}
    writes[manifest_path] = (json.dumps(manifest, indent=2) + '\n').encode()
    return writes


def apply(writes, home):
    snapshots = {path: (path.read_bytes(), path.stat().st_mode & 0o777) if path.exists() else None for path in writes}
    changed = []
    try:
        for path, data in writes.items():
            previous = snapshots[path]
            if previous and previous[0] == data:
                continue
            # Back up original user-facing files exactly once, with private modes.
            if previous and path.parent == home:
                backup = home / 'ecc-harness/backups' / path.name
                validate_path(home, backup)
                if not backup.exists():
                    atomic_write(backup, previous[0], 0o600)
            atomic_write(path, data, previous[1] if previous else 0o600)
            changed.append(path)
    except Exception:
        for path in reversed(changed):
            previous = snapshots[path]
            if previous:
                atomic_write(path, previous[0], previous[1])
            else:
                path.unlink()
        raise


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--codex-home', default=os.environ.get('CODEX_HOME', str(Path.home() / '.codex')))
    parser.add_argument('--apply', action='store_true')
    args = parser.parse_args()
    home = Path(args.codex_home).expanduser().absolute()
    if home.is_symlink():
        raise ValueError('Refusing symlink Codex home')
    writes = plan(home)
    if args.apply:
        apply(writes, home)
    print(json.dumps({'status': 'success', 'summary': 'Installed portable harness' if args.apply else 'Validated installation preview; no files written',
                      'artifacts': [str(path) for path in writes],
                      'next_actions': ['Reload Codex; verify ECC plugin installed/enabled and audit a target project.',
                                       'Hook trust is unchanged; review it in Codex. Browser actions and model rollouts remain untested.']}, indent=2))


if __name__ == '__main__':
    try:
        main()
    except (ValueError, OSError, KeyError) as error:
        print(json.dumps({'status': 'error', 'summary': str(error), 'next_actions': ['Resolve the conflict and rerun preview; no force overwrite is supported.'], 'artifacts': []}), file=sys.stderr)
        sys.exit(1)
