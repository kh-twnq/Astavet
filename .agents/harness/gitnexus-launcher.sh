#!/bin/sh
set -eu

mode=${1:-}
[ -n "$mode" ] || { echo 'usage: gitnexus-launcher.sh hook|<gitnexus-command> [args...]' >&2; exit 2; }

script_dir=$(cd "$(dirname "$0")" && pwd)
for candidate in "${GITNEXUS_NODE:-}" "$HOME"/.nvm/versions/node/v22*/bin/node "$(command -v node || true)" "$HOME"/.nvm/versions/node/v*/bin/node; do
  [ -n "$candidate" ] && [ -x "$candidate" ] || continue
  major=$("$candidate" -p 'Number(process.versions.node.split(".")[0])' 2>/dev/null) || continue
  [ "$major" -ge 22 ] 2>/dev/null || continue
  cli=${GITNEXUS_CLI:-$(dirname "$candidate")/../lib/node_modules/gitnexus/dist/cli/index.js}
  [ -f "$cli" ] || continue
  if [ "$mode" = hook ]; then
    export GITNEXUS_HOOK_CLI_PATH="$cli"
    exec "$candidate" "$script_dir/gitnexus-hooks/gitnexus-hook.cjs"
  fi
  exec "$candidate" "$cli" "$@"
done

echo 'GitNexus requires Node 22 or newer with a global GitNexus CLI; set GITNEXUS_NODE and GITNEXUS_CLI to override detection.' >&2
exit 127
