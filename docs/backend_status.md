# AthenaPress Backend-Status

Stand: AthenaPress v0.4

---

## Aktueller Fokus

AthenaPress ist eine Zeitungs-Mod für den Athena-Hytale-Server.

Das Backend ist stabil. Das Visual-Preview-System erzeugt echte PNG-Doppelseiten aus den Zeitungsdaten. Die nächste Entwicklungsphase ist die Anbindung an die Hytale Plugin-API.

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

### Tests (letzter bekannter stabiler Stand)
- Core: 103 Tests, 0 Failures, 0 Errors
- Integration: 169 Tests, 0 Failures, 0 Errors

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

Folgende Adapter-Klassen sind vorbereitet aber noch nicht mit echter Hytale-API verdrahtet:

| Klasse | Was fehlt |
|---|---|
| `HytalePlayerContextResolver<TPlayer>` | Hytale-Spieler → HytalePlayerContext |
| `HytaleNewspaperVisualUiBridge` | NoesisGUI-Fenster öffnen/schließen/aktualisieren |
| `HytaleNewspaperVisualInputAdapter` | Chat-Befehl `/ap`, Item-Use, NPC-Klick → Input-Event |

Sobald die Hytale Plugin-API dokumentiert vorliegt, werden nur diese drei Stellen ausgefüllt.

---

## Kamera-Item

Für die Integration von Ingame-Fotos in Artikel ist das Kamera-Item geplant.

Details: `docs/camera_workflow.md`

---

## Bewusst geparkt

- Echte Hytale-API-Anbindung (wartet auf offizielle Dokumentation)
- NoesisGUI-UI-Implementierung
- Automatische Veröffentlichung im Spiel
- Redaktions-UI für Spieler
