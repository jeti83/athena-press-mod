# AthenaPress Mod

AthenaPress ist ein Backend-/Mod-Konzept für eine Zeitung auf dem Athena-Hytale-Server.

Ziel ist ein datengetriebenes Zeitungssystem für Hytale-nahe Serverinhalte, zum Beispiel:

- Server-News
- Bauprojekte
- Spielerberichte
- Events
- humorvolle Ingame-Artikel
- Ausgaben mit mehreren Artikeln
- Zustellung an Abonnenten

AthenaPress ist ein laufendes Hytale-Plugin, das gegen die echte Hytale-Server-API kompiliert und ingame gestartet werden kann.

Der derzeitige Fokus liegt auf:

- sauberem Datenmodell
- stabiler Validierung
- CLI-Werkzeugen
- Java-Core als Vorschau-, Admin- und Debug-Werkzeug
- Java-Integration als Mod-/Visual-Schicht
- Ingame-GUI über die native Hytale-CustomUIPage-API

---



## Projektstatus

Aktueller Stand:

- Python-Backend funktionsfähig
- zentrales CLI über `press.py`
- deutsche und englische Befehlsaliase
- Artikelverwaltung
- Ausgabenverwaltung
- Abonnentenverwaltung
- Zustellstatus
- Lesestatus
- Archivierung
- Entwurfslöschung nur für Drafts
- Java-Core liest echte JSON-Daten
- Java-Core kann Ausgaben auflösen und anzeigen
- Java-Core validiert Daten
- Java-Core zeigt Statusübersicht
- Java-Core unterstützt deutsche und englische Demo-Befehle
- Java-Integration erzeugt spielnahe Textansichten
- Java-Integration erzeugt Visual-Doppelseitenstrukturen
- Visual-Preview-Pipeline und Runtime-Cache sind vorbereitet
- Visual-Input, Visual-Lifecycle und Hytale-Adapter-Schicht sind vorbereitet
- HytaleNewspaperVisualRuntime bündelt den nativen Visual-Pfad

Noch nicht enthalten:

- echte Ingame-Zustellung
- echte Ingame-Items
- Live-Kommunikation mit einem externen Server

Ingame-GUI aktiv (läuft auf dem Hytale-Singleplayer-Server):

- Hauptmenü via `/ap` (Buttons: Zeitung lesen, Kamera holen, Artikel schreiben, Ausgabe erstellen)
- Artikel-Editor als GUI-Overlay (Kategorie per Button, Text per Chat)
- Ausgaben-Editor als GUI-Overlay (Schritt-für-Schritt mit Buttons)

---

## Projektstruktur

text
athena-press-mod/
├─ AthenaPress/
│  ├─ config.json
│  ├─ articles/
│  │  ├─ draft/
│  │  ├─ published/
│  │  └─ archived/
│  ├─ issues/
│  │  ├─ draft/
│  │  ├─ published/
│  │  └─ archived/
│  ├─ images/
│  │  ├─ uploaded/
│  │  ├─ thumbnails/
│  │  └─ placeholders/
│  ├─ subscriptions/
│  │  └─ subscribers.json
│  ├─ players/
│  ├─ templates/
│  │  ├─ article_template.json
│  │  ├─ issue_template.json
│  │  └─ categories.json
│  └─ logs/
├─ docs/
├─ mods/
│  └─ HytaleAthena.AP_Camera/
│     └─ Common/UI/Custom/AthenaPress/   ← .ui-Layoutdateien
├─ tools/
├─ press.py
└─ java/
   ├─ athena-press-core/
   ├─ athena-press-integration/
   └─ athena-press-plugin/               ← Hytale-Plugin-Einstiegspunkt

---

## Python-CLI

Das Python-CLI wird aus dem Projektwurzelverzeichnis ausgeführt.

Beispiele:

powershell
python press.py pruefen
python press.py artikel liste
python press.py ausgabe liste
python press.py abonnent liste
python press.py ausgabe lesen issue_0002 --voll

Artikel erstellen:

powershell
python press.py artikel erstellen --titel "Titel" --kategorie server_news --autor HF_jeti83

Artikel mit Zusammenfassung erstellen:

powershell
python press.py artikel erstellen --titel "Titel" --kategorie server_news --autor HF_jeti83 --zusammenfassung "Kurze Vorschau."

Ausgabe zustellen:

powershell
python press.py ausgabe zustellen issue_0002

Weitere Befehle stehen in:

text
docs/commands.md

---

## Java-Core und Integration

Die Java-Module liegen unter:

text
java/

Der Java-Core ist aktuell ein lokales Werkzeug für:

- Vorschau
- Validierung
- Statusübersicht
- Debugging
- spätere Vorbereitung einer Mod-Anbindung

Er liest echte AthenaPress-JSON-Daten aus dem Projekt.

Das Integration-Modul ergänzt:

- spielnahe Zeitungssessions
- UI-nahe View-Modelle
- Visual-Doppelseiten und Layoutblöcke
- Visual-Preview-Pipeline
- native Hytale-Adapter-Vorbereitung
- Visual-Runtime-Fassade für spätere Hytale-Hooks

Das Plugin-Modul (`athena-press-plugin`) kompiliert gegen die echte `HytaleServer.jar` und läuft als natives Hytale-Plugin.
---

## Java-/Maven-Umgebung

Aktueller Zielstand:

- Java 25
- Maven 3.9.15
- JUnit 5
- Jackson
- Compiler-Release 25

Standard-Testbefehl im Maven-Modul:

powershell
mvn -B clean verify

Letzter bekannter stabiler Teststand:

text
Core: 103 Tests
Integration: 197 Tests
Gesamt: 300 Tests
Failures: 0
Errors: 0

---

## Java-Demo-Befehle

Alle Demo-Befehle aus dem `java/`-Verzeichnis ausführen. `-pl athena-press-core` ist zwingend nötig, damit Maven das richtige Modul wählt.

Standard-Preview:

powershell
cd java
mvn -q exec:java -pl athena-press-core "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo"

Konkrete Ausgabe anzeigen:

powershell
mvn -q exec:java -pl athena-press-core "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=issue_0002"

Ausgaben auflisten:

powershell
mvn -q exec:java -pl athena-press-core "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--list"
bzw.
powershell
mvn -q exec:java -pl athena-press-core "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--liste"

Artikel auflisten:

powershell
mvn -q exec:java -pl athena-press-core "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--articles"
bzw.
mvn -q exec:java -pl athena-press-core "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--artikel"

Ausgabe validieren:

powershell
mvn -q exec:java -pl athena-press-core "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--validate issue_0002"
bzw.
powershell
mvn -q exec:java -pl athena-press-core "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--pruefen issue_0002"

Statusübersicht:

powershell
mvn -q exec:java -pl athena-press-core "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--status"

bzw.
powershell
mvn -q exec:java -pl athena-press-core "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--uebersicht"

Hilfe:

powershell
mvn -q exec:java -pl athena-press-core "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--hilfe"

---

## Java-Integration Visual-Preview

Das Integration-Modul kann echte veröffentlichte Ausgaben als Doppelseiten-/Blockstruktur ausgeben.

Alle Befehle aus `java/` mit `-pl athena-press-integration -am` (damit athena-press-core als Abhängigkeit gebaut wird):

powershell
cd java
mvn -q exec:java -pl athena-press-integration -am "-Dexec.mainClass=pro.jeti.athenapress.integration.AthenaPressVisualPreviewDemo" "-Dexec.args=--visual-preview issue_0002"

Deutscher Alias:

powershell
mvn -q exec:java -pl athena-press-integration -am "-Dexec.mainClass=pro.jeti.athenapress.integration.AthenaPressVisualPreviewDemo" "-Dexec.args=--vorschau issue_0002"

Diese Ausgabe ist nur ein Debug-/Admin-Einstieg für die native Visual-Struktur, keine HTML- oder Browserlösung.

Bildvorschau als echte PNG-Doppelseiten:

powershell
cd java
mvn exec:java -pl athena-press-integration -am "-Dexec.mainClass=pro.jeti.athenapress.integration.AthenaPressVisualPngPreviewDemo" "-Dexec.args=issue_0003"

Die PNG-Dateien werden standardmäßig unter folgendem Ordner erzeugt:

text
java/target/visual-preview-png/

Ausgabe in einen eigenen Ordner:

powershell
mvn exec:java -pl athena-press-integration "-Dexec.mainClass=pro.jeti.athenapress.integration.AthenaPressVisualPngPreviewDemo" "-Dexec.args=issue_0003 C:\Users\DEINNAME\Downloads\athena-press-preview"

Ergebnis: Eine vollständige Zeitungsvorschau als Doppelseiten-PNGs mit Titelseite, Artikelseiten und Rückseite.

Auch diese Vorschau bleibt adapter-neutral. Sie rendert dieselbe native Visual-Struktur lediglich als lokale Bilddatei, damit Layoutentscheidungen früh sichtbar geprüft werden können.

---

## Native Hytale-Visual-Runtime

Der aktuelle Integrationsstand enthält eine Runtime-Fassade:

text
HytaleNewspaperVisualRuntime<TPlayer>

Sie bündelt:

- HytaleNewspaperVisualUiPort
- PlayerNewspaperVisualUiController
- HytaleNewspaperVisualInputAdapter
- HytaleNewspaperLifecycleAdapter
- PlayerNewspaperLifecycleHandler

Spätere echte Hytale-Hooks können dadurch gezielt an Adapter-Methoden angebunden werden, ohne die AthenaPress-Kernlogik umzubauen.

---

## Datenmodell

AthenaPress trennt Artikel und Ausgaben.

Artikel liegen unter:

text
AthenaPress/articles/

Ausgaben liegen unter:

text
AthenaPress/issues/

Ausgaben referenzieren Artikel über IDs.

Beispiel:

json
{
  "articles": [
    "article_0001",
    "article_0002"
  ]
}

Kategorien werden datengetrieben gepflegt unter:

text
AthenaPress/templates/categories.json

---

## Cover-Modell

Ausgaben können optional ein Cover enthalten.

Beispiel:

json
{
  "cover": {
    "mainArticleId": "article_0001",
    "image": "placeholders/dating.png"
  }
}

Regeln:

- `mainArticleId` muss auf einen Artikel der Ausgabe zeigen.
- `image` ist relativ zu `AthenaPress/images/`.
- Die Datei muss lokal vorhanden sein.

---

## Bildquellen

Erlaubte Werte für `image.sourceType`:

- `placeholder`
- `uploaded`
- `screenshot`
- `external`
- `camera_marker`

Bildpfade werden relativ zu folgendem Verzeichnis interpretiert:

text
AthenaPress/images/

---

## Zustellmodi

Aktuell bekannte Zustellmodi:

- `notification_only`
- `item_only`
- `item_and_notification`
- `mailbox`

---

## Dokumentation

Wichtige Dokumente:

docs/commands.md
docs/workflow.md
docs/data_model.md
docs/integration_architecture.md
docs/mod_mvp.md
docs/backend_status.md

`docs/commands.md` enthält die Befehlsübersicht.

`docs/workflow.md` beschreibt den empfohlenen Arbeitsablauf.

`docs/data_model.md` beschreibt die Datenstrukturen.

---

## Architektur-Dokumentation

Weitere technische Details:

- docs/backend_status.md
- docs/mod_mvp.md
- docs/integration_architecture.md

---

## Empfohlener Entwicklungsablauf

Zu Beginn:

powershell
git status
git pull

Nach Java- oder Datenmodelländerungen:

powershell
mvn -B clean verify

Vor größeren PRs zusätzlich:

powershell
mvn -B clean install

Optionaler Demo-Check:

powershell
mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--status"

Optionaler Artikellisten-Check:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--articles"

Commit-Beispiel:

powershell
g_sacp "Update AthenaPress README"

---

## Aktueller Stand

Das Hytale-Plugin läuft. `/ap` öffnet das Hauptmenü, Artikel- und Ausgaben-Editor sind ingame bedienbar.

Nächste Schritte: echte Ingame-Zustellung, Ingame-Items, Mehrspielerbetrieb.

