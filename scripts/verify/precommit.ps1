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
    throw '[precommit] JAVA_HOME is invalid and could not be auto-resolved to a JDK.'
}

Write-Host '[precommit] Running compile + unit test safety checks...'

mvn clean test jacoco:report --batch-mode --no-transfer-progress
if (-not $?) {
    throw '[precommit] Maven test phase failed.'
}

Write-Host '[precommit] OK'
