# AthenaPress

AthenaPress ist ein lokales Backend-Konzept für eine serverseitige Zeitung auf dem Athena-Hytale-Server.

Das System verwaltet:

- Artikel
- Zeitungsausgaben
- Kategorien
- Platzhalter- und Screenshot-Bilder
- Abonnenten
- Zustellung und Lesestatus

Der aktuelle Stand ist ein Python-basiertes Redaktions-Backend mit zentralem CLI über `press.py`.

## Start
Wichtige Befehle
python press.py artikel liste
python press.py ausgabe liste
python press.py abonnent liste
python press.py ausgabe lesen issue_0002 --voll
Dokumentation

Weitere Informationen:

docs/data_model.md
docs/commands.md
docs/workflow.md
Status

Aktueller Stand: AthenaPress Backend v0.3

Funktionen:

Artikel erstellen, bearbeiten, listen und archivieren
Ausgaben erstellen, veröffentlichen, lesen, zustellen und archivieren
Abonnenten hinzufügen, deaktivieren, reaktivieren und listen
Ausgaben als gelesen markieren
Entwürfe sicher löschen
Datenbestand validieren
deutsches und englisches CLI über press.py

