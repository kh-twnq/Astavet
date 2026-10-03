#!/usr/bin/env node
import { existsSync, readFileSync, readdirSync, writeFileSync } from 'node:fs';
import { dirname, resolve, relative, isAbsolute } from 'node:path';
import { fileURLToPath } from 'node:url';
import { createRequire } from 'node:module';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const require = createRequire(import.meta.url);
const { buildDependencyGraph } = require('../tools/ecc/scripts/lib/agent-proximity/graph.js');
const unique = values => [...new Set(values)];
const skillBinding = name => ({
  name,
  preferredSource: `.agents/skills/${name}/SKILL.md`,
  pluginAlternative: `ecc:${name}`,
});

function repoPath(value) {
  if (typeof value !== 'string' || !value || isAbsolute(value) ||
      value.includes('\\') || value.split('/').includes('..') || /[\x00-\x1f]/.test(value)) {
    throw new Error('Expected a safe repository-relative path');
  }
  return value.replace(/^\.\//, '');
}
function glob(pattern) {
  let out = '^';
  for (let i = 0; i < pattern.length; i++) {
    if (pattern.slice(i, i + 3) === '**/') { out += '(?:.*/)?'; i += 2; }
    else if (pattern.slice(i, i + 2) === '**') { out += '.*'; i++; }
    else if (pattern[i] === '*') out += '[^/]*';
    else out += pattern[i].replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }
  return new RegExp(out + '$');
}
export function selectRoutes(graph, paths) {
  const files = unique(paths.map(repoPath));
  const routes = graph.routes.filter(route => route.patterns.some(pattern =>
    files.some(file => glob(pattern).test(file))));
  const skills = unique(routes.flatMap(route => route.skills));
  const options = [...new Map(routes.flatMap(route => route.optionalSkills || [])
    .map(option => [option.name, option])).values()].filter(option => !skills.includes(option.name));
  return {
    files, routes: routes.map(route => route.id),
    rules: unique([...graph.baselineRules, ...routes.flatMap(route => route.rules)]),
    skills,
    skillBindings: skills.map(skillBinding),
    skillOptions: options.map(option => ({ ...skillBinding(option.name), when: option.when })),
    roles: unique(routes.flatMap(route => route.roles)),
    checks: unique(['ecc', ...routes.flatMap(route => route.checks)]),
    commands: unique(['ecc', ...routes.flatMap(route => route.checks)]).map(id => graph.checks[id]),
  };
}
export function validateGraph(graph, repoRoot) {
  const errors = [];
  const asset = path => {
    try {
      const local = repoPath(path);
      if (!existsSync(resolve(repoRoot, local))) errors.push(`Missing asset: ${local}`);
    } catch (error) { errors.push(error.message); }
  };
  if (graph.version !== 1) errors.push('Unsupported graph version');
  asset(graph.upstreamManifest);
  graph.baselineRules.forEach(asset);
  Object.values(graph.roles).forEach(asset);
  const ids = new Set();
  for (const route of graph.routes) {
    if (ids.has(route.id)) errors.push(`Duplicate route: ${route.id}`);
    ids.add(route.id);
    route.patterns.forEach(pattern => { try { repoPath(pattern); glob(pattern); } catch (e) { errors.push(e.message); } });
    route.rules.forEach(asset);
    route.skills.forEach(name => asset(`.agents/skills/${name}/SKILL.md`));
    for (const option of route.optionalSkills || []) {
      asset(`.agents/skills/${option.name}/SKILL.md`);
      if (typeof option.when !== 'string' || !option.when.trim()) errors.push(`Missing skill trigger: ${option.name}`);
    }
    route.roles.forEach(name => { if (!graph.roles[name]) errors.push(`Unknown role: ${name}`); });
    route.checks.forEach(name => { if (!graph.checks[name]) errors.push(`Unknown check: ${name}`); });
  }
  for (const [name, command] of Object.entries(graph.checks)) {
    if (!Array.isArray(command) || command.length !== 3 || command[0] !== 'bash' ||
        command[1] !== 'scripts/verify.sh' || !['ecc', 'backend', 'frontend', 'integration'].includes(command[2])) {
      errors.push(`Invalid check command: ${name}`);
    }
  }
  const phases = new Map(graph.phases.map(phase => [phase.id, phase]));
  if (phases.size !== graph.phases.length) errors.push('Duplicate phase');
  const visited = new Set(); const active = new Set();
  function visit(id) {
    if (active.has(id)) { errors.push(`Phase cycle: ${id}`); return; }
    if (visited.has(id)) return;
    if (!phases.has(id)) { errors.push(`Unknown phase: ${id}`); return; }
    active.add(id);
    phases.get(id).dependsOn.forEach(visit);
    active.delete(id); visited.add(id);
  }
  phases.forEach(phase => visit(phase.id));
  return errors;
}

export function dependencyGraph(repoRoot) {
  const files = [];
  function walk(directory) {
    if (!existsSync(directory)) return;
    for (const entry of readdirSync(directory, { withFileTypes: true })) {
      if (['node_modules', '.next', '.git', 'build', '.gradle', 'out'].includes(entry.name)) continue;
      const path = resolve(directory, entry.name);
      if (entry.isDirectory()) walk(path);
      else if (entry.isFile() && /\.(java|js|jsx|ts|tsx|mjs|cjs)$/.test(entry.name)) files.push(relative(repoRoot, path));
    }
  }
  walk(resolve(repoRoot, 'backend/src')); walk(resolve(repoRoot, 'frontend'));
  files.sort();
  const result = buildDependencyGraph(repoRoot, files);
  const sources = new Map(); const types = new Map();
  for (const file of files.filter(file => file.endsWith('.java'))) {
    const source = readFileSync(resolve(repoRoot, file), 'utf8'); sources.set(file, source);
    const pkg = source.match(/^\s*package\s+([\w.]+)\s*;/m)?.[1];
    if (pkg) types.set(`${pkg}.${file.split('/').pop().replace(/\.java$/, '')}`, file);
  }
  for (const [file, source] of sources) {
    const imports = [...source.matchAll(/^\s*import\s+(?:static\s+)?([\w.]+)(\.\*)?\s*;/gm)];
    const edges = [];
    for (const match of imports) {
      const name = match[1];
      for (const [type, target] of types) {
        if (type === name || name.startsWith(type + '.') || (match[2] && type.startsWith(name + '.'))) {
          if (file !== target) edges.push(target);
        }
      }
    }
    result.adjacency[file] = unique(edges).sort(); result.files.push(file);
  }
  return { ...result, files: unique(result.files).sort(),
    limitations: ['Static imports only; not a call graph.', 'JS/TS: upstream relative imports only; aliases and dynamic resolution are omitted.', 'Java: explicit imports only; same-package and runtime wiring dependencies are omitted.'] };
}

function mermaid(graph) {
  const lines = ['# AstaVet ECC configuration graph', '', 'Generated from `config/ecc/harness.json`. Run `node scripts/ecc-harness.mjs graph --write`.', '', '```mermaid', 'flowchart LR'];
  graph.phases.forEach(phase => {
    lines.push(`  ${phase.id}["${phase.id}"]`);
    phase.dependsOn.forEach(parent => lines.push(`  ${parent} --> ${phase.id}`));
  });
  graph.routes.forEach(route => {
    lines.push(`  route_${route.id}["${route.id}"]`);
    lines.push(`  inspect --> route_${route.id}`);
    route.skills.forEach(skill => lines.push(`  route_${route.id} --> skill_${skill.replaceAll('-', '_')}["${skill}"]`));
    (route.optionalSkills || []).forEach(option => lines.push(`  route_${route.id} -. "when needed" .-> skill_${option.name.replaceAll('-', '_')}["${option.name}"]`));
    route.roles.forEach(role => lines.push(`  route_${route.id} --> role_${role}["role: ${role}"]`));
    route.rules.forEach(rule => lines.push(`  route_${route.id} --> rule_${rule.replace(/[^a-zA-Z0-9]/g, '_')}["${rule.replace('docs/ecc/rules/', '')}"]`));
    route.checks.forEach(check => lines.push(`  route_${route.id} --> check_${check}["verify ${check}"]`));
  });
  lines.push('```', '', 'Baseline: common coding-style, security and testing. Java adds coding-style/security; Java test files add testing.', '', 'Dashed skill edges are conditional: activate only when their task trigger applies. Verification includes diff review.', '', 'Role definitions: `.codex/agents/`. Selection prints required guidance and checks; it does not execute commands or spawn agents.', '');
  return lines.join('\n');
}
if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  try {
    const graph = JSON.parse(readFileSync(resolve(root, 'config/ecc/harness.json'), 'utf8'));
    const errors = validateGraph(graph, root);
    if (errors.length) throw new Error(errors.join('\n'));
    const [action, ...args] = process.argv.slice(2);
    if (action === 'validate') console.log('PASS ECC configuration graph: all references and workflow dependencies valid');
    else if (action === 'route' && args.length) console.log(JSON.stringify(selectRoutes(graph, args), null, 2));
    else if (action === 'deps') console.log(JSON.stringify(dependencyGraph(root), null, 2));
    else if (action === 'graph') {
      const output = mermaid(graph);
      if (args[0] === '--write') writeFileSync(resolve(root, 'docs/ecc/GRAPH.md'), output);
      else console.log(output);
    } else throw new Error('Usage: node scripts/ecc-harness.mjs validate|route <files...>|deps|graph [--write]');
  } catch (error) { console.error(error.message); process.exitCode = 1; }
}
