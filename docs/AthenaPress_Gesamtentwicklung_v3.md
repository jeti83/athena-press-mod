# AthenaPress – Gesamtentwicklung v3

**Stand: Mai 2026**
**Version: 0.4.0-SNAPSHOT**
**Server: Athena (Hytale)**
**Autor: jeti83**

---

## Fortschrittsübersicht

> Massstab: ██ = 10 % | ░░ = offen | ✅ = abgeschlossen | 🔄 = aktiv in Arbeit | ⏳ = wartet

| Bereich | Fortschritt | Stand |
|---|---|---|
| **Python-CLI / Backend** | `██████████` 95 % | ✅ stabil, produktiv |
| **Java-Core (Datenmodell, Services)** | `█████████░` 90 % | ✅ 103 Tests grün |
| **Java-Integration (Layout, Sessions)** | `█████████░` 90 % | ✅ 197 Tests grün |
| **Hytale-Plugin (Infrastruktur)** | `████████░░` 80 % | ✅ läuft, Events korrekt |
| **Ingame-GUI – Öffnen** | `██████░░░░` 60 % | 🔄 Probe-Modi funktionieren |
| **Ingame-GUI – Events / Interaktion** | `█████░░░░░` 50 % | 🔄 InteractiveCustomUIPage migriert |
| **Kamera-System** | `██████░░░░` 60 % | 🔄 Watcher + Album laufen |
| **Zeitung lesen (Ingame-Overlay)** | `███░░░░░░░` 30 % | 🔄 Visual-Runtime vorbereitet |
| **Artikel-/Ausgaben-Editor (Ingame)** | `████░░░░░░` 40 % | 🔄 GUI-Flow implementiert, Test offen |
| **Kamera-Item-Modell (3D)** | `█░░░░░░░░░` 10 % | ⏳ Blockbench-Umbau ausstehend |
| **Ingame-Zustellung** | `░░░░░░░░░░`  5 % | ⏳ geplant |
| **Mehrspielerbetrieb** | `███░░░░░░░` 30 % | ⏳ SessionManager vorbereitet |
| **Deployment (Server-Admin-Installer)** | `██████████` 100 % | ✅ Release-Paket + Installer fertig |

**Gesamtfortschritt (gewichtet):** `███████░░░` ca. 65 %

---

## 1. Projektziel

AthenaPress ist ein datengetriebenes Zeitungssystem für den Athena-Hytale-Server. Ziel ist eine ingame-native Zeitung (*Athena Botenblatt*), in der Spieler Artikel schreiben, Fotos aufnehmen, Ausgaben zusammenstellen und diese als Zeitungsseiten direkt im Spiel lesen können.

Der Entwicklungsansatz ist bewusst backend-first: Alle Kernfunktionen sind vollständig implementiert und testbar. Die Hytale-Plugin-Schicht ist seit v0.4 aktiv verbunden – das Plugin läuft, Befehle und Events werden verarbeitet, die GUI-Anbindung befindet sich in aktiver Test- und Debugging-Phase.

---

## 2. Systemübersicht

| Schicht | Beschreibung | Status |
|---|---|---|
| Hytale-Plugin (`athena-press-plugin`) | Events, Commands, CustomUIPage, Kamera-Watcher | 🔄 aktiv, GUI in Test |
| `athena-press-integration` (Java) | Visual-Layout, Editor, Album, Kamera, Sessions | ✅ 197 Tests |
| `athena-press-core` (Java) | Datenmodell, Repositories, Services, Validierung | ✅ 103 Tests |
| Python-CLI (`press.py`) | Redaktions-Werkzeuge, Dateiverwaltung | ✅ produktiv |
| `AthenaPress/` (JSON-Datenspeicher) | Artikel, Ausgaben, Abonnenten, Bilder, Alben | ✅ 15 Artikel, 4 Ausgaben |

### Plugin-Infrastruktur (neu in v0.4)

Das Plugin läuft auf dem Hytale-Singleplayer-Server. Folgende Hytale-API-Teile sind korrekt angebunden:

| Komponente | API-Klasse | Status |
|---|---|---|
| Spieler-Connect | `PlayerConnectEvent` | ✅ |
| Spieler-Disconnect | `PlayerDisconnectEvent` | ✅ |
| Item-Interaktion | `PlayerInteractEvent` (registerGlobal) | ✅ |
| Chat-Eingabe | `PlayerChatEvent` (registerAsyncGlobal) | ✅ |
| GUI öffnen | `InteractiveCustomUIPage<UiEventData>` | 🔄 Probe-Modi OK |
| `/ap`-Befehl | `AbstractCommand` | ✅ |
| PlayerRef-Zugriff | `event.getPlayer().getPlayerRef()` | ✅ (deprecated, siehe 3.6) |
| Kamera-Screenshot-Watcher | `WatchService` auf Screenshot-Ordner | ✅ |

---

## 3. Entwicklungsphasen

### Phase 1 – Python-Backend `██████████` 95 %

Das Python-CLI (`press.py`) ist das produktive Redaktionswerkzeug.

**Funktionen:**
- Artikel erstellen, bearbeiten, veröffentlichen, archivieren
- Ausgaben erstellen, veröffentlichen, zustellen, archivieren
- Abonnentenverwaltung, Zustellstatus, Lesestatus
- Vollständige Validierung (inkl. Duplikat-Erkennung seit v0.4)
- Deutsche und englische Befehlsaliase

```powershell
python press.py pruefen
python press.py artikel erstellen --titel "Titel" --kategorie server_news --autor Jeti
python press.py ausgabe lesen issue_0002 --voll
python press.py ausgabe zustellen issue_0002
```

---

### Phase 2 – Java-Core `█████████░` 90 %

Liest echte JSON-Daten, stellt alle Services für Vorschau, Validierung und Spielintegration bereit.

**Teststand:** 103 Tests, 0 Failures

| Service | Zuständigkeit |
|---|---|
| `PressService` | Ausgaben und Artikel laden, auflösen |
| `ValidationService` | Mehrstufige Validierung inkl. Duplikat-IDs (neu v0.4) |
| `DeliveryService` | Zustellpläne berechnen |
| `GameNewspaperSessionService` | Spielerspezifische Zeitungssession |
| `ArticleWriteService` | Entwürfe als JSON-Dateien schreiben |
| `PlayerAlbumService` | Foto-Album-Operationen |

---

### Phase 3 – Java-Integration `█████████░` 90 %

**Teststand:** 197 Tests, 0 Failures

#### 3.1 Visual-Layout-System

```
GameIssueView
  → NewspaperArticleCompositionService   (Artikel → Visual-Blöcke)
  → NewspaperVisualPaginationService     (Blöcke → Seiten)
  → NewspaperDoublePageCompositionService (Seiten → Doppelseiten)
  → NewspaperPreviewService              (Doppelseiten → PreviewIssue)
  → NewspaperPreviewImageRenderer        (PreviewIssue → PNG-Dateien)
```

**Block-Typen:** HEADLINE, SUBHEADLINE, BODY_TEXT, IMAGE, CAPTION, QUOTE, NOTICE, ADVERTISEMENT, DIVIDER

#### 3.2 PNG-Vorschau

```powershell
cd java
mvn exec:java -pl athena-press-integration -am "-Dexec.mainClass=pro.jeti.athenapress.integration.AthenaPressVisualPngPreviewDemo" "-Dexec.args=issue_0004 C:\Users\jeti8\Downloads\athena-press-preview"
```

Ergebnis: Doppelseiten-PNGs mit Titelseite, Artikelseiten, Rückseite.

#### 3.3 Artikel-Editor (Ingame) `████░░░░░░` 40 %

Flow: Titel → Kategorie → Text (via Chat) → Foto aus Album → Vorschau → Einreichen → Draft-JSON

#### 3.4 Ausgaben-Editor (Ingame) `████░░░░░░` 40 %

Flow: Schritt-für-Schritt per Button-Wahl und Chat-Eingabe (IssueEditorService).

#### 3.5 Kamera-System `██████░░░░` 60 %

- **Watcher:** `ScreenshotFileWatcher` (WatchService) auf Screenshot-Ordner — läuft
- **Album:** Fotos werden automatisch eingetragen
- **Auslösung:** Chat-Nachricht mit F12-Aufforderung (CameraUiBridge)
- **Item:** `AP_Camera` im Inventar ausgebbar via `/ap`-Menü
- **Modell:** Blockbench-Umbau des Referenzmodells noch ausstehend

#### 3.6 Hytale-Plugin-Schicht `████████░░` 80 %

Das Plugin ist mit der echten Hytale-API verbunden. Alle Event-Klassen kommen aus dem korrekten Paket `com.hypixel.hytale.server.core.event.events.player.*`.

**Bekannte offene Punkte:**
- `getPlayerRef()` in `Player` ist `@Deprecated(forRemoval=true)`. Ersatz identifiziert:
  `PlayerEvent.getPlayerRef()` liefert bereits einen `Ref<EntityStore>`, aus dem sich die
  `PlayerRef`-Komponente per `store.getComponent(ref, PlayerRef.getComponentType())` holen
  liesse — das muss aber innerhalb von `world.execute()` passieren. Der Umbau der zwei
  betroffenen Stellen (`AthenaPressPlugin.onPlayerInteract`, `ApCommand.ensurePlayerRegistered`)
  wurde bewusst zurueckgestellt, solange das GUI-System noch im Crash-Debugging steckt;
  aktuell per `@SuppressWarnings({"deprecation","removal"})` dokumentiert.
- GUI in `NORMAL`-Modus (vollständiges Layout) noch in Debugging-Phase
- `ui.remove()` und `InteractiveCustomUIPage`-Verhalten unter Test

**Probe-Modi (aktiv):**

| Befehl | Modus | Ergebnis |
|---|---|---|
| `/ap uimin` | Nur Label | ✅ |
| `/ap uipanel` | Panel + Labels | ✅ |
| `/ap uibuttons` | Panel + Buttons | ✅ |
| `/ap uievents` | Buttons + Events | ✅ |
| `/ap uiactivate` | Activating-Events | 🔄 Test läuft |
| `/ap uidismiss` | Dismissing-Event | 🔄 Test läuft |
| `/ap uinormal` | Volles Hauptmenü | 🔄 in Debugging |

#### 3.7 Deployment für Server-Admins `██████████` 100 %

Server-Admins ohne eigenes Java/Maven koennen AthenaPress ueber ein fertiges Release-Paket
installieren, siehe [`docs/installation.md`](installation.md):

- `scripts/build-release.ps1` (Mod-Entwickler) baut Plugin-JAR + Asset-Pack + Icons zu
  `dist/athena-press-release-<version>.zip` mit `release-info.json` fuer den Installer.
- `tools/install_athena_press.py` (Server-Admin) installiert dieses Paket interaktiv oder
  non-interaktiv (Welten-Auswahl, `--dry-run`, `--non-interactive` fuer Automatisierung).
- Ergaenzt (nicht ersetzt) `scripts/deploy-ap.ps1`, das weiterhin fuer die lokale
  Entwicklungs-Iteration des Mod-Autors gedacht ist.

---

## 4. Verfügbare Ausgaben

| ID | Titel | Artikel | Spreads |
|---|---|---|---|
| `issue_0002` | Die erste echte Testausgabe | Dating, Baumfarm, Kurzmeldungen | 4 |
| `issue_0003` | Jubilaeumsausgabe | Servergeburtstag, Rathaus, Eisenmarkt, Kleinanzeigen | 4 |
| `issue_0004` | Sonderausgabe Stadtentwicklung | Marktfest, Bibliothek, Spawngeruecht, Wollbert | 5 |

**Artikel gesamt:** 15 veroeffentlicht, 0 im Entwurf, 1 archiviert

---

## 5. Datenmodell

### Artikel (`articles/published/article_XXXX.json`)

```json
{
  "id": "article_0001",
  "status": "published",
  "categoryId": "server_news",
  "title": "...",
  "author": { "playerName": "Jeti", "playerUuid": "..." },
  "body": "...",
  "image": { "file": "uploaded/cam_Jeti_20260515.png", "sourceType": "camera_marker" },
  "createdAt": "2026-05-15T19:00:00+02:00"
}
```

### Ausgabe (`issues/published/issue_XXXX.json`)

```json
{
  "id": "issue_0004",
  "status": "published",
  "issueNumber": 4,
  "title": "Athena Botenblatt",
  "cover": { "mainArticleId": "article_0012", "image": "placeholders/no_image.png" },
  "articles": ["article_0012", "article_0013", "article_0014", "article_0015"],
  "deliveredToSubscribers": false
}
```

### Kategorien

| ID | Name |
|---|---|
| `headline` | Schlagzeile |
| `server_news` | Server-News |
| `build_projects` | Bauprojekte |
| `economy` | Wirtschaft & Handel |
| `classifieds` | Kleinanzeigen |
| `dating` | Herzblatt der Farmwelt |

---

## 6. Teststand

| Modul | Tests | Failures | Errors |
|---|---|---|---|
| `athena-press-core` | 103 | 0 | 0 |
| `athena-press-integration` | 197 | 0 | 0 |
| **Gesamt** | **300** | **0** | **0** |

```powershell
cd java && mvn -B clean verify
```

---

## 7. Technische Umgebung

| Komponente | Version |
|---|---|
| Java | 25.0.2 (Microsoft OpenJDK) |
| Maven | 3.9.15 |
| JUnit | 5.11.4 |
| Jackson | 2.18.2 |
| Hytale Server | Singleplayer-Testserver (Entwicklungsversion) |
| Hytale UI | `CustomUIPage` / `InteractiveCustomUIPage<T>` (native Hytale API) |
| Python | 3.x |

---

## 8. Ordnerstruktur

```
athena-press-mod/
+-- AthenaPress/
|   +-- config.json
|   +-- articles/  (draft / published / archived)
|   +-- issues/    (draft / published / archived)
|   +-- images/    (placeholders / uploaded / cam_*.png)
|   +-- players/   (<name>/album.json)
|   +-- subscriptions/subscribers.json
|   +-- templates/ (categories.json, article_template.json, issue_template.json)
+-- docs/
+-- mods/HytaleAthena.AP_Camera/Common/UI/Custom/AthenaPress/
+-- scripts/deploy-ap.ps1
+-- press.py
+-- java/
    +-- athena-press-core/
    +-- athena-press-integration/
    +-- athena-press-plugin/          <- Hytale-Plugin, laeuft auf dem Server
        +-- ui/
            +-- MainMenuPage.java     (InteractiveCustomUIPage<UiEventData>)
            +-- ArticleEditorPage.java
            +-- IssueEditorPage.java
            +-- NewspaperPage.java
            +-- UiEventData.java      (Record + Codec fuer typed events)
```

---

## 9. Roadmap

### Kurzfristig – GUI-Debugging `████░░░░░░`

| Aufgabe | Stand |
|---|---|
| NORMAL-Modus ohne Crash | 🔄 Probe-Test laeuft |
| `ui.remove()` API-Verhalten klaeren | 🔄 offen |
| `@Deprecated getPlayerRef()` Ersatz finden | 🔄 Ersatz identifiziert, WorldThread-Umbau zurueckgestellt |
| Zeitung lesen via `/ap` (Visual-Runtime) | ⏳ nach GUI-Fix |

### Mittelfristig – Spielflow `███░░░░░░░`

| Aufgabe | Stand |
|---|---|
| Artikel-Editor via GUI vollstaendig bedienbar | 🔄 Flow implementiert |
| Ausgaben-Editor via GUI vollstaendig bedienbar | 🔄 Flow implementiert |
| Kamera-Item-Modell (Blockbench, `.blockymodel`) | ⏳ Referenzmodell vorhanden |
| HUD-toggle bei Kamera-Aufnahme | ⏳ API-Verhalten klaeren |

### Langfristig – Mehrspielerbetrieb `░░░░░░░░░░`

| Aufgabe | Stand |
|---|---|
| Ingame-Zustellung an Abonnenten | ⏳ |
| Mehrspielerfaehige Sessions | ⏳ SessionManager vorbereitet |
| Abonnenten-UI ingame | ⏳ |

---

## 10. Ingame-Flow (Zielzustand)

```
Spieler betritt den Server
  -> Ungelesene Ausgaben werden zugestellt (Item oder Benachrichtigung)

Spieler tippt /ap
  -> Hauptmenue oeffnet sich (Hytale CustomUIPage)
  -> Buttons: Zeitung lesen | Kamera holen | Artikel schreiben | Ausgabe erstellen

Spieler tippt /ap redaktion
  -> Artikel-Editor oeffnet sich (schrittweise GUI + Chat-Eingabe)
  -> Titel, Kategorie, Text, Foto aus Album -> Einreichen -> Draft gespeichert

Admin tippt /ap veroeffentlichen article_XXXX
  -> Artikel wird veroeffentlicht

Spieler haelt Kamera-Item -> linke Maustaste
  -> F12-Aufforderung per Chat, Screenshot, Foto im Album
  -> Foto im naechsten Artikel verwendbar
```

---

## 11. Bekannte Hinweismeldungen (harmlos)

| Meldung | Ursache | Auswirkung |
|---|---|---|
| `Unused key(s) in 'EditorTool_LaserPointer': BuilderTool.BrushData` | Hytale-interne Item-Datei mit veralteten Feldern | keine – Hytale ignoriert unbekannte Keys |
| `Texture width/height must be a multiple of 32: HalfBlock_CustomSide_Clay_Raw_Brick.png` | Hytale-interne Textur mit 48x48 px | keine – Rendering-Warnung des Engines |
| `getPlayerRef() ... veraltet und wurde zum Entfernen markiert` | Hytale markiert API als deprecated | funktioniert noch; Ersatz identifiziert (siehe 3.6), Umbau zurueckgestellt; bewusst per `@SuppressWarnings` unterdrueckt |

---

*Generiert: Mai 2026 — AthenaPress v0.4.0-SNAPSHOT — Plugin laeuft, GUI in Debugging-Phase*
