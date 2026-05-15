# AthenaPress Backend-Status

Stand: AthenaPress Backend/Integration v0.4

Dieses Dokument beschreibt den aktuellen Abschlussstand des AthenaPress-Backends.

Ziel ist nicht, das Backend als eigenständiges Endprodukt weiter auszubauen, sondern es als stabiles Fundament für die spätere Mod-/Ingame-Schicht zu nutzen.

---

## Aktueller Fokus

AthenaPress soll am Ende ein Zeitungssystem für den Athena-Hytale-Server werden.

Das Backend dient aktuell dazu, echte Zeitungsdaten strukturiert zu speichern, zu prüfen und für spätere Ingame-Anzeigen vorzubereiten.

Der aktuelle Entwicklungsfokus liegt auf der spielnahen Mod-/Preview-Schicht und einer adapter-neutralen Vorbereitung für native Hytale-UI.

---

## Aktuell stabil genug

Das Backend kann derzeit:

- Artikel als JSON-Daten verwalten
- Ausgaben als JSON-Daten verwalten
- Ausgaben mit mehreren Artikeln verbinden
- Artikel über IDs in Ausgaben referenzieren
- Kategorien datengetrieben nutzen
- Abonnenten verwalten
- Zustell- und Lesestatus abbilden
- veröffentlichte, archivierte und Entwurfsdaten unterscheiden
- Entwürfe gezielt löschen
- veröffentlichte und archivierte Inhalte erhalten
- Bild-Metadaten speichern und prüfen
- Cover-Daten für Ausgaben speichern und prüfen
- Daten über Python-Werkzeuge bearbeiten
- Daten über den Java-Core lesen und anzeigen
- Daten über das Java-Integration-Modul spielnah darstellen
- Visual-Doppelseiten und Blocklayouts vorbereiten
- lesbare Artikelgruppen beim Seitenumbruch geschlossen halten
- lange Artikeltexte absatzorientiert in Seitenfluss-Blöcke zerlegen
- fortlaufende Artikel auf Folgeseiten kenntlich machen
- Visual-Previews zwischenspeichern
- Visual-Input und Lifecycle-Ereignisse adapter-neutral verarbeiten
- eine native Hytale-Visual-Runtime ohne direkte Hytale-API-Imports zusammensetzen
- Validierungsfehler früh sichtbar machen
- Status-, Ausgaben- und Artikellisten anzeigen
- Demo-Befehle in deutscher und englischer Form nutzen

---

## Python-Backend

Das Python-Backend ist weiterhin der praktische Werkzeugkasten für Dateiverwaltung und Redaktionsabläufe.

Wichtige Einstiegspunkte:

    python press.py pruefen
    python press.py artikel liste
    python press.py ausgabe liste
    python press.py abonnent liste
    python press.py ausgabe lesen issue_0002 --voll

Das zentrale CLI `press.py` bleibt der bevorzugte Einstieg.

Die Einzeltools unter `tools/` bleiben als interne Bausteine erhalten.

---

## Java-Core

Der Java-Core unter `java/athena-press-core` ist aktuell ein lokales Admin-, Preview- und Debug-Werkzeug.

Er kann echte AthenaPress-Daten lesen und daraus Konsolenausgaben erzeugen.

Wichtige Demo-Befehle:

    mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo"

    mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--status"

    mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--articles"

    mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--validate issue_0002"

Der Java-Core ist noch keine echte Hytale-Mod.

Er bildet aber eine stabile Grundlage, um später eine spielnahe Anzeige- oder Server-Schicht daran anzuschließen.

---

## Java-Integration

Das Integration-Modul unter `java/athena-press-integration` bereitet die spätere Mod-Schicht vor.

Aktuell vorhanden:

- spielnahe Player-Zeitungssessions
- Text-UI-Port und View-Modelle
- Visual-UI-Port und View-Modelle
- Visual-Layout-, Pagination- und Doppelseiten-Komposition
- Preview-Pipeline für echte veröffentlichte Ausgaben
- Visual-Runtime-Cache
- Visual-Input-Pfad für Öffnen, Blättern, Aktualisieren und Schließen
- Lifecycle-Cleanup für Disconnect und Timeout
- Hytale-Adapter für Input, Lifecycle und Visual-UI
- HytaleNewspaperVisualRuntime als Composition-Root

Die Integration importiert weiterhin keine echte oder erfundene Hytale-API.

---

## Tests und Stabilität

Der aktuelle bekannte stabile Teststand:

    mvn -B clean verify

Erwarteter Stand:

- alle Tests grün
- keine Failures
- keine Errors

Der zuletzt bekannte Stand lag bei:

- Core: 91 Tests
- Integration: 139 Tests
- 0 Failures
- 0 Errors

Bei Änderungen am Java-Code oder am Datenmodell sollte dieser Test erneut ausgeführt werden.

Für größere Änderungspakete wird zusätzlich empfohlen:

    mvn -B clean install

---

## Bewusst geparkt

Folgende Dinge werden vorerst nicht weiter ausgebaut, solange sie nicht konkret für die Mod-/Ingame-Schicht benötigt werden:

- weitere Konsolen-Aliase
- zusätzliche Komfortbefehle ohne direkten Mod-Nutzen
- reine Kosmetik an Konsolenausgaben
- übermäßige Doku-Politur
- neue Backend-Funktionen ohne klaren Spielbezug
- echte Hytale-API-Anbindung
- echte Ingame-Zustellung
- echte Ingame-Items
- Redaktions-UI
- automatische Veröffentlichung im Spiel

Diese Punkte können später ergänzt werden, wenn sie für ein konkretes Spiel- oder Admin-Szenario gebraucht werden.

---

## Nächster Entwicklungsfokus

Der nächste sinnvolle Fokus ist ein Mod-MVP.

MVP bedeutet hier:

Ein Spieler soll eine vorhandene AthenaPress-Ausgabe spielnah öffnen und Artikel lesen können.

Minimaler Zielablauf:

1. Eine veröffentlichte Ausgabe wird geladen.
2. Die referenzierten Artikel werden geladen.
3. Eine spielnahe Ansicht zeigt Titel, Untertitel und Artikelliste.
4. Ein Artikel kann ausgewählt werden.
5. Der Artikeltext wird angezeigt.

Dieser Ablauf ist auf Java-/Integrations-Ebene vorbereitet.

Der nächste sinnvolle Fokus nach dem aktuellen Visual-Ausbau ist:

1. PR #2 sauber nach `main` übernehmen.
2. Danach echte Hytale-Hook-Punkte gegen die vorhandenen Adapter prüfen, sobald verlässliche Hytale-API-Details lokal vorliegen.
3. Vorher keine direkten Hytale-API-Imports erfinden.

---

## Leitentscheidung

Das Backend gilt ab diesem Stand als ausreichend stabil für den nächsten Entwicklungsschritt.

Neue Backend-Funktionen sollen nur noch ergänzt werden, wenn sie direkt helfen, das spätere Ingame-Zeitungserlebnis umzusetzen.

Kurzfassung:

Erst Zeitung lesbar machen.
Dann Zustellung.
Dann Ingame-Komfort.
Dann Konfetti.
