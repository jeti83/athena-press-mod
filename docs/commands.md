# AthenaPress Befehlsübersicht
Stand: AthenaPress Backend v0.3 mit Java-Core-Demo
Alle Befehle werden im Projektordner ausgeführt:

cd C:\Users\jeti8\Projekte\Hytale-Mods\athena-press-mod
 
---
## 1. Zentrales Python-CLI: `press.py`
Neben den einzelnen Tools im Ordner `tools/` gibt es ein zentrales Kommandozeilenwerkzeug:

python press.py ...
 
`press.py` ist ein Router für die bestehenden Tools.
Die eigentliche Logik bleibt in den einzelnen Python-Skripten, aber die Bedienung wird über eine einheitliche Befehlsstruktur gebündelt.
Das ist näher an der späteren Ingame-Befehlsstruktur, zum Beispiel:

/press artikel liste
/press ausgabe lesen issue_0002
/press abonnent liste
 
---
## 2. Englische und deutsche Befehle
Die englischen Befehle bleiben verfügbar:

python press.py article list
python press.py issue read issue_0002 --full
python press.py subscriber list
python press.py validate
 
Zusätzlich gibt es deutsche Aliase:

python press.py artikel liste
python press.py ausgabe lesen issue_0002 --voll
python press.py abonnent liste
python press.py pruefen
 
Umlaute werden in Befehlen bewusst vermieden:

pruefen statt prüfen
loeschen statt löschen
veroeffentlichen statt veröffentlichen
 
---
## 3. Wichtige `press.py`-Befehle
### System prüfen
Deutsch:

python press.py pruefen
 
Englisch:

python press.py validate
 
---
### Artikel erstellen
Deutsch:

python press.py artikel erstellen --titel "Neue Baumfarm eröffnet" --kategorie build_projects --autor HF_jeti83 --zusammenfassung "Kurze Vorschau zur neuen Baumfarm."
 
Englisch:

python press.py article create --title "Neue Baumfarm eröffnet" --category build_projects --author HF_jeti83 --summary "Short preview for the new tree farm."
 
---
### Artikel bearbeiten
Untertitel ändern:

python press.py artikel bearbeiten article_0002 --untertitel "Vier Plots gegen die Holzknappheit"
 
Zusammenfassung ändern:

python press.py artikel bearbeiten article_0002 --zusammenfassung "Die Baumfarm bündelt verschiedene Holzarten und verbessert die Versorgung."
 
Artikeltext setzen:

python press.py artikel bearbeiten article_0002 --text "Auf Athena entsteht eine neue Baumfarm, die verschiedene Holzarten übersichtlich und platzsparend anbauen soll."
 
Bilddaten setzen:

python press.py artikel bearbeiten article_0002 --bild uploaded/baumfarm_001.png --bildquelle uploaded --bildunterschrift "Die Baumfarm im Aufbau." --nachweis "Foto: HF_jeti83"
 
Tags setzen:

python press.py artikel bearbeiten article_0002 --tags baumfarm holz bauprojekt
 
---
### Artikel auflisten
Deutsch:

python press.py artikel liste
 
Englisch:

python press.py article list
 
---
### Artikel archivieren

python press.py artikel archivieren article_0003 --grund "Testarchivierung"
 
Testlauf:

python press.py artikel archivieren article_0003 --test
 
---
### Ausgabe erstellen

python press.py ausgabe erstellen --untertitel "Die erste echte Testausgabe" --ausgabe-name "Farmwelt Spezial" --artikel article_0001 article_0002
 
Mit Titelartikel:

python press.py ausgabe erstellen --untertitel "Farmwelt Spezial" --ausgabe-name "Baumfarm-Ausgabe" --artikel article_0001 article_0002 --titelartikel article_0002
 
Mit Titelbild:

python press.py ausgabe erstellen --untertitel "Farmwelt Spezial" --ausgabe-name "Baumfarm-Ausgabe" --artikel article_0001 article_0002 --titelbild uploaded/baumfarm_001.png
 
---
### Ausgabe veröffentlichen
Testlauf:

python press.py ausgabe veroeffentlichen issue_0002 --test
 
Veröffentlichen:

python press.py ausgabe veroeffentlichen issue_0002
 
---
### Ausgabe lesen
Kurzansicht:

python press.py ausgabe lesen issue_0002
 
Vollansicht:

python press.py ausgabe lesen issue_0002 --voll
 
---
### Ausgaben auflisten

python press.py ausgabe liste
 
---
### Ausgabe zustellen
Testlauf:

python press.py ausgabe zustellen issue_0002 --test
 
Zustellen:

python press.py ausgabe zustellen issue_0002
 
Erneut erzwingen:

python press.py ausgabe zustellen issue_0002 --erzwingen
 
---
### Ausgabe archivieren

python press.py ausgabe archivieren issue_0001 --grund "Alter Testentwurf"
 
---
### Abonnent hinzufügen

python press.py abonnent hinzufuegen --name Jeti
 
Mit Zustellmodus:

python press.py abonnent hinzufuegen --name HF_jeti83 --zustellung mailbox
 
---
### Abonnent deaktivieren

python press.py abonnent deaktivieren --name Jeti
 
Mit Entfernen ungelesener Ausgaben:

python press.py abonnent deaktivieren --name Jeti --ungelesene-loeschen
 
---
### Abonnenten auflisten

python press.py abonnent liste
 
---
### Ausgabe als gelesen markieren

python press.py abonnent gelesen --name HF_jeti83 --ausgabe issue_0002
 
---
### Entwurf löschen
Nur Entwürfe dürfen gelöscht werden.
Artikelentwurf löschen, Testlauf:

python press.py entwurf loeschen artikel article_0005 --test
 
Artikelentwurf löschen:

python press.py entwurf loeschen artikel article_0005 --ja
 
Ausgabenentwurf löschen:

python press.py entwurf loeschen ausgabe issue_0003 --ja
 
---
## 4. Java-Core-Demo
Das Java-Modul liegt hier:

java/athena-press-core
 
Die Java-Demo wird aus dem Maven-Modulordner gestartet:

cd java/athena-press-core
 
---
### Standard-Preview anzeigen

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo"
 
Zeigt standardmäßig:

issue_0002
 
---
### Bestimmte Ausgabe als Preview anzeigen

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=issue_0002"
 
Die Preview zeigt unter anderem:

Ausgabe
Prüfung
Artikel
Zustellplan
 
---
### Veröffentlichte Ausgaben auflisten

Englisch:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--list"
 
Deutsch:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--liste"
 
---
### Artikel auflisten

Englisch:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--articles"

Deutsch:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--artikel"

Die Artikelliste zeigt gruppiert:

Veröffentlichte Artikel
Entwürfe
Archivierte Artikel
Artikel mit anderem Status

Pro Artikel werden angezeigt:

Artikel-ID
Kategorie
Titel
Status
Bild-Hinweis
Zusammenfassung, falls vorhanden

---
### Ausgabe validieren
Englisch:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--validate issue_0002"
 
Deutsch:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--pruefen issue_0002"
 
Ohne angegebene Ausgabe wird die Standardausgabe verwendet:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--validate"
 
---
### Kompakte Statusübersicht anzeigen
Englisch:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--status"
 
Deutsch:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--uebersicht"
 
Die Statusübersicht zeigt:

Artikel
Ausgaben
Abonnenten
aktive Abonnenten
Kategorien
aktive Kategorien
Gesamtvalidierung
 
---
### Hilfe anzeigen
Englisch:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--help"
 
Deutsch:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--hilfe"
 
Kurzformen:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=-h"
 

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=/?"
 
---
## 5. Java-Core testen
Aus dem Maven-Modulordner:

mvn -B clean verify
 
Dieser Befehl baut den Java-Core und führt alle Tests aus.

---
## 5a. Java-Integration Visual-Preview
Das Integration-Modul enthält eine adapter-neutrale Visual-Preview für echte veröffentlichte Ausgaben.

Sie wird aus dem Integration-Modulordner gestartet:

cd java/athena-press-integration

Standardausgabe:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.integration.AthenaPressVisualPreviewDemo"

Bestimmte Ausgabe:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.integration.AthenaPressVisualPreviewDemo" "-Dexec.args=issue_0002"

Expliziter Preview-Befehl:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.integration.AthenaPressVisualPreviewDemo" "-Dexec.args=--visual-preview issue_0002"

Deutscher Alias:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.integration.AthenaPressVisualPreviewDemo" "-Dexec.args=--vorschau issue_0002"

Die Ausgabe zeigt Doppelseiten, Seitenrollen, platzierte Blöcke, Spalten-/Zeilenpositionen und Bildpfade.

Wichtig:
Das ist keine HTML- oder Browser-Preview, sondern eine Textdarstellung der nativen Visual-Struktur.

### Native Visual-Runtime vorbereiten

Für spätere echte Hytale-Hooks gibt es im Integration-Modul:

HytaleNewspaperVisualRuntime<TPlayer>

Sie bündelt den nativen Visual-Pfad:

HytaleNewspaperVisualUiPort
PlayerNewspaperVisualUiController
HytaleNewspaperVisualInputAdapter
HytaleNewspaperLifecycleAdapter

Geplante spätere Zuordnung:

/ap
→ runtime.onChatCommand(...)

Overlay-Button "Weiter"
→ runtime.onUiButton(..., "visual_next_spread", ...)

Overlay-Button "Zurück"
→ runtime.onUiButton(..., "visual_previous_spread", ...)

Spieler verlässt den Server
→ runtime.onPlayerDisconnected(...)

Das ist weiterhin keine HTML-, Browser- oder WebView-Lösung.

---
## 6. Git-Komfortbefehl
Wenn die-Funktion `g_sacp` geladen ist, kann ein Änderungspaket so gespeichert werden:

g_sacp "Commit message"
 
Der Befehl führt aus:

git status
git add -A
git commit -m "Commit message"
git push
 Falls die Funktion aus dem Projekt geladen werden soll:

. .\tools\git-shortcuts.ps1
 
Wichtig ist der Punkt am Anfang. Dadurch wird die Funktion in der aktuellen-Sitzung verfügbar.
---
## 7. Alias-Übersicht für `press.py`
| Englisch | Deutsch |
|---|---|
| validate | pruefen |
| article | artikel |
| issue | ausgabe |
| subscriber | abonnent, abo |
| draft | entwurf |
| create | erstellen |
| edit | bearbeiten |
| list | liste, auflisten |
| archive | archivieren |
| publish | veroeffentlichen |
| read | lesen |
| deliver | zustellen |
| add | hinzufuegen, aktivieren |
| remove | entfernen, deaktivieren |
| delete | loeschen |
---
## 8. Options-Aliase für `press.py`
| Englisch | Deutsch |
|---|---|
| --title | --titel |
| --category | --kategorie |
| --author | --autor |
| --subtitle | --untertitel |
| --body | --text |
| --image | --bild |
| --caption | --bildunterschrift |
| --credit | --nachweis |
| --source-type | --bildquelle |
| --main-article | --titelartikel |
| --cover-image | --titelbild |
| --delivery-mode | --zustellung |
| --issue | --ausgabe |
| --reason | --grund |
| --dry-run | --test |
| --force | --erzwingen |
| --full | --voll |
| --yes | --ja |
| --clear-tags | --tags-loeschen |
| --clear-location | --ort-loeschen |
| --clear-unread | --ungelesene-loeschen |
---
## 9. Einzeltools im Ordner `tools/`
Die Einzeltools bleiben weiterhin nutzbar und dienen als stabile interne Bausteine.
Für neue Bedienung wird aber bevorzugt `press.py` verwendet.
---
### System prüfen

python tools\validate_press.py
 
---
### Artikel erstellen

python tools\create_article.py --title "Neue Baumfarm eröffnet" --category build_projects --author HF_jeti83
 
Mit Zusatzdaten:

python tools\create_article.py --title "Neue Baumfarm eröffnet" --category build_projects --author HF_jeti83 --subtitle "Vier Plots gegen die Holzknappheit" --teaser "Auf Athena entsteht eine neue Baumfarm." --tags baumfarm holz bauprojekt
 
---
### Artikel bearbeiten

python tools\edit_article.py article_0002 --subtitle "Vier Plots gegen die Holzknappheit"
 

python tools\edit_article.py article_0002 --body "Auf Athena entsteht eine neue Baumfarm, die verschiedene Holzarten übersichtlich und platzsparend anbauen soll."
 

python tools\edit_article.py article_0002 --image uploaded/baumfarm_001.png --source-type uploaded --caption "Die Baumfarm im Aufbau." --credit "Foto: HF_jeti83"
 

python tools\edit_article.py article_0002 --tags baumfarm holz bauprojekt
 
---
### Artikel auflisten

python tools\list_articles.py
 
---
### Artikel archivieren

python tools\archive_article.py article_0003 --dry-run
 

python tools\archive_article.py article_0003 --reason "Testarchivierung"
 
---
### Ausgabe erstellen

python tools\create_issue.py --subtitle "Die erste echte Testausgabe" --edition "Farmwelt Spezial" --articles article_0001 article_0002
 
Mit Titelartikel:

python tools\create_issue.py --subtitle "Farmwelt Spezial" --edition "Baumfarm-Ausgabe" --articles article_0001 article_0002 --main-article article_0002
 
---
### Ausgabe veröffentlichen

python tools\publish_issue.py issue_0002 --dry-run
 

python tools\publish_issue.py issue_0002
 
---
### Ausgabe lesen

python tools\read_issue.py issue_0002
 

python tools\read_issue.py issue_0002 --full
 
---
### Ausgaben auflisten

python tools\list_issues.py
 
---
### Ausgabe archivieren

python tools\archive_issue.py issue_0001 --dry-run
 

python tools\archive_issue.py issue_0001 --reason "Alter Testentwurf"
 
---
### Entwürfe löschen
Nur Entwürfe können gelöscht werden.

python tools\delete_draft.py article article_0004 --dry-run
 

python tools\delete_draft.py article article_0004 --yes
 

python tools\delete_draft.py issue issue_0003 --yes
 
---
### Abonnent hinzufügen

python tools\subscribe_player.py --name Jeti
 

python tools\subscribe_player.py --name HF_jeti83 --delivery-mode mailbox
 
---
### Abonnent deaktivieren

python tools\unsubscribe_player.py --name Jeti
 
Mit Entfernen ungelesener Ausgaben:

python tools\unsubscribe_player.py --name Jeti --clear-unread
 
---
### Abonnenten auflisten

python tools\list_subscribers.py
 
---
### Zustellung
Ausgabe zustellen:

python tools\deliver_issue.py issue_0002 --dry-run
 

python tools\deliver_issue.py issue_0002
 
Erneut erzwingen:

python tools\deliver_issue.py issue_0002 --force
 
---
### Ausgabe als gelesen markieren

python tools\mark_issue_read.py --name HF_jeti83 --issue issue_0002
 
---
## 10. Empfohlene Kontrollbefehle

Java-Core testen:

mvn -B clean verify
 
Java-Demo-Status anzeigen:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--status"

Die Statusübersicht zeigt zusätzlich eine gruppierte Ausgabenliste mit:
- Veröffentlichte Ausgaben
- Entwürfe
- Archivierte Ausgaben
- Ausgaben mit anderem Status

Pro Ausgabe werden angezeigt:
- Ausgabe-ID
- Ausgabennummer
- Titel
- Status
- Artikelanzahl
- Cover-Hinweis
- Untertitel, falls vorhanden

Java-Demo-Artikelliste anzeigen:

mvn -q exec:java "-Dexec.mainClass=pro.jeti.athenapress.AthenaPressDemo" "-Dexec.args=--articles"

Die Artikelliste zeigt zusätzlich eine gruppierte Übersicht mit:

- Veröffentlichte Artikel
- Entwürfe
- Archivierte Artikel
- Artikel mit anderem Status

Pro Artikel werden angezeigt:

- Artikel-ID
- Kategorie
- Titel
- Status
- Bild-Hinweis
- Zusammenfassung, falls vorhanden

Python-Backend prüfen:

python press.py pruefen
 
Häufige Python-Übersichten:

python press.py artikel liste
python press.py ausgabe liste
python press.py abonnent liste
 
