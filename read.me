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

Aktuell ist AthenaPress noch keine direkte Hytale-API-Anbindung.

Der derzeitige Fokus liegt auf:

- sauberem Datenmodell
- stabiler Validierung
- CLI-Werkzeugen
- Java-Core als Vorschau-, Admin- und Debug-Werkzeug
- späterer Vorbereitung einer echten Mod-/Serverintegration

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

Noch nicht enthalten:

- echte Hytale-API
- echte Ingame-Zustellung
- echte Ingame-Items
- Live-Kommunikation mit einem Server

---

## Projektstruktur

```text
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
├─ tools/
├─ press.py
└─ java/
   └─ athena-press-core/
```

---

## Python-CLI

Das Python-CLI wird aus dem Projektwurzelverzeichnis ausgeführt.

Beispiele:

```powershell
python press.py pruefen
python press.py artikel liste
python press.py ausgabe liste
python press.py abonnent liste
python press.py ausgabe lesen issue_0002 --voll
```

Artikel erstellen:

```powershell
python press.py artikel erstellen --titel "Titel" --kategorie server_news --autor HF_jeti83
```

Artikel mit Zusammenfassung erstellen:

```powershell
python press.py artikel erstellen --titel "Titel" --kategorie server_news --autor HF_jeti83 --zusammenfassung "Kurze Vorschau."
```

Ausgabe zustellen:

```powershell
python press.py ausgabe zustellen issue_0002
```

Weitere Befehle stehen in:

```text
docs/commands.md
```

---

## Java-Core

Das Java-Modul liegt unter:

```text
java/athena-press-core
```

Der Java-Core ist aktuell ein lokales Werkzeug für:

- Vorschau
- Validierung
- Statusübersicht
- Debugging
- spätere Vorbereitung einer Mod-Anbindung

Er liest echte AthenaPress-JSON-Daten aus dem Projekt.

---

## Java-/Maven-Umgebung

Aktueller Zielstand:

- Java 25
- Maven 3.9.15
- JUnit 5
- Jackson
- Compiler-Release 25

Standard-Testbefehl im Maven-Modul:

```powershell
mvn -B clean verify
```

Letzter bekannter stabiler Teststand:

```text
70 runs
Failures: 0
Errors: 0
```

---

## Java-Demo-Befehle

Standard-Preview:

```powershell
mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo"
```

Konkrete Ausgabe anzeigen:

```powershell
mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=issue_0002"
```

Ausgaben auflisten:

```powershell
mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--list"
```

Deutsch:

```powershell
mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--liste"
```

Ausgabe validieren:

```powershell
mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--validate issue_0002"
```

Deutsch:

```powershell
mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--pruefen issue_0002"
```

Statusübersicht:

```powershell
mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--status"
```

Deutsch:

```powershell
mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--uebersicht"
```

Hilfe:

```powershell
mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--hilfe"
```

---

## Datenmodell

AthenaPress trennt Artikel und Ausgaben.

Artikel liegen unter:

```text
AthenaPress/articles/
```

Ausgaben liegen unter:

```text
AthenaPress/issues/
```

Ausgaben referenzieren Artikel über IDs.

Beispiel:

```json
{
  "articles": [
    "article_0001",
    "article_0002"
  ]
}
```

Kategorien werden datengetrieben gepflegt unter:

```text
AthenaPress/templates/categories.json
```

---

## Cover-Modell

Ausgaben können optional ein Cover enthalten.

Beispiel:

```json
{
  "cover": {
    "mainArticleId": "article_0001",
    "image": "placeholders/dating.png"
  }
}
```

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

```text
AthenaPress/images/
```

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

```text
docs/commands.md
docs/workflow.md
docs/data_model.md
```

`docs/commands.md` enthält die Befehlsübersicht.

`docs/workflow.md` beschreibt den empfohlenen Arbeitsablauf.

`docs/data_model.md` beschreibt die Datenstrukturen.

---

## Empfohlener Entwicklungsablauf

Zu Beginn:

```powershell
git status
git pull
```

Nach Java-Core- oder Datenmodelländerungen:

```powershell
mvn -B clean verify
```

Optionaler Demo-Check:

```powershell
mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--status"
```

Commit-Beispiel:

```powershell
g_sacp "Update AthenaPress README"
```

---

## Aktuelle Grenze

AthenaPress ist aktuell ein lokales Backend-/Core-Konzept.

Die eigentliche Hytale-Integration kommt später.

Bis dahin gilt:

Erst Datenmodell, Validierung, Preview und Dokumentation stabilisieren. Dann Konfetti. Dann Brandschutz. Dann vielleicht noch mehr Konfetti.