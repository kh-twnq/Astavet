# Lifecycle entrypoints

Codex uses discoverable skills instead of assuming custom Claude slash commands.

| Claude command | Codex entrypoint |
| --- | --- |
| /briefing | $briefing |
| /start-task | $start-task |
| /ship-task | $ship-task |
| /review-mr | $review-mr |
| /handoff | $handoff |

The five entrypoint contracts live in `.agents/skills/`; no second prompt copy is
maintained here. `implement-task` and `review-change` are ledger helpers composing
change-implementation and code-review.
