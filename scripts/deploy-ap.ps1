# AthenaPress Deployment-Skript
# Baut das Plugin-JAR und deployt es in die Hytale-Installation.
# Aufruf: .\scripts\deploy-ap.ps1
#
# Was dieses Skript macht:
#   1. Maven-Build (athena-press-integration + athena-press-plugin)
#   2. Shaded JAR → %APPDATA%\Hytale\UserData\Mods\
#   3. manifest.json aus JAR → Saves\<Welt>\mods\pro.jeti_AthenaPress\
#      (Hytale Client erwartet dort eine Kopie für Versions-Check;
#       ohne diese Datei erscheint eine harmlose DEBUG-Warnung 4x im Log)
#   4. Mod-Icons → jeweilige Mod-Ordner (icon-256.png, 256x256 PNG)

$ErrorActionPreference = "Stop"

$javaDir    = "$PSScriptRoot\..\java"
$modsDir    = "$env:APPDATA\Hytale\UserData\Mods"
$savesDir   = "$env:APPDATA\Hytale\UserData\Saves"
$worldName  = "Jetis World"
$pluginData = "$savesDir\$worldName\mods\pro.jeti_AthenaPress"

# ── 1. Build ──────────────────────────────────────────────────────────────────
Write-Host "Baue Plugin..." -ForegroundColor Cyan
Push-Location $javaDir
try {
    mvn -q clean package -pl athena-press-integration,athena-press-plugin -am -DskipTests
    if ($LASTEXITCODE -ne 0) { throw "Maven-Build fehlgeschlagen (Exit $LASTEXITCODE)" }
} finally {
    Pop-Location
}

# ── 2. JAR deployen ───────────────────────────────────────────────────────────
$jar = Get-ChildItem "$javaDir\athena-press-plugin\target\athena-press-plugin-*.jar" |
       Where-Object { $_.Name -notlike "*original*" } |
       Select-Object -First 1

if (-not $jar) { throw "Kein Plugin-JAR gefunden unter $javaDir\athena-press-plugin\target\" }

$destJar = "$modsDir\athena-press-plugin-0.4.0-SNAPSHOT.jar"
Copy-Item $jar.FullName $destJar -Force
Write-Host "JAR deployed: $($jar.Name) ($([int]($jar.Length/1024)) KB)" -ForegroundColor Green

# ── 3. manifest.json in Welt-Mods-Ordner ─────────────────────────────────────
# Der Client scannt Saves\<Welt>\mods\<Group>_<Name>\ und erwartet dort
# eine manifest.json. Ohne sie erscheint "has no manifest.json, skipping" im Log.
# Das verhindert keine Plugin-Funktion, ist aber unschön.
if (Test-Path $pluginData) {
    $manifestSrc = "$modsDir\pro.jeti_AthenaPress_manifest.json"

    # Manifest-Inhalt direkt aus der JAR-Ressource lesen
    $tmpDir = [System.IO.Path]::GetTempPath() + "ap_deploy_" + [System.IO.Path]::GetRandomFileName()
    New-Item -ItemType Directory -Path $tmpDir -Force | Out-Null
    try {
        Push-Location $tmpDir
        & jar xf $jar.FullName manifest.json 2>$null
        if (Test-Path "manifest.json") {
            Copy-Item "manifest.json" "$pluginData\manifest.json" -Force
            Write-Host "manifest.json in $pluginData gesetzt" -ForegroundColor Green
        } else {
            Write-Host "WARNUNG: manifest.json nicht im JAR gefunden" -ForegroundColor Yellow
        }
        Pop-Location
    } finally {
        Remove-Item $tmpDir -Recurse -Force -ErrorAction SilentlyContinue
    }
} else {
    Write-Host "Welt-Datenordner nicht gefunden: $pluginData" -ForegroundColor Yellow
    Write-Host "  (normal beim ersten Start – manifest.json wird beim naechsten Deploy gesetzt)" -ForegroundColor DarkGray
}

# ── 4. Mod-Icons deployen ────────────────────────────────────────────────────
# Hytale erwartet icon-256.png (256x256 PNG) im Mod-Ordner.
$iconsDir = "$PSScriptRoot\..\assets\icons"

$apIcon      = "$iconsDir\AP-Icon-256.png"
$cameraIcon  = "$iconsDir\AP-Camera-Icon-256.png"
$apCameraDir = "$modsDir\HytaleAthena.AP_Camera"

if (Test-Path $cameraIcon) {
    Copy-Item $cameraIcon "$apCameraDir\icon-256.png" -Force
    Write-Host "AP_Camera icon-256.png gesetzt" -ForegroundColor Green
}

if ((Test-Path $apIcon) -and (Test-Path $pluginData)) {
    Copy-Item $apIcon "$pluginData\icon-256.png" -Force
    Write-Host "AthenaPress icon-256.png in $pluginData gesetzt" -ForegroundColor Green
}

Write-Host ""
Write-Host "Deploy abgeschlossen. Hytale-Server neu starten." -ForegroundColor Cyan
