# AthenaPress – Gesamtentwicklung

**Stand: Mai 2026**
**Version: 0.4**
**Server: Athena (Hytale)**
**Autor: jeti83**

---

## 1. Projektziel

AthenaPress ist ein datengetriebenes Zeitungssystem für den Athena-Hytale-Server. Ziel ist eine ingame-native Zeitung, in der Spieler Artikel schreiben, Fotos aufnehmen, Ausgaben zusammenstellen und diese als Zeitungsseiten im Spiel lesen können.

Das System heißt intern **Athena Botenblatt**.

Der Entwicklungsansatz ist bewusst backend-first und API-neutral: Alle Kernfunktionen sind vollständig implementiert und testbar, bevor die echte Hytale-API angebunden wird.

---

## 2. Systemübersicht

AthenaPress besteht aus drei Schichten:

```
┌─────────────────────────────────────────────────────┐
│  Hytale-Spiel (NoesisGUI, Plugin-API)               │
│  ← Noch nicht angebunden (3 Adapter-Stubs)          │
├─────────────────────────────────────────────────────┤
│  athena-press-integration (Java)                    │
│  Visual-Layout, Editor, Album, Kamera, Sessions     │
├─────────────────────────────────────────────────────┤
│  athena-press-core (Java)                           │
│  Datenmodell, Repositories, Services, Validierung   │
├─────────────────────────────────────────────────────┤
│  Python-CLI (press.py)                              │
│  Redaktions-Werkzeuge, Dateiverwaltung              │
├─────────────────────────────────────────────────────┤
│  AthenaPress/ (JSON-Datenspeicher)                  │
│  Artikel, Ausgaben, Abonnenten, Bilder, Alben       │
└─────────────────────────────────────────────────────┘
```

---

## 3. Entwicklungsphasen

### Phase 1 – Python-Backend

Das erste Fundament ist ein Python-CLI (`press.py`) mit Einzelwerkzeugen unter `tools/`.

**Funktionen:**
- Artikel erstellen, bearbeiten, veröffentlichen, archivieren
- Ausgaben erstellen, veröffentlichen, zustellen, archivieren
- Abonnentenverwaltung
- Zustellstatus und Lesestatus
- Entwurfslöschung
- Vollständige Validierung
- Deutsche und englische Befehlsaliase (z.B. `pruefen` / `validate`)

**Befehle (Auswahl):**
```powershell
python press.py pruefen
python press.py artikel erstellen --titel "Titel" --kategorie server_news --autor Jeti
python press.py ausgabe lesen issue_0002 --voll
python press.py ausgabe zustellen issue_0002
```

---

### Phase 2 – Java-Core (athena-press-core)

Der Java-Core liest die echten AthenaPress-JSON-Daten und stellt sie für Vorschau, Validierung und spätere Spielintegration bereit.

**Repositories:**
| Klasse | Zuständigkeit |
|---|---|
| `ArticleRepository` | Artikel aus draft/published/archived |
| `IssueRepository` | Ausgaben aus draft/published/archived |
| `SubscriberRepository` | Abonnenten |
| `CategoryRepository` | Kategorien aus categories.json |
| `PlayerAlbumRepository` | Foto-Alben pro Spieler |

**Services:**
| Klasse | Zuständigkeit |
|---|---|
| `PressService` | Ausgaben und Artikel laden, auflösen |
| `ValidationService` | Mehrstufige Datenvalidierung |
| `DeliveryService` | Zustellpläne berechnen |
| `PreviewService` | Textvorschau für Ausgaben |
| `GameViewService` | Spielnahe Ausgaben- und Artikel-Views |
| `GameTextRendererService` | Konsolen-/Debugdarstellung |
| `GameNewspaperSessionService` | Spielerspezifische Zeitungssession |
| `ArticleWriteService` | Entwürfe als JSON-Dateien schreiben |
| `PlayerAlbumService` | Foto-Album-Operationen |
| `DemoCommandService` | Argument-Parsing für Demo-CLI |
| `DemoTextService` | Textformatierung für Demo-Ausgaben |

**Demo-Befehle (Maven):**
```powershell
cd java
mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--status"
mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--liste"
mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--pruefen issue_0002"
```

---

### Phase 3 – Java-Integration (athena-press-integration)

Das Integrationsmodul baut auf dem Core auf und stellt die vollständige Spielschicht bereit.

#### 3.1 Visual-Layout-System

Das Visual-System wandelt Zeitungsdaten in layoutierte Doppelseiten um.

**Ablauf:**
```
GameIssueView
  → NewspaperArticleCompositionService   (Artikel → Visual-Blöcke)
  → NewspaperVisualPaginationService     (Blöcke → Seiten)
  → NewspaperDoublePageCompositionService (Seiten → Doppelseiten)
  → NewspaperPreviewService              (Doppelseiten → PreviewIssue)
  → NewspaperPreviewImageRenderer        (PreviewIssue → PNG-Dateien)
```

**Block-Typen:** HEADLINE, SUBHEADLINE, BODY_TEXT, IMAGE, CAPTION, QUOTE, NOTICE, ADVERTISEMENT, DIVIDER

**Sektionen:** TITLE_PAGE, MAIN_ARTICLE, MIXED_ARTICLES, ADVERTISEMENTS, SHORT_NOTICES, MEMORIAL, BACK_PAGE

**Design-Optionen:**
- Layout-Mood: CLASSIC_NEWSPAPER, LOOSE_COMMUNITY_SHEET, FEATURE_DOCUMENT
- Seitenecken: NONE, SUBTLE_TOP_FOLDS, HANGING_TOP_CORNERS
- Cover-Policy: STANDALONE_TITLE_PAGE
- Artikel-Fluss: KEEP_ARTICLES_TOGETHER_WHEN_READABLE
- Navigation: PAGE_TURNING_WITH_SUBTLE_MENU

#### 3.2 PNG-Vorschau

Vollständige Zeitungsseiten werden als PNG-Doppelseiten exportiert.

```powershell
cd java
mvn exec:java -pl athena-press-integration -Dexec.mainClass=pro.jeti.athenapress.integration.AthenaPressVisualPngPreviewDemo "-Dexec.args=issue_0004 C:\Users\jeti8\Downloads\athena-press-preview"
```

**Visuelles Design:**
- Hintergrund: Dunkelbraun (#3E3731)
- Papier: Cremeweiß (#F2EBDD)
- Tinte: Tiefbraun (#27221E)
- Akzent: Rotbraun (#7C2D25)
- Schrift: Serif, Anti-Aliased
- Seitenecken: Hängende Dreiecke oben

#### 3.3 Artikel-Editor (Ingame)

Spieler können Artikel direkt im Spiel schreiben.

**Flow:**
```
/ap redaktion
  → Titel eingeben
  → Kategorie wählen (aus categories.json)
  → Artikeltext schreiben
  → Foto aus Album wählen (oder überspringen)
  → Vorschau und Einreichen
  → Draft-JSON wird gespeichert
```

**Klassen:**
| Klasse | Zuständigkeit |
|---|---|
| `ArticleEditorSession` | Bearbeitungsstand pro Spieler |
| `ArticleEditorService` | Schritt-für-Schritt-Flow |
| `ArticleEditorView` | Anzeige pro Schritt |
| `ArticleEditorStep` | ENTER_TITLE → ENTER_CATEGORY → ENTER_BODY → ATTACH_IMAGE → REVIEW → SUBMITTED |
| `ArticleContentPolicy` | Server-only-Regel: nur camera_marker + placeholder für Spieler |

#### 3.4 Foto-Album-System

Jeder Spieler besitzt ein persönliches Fotoalbum.

**Datenspeicherung:**
```
AthenaPress/players/<spielername>/album.json
AthenaPress/images/uploaded/cam_<spieler>_<timestamp>.png
```

**Album-Befehle:**
```
/album                              → Album öffnen
/album sortieren name|datum|favorit → Sortierung ändern
/album favorit <id>                 → Favorit togglen (★)
/album umbenennen <id> <name>       → Foto umbenennen
/album loeschen <id>                → Foto löschen
```

**Klassen:**
| Klasse | Zuständigkeit |
|---|---|
| `PlayerPhoto` | Einzelnes Foto (id, filename, name, capturedAt, favorite) |
| `PlayerAlbum` | Sammlung aller Fotos eines Spielers |
| `AlbumSortOrder` | DATE / NAME / FAVORITE |
| `PlayerAlbumService` | add, delete, toggleFavorite, rename, listPhotos |
| `AlbumCommandService` | /album-Befehlsverarbeitung |
| `AlbumView` | Anzeige des Albums |

#### 3.5 Kamera-Item-System

Die **Athena Presse-Kamera** ist ein craftbares/verdienbares Ingame-Item.

**Aufnahme-Ablauf (linke Maustaste):**
```
Linke Maustaste mit Kamera-Item
  → HUD ausblenden (F8 / Hytale API)
  → Screenshot auslösen (F12 / Hytale captureScreenshot)
  → HUD wieder einblenden
  → ScreenshotFileWatcher erkennt neue PNG-Datei
  → Kopieren nach AthenaPress/images/uploaded/
  → Album-Eintrag anlegen
```

**Klassen:**
| Klasse | Zuständigkeit |
|---|---|
| `CameraScreenshotService` | Gesamter Aufnahme-Ablauf pro Spieler |
| `ScreenshotFileWatcher` | Java-WatchService auf Screenshot-Ordner |
| `HytaleCameraUiBridge` | Interface: hideHud, showHud, triggerScreenshot |
| `CameraState` | IDLE / HIDING_HUD / CAPTURING / RESTORING_HUD |

**Modell:** Das OBJ-Referenzmodell (`PresseKamera_OBJ.zip`) enthält 691.795 Vertices und 1.204.625 Faces (photogrammetrischer Scan via ImageToStl). Für Hytale muss das Modell in **Blockbench** neu aufgebaut werden (~50–200 Faces). Detaillierte Bauanleitung: `docs/camera_workflow.md`.

#### 3.6 Hytale-Adapter-Schicht

Die Integration ist vollständig API-neutral. Drei Interface-Stubs warten auf die echte Hytale-API:

| Interface | Was fehlt |
|---|---|
| `HytalePlayerContextResolver<TPlayer>` | Hytale-Spieler → HytalePlayerContext |
| `HytaleNewspaperVisualUiBridge` | NoesisGUI-Fenster öffnen/aktualisieren/schließen |
| `HytaleCameraUiBridge` | HUD toggle + Screenshot-Auslösung |

**Einstiegspunkt für die Hytale-Anbindung:**
```java
HytaleNewspaperVisualRuntime<TPlayer> runtime = new HytaleNewspaperVisualRuntime<>(
    plugin,
    textUiPort,
    visualUiBridge,        // ← NoesisGUI hier implementieren
    playerContextResolver  // ← echten Spieler hier auflösen
);
```

---

## 4. Verfügbare Ausgaben

| ID | Titel | Inhalt | Spreads |
|---|---|---|---|
| `issue_0002` | Die erste echte Testausgabe | Dating, Baumfarm, Kurzmeldungen | 4 |
| `issue_0003` | Jubiläumsausgabe | Servergeburtstag, Rathaus, Eisenmarkt, Kleinanzeigen | 4 |
| `issue_0004` | Sonderausgabe Stadtentwicklung | Marktfest, Bibliothek, Spawngerücht, Wollbert | 5 |

---

## 5. Datenmodell

### Artikel (articles/published/article_XXXX.json)
```json
{
  "id": "article_0001",
  "status": "published",
  "categoryId": "server_news",
  "title": "...",
  "subtitle": "...",
  "teaser": "...",
  "summary": "...",
  "author": { "playerName": "Jeti", "playerUuid": "..." },
  "body": "...",
  "image": {
    "file": "uploaded/cam_Jeti_20260515.png",
    "caption": "...",
    "credit": "...",
    "sourceType": "camera_marker"
  },
  "location": { "enabled": true, "world": "Hauptwelt", "x": 42, "y": 64, "z": -18 },
  "tags": ["server", "news"],
  "createdAt": "2026-05-15T19:00:00+02:00",
  "publishedAt": "2026-05-15T19:30:00+02:00"
}
```

### Ausgabe (issues/published/issue_XXXX.json)
```json
{
  "id": "issue_0004",
  "status": "published",
  "issueNumber": 4,
  "title": "Athena Botenblatt",
  "subtitle": "Sonderausgabe: Stadtentwicklung",
  "cover": { "mainArticleId": "article_0012", "image": "placeholders/no_image.png" },
  "articles": ["article_0012", "article_0013", "article_0014", "article_0015"],
  "publishedAt": "2026-05-15T20:30:00+02:00",
  "deliveredToSubscribers": false
}
```

### Foto-Album (players/<name>/album.json)
```json
{
  "playerName": "Jeti",
  "photos": [
    {
      "id": "photo_0001",
      "filename": "cam_Jeti_20260515_204512.png",
      "name": "Marktplatz bei Sonnenuntergang",
      "capturedAt": "2026-05-15T20:45:12+02:00",
      "favorite": true,
      "tags": []
    }
  ]
}
```

### Kategorien (templates/categories.json)
| ID | Name |
|---|---|
| `headline` | Schlagzeile |
| `server_news` | Server-News |
| `build_projects` | Bauprojekte |
| `economy` | Wirtschaft & Handel |
| `classifieds` | Kleinanzeigen |
| `dating` | Herzblatt der Farmwelt |

---

## 6. Teststand

| Modul | Tests | Failures | Errors |
|---|---|---|---|
| athena-press-core | 115 | 0 | 0 |
| athena-press-integration | 177 | 0 | 0 |
| **Gesamt** | **292** | **0** | **0** |

**Testbefehl:**
```powershell
cd java && mvn -B clean verify
```

---

## 7. Technische Umgebung

| Komponente | Version |
|---|---|
| Java | 25.0.2 (Microsoft OpenJDK) |
| Maven | 3.9.15 |
| JUnit | 5.11.4 |
| Jackson | 2.18.2 |
| Python | 3.x |
| Hytale UI | NoesisGUI (geplant) |

---

## 8. Ordnerstruktur

```
athena-press-mod/
├── AthenaPress/
│   ├── config.json
│   ├── articles/
│   │   ├── draft/
│   │   ├── published/       ← article_0001 bis article_0015
│   │   └── archived/
│   ├── issues/
│   │   ├── draft/
│   │   ├── published/       ← issue_0002 bis issue_0004
│   │   └── archived/
│   ├── images/
│   │   ├── placeholders/
│   │   └── uploaded/        ← Kamera-Fotos (cam_*.png)
│   ├── players/
│   │   └── <name>/album.json
│   ├── subscriptions/
│   │   └── subscribers.json
│   └── templates/
│       ├── categories.json
│       ├── article_template.json
│       └── issue_template.json
├── docs/
│   ├── backend_status.md
│   ├── camera_workflow.md
│   ├── commands.md
│   ├── data_model.md
│   ├── integration_architecture.md
│   ├── mod_mvp.md
│   └── workflow.md
├── tools/                   ← Python-Einzelwerkzeuge
├── press.py                 ← Zentrales Python-CLI
└── java/
    ├── pom.xml
    ├── athena-press-core/
    └── athena-press-integration/
```

---

## 9. Offene Punkte (Roadmap)

### Sofort umsetzbar (kein Hytale API nötig)

| Aufgabe | Beschreibung |
|---|---|
| Blockbench-Modell | Kamera-Item in Blockbench bauen (~50–200 Faces, Anleitung in camera_workflow.md) |
| Ausgaben-Editor | Ingame-Tool zum Zusammenstellen von Ausgaben aus vorhandenen Artikeln |
| Admin-Befehle | `/ap veröffentlichen`, `/ap archivieren` für Redakteure |
| Pagination verbessern | Leerraum auf Folgeseiten bei langen Artikeln reduzieren |

### Wartet auf Hytale-API

| Aufgabe | Beschreibung |
|---|---|
| `HytaleNewspaperVisualUiBridge` | NoesisGUI-Implementierung für Zeitungs-Overlay |
| `HytalePlayerContextResolver` | Echten Spieler → HytalePlayerContext |
| `HytaleCameraUiBridge` | HUD toggle + captureScreenshot |
| Kamera-Item-JSON | Hytale Data Asset Definition |
| Plugin-Manifest | Hytale Plugin-Einstiegspunkt |

---

## 10. Ingame-Flow (Zielzustand)

```
Spieler betritt den Server
  → Ungelesene Ausgaben werden zugestellt (Ingame-Item oder Benachrichtigung)

Spieler tippt /ap
  → Zeitung öffnet sich (NoesisGUI Overlay)
  → Titelseite wird angezeigt
  → Navigation per Klick oder Taste (Doppelseiten-Blättern)

Spieler tippt /ap redaktion
  → Artikel-Editor öffnet sich
  → Schritt für Schritt: Titel, Kategorie, Text, Foto
  → Foto aus Album wählen oder mit Kamera-Item aufnehmen
  → Einreichen → Draft gespeichert

Admin tippt /ap veröffentlichen article_XXXX
  → Artikel wird veröffentlicht

Admin stellt Ausgabe zusammen und veröffentlicht
  → Abonnenten erhalten Benachrichtigung

Spieler hält Kamera-Item → linke Maustaste
  → HUD weg, Screenshot, HUD zurück
  → Foto im Album verfügbar
  → Foto im nächsten Artikel verwendbar
```

---

*Generiert: Mai 2026 — AthenaPress v0.4*
