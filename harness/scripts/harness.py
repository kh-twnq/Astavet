#!/usr/bin/env python3
"""Local task ledger and snapshot-bound verifier (Python 3.9+, Git)."""
import argparse
from datetime import datetime, timezone
import hashlib
import json
import os
from pathlib import Path
import re
import signal
import stat
import subprocess
import sys
import tempfile

HOME = Path(__file__).resolve().parents[2]
TASK_RE = re.compile(r'[A-Za-z0-9][A-Za-z0-9_.-]{0,79}\Z')
TOKEN_RE = re.compile(r'AKIA[0-9A-Z]{16}|gh[pousr]_[A-Za-z0-9]{20,}|glpat-[A-Za-z0-9_-]{20,}|sk-ant-[A-Za-z0-9_-]{20,}|(?:sk|rk)_live_[A-Za-z0-9]{16,}|whsec_[A-Za-z0-9]{16,}|eyJ[A-Za-z0-9_-]{15,}\.eyJ[A-Za-z0-9_-]{15,}\.[A-Za-z0-9_-]{10,}|xox[bapors]-[A-Za-z0-9-]{10,}|-----BEGIN (?:[A-Z]+ )*PRIVATE KEY-----')
TEMPLATES = {'.env.example', '.env.template', '.env.sample'}


def now():
    return datetime.now(timezone.utc).isoformat()


def digest(data):
    return hashlib.sha256(data).hexdigest()


def git(root, *args, check=True):
    result = subprocess.run(['git', '-C', str(root), *args], capture_output=True)
    if check and result.returncode:
        raise ValueError('Git operation failed: ' + ' '.join(args[:2]))
    return result.stdout


def repo_root(path):
    return Path(git(Path(path).resolve(), 'rev-parse', '--show-toplevel').decode().strip()).resolve()


def validate(value, schema, at='$'):
    """Validate the JSON Schema subset used in bundled contracts."""
    kinds = {'object': dict, 'array': list, 'string': str, 'boolean': bool, 'integer': int, 'null': type(None)}
    kind = schema.get('type')
    if kind and (not isinstance(value, kinds[kind]) or kind == 'integer' and isinstance(value, bool)):
        raise ValueError(at + ': expected ' + kind)
    if 'enum' in schema and value not in schema['enum']:
        raise ValueError(at + ': invalid value')
    if isinstance(value, dict):
        for key in schema.get('required', []):
            if key not in value:
                raise ValueError(at + ': missing ' + key)
        props = schema.get('properties', {})
        if schema.get('additionalProperties') is False and set(value) - set(props):
            raise ValueError(at + ': unknown fields')
        for key, child in props.items():
            if key in value:
                validate(value[key], child, at + '.' + key)
        extra = schema.get('additionalProperties')
        if isinstance(extra, dict):
            for key in set(value) - set(props):
                validate(value[key], extra, at + '.' + key)
    if isinstance(value, list):
        if len(value) < schema.get('minItems', 0):
            raise ValueError(at + ': too few items')
        for i, item in enumerate(value):
            validate(item, schema.get('items', {}), at + '[' + str(i) + ']')
    if isinstance(value, str):
        if len(value) < schema.get('minLength', 0):
            raise ValueError(at + ': empty value')
        if 'pattern' in schema and not re.search(schema['pattern'], value):
            raise ValueError(at + ': invalid format')
    if kind == 'integer' and value < schema.get('minimum', value):
        raise ValueError(at + ': below minimum')


def contract(name, value):
    validate(value, json.loads((HOME / 'harness/schemas' / (name + '.schema.json')).read_text()))


def atomic_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    if path.is_symlink():
        raise ValueError('Refusing symlink state file')
    fd, temp = tempfile.mkstemp(prefix='.ledger-', dir=path.parent)
    try:
        with os.fdopen(fd, 'w') as stream:
            json.dump(value, stream, indent=2)
            stream.write('\n')
        os.replace(temp, path)
    finally:
        if os.path.exists(temp):
            os.unlink(temp)


def state_path(root, task):
    if not TASK_RE.fullmatch(task):
        raise ValueError('Invalid task ID')
    directory = root / 'harness/state'
    if any(p.is_symlink() for p in [root / 'harness', directory]):
        raise ValueError('Refusing symlink state directory')
    return directory / (task + '.json')


def load_state(root, task):
    path = state_path(root, task)
    if path.is_symlink():
        raise ValueError('Refusing symlink state file')
    state = json.loads(path.read_text())
    contract('task-state', state)
    if state['task_id'] != task:
        raise ValueError('Task ID mismatch')
    return state


def save_state(root, state):
    state['updated_at'] = now()
    contract('task-state', state)
    atomic_json(state_path(root, state['task_id']), state)


def profile(root, require_commands=True):
    directory = root / 'harness/profiles'
    path = directory / 'local.json'
    if not path.exists():
        path = directory / 'default.json'
    value = json.loads(path.read_text())
    contract('profile', value)
    commands = value['verification']['commands']
    if require_commands and not commands and not value['verification']['allow_no_tests']:
        raise ValueError('Configure verification.commands before verifying this project')
    if len({c['name'] for c in commands}) != len(commands):
        raise ValueError('Verification command names must be unique')
    return value


def paths(root):
    raw = git(root, 'ls-files', '-z', '--cached', '--others', '--exclude-standard')
    return sorted(set(os.fsdecode(p) for p in raw.split(b'\0') if p))


def tree(root):
    result = {}
    for rel in paths(root):
        path = root / rel
        try:
            mode = path.lstat().st_mode
        except FileNotFoundError:
            continue
        if stat.S_ISLNK(mode):
            content, gitmode = os.fsencode(os.readlink(path)), '120000'
        elif stat.S_ISREG(mode):
            content = path.read_bytes()
            gitmode = '100755' if mode & stat.S_IXUSR else '100644'
        else:
            raise ValueError('Unsupported tree entry (submodules outside MVP): ' + rel)
        proc = subprocess.run(['git', '-C', str(root), 'hash-object', '--stdin'], input=content, capture_output=True, check=True)
        result[rel] = {'mode': gitmode, 'oid': proc.stdout.decode().strip()}
    return result


def snapshot(root):
    payload = {'head': git(root, 'rev-parse', '--verify', 'HEAD', check=False).decode().strip() or None,
               'branch': git(root, 'symbolic-ref', '--quiet', '--short', 'HEAD', check=False).decode().strip(),
               'tree': tree(root), 'index': digest(git(root, 'ls-files', '--stage', '-z')),
               'profile': digest(json.dumps(profile(root, False), sort_keys=True).encode())}
    payload['fingerprint'] = digest(json.dumps(payload, sort_keys=True).encode())
    return payload


def changed_paths(root):
    if git(root, 'rev-parse', '--verify', 'HEAD', check=False):
        raw = git(root, 'diff', 'HEAD', '--name-only', '-z', '--diff-filter=ACMRTUXB')
    else:
        raw = git(root, 'ls-files', '-z')
    raw += git(root, 'ls-files', '--others', '--exclude-standard', '-z')
    return sorted(set(os.fsdecode(p) for p in raw.split(b'\0') if p))


ASSIGNMENT_RE = re.compile(
    r"(?:password|passwd|secret|api[_-]?key|access[_-]?token|client[_-]?secret)"
    r"[\"']?\s*[:=]\s*[\"']?([A-Za-z0-9+/_.!@#-]{8,})", re.IGNORECASE)
PLACEHOLDER_RE = re.compile(r'(?:your[_-]|changeme|dummy|example|placeholder|xxxx)', re.IGNORECASE)


def scan_text(text, path):
    findings = []
    for n, line in enumerate(text.splitlines(), 1):
        if TOKEN_RE.search(line):
            category = 'credential-pattern'
        else:
            values = [m.group(1) for m in ASSIGNMENT_RE.finditer(line)]
            values = [v for v in values if not PLACEHOLDER_RE.match(v) and
                      not v.startswith(('process.env', 'os.environ', 'System.getenv', 'secretsmanager'))]
            if not values:
                continue
            category = 'suspicious-assignment'
        findings.append({'path': path, 'line': n, 'category': category})
    return findings


def secret_blockers(findings):
    # Suspicious assignments retain the original advisory semantics.
    return any(f['category'] != 'suspicious-assignment' for f in findings)


def secret_scan(root):
    findings = []
    for rel in changed_paths(root):
        path = root / rel
        if (path.name == '.env' or path.name.startswith('.env.')) and path.name not in TEMPLATES:
            findings.append({'path': rel, 'line': 0, 'category': 'local-env-file'})
        if not path.is_symlink() and path.is_file():
            findings.extend(scan_text(path.read_bytes().decode('utf-8', errors='replace'), rel))
        # Index content can differ from disk, including a staged secret later removed on disk.
        staged = git(root, 'show', ':' + rel, check=False)
        findings.extend(scan_text(staged.decode('utf-8', errors='replace'), rel + ' (index)'))
    return findings


def indexed_tree(root):
    result = {}
    for entry in git(root, 'ls-files', '--stage', '-z').split(b'\0'):
        if entry:
            metadata, rel = entry.split(b'\t', 1)
            mode, oid, stage = metadata.decode().split()
            if stage != '0':
                raise ValueError('Resolve index conflicts before readiness')
            result[os.fsdecode(rel)] = {'mode': mode, 'oid': oid}
    return result


def evidence(root, state):
    report = state.get('verification')
    if not report or not report['passed']:
        raise ValueError('No passing verification report')
    contract('verification', report)
    if report['snapshot']['fingerprint'] != snapshot(root)['fingerprint']:
        raise ValueError('Verification stale: HEAD, branch, index, profile or content changed')
    review = state.get('review')
    if not review or review['verdict'] != 'pass' or review['fingerprint'] != report['snapshot']['fingerprint']:
        raise ValueError('Passing review of verified snapshot required')
    from graph import readiness
    readiness(state, report['snapshot']['fingerprint'], root)
    return report


def run_command(root, command):
    try:
        proc = subprocess.Popen(command['argv'], cwd=root, stdout=subprocess.PIPE,
                                stderr=subprocess.PIPE, start_new_session=True)
    except OSError:
        return 127, b'command could not start', False
    try:
        out, err = proc.communicate(timeout=command['timeout_seconds'])
        return proc.returncode, out + b'\0' + err, False
    except subprocess.TimeoutExpired:
        try:
            os.killpg(proc.pid, signal.SIGKILL)
        except ProcessLookupError:
            pass
        out, err = proc.communicate()
        return 124, out + b'\0' + err, True


def do_verify(root, state):
    settings = profile(root)
    before = snapshot(root)
    findings = secret_scan(root)
    results = []
    for command in settings['verification']['commands']:
        code, output, timed_out = run_command(root, command)
        results.append({'name': command['name'], 'argv': command['argv'], 'exit_code': code,
                        'output_sha256': digest(output), 'timed_out': timed_out})
        print(command['name'] + ': ' + ('PASS' if code == 0 else 'FAIL (' + str(code) + ')'))
    unchanged = before['fingerprint'] == snapshot(root)['fingerprint']
    report = {'created_at': now(), 'snapshot': before, 'commands': results, 'secret_findings': findings,
              'unchanged': unchanged, 'passed': unchanged and not secret_blockers(findings) and all(r['exit_code'] == 0 for r in results)}
    contract('verification', report)
    state['verification'] = report
    state.pop('review', None)
    state['phase'] = 'verify' if report['passed'] else 'blocked'
    save_state(root, state)
    print(json.dumps({'passed': report['passed'], 'unchanged': unchanged, 'secret_findings': findings}, indent=2))
    return 0 if report['passed'] else 1


def committed_tree(root):
    result = {}
    for entry in git(root, 'ls-tree', '-r', '-z', 'HEAD').split(b'\0'):
        if entry:
            metadata, rel = entry.split(b'\t', 1)
            mode, kind, oid = metadata.decode().split()
            if kind != 'blob':
                raise ValueError('Submodules outside MVP scope')
            result[os.fsdecode(rel)] = {'mode': mode, 'oid': oid}
    return result


def doctor(root):
    failures, warnings = [], []
    try:
        profile(root)
    except (ValueError, OSError) as exc:
        failures.append(str(exc))
    expected = json.loads((HOME / 'harness/capabilities.json').read_text())
    for name in expected['skills']:
        path = root / '.agents/skills' / name / 'SKILL.md'
        if not path.exists():
            failures.append('Missing skill: ' + name)
            continue
        parts = path.read_text().split('---', 2)
        if len(parts) != 3 or parts[0].strip() or not re.search(r'^name: ' + re.escape(name) + r'$', parts[1], re.M) or not re.search(r'^description: \S', parts[1], re.M):
            failures.append('Invalid skill metadata: ' + name)
    for path in list((root / 'harness').rglob('*.json')) + [root / '.codex/hooks.json']:
        if 'state' in path.relative_to(root).parts:
            continue
        try:
            json.loads(path.read_text())
        except (OSError, ValueError):
            failures.append('Invalid JSON: ' + str(path.relative_to(root)))
    documents = list((root / '.agents').rglob('*.md')) + list((root / 'harness').rglob('*.md')) + [root / 'README.md']
    for path in documents:
        if 'archive' in path.relative_to(root).parts or 'state' in path.relative_to(root).parts:
            continue
        if path.exists():
            for link in re.findall(r'\]\(([^)\s]+)\)', path.read_text()):
                if not link.startswith(('https://', 'http://', '#')) and not (path.parent / link.split('#')[0]).exists():
                    failures.append('Broken link: ' + str(path.relative_to(root)) + ' -> ' + link)
    for ignored in ['harness/state/check.json', 'harness/profiles/local.json']:
        if subprocess.run(['git', '-C', str(root), 'check-ignore', '-q', ignored]).returncode:
            failures.append('Runtime path not ignored: ' + ignored)
    try:
        import tomllib
        for path in (root / '.codex').rglob('*.toml'):
            with path.open('rb') as stream:
                tomllib.load(stream)
    except ImportError:
        warnings.append('TOML validation needs Python 3.11+; CI covers this')
    except ValueError as exc:
        failures.append('Invalid TOML: ' + str(exc))
    if not (root / 'AGENTS.md').is_file():
        failures.append('Missing AGENTS.md')
    for name in expected['agents']:
        if not (root / '.codex/agents' / (name + '.toml')).is_file():
            failures.append('Missing agent: ' + name)
    for name in ['ticket', 'bugfix', 'release']:
        try:
            value = json.loads((root / 'harness/graph/workflows' / (name + '.json')).read_text())
            contract('workflow', value)
            ids = [node['id'] for node in value['nodes']]
            if len(set(ids)) != len(ids):
                failures.append('Duplicate graph node: ' + name)
            for node in value['nodes']:
                if node['skill'] not in expected['skills'] or any(dep not in ids for dep in node['dependencies']):
                    failures.append('Invalid graph reference: ' + name + ':' + node['id'])
        except (ValueError, OSError) as exc:
            failures.append('Invalid workflow ' + name + ': ' + str(exc))
    print(json.dumps({'ok': not failures, 'failures': failures, 'warnings': warnings}, indent=2))
    return int(bool(failures))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--repo', default='.')
    sub = parser.add_subparsers(dest='command', required=True)
    sub.add_parser('doctor')
    sub.add_parser('scan')
    start = sub.add_parser('start')
    start.add_argument('task')
    start.add_argument('--objective', required=True)
    start.add_argument('--base', required=True)
    for name in ['status', 'verify', 'ready', 'phase', 'review', 'ship', 'handoff']:
        child = sub.add_parser(name)
        child.add_argument('task')
        if name == 'phase':
            child.add_argument('phase', choices=['implement', 'blocked'])
        if name == 'review':
            child.add_argument('--verdict', choices=['pass', 'fail'], required=True)
            child.add_argument('--summary', required=True)
        if name == 'handoff':
            child.add_argument('--summary', required=True)
        if name == 'ship':
            child.add_argument('--url', required=True)
    args = parser.parse_args()
    try:
        root = repo_root(args.repo)
        if args.command == 'doctor':
            return doctor(root)
        if args.command == 'scan':
            findings = secret_scan(root)
            print(json.dumps({'secret_findings': findings}, indent=2))
            return int(secret_blockers(findings))
        if args.command == 'start':
            if state_path(root, args.task).exists():
                raise ValueError('Task exists; use status/handoff instead of overwriting')
            if not args.objective.strip():
                raise ValueError('Objective must not be empty')
            git(root, 'rev-parse', '--verify', args.base + '^{commit}')
            state = {'version': 1, 'task_id': args.task, 'objective': args.objective, 'base': args.base,
                     'phase': 'start', 'created_at': now(), 'updated_at': now()}
            save_state(root, state)
        else:
            state = load_state(root, args.task)
            if args.command == 'status':
                print(json.dumps(state, indent=2))
                return 0
            if state['phase'] == 'shipped' and args.command != 'handoff':
                raise ValueError('Shipped task is immutable; start a new task')
            if args.command == 'verify':
                return do_verify(root, state)
            if args.command == 'phase':
                state['phase'] = args.phase
                state.pop('verification', None)
                state.pop('review', None)
            elif args.command == 'review':
                if not args.summary.strip():
                    raise ValueError('Review summary must not be empty')
                state['review'] = {'verdict': args.verdict, 'summary': args.summary,
                                   'fingerprint': snapshot(root)['fingerprint'], 'created_at': now()}
            elif args.command == 'ready':
                evidence(root, state)
                if state['phase'] not in ['verify', 'ready_to_ship']:
                    raise ValueError('Verify before readiness')
                if indexed_tree(root) != state['verification']['snapshot']['tree']:
                    raise ValueError('Stage the intended snapshot before verify; index and working tree differ')
                state['phase'] = 'ready_to_ship'
            elif args.command == 'handoff':
                if not args.summary.strip():
                    raise ValueError('Handoff summary must not be empty')
                state['handoff'] = {'summary': args.summary, 'created_at': now()}
            elif args.command == 'ship':
                if state['phase'] != 'ready_to_ship':
                    raise ValueError('Run ready immediately before committing')
                report, review = state['verification'], state.get('review', {})
                if not report['passed'] or review.get('verdict') != 'pass' or review.get('fingerprint') != report['snapshot']['fingerprint']:
                    raise ValueError('Missing passing evidence')
                if git(root, 'status', '--porcelain'):
                    raise ValueError('Working tree/index must be clean after commit')
                parents = git(root, 'rev-list', '--parents', '-n', '1', 'HEAD').decode().split()
                if len(parents) != 2 or parents[1] != report['snapshot']['head']:
                    raise ValueError('Ship expects one new non-merge commit on verified HEAD')
                if committed_tree(root) != report['snapshot']['tree']:
                    raise ValueError('Committed tree differs from verified snapshot')
                if snapshot(root)['branch'] != report['snapshot']['branch']:
                    raise ValueError('Branch changed after verification')
                if not re.match(r'https?://\S+\Z', args.url):
                    raise ValueError('Review URL must be http(s)')
                state['phase'] = 'shipped'
                state['ship'] = {'url': args.url, 'head': parents[0], 'created_at': now()}
                if state.get('graph'):
                    from graph import readiness
                    readiness(state, report['snapshot']['fingerprint'], root)
                    state['graph']['results']['ship'] = {
                        'node': 'ship', 'status': 'pass', 'summary': args.url,
                        'fingerprint': report['snapshot']['fingerprint'],
                        'created_at': now(), 'attempt': 1}
            save_state(root, state)
        print(json.dumps({'task_id': state['task_id'], 'phase': state['phase']}, indent=2))
        return 0
    except (ValueError, OSError, KeyError, subprocess.SubprocessError) as exc:
        print('ERROR: ' + str(exc), file=sys.stderr)
        return 2


if __name__ == '__main__':
    sys.exit(main())
