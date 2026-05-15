# AthenaPress – Kamera-Item und Bildintegration

## Konzept

Spieler sollen mit einem ingame Kamera-Item Fotos auf dem Server aufnehmen und diese direkt als Bilder in Zeitungsartikel einbinden können.

Das System ist bereits im Datenmodell vorbereitet: `sourceType: "camera_marker"` kennzeichnet Bilder, die über das Kamera-Item entstanden sind.

---

## Aktuell verfügbare Bildquellen (sourceType)

| Wert | Bedeutung |
|---|---|
| `placeholder` | Platzhalterbild aus `AthenaPress/images/placeholders/` |
| `uploaded` | Manuell hochgeladene Bilddatei |
| `screenshot` | Externer Screenshot, manuell eingebunden |
| `external` | Externe Bild-URL |
| `camera_marker` | Aufgenommen mit dem Athena-Kamera-Item (Zielzustand) |

---

## Geplanter Workflow mit dem Kamera-Item

### Schritt 1 – Foto aufnehmen
Der Spieler hält das Kamera-Item in der Hand und löst es aus (Rechtsklick / Item-Use).

### Schritt 2 – Bild speichern
Das Hytale-Plugin erfasst die Spieler-Perspektive und speichert das Bild automatisch:

```
AthenaPress/images/uploaded/cam_<spieler>_<timestamp>.png
```

Beispiel:
```
AthenaPress/images/uploaded/cam_Mira_Baut_20260515_204512.png
```

### Schritt 3 – Artikel verknüpfen
Im Artikel wird das Bild referenziert:

```json
"image": {
  "file": "uploaded/cam_Mira_Baut_20260515_204512.png",
  "caption": "Frontansicht der neuen Bibliothek.",
  "credit": "Mira_Baut",
  "sourceType": "camera_marker"
}
```

### Schritt 4 – Preview und Validierung
Das Python-CLI und der Java-Core prüfen automatisch ob die Bilddatei existiert:

```powershell
python press.py pruefen
```

---

## Technische Vorbereitung (Java-Plugin-Seite)

Wenn das Kamera-Item in Hytale implementiert wird, muss das Plugin folgendes tun:

```java
// Pseudocode – an echte Hytale-API anpassen
onItemUse(player, item) {
    if (item.getId().equals("athena_kamera")) {
        BufferedImage screenshot = capturePlayerView(player);
        String filename = "cam_" + player.getName() + "_" + timestamp() + ".png";
        Path outputPath = dataRoot.resolve("images/uploaded/" + filename);
        ImageIO.write(screenshot, "png", outputPath.toFile());
        sendMessage(player, "Foto gespeichert: " + filename);
    }
}
```

Die gespeicherte Datei wird sofort von der Validierung und dem PNG-Preview-Renderer erkannt.

---

## Kamera-Item als Hytale Data Asset

Das Kamera-Item wird als Hytale-Item-JSON definiert:

```json
{
  "id": "athena_kamera",
  "name": "Athena Kamera",
  "description": "Nimm Fotos für das Athena Botenblatt auf.",
  "model": "items/athena_kamera.bbmodel",
  "stackable": false,
  "onUse": {
    "plugin": "AthenaPress",
    "action": "CAMERA_CAPTURE"
  }
}
```

Das `onUse`-Feld bindet das Item an unser Java-Plugin. Die genaue Syntax hängt von der finalen Hytale Plugin-API ab.

---

## Empfohlene Ordnerstruktur für Kamera-Bilder

```
AthenaPress/images/
├── placeholders/      ← Standardbilder für Artikel ohne echtes Foto
├── uploaded/          ← Manuell hochgeladene oder per Kamera aufgenommene Bilder
│   └── cam_*.png      ← Kamera-Item-Fotos (Namensschema: cam_<spieler>_<timestamp>.png)
└── thumbnails/        ← Automatisch generierte Vorschaubilder (zukünftig)
```

---

## Status

| Komponente | Status |
|---|---|
| `sourceType: "camera_marker"` im Datenmodell | Fertig |
| Validierung erkennt Kamera-Bilder | Fertig |
| PNG-Preview rendert Kamera-Bilder | Fertig (sobald Datei existiert) |
| Hytale Kamera-Item Definition | Ausstehend |
| Plugin-seitige Screenshot-Erfassung | Ausstehend (Hytale API benötigt) |
