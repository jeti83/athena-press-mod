# AthenaPress installieren (fuer Server-Admins)

Diese Anleitung richtet sich an Admins, die AthenaPress auf einem Hytale-Server
(Singleplayer-Host oder dedizierter Server, Windows oder Linux) installieren
wollen, ohne selbst Java/Maven zu benoetigen.

## Voraussetzung

- Python 3 auf der Maschine, auf der die Server-/Welt-Dateien liegen
- Ein Release-Paket (`athena-press-release-<version>.zip` oder der entpackte
  Ordner), bereitgestellt vom Mod-Autor

## Ablauf

```bash
python tools/install_athena_press.py
```

Das Skript fuehrt interaktiv durch:

1. **Release-Paket finden** – sucht automatisch im aktuellen Ordner, sonst mit
   `--package <Pfad zur .zip oder zum Ordner>` angeben.
2. **Server-Verzeichnis finden** – prueft bekannte Speicherorte
   (`%APPDATA%\Hytale\UserData` unter Windows, `~/.local/share/Hytale/UserData`
   u.ae. unter Linux). Wird nichts gefunden, fragt das Skript nach dem Pfad.
   Manuell angeben: `--server-root <Pfad>`.
3. **Welt(en) auswaehlen** – listet alle Welten im `Saves`/`Worlds`-Ordner auf,
   Mehrfachauswahl per Komma oder `alle`. Manuell: `--world "Weltname"`
   (mehrfach angebbar) oder `--all-worlds`.
4. **Installation** – kopiert Plugin-JAR + Asset-Pack nach `Mods/` und schreibt
   die `manifest.json` in den `mods/pro.jeti_AthenaPress`-Ordner jeder
   gewaehlten Welt.

Am Ende: Server/Welt neu starten, ingame `/ap` eingeben.

## Nicht-interaktiv (Automatisierung, CI, mehrere Server)

```bash
python tools/install_athena_press.py \
  --package athena-press-release-0.4.0.zip \
  --server-root /srv/hytale/UserData \
  --world "Athena" \
  --non-interactive
```

## Testlauf ohne Aenderungen

```bash
python tools/install_athena_press.py --dry-run
```

Zeigt alle geplanten Datei-Operationen an, ohne etwas zu kopieren.

## Release-Paket erzeugen (nur fuer den Mod-Entwickler)

```powershell
.\scripts\build-release.ps1
```

Baut das Plugin (`mvn package`), extrahiert die `manifest.json` aus dem JAR
und packt JAR + Asset-Pack + Icons + `release-info.json` nach
`dist/athena-press-release/` sowie als `dist/athena-press-release-<version>.zip`.
Dieses Paket ist die Grundlage fuer `install_athena_press.py` – Admins
brauchen dafuer kein Maven/JDK.
