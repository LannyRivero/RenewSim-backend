#!/usr/bin/env node

import { promises as fs } from 'node:fs';

const ALLOWED_BASELINE_STATUS = new Set(['PASS', 'FAIL', 'N/A']);

function parseArgs(argv) {
  const args = {
    applyProgress: 'sdd/harden-simulation-verification-observability/apply-progress.md'
  };

  for (let i = 2; i < argv.length; i += 1) {
    const token = argv[i];

    if (token === '--apply-progress') {
      args.applyProgress = argv[++i];
    } else if (token === '--help' || token === '-h') {
      printHelp();
      process.exit(0);
    } else {
      throw new Error(`Unknown argument: ${token}`);
    }
  }

  return args;
}

function printHelp() {
  process.stdout.write(
    [
      'Usage: node scripts/verify/check-safety-net-baseline.mjs [--apply-progress <path>]',
      '',
      'Defaults:',
      '  --apply-progress sdd/harden-simulation-verification-observability/apply-progress.md'
    ].join('\n')
  );
}

async function readApplyProgress(filePath) {
  try {
    return await fs.readFile(filePath, 'utf8');
  } catch (error) {
    if (error && error.code === 'ENOENT') {
      throw new Error(`Missing required artifact: apply-progress not found at ${filePath}`);
    }
    throw error;
  }
}

function parseSimulationRows(markdown) {
  const lines = markdown.split(/\r?\n/);
  const rows = [];
  let inSection = false;

  for (let i = 0; i < lines.length; i += 1) {
    const line = lines[i].trim();

    if (line.startsWith('## ')) {
      if (line === '## Simulation Work-Unit Rows') {
        inSection = true;
        continue;
      }

      if (inSection) {
        break;
      }
    }

    if (!inSection || !line.startsWith('|')) {
      continue;
    }

    if (/^\|\s*-+/.test(line)) {
      continue;
    }

    const columns = line
      .split('|')
      .map((value) => value.trim())
      .filter((value, index, arr) => !(index === 0 || index === arr.length - 1));

    if (columns.length < 3 || columns[0] === 'Row ID') {
      continue;
    }

    rows.push({
      rowId: columns[0],
      workUnit: columns[1],
      baseline: columns[2]
    });
  }

  return rows;
}

function validateRows(rows) {
  const offendingRowIds = rows
    .filter((row) => row.workUnit.toLowerCase().includes('simulation'))
    .filter((row) => !ALLOWED_BASELINE_STATUS.has(row.baseline))
    .map((row) => row.rowId);

  return [...new Set(offendingRowIds)].sort((a, b) => a.localeCompare(b, 'en'));
}

async function main() {
  try {
    const args = parseArgs(process.argv);
    const applyProgress = await readApplyProgress(args.applyProgress);
    const rows = parseSimulationRows(applyProgress);

    if (rows.length === 0) {
      throw new Error('Malformed apply-progress artifact: no simulation work-unit rows found under "## Simulation Work-Unit Rows"');
    }

    const offendingRowIds = validateRows(rows);
    if (offendingRowIds.length > 0) {
      throw new Error(`Missing Safety Net Baseline status for row id(s): ${offendingRowIds.join(', ')}`);
    }

    process.stdout.write(`Safety-net baseline completeness check PASSED (${rows.length} simulation rows validated)\n`);
  } catch (error) {
    process.stderr.write(`${error.message}\n`);
    process.exitCode = 1;
  }
}

await main();
