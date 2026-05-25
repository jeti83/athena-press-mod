# AthenaPress Live-Log-Monitor
# Startet automatisch mit der neuesten Hytale-Client-Log und filtert AthenaPress-Zeilen.
# Aufruf: .\scripts\watch-ap-log.ps1

$logDir = "$env:APPDATA\Hytale\UserData\Logs"

function Find-LatestLog {
    Get-ChildItem $logDir -Filter "*client.log" -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
}

$log = Find-LatestLog
if (-not $log) {
    Write-Host "Kein client.log gefunden in: $logDir" -ForegroundColor Red
    exit 1
}

Write-Host "AthenaPress Log-Monitor gestartet" -ForegroundColor Cyan
Write-Host "Datei: $($log.FullName)" -ForegroundColor DarkGray
Write-Host "Strg+C zum Beenden" -ForegroundColor DarkGray
Write-Host ("─" * 80) -ForegroundColor DarkGray

# Patterns die immer gefiltert werden sollen (unwichtig)
$skipPattern = 'cached reference to asset|Skipping pack|Unable to map shared'

# AthenaPress-relevante Patterns
$apPattern   = '\[AP\]|\[AthenaPress|AthenaPress|openPage|openCustomPage|PlayerRef|lazy registriert|CMD /ap|Menü|Editor|Zeitung'

Get-Content $log.FullName -Wait -ErrorAction SilentlyContinue | ForEach-Object {
    $line = $_

    # Neue Log-Session erkennen → ggf. auf neuere Datei wechseln
    if ($line -match 'AthenaPress geladen') {
        $newer = Find-LatestLog
        if ($newer.FullName -ne $log.FullName) {
            Write-Host ""
            Write-Host "Neue Log-Datei erkannt: $($newer.FullName)" -ForegroundColor Cyan
            # Neustart nötig – Nutzer informieren
            Write-Host "Bitte Skript neu starten für neue Datei." -ForegroundColor Yellow
        }
    }

    if ($line -match $skipPattern) { return }
    if ($line -notmatch $apPattern) { return }

    # Farbe nach Typ
    $color = 'Gray'
    if ($line -match 'SEVERE|ERROR|Ausnahme|fehlgeschlagen|null für|nicht gefunden') {
        $color = 'Red'
    } elseif ($line -match 'WARN|Fehler|kein PlayerRef|World null|Store null') {
        $color = 'Yellow'
    } elseif ($line -match 'openCustomPage.*aufgerufen|lazy registriert|AthenaPress aktiv|AthenaPress geladen') {
        $color = 'Green'
    } elseif ($line -match '\[AP\].*CMD|\[AP\].*open|\[AP\].*start') {
        $color = 'Cyan'
    }

    # Timestamp kürzen für Lesbarkeit
    $display = $line -replace '^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d+)\|[A-Z]+\|[^|]*\|', '[$1] '
    $display = $display -replace 'SERVER - \[\d{4}/\d{2}/\d{2} (\d{2}:\d{2}:\d{2})\s+\w+\]\s+', '[$1] '

    Write-Host $display -ForegroundColor $color
}
