# AthenaPress Datenmodell

Stand: AthenaPress Backend v0.3

AthenaPress verwaltet Artikel, Zeitungsausgaben, Bilder, Kategorien und Abonnenten über JSON-Dateien. Die Struktur ist bewusst einfach gehalten, damit sie später in ein Hytale-Java-Plugin übernommen werden kann.

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
│
├─ tools/
│  ├─ presslib.py
│  ├─ create_article.py
│  ├─ edit_article.py
│  ├─ create_issue.py
│  ├─ publish_issue.py
│  ├─ deliver_issue.py
│  ├─ read_issue.py
│  ├─ list_articles.py
│  ├─ list_issues.py
│  ├─ subscribe_player.py
│  ├─ unsubscribe_player.py
│  ├─ list_subscribers.py
│  ├─ mark_issue_read.py
│  ├─ archive_article.py
│  ├─ archive_issue.py
│  ├─ delete_draft.py
│  └─ validate_press.py
│
└─ docs/
   ├─ data_model.md
   ├─ commands.md
   └─ workflow.md

   Artikel

Artikel liegen unter:

AthenaPress/articles/draft/
AthenaPress/articles/published/
AthenaPress/articles/archived/

Beispiel:

{
  "id": "article_0002",
  "status": "published",

  "categoryId": "build_projects",

  "title": "Neue Baumfarm eröffnet",
  "subtitle": "Vier Plots gegen die Holzknappheit",
  "teaser": "Auf Athena entsteht eine neue Baumfarm für verschiedene Holzarten.",
  "summary": "Die neue Baumfarm soll verschiedene Holzarten gebündelt verfügbar machen und die Versorgung auf Athena verbessern.",

  "author": {
    "playerName": "HF_jeti83",
    "playerUuid": "unknown"
  },

  "body": "Auf Athena entsteht eine neue Baumfarm, die verschiedene Holzarten übersichtlich und platzsparend anbauen soll.",

  "image": {
    "file": "placeholders/no_image.png",
    "caption": "Hier könnte ihr Bild von der neuen Baumfarm stehen, um die Vielfalt der Sätzlinge zu zeigen.",
    "credit": "Foto: HF_jeti83",
    "sourceType": "placeholder"
  },

  "location": {
    "enabled": false,
    "world": "",
    "x": 0,
    "y": 0,
    "z": 0
  },

  "tags": ["baumfarm", "holz", "bauprojekt"],

  "createdAt": "2026-05-10T13:55:00+02:00",
  "updatedAt": "2026-05-10T14:38:00+02:00",
  "publishedAt": "2026-05-10T14:38:00+02:00"
}

Optionale Artikelfelder:

| Feld | Bedeutung |
| ---- | --------- |
| `summary` | Kurze Zusammenfassung des Artikels für Vorschauen, Übersichten und Java-Demo-Ausgaben. Das Feld ist optional und darf leer sein. |

Artikelstatus

Erlaubte Werte:

draft
published
archived

Bedeutung:

Status	Bedeutung
draft	Artikel ist ein Entwurf.
published	Artikel wurde veröffentlicht.
archived	Artikel wurde archiviert.

Archivierte Artikel können weiterhin von alten Ausgaben referenziert werden.

Artikelbilder

Bilder werden relativ zu diesem Ordner angegeben:

AthenaPress/images/

Beispiel:

"image": {
  "file": "uploaded/baumfarm_001.png",
  "caption": "Die neue Baumfarm im Aufbau.",
  "credit": "Foto: HF_jeti83",
  "sourceType": "uploaded"
}

Erlaubte sourceType-Werte:

placeholder
uploaded
screenshot
external
camera_marker

Bedeutung:

sourceType	Bedeutung
placeholder	Platzhalterbild
uploaded	manuell hochgeladenes Bild
screenshot	Screenshot aus dem Spiel
external	extern referenziertes Bild
camera_marker	späterer Platzhalter für virtuelle Kamera-/Positionsdaten
Ausgaben

Ausgaben liegen unter:

AthenaPress/issues/draft/
AthenaPress/issues/published/
AthenaPress/issues/archived/

Beispiel:

{
  "id": "issue_0002",
  "status": "published",

  "issueNumber": 2,
  "title": "Athena Botenblatt",
  "subtitle": "Die erste echte Testausgabe",
  "editionName": "Farmwelt Spezial",

  "cover": {
    "mainArticleId": "article_0001",
    "image": "placeholders/dating.png"
  },

  "articles": [
    "article_0001",
    "article_0002"
  ],

  "createdAt": "2026-05-10T14:30:00+02:00",
  "updatedAt": "2026-05-10T14:56:00+02:00",
  "publishedAt": "2026-05-10T14:38:00+02:00",

  "deliveredToSubscribers": true,
  "lastDeliveredAt": "2026-05-10T14:56:00+02:00"
}

Eine Ausgabe enthält keine Artikel direkt, sondern verweist über Artikel-IDs auf Artikeldateien.

Ausgabenstatus

Erlaubte Werte:

draft
published
archived

Bedeutung:

Status	Bedeutung
draft	Ausgabe ist ein Entwurf.
published	Ausgabe wurde veröffentlicht.
archived	Ausgabe wurde archiviert.
Kategorien

Kategorien liegen in:

AthenaPress/templates/categories.json

Beispiel:

{
  "id": "dating",
  "name": "Herzblatt der Farmwelt",
  "description": "Builder sucht Builderin, eventuell zwecks gemeinsamer NPC-Planung",
  "defaultImage": "placeholders/dating.png",
  "enabled": true
}

Aktuelle Kategorien:

headline
server_news
build_projects
economy
classifieds
dating

Kategorien sind bewusst datengetrieben. Neue Kategorien sollen in categories.json ergänzt werden, nicht fest im Code.

Abonnenten

Abonnenten liegen in:

AthenaPress/subscriptions/subscribers.json

Beispiel:

{
  "subscribers": [
    {
      "playerName": "HF_jeti83",
      "playerUuid": "unknown",
      "subscribed": true,
      "deliveryMode": "mailbox",
      "subscribedAt": "2026-05-10T14:53:00+02:00",
      "updatedAt": "2026-05-10T15:21:00+02:00",
      "lastReceivedIssueId": "issue_0002",
      "lastDeliveryMode": "mailbox",
      "lastDeliveredAt": "2026-05-10T14:56:00+02:00",
      "lastReadIssueId": "issue_0002",
      "lastReadAt": "2026-05-10T15:21:00+02:00",
      "unreadIssues": []
    }
  ]
}
Zustellmodi

Erlaubte Werte:

notification_only
item_only
item_and_notification
mailbox

Bedeutung:

deliveryMode	Bedeutung
notification_only	Nur Benachrichtigung
item_only	Nur Zeitungsitem
item_and_notification	Item und Benachrichtigung
mailbox	Zustellung an Briefkasten/Mailbox
Zeitstempel

Alle Zeitstempel sollen im ISO-8601-Format mit lokaler Zeitzone gespeichert werden:

2026-05-10T14:38:22+02:00

Bedeutung wichtiger Felder:

Feld	Bedeutung
createdAt	Zeitpunkt der Erstellung
updatedAt	Zeitpunkt der letzten Änderung
publishedAt	Zeitpunkt der Veröffentlichung
archivedAt	Zeitpunkt der Archivierung
subscribedAt	Zeitpunkt des ersten Abos
unsubscribedAt	Zeitpunkt der Deaktivierung
lastDeliveredAt	Zeitpunkt der letzten Zustellung
lastReadAt	Zeitpunkt der letzten Lesemarkierung
Validierung

Das gesamte System wird geprüft mit:

python tools\validate_press.py

Der Validator prüft unter anderem:

- gültige JSON-Dateien
- vorhandene Pflichtfelder
- gültige Kategorien
- gültige Statuswerte
- gültige Bildquellen
- vorhandene Bilddateien
- Ausgaben verweisen nur auf existierende Artikel
- Titelartikel existiert
- Abonnentenstruktur ist korrekt
- ungelesene Ausgaben existieren
- lastReceivedIssueId und lastReadIssueId existieren

---
