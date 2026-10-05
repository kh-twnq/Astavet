#!/usr/bin/env python3
"""Read workspace snapshots, verify per-repo tasks, and audit local release evidence."""
import argparse
import json
from pathlib import Path
import shutil
import subprocess
import sys
import harness


def repositories(root):
    settings = harness.profile(root, False)
    entries = settings.get('workspace', {}).get('repos', []) or [{'name': root.name, 'path': '.'}]
    names = [entry['name'] for entry in entries]
    if len(names) != len(set(names)):
        raise ValueError('Workspace repo names must be unique')
    result = []
    roots = set()
    for entry in entries:
        path = Path(entry['path'])
        if not path.is_absolute():
            path = root / path
        try:
            resolved = harness.repo_root(path)
            if resolved != path.resolve():
                raise ValueError('Configured path must be its own Git root')
            if resolved in roots:
                raise ValueError('Duplicate workspace Git root')
            roots.add(resolved)
            result.append({'name': entry['name'], 'root': resolved, 'toolchain': entry.get('toolchain', 'unspecified')})
        except (ValueError, OSError) as exc:
            result.append({'name': entry['name'], 'error': str(exc)})
    for contract in settings.get('contracts', []):
        if contract['producer'] not in names or any(c not in names for c in contract['consumers']):
            raise ValueError('Contract names must reference configured workspace repositories')
    return result, settings


def snapshot(root):
    repos, settings = repositories(root)
    result = []
    protected = settings.get('policy', {}).get('protected_branches', [])
    for repo in repos:
        item = {'name': repo['name'], 'toolchain': repo.get('toolchain', 'unknown')}
        if 'error' in repo:
            item['error'] = repo['error']
            result.append(item)
            continue
        target = repo['root']
        branch = harness.git(target, 'symbolic-ref', '--short', '-q', 'HEAD', check=False).decode().strip() or '(detached)'
        status = harness.git(target, 'status', '--porcelain', '-z').split(b'\0')
        # Git status rename records use a second path; list statuses rather than guessing file counts.
        dirty = bool(status[0])
        upstream = subprocess.run(['git', '-C', str(target), 'rev-parse', '--verify', '@{u}'], capture_output=True)
        ahead = behind = None
        if upstream.returncode == 0:
            counts = harness.git(target, 'rev-list', '--left-right', '--count', 'HEAD...@{u}').decode().split()
            ahead, behind = map(int, counts)
        index = target / '.gitnexus'
        head_time = harness.git(target, 'log', '-1', '--format=%ct', check=False).decode().strip()
        index_times = [p.stat().st_mtime for p in index.rglob('*') if p.is_file()] if index.is_dir() else []
        stale = max(index_times) < int(head_time) if index_times and head_time else None
        item.update(path=str(target), branch=branch, dirty=dirty, protected=branch in protected,
                    ahead=ahead, behind=behind, gitnexus_stale=stale)
        result.append(item)
    return {'repos': result, 'contracts': settings.get('contracts', []),
            'tools': {tool: bool(shutil.which(tool)) for tool in ['git', 'python3', 'jq', 'gh', 'glab', 'java', 'node', 'docker']},
            'note': 'Missing upstream/index/connector is unknown. No remote API or DB was queried.'}


def verify(root, task):
    repos, settings = repositories(root)
    results = []
    for repo in repos:
        item = {'repo': repo['name']}
        if 'error' in repo:
            item.update(passed=False, error=repo['error'])
        else:
            target = repo['root']
            script = target / 'harness/scripts/harness.py'
            if not script.exists():
                item.update(passed=False, error='Install the harness at this Git root first')
            else:
                run = subprocess.run([sys.executable, str(script), '--repo', str(target), 'verify', task], capture_output=True, text=True)
                item.update(passed=run.returncode == 0, exit_code=run.returncode,
                            summary='Current per-repo verifier passed' if run.returncode == 0 else 'Verifier failed or task/profile is missing; run it in this repo for details')
        results.append(item)
    return {'task_id': task, 'passed': all(r['passed'] for r in results), 'repos': results,
            'contracts': settings.get('contracts', []),
            'contract_verdict': 'manual evidence required' if settings.get('contracts') else 'no declared contracts'}


def branch_gates(root, base, branch):
    result = {'base': base, 'branch': branch, 'outdated': 'unknown', 'no_conflict': 'unknown'}
    resolved = []
    for ref in (base, branch):
        probe = subprocess.run(['git', '-C', str(root), 'rev-parse', '--verify', '--end-of-options',
                                ref + '^{commit}'], capture_output=True, text=True)
        if probe.returncode:
            result['reason'] = 'Missing or invalid ref; confirm/fetch the audited base and branch'
            return result
        resolved.append(probe.stdout.strip())
    base_oid, branch_oid = resolved
    result.update(base_oid=base_oid, branch_oid=branch_oid,
                  freshness='Local refs; confirm remote freshness separately')
    behind = int(harness.git(root, 'rev-list', '--count', branch_oid + '..' + base_oid).strip())
    result.update(behind=behind, outdated='needs_rebase' if behind else 'pass')
    merge = subprocess.run(['git', '-C', str(root), 'merge-tree', '--write-tree', base_oid, branch_oid],
                           capture_output=True, text=True)
    if merge.returncode in (0, 1):
        result['no_conflict'] = 'pass' if merge.returncode == 0 else 'conflict'
    else:
        result['reason'] = 'Merge simulation unavailable; check Git support and related histories'
    return result


def audit(root, tasks, base=None, branch=None):
    if bool(base) != bool(branch):
        raise ValueError("--base and --branch must be supplied together")
    repos, settings = repositories(root)
    rows, ownership = [], {}
    for task in tasks:
        for repo in repos:
            row = {'task_id': task, 'repo': repo['name'], 'verdict': 'unknown'}
            if 'error' in repo:
                row['reason'] = repo['error']
            else:
                if base:
                    row['branch_gates'] = branch_gates(repo['root'], base, branch)
                try:
                    target = repo['root']
                    state = harness.load_state(target, task)
                    report = state.get('verification', {})
                    row.update(phase=state['phase'], objective=state['objective'],
                               checks_passed=report.get('passed', False), review_url=state.get('ship', {}).get('url'))
                    ship = state.get('ship')
                    if ship:
                        head = ship['head']
                        audited_ref = row.get('branch_gates', {}).get('base_oid', base or 'HEAD')
                        row['audited_ref'] = base or 'HEAD'
                        check = subprocess.run(['git', '-C', str(target), 'merge-base', '--is-ancestor', head, audited_ref], capture_output=True)
                        row['commit_reachable'] = check.returncode == 0
                        row['verdict'] = 'shipped_local_evidence' if check.returncode == 0 else 'unknown'
                        row['reason'] = 'Inspect acceptance content on the release ref; local ancestry does not establish remote merge/deploy' if check.returncode == 0 else 'Commit not reachable; check content after squash/rebase'
                        paths = harness.git(target, 'diff-tree', '--no-commit-id', '--name-only', '-r', '-z', head).split(b'\0')
                        row['changed_files'] = [p.decode(errors='replace') for p in paths if p]
                        for path in row['changed_files']:
                            ownership.setdefault(repo['name'] + ':' + path, set()).add(task)
                    else:
                        row['verdict'] = 'partial'
                        row['reason'] = 'No local ship evidence; verify actual release content independently'
                except (ValueError, OSError, KeyError) as exc:
                    row['reason'] = str(exc)
            rows.append(row)
    return {'tickets': rows, 'overlap_leads': [{'location': p, 'tasks': sorted(t)} for p, t in ownership.items() if len(t) > 1],
            'contracts': settings.get('contracts', []), 'recommendation': 'requires acceptance and cross-ticket content review',
            'note': 'Read-only local evidence audit; no checkout, stash, ticket transition or release.'}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--repo', default='.')
    sub = parser.add_subparsers(dest='command', required=True)
    sub.add_parser('snapshot')
    verify_cmd = sub.add_parser('verify')
    verify_cmd.add_argument('--task', required=True)
    audit_cmd = sub.add_parser('audit')
    audit_cmd.add_argument('--task', nargs='+', required=True)
    audit_cmd.add_argument('--base', help='Explicit local release/base ref for every configured repo')
    audit_cmd.add_argument('--branch', help='Ticket branch/ref to compare with --base')
    args = parser.parse_args()
    try:
        root = harness.repo_root(args.repo)
        if args.command == 'snapshot':
            value = snapshot(root)
        elif args.command == 'verify':
            value = verify(root, args.task)
        else:
            value = audit(root, args.task, args.base, args.branch)
        print(json.dumps(value, indent=2))
        if args.command == 'verify':
            return 0 if value['passed'] else 1
        if args.command == 'snapshot':
            return int(any('error' in row for row in value['repos']))
        return 0
    except (ValueError, OSError, KeyError) as exc:
        print('ERROR: ' + str(exc), file=sys.stderr)
        return 2


if __name__ == '__main__':
    sys.exit(main())
