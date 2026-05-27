param(
    [string]$BaseRef = 'origin/dev/v1.2.0',
    [string]$FailFastOnFallback = 'false'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

& "$PSScriptRoot/scripts/verify/prepr.ps1" -BaseRef $BaseRef -FailFastOnFallback $FailFastOnFallback
