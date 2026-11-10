param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'java-tools.ps1')
& (Join-Path $PSScriptRoot 'build.ps1')

$projectRoot = Split-Path -Parent $PSScriptRoot
$javaTools = Get-TourJavaTools
$classPath = (Join-Path $projectRoot 'bin') + [IO.Path]::PathSeparator +
    (Join-Path $projectRoot 'resources')

& $javaTools.Java -cp $classPath tourdelost.Main
if ($LASTEXITCODE -ne 0) {
    throw "Game exited with code $LASTEXITCODE."
}
