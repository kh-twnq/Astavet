#!/usr/bin/env python3
"""Route and record Codex workflow nodes in the existing task ledger."""
import argparse
import json
import sys
import harness


FRESH_NODES = {'integrate', 'test', 'review', 'security'}


def workflow(name):
    path = harness.HOME / 'harness/graph/workflows' / (name + '.json')
    value = json.loads(path.read_text())
    harness.contract('workflow', value)
    return value


def route(name, repo_count=1, security=False, contract=False, docs_only=False):
    definition = workflow(name)
    nodes = []
    for node in definition['nodes']:
        condition = node['condition']
        if condition == 'always' or condition == 'security' and security or condition == 'cross_repo_contract' and repo_count >= 2 and contract or condition == 'code_change' and not docs_only:
            nodes.append(node['id'])
    return nodes


def dependencies(graph, node_id):
    spec = next((n for n in workflow(graph['workflow'])['nodes'] if n['id'] == node_id), None)
    if spec is None or node_id not in graph['planned_nodes']:
        raise ValueError('Node is not routed into this graph')
    return spec, [dep for dep in spec['dependencies'] if dep in graph['planned_nodes']]


def readiness(state, fingerprint, root=None):
    graph = state.get('graph')
    if not graph:
        return
    if 'ship' not in graph['planned_nodes']:
        raise ValueError('Read-only release audit graph cannot ship')
    for node in graph['planned_nodes']:
        if node == 'ship':
            continue
        result = graph['results'].get(node)
        if not result or result['status'] != 'pass':
            raise ValueError('Graph dependency incomplete: ' + node)
        if node in FRESH_NODES and result['fingerprint'] != fingerprint:
            raise ValueError('Graph evidence is stale: ' + node)
        if node == 'integrate' and root is not None:
            from workspace import repositories
            repos, _ = repositories(root)
            bindings = result.get('repo_fingerprints', {})
            for repo in repos:
                if repo['name'] in graph['repos'] and repo.get('root') != root:
                    if 'error' in repo or bindings.get(repo['name']) != harness.snapshot(repo['root'])['fingerprint']:
                        raise ValueError('Cross-repo integration evidence is stale: ' + repo['name'])


def record(root, state, node_id, status, summary):
    if not summary.strip():
        raise ValueError('Node evidence summary is required')
    graph = state.get('graph')
    if not graph:
        raise ValueError('Initialize graph first')
    spec, deps = dependencies(graph, node_id)
    for dep in deps:
        if graph['results'].get(dep, {}).get('status') != 'pass':
            raise ValueError('Dependency not passed: ' + dep)
    count = graph['attempts'].get(node_id, 0) + 1
    if count > spec['maximum_attempts']:
        raise ValueError('Node attempt limit reached; use retry after resolving the cause')
    fingerprint = harness.snapshot(root)['fingerprint']
    if status == 'pass':
        if node_id == 'test':
            report = state.get('verification', {})
            if not report.get('passed') or report['snapshot']['fingerprint'] != fingerprint:
                raise ValueError('Test node requires current passing verifier evidence')
        elif node_id == 'review':
            review = state.get('review', {})
            if review.get('verdict') != 'pass' or review.get('fingerprint') != fingerprint:
                raise ValueError('Review node requires current passing review evidence')
        elif node_id == 'security':
            if harness.secret_blockers(harness.secret_scan(root)):
                raise ValueError('Security node cannot pass while secret scan is red')
        elif node_id == 'ship':
            raise ValueError('Ship node is recorded by harness.py ship after local commit checks')
    result = {'node': node_id, 'status': status, 'summary': summary,
              'fingerprint': fingerprint, 'created_at': harness.now(), 'attempt': count}
    if node_id == 'integrate' and status == 'pass':
        from workspace import repositories
        repos, _ = repositories(root)
        mapped = {repo['name']: repo for repo in repos}
        bindings = {}
        for name in graph['repos']:
            repo = mapped.get(name)
            if not repo or 'error' in repo:
                raise ValueError('Integration requires a configured reachable repo: ' + name)
            target = repo['root']
            current = harness.snapshot(target)
            ledger = harness.load_state(target, state['task_id'])
            report = ledger.get('verification', {})
            if not report.get('passed'):
                raise ValueError('Integration requires passing per-repo verification: ' + name)
            if report['snapshot']['fingerprint'] != current['fingerprint']:
                # A dependency may already have shipped exactly its verified tree.
                if ledger.get('ship', {}).get('head') != current['head'] or current['tree'] != report['snapshot']['tree'] or harness.git(target, 'status', '--porcelain'):
                    raise ValueError('Integration requires current per-repo verification: ' + name)
            bindings[name] = current['fingerprint']
        result['repo_fingerprints'] = bindings
    harness.contract('node-result', result)
    graph['attempts'][node_id] = count
    graph['results'][node_id] = result
    # Changed producer evidence invalidates downstream conclusions even if content is unchanged.
    invalid = {node_id}
    for node in workflow(graph['workflow'])['nodes']:
        if node['id'] != node_id and any(dep in invalid for dep in node['dependencies']):
            graph['results'].pop(node['id'], None)
            invalid.add(node['id'])
    state['phase'] = 'blocked' if status != 'pass' else state['phase']
    harness.save_state(root, state)
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--repo', default='.')
    sub = parser.add_subparsers(dest='command', required=True)
    init = sub.add_parser('init')
    init.add_argument('task')
    init.add_argument('--workflow', choices=['ticket', 'bugfix', 'release'], default='ticket')
    init.add_argument('--repos', nargs='+')
    init.add_argument('--security', action='store_true')
    init.add_argument('--cross-repo-contract', action='store_true')
    init.add_argument('--docs-only', action='store_true')
    complete = sub.add_parser('record')
    complete.add_argument('task')
    complete.add_argument('node')
    complete.add_argument('--status', choices=['pass', 'fail', 'blocked'], required=True)
    complete.add_argument('--summary', required=True)
    status = sub.add_parser('status')
    status.add_argument('task')
    retry = sub.add_parser('retry')
    retry.add_argument('task')
    retry.add_argument('node')
    retry.add_argument('--reason', required=True)
    args = parser.parse_args()
    try:
        root = harness.repo_root(args.repo)
        state = harness.load_state(root, args.task)
        if state['phase'] == 'shipped' and args.command != 'status':
            raise ValueError('Shipped task graph is immutable')
        if args.command == 'init':
            if state.get('graph'):
                raise ValueError('Graph already exists; inspect status instead of replacing it')
            if args.docs_only and args.workflow == 'bugfix':
                raise ValueError('Bugfix workflow requires regression verification')
            names = args.repos or [root.name]
            if len(set(names)) != len(names):
                raise ValueError('Graph repository names must be unique')
            if args.cross_repo_contract and len(names) < 2:
                raise ValueError('Cross-repo integration requires at least two repos')
            state['graph'] = {'workflow': args.workflow, 'repos': names,
                              'planned_nodes': route(args.workflow, len(names), args.security, args.cross_repo_contract, args.docs_only),
                              'results': {}, 'attempts': {}, 'retry_reasons': []}
            harness.save_state(root, state)
        elif args.command == 'record':
            record(root, state, args.node, args.status, args.summary)
        elif args.command == 'retry':
            graph = state.get('graph')
            if not graph:
                raise ValueError('Initialize graph first')
            dependencies(graph, args.node)
            if not args.reason.strip():
                raise ValueError('Retry reason must describe new evidence or corrective work')
            graph['retry_reasons'].append({'node': args.node, 'reason': args.reason, 'created_at': harness.now()})
            graph['attempts'][args.node] = 0
            graph['results'].pop(args.node, None)
            harness.save_state(root, state)
        print(json.dumps(state.get('graph'), indent=2))
        return 0
    except (ValueError, OSError, KeyError, StopIteration) as exc:
        print('ERROR: ' + str(exc), file=sys.stderr)
        return 2


if __name__ == '__main__':
    sys.exit(main())
