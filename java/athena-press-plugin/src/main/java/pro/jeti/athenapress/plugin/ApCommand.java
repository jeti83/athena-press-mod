package pro.jeti.athenapress.plugin;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

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

    private final HytaleNewspaperVisualRuntime<String> runtime;

    public ApCommand(HytaleNewspaperVisualRuntime<String> runtime) {
        super("ap", "Zeitung öffnen", false);
        this.runtime = runtime;
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        String playerId   = ctx.sender().getUuid().toString();
        String playerName = ctx.sender().getDisplayName();
        // TODO: isAdmin über Hytale-Permissions-API ermitteln
        boolean admin     = false;

        // TODO: Verifizieren ob getInputString() nur die Argumente (ohne "ap") liefert
        String input = ctx.getInputString().trim();
        String[] args = input.isEmpty() ? new String[0] : input.split("\\s+");

        handleCommand(playerId, playerName, admin, args);
        return CompletableFuture.completedFuture(null);
    }

    private void handleCommand(String playerId, String playerName, boolean admin, String[] args) {
        AthenaPressIntegrationPlugin plugin = runtime.plugin();

        // Aktive Editor-Sessions bekommen rohen Text als Eingabe
        if (args.length > 0) {
            String rawInput = String.join(" ", args);

            if (plugin.hasActiveEditorSession(playerId)) {
                forwardToArticleEditor(plugin, playerId, rawInput);
                return;
            }
            if (plugin.hasActiveIssueEditorSession(playerId)) {
                forwardToIssueEditor(plugin, playerId, rawInput);
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
                startArticleEditor(plugin, playerId, playerName, admin);

            case "ausgabe" ->
                startIssueEditor(plugin, playerId, playerName, admin);

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
            AthenaPressIntegrationPlugin plugin,
            String playerId,
            String playerName,
            boolean admin
    ) {
        try {
            ArticleEditorView view = plugin.startArticleEditor(playerId, playerName, admin);
            logEditorView(playerId, view.prompt());
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("Artikel-Editor konnte nicht gestartet werden");
        }
    }

    private void startIssueEditor(
            AthenaPressIntegrationPlugin plugin,
            String playerId,
            String playerName,
            boolean admin
    ) {
        try {
            IssueEditorView view = plugin.startIssueEditor(playerId, playerName, admin);
            logEditorView(playerId, view.prompt());
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("Ausgaben-Editor konnte nicht gestartet werden");
        }
    }

    private void forwardToArticleEditor(
            AthenaPressIntegrationPlugin plugin,
            String playerId,
            String input
    ) {
        try {
            ArticleEditorView view = plugin.handleEditorInput(playerId, input);
            logEditorView(playerId, view.message() != null ? view.message() : view.prompt());
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("Artikel-Editor-Eingabe fehlgeschlagen");
        }
    }

    private void forwardToIssueEditor(
            AthenaPressIntegrationPlugin plugin,
            String playerId,
            String input
    ) {
        try {
            IssueEditorView view = plugin.handleIssueEditorInput(playerId, input);
            logEditorView(playerId, view.message() != null ? view.message() : view.prompt());
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("Ausgaben-Editor-Eingabe fehlgeschlagen");
        }
    }

    private void logEditorView(String playerId, String text) {
        if (text == null || text.isBlank()) return;
        // TODO: Text als Spieler-Nachricht senden sobald Hytale-Nachrichten-API verfügbar ist
        LOGGER.at(Level.INFO).log("[AP-Editor] " + playerId + ": " + text);
    }
}
