package pro.jeti.athenapress.plugin;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;

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
        super("ap", "Zeitung oeffnen", false);
        setAllowsExtraArguments(true);
        this.runtime      = runtime;
        this.editorBridge = editorBridge;
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        String playerId   = ctx.sender().getUuid().toString();
        String playerName = ctx.sender().getDisplayName();
        boolean admin     = ctx.sender().hasPermission(AP_ADMIN_PERMISSION);

        String input     = ctx.getInputString().trim();
        String[] rawArgs = input.isEmpty() ? new String[0] : input.split("\\s+");
        String[] args    = (rawArgs.length > 0 && "ap".equalsIgnoreCase(rawArgs[0]))
                ? java.util.Arrays.copyOfRange(rawArgs, 1, rawArgs.length)
                : rawArgs;

        sendDbg(ctx, "CMD id=" + playerId.substring(0, 8) + "... input=\"" + input + "\" admin=" + admin);

        if (!ensurePlayerRegistered(ctx, playerId)) {
            sendMsg(ctx, "[AP] Spieler-Referenz nicht gefunden - bitte Server-Log pruefen oder neu verbinden.");
            return CompletableFuture.completedFuture(null);
        }

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
            sendDbg(ctx, "openMainMenu admin=" + admin);
            editorBridge.openMainMenu(playerId, admin);
            return;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "redaktion" -> {
                sendDbg(ctx, "startArticleEditor");
                var view = plugin.startArticleEditor(playerId, playerName, admin);
                editorBridge.openOrUpdateArticleEditor(playerId, view);
            }

            case "ausgabe" -> {
                try {
                    sendDbg(ctx, "startIssueEditor");
                    var view = plugin.startIssueEditor(playerId, playerName, admin);
                    editorBridge.openOrUpdateIssueEditor(playerId, view);
                } catch (IOException e) {
                    LOGGER.at(Level.WARNING).withCause(e).log("Ausgaben-Editor konnte nicht gestartet werden");
                    sendMsg(ctx, "Fehler beim Starten des Ausgaben-Editors: " + e.getMessage());
                }
            }

            case "kamera", "camera" -> {
                sendDbg(ctx, "openMainMenu (via kamera)");
                editorBridge.openMainMenu(playerId, admin);
            }

            case "schliessen", "schließen", "close" -> {
                plugin.onPlayerCloseNewspaper(playerId);
                sendMsg(ctx, "Zeitung geschlossen.");
            }

            default -> {
                if (!admin) {
                    sendMsg(ctx, "Keine Berechtigung. Benoetigt: " + AP_ADMIN_PERMISSION);
                    return;
                }
                try {
                    String result = plugin.handleChefRedakteurCommand(args);
                    sendMsg(ctx, result);
                } catch (IOException | RuntimeException e) {
                    LOGGER.at(Level.WARNING).withCause(e).log("Chef-Befehl fehlgeschlagen");
                    sendMsg(ctx, "Befehl konnte nicht ausgefuehrt werden: " + e.getMessage());
                }
            }
        }
    }

    private void sendMsg(CommandContext ctx, String text) {
        if (text == null || text.isBlank()) return;
        ctx.sendMessage(Message.raw(text));
    }

    /** Debug-Nachricht: erscheint ingame in grau und im Server-Log. */
    private void sendDbg(CommandContext ctx, String info) {
        LOGGER.at(Level.INFO).log("[AP] " + info);
        ctx.sendMessage(Message.raw("§7[AP] " + info));
    }

    /**
     * Prüft ob der Spieler beim Connect-Event registriert wurde.
     * Fehlt der Eintrag, war der PlayerConnectEvent noch nicht verarbeitet –
     * der Spieler soll sich neu verbinden.
     */
    private boolean ensurePlayerRegistered(CommandContext ctx, String playerId) {
        if (editorBridge.isRegistered(playerId)) return true;
        LOGGER.at(Level.WARNING).log("[AP] PlayerRef nicht gefunden fuer " + playerId
                + " - neu verbinden oder Server-Log pruefen.");
        return false;
    }
}
