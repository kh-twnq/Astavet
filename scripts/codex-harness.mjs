#!/usr/bin/env node
// Portable, read-only guidance. Never runs project scripts or model turns.
import { existsSync, readFileSync, realpathSync, readdirSync, lstatSync } from 'node:fs';
import { resolve, relative, isAbsolute, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const excluded = new Set(['.git', '.codex', '.agents', 'node_modules', '.next', 'build', 'dist', '.gradle', 'vendor', 'tools']);
const unique = values => [...new Set(values)];
function safePath(root, value) {
  if (!value || isAbsolute(value) || value.includes('\\') || value.split('/').includes('..') || /[\x00-\x1f]/.test(value)) {
    throw new Error('Expected a safe project-relative path');
  }
  const path = resolve(root, value);
  let component = root;
  for (const part of relative(root, path).split('/')) {
    component = resolve(component, part);
    const stat = lstatSync(component, { throwIfNoEntry: false });
    if (stat?.isSymbolicLink()) {
      const target = relative(root, realpathSync(component));
      if (target.startsWith('../') || target === '..' || isAbsolute(target)) throw new Error('Path escapes target project');
    }
  }
  let ancestor = path;
  while (!existsSync(ancestor)) ancestor = dirname(ancestor);
  const actual = relative(root, realpathSync(ancestor));
  if (actual.startsWith('../') || actual === '..' || isAbsolute(actual)) throw new Error('Path escapes target project');
  return relative(root, path).replaceAll('\\', '/');
}
function read(path) { return existsSync(path) ? readFileSync(path, 'utf8') : ''; }
function modules(root) {
  const found = [];
  function visit(dir, depth) {
    const markers = ['package.json', 'build.gradle', 'build.gradle.kts', 'pom.xml'];
    let matched = false;
    for (const name of markers) {
      const path = resolve(dir, name);
      if (lstatSync(path, { throwIfNoEntry: false })) {
        safePath(root, relative(root, path));
        if (!lstatSync(realpathSync(path)).isFile()) throw new Error('Manifest must be a regular file');
        matched = true;
      }
    }
    if (matched) found.push(dir);
    if (depth === 2) return;
    for (const entry of readdirSync(dir, { withFileTypes: true })) {
      if (entry.isDirectory() && !entry.name.startsWith('.') && !excluded.has(entry.name)) visit(resolve(dir, entry.name), depth + 1);
    }
  }
  visit(root, 0);
  return found.sort();
}
function nodeAdapter(dir, root, warnings) {
  let pkg;
  try {
    pkg = JSON.parse(read(resolve(dir, 'package.json')));
    if (!pkg || typeof pkg !== 'object' || Array.isArray(pkg)) throw new Error('Invalid package shape');
  }
  catch { throw new Error(`Invalid package.json in ${relative(root, dir) || '.'}`); }
  const deps = { ...pkg.dependencies, ...pkg.devDependencies };
  const adapter = deps.next ? 'nextjs' : deps.react ? 'react' : 'node';
  const locks = [['package-lock.json', 'npm'], ['npm-shrinkwrap.json', 'npm'], ['pnpm-lock.yaml', 'pnpm'], ['yarn.lock', 'yarn'], ['bun.lock', 'bun'], ['bun.lockb', 'bun']];
  const managers = unique(locks.filter(([name]) => existsSync(resolve(dir, name))).map(([, manager]) => manager));
  const declared = typeof pkg.packageManager === 'string' ? pkg.packageManager.split('@')[0] : null;
  let manager = declared || (managers.length === 1 ? managers[0] : managers.length === 0 ? 'npm' : null);
  if (!['npm', 'pnpm', 'yarn', 'bun'].includes(manager) || managers.some(pm => pm !== manager)) {
    warnings.push(`Resolve conflicting or unsupported package manager in ${relative(root, dir) || '.'}; checks withheld.`);
    manager = null;
  }
  const scripts = pkg.scripts && typeof pkg.scripts === 'object' ? pkg.scripts : {};
  const checks = manager ? ['test', 'lint', 'typecheck', 'build', 'test:coverage', 'test:e2e'].filter(name => typeof scripts[name] === 'string').map(name => ({ cwd: relative(root, dir) || '.', argv: [manager, 'run', name] })) : [];
  return { adapter, skills: adapter === 'node' ? ['coding-standards'] : ['frontend-patterns'], checks };
}
export function routeProject(project, paths, options = {}) {
  const root = realpathSync(project);
  if (!lstatSync(root).isDirectory()) throw new Error('Target project must be a directory');
  const files = unique(paths.map(path => safePath(root, path)));
  const warnings = []; const checks = []; const adapters = []; const skills = ['verification-loop'];
  const candidates = modules(root);
  const selected = files.length ? unique(files.flatMap(file => {
    // Select the nearest module; do not include sibling or parent package commands.
    const matches = candidates.filter(dir => { const prefix = relative(root, dir); return !prefix || file === prefix || file.startsWith(prefix + '/'); });
    return matches.sort((a, b) => b.length - a.length).slice(0, 1);
  })) : candidates;
  for (const dir of selected) {
    const cwd = relative(root, dir) || '.';
    if (existsSync(resolve(dir, 'package.json'))) {
      const node = nodeAdapter(dir, root, warnings);
      adapters.push({ id: node.adapter, cwd }); skills.push(...node.skills); checks.push(...node.checks);
    }
    const hasGradle = ['build.gradle', 'build.gradle.kts'].some(name => existsSync(resolve(dir, name)));
    const gradle = read(resolve(dir, 'build.gradle')) + read(resolve(dir, 'build.gradle.kts'));
    const pom = read(resolve(dir, 'pom.xml'));
    if (hasGradle || existsSync(resolve(dir, 'pom.xml'))) {
      const spring = /org\.springframework\.boot|spring-boot/.test(gradle + pom);
      adapters.push({ id: spring ? 'springboot' : 'java', cwd }); skills.push('java-coding-standards');
      if (spring) skills.push('springboot-patterns');
      const wrapper = hasGradle ? 'gradlew' : 'mvnw';
      if (existsSync(resolve(dir, wrapper))) checks.push({ cwd, argv: hasGradle ? ['./gradlew', '--no-daemon', 'check', 'assemble'] : ['./mvnw', 'verify'] });
      else warnings.push(`No ${wrapper} in ${cwd}; inspect the project's documented Java verification command.`);
      if (files.some(file => /\/(entity|repository|service)\/|(?:Service|Repository|Entity)\.java$/.test(file))) skills.push('jpa-patterns');
      if (options.behavior) skills.push(spring ? 'springboot-tdd' : 'tdd-workflow');
      if (files.some(file => /\/(auth|security)\/|Security[^/]*\.java$/.test(file)) && spring) skills.push('springboot-security');
    }
  }
  if (options.behavior && !skills.includes('springboot-tdd') && !skills.includes('tdd-workflow')) skills.push('tdd-workflow');
  if (files.some(file => /db\/migration\/.*\.sql$/.test(file))) {
    skills.push('database-migrations', 'postgres-patterns');
    warnings.push('Use the project disposable database for migration/transaction integration; proposed commands do not prove database coverage.');
  }
  if (options.security) skills.push('security-review');
  const core = process.env.ECC_HARNESS_ROOT || dirname(fileURLToPath(import.meta.url));
  const ruleRoot = existsSync(resolve(core, 'rules/common')) ? core : resolve(core, '../docs/ecc');
  const rules = ['coding-style', 'security', 'testing'].map(name => resolve(ruleRoot, `rules/common/${name}.md`));
  const languages = [];
  if (adapters.some(adapter => ['java', 'springboot'].includes(adapter.id))) languages.push('java');
  if (adapters.some(adapter => ['nextjs', 'react', 'node'].includes(adapter.id))) languages.push('typescript');
  for (const language of languages) {
    for (const name of ['coding-style', 'security']) rules.push(resolve(ruleRoot, `rules/${language}/${name}.md`));
    if (options.behavior || files.some(file => /(?:test|spec)/i.test(file))) rules.push(resolve(ruleRoot, `rules/${language}/testing.md`));
  }
  if (!selected.length) warnings.push('No supported manifest matched; inspect project instructions and choose its existing test runner.');
  const next = ['Read target project AGENTS.md and README; project-specific guidance overrides generic examples.', 'Review proposed scripts and side effects before executing commands; use cwd and argv, never shell-concatenate output.', 'Report checks as passed, failed or skipped; do not infer coverage, E2E or security scan success.'];
  return { status: warnings.length ? 'warning' : 'success', summary: `Selected ${adapters.length} adapter(s) for target project`, project: root, files, adapters, rules, skills: unique(skills).map(name => `ecc:${name}`), checks, warnings, next_actions: next, artifacts: rules, limitations: ['Manifest discovery is bounded to two directory levels.', 'Guidance only: no commands executed; unsupported stacks use project instructions.', 'No context or memory from other projects is read.'] };
}
if (process.argv[1] && realpathSync(process.argv[1]) === fileURLToPath(import.meta.url)) {
  try {
    const args = process.argv.slice(2);
    if (args.shift() !== 'route') throw new Error('Usage: route --project <directory> [--behavior] [--security] [--] <relative files...>');
    let project = process.cwd(); const files = []; const options = {}; let positional = false;
    while (args.length) {
      const arg = args.shift();
      if (!positional && arg === '--') positional = true;
      else if (!positional && arg === '--project') { project = args.shift(); if (!project) throw new Error('Missing --project directory'); }
      else if (!positional && arg === '--behavior') options.behavior = true;
      else if (!positional && arg === '--security') options.security = true;
      else if (!positional && arg.startsWith('-')) throw new Error('Unknown option');
      else files.push(arg);
    }
    console.log(JSON.stringify(routeProject(project, files, options), null, 2));
  } catch (error) {
    console.error(JSON.stringify({ status: 'error', summary: error.message, next_actions: ['Correct the target directory/relative paths or manifest; retry read-only routing.'], artifacts: [] }));
    process.exitCode = 1;
  }
}
