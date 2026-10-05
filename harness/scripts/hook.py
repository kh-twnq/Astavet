#!/usr/bin/env python3
"""Codex hook adapter; accidental-destruction guard, not a shell sandbox."""
import json
from pathlib import Path
import re
import shlex
import sys
from harness import repo_root, scan_text, TEMPLATES


def env_file(path):
    name = Path(path).name
    return (name == '.env' or name.startswith('.env.')) and name not in TEMPLATES


def shell_reason(command, depth=0):
    if depth > 5:
        return 'Nested command wrappers exceed guard inspection depth'
    try:
        tokens = list(shlex.shlex(command, posix=True, punctuation_chars=';&|()'))
    except ValueError:
        return 'Cannot inspect malformed shell quoting'
    for i, token in enumerate(tokens):
        exe = Path(token).name
        tail = tokens[i + 1:]
        # Recursively inspect shell execution; quoted commit messages remain data.
        if exe in {'bash', 'sh', 'zsh', 'dash', 'ksh'}:
            for j, arg in enumerate(tail):
                if arg.startswith('-') and 'c' in arg[1:] and j + 1 < len(tail):
                    reason = shell_reason(tail[j + 1], depth + 1)
                    if reason:
                        return reason
                    break
        if exe == 'git':
            j = 0
            while j < len(tail) and tail[j].startswith('-'):
                arg = tail[j]
                if arg in {'-C', '-c', '--git-dir', '--work-tree'}:
                    j += 2
                else:
                    j += 1
            if j >= len(tail):
                continue
            sub = tail[j]
            args = []
            for arg in tail[j + 1:]:
                if arg and all(c in ';&|()' for c in arg):
                    break
                args.append(arg)
            if sub == 'reset' and '--hard' in args:
                return 'git reset --hard discards uncommitted work'
            if sub == 'clean' and any(a.startswith('-') and not a.startswith('--') and 'f' in a for a in args):
                return 'git clean -f deletes untracked work'
            if sub == 'branch' and ('-D' in args or ('--delete' in args and '--force' in args) or '-df' in args or '-fd' in args):
                return 'Forced branch deletion can lose unmerged work'
            if sub in {'checkout', 'restore'} and any(a.rstrip('/') in {'.', '*'} for a in args):
                return 'Restoring the entire worktree discards uncommitted work'
            if sub == 'push' and any(a == '-f' or a == '--force' or a.startswith('--force=') or a.startswith('+') for a in args):
                return 'Force push can overwrite remote history; inspect a lease-based alternative'
        if exe == 'rm':
            flags, targets = [], []
            for arg in tail:
                if arg and all(c in ';&|()' for c in arg):
                    break
                (flags if arg.startswith('-') else targets).append(arg)
            short = ''.join(a[1:] for a in flags if not a.startswith('--'))
            recursive = 'r' in short or 'R' in short or '--recursive' in flags
            force = 'f' in short or '--force' in flags
            if recursive and force:
                for target in targets:
                    normalized = target.rstrip('/') or '/'
                    if normalized in {'/', '.', '~', '$HOME', '${HOME}', '*'} or Path(normalized).name == '.git' or env_file(normalized):
                        return 'Recursive forced removal of repository/env/root/home target'
    return None


def pre_reason(payload):
    tool = payload.get('tool_name', '')
    args = payload.get('tool_input', {})
    if not isinstance(args, dict):
        return 'Unsupported tool input shape'
    if any(key in args and not isinstance(args[key], str)
           for key in ['command', 'cmd', 'patch', 'input', 'file_path', 'content', 'new_string']):
        return 'Unsupported tool argument type'
    if tool in {'Bash', 'exec_command'}:
        return shell_reason(args.get('command', args.get('cmd', '')))
    if tool == 'apply_patch':
        patch = args.get('command', args.get('patch', args.get('input', '')))
        path = '<patch>'
        added = []
        for line in patch.splitlines():
            if line.startswith(('*** Add File: ', '*** Update File: ', '*** Delete File: ', '*** Move to: ')):
                path = line.split(': ', 1)[1]
                if env_file(path):
                    return 'Local .env files must be configured outside agent edits'
            elif line.startswith('+') and not line.startswith('+++'):
                added.append(line[1:])
        if scan_text('\n'.join(added), '<patch>'):
            return 'Added content contains a high-confidence credential pattern'
    elif tool in {'Edit', 'Write'}:
        if env_file(args.get('file_path', '')):
            return 'Local .env files must be configured outside agent edits'
        if scan_text(args.get('content', args.get('new_string', '')), '<edit>'):
            return 'Added content contains a high-confidence credential pattern'
    return None


def main():
    event = 'PreToolUse'
    try:
        payload = json.load(sys.stdin)
        if not isinstance(payload, dict):
            raise ValueError('Expected hook object')
        event = payload.get('hook_event_name', 'PreToolUse')
        output = {'hookEventName': event}
        if event == 'PreToolUse':
            reason = pre_reason(payload)
            if reason:
                output.update(permissionDecision='deny', permissionDecisionReason=reason)
        elif event == 'PostToolUse':
            output['additionalContext'] = 'Edits may invalidate verification. Rerun verify and review before ready/ship.'
            args = payload.get('tool_input', {})
            content = args.get('command', args.get('file_path', '')) if isinstance(args, dict) else ''
            if '.java' in content:
                output['additionalContext'] += ' Java gate: read harness/rules/java.md, match the JDK, review layering and configured comments, and run affected tests.'
        elif event == 'SessionStart':
            root = repo_root(payload.get('cwd', '.'))
            summaries = []
            for path in sorted((root / 'harness/state').glob('*.json'))[:10]:
                value = json.loads(path.read_text())
                summaries.append(value.get('task_id', '?') + ': ' + value.get('phase', '?'))
            output['additionalContext'] = 'Read AGENTS.md and project profile. Local tasks: ' + (', '.join(summaries) or 'none') + '. Recheck evidence before shipping.'
            from workspace import snapshot
            view = snapshot(root)
            for repo in view['repos']:
                if 'error' in repo:
                    output['additionalContext'] += ' ' + repo['name'] + ': unavailable.'
                else:
                    flags = (' dirty' if repo['dirty'] else '') + (' protected' if repo['protected'] else '')
                    flags += ' GitNexus may be stale' if repo['gitnexus_stale'] else ''
                    output['additionalContext'] += ' ' + repo['name'] + ': ' + repo['branch'] + flags + '.'
            output['additionalContext'] += ' Tool availability: ' + ', '.join(name for name, available in view['tools'].items() if available) + '.'
        print(json.dumps({'hookSpecificOutput': output}))
        return 0
    except (ValueError, OSError, TypeError) as exc:
        # Explicit denial for pre-tool adapter failure; advisory events report context.
        output = {'hookEventName': event}
        if event == 'PreToolUse':
            output.update(permissionDecision='deny',
                          permissionDecisionReason='Harness could not inspect hook payload; check adapter configuration')
        else:
            output['additionalContext'] = 'Harness snapshot/reminder unavailable; check adapter configuration.'
        print(json.dumps({'hookSpecificOutput': output}))
        print('hook input/configuration error: ' + type(exc).__name__, file=sys.stderr)
        return 0


if __name__ == '__main__':
    sys.exit(main())
