import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, mkdirSync, writeFileSync, readFileSync, rmSync, symlinkSync, realpathSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { resolve } from 'node:path';
import { spawnSync } from 'node:child_process';
import { routeProject } from '../codex-harness.mjs';

const root = resolve(import.meta.dirname, '../..');
function fixture(run) {
  const dir = mkdtempSync(resolve(tmpdir(), 'codex-portable-'));
  const file = (path, value) => {
    const target = resolve(dir, path);
    mkdirSync(resolve(target, '..'), { recursive: true });
    writeFileSync(target, value);
  };
  try { run(dir, file); } finally { rmSync(dir, { recursive: true, force: true }); }
}
function route(project, files = [], extra = []) {
  try {
    return { status: 0, stdout: JSON.stringify(routeProject(project, files, { behavior: extra.includes('--behavior'), security: extra.includes('--security') })), stderr: '' };
  } catch (error) { return { status: 1, stdout: '', stderr: error.message }; }
}
function install(home, extra = []) {
  const python = spawnSync('python3', ['-c', 'import tomllib']).status === 0 ? 'python3' : 'python3.11';
  return spawnSync(python, [resolve(root, 'scripts/install-codex-harness.py'), '--codex-home', home, ...extra], { encoding: 'utf8' });
}
test('portable Java and Next adapters select only affected modules and project commands', () => fixture((dir, file) => {
  file('server/build.gradle', "plugins { id 'org.springframework.boot' version '4.1.1' }");
  file('server/gradlew', '#!/bin/sh\n');
  file('web/package.json', JSON.stringify({ dependencies: { next: '16' }, scripts: { test: 'vitest run', lint: 'eslint .', build: 'next build' } }));
  file('web/pnpm-lock.yaml', 'lockfileVersion: 9');
  let r = route(dir, ['server/src/main/java/demo/OrderService.java'], ['--behavior']);
  assert.equal(r.status, 0, r.stderr);
  let output = JSON.parse(r.stdout);
  assert.ok(output.skills.includes('ecc:springboot-tdd'));
  assert.ok(output.skills.includes('ecc:jpa-patterns'));
  assert.ok(output.rules.some(path => path.endsWith('/java/security.md')));
  assert.deepEqual(output.checks, [{ cwd: 'server', argv: ['./gradlew', '--no-daemon', 'check', 'assemble'] }]);
  assert.ok(!JSON.stringify(output).includes('astavet'));
  r = route(dir, ['web/app/page.tsx'], ['--behavior']);
  assert.equal(r.status, 0, r.stderr);
  output = JSON.parse(r.stdout);
  assert.ok(output.skills.includes('ecc:frontend-patterns'));
  assert.ok(output.skills.includes('ecc:tdd-workflow'));
  assert.ok(!output.skills.includes('ecc:java-coding-standards'));
  assert.ok(output.rules.some(path => path.endsWith('/typescript/coding-style.md')));
  assert.ok(!output.rules.some(path => path.includes('/java/')));
  assert.deepEqual(output.checks[0], { cwd: 'web', argv: ['pnpm', 'run', 'test'] });
}));
test('unknown stacks never invent commands; adapters do not leak between projects', () => fixture((dir, file) => {
  file('known/package.json', '{"scripts":{"test":"vitest"}}');
  mkdirSync(resolve(dir, 'unknown'));
  const r = route(resolve(dir, 'unknown'), ['notes.md']);
  assert.equal(r.status, 0, r.stderr);
  const output = JSON.parse(r.stdout);
  assert.deepEqual(output.checks, []);
  assert.deepEqual(output.adapters, []);
  assert.ok(output.next_actions.length);
}));
test('routing rejects traversal and symlinks escaping target project', () => fixture((dir, file) => {
  file('project/package.json', '{}');
  file('outside/secret', 'private');
  symlinkSync(resolve(dir, 'outside'), resolve(dir, 'project/link'));
  for (const path of ['../outside/secret', '/etc/passwd', 'link/secret']) {
    const r = route(resolve(dir, 'project'), [path]);
    assert.notEqual(r.status, 0);
    assert.ok(!r.stdout.includes('private'));
  }
}));
test('manifest symlinks cannot import another project and deleted symlink paths fail closed', () => fixture((dir, file) => {
  file('other/package.json', '{"scripts":{"test":"echo private"}}');
  mkdirSync(resolve(dir, 'project'));
  symlinkSync(resolve(dir, 'other/package.json'), resolve(dir, 'project/package.json'));
  assert.throws(() => routeProject(resolve(dir, 'project'), []), /escapes target project/);
  rmSync(resolve(dir, 'project/package.json'));
  symlinkSync(resolve(dir, 'missing'), resolve(dir, 'project/broken'));
  assert.throws(() => routeProject(resolve(dir, 'project'), ['broken/file.ts']));
}));
test('empty manifests and invalid JSON shapes have an explicit outcome', () => fixture((dir, file) => {
  file('build.gradle', ''); file('gradlew', '');
  const output = routeProject(dir, []);
  assert.ok(output.adapters.some(adapter => adapter.id === 'java'));
  assert.deepEqual(output.checks[0].argv, ['./gradlew', '--no-daemon', 'check', 'assemble']);
  file('package.json', 'null');
  assert.throws(() => routeProject(dir, []), /Invalid package/);
}));
test('ambiguous managers and unsupported scripts produce warnings without execution', () => fixture((dir, file) => {
  file('package.json', '{"scripts":{"test":"touch should-not-exist"}}');
  file('pnpm-lock.yaml', ''); file('package-lock.json', '{}');
  const r = route(dir, ['app.ts']);
  assert.equal(r.status, 0, r.stderr);
  const output = JSON.parse(r.stdout);
  assert.deepEqual(output.checks, []);
  assert.ok(output.warnings.some(w => w.includes('package manager')));
}));
test('Maven, plain Java, security and migration tasks use appropriate guidance', () => fixture((dir, file) => {
  file('pom.xml', '<project>plain Java</project>');
  let output = routeProject(dir, ['src/main/java/demo/Model.java'], { behavior: true });
  assert.ok(output.skills.includes('ecc:tdd-workflow'));
  assert.ok(!output.skills.includes('ecc:springboot-patterns'));
  assert.deepEqual(output.checks, []);
  file('mvnw', '#!/bin/sh');
  file('pom.xml', '<project>spring-boot</project>');
  output = routeProject(dir, ['src/main/java/demo/security/Access.java', 'src/main/resources/db/migration/V2__data.sql'], { security: true });
  assert.deepEqual(output.checks[0].argv, ['./mvnw', 'verify']);
  assert.ok(output.skills.includes('ecc:springboot-security'));
  assert.ok(output.skills.includes('ecc:database-migrations'));
  assert.ok(output.skills.includes('ecc:security-review'));
  assert.ok(output.warnings.some(w => w.includes('disposable database')));
}));
test('packageManager and lockfiles resolve scripts; root discovery stays bounded', () => fixture((dir, file) => {
  file('package.json', JSON.stringify({ packageManager: 'yarn@4.0', dependencies: { react: '19' }, scripts: { test: 'jest' } }));
  file('deep/three/levels/package.json', '{}');
  file('node_modules/untrusted/package.json', 'not json');
  let output = routeProject(dir, []);
  assert.deepEqual(output.adapters, [{ id: 'react', cwd: '.' }]);
  assert.deepEqual(output.checks[0].argv, ['yarn', 'run', 'test']);
  file('package.json', '{"scripts":{"test":"vitest"}}');
  output = routeProject(dir, ['new-file.ts'], { behavior: true });
  assert.deepEqual(output.checks[0].argv, ['npm', 'run', 'test']);
  assert.ok(output.skills.includes('ecc:tdd-workflow'));
  file('package.json', 'invalid json');
  assert.throws(() => routeProject(dir, []), /Invalid package/);
}));
test('installer refuses malformed sections, symlinks and a shadowing user override', () => fixture((dir, file) => {
  const home = resolve(dir, 'user');
  file('user/config.toml', '# END ECC PORTABLE HARNESS v1\n# BEGIN ECC PORTABLE HARNESS v1\n');
  assert.notEqual(install(home, ['--apply']).status, 0);
  file('user/config.toml', '');
  file('user/AGENTS.override.md', 'My override');
  assert.notEqual(install(home, ['--apply']).status, 0);
  rmSync(resolve(home, 'AGENTS.override.md'));
  file('outside/instructions.md', 'Private');
  symlinkSync(resolve(dir, 'outside/instructions.md'), resolve(home, 'AGENTS.md'));
  assert.notEqual(install(home, ['--apply']).status, 0);
  assert.equal(readFileSync(resolve(dir, 'outside/instructions.md'), 'utf8'), 'Private');
}));
test('installer preserves unmanaged edits across updates and rejects edited managed instructions', () => fixture((dir, file) => {
  const home = resolve(dir, 'user');
  file('user/config.toml', 'model = "personal"\n');
  assert.equal(install(home, ['--apply']).status, 0);
  const cfg = readFileSync(resolve(home, 'config.toml'), 'utf8');
  file('user/config.toml', cfg.replace('personal', 'new-preference'));
  assert.equal(install(home, ['--apply']).status, 0);
  assert.ok(readFileSync(resolve(home, 'config.toml'), 'utf8').includes('new-preference'));
  const instructions = readFileSync(resolve(home, 'AGENTS.md'), 'utf8');
  file('user/AGENTS.md', instructions.replace('Portable Codex workflow', 'My workflow'));
  assert.notEqual(install(home, ['--apply']).status, 0);
}));
test('installer is independent of source repo, preserves user config and is idempotent', () => fixture((dir, file) => {
  file('user/config.toml', 'model = "personal-model"\n[plugins."ecc@ecc"]\nenabled = true\n');
  file('user/AGENTS.md', '# Personal instructions\nKeep my preferences.\n');
  const home = resolve(dir, 'user');
  const preview = install(home);
  assert.equal(preview.status, 0, preview.stderr);
  assert.equal(readFileSync(resolve(home, 'AGENTS.md'), 'utf8'), '# Personal instructions\nKeep my preferences.\n');
  const result = install(home, ['--apply']);
  assert.equal(result.status, 0, result.stderr);
  const config = readFileSync(resolve(home, 'config.toml'), 'utf8');
  assert.ok(config.startsWith('model = "personal-model"'));
  assert.ok(!config.includes('Astavet'));
  assert.ok(config.includes('ecc_reviewer'));
  const instructions = readFileSync(resolve(home, 'AGENTS.md'), 'utf8');
  assert.ok(instructions.startsWith('# Personal instructions'));
  assert.ok(!/\bCOD\b|\bstock\b/.test(instructions));
  assert.equal(install(home, ['--apply']).status, 0);
  assert.equal(readFileSync(resolve(home, 'config.toml'), 'utf8'), config);
  mkdirSync(resolve(dir, 'other-project'));
  const routed = spawnSync('bash', [resolve(home, 'ecc-harness/bin/route'), 'route', '--project', resolve(dir, 'other-project'), 'notes.md'], { encoding: 'utf8', env: { ...process.env, ECC_NODE: process.execPath } });
  assert.equal(routed.status, 0, routed.stderr);
  assert.deepEqual(JSON.parse(routed.stdout).checks, []);
}));
test('installer preflights conflicts and preserves changed managed files', () => fixture((dir, file) => {
  const home = resolve(dir, 'user');
  file('user/config.toml', '[agents.ecc_reviewer]\ndescription = "mine"\n');
  assert.notEqual(install(home, ['--apply']).status, 0);
  assert.equal(readFileSync(resolve(home, 'config.toml'), 'utf8'), '[agents.ecc_reviewer]\ndescription = "mine"\n');
  file('user/config.toml', '');
  assert.equal(install(home, ['--apply']).status, 0);
  file('user/ecc-harness/route.mjs', '// my local edit');
  const before = readFileSync(resolve(home, 'config.toml'), 'utf8');
  assert.notEqual(install(home, ['--apply']).status, 0);
  assert.equal(readFileSync(resolve(home, 'ecc-harness/route.mjs'), 'utf8'), '// my local edit');
  assert.equal(readFileSync(resolve(home, 'config.toml'), 'utf8'), before);
}));
test('audit passes the requested cwd to every scoped read without model turns', () => fixture((dir, file) => {
  mkdirSync(resolve(dir, 'target'));
  file('bin/codex', `#!/usr/bin/env python3
import json, sys, os
assert sys.argv[1:] == ['app-server','--strict-config','--stdio']
for line in sys.stdin:
 request=json.loads(line)
 if 'id' not in request: continue
 method=request['method']; params=request['params']
 assert method in ['initialize','config/read','skills/list','hooks/list']
 if method=='config/read': assert params['cwd']==os.getcwd()
 if method in ['skills/list','hooks/list']: assert params['cwds']==[os.getcwd()]
 print(json.dumps({'id':request['id'],'result':{'data':[],'config':{}}}),flush=True)
`);
  // The fake provider validates the actual process cwd and protocol; no model API.
  const chmod = spawnSync('chmod', ['+x', resolve(dir, 'bin/codex')]);
  assert.equal(chmod.status, 0);
  const result = spawnSync('python3', [resolve(root, 'scripts/audit-codex.py'), '--project', resolve(dir, 'target')], {
    encoding: 'utf8', env: { ...process.env, PATH: `${resolve(dir, 'bin')}:${process.env.PATH}` },
  });
  assert.equal(result.status, 0, result.stderr);
  assert.equal(JSON.parse(result.stdout).project, realpathSync(resolve(dir, 'target')));
}));
test('AstaVet inherits the global MCP without an incomplete or duplicate project launcher', () => {
  const config = readFileSync(resolve(root, '.codex/config.toml'), 'utf8');
  assert.ok(!/^\[mcp_servers\./m.test(config));
});
