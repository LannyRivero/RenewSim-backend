#!/usr/bin/env node

import { promises as fs } from 'node:fs';
import path from 'node:path';

function parseArgs(argv) {
  const args = {
    jacoco: 'target/site/jacoco/jacoco.xml',
    changedFiles: undefined,
    output: 'target/verify/simulation-changed-file-coverage.json'
  };

  for (let i = 2; i < argv.length; i += 1) {
    const token = argv[i];
    if (token === '--jacoco') {
      args.jacoco = argv[++i];
    } else if (token === '--changed-files') {
      args.changedFiles = argv[++i];
    } else if (token === '--output') {
      args.output = argv[++i];
    } else if (token === '--help' || token === '-h') {
      printHelp();
      process.exit(0);
    } else {
      throw new Error(`Unknown argument: ${token}`);
    }
  }

  if (!args.changedFiles) {
    throw new Error('Missing required argument: --changed-files <path>');
  }

  return args;
}

function printHelp() {
  process.stdout.write(
    [
      'Usage: node scripts/verify/extract-simulation-changed-coverage.mjs --changed-files <path> [--jacoco <path>] [--output <path>]',
      '',
      'Defaults:',
      '  --jacoco target/site/jacoco/jacoco.xml',
      '  --output target/verify/simulation-changed-file-coverage.json'
    ].join('\n')
  );
}

async function readRequiredFile(filePath, label) {
  try {
    return await fs.readFile(filePath, 'utf8');
  } catch (error) {
    if (error && error.code === 'ENOENT') {
      throw new Error(`Missing required artifact: ${label} not found at ${filePath}`);
    }
    throw error;
  }
}

function parseChangedFiles(rawContent) {
  const trimmed = rawContent.trim();
  if (!trimmed) {
    return [];
  }

  if (trimmed.startsWith('[')) {
    const jsonValue = JSON.parse(trimmed);
    if (!Array.isArray(jsonValue)) {
      throw new Error('Malformed changed-files artifact: expected JSON array of file paths');
    }
    return jsonValue
      .filter((item) => typeof item === 'string')
      .map((item) => normalizePath(item))
      .filter(Boolean);
  }

  return trimmed
    .split(/\r?\n/)
    .map((line) => normalizePath(line))
    .filter(Boolean);
}

function normalizePath(filePath) {
  return filePath.trim().replace(/\\/g, '/');
}

function parseJacocoSourcefiles(xml) {
  const packageRegex = /<package\s+name="([^"]+)"\s*>([\s\S]*?)<\/package>/g;
  const sourceRegex = /<sourcefile\s+name="([^"]+)"\s*>([\s\S]*?)<\/sourcefile>/g;
  const lineRegex = /<line\s+([^>]*)\/>/g;

  const byFile = new Map();
  let packageMatch;

  while ((packageMatch = packageRegex.exec(xml)) !== null) {
    const packageName = packageMatch[1];
    const packageBody = packageMatch[2];

    let sourceMatch;
    while ((sourceMatch = sourceRegex.exec(packageBody)) !== null) {
      const sourceName = sourceMatch[1];
      const sourceBody = sourceMatch[2];
      const key = `${packageName}/${sourceName}`;

      let coveredLines = 0;
      let missedLines = 0;

      let lineMatch;
      while ((lineMatch = lineRegex.exec(sourceBody)) !== null) {
        const attrs = parseAttributes(lineMatch[1]);
        const ci = Number.parseInt(attrs.ci ?? '0', 10);
        const mi = Number.parseInt(attrs.mi ?? '0', 10);

        if (Number.isNaN(ci) || Number.isNaN(mi)) {
          continue;
        }

        if (ci + mi === 0) {
          continue;
        }

        if (ci > 0) {
          coveredLines += 1;
        } else {
          missedLines += 1;
        }
      }

      byFile.set(key, { coveredLines, missedLines });
    }
  }

  if (byFile.size === 0) {
    throw new Error('Malformed JaCoCo artifact: no sourcefile entries found');
  }

  return byFile;
}

function parseAttributes(raw) {
  const attrs = {};
  const attrRegex = /(\w+)="([^"]*)"/g;
  let match;
  while ((match = attrRegex.exec(raw)) !== null) {
    attrs[match[1]] = match[2];
  }
  return attrs;
}

function toCoverageRatio(coveredLines, missedLines) {
  const total = coveredLines + missedLines;
  if (total === 0) {
    return 0;
  }
  return Number((coveredLines / total).toFixed(4));
}

function buildResult(changedFiles, jacocoByFile) {
  const normalizedUniqueChangedFiles = [...new Set(changedFiles)]
    .filter((file) => file.includes('/simulation_service/'));

  const rows = normalizedUniqueChangedFiles.map((changedFile) => {
    const jacocoKey = changedFile.replace(/^src\/main\/java\//, '');
    const metrics = jacocoByFile.get(jacocoKey) ?? { coveredLines: 0, missedLines: 0 };
    return {
      file: changedFile,
      coveredLines: metrics.coveredLines,
      missedLines: metrics.missedLines,
      coverageRatio: toCoverageRatio(metrics.coveredLines, metrics.missedLines)
    };
  });

  rows.sort((a, b) => a.file.localeCompare(b.file, 'en'));
  return rows;
}

async function main() {
  try {
    const args = parseArgs(process.argv);

    const jacocoXml = await readRequiredFile(args.jacoco, 'jacoco');
    const changedFilesRaw = await readRequiredFile(args.changedFiles, 'changed-files');

    let changedFiles;
    try {
      changedFiles = parseChangedFiles(changedFilesRaw);
    } catch (error) {
      throw new Error(`Malformed changed-files artifact: ${error.message}`);
    }

    let jacocoByFile;
    try {
      jacocoByFile = parseJacocoSourcefiles(jacocoXml);
    } catch (error) {
      throw new Error(`Malformed JaCoCo artifact: ${error.message}`);
    }

    const result = buildResult(changedFiles, jacocoByFile);

    await fs.mkdir(path.dirname(args.output), { recursive: true });
    await fs.writeFile(args.output, `${JSON.stringify(result, null, 2)}\n`, 'utf8');

    process.stdout.write(`Wrote ${result.length} coverage rows to ${args.output}\n`);
  } catch (error) {
    process.stderr.write(`${error.message}\n`);
    process.exitCode = 1;
  }
}

await main();
