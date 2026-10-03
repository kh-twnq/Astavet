#!/usr/bin/env bash
set -euo pipefail

# Native plugin lifecycle; safe to rerun on another development machine.
# Writes Codex's user plugin state and needs network access.
if ! command -v codex >/dev/null 2>&1; then
  echo 'Codex CLI is required for native ECC plugin installation.' >&2
  exit 1
fi
MARKETPLACE_JSON="$(codex plugin marketplace list --json)"
MARKETPLACE_PRESENT="$(printf '%s' "$MARKETPLACE_JSON" | node -e '
  let data=""; process.stdin.on("data", chunk => data += chunk);
  process.stdin.on("end", () => { const found=JSON.parse(data).marketplaces.some(item => item.name === "ecc"); console.log(found ? "yes" : "no"); });
')"
if [[ "$MARKETPLACE_PRESENT" == yes ]]; then
  codex plugin marketplace upgrade ecc --json
else
  codex plugin marketplace add affaan-m/ECC
fi
INSTALL_JSON="$(codex plugin add ecc@ecc --json)"
printf '%s' "$INSTALL_JSON" | node -e '
  let data=""; process.stdin.on("data", chunk => data += chunk);
  process.stdin.on("end", () => {
    const result=JSON.parse(data);
    if (result.pluginId !== "ecc@ecc" || typeof result.installedPath !== "string" || !result.installedPath.startsWith("/") || /[\x00-\x1f]/.test(result.installedPath)) process.exit(1);
    console.log(`Installed ${result.pluginId} version ${result.version}`);
  });
'
PLUGIN_JSON="$(codex plugin list --json)"
printf '%s' "$PLUGIN_JSON" | node -e '
  let data=""; process.stdin.on("data", chunk => data += chunk);
  process.stdin.on("end", () => {
    const result=JSON.parse(data).installed.find(plugin => plugin.pluginId === "ecc@ecc");
    if (!result || result.installed !== true || result.enabled !== true) { console.error("ECC installation is not verified installed and enabled"); process.exit(1); }
    console.log("PASS ECC installed and enabled");
  });
'
echo 'ECC installed through Codex. Review hook trust in Codex and reload the session.'
