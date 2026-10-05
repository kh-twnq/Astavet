#!/usr/bin/env python3
"""Check native Codex discovery/config loading without starting a model turn."""
import argparse
import json
from pathlib import Path
import queue
import shutil
import subprocess
import sys
import threading
import time
from harness import HOME


def probe(root, timeout=20):
    executable = shutil.which('codex')
    if not executable:
        raise ValueError('Codex CLI unavailable; install the CLI to run native checks')
    root = root.resolve()
    messages = queue.Queue()
    process = subprocess.Popen([executable, 'app-server', '--stdio'], cwd=root,
                               stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                               stderr=subprocess.DEVNULL, text=True)

    def read_messages():
        for line in process.stdout:
            try:
                messages.put(json.loads(line))
            except ValueError:
                continue
        messages.put(None)

    threading.Thread(target=read_messages, daemon=True).start()

    def send(value):
        process.stdin.write(json.dumps(value) + '\n')
        process.stdin.flush()

    def request(identifier, method, params):
        send({'id': identifier, 'method': method, 'params': params})
        deadline = time.monotonic() + timeout
        while True:
            message = messages.get(timeout=max(0, deadline - time.monotonic()))
            if message is None:
                raise ValueError('Native app-server exited before responding')
            if message.get('id') == identifier:
                if 'error' in message:
                    raise ValueError('Native request failed: ' + method)
                return message['result']

    try:
        request(1, 'initialize', {'clientInfo': {'name': 'codex_harness_check', 'version': '1'},
                                  'capabilities': {'experimentalApi': True}})
        send({'method': 'initialized'})
        config = request(2, 'config/read', {'cwd': str(root), 'includeLayers': True})
        listing = request(3, 'skills/list', {'cwds': [str(root)], 'forceReload': True})
        local = [s for row in listing['data'] for s in row['skills']
                 if Path(s['path']).resolve().is_relative_to(root / '.agents/skills')]
        actual = {s['name'] for s in local if s['enabled']}
        declared = set(json.loads((root / 'harness/capabilities.json').read_text())['skills'])
        errors = [e['message'] for row in listing['data'] for e in row['errors']
                  if Path(e['path']).resolve().is_relative_to(root)]
        layers = [layer for layer in config.get('layers', []) or []
                  if isinstance(layer.get('name'), dict) and
                  layer['name'].get('dotCodexFolder') == str(root / '.codex')]
        active = any(not layer.get('disabledReason') for layer in layers)
        failures = []
        if actual != declared or errors:
            failures.append('Native skill discovery differs from the capabilities manifest or has parse errors')
        if not active:
            failures.append('Project config is not loaded; review and trust this project in the Codex client, then rerun')
        return {'ok': not failures, 'native_skills': len(actual), 'project_config_loaded': active,
                'skill_errors': errors, 'failures': failures,
                'note': 'No model turn, tool execution, agent delegation or connector mutation was performed. Hook execution and agent behavior need a trusted live session.'}
    finally:
        process.terminate()
        try:
            process.wait(timeout=5)
        except subprocess.TimeoutExpired:
            process.kill(); process.wait()
        process.stdin.close(); process.stdout.close()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--repo', type=Path, default=HOME)
    args = parser.parse_args()
    try:
        result = probe(args.repo)
        print(json.dumps(result, indent=2))
        return int(not result['ok'])
    except (ValueError, OSError, KeyError, TypeError, queue.Empty) as exc:
        print('ERROR: ' + (str(exc) or 'Native app-server response timed out'), file=sys.stderr)
        return 2


if __name__ == '__main__':
    sys.exit(main())
