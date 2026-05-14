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

5. Visual-Layout-Schicht

NewspaperVisualIssue

Visuelle Zeitungsstruktur für immersive Ausgaben.

Enthält:

Titel
Theme
Seiten
Doppelseiten-Navigation

NewspaperVisualPage

Visuelle Einzelseite.

Enthält:

Seitennummer
Seitentitel
Zeitungsblöcke

NewspaperVisualBlock

Inhaltlicher Layoutblock.

Beispiele:

HEADLINE
SUBHEADLINE
BODY_TEXT
IMAGE
QUOTE
NOTICE
ADVERTISEMENT
DIVIDER

NewspaperLayoutTemplate

API-neutrales Layoutschema für Zeitungseiten.

Enthält:

Seitenmaße
Ränder
Spalten
Zeilenkapazität
Spaltenabstände

NewspaperPageLayout

Konkretes Layout einer Seite.

Enthält:

Spalten
Content-Platzierungen
Bild-Platzierungen

NewspaperBlockLayoutRule

Beschreibt Layoutregeln für einen Visual-Blocktyp.

Enthält:

Standardhöhe
Featured-Höhe
Mindestspaltenbreite
Full-Width-Präferenz

NewspaperBlockLayoutRuleSet

Zentrale Regelquelle für Pagination und Rendering.

Verantwortlich für:

Zeilenhöhe pro Blocktyp
Spaltenbreite pro Blocktyp
Gewichtung für Seitenumbrüche
Featured-Bilder
Hauptartikel-Blöcke
volle Breite für Headlines und Divider

NewspaperLayoutMood

Beschreibt die gewünschte Layoutstimmung.

Aktuelle Varianten:

CLASSIC_NEWSPAPER
LOOSE_COMMUNITY_SHEET
FEATURE_DOCUMENT

NewspaperPageCornerStyle

Beschreibt dekorative Seitenecken.

Aktuelle Varianten:

NONE
SUBTLE_TOP_FOLDS
HANGING_TOP_CORNERS

NewspaperVisualDesignProfile

Fasst optische Designentscheidungen backend-neutral zusammen.

AthenaPress nutzt aktuell als Zielrichtung:

klassisches Zeitungsgefühl
leichte asymmetrische Community-Zeitung
optional dokumentartige Artikelseiten
dezente obere Seitenecken
Anzeigen- und Dokumentblöcke als Stilmittel
maximal lesbarer Seitenkörper
keine starre Pflicht zu vier Spalten
einzelne Titelseite vor den Artikeln
Artikel möglichst geschlossen lesbar
Umbruch auf Folgeseiten, wenn Lesbarkeit es erfordert
haptisch wirkendes Blättern
dezentes Seitenmenü nur als Zusatznavigation

NewspaperCoverPolicy

Beschreibt, wie Ausgaben beginnen.

Aktuell bevorzugt:

STANDALONE_TITLE_PAGE

NewspaperArticleFlowPolicy

Beschreibt den Umgang mit Artikelfluss.

Aktuell bevorzugt:

KEEP_ARTICLES_TOGETHER_WHEN_READABLE

NewspaperNavigationStyle

Beschreibt die spätere Navigation.

Aktuell bevorzugt:

PAGE_TURNING_WITH_SUBTLE_MENU

Redaktioneller Ton:
AthenaPress soll wie ein Spaßblatt mit seriösem Auftreten wirken.
Reißerische und lustige Anekdoten sind der Normalfall, ernste Meldungen wie Informationen oder Beileidsartikel bleiben aber möglich.

NewspaperPageSectionType

Beschreibt optionale Kompositionsbereiche einer Ausgabe.

Aktuelle Typen:

TITLE_PAGE
MAIN_ARTICLE
MIXED_ARTICLES
ADVERTISEMENTS
SHORT_NOTICES
MEMORIAL
BACK_PAGE

NewspaperSectionRequirement

Beschreibt, ob eine Section erzeugt wird.

Varianten:

REQUIRED
WHEN_CONTENT_EXISTS
DISABLED

NewspaperPageSectionPolicy

Verhindert leere Pflichtseiten.

Grundregel:
Nur die Titelseite ist standardmäßig verpflichtend.
Alle weiteren Bereiche entstehen nur, wenn sie Inhalt haben.

Dadurch können Anzeigen-, Kurzmeldungs-, Beileids- oder Rückseitenbereiche komplett wegfallen, wenn die Ausgabe sie nicht braucht.

NewspaperArticleCompositionService

Erzeugt aus einer spielnahen Ausgabe eine visuelle Zeitung.

Verantwortlich für:

Titelblock
Coverbild
Artikelblöcke
Zusammenfassungen
Seitenaufbau

NewspaperVisualPaginationService

Verteilt Visual-Blöcke auf mehrere Seiten.

NewspaperVisualRenderer

Erzeugt aus visuellen Seiten adapter-neutrale Layoutplatzierungen.

NewspaperPageRole

Beschreibt die Rolle einer Seite innerhalb einer Ausgabe.

Aktuelle Rollen:

FRONT_COVER
LEFT_INNER
RIGHT_INNER
BACK_COVER
SINGLE_PAGE

NewspaperDoublePageLayout

Beschreibt eine konkrete Doppelseite.

Enthält:

linke Seite
rechte Seite
Seitenrollen
Spread-Index
Navigationsbuttons

NewspaperDoublePageCompositionService

Erzeugt aus visuellen Seiten bewusste Doppelseiten.

Verantwortlich für:

Cover-Erkennung
Innenseiten-Paare
Rückseitenrolle
einzelne letzte Seite
Blätter-Navigation

Wichtig:
Diese Schicht ist keine Browser-, HTML- oder WebView-Lösung.
Sie bereitet nur Daten für ein späteres natives Hytale-Overlay vor.

6. UI-Controller-Schicht
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
7. Event-/Input-Layer
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

8. Session-/Lifecycle-Management
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
9. Hytale-API-Adapter

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

10. Architekturfluss
Zeitung öffnen
Hytale Event
→ InputAdapter
→ InputDispatcher
→ UiController
→ InteractionService
→ Core Services
→ Response
→ ViewFactory
→ Visual Composition
→ Visual Renderer
→ UiPort
→ HytaleUiBridge
→ Spiel-UI
11. Wichtige Architekturentscheidungen
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
12. Aktueller Projektstatus
Architektur

Sehr weit fortgeschritten.

Core-System

Nahe MVP-Fertigstellung.

Spielintegration

Strukturell vorbereitet.

Echte Hytale-API

Noch nicht konkret angebunden.

Visual-System

Begonnen.

Vorhanden sind:

Visual-Blöcke
Visual-Seiten
Doppelseiten
Layout-Templates
Layout-Regeln
Designprofile
Layoutstimmungen
Seitenecken-Optionen
Titelseiten-Policy
Artikel-Fluss-Policy
Navigationsstil
optionale Sections
Section-Erzeugungsregeln
Spaltenmodell
Content-Platzierungen
Bild-Platzierungen
Artikel-Komposition
Pagination
adapter-neutrales Rendering
Doppelseiten-Komposition
Seitenrollen
Spread-Navigation

13. Nächster realistischer Großschritt

Weiterer Ausbau des Visual-Layout-Systems.

Als Nächstes werden relevant:

stärkere Layoutregeln
Artikelüberläufe
Doppelseiten-Komposition
Bildgrößen
Titel-/Covervarianten
Anzeigenblöcke
spätere native Hytale-UI-Verdrahtung

Der große Unterbau dafür existiert inzwischen.
