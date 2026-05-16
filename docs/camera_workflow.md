# AthenaPress – Kamera-Item und Bildintegration

## Konzept

Spieler verdienen oder craften die **Athena Presse-Kamera** und können damit ingame Fotos aufnehmen. Fotos landen automatisch im persönlichen Album des Spielers und können beim Schreiben von Artikeln direkt eingebunden werden.

---

## Aufnahme-Workflow (linke Maustaste)

Der Aufnahme-Ablauf läuft vollautomatisch:

```
Spieler hält Kamera-Item
Linke Maustaste drücken
        ↓
Plugin: HUD ausblenden (F8 / Hytale clean-view)
        ↓
Plugin: Screenshot auslösen (F12 / Hytale captureScreenshot)
        ↓
Plugin: HUD wieder einblenden
        ↓
ScreenshotFileWatcher erkennt neue Datei im Screenshot-Ordner
        ↓
Datei wird kopiert nach AthenaPress/images/uploaded/cam_<spieler>_<timestamp>.png
        ↓
Album-Eintrag wird angelegt (PlayerAlbumService)
        ↓
Spieler erhält Bestätigung: "Foto gespeichert"
```

Der Spieler sieht den gesamten Vorgang nicht — HUD verschwindet kurz, Bild wird geschossen, HUD kommt zurück.

---

## Java-Implementierung (Plugin-Seite)

Die vollständige Ablauflogik ist bereits implementiert:

| Klasse | Zuständigkeit |
|---|---|
| `CameraScreenshotService` | Orchestriert den gesamten Ablauf pro Spieler |
| `ScreenshotFileWatcher` | Überwacht den Screenshot-Ordner auf neue PNG-Dateien |
| `HytaleCameraUiBridge` | Interface für HUD-Toggle und Screenshot-Auslösung (Hytale API) |
| `CameraState` | Zustandsmaschine: IDLE → HIDING_HUD → CAPTURING → RESTORING_HUD |

Wenn das Kamera-Item benutzt wird:

```java
// In der Hytale-Plugin-Implementierung:
plugin.getCameraScreenshotService().onCameraItemUse(playerId, playerName);
```

Das Plugin muss `HytaleCameraUiBridge` implementieren:

```java
public class HytaleApiBridge implements HytaleCameraUiBridge {

    @Override
    public void hideHud(String playerId) {
        // Hytale API: HUD für diesen Spieler ausblenden
        // z.B. hytalePlayer.setHudVisible(false);
    }

    @Override
    public void showHud(String playerId) {
        // Hytale API: HUD wieder einblenden
        // z.B. hytalePlayer.setHudVisible(true);
    }

    @Override
    public void triggerScreenshot(String playerId) {
        // Hytale API: Screenshot auslösen
        // z.B. hytalePlayer.captureScreenshot();
        // Alternativ: KeyboardSimulator.press(KeyCode.F12);
    }
}
```

Der Screenshot-Ordner wird beim Start des Plugins übergeben:

```java
Path screenshotDir = Path.of(System.getProperty("user.home"), "Pictures", "Screenshots");
CameraScreenshotService cameraService = new CameraScreenshotService(
    hytaleCameraUiBridge,
    core.getPlayerAlbumService(),
    athenaPressRoot,
    screenshotDir
);
```

---

## Album-Verwaltung (/album)

Jeder Spieler hat ein eigenes Album unter:

```
AthenaPress/players/<spielername>/album.json
```

| Befehl | Funktion |
|---|---|
| `/album` | Album öffnen (sortiert nach Datum) |
| `/album sortieren name` | Alphabetisch sortieren |
| `/album sortieren favorit` | Favoriten zuerst |
| `/album favorit photo_0001` | Foto als Favorit markieren (★) |
| `/album umbenennen photo_0001 Marktplatz` | Foto umbenennen |
| `/album loeschen photo_0001` | Foto löschen |

---

## Integration in den Artikel-Editor

Im Schritt `/ap redaktion → Bild anhängen` wird das eigene Album automatisch angezeigt:

```
Wähle ein Foto aus deinem Album (Nummer eingeben) oder tippe 'weiter':
1. Marktplatz bei Sonnenuntergang ★
2. Rathaus Westseite
3. Brunnen bei Nacht
```

Der Spieler gibt eine Nummer ein — das Foto wird als `camera_marker` automatisch gesetzt.

---

## Bildquellen und Content Policy

| sourceType | Spieler | Admin |
|---|---|---|
| `camera_marker` | ✅ | ✅ |
| `placeholder` | ✅ | ✅ |
| `uploaded` | ❌ | ✅ |
| `screenshot` | ❌ | ✅ |
| `external` | ❌ | ✅ |

---

## Kamera-Item: Modell und Trigger

### Aktueller Stand (geparkt)

Das eigene Kamera-Modell ist vorerst zurückgestellt. Die Blockbench-Konvertierung
(korrekte Face-Anzahl, UV-Textur, Hytale-Proportionen) erweist sich ohne Modellier-
Erfahrung als zu aufwändig. Ein öffentliches Community-Modell für ein Kamera-Item
existiert bislang nicht.

Das OBJ-Quellmodell bleibt als Referenz im Projekt:

```
items/source/AP_PresseKamera.obj   (Geometrie)
items/source/AP_PresseKamera.mtl   (Materialien)
```

### Optionen für den Item-Trigger (sobald Hytale-API verfügbar)

**Option A – Machinima-Filmkamera (bevorzugt, falls API es erlaubt)**

Hytale enthält ein Machinima-Tool mit einer eingebauten Filmkamera. Falls die
Hytale-API Zugriff auf diese Asset-ID gibt, kann die Filmkamera direkt als
Modell für das AthenaPress-Kamera-Item genutzt werden – ohne eigenes Modell.

```java
// Voraussetzung: Hytale exposiert die Machinima-Kamera-Asset-ID
"model": "hytale:machinima/filmkamera"   // hypothetisch
```

**Option B – Bestehendes Hytale-Item verwenden**

Die Kamera-Funktion an ein anderes craftbares Item hängen (z.B. Fernglas,
Buch, Linse). Das Modell ist dann Hytale-intern, kein eigenes `.bbmodel` nötig.
`athena_kamera.json` muss dafür nur `"model"` auf das native Asset zeigen.

**Option C – Kein Item, Befehl stattdessen**

```
/kamera
```

Löst `CameraScreenshotService.onCameraItemUse(playerId, playerName)` direkt aus.
Modellunabhängig, sofort mit bestehendem Code umsetzbar. Kein Blockbench nötig.
Sinnvoll als Übergangslösung bis ein passendes Modell vorliegt.

### Machinima als zukünftige Erweiterung

Das Hytale-Machinima-Tool ist komplex, bietet aber Funktionen die für AthenaPress
langfristig interessant sein könnten: gesteuerte Kamerafahrten, definierte
Bildausschnitte, Szenenregie. Eine tiefere Integration ist für eine spätere Phase
denkbar, sobald die Hytale-API und das Machinima-API dokumentiert vorliegen.

### Blockbench-Referenz (falls Modell später doch gebaut wird)

Projekttyp: **Hytale Prop** (es gibt kein separates „Hytale Item" in Blockbench).
OBJ öffnen über: `Datei → Modell öffnen → AP_PresseKamera.obj` (öffnet als
Generic Model), dann `Datei → Projekt konvertieren → Hytale Prop`.

Zieldatei: `items/athena_kamera.bbmodel`

---

## Hytale Item-Definition

```json
{
  "id": "athena_kamera",
  "name": "Athena Presse-Kamera",
  "description": "Drücke die linke Maustaste um ein Foto aufzunehmen.",
  "model": "items/athena_kamera.bbmodel",
  "stackable": false,
  "maxCount": 1,
  "onPrimaryUse": {
    "plugin": "AthenaPress",
    "action": "CAMERA_CAPTURE"
  }
}
```

---

## Ordnerstruktur

```
AthenaPress/
├── images/
│   ├── placeholders/
│   └── uploaded/
│       └── cam_<spieler>_<timestamp>.png
└── players/
    └── <spielername>/
        └── album.json
```

---

## Status

| Komponente | Status |
|---|---|
| `sourceType: "camera_marker"` im Datenmodell | ✅ Fertig |
| Validierung erkennt Kamera-Bilder | ✅ Fertig |
| PNG-Preview rendert Kamera-Bilder | ✅ Fertig |
| PlayerAlbumService (add, delete, favorite, rename, sort) | ✅ Fertig |
| AlbumCommandService (/album-Befehle) | ✅ Fertig |
| CameraScreenshotService (HUD-Toggle → F12 → Album) | ✅ Fertig (API-neutral) |
| ScreenshotFileWatcher (erkennt neue Dateien) | ✅ Fertig |
| HytaleCameraUiBridge (Interface für Hytale API) | ✅ Fertig (Stub) |
| OBJ-Quellmodell `items/source/AP_PresseKamera.obj` | ✅ Fertig (Referenz) |
| Blockbench-Export als `items/athena_kamera.bbmodel` | 🔲 Geparkt – siehe Optionen A/B/C |
| Hytale Item-JSON `items/athena_kamera.json` | ✅ Angelegt (wartet auf Hytale API) |
| Item-Trigger (Item-Klick oder `/kamera`-Befehl) | ⏳ Ausstehend (Hytale API) |
| HytaleCameraUiBridge Implementierung | ⏳ Ausstehend (Hytale API) |
| Machinima-Integration (Kamerafahrten, Szenenregie) | 🔭 Langfristig – nach Hytale-API-Doku |
