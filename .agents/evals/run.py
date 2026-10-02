"""Run one Codex trial from a prepared manifest and retain its raw evidence."""

import argparse
import hashlib
import json
import os
import subprocess
import time
from pathlib import Path


def save_checkout_evidence(checkout, evidence, label):
    with (evidence / (label + ".diff.patch")).open("wb") as output:
        subprocess.run(["git", "-C", str(checkout), "diff", "HEAD", "--binary", "--no-color"], check=True, stdout=output)
        untracked = subprocess.run(
            ["git", "-C", str(checkout), "ls-files", "--others", "--exclude-standard", "-z"],
            check=True, capture_output=True,
        ).stdout
        for raw_path in filter(None, untracked.split(b"\0")):
            path = os.fsdecode(raw_path)
            if (checkout / path).is_dir() and not (checkout / path).is_symlink():
                continue  # A nested Git checkout has its own evidence file.
            diff = subprocess.run(
                ["git", "-C", str(checkout), "diff", "--no-index", "--binary", "--no-color", "--", "/dev/null", path],
                capture_output=True,
            )
            if diff.returncode not in (0, 1):
                raise subprocess.CalledProcessError(diff.returncode, diff.args, stderr=diff.stderr)
            output.write(diff.stdout)
    with (evidence / (label + ".status.txt")).open("w", encoding="utf-8") as output:
        subprocess.run(
            ["git", "-C", str(checkout), "status", "--short", "--untracked-files=all"],
            check=True, stdout=output,
        )


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--manifest", required=True, type=Path)
    parser.add_argument("--variant", required=True, choices=("baseline", "candidate"))
    parser.add_argument("--model", required=True)
    args = parser.parse_args()
    manifest = json.loads(args.manifest.read_text(encoding="utf-8"))
    checkout = Path(manifest[args.variant]).resolve()
    evidence = args.manifest.parent / (args.variant + "-evidence")
    if not checkout.is_dir() or evidence.exists():
        parser.error("checkout must exist and evidence directory must be new")
    evidence.mkdir()

    prompt = (
        manifest["prompt"]
        + "\n\nWork alone; do not delegate. Do not commit, push, deploy or publish."
        + " If Gradle or npm is blocked by sandbox permissions, try at most one safe workaround,"
        + " then report the blocked check; an independent reviewer will run it."
        + " For GitNexus, use this checkout's absolute path as the repo and do not use"
        + " the live Astavet index."
    )
    command = [
        "codex", "exec", "--json", "--ephemeral", "-s", "workspace-write",
        "-c", 'approval_policy="never"', "-C", str(checkout),
        "-m", args.model, "-o", str(evidence / "final.txt"), "-",
    ]
    env = os.environ.copy()
    for name in ("ASTAVET_TEST_DATABASE_URL", "ASTAVET_TEST_DATABASE_USERNAME", "ASTAVET_TEST_DATABASE_PASSWORD"):
        env.pop(name, None)
    start = time.monotonic()
    with (evidence / "events.jsonl").open("w", encoding="utf-8") as events, (evidence / "stderr.txt").open("w", encoding="utf-8") as errors:
        completed = subprocess.run(command, input=prompt, text=True, stdout=events, stderr=errors, env=env)
    duration = round(time.monotonic() - start, 2)
    save_checkout_evidence(checkout, evidence, "application")
    if args.variant == "baseline":
        save_checkout_evidence(checkout / ".agent", evidence, "guidance")
    usage = None
    for line in (evidence / "events.jsonl").read_text(encoding="utf-8").splitlines():
        event = json.loads(line)
        if event.get("type") == "turn.completed":
            usage = event.get("usage")
    version = subprocess.run(["codex", "--version"], check=True, text=True, capture_output=True).stdout.strip()
    (evidence / "run.json").write_text(json.dumps({
        "case_id": manifest["case_id"], "variant": args.variant, "trial": manifest["trial"],
        "base_commit": manifest["base_commit"], "seed_sha256": manifest["seed_sha256"],
        "prompt_sha256": hashlib.sha256(prompt.encode("utf-8")).hexdigest(),
        "model": args.model, "codex_version": version, "exit_code": completed.returncode,
        "wall_seconds": duration, "usage": usage,
    }, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"variant": args.variant, "exit_code": completed.returncode, "wall_seconds": duration, "evidence": str(evidence)}))
    raise SystemExit(completed.returncode)


if __name__ == "__main__":
    main()
