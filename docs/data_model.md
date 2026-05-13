# AthenaPress Datenmodell

Diese Datei beschreibt die aktuellen Datenstrukturen von AthenaPress.

AthenaPress ist derzeit ein lokales Backend-/Core-Konzept für eine spätere Zeitung auf dem Athena-Hytale-Server.

Aktuell gibt es noch keine direkte Hytale-API-Anbindung.

Der Fokus liegt auf:

- sauberen JSON-Daten
- stabiler Validierung
- Python-CLI-Werkzeugen
- Java-Core-Preview
- Java-Core-Statusübersicht
- späterer Vorbereitung einer echten Mod-/Serverintegration

---

## Grundprinzip

AthenaPress trennt Inhalte fachlich in mehrere Bereiche:

- Artikel
- Ausgaben
- Kategorien
- Bilder
- Abonnenten
- Zustellstatus
- Lesestatus

Artikel und Ausgaben sind getrennt.

Eine Ausgabe enthält nicht den vollständigen Artikeltext, sondern referenziert Artikel über IDs.

Dadurch können Artikel unabhängig erstellt, geprüft, veröffentlicht, archiviert und in Ausgaben verwendet werden.

---

## Projektverzeichnis

Die Daten liegen im Projekt unter:

```
AthenaPress/
```

Wichtige Unterordner:

```
AthenaPress/
├─ articles/
│  ├─ draft/
│  ├─ published/
│  └─ archived/
├─ issues/
│  ├─ draft/
│  ├─ published/
│  └─ archived/
├─ images/
│  ├─ uploaded/
│  ├─ thumbnails/
│  └─ placeholders/
├─ subscriptions/
│  └─ subscribers.json
├─ templates/
│  ├─ article_template.json
│  ├─ issue_template.json
│  └─ categories.json
└─ logs/
```

---

## Statusordner

Artikel und Ausgaben werden über ihren Speicherort getrennt.

Für Artikel:

```
AthenaPress/articles/draft/
AthenaPress/articles/published/
AthenaPress/articles/archived/
```

Für Ausgaben:

```
AthenaPress/issues/draft/
AthenaPress/issues/published/
AthenaPress/issues/archived/
```

Bedeutung:

- `draft`: Entwurf, darf noch verändert oder gelöscht werden
- `published`: veröffentlicht, soll nicht gelöscht werden
- `archived`: archiviert, bleibt als Verlauf erhalten

Wichtig:

Nur Entwürfe dürfen gelöscht werden.

Veröffentlichte oder archivierte Inhalte bleiben erhalten.

---

## Artikel

Artikel liegen als JSON-Dateien unter:

```
AthenaPress/articles/
```

Beispiel:

```
AthenaPress/articles/published/article_0002.json
```

Ein Artikel beschreibt einen einzelnen Zeitungsbeitrag.

Beispielstruktur:

```
{
  "id": "article_0002",
  "title": "Neue Baumfarm eröffnet",
  "subtitle": "Vier Plots gegen die Holzknappheit",
  "summary": "Eine neue Baumfarm auf Athena soll verschiedene Holzarten übersichtlich und platzsparend anbauen.",
  "categoryId": "build_projects",
  "author": {
    "name": "HF_jeti83"
  },
  "image": {
    "path": "placeholders/no_image.png",
    "sourceType": "placeholder",
    "caption": "Platzhalterbild"
  },
  "content": [
    "Der eigentliche Artikeltext steht hier."
  ],
  "status": "published"
}
```

---

## Wichtige Artikelfelder

### `id`

Eindeutige Artikel-ID.

Beispiel:

```
article_0002
```

### `title`

Titel des Artikels.

### `subtitle`

Untertitel des Artikels.

### `summary`

Optionale kurze Zusammenfassung.

Die Zusammenfassung wird für Vorschau- und Demo-Ausgaben verwendet.

Wenn `summary` vorhanden ist, kann die Java-Preview sie unter dem Artikel anzeigen.

### `categoryId`

Verweist auf eine Kategorie aus:

```
AthenaPress/templates/categories.json
```

Beispiel:

```
build_projects
```

### `author`

Informationen zum Autor.

Derzeit reicht mindestens ein Name.

Beispiel:

```
{
  "name": "HF_jeti83"
}
```

### `image`

Optionale Bildinformationen zum Artikel.

Siehe Abschnitt „Bilder“.

### `content`

Artikelinhalt.

Aktuell als Liste von Textabschnitten.

### `status`

Fachlicher Status des Artikels.

Typische Werte:

- `draft`
- `published`
- `archived`

---

## Ausgaben

Ausgaben liegen als JSON-Dateien unter:

```
AthenaPress/issues/
```

Beispiel:

```
AthenaPress/issues/published/issue_0002.json
```

Eine Ausgabe bündelt mehrere Artikel.

Beispielstruktur:

```
{
  "id": "issue_0002",
  "title": "Athena Botenblatt",
  "subtitle": "Die erste echte Testausgabe",
  "issueName": "Ausgabe 2",
  "status": "published",
  "cover": {
    "mainArticleId": "article_0001",
    "image": "placeholders/dating.png"
  },
  "articles": [
    "article_0001",
    "article_0002"
  ]
}
```

---

## Wichtige Ausgabenfelder

### `id`

Eindeutige Ausgaben-ID.

Beispiel:

```
issue_0002
```

### `title`

Titel der Zeitung oder Ausgabe.

Beispiel:

```
Athena Botenblatt
```

### `subtitle`

Untertitel der Ausgabe.

### `issueName`

Optionaler Name oder Anzeigename der Ausgabe.

### `status`

Fachlicher Status der Ausgabe.

Typische Werte:

- `draft`
- `published`
- `archived`

### `articles`

Liste der Artikel-IDs, die zu dieser Ausgabe gehören.

Beispiel:

```
[
  "article_0001",
  "article_0002"
]
```

Wichtig:

Eine Ausgabe ohne Artikel ist nicht sinnvoll und wird vom Java-Core als Validierungsproblem behandelt.

---

## Cover

Ausgaben können optional ein Cover enthalten.

Beispiel:

```
{
  "cover": {
    "mainArticleId": "article_0001",
    "image": "placeholders/dating.png"
  }
}
```

Das Cover ist optional.

Wenn ein Cover vorhanden ist, gelten folgende Regeln:

- `cover.mainArticleId` muss auf einen Artikel zeigen, der in `articles` der Ausgabe enthalten ist.
- `cover.image` muss auf eine vorhandene lokale Bilddatei zeigen.
- Der Bildpfad ist relativ zu `AthenaPress/images/`.

Beispiel:

```
placeholders/dating.png
```

entspricht:

```
AthenaPress/images/placeholders/dating.png
```

---

## Bilder

Bildinformationen können bei Artikeln oder Ausgaben verwendet werden.

Bildpfade werden relativ zu folgendem Ordner interpretiert:

```
AthenaPress/images/
```

Beispiel:

```
placeholders/no_image.png
```

entspricht:

```
AthenaPress/images/placeholders/no_image.png
```

---

## Bildquellen

Erlaubte Werte für `image.sourceType`:

- `placeholder`
- `uploaded`
- `screenshot`
- `external`
- `camera_marker`

Bedeutung:

### `placeholder`

Ein bewusst verwendetes Platzhalterbild.

Beispiel:

```
placeholders/no_image.png
```

### `uploaded`

Ein manuell hochgeladenes Bild.

### `screenshot`

Ein Screenshot, zum Beispiel aus Hytale oder einer späteren Serverumgebung.

### `external`

Ein externer Bildverweis.

Hinweis: Externe Bilder sollten später besonders vorsichtig behandelt werden, damit keine instabilen oder unerwünschten Quellen verwendet werden.

### `camera_marker`

Platzhalter für eine spätere mögliche Ingame-Kamera- oder Marker-Funktion.

Aktuell ist das nur ein Datenmodell-Konzept, keine echte Hytale-Funktion.

---

## Kategorien

Kategorien werden datengetrieben gepflegt unter:

```
AthenaPress/templates/categories.json
```

Artikel verweisen über `categoryId` auf diese Kategorien.

Beispiel:

```
"categoryId": "build_projects"
```

Der Java-Core kann Kategorien einlesen und validieren.

Aktuell bekannte Kategorien werden nicht fest im Java-Code verdrahtet, sondern aus der JSON-Datei gelesen.

Das macht spätere Anpassungen einfacher.

---

## Abonnenten

Abonnenten liegen unter:

```
AthenaPress/subscriptions/subscribers.json
```

Ein Abonnent beschreibt einen Spieler oder Empfänger der Zeitung.

Beispielstruktur:

```
{
  "name": "HF_jeti83",
  "active": true,
  "deliveryMode": "mailbox",
  "readIssues": [
    "issue_0002"
  ]
}
```

---

## Wichtige Abonnentenfelder

### `name`

Name des Abonnenten.

Beispiel:

```
HF_jeti83
```

### `active`

Gibt an, ob der Abonnent aktuell beliefert werden soll.

Nur aktive Abonnenten werden im Java-Core für den Zustellplan berücksichtigt.

### `deliveryMode`

Gewünschte Zustellart.

Erlaubte Zustellmodi:

- `notification_only`
- `item_only`
- `item_and_notification`
- `mailbox`

### `readIssues`

Liste bereits gelesener Ausgaben.

Beispiel:

```
[
  "issue_0002"
]
```

---

## Zustellung

Die Zustellung ist aktuell noch rein logisch.

Das bedeutet:

- Es wird noch kein echtes Hytale-Item erzeugt.
- Es wird noch keine echte Servernachricht verschickt.
- Es wird noch keine echte Mailbox im Spiel verwendet.

Der Java-Core kann aber bereits einen Zustellplan erzeugen.

Beispiel:

```
Jeti -> item_and_notification -> unread true
HF_jeti83 -> mailbox -> unread false
```

Bedeutung:

- Empfänger
- Zustellmodus
- Lesestatus für die Ausgabe

---

## Lesestatus

Der Lesestatus wird über die gelesenen Ausgaben eines Abonnenten abgeleitet.

Wenn eine Ausgabe in `readIssues` steht, gilt sie für diesen Abonnenten als gelesen.

Wenn sie dort nicht steht, gilt sie als ungelesen.

---

## Validierung im Java-Core

Der Java-Core prüft aktuell unter anderem:

- ob Ausgaben existieren
- ob Ausgaben Artikel enthalten
- ob referenzierte Artikel existieren
- ob Kategorien gültig sind
- ob Bild-Metadaten plausibel sind
- ob lokale Bilddateien vorhanden sind
- ob ein Cover-Hauptartikel Teil der Ausgabe ist
- ob ein Cover-Bild existiert

Die Validierung ist bewusst streng.

Sie soll Datenprobleme früh sichtbar machen, bevor später echte Serverlogik oder Ingame-Funktionen darauf aufbauen.

---

## Java-Core-Modelle

Wichtige Java-Modelle im aktuellen Stand:

```
Article
AuthorInfo
ImageInfo
LocationInfo
Issue
CoverInfo
ResolvedIssue
DeliveryTarget
Subscriber
```

Das aktuelle Cover-Modell ist:

```
CoverInfo
```

mit den Feldern:

```
String mainArticleId
String image
```

`Issue` nutzt aktuell:

```
CoverInfo cover
```

---

## Aktueller bekannter Datenbestand

Aktueller Beispiel-/Testbestand:

- `article_0001` ist veröffentlicht
- `article_0002` ist veröffentlicht
- `article_0003` ist archiviert
- `issue_0002` ist veröffentlicht
- `issue_0001` ist archiviert
- `Jeti` ist aktiver Abonnent
- `HF_jeti83` ist aktiver Abonnent
- `TestUser` ist inaktiver Test-Abonnent

---

## Aktuelle Grenze

AthenaPress ist aktuell noch keine echte Hytale-Mod.

Noch nicht umgesetzt:

- echte Hytale-API
- echte Ingame-Zustellung
- echte Ingame-Items
- echte Ingame-Mailbox
- Live-Kommunikation mit einem Hytale-Server
- automatische Veröffentlichung im Spiel

Diese Funktionen kommen erst später.

Der aktuelle Stand soll zuerst stabil, verständlich und gut validierbar bleiben.