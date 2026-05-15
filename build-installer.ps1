param(
    [ValidateSet("auto", "exe", "msi", "app-image")]
    [string]$Type = "auto"
)

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$target     = Join-Path $PSScriptRoot "target"
$stagingDir = Join-Path $target "installer-input"
$destDir    = Join-Path $target "installer"
$fatJar     = Join-Path $target "gradeflow-1.0.jar"

Write-Host "=== GradeFlow Installer Builder ===" -ForegroundColor Cyan
Write-Host "  Project: $PSScriptRoot"

Write-Host "`n[1/4] Building project with Maven..." -ForegroundColor Yellow
& mvn clean package
if ($LASTEXITCODE -ne 0) { Write-Error "Maven build failed"; exit 1 }

if (-not (Test-Path $fatJar)) {
    Write-Error "Fat JAR not found at: $fatJar"
    exit 1
}
$size = [math]::Round((Get-Item $fatJar).Length / 1MB, 1)
Write-Host "  Built fat JAR: gradeflow-1.0.jar ($size MB)"

Write-Host "`n[2/4] Preparing installer staging directory..." -ForegroundColor Yellow

if (Test-Path $stagingDir) { Remove-Item $stagingDir -Recurse -Force }
New-Item -ItemType Directory -Path $stagingDir | Out-Null

[System.IO.File]::Copy($fatJar, (Join-Path $stagingDir "gradeflow-1.0.jar"), $true)

$count = (Get-ChildItem $stagingDir -Filter "*.jar").Count
Write-Host "  Staging directory: $count JAR(s)"

Write-Host "`n[3/4] Checking build environment..." -ForegroundColor Yellow
if ($Type -eq "auto") {
    $wix = Get-Command candle.exe -ErrorAction SilentlyContinue
    if ($wix) {
        $Type = "exe"
        Write-Host "  WiX Toolset found -> will produce .exe installer" -ForegroundColor Green
    } else {
        $Type = "app-image"
        Write-Host "  WiX Toolset not found -> will produce app-image (runnable directory)" -ForegroundColor Yellow
        Write-Host "  To get a proper .exe installer, install WiX Toolset 3.x and re-run:" -ForegroundColor Gray
        Write-Host "    winget install WiXToolset.WiXToolset" -ForegroundColor Gray
        Write-Host "    .\build-installer.ps1 -Type exe" -ForegroundColor Gray
    }
}

Write-Host "`n[4/4] Running jpackage (type=$Type)..." -ForegroundColor Yellow

if (Test-Path $destDir) { Remove-Item $destDir -Recurse -Force }
New-Item -ItemType Directory -Path $destDir | Out-Null

$jpackageArgs = @(
    "--input",       $stagingDir,
    "--main-jar",    "gradeflow-1.0.jar",
    "--main-class",  "gradeflow.Launcher",
    "--name",        "GradeFlow",
    "--app-version", "1.0",
    "--dest",        $destDir,
    "--type",        $Type,
    "--description", "Automated Student Assignment Grading System",
    "--vendor",      "CE316"
)

if ($Type -eq "exe" -or $Type -eq "msi") {
    $jpackageArgs += "--win-menu"
    $jpackageArgs += "--win-shortcut"
    $jpackageArgs += "--win-dir-chooser"
    $jpackageArgs += "--win-menu-group"
    $jpackageArgs += "GradeFlow"
}

& jpackage @jpackageArgs
if ($LASTEXITCODE -ne 0) { Write-Error "jpackage failed"; exit 1 }

Write-Host "`n=== Done! ===" -ForegroundColor Green
if ($Type -eq "app-image") {
    Write-Host "App image: $destDir\GradeFlow\" -ForegroundColor Cyan
    Write-Host "Run with:  $destDir\GradeFlow\GradeFlow.exe" -ForegroundColor Cyan
} else {
    $installer = Get-ChildItem $destDir | Select-Object -First 1
    Write-Host "Installer: $destDir\$($installer.Name)" -ForegroundColor Cyan
}
