package pro.jeti.athenapress.plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// Hytale Plugin API
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;

// AthenaPress Integration
import pro.jeti.athenapress.integration.ArticleEditorView;
import pro.jeti.athenapress.integration.AthenaPressIntegrationPlugin;
import pro.jeti.athenapress.integration.CameraScreenshotService;
import pro.jeti.athenapress.integration.ConsoleNewspaperUiPort;
import pro.jeti.athenapress.integration.HytaleNewspaperVisualRuntime;
import pro.jeti.athenapress.integration.IssueEditorView;

/**
 * Einstiegspunkt der AthenaPress Hytale-Mod.
 *
 * Deployment: athena-press-plugin-*.jar → <server>/mods/
 * Datenpfad:  <plugin-data>/AthenaPress/
 */
@SuppressWarnings("deprecation") // ConsoleNewspaperUiPort als Text-Fallback
public class AthenaPressPlugin extends JavaPlugin {

    private HytaleNewspaperVisualRuntime<String> runtime;
    private PlayerContextResolver contextResolver;
    private NewspaperVisualBridge visualBridge;
    private CameraUiBridge cameraBridge;
    private EditorUiBridge editorBridge;
    private CameraScreenshotService cameraService;
    private AthenaPressIntegrationPlugin core;

    public AthenaPressPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        Path athenaPressRoot = getDataDirectory().resolve("AthenaPress");

        contextResolver = new PlayerContextResolver();
        visualBridge    = new NewspaperVisualBridge();
        cameraBridge    = new CameraUiBridge();
        editorBridge    = new EditorUiBridge();

        core = new AthenaPressIntegrationPlugin(athenaPressRoot);

        runtime = new HytaleNewspaperVisualRuntime<>(
                core,
                new ConsoleNewspaperUiPort(),
                visualBridge,
                contextResolver
        );
        visualBridge.setRuntime(runtime);

        // EditorUiBridge-Callbacks verdrahten
        editorBridge.setMenuHandler(this::handleMenuCommand);
        editorBridge.setArticleEditorHandler(this::handleArticleEditorCommand);
        editorBridge.setIssueEditorHandler(this::handleIssueEditorCommand);

        // CameraScreenshotService starten
        try {
            Path screenshotDir = readScreenshotDir(athenaPressRoot);
            cameraService = new CameraScreenshotService(
                    cameraBridge,
                    core.getAlbumService(),
                    athenaPressRoot,
                    screenshotDir
            );
            getLogger().at(Level.INFO)
                    .log("Kamera-Watcher gestartet - Verzeichnis: " + screenshotDir);
        } catch (Exception e) {
            getLogger().at(Level.WARNING)
                    .withCause(e)
                    .log("Kamera-Watcher konnte nicht gestartet werden");
        }

        getEventRegistry().register(PlayerConnectEvent.class,    this::onPlayerConnect);
        getEventRegistry().register(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
        getEventRegistry().registerGlobal(PlayerInteractEvent.class, this::onPlayerInteract);
        getEventRegistry().registerAsyncGlobal(PlayerChatEvent.class,
                future -> future.thenApply(event -> {
                    onPlayerChat(event);
                    return event;
                }));

        getCommandRegistry().registerCommand(new ApCommand(runtime, editorBridge));

        getLogger().at(Level.INFO)
                .log("AthenaPress geladen - Datenpfad: " + athenaPressRoot);
    }

    @Override
    protected void start() {
        getLogger().at(Level.INFO).log("AthenaPress aktiv.");
    }

    @Override
    protected void shutdown() {
        if (cameraService != null) cameraService.shutdown();
        try {
            runtime.onServerShutdown();
        } catch (Exception | Error e) {
            getLogger().at(Level.WARNING)
                    .log("Shutdown-Cleanup uebersprungen (Classloader bereits geschlossen)");
        }
        getLogger().at(Level.INFO).log("AthenaPress beendet.");
    }

    // -----------------------------------------------------------------------
    // Event-Handler
    // -----------------------------------------------------------------------

    private void onPlayerConnect(PlayerConnectEvent event) {
        Object rawRef = event.getPlayerRef();
        getLogger().at(Level.INFO)
                .log("[Connect] getPlayerRef() = " + (rawRef == null ? "NULL" : rawRef.getClass().getName()));

        String playerId   = event.getPlayerRef().getUuid().toString();
        String playerName = event.getPlayerRef().getUsername();

        contextResolver.register(playerId, playerName);
        visualBridge.registerPlayer(playerId, rawRef);
        cameraBridge.registerPlayer(playerId, rawRef);
        editorBridge.registerPlayer(playerId, rawRef);

        getLogger().at(Level.INFO)
                .log("[Connect] editorBridge registriert: " + editorBridge.isRegistered(playerId)
                     + " fuer " + playerId.substring(0, 8));

        runtime.onPlayerConnected(playerId);
    }

    private void onPlayerDisconnect(PlayerDisconnectEvent event) {
        String playerId = event.getPlayerRef().getUuid().toString();

        runtime.onPlayerDisconnected(playerId);

        visualBridge.unregisterPlayer(playerId);
        cameraBridge.unregisterPlayer(playerId);
        editorBridge.unregisterPlayer(playerId);
        contextResolver.unregister(playerId);
    }

    @SuppressWarnings({"deprecation", "removal"}) // Player.getPlayerRef() ist die einzige aktuelle
    // Quelle fuer PlayerRef hier; ein Ersatz erfordert store.getComponent(...) in world.execute()
    // (siehe Memory).
    private void onPlayerInteract(PlayerInteractEvent event) {
        var item = event.getItemInHand();
        if (item == null || item.isEmpty()) return;

        String itemId = item.getItemId();
        if (itemId == null) return;

        if (itemId.contains("AP_Camera")) {
            getLogger().at(Level.INFO)
                    .log("AP_Camera erkannt - tatsaechliche Item-ID: " + itemId);
        }

        boolean isCamera = ApCommand.CAMERA_ITEM_ID.equals(itemId)
                || ("HytaleAthena.AP_Camera:" + ApCommand.CAMERA_ITEM_ID).equals(itemId);

        if (isCamera && cameraService != null) {
            PlayerRef playerRef = event.getPlayer() != null ? event.getPlayer().getPlayerRef() : null;
            if (playerRef == null) {
                getLogger().at(Level.WARNING).log("AP_Camera benutzt, aber PlayerRef fehlt im PlayerInteractEvent");
                return;
            }
            String playerId   = playerRef.getUuid().toString();
            String playerName = contextResolver.resolve(playerId).playerName();
            try {
                cameraService.onCameraItemUse(playerId, playerName);
            } catch (Exception e) {
                getLogger().at(Level.WARNING)
                        .withCause(e)
                        .log("Fehler bei Kamera-Aufnahme");
            }
        }
    }

    private void onPlayerChat(PlayerChatEvent event) {
        String playerId = event.getSender().getUuid().toString();
        String message  = event.getContent();

        getLogger().at(Level.INFO)
                .log("[Chat] id=" + playerId + " msg=" + message
                        + " articleEditor=" + core.hasActiveEditorSession(playerId)
                        + " issueEditor="   + core.hasActiveIssueEditorSession(playerId));

        if (message == null || message.isBlank()) return;

        if (core.hasActiveEditorSession(playerId)) {
            try {
                ArticleEditorView view = core.handleEditorInput(playerId, message);
                editorBridge.openOrUpdateArticleEditor(playerId, view);
                event.setCancelled(true);
            } catch (IOException e) {
                getLogger().at(Level.WARNING)
                        .withCause(e).log("Artikel-Editor Chat-Eingabe fehlgeschlagen");
            }
            return;
        }

        if (core.hasActiveIssueEditorSession(playerId)) {
            try {
                IssueEditorView view = core.handleIssueEditorInput(playerId, message);
                editorBridge.openOrUpdateIssueEditor(playerId, view);
                event.setCancelled(true);
            } catch (IOException e) {
                getLogger().at(Level.WARNING)
                        .withCause(e).log("Ausgaben-Editor Chat-Eingabe fehlgeschlagen");
            }
        }
    }

    // -----------------------------------------------------------------------
    // EditorUiBridge-Callbacks
    // -----------------------------------------------------------------------

    private void handleMenuCommand(String playerId, String cmd, boolean isAdmin) {
        getLogger().at(Level.INFO).log("[Menue] id=" + playerId + " cmd=" + cmd);
        switch (cmd) {
            case "open_newspaper" -> {
                try {
                    // Text-Session öffnen, um die neueste Ausgaben-ID zu ermitteln
                    core.onPlayerOpenLatestNewspaper(playerId);
                    String issueId = core.getOpenIssueId(playerId);
                    if (issueId != null) {
                        // Visuelle Zeitung über den Runtime öffnen
                        runtime.onPlayerChatCommand(playerId, "open", issueId);
                    }
                } catch (IOException e) {
                    getLogger().at(Level.WARNING).withCause(e).log("Zeitung konnte nicht geoeffnet werden");
                }
            }
            case "start_article_editor" -> {
                String playerName = contextResolver.resolve(playerId).playerName();
                ArticleEditorView view = core.startArticleEditor(playerId, playerName, isAdmin);
                editorBridge.openOrUpdateArticleEditor(playerId, view);
            }
            case "start_issue_editor" -> {
                String playerName = contextResolver.resolve(playerId).playerName();
                try {
                    IssueEditorView view = core.startIssueEditor(playerId, playerName, isAdmin);
                    editorBridge.openOrUpdateIssueEditor(playerId, view);
                } catch (IOException e) {
                    getLogger().at(Level.WARNING).withCause(e).log("Ausgaben-Editor konnte nicht gestartet werden");
                }
            }
            case "give_camera" -> giveCameraItem(playerId);
            case "close"       -> { /* Seite schließt sich bereits durch die UI */ }
            default            -> getLogger().at(Level.WARNING).log("Unbekannter Menue-Befehl: " + cmd);
        }
    }

    private void handleArticleEditorCommand(String playerId, String cmd, String val) {
        getLogger().at(Level.INFO).log("[ArtikelEditor] id=" + playerId + " cmd=" + cmd + " val=" + val);
        switch (cmd) {
            case "select" -> {
                try {
                    ArticleEditorView view = core.handleEditorInput(playerId, val);
                    editorBridge.openOrUpdateArticleEditor(playerId, view);
                } catch (IOException e) {
                    getLogger().at(Level.WARNING).withCause(e).log("Artikel-Editor-Schaltflaeche fehlgeschlagen");
                }
            }
            case "cancel" -> {
                core.cancelEditorSession(playerId);
                // EditorUiBridge kennt den Admin-Status aus dem Cache (gesetzt beim openMainMenu)
                editorBridge.openMainMenu(playerId,
                        editorBridge.getCachedAdminStatus(playerId));
            }
            default -> getLogger().at(Level.WARNING).log("Unbekannter Artikel-Editor-Befehl: " + cmd);
        }
    }

    private void handleIssueEditorCommand(String playerId, String cmd, String val) {
        getLogger().at(Level.INFO).log("[AusgabenEditor] id=" + playerId + " cmd=" + cmd + " val=" + val);
        switch (cmd) {
            case "select" -> {
                try {
                    IssueEditorView view = core.handleIssueEditorInput(playerId, val);
                    editorBridge.openOrUpdateIssueEditor(playerId, view);
                } catch (IOException e) {
                    getLogger().at(Level.WARNING).withCause(e).log("Ausgaben-Editor-Schaltflaeche fehlgeschlagen");
                }
            }
            case "cancel" -> core.cancelIssueEditorSession(playerId);
            default -> getLogger().at(Level.WARNING).log("Unbekannter Ausgaben-Editor-Befehl: " + cmd);
        }
    }

    // -----------------------------------------------------------------------
    // Hilfsmethoden
    // -----------------------------------------------------------------------

    private void giveCameraItem(String playerId) {
        var entityRef = editorBridge.getEntityRef(playerId);
        if (entityRef == null) return;

        var store = entityRef.getStore();
        var ext   = store != null ? store.getExternalData() : null;
        var world = ext  != null ? ext.getWorld()           : null;
        if (world == null) return;

        world.execute(() -> {
            try {
                var hotbar = store.getComponent(entityRef, InventoryComponent.Hotbar.getComponentType());
                if (hotbar == null) return;
                hotbar.getInventory().addItemStack(new ItemStack(ApCommand.CAMERA_ITEM_ID, 1));
            } catch (Exception e) {
                getLogger().at(Level.WARNING).withCause(e).log("Kamera-Item konnte nicht gegeben werden");
            }
        });
    }

    private Path readScreenshotDir(Path athenaPressRoot) throws IOException {
        Path configFile = athenaPressRoot.resolve("config.json");
        if (!configFile.toFile().exists()) {
            return defaultScreenshotDir();
        }
        JsonNode root = new ObjectMapper().readTree(configFile.toFile());
        JsonNode camera = root.path("camera");
        if (!camera.isMissingNode() && camera.has("screenshotDirectory")) {
            return Paths.get(camera.get("screenshotDirectory").asText());
        }
        return defaultScreenshotDir();
    }

    private Path defaultScreenshotDir() {
        return Paths.get(System.getProperty("user.home"),
                "OneDrive", "Pictures", "Hytale Screenshots");
    }
}
