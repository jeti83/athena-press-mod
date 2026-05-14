AthenaPress – Architekturstand nach Hytale-Adapter
Gesamtstruktur

## Zielbild der AthenaPress-UI

AthenaPress ist als immersives Zeitungs- und Storytelling-System für den Athena-Hytale-Server gedacht.

Das langfristige Ziel ist kein Chatfenster, keine Browserlösung und keine reine Textausgabe, sondern ein ingame-natives Zeitungs-Overlay.

Spieler sollen AthenaPress später beispielsweise über einen Befehl wie `/ap` öffnen können und darin Ausgaben, Artikel, Bilder und interaktive Elemente lesen oder nutzen.

Die visuelle Richtung orientiert sich an Zeitungslayouts, Doppelseiten, Artikelseiten, Anzeigen, eingebetteten Bildern, satirischen Dokumenten, Community-Humor und serverinternen Geschichten.

Humoristische oder pseudo-offizielle Grafiken können als Bildmaterial innerhalb einzelner Artikel auftauchen, dienen aber nur als Stilmittel für Glaubwürdigkeit, Atmosphäre und Augenzwinkern.

Der Kern von AthenaPress ist damit ein immersives Medien- und Storytelling-Overlay, nicht ein Verwaltungs-, Behörden- oder reines Command-System.


Projekt:

athena-press-mod

Maven-Reactor:

java/
├─ pom.xml
├─ athena-press-core/
└─ athena-press-integration/
1. athena-press-core

Zuständig für:

Datenmodell
Artikel
Ausgaben
Sessions
Rendering
Spiellogik
Textdarstellung
Zeitungszustand
Wichtige Services
GameIssueView

Spielnahe Ansicht einer Zeitungsausgabe.

Enthält:

Metadaten
Artikelliste
Referenzen
Anzeigeinformationen
GameArticleView

Spielnahe Artikelansicht.

Enthält:

Titel
Untertitel
Text
Kategorien
Anzeigeinformationen
GameViewService

Erzeugt spielnahe Zeitung-/Artikelansichten.

Verantwortlich für:

Laden veröffentlichter Ausgaben
Auflösen referenzierter Artikel
Aufbau von Spielansichten
GameTextRendererService

Text-/Konsolenrenderer.

Verantwortlich für:

Übersichten
Artikelanzeige
Listen
Konsolen-/Debugdarstellung
GameNewspaperSessionService

Spielerspezifische Zeitungssessions.

Verantwortlich für:

geöffnete Ausgaben
aktueller Artikel
Sessionzustand
2. athena-press-integration

Zuständig für:

Spielintegration
UI-Flow
Eventfluss
Inputsystem
Hytale-Anbindung
Lifecycle
UI-Ports
3. Integration-Kern
AthenaPressIntegrationPlugin

Zentrale Integrationsklasse.

Verantwortlich für:

Öffnen von Zeitungen
Artikelauswahl
Sessioninteraktion
Controller-Zugriffe
NewspaperIntegrationGateway

Brücke zwischen:

Core
Sessionsystem
Integration
PlayerNewspaperInteractionService

Verarbeitet spielnahe Spieleraktionen:

OPEN_ISSUE
SHOW_OVERVIEW
SELECT_ARTICLE_BY_NUMBER
SELECT_ARTICLE_BY_ID
CLOSE_ISSUE
4. UI-State-/View-Modell
NewspaperUiView

Zentrale UI-Ansicht.

Enthält:

Titel
Body
Buttons
ScreenType
Issue-Zustand
Close-State
NewspaperUiButton

Interaktive UI-Aktion.

Enthält:

Label
Style
Command
NewspaperUiScreenType

UI-Ansichtstypen:

OVERVIEW
ARTICLE
MESSAGE
ERROR
CLOSED
NewspaperUiButtonStyle

Buttondarstellung:

PRIMARY
SECONDARY
DANGER
NewspaperUiViewFactory

Erzeugt UI-Views aus Integration-Responses.

Wichtig:
Die Spiel-UI kennt dadurch nicht direkt Core-Objekte.

5. UI-Controller-Schicht
PlayerNewspaperUiController

Zentrale UI-Steuerung.

Verantwortlich für:

Commands
UI-Aktionen
Fehlerhandling
Übergang UI ↔ Integration
PlayerNewspaperUiCommand

API-neutrale UI-Befehle.

Beispiele:

openIssue
showOverview
selectArticle
closeIssue
PlayerNewspaperResponse

Antwortobjekt für UI-Schicht.

Enthält:

Aktion
Text
Sessionzustand
offene Ausgabe
closeRequested
6. Event-/Input-Layer
PlayerNewspaperInputEvent

Spielereingabe-Ereignis.

Quellen:

NPC
Item
UI
Chat
Keybind
Server
PlayerNewspaperInputMapper

Übersetzt Eingaben zu UI-Commands.

PlayerNewspaperInputDispatcher

Verteilt Eingaben an UI-Controller.

7. Session-/Lifecycle-Management
PlayerNewspaperLifecycleEvent

Lifecycle-Ereignisse:

connect
disconnect
timeout
shutdown
PlayerNewspaperLifecycleHandler

Verantwortlich für:

Sessioncleanup
Zeitung schließen
Timeoutverarbeitung
8. Hytale-API-Adapter

Das ist die wichtigste neue Ebene.

Sie trennt:

AthenaPress-Architektur
von
echter Hytale-API
HytalePlayerContext

API-neutraler Spielerkontext.

HytalePlayerContextResolver<TPlayer>

Spätere echte Hytale-Spielerauflösung.

HytaleNewspaperUiBridge

Abstrakte echte Hytale-UI.

Später zuständig für:

Fenster öffnen
Buttons aktualisieren
UI schließen
HytaleNewspaperUiPort

Adapter:

AthenaPress-UI
→ Hytale-UI

HytaleNewspaperInputAdapter

Übersetzt echte Hytale-Ereignisse:

NPC-Klick
Item-Use
UI-Button
Keybind

in AthenaPress-InputEvents.

HytaleNewspaperLifecycleAdapter

Verbindet:

Join
Disconnect
Timeout
Shutdown

mit AthenaPress-Lifecycle.

9. Architekturfluss
Zeitung öffnen
Hytale Event
→ InputAdapter
→ InputDispatcher
→ UiController
→ InteractionService
→ Core Services
→ Response
→ ViewFactory
→ UiPort
→ HytaleUiBridge
→ Spiel-UI
10. Wichtige Architekturentscheidungen
Kein HTML-System

Bewusst vermieden:

Browserlösungen
WebView-Systeme
HTML-Rendering
künstliche Webseiten

Ziel:

echte Hytale-UI.

API-neutrale Integration

Keine direkten Fantasie-Hytale-Imports.

Dadurch:

stabilere Architektur
leichteres API-Upgrade
testbare Integration
weniger Rewrite-Risiko
Spieler = eigene Session

Jeder Spieler besitzt:

eigene Zeitung
eigenen UI-State
eigene Navigation
eigene Artikelauswahl
11. Aktueller Projektstatus
Architektur

Sehr weit fortgeschritten.

Core-System

Nahe MVP-Fertigstellung.

Spielintegration

Strukturell vorbereitet.

Echte Hytale-API

Noch nicht konkret angebunden.

12. Nächster realistischer Großschritt

Nun beginnt erstmals:

echte Hytale-API-Recherche
statt Architekturaufbau.

Ab jetzt werden relevant:

echte UI-Klassen
echte Eventsysteme
echte Buttons
echte Fenster
echte Server-Callbacks
echte Player-Objekte

Der große Unterbau dafür existiert inzwischen.