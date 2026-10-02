"""Regression checks for isolated harness trial preparation and evidence."""

import importlib.util
from pathlib import Path
import subprocess
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[2]


def load_module(name):
    path = ROOT / ".agents" / "evals" / (name + ".py")
    spec = importlib.util.spec_from_file_location("eval_" + name, path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class EvalMigrationTests(unittest.TestCase):
    def test_candidate_overlay_includes_skills_rules_and_project_config(self):
        prepare = load_module("prepare")
        with tempfile.TemporaryDirectory() as directory:
            checkout = Path(directory)
            prepare.copy_candidate_guidance(checkout)
            for relative in (
                "AGENTS.md",
                ".agents/rules/README.md",
                ".agents/skills/feature/SKILL.md",
                ".agents/skills/fix/SKILL.md",
                ".agents/skills/review/SKILL.md",
                ".codex/config.toml",
                ".codex/hooks.json",
            ):
                self.assertTrue((checkout / relative).is_file(), relative)
            self.assertFalse((checkout / ".agents/.git").exists())

    def test_saved_patch_includes_untracked_file_contents(self):
        trial = load_module("run")
        with tempfile.TemporaryDirectory() as directory:
            checkout = Path(directory) / "checkout"
            checkout.mkdir()
            subprocess.run(["git", "init", "-q"], cwd=checkout, check=True)
            (checkout / "base.txt").write_text("base\n", encoding="utf-8")
            subprocess.run(["git", "add", "base.txt"], cwd=checkout, check=True)
            subprocess.run(
                ["git", "-c", "user.name=Test", "-c", "user.email=test@example.invalid",
                 "commit", "-qm", "base"],
                cwd=checkout, check=True,
            )
            (checkout / "new.txt").write_text("new evidence content\n", encoding="utf-8")
            evidence = Path(directory) / "evidence"
            evidence.mkdir()
            trial.save_checkout_evidence(checkout, evidence, "application")
            self.assertIn(b"new evidence content", (evidence / "application.diff.patch").read_bytes())
            self.assertIn("new.txt", (evidence / "application.status.txt").read_text())
            (checkout / "new.txt").unlink()
            subprocess.run(
                ["git", "apply", "--check", str(evidence / "application.diff.patch")],
                cwd=checkout, check=True,
            )


if __name__ == "__main__":
    unittest.main()
