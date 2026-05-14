# AthenaPress Workflow

Stand: AthenaPress Backend v0.3

Dieses Dokument beschreibt den typischen Ablauf einer AthenaPress-Ausgabe.

## Grundablauf

Artikel erstellen  
→ Artikel bearbeiten  
→ Ausgabe erstellen  
→ Ausgabe prüfen  
→ Ausgabe veröffentlichen  
→ Ausgabe zustellen  
→ Spieler liest Ausgabe  
→ Ausgabe wird als gelesen markiert

---

## 1. Artikel erstellen

    python tools\create_article.py --title "Neue Baumfarm eröffnet" --category build_projects --author HF_jeti83

Der Artikel landet als Entwurf unter:

    AthenaPress/articles/draft/

---

## 2. Artikel bearbeiten

Untertitel setzen:

    python tools\edit_article.py article_XXXX --subtitle "Vier Plots gegen die Holzknappheit"

Teaser setzen:

    python tools\edit_article.py article_XXXX --teaser "Auf Athena entsteht eine neue Baumfarm für verschiedene Holzarten."

Artikeltext setzen:

    python tools\edit_article.py article_XXXX --body "Hier steht der eigentliche Artikeltext."

Bilddaten setzen:

    python tools\edit_article.py article_XXXX --image uploaded/baumfarm_001.png --source-type uploaded --caption "Die Baumfarm im Aufbau." --credit "Foto: HF_jeti83"

Tags setzen:

    python tools\edit_article.py article_XXXX --tags baumfarm holz bauprojekt

---

## 3. Artikel prüfen

    python tools\validate_press.py
    python tools\list_articles.py

Vor der Ausgabe sollte geprüft werden:

- Titel vorhanden
- Kategorie korrekt
- Autor korrekt
- Text vorhanden
- Bildpfad korrekt oder bewusst Platzhalter

---

## 4. Ausgabe erstellen

    python tools\create_issue.py --subtitle "Farmwelt Spezial" --edition "Baumfarm-Ausgabe" --articles article_XXXX

Mit mehreren Artikeln:

    python tools\create_issue.py --subtitle "Neue Geschichten aus der Farmwelt" --edition "Wochenblatt" --articles article_0001 article_0002 article_XXXX

---

## 5. Ausgabe prüfen

    python tools\list_issues.py
    python tools\read_issue.py issue_XXXX --full
    python tools\validate_press.py

Vor dem Veröffentlichen prüfen:

- richtige Artikel enthalten
- Titelartikel korrekt
- Ausgabe noch draft
- Artikeltexte vollständig

---

## 6. Ausgabe veröffentlichen

Erst Testlauf:

    python tools\publish_issue.py issue_XXXX --dry-run

Dann veröffentlichen:

    python tools\publish_issue.py issue_XXXX

Dabei werden die Ausgabe und enthaltene Entwurfsartikel nach `published/` verschoben.

---

## 7. Abonnenten verwalten

Abonnent hinzufügen:

    python tools\subscribe_player.py --name HF_jeti83 --delivery-mode mailbox

Abonnent deaktivieren:

    python tools\unsubscribe_player.py --name HF_jeti83

Übersicht:

    python tools\list_subscribers.py

---

## 8. Ausgabe zustellen

Erst Testlauf:

    python tools\deliver_issue.py issue_XXXX --dry-run

Dann zustellen:

    python tools\deliver_issue.py issue_XXXX

Die Ausgabe wird bei aktiven Abonnenten in `unreadIssues` eingetragen.

---

## 9. Ausgabe lesen

    python tools\read_issue.py issue_XXXX --full

---

## 10. Ausgabe als gelesen markieren

    python tools\mark_issue_read.py --name HF_jeti83 --issue issue_XXXX

Dadurch wird die Ausgabe aus `unreadIssues` entfernt.

---

## 11. Archivieren

Alte Ausgabe archivieren:

    python tools\archive_issue.py issue_XXXX --reason "Alter Testentwurf"

Alten Artikel archivieren:

    python tools\archive_article.py article_XXXX --reason "Testartikel"

Archivierte Inhalte werden nicht gelöscht.

---

## 12. Entwürfe löschen

Nur Entwürfe können gelöscht werden:

    python tools\delete_draft.py article article_XXXX --yes
    python tools\delete_draft.py issue issue_XXXX --yes

---

## Standardablauf für eine echte Ausgabe

    python tools\create_article.py --title "Titel" --category server_news --author HF_jeti83
    python tools\edit_article.py article_XXXX --subtitle "Untertitel"
    python tools\edit_article.py article_XXXX --teaser "Kurzer Teaser"
    python tools\edit_article.py article_XXXX --body "Artikeltext"
    python tools\validate_press.py
    python tools\create_issue.py --subtitle "Untertitel der Ausgabe" --edition "Ausgabenname" --articles article_XXXX
    python tools\read_issue.py issue_XXXX --full
    python tools\publish_issue.py issue_XXXX --dry-run
    python tools\publish_issue.py issue_XXXX
    python tools\deliver_issue.py issue_XXXX --dry-run
    python tools\deliver_issue.py issue_XXXX
    python tools\validate_press.py

---

## Spätere Hytale-Entsprechung

| Python-Tool | Möglicher Hytale-Befehl |
|---|---|
| subscribe_player.py | /press subscribe |
| unsubscribe_player.py | /press unsubscribe |
| list_issues.py | /press list |
| read_issue.py | /press read &lt;issue&gt; |
| publish_issue.py | /press publish &lt;issue&gt; |
| mark_issue_read.py | automatisch nach Lesen |
| create_article.py | Redaktions-UI |
| edit_article.py | Redaktions-UI |

---

## Native Visual-Runtime für Hytale

Der aktuelle Java-Integrationsstand bereitet eine native Zeitungs-UI vor,
ohne echte Hytale-API-Klassen zu importieren.

Zentraler Einstiegspunkt:

    HytaleNewspaperVisualRuntime<TPlayer>

Diese Runtime bündelt:

- Visual-UI-Port
- Visual-UI-Controller
- Visual-Input-Adapter
- Lifecycle-Adapter
- Player-Kontext-Registrierung

Spätere Hytale-Hooks sollen ungefähr so angebunden werden:

| Hytale-Hook | AthenaPress-Ziel |
|---|---|
| Player Join | lifecycleAdapter().onPlayerConnected(player) |
| Player Leave | lifecycleAdapter().onPlayerDisconnected(player) |
| Session Timeout | lifecycleAdapter().onSessionTimeout(player) |
| Server Shutdown | lifecycleAdapter().onServerShutdown() |
| UI Button | visualInputAdapter().onUiButton(context, command, value) |
| Keybind | visualInputAdapter().onKeyBind(context, command, value) |
| /ap | visualInputAdapter().onChatCommand(context, "/ap", issueId) |

Wichtig:
Der Visual-Pfad bleibt getrennt vom Text-/Debug-Pfad.
Die Runtime ist nur Verdrahtung, keine zusätzliche Spiellogik.

---

## Java-Core-Demo und Prüfworkflow

Der Java-Core unter `java/athena-press-core` dient aktuell als Admin-, Debug- und Preview-Werkzeug für echte AthenaPress-Daten.

Er ist noch keine direkte Hytale-API-Anbindung.

Ziel des Java-Cores ist derzeit:

- echte JSON-Daten aus dem AthenaPress-Projekt zu lesen
- Artikel, Ausgaben, Kategorien und Abonnenten fachlich aufzulösen
- Ausgaben konsolenbasiert vorzuschauen
- Datenprobleme früh sichtbar zu machen
- eine spätere Mod-/Server-Anbindung stabil vorzubereiten

---

### Standardprüfung nach Änderungen

Nach Änderungen am Java-Core oder an relevanten AthenaPress-Daten sollte im Maven-Modul geprüft werden:

    mvn -B clean verify

Der bekannte stabile Stand liegt aktuell bei:

- `70 runs`
- `Failures: 0`
- `Errors: 0`

Wenn dieser Testlauf fehlschlägt, sollte vor weiteren Änderungen zuerst die Ursache geklärt werden.

---

### Manuelle Java-Demo-Prüfung

Nach einem erfolgreichen Maven-Test kann zusätzlich die Java-Demo manuell geprüft werden.

Preview einer konkreten Ausgabe:

    mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=issue_0002"

Statusübersicht:

    mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--status"

Deutschsprachige Statusübersicht:

    mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--uebersicht"

Artikelliste:

    mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--articles"

Deutschsprachige Artikelliste:

    mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--artikel"

Validierung einer Ausgabe:

    mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--validate issue_0002"

Deutschsprachige Validierung:

    mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--pruefen issue_0002"

Hilfeseite:

    mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--hilfe"

---

### Erwartete Java-Demo-Funktionen

Die Java-Demo kann aktuell:

- eine Ausgabe per ID anzeigen
- alle bekannten Ausgaben auflisten
- alle bekannten Artikel auflisten
- eine Ausgabe validieren
- eine kompakte Statusübersicht anzeigen
- eine Hilfeseite ausgeben
- deutsche und englische Befehlsaliase verstehen

Unterstützte Argumente:

- `--help`
- `--hilfe`
- `-h`
- `/?`
- `--list`
- `--liste`
- `--articles`
- `--artikel`
- `--validate`
- `--pruefen`
- `--status`
- `--uebersicht`
- `<issueId>`

---

### Validierungsziel

Die Validierung soll verhindern, dass fehlerhafte oder unvollständige Zeitungsdaten später unbemerkt in eine Ausgabe oder Serverintegration gelangen.

Sie prüft aktuell unter anderem:

- ob Ausgaben existieren
- ob Ausgaben Artikel enthalten
- ob referenzierte Artikel existieren
- ob Kategorien gültig sind
- ob Bild-Metadaten plausibel sind
- ob lokale Bilddateien vorhanden sind
- ob ein Cover-Hauptartikel Teil der Ausgabe ist
- ob ein Cover-Bild existiert

Die Validierung ist bewusst strenger als die aktuelle Demo technisch unbedingt bräuchte.

Grund: Fehler sollen früh auffallen, bevor später echte Serverlogik, Ingame-Anzeige oder Zustellung darauf aufbauen.

---

### Empfohlener Ablauf vor einem Commit

1. Änderungen speichern.
2. Status prüfen:

    git status

3. Maven-Test ausführen:

    mvn -B clean verify

4. Bei Bedarf Demo prüfen:

    mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--status"

5. Optional Artikelliste prüfen:

    mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--articles"

6. Änderungen committen:

    g_sacp "Update AthenaPress workflow documentation"

---

### Aktuelle Grenze

Der Java-Core bleibt vorerst ein lokales Werkzeug.

Noch nicht Teil dieses Workflows:

- echte Hytale-API
- echte Ingame-Items
- echte Zustellung auf dem Server
- Live-Kommunikation mit einem Hytale-Server
- automatische Veröffentlichung im Spiel

Diese Schritte kommen erst später, wenn Datenmodell, Validierung, Preview und Dokumentation zuverlässig stehen.
