package pro.jeti.athenapress.plugin;

import java.nio.file.Path;
import javax.annotation.Nonnull;

// Hytale Plugin API
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.event.PlayerConnectEvent;
import com.hypixel.hytale.event.PlayerDisconnectEvent;
import com.hypixel.hytale.event.PlayerInteractEvent;
import com.hypixel.hytale.event.PlayerChatEvent;

// AthenaPress Integration
import pro.jeti.athenapress.integration.AthenaPressIntegrationPlugin;
import pro.jeti.athenapress.integration.ConsoleNewspaperUiPort;
import pro.jeti.athenapress.integration.HytaleNewspaperVisualRuntime;

/**
 * Einstiegspunkt der AthenaPress Hytale-Mod.
 *
 * Verbindet die fertige AthenaPress-Logik (core + integration) mit
 * dem Hytale-Server über die drei Adapter-Stubs:
 *   - PlayerContextResolver   → HytalePlayerContextResolver
 *   - NewspaperVisualBridge   → HytaleNewspaperVisualUiBridge
 *   - CameraUiBridge          → HytaleCameraUiBridge
 *
 * Deployment: athena-press-plugin-*.jar → <server>/mods/
 * Datenpfad:  <server>/mods/pro.jeti_AthenaPress/AthenaPress/
 */
@SuppressWarnings("deprecation") // ConsoleNewspaperUiPort als Text-Fallback
public class AthenaPressPlugin extends JavaPlugin {

    private HytaleNewspaperVisualRuntime<String> runtime;
    private PlayerContextResolver contextResolver;
    private NewspaperVisualBridge visualBridge;
    private CameraUiBridge cameraBridge;

    public AthenaPressPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        // Datenpfad: <plugin-data>/AthenaPress/
        Path athenaPressRoot = getDataDirectory().resolve("AthenaPress");

        // Adapter und Bridge-Implementierungen
        contextResolver = new PlayerContextResolver();
        visualBridge    = new NewspaperVisualBridge();
        cameraBridge    = new CameraUiBridge();

        // AthenaPress Core + Integration
        AthenaPressIntegrationPlugin core = new AthenaPressIntegrationPlugin(athenaPressRoot);

        // Runtime zusammensetzen
        // ConsoleNewspaperUiPort = Text-Fallback bis Visual-Bridge vollständig ist
        runtime = new HytaleNewspaperVisualRuntime<>(
                core,
                new ConsoleNewspaperUiPort(),
                visualBridge,
                contextResolver
        );

        // Events registrieren
        getEventRegistry().register(PlayerConnectEvent.class, this::onPlayerConnect);
        getEventRegistry().register(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
        getEventRegistry().register(PlayerInteractEvent.class, this::onPlayerInteract);
        getEventRegistry().register(PlayerChatEvent.class, this::onPlayerChat);

        // Befehle registrieren
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
        runtime.onServerShutdown();
        getLogger().at(java.util.logging.Level.INFO).log("AthenaPress beendet.");
    }

    // -----------------------------------------------------------------------
    // Event-Handler
    // -----------------------------------------------------------------------

    private void onPlayerConnect(PlayerConnectEvent event) {
        // TODO: Methoden-Namen gegen HytaleServer.jar prüfen
        // Wahrscheinlich: event.getPlayerRef().getUuid().toString()
        //                 event.getPlayerRef().getUsername()
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
        // Kamera-Item-Erkennung: PlayerInteractEvent hat getItemInHand()
        // ItemStack aus der Hand → ID prüfen
        var item = event.getItemInHand();
        if (item == null || item.isEmpty()) return;

        if ("athena_kamera".equals(item.getItemId())) {
            String playerId   = extractPlayerId(event);
            String playerName = contextResolver.resolve(playerId).playerName();

            // Läuft auf dem Server – CameraScreenshotService übernimmt den Rest
            try {
                // TODO: athenaPressRoot-Pfad und screenshotDir aus Config lesen
                // runtime.plugin() gibt AthenaPressIntegrationPlugin zurück:
                // runtime.plugin().getGateway().getCameraService()
                //     .onCameraItemUse(playerId, playerName);
                getLogger().at(java.util.logging.Level.INFO)
                        .log("Kamera benutzt von " + playerName);
            } catch (Exception e) {
                getLogger().at(java.util.logging.Level.WARNING)
                        .withCause(e)
                        .log("Fehler bei Kamera-Aufnahme");
            }
        }
    }

    private void onPlayerChat(PlayerChatEvent event) {
        // Chat-Nachrichten während aktiver Editor-Sessions weiterleiten
        // TODO: event.getSender().getUuid().toString() für Player-ID
        //       event.getContent() für Eingabe-Text
        //
        // String playerId = extractPlayerId(event.getSender());
        // String input    = event.getContent();
        //
        // if (runtime.plugin().hasActiveEditorSession(playerId)) {
        //     event.setCancelled(true);
        //     ArticleEditorView view = runtime.plugin().handleEditorInput(playerId, input);
        //     // View als Nachricht an den Spieler senden
        // }
    }

    // -----------------------------------------------------------------------
    // Hilfsmethoden – TODO: gegen echte Hytale-API verifizieren
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
}
