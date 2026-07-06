# AthenaPress Release-Paket-Skript (nur fuer den Mod-Entwickler)
# Baut das Plugin-JAR und schnuert daraus ein self-contained Release-Paket,
# das Server-Admins ohne Java/Maven installieren koennen (siehe tools/install_athena_press.py).
#
# Aufruf: .\scripts\build-release.ps1
#
# Ergebnis:
#   dist/athena-press-release/
#     release-info.json          <- Metadaten fuer den Installer (Version, Dateinamen)
#     plugin/<jar>                <- Shaded Plugin-JAR
#     plugin/manifest.json         <- aus dem JAR extrahiert (Installer braucht kein `jar`-Tool)
#     assets/HytaleAthena.AP_Camera/  <- Asset-Pack (Item, Texturen, Sprachdateien)
#     icons/*.png
#   dist/athena-press-release-<version>.zip

$ErrorActionPreference = "Stop"

$root       = Resolve-Path "$PSScriptRoot\.."
$javaDir    = "$root\java"
$distDir    = "$root\dist"
$releaseDir = "$distDir\athena-press-release"

# ── 1. Build ──────────────────────────────────────────────────────────────────
Write-Host "Baue Plugin..." -ForegroundColor Cyan
Push-Location $javaDir
try {
    mvn -q clean package -pl athena-press-integration,athena-press-plugin -am -DskipTests
    if ($LASTEXITCODE -ne 0) { throw "Maven-Build fehlgeschlagen (Exit $LASTEXITCODE)" }
} finally {
    Pop-Location
}

$jar = Get-ChildItem "$javaDir\athena-press-plugin\target\athena-press-plugin-*.jar" |
       Where-Object { $_.Name -notlike "*original*" } |
       Select-Object -First 1

if (-not $jar) { throw "Kein Plugin-JAR gefunden unter $javaDir\athena-press-plugin\target\" }

# Version aus dem JAR-Dateinamen ableiten, z.B. athena-press-plugin-0.4.0-SNAPSHOT.jar -> 0.4.0-SNAPSHOT
$version = $jar.BaseName -replace '^athena-press-plugin-', ''

# ── 2. Release-Ordner frisch aufbauen ─────────────────────────────────────────
if (Test-Path $releaseDir) { Remove-Item $releaseDir -Recurse -Force }
New-Item -ItemType Directory -Path "$releaseDir\plugin" -Force | Out-Null
New-Item -ItemType Directory -Path "$releaseDir\icons" -Force | Out-Null

Copy-Item $jar.FullName "$releaseDir\plugin\$($jar.Name)" -Force
Write-Host "JAR eingepackt: $($jar.Name) ($([int]($jar.Length/1024)) KB)" -ForegroundColor Green

# ── 3. manifest.json aus dem JAR extrahieren ─────────────────────────────────
$tmpDir = [System.IO.Path]::GetTempPath() + "ap_release_" + [System.IO.Path]::GetRandomFileName()
New-Item -ItemType Directory -Path $tmpDir -Force | Out-Null
try {
    Push-Location $tmpDir
    & jar xf $jar.FullName manifest.json 2>$null
    if (-not (Test-Path "manifest.json")) { throw "manifest.json nicht im JAR gefunden" }
    Copy-Item "manifest.json" "$releaseDir\plugin\manifest.json" -Force
    Pop-Location
} finally {
    Remove-Item $tmpDir -Recurse -Force -ErrorAction SilentlyContinue
}
$pluginManifest = Get-Content "$releaseDir\plugin\manifest.json" -Raw | ConvertFrom-Json
$pluginFolderName = "$($pluginManifest.Group)_$($pluginManifest.Name)"

# ── 4. Asset-Pack kopieren ────────────────────────────────────────────────────
$assetSrc = "$root\mods\HytaleAthena.AP_Camera"
if (-not (Test-Path $assetSrc)) { throw "Asset-Pack nicht gefunden: $assetSrc" }
Copy-Item $assetSrc "$releaseDir\assets\HytaleAthena.AP_Camera" -Recurse -Force
Write-Host "Asset-Pack eingepackt: HytaleAthena.AP_Camera" -ForegroundColor Green

$assetManifest = Get-Content "$assetSrc\manifest.json" -Raw | ConvertFrom-Json
$assetFolderName = "HytaleAthena.AP_Camera"

# ── 5. Icons kopieren ─────────────────────────────────────────────────────────
$iconsSrc = "$root\assets\icons"
foreach ($icon in @("AP-Icon-256.png", "AP-Camera-Icon-256.png")) {
    $src = "$iconsSrc\$icon"
    if (Test-Path $src) {
        Copy-Item $src "$releaseDir\icons\$icon" -Force
    }
}

# ── 6. release-info.json fuer den Installer ──────────────────────────────────
$releaseInfo = [ordered]@{
    version           = $version
    generatedAt       = (Get-Date).ToString("yyyy-MM-ddTHH:mm:sszzz")
    pluginJar         = $jar.Name
    pluginFolderName  = $pluginFolderName
    assetFolderName   = $assetFolderName
    icons             = [ordered]@{
        plugin = "AP-Icon-256.png"
        camera = "AP-Camera-Icon-256.png"
    }
}
$releaseInfo | ConvertTo-Json -Depth 5 | Set-Content "$releaseDir\release-info.json" -Encoding utf8
Write-Host "release-info.json geschrieben (Plugin-Ordner: $pluginFolderName)" -ForegroundColor Green

# ── 7. Zip ────────────────────────────────────────────────────────────────────
$zipPath = "$distDir\athena-press-release-$version.zip"
if (Test-Path $zipPath) { Remove-Item $zipPath -Force }
Compress-Archive -Path "$releaseDir\*" -DestinationPath $zipPath
Write-Host ""
Write-Host "Release-Paket fertig:" -ForegroundColor Cyan
Write-Host "  Ordner: $releaseDir" -ForegroundColor Green
Write-Host "  Zip:    $zipPath" -ForegroundColor Green
Write-Host ""
Write-Host "Weitergabe an Server-Admins: Zip + tools/install_athena_press.py" -ForegroundColor Cyan
