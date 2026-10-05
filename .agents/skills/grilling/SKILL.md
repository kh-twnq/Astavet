---
name: grilling
description: "Interview the user one decision at a time to stress-test a requested plan, design or estimate."
---

# Grilling

Read [the Codex adaptation contract](../../../harness/references/codex-adaptation.md) before using this workflow.

Interview the user relentlessly about every aspect of the plan, decision, or idea until you reach a
shared understanding. Walk down each branch of the decision tree, resolving dependencies between
decisions one by one. For each question, provide your recommended answer.

Ask the questions **one at a time** (use the active client’s question tool where it fits — put your recommendation as
the first option), waiting for feedback on each before continuing. Asking multiple questions at once
is bewildering.

If a **fact** can be found by exploring the environment (codebase, GitNexus graph, Jira ticket, git
history, configs), look it up rather than asking. The **decisions**, though, are the user's — put
each one to them and wait for the answer.

Do not act on the plan until the user confirms shared understanding has been reached.

## Workspace specifics

- When grilling a ticket plan, ground questions in the workspace realities first: which repo(s),
  cross-repo contract impact (frontend ↔ backend ↔ downstream services), which migration repo/DB,
  the working branch base (confirm it per your project's release model). These are facts to verify,
  then decision points to confirm.
- Natural pairing: run this **before** `solution-planning` finalizes an estimate, or before
  `change-implementation` starts on anything large or ambiguous.
- End with a short recap: decisions made (one line each) + anything explicitly deferred.

*Adapted from [mattpocock/skills](https://github.com/mattpocock/skills) `grilling` (MIT).*
