# Optional tracker, host and navigation integrations

Use connectors/MCP tools already available in the active client or authenticated
Git host CLIs. Configuration belongs to the user's Codex config/project profile;
never copy credentials or Claude settings into this repository. Tool names differ
by provider/client: discover the available capability instead of assuming a
getJiraIssue or glab command exists.

GitHub: gh for read-only PR metadata/diff/checks and authorized creation/comments.
GitLab: glab for equivalent MR operations; discussion APIs are host-specific.
Read all pages, verify target/source refs and preserve existing labels/fields.
Jira/other tracker: read ticket, comments/subtasks; prepare field/subtask/status/
worklog mutations before authorized writes. Worklogs use actual user-confirmed
time, never estimates. Confluence/other docs: inspect destination/template and
prepare an exact patch before authorized publication.

Missing tools/auth/network are unknown/unavailable; use pasted tickets/diffs and
produce a complete local artifact. Do not install plugins, authenticate accounts
or publish merely because a related skill was selected.
