# AthenaPress Mod-MVP

Diese Datei beschreibt den aktuellen Fokus der Mod-Entwicklung.

AthenaPress soll nicht nur ein Konsolenbackend für Zeitungsdaten sein, sondern später ein spielnahes Zeitungssystem für den Athena-Hytale-Server.

Der Backend-Stand gilt für den nächsten Schritt als ausreichend stabil. Neue Backend-Funktionen sollen nur noch ergänzt werden, wenn sie direkt dem späteren Ingame-Zeitungserlebnis helfen.

## MVP 1: Zeitung öffnen und Artikel lesen

MVP bedeutet Minimum Viable Product, also die kleinste brauchbare Version.

MVP 1 konzentriert sich auf genau diesen Spielerfluss:

1. Ein Spieler öffnet eine vorhandene veröffentlichte Zeitungsausgabe.
2. Die Ausgabe wird geladen.
3. Die referenzierten Artikel werden aufgelöst.
4. Eine spielnahe Übersicht wird erzeugt.
5. Ein Artikel kann ausgewählt werden.
6. Der Artikeltext wird lesbar angezeigt.
7. Der Spieler kann zur Übersicht zurückkehren.
8. Die Ausgabe kann geschlossen werden.

Damit ist MVP 1 auf Java-Core-Ebene erreicht.

## MVP 2: Native Visual-Zeitung vorbereiten

MVP 2 konzentriert sich darauf, eine veröffentlichte Ausgabe als immersive, native Zeitungsansicht vorzubereiten.

Zielbild:

1. Ein Spieler öffnet eine veröffentlichte Ausgabe, später vermutlich über `/ap`.
2. Die Ausgabe wird als Visual-Preview geladen.
3. Artikel werden klassifiziert und in Zeitungsbereiche einsortiert.
4. Inhalte werden auf Seiten und Doppelseiten verteilt.
5. Eine UI-nahe Visual-View wird erzeugt.
6. Spätere native Hytale-Overlay-Buttons können weiterblättern, zurückblättern und schließen.
7. Disconnect und Timeout räumen Visual-Sessions sauber auf.

Damit ist MVP 2 adapterseitig vorbereitet, aber noch nicht an echte Hytale-API gebunden.

## Aktuell erfüllte MVP-1-Bausteine

### Veröffentlichung laden

Zuständige Bausteine:

- PressService
- GameViewService

Aufgabe:

- veröffentlichte Ausgabe anhand ihrer ID laden
- referenzierte Artikel auflösen
- nicht veröffentlichte oder fehlende Ausgaben nicht als spielbare Ausgabe behandeln

### Spielnahe Datenstruktur erzeugen

Zuständige Bausteine:

- GameIssueView
- GameArticleView
- GameViewService

Aufgabe:

- technische JSON-Daten in eine UI-freundliche Struktur überführen
- Ausgabeninformationen bereitstellen
- Artikelliste bereitstellen
- Artikel per ID auffindbar machen

### Spielnahe Textdarstellung erzeugen

Zuständiger Baustein:

- GameTextRendererService

Aufgabe:

- Übersichtstext für eine Zeitungsausgabe erzeugen
- Artikelnummern anzeigen
- Artikelüberschriften und Kurztexte darstellen
- vollständigen Artikeltext lesbar ausgeben
- sinnvolle Meldungen bei fehlender Ausgabe oder fehlendem Artikel liefern

### Spielerfluss steuern

Zuständiger Baustein:

- GameNewspaperSessionService

Aufgabe:

- Ausgabe öffnen
- aktuelle Übersicht anzeigen
- Artikel per Nummer anzeigen
- Artikel per ID anzeigen
- zur Übersicht zurückkehren
- Ausgabe schließen
- aktuellen Sitzungszustand kennen

## Aktueller technischer Ablauf

```text
JSON-Daten
   ↓
PressService
   ↓
GameViewService
   ↓
GameIssueView / GameArticleView
   ↓
GameTextRendererService
   ↓
GameNewspaperSessionService
   ↓
spätere Hytale-Mod / UI / Item-Interaktion
```

## Aktuell erfüllte MVP-2-Bausteine

### Visual-Preview-Pipeline

Zuständige Bausteine:

- NewspaperPreviewPipelineService
- NewspaperVisualRuntimeCache
- NewspaperArticleCompositionService
- NewspaperVisualPaginationService
- NewspaperDoublePageCompositionService

Aufgabe:

- echte veröffentlichte Ausgaben laden
- Artikel in Visual-Blöcke überführen
- optionale Sektionen auslassen, wenn sie nicht benötigt werden
- Inhalte auf Seiten und Doppelseiten verteilen
- vorbereitete Previews zwischenspeichern

### Visual-UI-Schicht

Zuständige Bausteine:

- PlayerNewspaperVisualView
- PlayerNewspaperVisualViewFactory
- PlayerNewspaperVisualUiController
- PlayerNewspaperVisualUiPort

Aufgabe:

- Visual-Responses in UI-nahe Views umwandeln
- aktuelle Doppelseite anzeigen
- weiterblättern
- zurückblättern
- Zeitung schließen

### Hytale-Adapter-Vorbereitung

Zuständige Bausteine:

- HytalePlayerContext
- HytalePlayerContextResolver
- HytaleNewspaperVisualUiBridge
- HytaleNewspaperVisualUiPort
- HytaleNewspaperVisualInputAdapter
- HytaleNewspaperLifecycleAdapter
- HytaleNewspaperVisualRuntime

Aufgabe:

- spätere echte Hytale-Spielerobjekte in neutrale Kontexte übersetzen
- native Visual-Views an eine spätere Hytale-UI-Brücke weiterreichen
- Overlay-Eingaben adapter-neutral verarbeiten
- Join, Disconnect, Timeout und Shutdown vorbereiten
- Runtime-Bausteine zentral zusammensetzen

## Aktuelle Grenze

Noch nicht Teil des MVP (wartet auf echten Hytale-Server):

- Echte Hytale-API-Imports (Server noch nicht öffentlich)
- Tatsächliches Rendern der `CustomUIPage`-Overlays im Spiel
- Echte Server-Hooks
- Echte Zustellung im Spiel (Items, Mailbox)

## MVP 3: Ingame-GUI (implementiert, wartet auf Server)

Die GUI-Infrastruktur ist vollständig vorbereitet und wartet nur auf einen laufenden Hytale-Server:

- **Hauptmenü** (`MainMenuPage`) – alle Funktionen per Klick erreichbar
- **Zeitungsleser** (`NewspaperPage`) – Doppelseiten-Overlay mit Navigation
- **Artikel-Editor** (`ArticleEditorPage`) – Kategorie per Button, Text per Chat
- **Ausgaben-Editor** (`IssueEditorPage`) – Schritt-für-Schritt mit Aktions-Buttons
- **`EditorUiBridge`** – koordiniert Öffnen/Aktualisieren aller Seiten

Der gesamte Code kompiliert, alle 300 Tests sind grün. Die Funktionalität kann aktiviert werden, sobald `openCustomPage()` auf einem echten Hytale-Server ausführbar ist.
