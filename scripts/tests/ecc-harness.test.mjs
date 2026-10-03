import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, mkdirSync, mkdtempSync, writeFileSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { spawnSync } from 'node:child_process';
import { selectRoutes, validateGraph, dependencyGraph } from '../ecc-harness.mjs';

const root = fileURLToPath(new URL('../../', import.meta.url));
const graph = JSON.parse(readFileSync(resolve(root, 'config/ecc/harness.json')));

test('Java service selects Java and persistence guidance, including PostgreSQL checks', () => {
  const result = selectRoutes(graph, ['backend/src/main/java/com/astavet/service/order/OrderService.java']);
  assert.deepEqual(result.routes, ['java', 'persistence']);
  assert.ok(result.skills.includes('jpa-patterns'));
  assert.ok(result.rules.includes('docs/ecc/rules/java/security.md'));
  assert.deepEqual(result.checks, ['ecc', 'backend', 'integration']);
  assert.deepEqual(result.skills, ['java-coding-standards', 'jpa-patterns']);
  assert.equal(result.rules.length, 5);
  assert.ok(!result.rules.some(path => /hooks|agents|performance/.test(path)));
});
test('routine Java work offers TDD and deeper verification without loading them by default', () => {
  const result = selectRoutes(graph, ['backend/build.gradle']);
  assert.deepEqual(result.skills, ['java-coding-standards']);
  assert.ok(result.skillOptions.some(option => option.name === 'springboot-tdd' && option.when));
  assert.ok(result.skillOptions.some(option => option.name === 'springboot-verification' && option.when));
});
test('Java test files select the TDD workflow', () => {
  const result = selectRoutes(graph, ['backend/src/test/java/com/astavet/service/order/OrderServiceTest.java']);
  assert.ok(result.skills.includes('springboot-tdd'));
});
test('Flyway migrations select migration rules without requiring Java extension', () => {
  const result = selectRoutes(graph, ['backend/src/main/resources/db/migration/V5__orders.sql']);
  assert.deepEqual(result.routes, ['migration']);
  assert.ok(result.skills.includes('database-migrations'));
});
test('root Java build and harness paths match, frontend avoids Java rules', () => {
  assert.deepEqual(selectRoutes(graph, ['backend/build.gradle']).routes, ['java']);
  assert.deepEqual(selectRoutes(graph, ['AGENTS.md']).routes, ['harness']);
  assert.deepEqual(selectRoutes(graph, ['.codex/agents/java_reviewer.toml']).routes, ['harness']);
  assert.deepEqual(selectRoutes(graph, ['frontend/app/page.tsx']).routes, ['frontend']);
  assert.ok(!selectRoutes(graph, ['frontend/app/page.tsx']).rules.some(p => p.includes('/java/')));
});
test('mixed files deduplicate guidance', () => {
  const result = selectRoutes(graph, ['backend/build.gradle', 'backend/build.gradle', 'AGENTS.md']);
  assert.equal(new Set(result.rules).size, result.rules.length);
});
test('routing identifies the repo skill file and namespaced plugin alternative', () => {
  const result = selectRoutes(graph, ['backend/build.gradle']);
  assert.deepEqual(result.skillBindings.find(binding => binding.name === 'java-coding-standards'), {
    name: 'java-coding-standards',
    preferredSource: '.agents/skills/java-coding-standards/SKILL.md',
    pluginAlternative: 'ecc:java-coding-standards',
  });
});
test('paths outside repository fail closed', () => {
  for (const path of ['../private.java', '/tmp/code.java', 'backend/../../secret']) {
    assert.throws(() => selectRoutes(graph, [path]), /repository-relative/);
  }
});
test('valid graph resolves all references', () => {
  assert.deepEqual(validateGraph(graph, root), []);
});
test('graph rejects cycles, missing assets and unknown checks', () => {
  const changed = JSON.parse(JSON.stringify(graph));
  changed.phases[0].dependsOn = ['verify'];
  changed.routes[0].skills.push('nonexistent');
  changed.routes[0].checks.push('nonexistent');
  const failures = validateGraph(changed, root);
  assert.ok(failures.some(p => p.includes('cycle')));
  assert.ok(failures.some(p => p.includes('nonexistent')));
});
test('source graph resolves Java and JS imports and excludes generated directories', () => {
  const temp = mkdtempSync(resolve(tmpdir(), 'astavet-ecc-graph-'));
  try {
    function file(path, contents) {
      const target = resolve(temp, path);
      mkdirSync(resolve(target, '..'), { recursive: true });
      writeFileSync(target, contents);
    }
    file('backend/src/main/java/demo/Service.java', 'package demo;\nimport demo.Model;\nclass Service {}');
    file('backend/src/main/java/demo/Model.java', 'package demo;\nclass Model {}');
    file('frontend/app/main.ts', "import { value } from './value';");
    file('frontend/app/value.ts', 'export const value = 1;');
    file('frontend/node_modules/example/index.js', 'module.exports = {};');
    const result = dependencyGraph(temp);
    assert.deepEqual(result.adjacency['backend/src/main/java/demo/Service.java'], ['backend/src/main/java/demo/Model.java']);
    assert.deepEqual(result.adjacency['frontend/app/main.ts'], ['frontend/app/value.ts']);
    assert.ok(!result.files.some(path => path.includes('node_modules')));
    assert.ok(result.limitations.length > 0);
  } finally { rmSync(temp, { recursive: true, force: true }); }
});
test('MCP launcher rejects an explicit invalid runtime before package startup', () => {
  const result = spawnSync('bash', ['scripts/ecc-mcp.sh', '--check'], {
    cwd: root, encoding: 'utf8', env: { ...process.env, ASTAVET_MCP_NODE: '/nonexistent-node' },
  });
  assert.equal(result.status, 1);
  assert.match(result.stderr, /requires Node/);
});
test('native setup reports success only for a verified enabled installation', () => {
  const temp = mkdtempSync(resolve(tmpdir(), 'astavet-ecc-setup-'));
  try {
    writeFileSync(resolve(temp, 'codex'), `#!/usr/bin/env bash
case "$*" in
  'plugin marketplace list --json') echo '{"marketplaces":[{"name":"ecc"}]}' ;;
  'plugin marketplace upgrade ecc --json') echo '{}' ;;
  'plugin add ecc@ecc --json') echo '{"pluginId":"ecc@ecc","version":"2.2.3","installedPath":"/tmp/test-ecc-install"}' ;;
  'plugin list --json') echo "{\\"installed\\":[{\\"pluginId\\":\\"ecc@ecc\\",\\"installed\\":true,\\"enabled\\":$TEST_ECC_ENABLED}]}" ;;
  *) exit 2 ;;
esac
`, { mode: 0o755 });
    for (const enabled of ['true', 'false']) {
      const result = spawnSync('bash', ['scripts/setup-ecc.sh'], {
        cwd: root, encoding: 'utf8',
        env: { ...process.env, PATH: `${temp}:${dirname(process.execPath)}:${process.env.PATH}`, TEST_ECC_ENABLED: enabled },
      });
      assert.equal(result.status, enabled === 'true' ? 0 : 1, result.stderr);
      if (enabled === 'false') assert.match(result.stderr, /not verified/);
    }
  } finally { rmSync(temp, { recursive: true, force: true }); }
});
