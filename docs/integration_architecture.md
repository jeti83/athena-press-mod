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

PlayerNewspaperVisualNavigationService

Verwaltet die spätere visuelle Zeitungsnavigation pro Spieler.

Pro Spieler wird nur gehalten:

offene Ausgaben-ID
aktueller Doppelseiten-Index

Die vorbereiteten Doppelseiten selbst kommen aus der Preview-Pipeline und dem Visual-Runtime-Cache.

Dadurch muss der spätere Livepfad nicht pro Klick und pro Spieler neu komponieren, sondern nur den aktuellen Spread auswählen.

Unterstützt:

visuelle Ausgabe öffnen
aktuelle Doppelseite anzeigen
weiterblättern
zurückblättern
visuelle Session schließen

PlayerNewspaperVisualView

Adapter-neutrale visuelle UI-Ansicht für eine geöffnete Doppelseite.

Enthält:

Spieler-ID
Ausgaben-ID
Titel
Doppelseiten-Index
linke Seite
rechte Seite
Navigationszustand
Buttons

PlayerNewspaperVisualViewFactory

Erzeugt aus einer PlayerNewspaperVisualResponse eine UI-nahe Visual-View.

Der spätere Hytale-Adapter muss dadurch nicht direkt Session-Responses interpretieren, sondern bekommt bereits eine klare Ansicht mit Seiten und Buttons.

PlayerNewspaperVisualUiController

Steuert den visuellen UI-Fluss.

Verantwortlich für:

visuelle Ausgabe öffnen
aktuelle Doppelseite anzeigen
Weiter-/Zurückblättern aus UI-Commands verarbeiten
visuelle Ausgabe schließen
Fehler in Message-Views übersetzen

PlayerNewspaperVisualUiPort

Port für spätere native Visual-UI-Ausgabe.

ConsoleNewspaperVisualUiPort

Einfache Debug-Ausgabe für Visual-Views ohne Browser, HTML oder WebView.
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

Hinweis zur Benennung:
Im Java-Code wird der englische Begriff `Section` verwendet, weil die technische API bereits englisch benannt ist.
In der Produkt- und Redaktionssprache entspricht das einem Bereich oder einer Rubrik.

NewspaperArticleClassifier

Ordnet Artikel in passende Bereiche ein.

Aktuelle Regeln:

Cover-Artikel wird Hauptartikel
Anzeigen-Kategorien werden Anzeigenbereich
kurze Inhalte oder Kurzmeldungs-Kategorien werden Kurzmeldungen
Texte mit Verschollen-/Beileids-/Nachruf-Signalen werden Memorial-Bereich
alle übrigen Artikel werden Mischartikel

NewspaperArticleClassification

Beschreibt das Klassifizierungsergebnis.

Enthält:

Zielbereich
Hauptartikel-Markierung
Kurzmeldungs-Markierung
Sonderton-Markierung

NewspaperPreviewIssue

Adapter-neutrale Vorschau einer visuellen Ausgabe.

Enthält:

Ausgaben-ID
Titel
Theme
Designprofil
Doppelseiten-Preview

NewspaperPreviewSpread

Vorschau einer Doppelseite.

Enthält:

Spread-Index
linke Seite
rechte Seite
Navigationsbuttons

NewspaperPreviewPage

Vorschau einer einzelnen Zeitungsseite.

Enthält:

Seitennummer
Titel
Seitenrolle
Designprofil
Blockvorschau

NewspaperPreviewBlock

Vorschau eines platzierten Blocks.

Enthält:

Blocktyp
Text
Asset-Pfad
Spaltenposition
Zeilenposition
Spaltenbreite
Zeilenhöhe

NewspaperPreviewService

Erzeugt aus einer visuellen Ausgabe eine strukturierte Vorschau.

NewspaperPreviewTextRenderer

Erzeugt eine lesbare Textvorschau für Debugging, Admin-Werkzeuge oder Tests.

Wichtig:
Die Preview-Schicht ist keine Browser-, HTML- oder finale Hytale-UI.
Sie dient nur dazu, das visuelle Zeitungsmodell vor einer echten UI-Anbindung prüfen zu können.

NewspaperPreviewPipelineService

Verbindet echte veröffentlichte Ausgaben mit der Preview-Schicht.

Fluss:

GameViewService
→ NewspaperArticleCompositionService
→ NewspaperVisualRuntimeCache
→ NewspaperPreviewService
→ NewspaperPreviewTextRenderer

Damit kann eine reale Ausgabe bereits als adapter-neutrale Doppelseiten-Vorschau geprüft werden, ohne dass eine native Hytale-UI fertig sein muss.

NewspaperIntegrationGateway und AthenaPressIntegrationPlugin stellen dafür schlanke Preview-Methoden bereit.

NewspaperVisualRuntimeCache

Hält vorbereitete Visual-Previews für veröffentlichte Ausgaben im Speicher.

Ziel:

Ausgaben werden nicht pro Spieler oder pro Klick neu komponiert.
Der spätere Livepfad soll nur leichte Spieler-Sessions und Navigation halten.
Die teurere Komposition von Artikeln, Blöcken, Seiten und Doppelseiten kann wiederverwendet werden.

Fehlende oder leere Ausgaben werden nicht gecacht, damit spätere Veröffentlichungen nicht durch einen alten Fehlzustand blockiert werden.

Der Cache kann pro Ausgabe invalidiert oder vollständig geleert werden, zum Beispiel nach Veröffentlichung, Archivierung oder Server-Reload.

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

PlayerNewspaperVisualInputMapper

Übersetzt Overlay-Eingaben zu Visual-UI-Commands.

Unterstützt unter anderem:

/ap
öffnen
weiter
zurück
schließen

PlayerNewspaperVisualInputDispatcher

Verteilt Visual-Eingaben an den PlayerNewspaperVisualUiController.

Der Text-Dispatcher bleibt davon getrennt, damit klassische Text-/Debug-Ausgabe
und native Visual-Zeitung nicht versehentlich dieselben Zustände vermischen.

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
Visual-Overlay-Cleanup

Aktuelle Cleanup-Regel:

disconnect
→ Text-Zeitung schließen
→ Visual-Zeitung schließen
→ registrierten Visual-Spielerkontext freigeben

timeout
→ Text-Zeitung schließen
→ Visual-Zeitung schließen
→ Spieler-Kontext behalten

Server-Shutdown bleibt bewusst placeholder-only, solange kein globales
closeAllNewspapers() im Plugin existiert.

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

HytaleNewspaperVisualUiBridge

Abstrakte native Visual-UI-Brücke.

Später zuständig für:

Doppelseiten-Overlay öffnen
Doppelseiten-Overlay aktualisieren
Visual-UI schließen

HytaleNewspaperVisualUiPort

Adapter:

AthenaPress Visual-View
→ native Hytale-Zeitungs-UI

Er hält nur registrierte Spieler-Kontexte und reicht fertige PlayerNewspaperVisualViews an die Bridge weiter.

HytaleNewspaperInputAdapter

Übersetzt echte Hytale-Ereignisse:

NPC-Klick
Item-Use
UI-Button
Keybind

in AthenaPress-InputEvents.

HytaleNewspaperVisualInputAdapter

Übersetzt echte Hytale-Ereignisse in den Visual-Input-Pfad.

Damit können spätere native Overlay-Buttons oder Keybinds direkt steuern:

öffnen
aktualisieren
weiterblättern
zurückblättern
schließen

HytaleNewspaperLifecycleAdapter

Verbindet:

Join
Disconnect
Timeout
Shutdown

mit AthenaPress-Lifecycle.

Der Adapter ist generisch über TPlayer und nutzt ausschließlich einen
HytalePlayerContextResolver<TPlayer>. Dadurch bleibt AthenaPress frei von
erfundenen direkten Hytale-API-Imports.

Bei Join kann der Adapter den Spieler im HytaleNewspaperVisualUiPort
registrieren.

Bei Disconnect wird nur ein Lifecycle-Event ausgelöst; das eigentliche
Schließen und Freigeben des Visual-Kontexts übernimmt der
PlayerNewspaperLifecycleHandler.

HytaleNewspaperVisualRuntime<TPlayer>

Composition-Root für den nativen Visual-Pfad.

Sie bündelt:

AthenaPressIntegrationPlugin
HytaleNewspaperVisualUiPort
PlayerNewspaperVisualUiController
PlayerNewspaperVisualInputDispatcher
HytaleNewspaperVisualInputAdapter
PlayerNewspaperLifecycleHandler
HytaleNewspaperLifecycleAdapter<TPlayer>

Spätere Hytale-Hooks müssen dadurch nicht jeden Baustein einzeln
zusammensetzen, sondern können die Runtime erzeugen und ihre Adapter
verwenden.

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
Artikel-Klassifizierung
Preview-Struktur
Text-Preview
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
