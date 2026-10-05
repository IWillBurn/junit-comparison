# Downloads the build of the JUnit fork used by this project (junit.version in
# pom.xml) from the releases of the fork and installs it into the local Maven
# repository.
#
#   scripts\install-junit-fork.cmd [-Version <version>]
#
# MAVEN_REPO_LOCAL overrides the local repository (default: ~\.m2\repository).
param([string] $Version)

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

$root = Split-Path -Parent $PSScriptRoot
if (-not $Version) {
    $Version = ([xml] (Get-Content -Raw -Path (Join-Path $root 'pom.xml'))).project.properties.'junit.version'
}
$releases = if ($env:JUNIT_FORK_RELEASES) { $env:JUNIT_FORK_RELEASES } else { 'https://github.com/IWillBurn/junit-framework-composition/releases/download' }
$repository = if ($env:MAVEN_REPO_LOCAL) { $env:MAVEN_REPO_LOCAL } else { Join-Path $HOME '.m2\repository' }
$archive = "junit-$Version-maven-repository.zip"
$download = Join-Path ([IO.Path]::GetTempPath()) $archive

Write-Host "Downloading JUnit $Version"
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
try {
    Invoke-WebRequest -UseBasicParsing -Uri "$releases/$Version/$archive" -OutFile $download
}
catch {
    throw "Cannot download $releases/$Version/$archive ($($_.Exception.Message)). Is there a release $Version of the fork?"
}
try {
    New-Item -ItemType Directory -Force -Path $repository | Out-Null
    Expand-Archive -Force -Path $download -DestinationPath $repository
}
finally {
    Remove-Item -Force $download
}
Write-Host "Installed JUnit $Version into $repository"
