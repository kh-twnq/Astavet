# Evaluation sources

These are the sources used for the evaluation protocol. Access date: 2026-10-01. Dates below are publication dates, not installed tool versions.

| Source and type | Version/date | Decision supported | Limit |
| --- | --- | --- | --- |
| [Anthropic, Demystifying evals for AI agents](https://www.anthropic.com/engineering/demystifying-evals-for-ai-agents) — vendor engineering report | 2026-01-09 | Use outcome checks independent of an agent's final message; keep trial environment and recorded evidence explicit; repeat before inferring reliability. | Examples are guidance, not proof that this harness improves AstaVet work. |
| [OpenAI, Harness engineering](https://openai.com/index/harness-engineering/) — vendor engineering report | 2026-02-11 | Treat repository guidance, architecture checks, and evaluation as a feedback loop; keep the guidance concise and review trial failures before adding machinery. | OpenAI's repository and results are not AstaVet benchmark results. |
| [Official Codex developer commands](https://learn.chatgpt.com/docs/developer-commands) — product documentation | Living page; no fixed document version stated | Use `codex exec` JSON event output and non-interactive trials. | CLI configuration is version-sensitive: the installed `codex-cli 0.159.3` was checked directly with `codex exec --help` before running. |

The actual run record, local tool versions, measured outcomes, and environment limits are in [the pilot report](results/2026-10-02-pilot.md). The installed GitNexus CLI version was 1.6.12 during clone indexing; its graph output was treated as static evidence and verified against source.
