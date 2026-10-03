#!/usr/bin/env node
import { createHash } from 'node:crypto';
import { readFileSync, readdirSync } from 'node:fs';
import { dirname, resolve, relative, isAbsolute } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const manifest = JSON.parse(readFileSync(resolve(root, 'tools/ecc/manifest.json'), 'utf8'));
const failures = [];
if (manifest.repository !== 'https://github.com/affaan-m/ECC' || !/^[a-f0-9]{40}$/.test(manifest.commit)) {
  failures.push('Invalid upstream identity');
}
const destinations = new Set();
for (const entry of manifest.files) {
  const path = resolve(root, entry.destination);
  const local = relative(root, path);
  if (local.startsWith('..') || isAbsolute(local) || destinations.has(entry.destination)) {
    failures.push(`Invalid or duplicate destination: ${entry.destination}`);
    continue;
  }
  destinations.add(entry.destination);
  try {
    const hash = createHash('sha256').update(readFileSync(path)).digest('hex');
    if (hash !== entry.sha256) failures.push(`Upstream drift: ${entry.destination}`);
  } catch {
    failures.push(`Missing file: ${entry.destination}`);
  }
}
const actualSkills = readdirSync(resolve(root, '.agents/skills'), { withFileTypes: true })
  .filter(entry => entry.isDirectory()).map(entry => entry.name);
for (const name of manifest.skills) {
  const skillPath = `.agents/skills/${name}/SKILL.md`;
  if (!destinations.has(skillPath)) failures.push(`Untracked skill: ${name}`);
  try {
    const contents = readFileSync(resolve(root, skillPath), 'utf8');
    if (!contents.startsWith('---\n') || !contents.includes(`\nname: ${name}\n`) ||
        !/\ndescription: .+/.test(contents)) failures.push(`Invalid skill metadata: ${name}`);
  } catch {
    failures.push(`Missing skill: ${name}`);
  }
}
for (const name of actualSkills) {
  if (!manifest.skills.includes(name)) failures.push(`Skill missing from manifest: ${name}`);
}
for (const required of ['AGENTS.md', 'backend/AGENTS.md', '.codex/config.toml', 'scripts/verify.sh', 'tools/ecc/LICENSE']) {
  try { readFileSync(resolve(root, required)); } catch { failures.push(`Missing integration file: ${required}`); }
}
if (failures.length) {
  failures.forEach(failure => console.error(`FAIL ${failure}`));
  process.exitCode = 1;
} else {
  console.log(`PASS ECC ${manifest.commit}: ${manifest.skills.length} skills, ${manifest.files.length} upstream files verified`);
}
