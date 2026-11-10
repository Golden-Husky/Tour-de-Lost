param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'java-tools.ps1')

$projectRoot = Split-Path -Parent $PSScriptRoot
$sourceRoot = Join-Path $projectRoot 'src'
$outputRoot = Join-Path $projectRoot 'bin'
$javaTools = Get-TourJavaTools
$sourceFiles = @(Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter '*.java' |
    Sort-Object FullName | ForEach-Object { $_.FullName })

if ($sourceFiles.Count -eq 0) {
    throw 'No Java source files found under src.'
}

New-Item -ItemType Directory -Path $outputRoot -Force | Out-Null
Write-Host "JDK: $($javaTools.Home)"
& $javaTools.Javac -encoding UTF-8 --release 21 -d $outputRoot @sourceFiles
if ($LASTEXITCODE -ne 0) {
    throw "Compilation failed (exit code $LASTEXITCODE)."
}

Write-Host "Build successful: $($sourceFiles.Count) source files. Target: Java 21."
