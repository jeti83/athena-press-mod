
---

# Inhalt für `docs/workflow.md`

```markdown
# AthenaPress Workflow

Stand: AthenaPress Backend v0.3

Dieses Dokument beschreibt den typischen Ablauf einer AthenaPress-Ausgabe.

## Grundablauf

```text
Artikel erstellen
→ Artikel bearbeiten
→ Ausgabe erstellen
→ Ausgabe prüfen
→ Ausgabe veröffentlichen
→ Ausgabe zustellen
→ Spieler liest Ausgabe
→ Ausgabe wird als gelesen markiert

1. Artikel erstellen
python tools\create_article.py --title "Neue Baumfarm eröffnet" --category build_projects --author HF_jeti83

Der Artikel landet als Entwurf unter:
AthenaPress/articles/draft/

2. Artikel bearbeiten

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


3. Artikel prüfen

python tools\validate_press.py
python tools\list_articles.py

Vor der Ausgabe sollte geprüft werden:
- Titel vorhanden
- Kategorie korrekt
- Autor korrekt
- Text vorhanden
- Bildpfad korrekt oder bewusst Platzhalter

4. Ausgabe erstellen

python tools\create_issue.py --subtitle "Farmwelt Spezial" --edition "Baumfarm-Ausgabe" --articles article_XXXX

Mit mehreren Artikeln:
python tools\create_issue.py --subtitle "Neue Geschichten aus der Farmwelt" --edition "Wochenblatt" --articles article_0001 article_0002 article_XXXX

5. Ausgabe prüfen

python tools\list_issues.py
python tools\read_issue.py issue_XXXX --full
python tools\validate_press.py

Vor dem Veröffentlichen prüfen:

- richtige Artikel enthalten
- Titelartikel korrekt
- Ausgabe noch draft
- Artikeltexte vollständig

6. Ausgabe veröffentlichen

Erst Testlauf:
python tools\publish_issue.py issue_XXXX --dry-run

Dann veröffentlichen:
python tools\publish_issue.py issue_XXXX

Dabei werden die Ausgabe und enthaltene Entwurfsartikel nach published/ verschoben.

7. Abonnenten verwalten

Abonnent hinzufügen:
python tools\subscribe_player.py --name HF_jeti83 --delivery-mode mailbox

Abonnent deaktivieren:
python tools\unsubscribe_player.py --name HF_jeti83

Übersicht:
python tools\list_subscribers.py

8. Ausgabe zustellen

Erst Testlauf:
python tools\deliver_issue.py issue_XXXX --dry-run

Dann zustellen:
python tools\deliver_issue.py issue_XXXX

Die Ausgabe wird bei aktiven Abonnenten in unreadIssues eingetragen.

9. Ausgabe lesen

python tools\read_issue.py issue_XXXX --full

10. Ausgabe als gelesen markieren

python tools\mark_issue_read.py --name HF_jeti83 --issue issue_XXXX

Dadurch wird die Ausgabe aus unreadIssues entfernt.

11. Archivieren

Alte Ausgabe archivieren:
python tools\archive_issue.py issue_XXXX --reason "Alter Testentwurf"

Alten Artikel archivieren:
python tools\archive_article.py article_XXXX --reason "Testartikel"

Archivierte Inhalte werden nicht gelöscht.

12. Entwürfe löschen

Nur Entwürfe können gelöscht werden:

python tools\delete_draft.py article article_XXXX --yes
python tools\delete_draft.py issue issue_XXXX --yes
Standardablauf für eine echte Ausgabe
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
Spätere Hytale-Entsprechung
Python-Tool	Möglicher Hytale-Befehl
subscribe_player.py	/press subscribe
unsubscribe_player.py	/press unsubscribe
list_issues.py	/press list
read_issue.py	/press read <issue>
publish_issue.py	/press publish <issue>
mark_issue_read.py	automatisch nach Lesen
create_article.py	Redaktions-UI
edit_article.py	Redaktions-UI