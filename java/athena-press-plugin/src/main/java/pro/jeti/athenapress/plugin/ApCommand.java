package pro.jeti.athenapress.plugin;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import pro.jeti.athenapress.integration.AthenaPressIntegrationPlugin;
import pro.jeti.athenapress.integration.HytaleNewspaperVisualRuntime;

/**
 * Verarbeitet den Ingame-Befehl /ap.
 *
 * /ap                     → Hauptmenü öffnen (GUI)
 * /ap redaktion           → Artikel-Editor öffnen (GUI)
 * /ap ausgabe             → Ausgaben-Editor öffnen (GUI)
 * /ap kamera|camera       → Kamera-Item erhalten
 * /ap publish|archive|... → Chef-Redakteur-Befehle (nur Admins)
 * /ap <text>              → während aktiver Editor-Session: Eingabe weiterleiten
 */
public class ApCommand extends AbstractCommand {

    static final String AP_ADMIN_PERMISSION = "athenapress.admin";
    static final String CAMERA_ITEM_ID      = "Items/EditorTool/AP_Camera";

    private final HytaleNewspaperVisualRuntime<String> runtime;
    private final EditorUiBridge editorBridge;

    public ApCommand(HytaleNewspaperVisualRuntime<String> runtime, EditorUiBridge editorBridge) {
        super("ap", "Zeitung öffnen", false);
        setAllowsExtraArguments(true);
        this.runtime      = runtime;
        this.editorBridge = editorBridge;
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        String playerId   = ctx.sender().getUuid().toString();
        String playerName = ctx.sender().getDisplayName();
        boolean admin     = ctx.sender().hasPermission(AP_ADMIN_PERMISSION);

        String input   = ctx.getInputString().trim();
        String[] rawArgs = input.isEmpty() ? new String[0] : input.split("\\s+");
        String[] args  = (rawArgs.length > 0 && "ap".equalsIgnoreCase(rawArgs[0]))
                ? java.util.Arrays.copyOfRange(rawArgs, 1, rawArgs.length)
                : rawArgs;

        LOGGER.at(Level.INFO).log("[CMD /ap] id=" + playerId + " input=\"" + input + "\"");

        // Spieler lazy registrieren – PlayerConnectEvent-Stub matcht ggf. nicht
        // mit dem echten Hytale-Event. Hier beim ersten Befehl sicherstellen,
        // dass ein echter PlayerRef (aus dem EntityStore) hinterlegt ist.
        ensurePlayerRegistered(ctx, playerId);

        handleCommand(ctx, playerId, playerName, admin, args);
        return CompletableFuture.completedFuture(null);
    }

    private void handleCommand(
            CommandContext ctx,
            String playerId,
            String playerName,
            boolean admin,
            String[] args
    ) {
        AthenaPressIntegrationPlugin plugin = runtime.plugin();

        // Aktive Editor-Sessions bekommen rohen Text als Eingabe (Fallback falls GUI nicht offen)
        if (args.length > 0) {
            String rawInput = String.join(" ", args);

            if (plugin.hasActiveEditorSession(playerId)) {
                try {
                    var view = plugin.handleEditorInput(playerId, rawInput);
                    editorBridge.openOrUpdateArticleEditor(playerId, view);
                } catch (IOException e) {
                    LOGGER.at(Level.WARNING).withCause(e).log("Artikel-Editor-Eingabe fehlgeschlagen");
                    sendMsg(ctx, "Fehler: " + e.getMessage());
                }
                return;
            }
            if (plugin.hasActiveIssueEditorSession(playerId)) {
                try {
                    var view = plugin.handleIssueEditorInput(playerId, rawInput);
                    editorBridge.openOrUpdateIssueEditor(playerId, view);
                } catch (IOException e) {
                    LOGGER.at(Level.WARNING).withCause(e).log("Ausgaben-Editor-Eingabe fehlgeschlagen");
                    sendMsg(ctx, "Fehler: " + e.getMessage());
                }
                return;
            }
        }

        // /ap ohne Argumente → Hauptmenü
        if (args.length == 0) {
            editorBridge.openMainMenu(playerId, admin);
            return;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "redaktion" -> {
                var view = plugin.startArticleEditor(playerId, playerName, admin);
                editorBridge.openOrUpdateArticleEditor(playerId, view);
            }

            case "ausgabe" -> {
                try {
                    var view = plugin.startIssueEditor(playerId, playerName, admin);
                    editorBridge.openOrUpdateIssueEditor(playerId, view);
                } catch (IOException e) {
                    LOGGER.at(Level.WARNING).withCause(e).log("Ausgaben-Editor konnte nicht gestartet werden");
                    sendMsg(ctx, "Fehler beim Starten des Ausgaben-Editors: " + e.getMessage());
                }
            }

            case "kamera", "camera" -> editorBridge.openMainMenu(playerId, admin);
            // Kamera über Menü-Button "Kamera holen" – direkter Befehl öffnet auch das Menü

            case "schliessen", "schließen", "close" -> {
                plugin.onPlayerCloseNewspaper(playerId);
                sendMsg(ctx, "Zeitung geschlossen.");
            }

            default -> {
                if (!admin) {
                    sendMsg(ctx, "Keine Berechtigung. Benötigt: " + AP_ADMIN_PERMISSION);
                    return;
                }
                try {
                    String result = plugin.handleChefRedakteurCommand(args);
                    sendMsg(ctx, result);
                } catch (IOException | RuntimeException e) {
                    LOGGER.at(Level.WARNING).withCause(e).log("Chef-Befehl fehlgeschlagen");
                    sendMsg(ctx, "Befehl konnte nicht ausgeführt werden: " + e.getMessage());
                }
            }
        }
    }

    private void sendMsg(CommandContext ctx, String text) {
        if (text == null || text.isBlank()) return;
        LOGGER.at(Level.INFO).log("[AP→Spieler] " + text.replace("\n", "\\n"));
        ctx.sendMessage(Message.raw(text));
    }

    private void ensurePlayerRegistered(CommandContext ctx, String playerId) {
        if (editorBridge.getPlayerRef(playerId) != null) return;
        try {
            var entityRef = ctx.senderAsPlayerRef();
            var store     = entityRef.getStore();
            PlayerRef playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
            if (playerRef != null) {
                editorBridge.registerPlayer(playerId, playerRef);
                LOGGER.at(Level.INFO).log("[AP] PlayerRef lazy registriert für " + playerId);
            }
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e)
                    .log("[AP] PlayerRef-Lookup fehlgeschlagen für " + playerId);
        }
    }
}
