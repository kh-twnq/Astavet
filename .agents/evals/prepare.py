"""Prepare isolated baseline/candidate checkouts for one seeded trial."""

import argparse
import hashlib
import json
import shutil
import subprocess
from pathlib import Path


REPO = Path(__file__).resolve().parents[2]
COLLECTION = REPO / ".agents"


def run(*args):
    return subprocess.run(args, check=True, text=True, capture_output=True).stdout.strip()


def copy_candidate_guidance(checkout):
    shutil.copy2(REPO / "AGENTS.md", checkout / "AGENTS.md")
    shutil.copytree(
        COLLECTION, checkout / ".agents", dirs_exist_ok=True,
        ignore=shutil.ignore_patterns(".git", "__pycache__", "*.pyc"),
    )
    project_config = checkout / ".codex"
    project_config.mkdir(exist_ok=True)
    for name in ("config.toml", "hooks.json"):
        shutil.copy2(REPO / ".codex" / name, project_config / name)
    architecture_test = Path("backend/src/test/java/com/astavet/architecture/ControllerRepositoryBoundaryTest.java")
    target = checkout / architecture_test
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(REPO / architecture_test, target)


def seed_case(checkout, seed):
    target = checkout / seed["path"]
    original = target.read_text(encoding="utf-8")
    count = original.count(seed["find"])
    if count != 1:
        raise ValueError(f"{target}: expected exactly one seed match, found {count}")
    target.write_text(original.replace(seed["find"], seed["replace"], 1), encoding="utf-8")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--case", required=True, help="case id from cases.json")
    parser.add_argument("--trial", required=True, type=int)
    parser.add_argument("--base-ref", required=True, help="application commit before the candidate guidance")
    parser.add_argument("--baseline-guidance-source", required=True,
                        help="path or URL of the historical guidance Git repository")
    parser.add_argument("--baseline-guidance-ref", required=True,
                        help="commit of the historical guidance collection")
    parser.add_argument("--destination", required=True, type=Path, help="new directory outside the live repository")
    args = parser.parse_args()
    if args.trial < 1:
        parser.error("trial must be positive")
    destination = args.destination.resolve()
    if destination == REPO or REPO in destination.parents or destination.exists():
        parser.error("destination must be a new directory outside the live repository")

    cases = json.loads(Path(__file__).with_name("cases.json").read_text(encoding="utf-8"))
    selected = [case for case in cases if case["id"] == args.case]
    if len(selected) != 1:
        parser.error("case id not found or duplicated")
    case = selected[0]
    seed = case["seed"]
    relative = Path(seed["path"])
    if relative.is_absolute() or ".." in relative.parts:
        parser.error("seed path must stay inside the trial checkout")

    destination.mkdir(parents=True)
    base_commit = run("git", "-C", str(REPO), "rev-parse", "--verify", args.base_ref + "^{commit}")
    for variant in ("baseline", "candidate"):
        checkout = destination / variant
        run("git", "clone", "--local", "--no-hardlinks", "--quiet", str(REPO), str(checkout))
        run("git", "-C", str(checkout), "checkout", "--quiet", "--detach", base_commit)
        if variant == "baseline":
            historical = checkout / ".agent"
            run("git", "clone", "--quiet", args.baseline_guidance_source, str(historical))
            historical_commit = run(
                "git", "-C", str(historical), "rev-parse", "--verify",
                args.baseline_guidance_ref + "^{commit}",
            )
            run("git", "-C", str(historical), "checkout", "--quiet", "--detach", historical_commit)
        else:
            copy_candidate_guidance(checkout)
        seed_case(checkout, seed)

    seed_digest = hashlib.sha256(json.dumps(seed, sort_keys=True).encode("utf-8")).hexdigest()
    manifest = {
        "case_id": case["id"],
        "trial": args.trial,
        "base_commit": base_commit,
        "baseline_guidance_commit": historical_commit,
        "seed_sha256": seed_digest,
        "prompt": case["prompt"],
        "baseline": str(destination / "baseline"),
        "candidate": str(destination / "candidate"),
    }
    (destination / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({key: value for key, value in manifest.items() if key != "prompt"}, indent=2))


if __name__ == "__main__":
    main()
