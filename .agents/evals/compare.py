"""Validate paired AstaVet harness trial records and print a descriptive summary."""

import argparse
import json
import statistics
from pathlib import Path


VARIANTS = {"baseline", "candidate"}
CHECK_STATUSES = {"passed", "failed", "skipped", "blocked"}


def read_json(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))


def validate_cases(cases):
    if not isinstance(cases, list) or not cases:
        raise ValueError("cases must be a non-empty list")
    ids = set()
    for case in cases:
        case_id = case.get("id")
        if not isinstance(case_id, str) or not case_id or case_id in ids:
            raise ValueError(f"invalid or duplicate case id: {case_id!r}")
        ids.add(case_id)
        if not isinstance(case.get("prompt"), str) or not case["prompt"]:
            raise ValueError(f"{case_id}: prompt is required")
        if not isinstance(case.get("required_checks"), list) or not case["required_checks"]:
            raise ValueError(f"{case_id}: required_checks must be non-empty")
        seed = case.get("seed")
        if not isinstance(seed, dict) or any(not isinstance(seed.get(key), str) or not seed[key] for key in ("path", "find", "replace")):
            raise ValueError(f"{case_id}: seed path/find/replace are required")
        path = Path(seed["path"])
        if path.is_absolute() or ".." in path.parts:
            raise ValueError(f"{case_id}: seed path must stay inside the trial checkout")
    return ids


def validate_runs(runs, case_ids):
    if not isinstance(runs, list) or not runs:
        raise ValueError("runs must be a non-empty list")
    pairs = {}
    for run in runs:
        case_id = run.get("case_id")
        variant = run.get("variant")
        trial = run.get("trial")
        if case_id not in case_ids or variant not in VARIANTS or type(trial) is not int or trial < 1:
            raise ValueError(f"invalid case/variant/trial: {case_id!r}/{variant!r}/{trial!r}")
        for field in ("base_commit", "seed_sha256", "prompt_sha256", "codex_version", "model", "evidence"):
            if not isinstance(run.get(field), str) or not run[field].strip():
                raise ValueError(f"{case_id}/{variant}/{trial}: {field} is required")
        for field in ("outcome_pass", "safety_pass"):
            if type(run.get(field)) is not bool:
                raise ValueError(f"{case_id}/{variant}/{trial}: {field} must be boolean")
        if not isinstance(run.get("wall_seconds"), (int, float)) or isinstance(run["wall_seconds"], bool) or run["wall_seconds"] < 0:
            raise ValueError(f"{case_id}/{variant}/{trial}: wall_seconds must be non-negative")
        for field in ("input_tokens", "output_tokens"):
            value = run.get(field)
            if value is not None and (type(value) is not int or value < 0):
                raise ValueError(f"{case_id}/{variant}/{trial}: {field} must be non-negative integer or null")
        checks = run.get("checks")
        if not isinstance(checks, list) or not checks:
            raise ValueError(f"{case_id}/{variant}/{trial}: at least one check is required")
        for check in checks:
            if check.get("status") not in CHECK_STATUSES or any(not isinstance(check.get(key), str) or not check[key].strip() for key in ("command", "evidence")):
                raise ValueError(f"{case_id}/{variant}/{trial}: check needs command, status and evidence")
        key = (case_id, trial)
        pair = pairs.setdefault(key, {})
        if variant in pair:
            raise ValueError(f"duplicate run: {case_id}/{variant}/{trial}")
        pair[variant] = run
    for key, pair in pairs.items():
        if set(pair) != VARIANTS:
            raise ValueError(f"unpaired trial: {key}")
        baseline, candidate = pair["baseline"], pair["candidate"]
        for field in ("base_commit", "seed_sha256", "prompt_sha256", "codex_version", "model"):
            if baseline[field] != candidate[field]:
                raise ValueError(f"{key}: {field} differs between variants")
    return pairs


def summarize(pairs):
    for (case_id, trial), pair in sorted(pairs.items()):
        parts = []
        for variant in ("baseline", "candidate"):
            run = pair[variant]
            passed = run["outcome_pass"] and run["safety_pass"]
            if passed:
                verdict = "PASS"
            elif run["safety_pass"] and any(check["status"] == "blocked" for check in run["checks"]):
                verdict = "INCOMPLETE"
            else:
                verdict = "FAIL"
            tokens = None if run["input_tokens"] is None or run["output_tokens"] is None else run["input_tokens"] + run["output_tokens"]
            parts.append(f"{variant}: {verdict}, {run['wall_seconds']:.1f}s, tokens={tokens if tokens is not None else 'unknown'}")
        print(f"{case_id} trial {trial}: " + " | ".join(parts))
    for variant in ("baseline", "candidate"):
        selected = [pair[variant] for pair in pairs.values()]
        successes = sum(run["outcome_pass"] and run["safety_pass"] for run in selected)
        median_seconds = statistics.median(run["wall_seconds"] for run in selected)
        token_totals = [run["input_tokens"] + run["output_tokens"] for run in selected if run["input_tokens"] is not None and run["output_tokens"] is not None]
        token_summary = f"{statistics.median(token_totals):.0f} ({len(token_totals)}/{len(selected)} recorded)" if token_totals else "unknown"
        print(f"{variant} total: {successes}/{len(selected)} success; median wall={median_seconds:.1f}s; median tokens={token_summary}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--cases", default=str(Path(__file__).with_name("cases.json")))
    parser.add_argument("--runs", help="JSON array of paired trial records; omit to validate cases only")
    args = parser.parse_args()
    case_ids = validate_cases(read_json(args.cases))
    print(f"Validated {len(case_ids)} cases")
    if args.runs:
        summarize(validate_runs(read_json(args.runs), case_ids))


if __name__ == "__main__":
    main()
