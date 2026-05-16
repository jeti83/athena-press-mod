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

## Blockbench-Modell (Athena Presse-Kamera)

Das Kamera-Modell liegt als photogrammetrischer OBJ-Scan vor (`PresseKamera_OBJ.zip`).
Der Scan hat **691.795 Vertices / 1.204.625 Faces** und ist für Hytale nicht direkt verwendbar.
Er dient als visuelle Vorlage für das Nachbauen in **Blockbench**.

### OBJ-Quellmodell

Das fertige Quellmodell liegt unter:

```
items/source/AP_PresseKamera.obj   (Geometrie)
items/source/AP_PresseKamera.mtl   (Materialien)
```

### Schritt-für-Schritt: OBJ → Blockbench → Hytale

**Schritt 1 – OBJ importieren:**
1. Blockbench öffnen → `Datei → Neu → Hytale Item` als Projekttyp wählen
2. `Datei → Importieren → OBJ-Modell` → `items/source/AP_PresseKamera.obj` auswählen
3. Modell prüfen: Falls zu viele Vertices (> 1.000), über `Polygone reduzieren` vereinfachen

**Schritt 2 – Textur anlegen:**
1. Im Panel `Texturen` → `Neu` → Größe 16×16 oder 32×32 Pixel
2. Farben manuell setzen (UV-Map in Blockbench zeichnen):
   - Gehäuse: `#1A1A1A` (sehr dunkles Grau)
   - Objektiv-Ring: `#C0C0C0` (Silber), Linse: fast-schwarz mit blauem Schimmer
   - Griff: `#2A2A2A` (leicht texturiertes Dunkelgrau)

**Geometrie (alle Maße in Blockbench-Units, 1 Unit = 1/16 Block):**

```
Hauptkörper (Kameragehäuse):
  Größe:     14 × 10 × 6 Units
  Position:  Mitte des Items
  Textur:    dunkelgrau / schwarz

Objektiv (vorne, mittig):
  Form:      Zylinder oder abgestufter Quader (3 Stufen)
  Größe:     4 × 4 × 5 Units (herausragend)
  Position:  Vordermitte des Gehäuses
  Textur:    dunkel mit hellem Glasring

Sucher (oben links):
  Größe:     3 × 2 × 2 Units
  Position:  Oberkante, linke Seite
  Textur:    schwarz

Griff (rechte Seite, unten):
  Größe:     3 × 6 × 5 Units
  Position:  Rechts am Gehäuse, leicht abgesetzt
  Textur:    leicht geriffelt / dunkelgrau

Auslöser-Knopf (oben, Griff):
  Größe:     2 × 1 × 2 Units
  Textur:    metallisch silber
```

**Textur-Workflow:**
1. UV-Map in Blockbench erstellen (16×16 oder 32×32 Pixel reichen für ein Item)
2. `image0.jpg` in einem Bildeditor als Farbvorlage öffnen
3. Hauptfarbe: sehr dunkles Grau (#1A1A1A) für Gehäuse
4. Objektiv-Ring: Silber (#C0C0C0), Linse: fast-schwarz mit blauem Schimmer
5. Griff: leicht texturiertes Dunkelgrau

**Export:**
- `Datei → Exportieren → Hytale Item (.bbmodel)`
- Dateiname: `athena_kamera.bbmodel`
- Ablage: `items/athena_kamera.bbmodel` im Hytale-Mod-Ordner

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
| OBJ-Quellmodell `items/source/AP_PresseKamera.obj` | ✅ Fertig |
| Blockbench-Import und Export als `items/athena_kamera.bbmodel` | ⏳ Ausstehend (manuell in Blockbench) |
| Hytale Item-JSON `items/athena_kamera.json` | ✅ Angelegt (wartet auf Hytale API) |
| HytaleCameraUiBridge Implementierung | ⏳ Ausstehend (Hytale API) |
