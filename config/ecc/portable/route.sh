#!/usr/bin/env bash
set -euo pipefail
ECC_PORTABLE_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "$ECC_PORTABLE_ROOT/node-runtime.sh"
export ECC_HARNESS_ROOT="$ECC_PORTABLE_ROOT"
exec "$ECC_NODE_EXEC" "$ECC_PORTABLE_ROOT/route.mjs" "$@"
