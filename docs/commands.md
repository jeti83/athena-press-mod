# AthenaPress Befehlsübersicht

Stand: AthenaPress Backend v0.3

Alle Befehle werden im Projektordner ausgeführt:

```powershell
cd C:\Users\jeti8\Projekte\Hytale-Mods\athena-press-mod

## Zentrales CLI: `press.py`

Neben den einzelnen Tools im Ordner `tools/` gibt es ein zentrales Kommandozeilenwerkzeug:

```powershell
python press.py ...

press.py ist ein Router für die bestehenden Tools. Die eigentliche Logik bleibt in den einzelnen Python-Skripten, aber die Bedienung wird über eine einheitliche Befehlsstruktur gebündelt.

Das ist näher an der späteren Ingame-Befehlsstruktur, z. B.:

/press artikel liste
/press ausgabe lesen issue_0002
/press abonnent liste
Englische und deutsche Befehle

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

Wichtige press.py-Befehle:

System prüfen

Deutsch:
    python press.py pruefen

Englisch:
    python press.py validate

Artikel erstellen

Deutsch:
    python press.py artikel erstellen --titel "Neue Baumfarm eröffnet" --kategorie build_projects --autor HF_jeti83 --zusammenfassung "Kurze Vorschau zur neuen Baumfarm."

Englisch:
    python press.py article create --title "Neue Baumfarm eröffnet" --category build_projects --author HF_jeti83 --summary "Short preview for the new tree farm."

Artikel bearbeiten

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

Artikel auflisten
    python press.py artikel liste

Englisch:
    python press.py article list

Artikel archivieren
    python press.py artikel archivieren article_0003 --grund "Testarchivierung"

Testlauf:

    python press.py artikel archivieren article_0003 --test

Ausgabe erstellen
    python press.py ausgabe erstellen --untertitel "Die erste echte Testausgabe" --ausgabe-name "Farmwelt Spezial" --artikel article_0001 article_0002

Mit Titelartikel:

    python press.py ausgabe erstellen --untertitel "Farmwelt Spezial" --ausgabe-name "Baumfarm-Ausgabe" --artikel article_0001 article_0002 --titelartikel article_0002

Mit Titelbild:

    python press.py ausgabe erstellen --untertitel "Farmwelt Spezial" --ausgabe-name "Baumfarm-Ausgabe" --artikel article_0001 article_0002 --titelbild uploaded/baumfarm_001.png

Ausgabe veröffentlichen

Testlauf:

    python press.py ausgabe veroeffentlichen issue_0002 --test

Veröffentlichen:

    python press.py ausgabe veroeffentlichen issue_0002

Ausgabe lesen

Kurzansicht:

    python press.py ausgabe lesen issue_0002

Vollansicht:

    python press.py ausgabe lesen issue_0002 --voll


Ausgaben auflisten

    python press.py ausgabe liste

Ausgabe zustellen

Testlauf:

    python press.py ausgabe zustellen issue_0002 --test

Zustellen:

    python press.py ausgabe zustellen issue_0002

Erneut erzwingen:

    python press.py ausgabe zustellen issue_0002 --erzwingen

Ausgabe archivieren

    python press.py ausgabe archivieren issue_0001 --grund "Alter Testentwurf"

Abonnent hinzufügen

    python press.py abonnent hinzufuegen --name Jeti

Mit Zustellmodus:

    python press.py abonnent hinzufuegen --name HF_jeti83 --zustellung mailbox

Abonnent deaktivieren

    python press.py abonnent deaktivieren --name Jeti

Mit Entfernen ungelesener Ausgaben:

    python press.py abonnent deaktivieren --name Jeti --ungelesene-loeschen

Abonnenten auflisten

    python press.py abonnent liste

Ausgabe als gelesen markieren

    python press.py abonnent gelesen --name HF_jeti83 --ausgabe issue_0002

Entwurf löschen

Testlauf:

    python press.py entwurf loeschen artikel article_0005 --test

Löschen:

    python press.py entwurf loeschen artikel article_0005 --ja

Ausgabenentwurf löschen:

    python press.py entwurf loeschen ausgabe issue_0003 --ja

Alias-Übersicht
Englisch	Deutsch
validate	pruefen
article	artikel
issue	ausgabe
subscriber	abonnent, abo
draft	entwurf
create	erstellen
edit	bearbeiten
list	liste, auflisten
archive	archivieren
publish	veroeffentlichen
read	lesen
deliver	zustellen
add	hinzufuegen, aktivieren
remove	entfernen, deaktivieren
delete	loeschen

Options-Aliase
Englisch	Deutsch
--title	--titel
--category	--kategorie
--author	--autor
--subtitle	--untertitel
--body	--text
--image	--bild
--caption	--bildunterschrift
--credit	--nachweis
--source-type	--bildquelle
--main-article	--titelartikel
--cover-image	--titelbild
--delivery-mode	--zustellung
--issue	--ausgabe
--reason	--grund
--dry-run	--test
--force	--erzwingen
--full	--voll
--yes	--ja
--clear-tags	--tags-loeschen
--clear-location	--ort-loeschen
--clear-unread	--ungelesene-loeschen
Empfehlung

Für neue Bedienung bevorzugt press.py verwenden:

python press.py pruefen
python press.py artikel liste
python press.py ausgabe liste
python press.py abonnent liste

Die Einzeltools in tools/ bleiben weiterhin nutzbar und dienen als stabile interne Bausteine.

System prüfen
python tools\validate_press.py
Artikel
Artikel erstellen
python tools\create_article.py --title "Neue Baumfarm eröffnet" --category build_projects --author HF_jeti83

Mit Zusatzdaten:

python tools\create_article.py --title "Neue Baumfarm eröffnet" --category build_projects --author HF_jeti83 --subtitle "Vier Plots gegen die Holzknappheit" --teaser "Auf Athena entsteht eine neue Baumfarm." --tags baumfarm holz bauprojekt
Artikel bearbeiten
python tools\edit_article.py article_0002 --subtitle "Vier Plots gegen die Holzknappheit"
python tools\edit_article.py article_0002 --body "Auf Athena entsteht eine neue Baumfarm, die verschiedene Holzarten übersichtlich und platzsparend anbauen soll."
python tools\edit_article.py article_0002 --image uploaded/baumfarm_001.png --source-type uploaded --caption "Die Baumfarm im Aufbau." --credit "Foto: HF_jeti83"
python tools\edit_article.py article_0002 --tags baumfarm holz bauprojekt
Artikel auflisten
python tools\list_articles.py
Artikel archivieren
python tools\archive_article.py article_0003 --dry-run
python tools\archive_article.py article_0003 --reason "Testarchivierung"
Ausgaben
Ausgabe erstellen
python tools\create_issue.py --subtitle "Die erste echte Testausgabe" --edition "Farmwelt Spezial" --articles article_0001 article_0002

Mit Titelartikel:

python tools\create_issue.py --subtitle "Farmwelt Spezial" --edition "Baumfarm-Ausgabe" --articles article_0001 article_0002 --main-article article_0002
Ausgabe veröffentlichen
python tools\publish_issue.py issue_0002 --dry-run
python tools\publish_issue.py issue_0002
Ausgabe lesen
python tools\read_issue.py issue_0002
python tools\read_issue.py issue_0002 --full
Ausgaben auflisten
python tools\list_issues.py
Ausgabe archivieren
python tools\archive_issue.py issue_0001 --dry-run
python tools\archive_issue.py issue_0001 --reason "Alter Testentwurf"
Entwürfe löschen

Nur Entwürfe können gelöscht werden.

python tools\delete_draft.py article article_0004 --dry-run
python tools\delete_draft.py article article_0004 --yes
python tools\delete_draft.py issue issue_0003 --yes
Abonnenten
Abonnent hinzufügen
python tools\subscribe_player.py --name Jeti
python tools\subscribe_player.py --name HF_jeti83 --delivery-mode mailbox
Abonnent deaktivieren
python tools\unsubscribe_player.py --name Jeti

Mit Entfernen ungelesener Ausgaben:

python tools\unsubscribe_player.py --name Jeti --clear-unread
Abonnenten auflisten
python tools\list_subscribers.py
Zustellung
Ausgabe zustellen
python tools\deliver_issue.py issue_0002 --dry-run
python tools\deliver_issue.py issue_0002

Erneut erzwingen:

python tools\deliver_issue.py issue_0002 --force
Ausgabe als gelesen markieren
python tools\mark_issue_read.py --name HF_jeti83 --issue issue_0002
Empfohlene Kontrolle
python tools\validate_press.py
python tools\list_articles.py
python tools\list_issues.py
python tools\list_subscribers.py