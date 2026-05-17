package pro.jeti.athenapress.plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// Hytale Plugin API
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.event.PlayerConnectEvent;
import com.hypixel.hytale.event.PlayerDisconnectEvent;
import com.hypixel.hytale.event.PlayerInteractEvent;
import com.hypixel.hytale.event.PlayerChatEvent;

// AthenaPress Integration
import pro.jeti.athenapress.integration.AthenaPressIntegrationPlugin;
import pro.jeti.athenapress.integration.CameraScreenshotService;
import pro.jeti.athenapress.integration.ConsoleNewspaperUiPort;
import pro.jeti.athenapress.integration.HytaleNewspaperVisualRuntime;

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
    private CameraScreenshotService cameraService;

    public AthenaPressPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        Path athenaPressRoot = getDataDirectory().resolve("AthenaPress");

        contextResolver = new PlayerContextResolver();
        visualBridge    = new NewspaperVisualBridge();
        cameraBridge    = new CameraUiBridge();

        AthenaPressIntegrationPlugin core = new AthenaPressIntegrationPlugin(athenaPressRoot);

        runtime = new HytaleNewspaperVisualRuntime<>(
                core,
                new ConsoleNewspaperUiPort(),
                visualBridge,
                contextResolver
        );
        visualBridge.setRuntime(runtime);

        // CameraScreenshotService starten
        try {
            Path screenshotDir = readScreenshotDir(athenaPressRoot);
            cameraService = new CameraScreenshotService(
                    cameraBridge,
                    core.getAlbumService(),
                    athenaPressRoot,
                    screenshotDir
            );
            getLogger().at(java.util.logging.Level.INFO)
                    .log("Kamera-Watcher gestartet – Verzeichnis: " + screenshotDir);
        } catch (Exception e) {
            getLogger().at(java.util.logging.Level.WARNING)
                    .withCause(e)
                    .log("Kamera-Watcher konnte nicht gestartet werden");
        }

        getEventRegistry().register(PlayerConnectEvent.class,    this::onPlayerConnect);
        getEventRegistry().register(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
        getEventRegistry().register(PlayerInteractEvent.class,   this::onPlayerInteract);
        getEventRegistry().register(PlayerChatEvent.class,       this::onPlayerChat);

        getCommandRegistry().registerCommand(new ApCommand(runtime));

        getLogger().at(java.util.logging.Level.INFO)
                .log("AthenaPress geladen – Datenpfad: " + athenaPressRoot);
    }

    @Override
    protected void start() {
        getLogger().at(java.util.logging.Level.INFO).log("AthenaPress aktiv.");
    }

    @Override
    protected void shutdown() {
        if (cameraService != null) cameraService.shutdown();
        runtime.onServerShutdown();
        getLogger().at(java.util.logging.Level.INFO).log("AthenaPress beendet.");
    }

    // -----------------------------------------------------------------------
    // Event-Handler
    // -----------------------------------------------------------------------

    private void onPlayerConnect(PlayerConnectEvent event) {
        String playerId   = extractPlayerId(event);
        String playerName = extractPlayerName(event);

        contextResolver.register(playerId, playerName);
        visualBridge.registerPlayer(playerId, event.getPlayerRef());
        cameraBridge.registerPlayer(playerId, event.getPlayerRef());

        runtime.onPlayerConnected(playerId);
    }

    private void onPlayerDisconnect(PlayerDisconnectEvent event) {
        String playerId = extractPlayerId(event);

        runtime.onPlayerDisconnected(playerId);

        visualBridge.unregisterPlayer(playerId);
        cameraBridge.unregisterPlayer(playerId);
        contextResolver.unregister(playerId);
    }

    private void onPlayerInteract(PlayerInteractEvent event) {
        var item = event.getItemInHand();
        if (item == null || item.isEmpty()) return;

        String itemId = item.getItemId();
        if (itemId == null) return;

        // Logging zur Item-ID-Verifizierung (wird ins Server-Log geschrieben)
        if (itemId.contains("AP_Camera")) {
            getLogger().at(java.util.logging.Level.INFO)
                    .log("AP_Camera erkannt – tatsächliche Item-ID: " + itemId);
        }

        boolean isCamera = ApCommand.CAMERA_ITEM_ID.equals(itemId)
                || ("HytaleAthena.AP_Camera:" + ApCommand.CAMERA_ITEM_ID).equals(itemId);

        if (isCamera && cameraService != null) {
            String playerId   = extractPlayerId(event);
            String playerName = contextResolver.resolve(playerId).playerName();
            try {
                cameraService.onCameraItemUse(playerId, playerName);
            } catch (Exception e) {
                getLogger().at(java.util.logging.Level.WARNING)
                        .withCause(e)
                        .log("Fehler bei Kamera-Aufnahme");
            }
        }
    }

    private void onPlayerChat(PlayerChatEvent event) {
        // TODO: Chat-Weiterleitung an aktive Editor-Sessions
    }

    // -----------------------------------------------------------------------
    // Hilfsmethoden
    // -----------------------------------------------------------------------

    private String extractPlayerId(PlayerConnectEvent event) {
        return event.getPlayerRef().getUuid().toString();
    }

    private String extractPlayerName(PlayerConnectEvent event) {
        return event.getPlayerRef().getUsername();
    }

    private String extractPlayerId(PlayerDisconnectEvent event) {
        return event.getPlayerRef().getUuid().toString();
    }

    private String extractPlayerId(PlayerInteractEvent event) {
        return event.getPlayerRef().getUuid().toString();
    }

    /** Liest das Screenshot-Verzeichnis aus AthenaPress/config.json. */
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
        // Fallback: Hytale-Standard-Screenshots-Ordner unter OneDrive/Pictures
        return Paths.get(System.getProperty("user.home"),
                "OneDrive", "Pictures", "Hytale Screenshots");
    }
}
