#!/usr/bin/env bash
set -euo pipefail
ECC_PORTABLE_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "$ECC_PORTABLE_ROOT/node-runtime.sh"
if [[ "${1:-}" == --check ]]; then "$ECC_NODE_EXEC" --version; exit 0; fi
exec npx --yes chrome-devtools-mcp@1.10.1
