package pro.jeti.athenapress.plugin;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import pro.jeti.athenapress.integration.ArticleEditorView;
import pro.jeti.athenapress.integration.AthenaPressIntegrationPlugin;
import pro.jeti.athenapress.integration.HytaleNewspaperVisualRuntime;
import pro.jeti.athenapress.integration.IssueEditorView;

/**
 * Verarbeitet den Ingame-Befehl /ap.
 *
 * Routing:
 *   /ap                     → neueste veröffentlichte Ausgabe öffnen
 *   /ap redaktion           → Artikel-Editor starten
 *   /ap ausgabe             → Ausgaben-Editor starten
 *   /ap publish|archive|... → Chef-Redakteur-Befehle (nur Admins)
 *   /ap <text>              → während aktiver Editor-Session: Eingabe weiterleiten
 */
public class ApCommand extends AbstractCommand {

    static final String AP_ADMIN_PERMISSION = "athenapress.admin";

    private final HytaleNewspaperVisualRuntime<String> runtime;

    public ApCommand(HytaleNewspaperVisualRuntime<String> runtime) {
        super("ap", "Zeitung öffnen", false);
        this.runtime = runtime;
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        String playerId   = ctx.sender().getUuid().toString();
        String playerName = ctx.sender().getDisplayName();
        boolean admin     = ctx.sender().hasPermission(AP_ADMIN_PERMISSION);

        // TODO: Verifizieren ob getInputString() nur die Argumente (ohne "ap") liefert
        String input = ctx.getInputString().trim();
        String[] args = input.isEmpty() ? new String[0] : input.split("\\s+");

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

        // Aktive Editor-Sessions bekommen rohen Text als Eingabe
        if (args.length > 0) {
            String rawInput = String.join(" ", args);

            if (plugin.hasActiveEditorSession(playerId)) {
                forwardToArticleEditor(ctx, plugin, playerId, rawInput);
                return;
            }
            if (plugin.hasActiveIssueEditorSession(playerId)) {
                forwardToIssueEditor(ctx, plugin, playerId, rawInput);
                return;
            }
        }

        if (args.length == 0) {
            runtime.onPlayerChatCommand(playerId, "open", null);
            return;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "redaktion" ->
                startArticleEditor(ctx, plugin, playerId, playerName, admin);

            case "ausgabe" ->
                startIssueEditor(ctx, plugin, playerId, playerName, admin);

            case "weiter", "next" ->
                runtime.onPlayerChatCommand(playerId, "visual_next_spread", null);

            case "zurueck", "zurück", "back" ->
                runtime.onPlayerChatCommand(playerId, "visual_previous_spread", null);

            case "schliessen", "schließen", "close" ->
                runtime.onPlayerChatCommand(playerId, "visual_close", null);

            default ->
                runtime.onPlayerChatCommand(playerId, "chef", String.join(" ", args));
        }
    }

    // -----------------------------------------------------------------------
    // Editor-Hilfsmethoden
    // -----------------------------------------------------------------------

    private void startArticleEditor(
            CommandContext ctx,
            AthenaPressIntegrationPlugin plugin,
            String playerId,
            String playerName,
            boolean admin
    ) {
        ArticleEditorView view = plugin.startArticleEditor(playerId, playerName, admin);
        sendEditorText(ctx, view.prompt());
    }

    private void startIssueEditor(
            CommandContext ctx,
            AthenaPressIntegrationPlugin plugin,
            String playerId,
            String playerName,
            boolean admin
    ) {
        try {
            IssueEditorView view = plugin.startIssueEditor(playerId, playerName, admin);
            sendEditorText(ctx, view.prompt());
        } catch (IOException e) {
            LOGGER.at(Level.WARNING).withCause(e).log("Ausgaben-Editor konnte nicht gestartet werden");
        }
    }

    private void forwardToArticleEditor(
            CommandContext ctx,
            AthenaPressIntegrationPlugin plugin,
            String playerId,
            String input
    ) {
        try {
            ArticleEditorView view = plugin.handleEditorInput(playerId, input);
            String text = view.message() != null && !view.message().isBlank()
                    ? view.message() : view.prompt();
            sendEditorText(ctx, text);
        } catch (IOException e) {
            LOGGER.at(Level.WARNING).withCause(e).log("Artikel-Editor-Eingabe fehlgeschlagen");
        }
    }

    private void forwardToIssueEditor(
            CommandContext ctx,
            AthenaPressIntegrationPlugin plugin,
            String playerId,
            String input
    ) {
        try {
            IssueEditorView view = plugin.handleIssueEditorInput(playerId, input);
            String text = view.message() != null && !view.message().isBlank()
                    ? view.message() : view.prompt();
            sendEditorText(ctx, text);
        } catch (IOException e) {
            LOGGER.at(Level.WARNING).withCause(e).log("Ausgaben-Editor-Eingabe fehlgeschlagen");
        }
    }

    private void sendEditorText(CommandContext ctx, String text) {
        if (text == null || text.isBlank()) return;
        ctx.sendMessage(Message.raw(text));
    }
}
