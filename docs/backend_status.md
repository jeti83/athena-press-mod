# AthenaPress Backend-Status

Stand: AthenaPress v0.4 (Mai 2026)

---

## Aktueller Fokus

AthenaPress ist eine Zeitungs-Mod für den Athena-Hytale-Server.

Das Backend ist stabil. Der Ausgaben-Editor ist verdrahtet und einsatzbereit. Das Plugin-Modul kompiliert gegen die echte HytaleServer.jar und nutzt die reale Permission- und Message-API. Die nächste Phase ist die Vervollständigung der drei Hytale-Adapter-Stubs, sobald die Hytale Plugin-API öffentlich verfügbar ist.

---

## Was aktuell stabil funktioniert

### Python-Backend
- Artikel als JSON-Daten verwalten
- Ausgaben als JSON-Daten verwalten
- Ausgaben mit mehreren Artikeln verbinden
- Kategorien datengetrieben nutzen
- Abonnenten verwalten
- Zustell- und Lesestatus abbilden
- Entwürfe gezielt löschen
- Validierung über `python press.py pruefen`
- Zentrales CLI `press.py` mit deutschen und englischen Befehlen

### Java Core (athena-press-core)
- Liest echte AthenaPress-JSON-Daten
- Löst Ausgaben auf und zeigt sie an
- Validiert Daten auf mehreren Ebenen
- Zeigt Status-, Ausgaben- und Artikellisten
- `DemoCommandService` – Argument-Parsing
- `DemoTextService` – Textformatierung (getrennt refaktoriert)
- `DemoCommand` und `DemoCommandType` als eigene Klassen

### Java Integration (athena-press-integration)
- Spielnahe Zeitungssessions pro Spieler
- Visual-Layout-System mit Doppelseiten, Pagination, Designprofilen
- **PNG-Preview funktionsfähig** – erzeugt echte Zeitungsseiten als Bilddateien
- Artikel-Klassifizierung in Sektionen (Hauptartikel, Kurzmeldungen, Memorial, Anzeigen, Rückseite)
- Preview-Pipeline mit Runtime-Cache
- Hytale-Adapter-Schicht vorbereitet (API-neutral)
- `HytaleNewspaperVisualRuntime` als Einstiegspunkt für spätere Verdrahtung
- Input-System vorbereitet: Item, NPC, Chat-Befehl, Keybind, UI
- **`ArticleEditorService`** – Schritt-für-Schritt Artikel-Editor (vollständig)
- **`IssueEditorService`** – Ausgaben-Editor verdrahtet: Gateway, Plugin, `ApCommand`
- Pagination verbessert: keepTogether-Schwellwert 30 % → 50 % (weniger Leerraum)

### Java Plugin (athena-press-plugin)
- Kompiliert gegen echte `HytaleServer.jar`
- Nutzt reale Hytale-API: `JavaPlugin`, `AbstractCommand`, `EventRegistry`, `Message.raw()`, `hasPermission()`
- Stubs für noch nicht existente Player-Events (`PlayerConnectEvent` etc.) in `com.hypixel.hytale.event`
- `AP_ADMIN_PERMISSION = "athenapress.admin"` – via Hytale Permissions-System zuzuweisen
- **GUI-Seiten als `CustomUIPage`**: `MainMenuPage`, `ArticleEditorPage`, `IssueEditorPage`, `NewspaperPage`
- **`EditorUiBridge`** öffnet/aktualisiert GUI-Seiten über den WorldThread
- `/ap` öffnet das Hauptmenü (GUI-Overlay mit klickbaren Buttons)
- Artikel-Editor: Kategorie per Button klickbar, freier Text per Chat; UI aktualisiert sich automatisch
- Ausgaben-Editor: Schritt-für-Schritt-Overlay mit Aktions-Buttons (Weiter, Einreichen, Abbrechen)
- Nach Abschluss/Abbruch kehrt die UI automatisch zum Hauptmenü zurück

### Tests (aktueller stabiler Stand)
- Core: 103 Tests, 0 Failures, 0 Errors
- Integration: 197 Tests, 0 Failures, 0 Errors
- **Gesamt: 300 Tests**

---

## PNG-Preview erzeugen

```powershell
cd java
mvn exec:java -pl athena-press-integration -Dexec.mainClass=pro.jeti.athenapress.integration.AthenaPressVisualPngPreviewDemo -Dexec.args="issue_0003"
```

Mit eigenem Ausgabeordner:

```powershell
mvn exec:java -pl athena-press-integration -Dexec.mainClass=pro.jeti.athenapress.integration.AthenaPressVisualPngPreviewDemo "-Dexec.args=issue_0004 C:\Users\DEINNAME\Downloads\athena-press-preview"
```

---

## Verfügbare Ausgaben

| ID | Titel | Thema |
|---|---|---|
| `issue_0002` | Athena Botenblatt – Die erste echte Testausgabe | Dating, Baumfarm |
| `issue_0003` | Athena Botenblatt – Jubiläumsausgabe | Jubiläum, Rathaus, Wirtschaft, Kleinanzeigen |
| `issue_0004` | Athena Botenblatt – Sonderausgabe Stadtentwicklung | Marktfest, Bibliothek, Gerüchte, Wollbert |

---

## Offene Hytale-Anbindung

Die GUI-Infrastruktur (`CustomUIPage`, `.ui`-Dateien, `EditorUiBridge`) ist vollständig implementiert. Folgende Adapter-Klassen warten noch auf die echte Hytale-API:

| Klasse | Was fehlt |
|---|---|
| `HytalePlayerContextResolver<TPlayer>` | Hytale-Spieler → HytalePlayerContext (UUID, Name) |
| `NewspaperVisualBridge` / `HytaleNewspaperVisualUiBridge` | `openCustomPage()` läuft erst auf echtem Hytale-Server |
| `HytaleCameraUiBridge` | HUD ausblenden, Screenshot auslösen |

Sobald die Hytale Plugin-API öffentlich verfügbar ist und ein Server läuft, sind nur noch diese Stellen anzupassen. Die gesamte GUI-Logik, alle Services, alle Tests und alle `.ui`-Dateien sind bereits fertig.

---

## Kamera-Item

Für die Integration von Ingame-Fotos in Artikel ist das Kamera-Item geplant.

Details: `docs/camera_workflow.md`

---

## Bewusst geparkt

- Echte Hytale-API-Anbindung (wartet auf offizielle Dokumentation und laufenden Server)
- Automatische Veröffentlichung im Spiel
- Echte Ingame-Zustellung (Items, Mailbox)

## Umgesetzt (wartete früher auf Hytale-API)

- **Ingame-GUI vollständig implementiert**: Hauptmenü, Zeitungsleser, Artikel-Editor, Ausgaben-Editor als `CustomUIPage`
- **`.ui`-Layoutdateien** für alle vier Seiten vorhanden
- **`EditorUiBridge`** koordiniert Öffnen/Aktualisieren aller Seiten über WorldThread
