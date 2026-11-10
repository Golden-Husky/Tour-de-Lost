Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Get-TourJavaTools {
    $candidateHomes = @()

    if ($env:JAVA_HOME) {
        $candidateHomes += $env:JAVA_HOME
    }

    # Oracle javapath contains launcher shims; prefer a real JDK bin directory.
    $compilerCommands = @(Get-Command javac.exe -All -ErrorAction SilentlyContinue)
    foreach ($compilerCommand in $compilerCommands) {
        if ($compilerCommand.Source -and $compilerCommand.Source -notmatch '[\\/]javapath[\\/]') {
            $candidateHomes += Split-Path -Parent (Split-Path -Parent $compilerCommand.Source)
        }
    }

    $installRoots = @(
        (Join-Path $env:ProgramFiles 'Java'),
        (Join-Path $env:ProgramFiles 'Eclipse Adoptium'),
        (Join-Path $env:ProgramFiles 'Microsoft'),
        (Join-Path $env:USERPROFILE '.jdks')
    )

    foreach ($installRoot in $installRoots) {
        if (Test-Path -LiteralPath $installRoot -PathType Container) {
            $installedJdks = @(Get-ChildItem -LiteralPath $installRoot -Directory |
                Where-Object { $_.Name -match '(?i)(jdk|temurin|openjdk)' } |
                Sort-Object Name -Descending)
            foreach ($installedJdk in $installedJdks) {
                $candidateHomes += $installedJdk.FullName
            }
        }
    }

    foreach ($candidateHome in ($candidateHomes | Select-Object -Unique)) {
        $compilerPath = Join-Path $candidateHome 'bin\javac.exe'
        $runtimePath = Join-Path $candidateHome 'bin\java.exe'
        if ((Test-Path -LiteralPath $compilerPath -PathType Leaf) -and
            (Test-Path -LiteralPath $runtimePath -PathType Leaf)) {
            return [PSCustomObject]@{
                Home = $candidateHome
                Javac = $compilerPath
                Java = $runtimePath
            }
        }
    }

    throw 'No JDK found. Install JDK 21 or newer, or set JAVA_HOME to a JDK directory.'
}
