#!/usr/bin/env bash
set -euo pipefail
# Sourced by portable launchers; no package download or shell profile changes.
if [[ -n "${ECC_NODE:-}" ]]; then
  ECC_NODE_CANDIDATES=("$ECC_NODE")
else
  ECC_NODE_CANDIDATES=("$(command -v node || true)" /opt/homebrew/opt/node/bin/node /opt/homebrew/opt/node@22/bin/node /usr/local/bin/node)
  for ECC_NVM_NODE in "$HOME"/.nvm/versions/node/*/bin/node; do
    [[ -x "$ECC_NVM_NODE" ]] && ECC_NODE_CANDIDATES+=("$ECC_NVM_NODE")
  done
fi
ECC_NODE_EXEC=""
for ECC_CANDIDATE in "${ECC_NODE_CANDIDATES[@]}"; do
  if [[ -x "$ECC_CANDIDATE" ]] && "$ECC_CANDIDATE" -e 'const [a,b]=process.versions.node.split(".").map(Number);process.exit((a===20&&b>=19)||(a===22&&b>=12)||a>=23?0:1)' >/dev/null 2>&1; then
    ECC_NODE_EXEC="$ECC_CANDIDATE"; break
  fi
done
if [[ -z "$ECC_NODE_EXEC" ]]; then
  echo 'A compatible Node runtime is required (20.19+, 22.12+, or 23+); set ECC_NODE to its executable.' >&2
  return 1
fi
export PATH="$(dirname "$ECC_NODE_EXEC"):$PATH"
