param(
    [string]$BaseRef = 'origin/dev/v1.2.0',
    [string]$FailFastOnFallback = 'false'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Resolve-JavaHome {
    $candidates = @()

    $javacCmd = Get-Command javac -ErrorAction SilentlyContinue
    if ($null -ne $javacCmd) {
        $javacExe = (Resolve-Path -LiteralPath $javacCmd.Source).Path
        $candidates += (Split-Path -Path (Split-Path -Path $javacExe -Parent) -Parent)
    }

    $javaCmd = Get-Command java -ErrorAction SilentlyContinue
    if ($null -ne $javaCmd) {
        $javaExe = (Resolve-Path -LiteralPath $javaCmd.Source).Path
        $candidates += (Split-Path -Path (Split-Path -Path $javaExe -Parent) -Parent)
    }

    foreach ($candidate in $candidates | Select-Object -Unique) {
        if ((Test-Path -LiteralPath $candidate) -and (Test-Path -LiteralPath (Join-Path $candidate 'bin\javac.exe'))) {
            return $candidate
        }
    }

    $jdkSearchRoots = @(
        'C:\Program Files\Java',
        'C:\Program Files (x86)\Java'
    )

    foreach ($root in $jdkSearchRoots) {
        if (-not (Test-Path -LiteralPath $root)) {
            continue
        }

        $jdkDirs = Get-ChildItem -LiteralPath $root -Directory -Filter 'jdk*' -ErrorAction SilentlyContinue |
            Sort-Object Name -Descending

        foreach ($jdkDir in $jdkDirs) {
            if (Test-Path -LiteralPath (Join-Path $jdkDir.FullName 'bin\javac.exe')) {
                return $jdkDir.FullName
            }
        }
    }

    return $null
}

if ([string]::IsNullOrWhiteSpace($env:JAVA_HOME) -or -not (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
    $resolvedJavaHome = Resolve-JavaHome
    if (-not [string]::IsNullOrWhiteSpace($resolvedJavaHome)) {
        $env:JAVA_HOME = $resolvedJavaHome
    }
}

if ([string]::IsNullOrWhiteSpace($env:JAVA_HOME) -or -not (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
    throw '[prepr] JAVA_HOME is invalid and could not be auto-resolved to a JDK.'
}

$testDbAdminUrl = 'jdbc:mysql://127.0.0.1:3306/mysql'
$testDbHostPort = '127.0.0.1:3306'
$testDbUser = 'root'
$testDbPass = 'root'

Write-Host '[prepr] Running unit tests + coverage...'
mvn clean test jacoco:report `
  "-Dtest.db.admin.url=$testDbAdminUrl" `
  "-Dtest.db.hostport=$testDbHostPort" `
  "-Dtest.db.user=$testDbUser" `
  "-Dtest.db.pass=$testDbPass" `
  "-Dmigration.it.fail-fast-on-fallback=$FailFastOnFallback" `
  --batch-mode --no-transfer-progress
if (-not $?) { throw '[prepr] Unit tests/coverage failed.' }

Write-Host '[prepr] Running scoped migration integration test...'
mvn failsafe:integration-test failsafe:verify `
  '-Dit.test=SimulationSchemaMigrationIT' `
  "-Dtest.db.admin.url=$testDbAdminUrl" `
  "-Dtest.db.hostport=$testDbHostPort" `
  "-Dtest.db.user=$testDbUser" `
  "-Dtest.db.pass=$testDbPass" `
  "-Dmigration.it.fail-fast-on-fallback=$FailFastOnFallback" `
  --batch-mode --no-transfer-progress
if (-not $?) { throw '[prepr] SimulationSchemaMigrationIT failed.' }

$reportFile = Get-ChildItem -LiteralPath 'target/failsafe-reports' -Filter 'TEST-*SimulationSchemaMigrationIT*.xml' -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $reportFile) { throw '[prepr] Missing SimulationSchemaMigrationIT report.' }

[xml]$reportXml = Get-Content -LiteralPath $reportFile.FullName
$suite = $reportXml.testsuite
if (($suite.failures -as [int]) -gt 0 -or ($suite.errors -as [int]) -gt 0) {
    throw '[prepr] SimulationSchemaMigrationIT report contains failures/errors.'
}

$evidenceFile = 'target/verify/migration-it-mode-evidence.json'
if (-not (Test-Path -LiteralPath $evidenceFile)) {
    throw "[prepr] Missing migration evidence file: $evidenceFile"
}

$evidence = Get-Content -LiteralPath $evidenceFile -Raw | ConvertFrom-Json
if ([string]::IsNullOrWhiteSpace($evidence.mode)) {
    throw '[prepr] Migration evidence missing required field: mode.'
}

if ($FailFastOnFallback -eq 'true' -and $evidence.mode -eq 'JDBC_FALLBACK') {
    throw "[prepr] E_MIGRATION_IT_FALLBACK_POLICY violated (mode=$($evidence.mode), reason=$($evidence.fallbackReason))."
}

New-Item -ItemType Directory -Path 'target/verify' -Force | Out-Null

Write-Host "[prepr] Building changed-files from base ref: $BaseRef"
$baseExists = $false
git rev-parse --verify $BaseRef *> $null
if ($LASTEXITCODE -eq 0) { $baseExists = $true }

if ($baseExists) {
    git diff --name-only $BaseRef HEAD | Out-File -LiteralPath 'target/verify/changed-files.txt' -Encoding utf8
} else {
    Write-Host "[prepr] Base ref not found ($BaseRef). Falling back to git ls-files."
    git ls-files | Out-File -LiteralPath 'target/verify/changed-files.txt' -Encoding utf8
}

Write-Host '[prepr] Extracting simulation changed-file coverage artifact...'
node scripts/verify/extract-simulation-changed-coverage.mjs `
  --jacoco target/site/jacoco/jacoco.xml `
  --changed-files target/verify/changed-files.txt `
  --output target/verify/simulation-changed-file-coverage.json
if (-not $?) { throw '[prepr] Failed to extract changed-file coverage.' }

Write-Host '[prepr] Enforcing safety-net baseline gate...'
node scripts/verify/check-safety-net-baseline.mjs `
  --apply-progress sdd/harden-simulation-verification-observability/apply-progress.md
if (-not $?) { throw '[prepr] Safety-net baseline gate failed.' }

$securityReport = Get-ChildItem -LiteralPath 'target/surefire-reports' -Filter 'TEST-*SecurityRateLimitPropertiesBindingTest*.xml' -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $securityReport) {
    throw '[prepr] Missing SecurityRateLimitPropertiesBindingTest report.'
}

[xml]$securityXml = Get-Content -LiteralPath $securityReport.FullName
$securitySuite = $securityXml.testsuite
if (($securitySuite.failures -as [int]) -gt 0 -or ($securitySuite.errors -as [int]) -gt 0) {
    throw '[prepr] SecurityRateLimitPropertiesBindingTest failed.'
}

Write-Host "[prepr] OK (mode=$($evidence.mode), fallbackReason=$($evidence.fallbackReason), isolationId=$($evidence.isolationId))"
