#!/usr/bin/env bash
set -euo pipefail

# Upstream Chrome DevTools MCP needs ^20.19, ^22.12 or >=23.
# Select a compatible installed runtime without changing the user's shell.
if [[ -n "${ASTAVET_MCP_NODE:-}" ]]; then
  NODE_CANDIDATES=("$ASTAVET_MCP_NODE")
else
  NODE_CANDIDATES=(
    "$(command -v node || true)"
    /opt/homebrew/opt/node@26/bin/node
    /opt/homebrew/opt/node@24/bin/node
    /opt/homebrew/opt/node@22/bin/node
    /opt/homebrew/opt/node/bin/node
    /usr/local/opt/node@24/bin/node
    /usr/local/opt/node@22/bin/node
    /usr/local/bin/node
    /usr/bin/node
  )
fi
NODE_EXEC=""
for CANDIDATE in "${NODE_CANDIDATES[@]}"; do
  if [[ -x "$CANDIDATE" ]] && "$CANDIDATE" -e '
    const [major, minor] = process.versions.node.split(".").map(Number);
    process.exit((major === 20 && minor >= 19) || (major === 22 && minor >= 12) || major >= 23 ? 0 : 1);
  ' >/dev/null 2>&1; then
    NODE_EXEC="$CANDIDATE"
    break
  fi
done
if [[ -z "$NODE_EXEC" ]]; then
  echo 'ECC MCP requires Node 20.19+, 22.12+, or 23+. Install one or set ASTAVET_MCP_NODE to its executable.' >&2
  exit 1
fi
export PATH="$(dirname "$NODE_EXEC"):$PATH"
if [[ "${1:-}" == --check ]]; then
  "$NODE_EXEC" --version
  exit 0
fi
exec npx --yes chrome-devtools-mcp@1.10.1
