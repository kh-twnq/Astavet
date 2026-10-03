#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MODE="${1:-all}"
case "$MODE" in
  ecc|backend|frontend|integration|all) ;;
  *) echo 'Usage: bash scripts/verify.sh [ecc|backend|frontend|integration|all]' >&2; exit 2 ;;
esac
cd "$ROOT_DIR"
node -e 'if (Number(process.versions.node.split(".")[0]) < 20) { console.error("Verification requires Node.js 20+; select a supported Node runtime."); process.exit(1); }'
node scripts/check-ecc.mjs
node scripts/ecc-harness.mjs validate
node --test scripts/tests/ecc-harness.test.mjs
git diff --check

if [[ "$MODE" == backend || "$MODE" == all ]]; then
  (cd backend && ./gradlew --no-daemon check assemble)
  if [[ -z "${ASTAVET_TEST_DATABASE_URL:-}" ]]; then
    echo 'SKIP PostgreSQL integration: ASTAVET_TEST_DATABASE_URL is unset.'
  fi
fi
if [[ "$MODE" == integration ]]; then
  : "${ASTAVET_TEST_DATABASE_URL:?Set a disposable PostgreSQL test database URL; tests delete data.}"
  (cd backend && ./gradlew --no-daemon test --tests '*IntegrationTest' --rerun-tasks)
fi
if [[ "$MODE" == frontend || "$MODE" == all ]]; then
  (cd frontend && npm test && npm run lint && npm run build)
fi
echo "PASS verification: $MODE"
