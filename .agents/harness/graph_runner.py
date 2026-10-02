#!/usr/bin/env python3
"""Run a bounded, stateful Codex execution graph for a local project."""

import argparse
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
from datetime import datetime, timezone


SCHEMAS = {
    "plan": {
        "decision": ["ready", "blocked"],
        "summary": "string",
        "steps": "array",
        "checks": "array",
    },
    "execute": {
        "decision": ["done", "blocked"],
        "summary": "string",
        "checks": "array",
    },
    "review": {
        "verdict": ["pass", "issues", "blocked"],
        "summary": "string",
        "findings": "array",
    },
}


def schema_for(node):
    properties = {}
    for key, kind in SCHEMAS[node].items():
        if isinstance(kind, list):
            properties[key] = {"type": "string", "enum": kind}
        elif kind == "array":
            properties[key] = {"type": "array", "items": {"type": "string"}}
        else:
            properties[key] = {"type": "string"}
    return {
        "type": "object",
        "properties": properties,
        "required": list(properties),
        "additionalProperties": False,
    }


def validate_result(node, result):
    spec = SCHEMAS[node]
    if not isinstance(result, dict) or set(result) != set(spec):
        raise ValueError("Codex returned an unexpected result shape")
    for key, kind in spec.items():
        value = result[key]
        if isinstance(kind, list) and value not in kind:
            raise ValueError("Codex returned an invalid " + key)
        if kind == "array" and (not isinstance(value, list) or
                                not all(isinstance(item, str) for item in value)):
            raise ValueError("Codex returned an invalid " + key)
        if kind == "string" and not isinstance(value, str):
            raise ValueError("Codex returned an invalid " + key)


def write_json(path, value):
    temporary = path.with_suffix(path.suffix + ".tmp")
    with temporary.open("w", encoding="utf-8") as handle:
        json.dump(value, handle, ensure_ascii=False, indent=2)
        handle.write("\n")
    os.replace(temporary, path)


def git_status(project):
    result = subprocess.run(
        ["git", "status", "--short", "--untracked-files=normal"],
        cwd=str(project), text=True, capture_output=True, check=False,
    )
    return result.stdout.strip() if result.returncode == 0 else None


def prompt_for(state, node):
    base = (
        "You are one node in an externally controlled Codex execution graph. "
        "Do not launch another graph run or spawn subagents. Follow the user's "
        "request and this project's AGENTS.md and relevant guidance. Do not "
        "commit, push, deploy, or publish. Do not use GitNexus merely because "
        "it is installed globally; use it only when this project requires it "
        "or an index for this exact project is confirmed. Report only work "
        "you actually did.\n\n"
        "Project: " + state["project"] + "\n"
        "Mode: " + state["mode"] + "\n"
        "Task: " + state["task"] + "\n"
        "Initial working tree status:\n" +
        (state["initial_status"] or "(clean or not a Git repository)") + "\n\n"
    )
    if node == "plan":
        return base + (
            "Read relevant source and instructions without editing. This plan "
            "node is intentionally read-only; a later execute node will have "
            "workspace-write access. Do not mark the plan blocked merely because "
            "you cannot write files in this node. Define the smallest "
            "implementation plan and checks appropriate to this project. "
            "Return decision=blocked if a material requirement is missing or a "
            "required safety precondition for the later execute node cannot "
            "be met; otherwise ready."
        )
    if node == "execute":
        previous = state.get("plan", {})
        repair = state.get("review", {}) if state["repairs_used"] else {}
        return base + (
            "Implement the scoped change. Preserve existing changes. Follow "
            "project safety gates before running checks. If a check fails, "
            "investigate within scope. Return decision=blocked for missing "
            "requirements or safety gates. In checks, list commands actually "
            "run with their result; do not label unrun checks as passed.\n\n"
            "Plan: " + json.dumps(previous, ensure_ascii=False) + "\n"
            "Prior review to address: " + json.dumps(repair, ensure_ascii=False)
        )
    previous = state.get("execute", {})
    if state["mode"] == "review":
        instruction = (
            "Review the requested change without editing. If no change target "
            "was given, review the current working tree diff. Return verdict=issues "
            "for actionable defects, pass if none are supported, or blocked if "
            "there is no reviewable target. Give file and line evidence in findings."
        )
    else:
        instruction = (
            "Independently inspect the resulting diff, relevant source, and "
            "verification evidence without editing. Return verdict=issues for "
            "actionable defects or missing required verification; pass only if "
            "the requested outcome and applicable gates are supported; blocked "
            "if you cannot assess them. Give file and line evidence in findings. "
            "The execute node's check list is self-reported evidence."
        )
    return base + instruction + "\n\nExecute result: " + json.dumps(previous, ensure_ascii=False)


def run_codex(state, node, index, codex_bin, timeout):
    run_dir = Path(state["run_dir"])
    stem = "%02d-%s" % (index, node)
    schema_path = run_dir / (stem + "-schema.json")
    result_path = run_dir / (stem + "-result.json")
    events_path = run_dir / (stem + "-events.jsonl")
    errors_path = run_dir / (stem + "-stderr.txt")
    write_json(schema_path, schema_for(node))
    sandbox = "workspace-write" if node == "execute" else "read-only"
    command = [
        codex_bin, "exec", "--cd", state["project"],
        "--skip-git-repo-check", "--sandbox", sandbox,
        "--config", "approval_policy=never", "--json",
        "--output-schema", str(schema_path),
        "--output-last-message", str(result_path), "-",
    ]
    if state.get("model"):
        command[2:2] = ["--model", state["model"]]
    environment = os.environ.copy()
    environment["CODEX_GRAPH_NODE"] = "1"
    try:
        with events_path.open("w", encoding="utf-8") as events, \
                errors_path.open("w", encoding="utf-8") as errors:
            process = subprocess.run(
                command, input=prompt_for(state, node), text=True,
                stdout=events, stderr=errors, cwd=state["project"],
                env=environment, timeout=timeout, check=False,
            )
    except subprocess.TimeoutExpired as error:
        raise RuntimeError("Codex node timed out after %d seconds" % timeout) from error
    if process.returncode:
        raise RuntimeError(
            "Codex node failed (exit %d); see %s" % (process.returncode, errors_path)
        )
    try:
        result = json.loads(result_path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        raise RuntimeError("Codex did not write a valid result: " + str(result_path)) from error
    validate_result(node, result)
    return result


def advance(state, node, result, max_repairs):
    state[node] = result
    if node == "plan":
        state["next"] = "execute" if result["decision"] == "ready" else None
        state["status"] = "running" if state["next"] else "blocked"
    elif node == "execute":
        state["next"] = "review" if result["decision"] == "done" else None
        state["status"] = "running" if state["next"] else "blocked"
    elif result["verdict"] == "blocked":
        state["next"] = None
        state["status"] = "blocked"
    elif state["mode"] == "review":
        state["next"] = None
        state["status"] = "reviewed"
    elif result["verdict"] == "pass":
        state["next"] = None
        state["status"] = "complete"
    elif state["repairs_used"] < max_repairs:
        state["repairs_used"] += 1
        state["next"] = "execute"
        state["status"] = "running"
    else:
        state["next"] = None
        state["status"] = "issues"
    return state


def run_graph(state, codex_bin, timeout, max_repairs, invoke=run_codex):
    state_path = Path(state["run_dir"]) / "state.json"
    write_json(state_path, state)
    while state["next"]:
        node = state["next"]
        index = len(state["history"]) + 1
        state["active"] = node
        write_json(state_path, state)
        try:
            result = invoke(state, node, index, codex_bin, timeout)
        except (OSError, ValueError, RuntimeError) as error:
            state["status"] = "blocked"
            state["next"] = None
            state["error"] = str(error)
            write_json(state_path, state)
            break
        state["history"].append({"node": node, "result": result})
        advance(state, node, result, max_repairs)
        state["active"] = None
        write_json(state_path, state)
    return state


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--project", type=Path, default=Path.cwd())
    parser.add_argument("--mode", choices=("feature", "fix", "review"), required=True)
    task = parser.add_mutually_exclusive_group(required=True)
    task.add_argument("--task")
    task.add_argument("--task-file", type=Path)
    parser.add_argument("--codex-bin", default="codex")
    parser.add_argument("--model", help="Codex model for every node; defaults to CLI config")
    parser.add_argument("--timeout-seconds", type=int, default=900)
    parser.add_argument("--max-repairs", type=int, default=1)
    parser.add_argument("--run-dir", type=Path,
                        help="New private directory for state and node logs")
    args = parser.parse_args(argv)
    if not args.project.is_dir():
        parser.error("--project must be an existing directory")
    if args.timeout_seconds < 1 or not 0 <= args.max_repairs <= 3:
        parser.error("timeout must be positive and max repairs must be 0 to 3")
    task_text = args.task if args.task is not None else args.task_file.read_text(encoding="utf-8")
    if not task_text.strip():
        parser.error("task must not be empty")
    project = args.project.resolve()
    if args.run_dir:
        run_dir = args.run_dir.resolve()
        run_dir.mkdir(mode=0o700, parents=True, exist_ok=False)
    else:
        run_dir = Path(tempfile.mkdtemp(prefix="codex-graph-"))
    state = {
        "version": 1,
        "created_at": datetime.now(timezone.utc).isoformat(),
        "project": str(project), "mode": args.mode, "task": task_text,
        "model": args.model,
        "initial_status": git_status(project), "run_dir": str(run_dir),
        "status": "running", "active": None,
        "next": "review" if args.mode == "review" else "plan",
        "repairs_used": 0, "history": [],
    }
    print("Run state: " + str(run_dir / "state.json"), flush=True)
    result = run_graph(state, args.codex_bin, args.timeout_seconds, args.max_repairs)
    print("Status: " + result["status"])
    if result.get("error"):
        print("Error: " + result["error"], file=sys.stderr)
    if result.get("review"):
        print("Review: " + result["review"]["summary"])
        for finding in result["review"]["findings"]:
            print("- " + finding)
    return 0 if result["status"] in ("complete", "reviewed") else 1


if __name__ == "__main__":
    sys.exit(main())
