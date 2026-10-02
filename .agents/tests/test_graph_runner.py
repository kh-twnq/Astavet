import importlib.util
import json
from pathlib import Path
import sys
import tempfile
import unittest


MODULE_PATH = Path(__file__).resolve().parents[1] / "harness" / "graph_runner.py"
SPEC = importlib.util.spec_from_file_location("graph_runner", MODULE_PATH)
graph = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(graph)


class GraphRunnerTests(unittest.TestCase):
    def make_state(self, directory, mode="feature"):
        return {
            "project": str(directory), "mode": mode, "task": "Example task",
            "initial_status": "", "run_dir": str(directory),
            "status": "running", "active": None,
            "next": "review" if mode == "review" else "plan",
            "repairs_used": 0, "history": [],
        }

    def test_feature_routes_through_one_repair_and_pass(self):
        with tempfile.TemporaryDirectory() as folder:
            state = self.make_state(folder)
            sequence = []
            responses = [
                {"decision": "ready", "summary": "plan", "steps": ["edit"], "checks": ["test"]},
                {"decision": "done", "summary": "edit", "checks": ["test passed"]},
                {"verdict": "issues", "summary": "bug", "findings": ["file:1 bug"]},
                {"decision": "done", "summary": "repair", "checks": ["test passed"]},
                {"verdict": "pass", "summary": "verified", "findings": []},
            ]

            def invoke(current, node, index, codex_bin, timeout):
                sequence.append(node)
                return responses.pop(0)

            result = graph.run_graph(state, "codex", 60, 1, invoke)
            self.assertEqual(sequence, ["plan", "execute", "review", "execute", "review"])
            self.assertEqual(result["status"], "complete")
            self.assertEqual(result["repairs_used"], 1)
            self.assertEqual(json.loads((Path(folder) / "state.json").read_text())["status"], "complete")

    def test_review_mode_never_executes_or_repairs(self):
        with tempfile.TemporaryDirectory() as folder:
            state = self.make_state(folder, mode="review")
            nodes = []

            def invoke(current, node, index, codex_bin, timeout):
                nodes.append(node)
                return {"verdict": "issues", "summary": "one finding", "findings": ["file:2 bug"]}

            result = graph.run_graph(state, "codex", 60, 1, invoke)
            self.assertEqual(nodes, ["review"])
            self.assertEqual(result["status"], "reviewed")

    def test_blocked_plan_does_not_edit(self):
        with tempfile.TemporaryDirectory() as folder:
            state = self.make_state(folder)
            nodes = []

            def invoke(current, node, index, codex_bin, timeout):
                nodes.append(node)
                return {"decision": "blocked", "summary": "missing requirement", "steps": [], "checks": []}

            result = graph.run_graph(state, "codex", 60, 1, invoke)
            self.assertEqual(nodes, ["plan"])
            self.assertEqual(result["status"], "blocked")

    def test_review_failure_stops_after_retry_bound(self):
        with tempfile.TemporaryDirectory() as folder:
            state = self.make_state(folder)
            nodes = []

            def invoke(current, node, index, codex_bin, timeout):
                nodes.append(node)
                if node == "plan":
                    return {"decision": "ready", "summary": "plan", "steps": [], "checks": []}
                if node == "execute":
                    return {"decision": "done", "summary": "edit", "checks": []}
                return {"verdict": "issues", "summary": "still broken", "findings": ["file:3"]}

            result = graph.run_graph(state, "codex", 60, 1, invoke)
            self.assertEqual(nodes, ["plan", "execute", "review", "execute", "review"])
            self.assertEqual(result["status"], "issues")

    def test_invalid_model_result_stops_and_records_error(self):
        with tempfile.TemporaryDirectory() as folder:
            state = self.make_state(folder)

            def invoke(current, node, index, codex_bin, timeout):
                raise ValueError("invalid result")

            result = graph.run_graph(state, "codex", 60, 1, invoke)
            self.assertEqual(result["status"], "blocked")
            self.assertEqual(result["error"], "invalid result")
            self.assertEqual(result["active"], "plan")

    def test_cli_nodes_receive_separate_sandboxes_and_schema(self):
        with tempfile.TemporaryDirectory() as folder:
            directory = Path(folder)
            fake_codex = directory / "fake-codex"
            fake_codex.write_text(
                "#!" + sys.executable + "\n"
                "import json,sys,pathlib\n"
                "a=sys.argv[1:]\n"
                "schema=pathlib.Path(a[a.index('--output-schema')+1])\n"
                "out=pathlib.Path(a[a.index('--output-last-message')+1])\n"
                "node=schema.name.split('-')[1]\n"
                "values={'plan':{'decision':'ready','summary':'ok','steps':[],'checks':[]},"
                "'execute':{'decision':'done','summary':'ok','checks':[]},"
                "'review':{'verdict':'pass','summary':'ok','findings':[]}}\n"
                "out.write_text(json.dumps(values[node]))\n"
                "print(json.dumps({'argv':a}))\n"
            )
            fake_codex.chmod(0o700)
            state = self.make_state(folder)
            state["model"] = "example-model"
            for index, node in enumerate(("plan", "execute", "review"), start=1):
                result = graph.run_codex(state, node, index, str(fake_codex), 10)
                graph.validate_result(node, result)
                events = directory / ("%02d-%s-events.jsonl" % (index, node))
                argv = json.loads(events.read_text())["argv"]
                expected = "workspace-write" if node == "execute" else "read-only"
                self.assertEqual(argv[argv.index("--sandbox") + 1], expected)
                self.assertEqual(argv[argv.index("--config") + 1], "approval_policy=never")
                self.assertEqual(argv[argv.index("--model") + 1], "example-model")


if __name__ == "__main__":
    unittest.main()
