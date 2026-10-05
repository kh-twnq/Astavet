---
name: review-mr
description: "Review an existing MR/PR with ranked evidence and an explicit recommendation; post only within user authorization."
---

# review-mr

Read [the Codex adaptation contract](../../../harness/references/codex-adaptation.md) before using this workflow.

Review an existing hosted MR/PR, without switching the user's branch. It complements
`$ship-task` (working-diff review) and `mr-feedback` (addressing reviewer threads).

## Inputs

Repository/path and review ID/URL; infer the repo from the URL when unambiguous.
`--post` is an explicit request to publish the composed review. Otherwise prepare
it locally and post only after approval already given or newly obtained. Posting
is separate from approving/merging the review on the host.

## Steps

1. **Resolve the repo.** Use AGENTS.md's Repository Map/profile and run each host
   command in that repo. Read the branch/release policy and target component rules.
2. **Read the review.** Fetch title, description, comments, source/target refs,
   exact head SHA, full paginated diff and checks/pipeline. GitLab examples:
   `glab mr view <iid>` and `glab mr diff <iid>`. Authenticate through existing
   tools; an unavailable remote should produce a local review of supplied data.
3. **Review the exact snapshot.** Run [code-review](../code-review/SKILL.md) with
   correctness/project-rules plus appropriate deep/performance/API lenses. Add
   [security-review](../security-review/SKILL.md) when relevant; combine deep lenses
   in one permitted reviewer. Supply graph/rg evidence and inspect other repos'
   consumers. A target that conflicts with the confirmed project model is a
   finding; an unknown intended base requires clarification rather than guessing.
4. **Compose the review.** Recommendation: Blocker/Major → `REQUEST_CHANGES`;
   only minor findings → `COMMENT`; no findings → `APPROVE` recommendation.
   Include a short behavior summary, ranked findings with `repo:path:line`, trigger,
   impact and fix, then uncertainties and consumer follow-ups. Omit empty sections.
5. **Publish if authorized.** Prepare the exact body first. Recheck head SHA;
   changes require revisiting the review. Use a structured tool body or a file
   accepted by the host CLI, preserving real newlines. Posting this skill's review
   never calls the host's approve/merge operation. Report the posted URL or leave
   the complete review in chat when posting was not authorized.

## Headless use

A configured Codex client/CLI may invoke `$review-mr` non-interactively when the
host adapter and required credentials are already available. Follow that client's
actual invocation/auth configuration; do not copy `claude -p` or assume an
Anthropic API key. Adding CI/cron integration is separate authorized project work.
