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