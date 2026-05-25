package pro.jeti.athenapress.plugin;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;

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

    // TODO: Item-ID im laufenden Spiel prüfen – ggf. "HytaleAthena.AP_Camera:Items/EditorTool/AP_Camera"
    static final String CAMERA_ITEM_ID = "Items/EditorTool/AP_Camera";

    private final HytaleNewspaperVisualRuntime<String> runtime;

    public ApCommand(HytaleNewspaperVisualRuntime<String> runtime) {
        super("ap", "Zeitung öffnen", false);
        setAllowsExtraArguments(true);
        this.runtime = runtime;
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        String playerId   = ctx.sender().getUuid().toString();
        String playerName = ctx.sender().getDisplayName();
        boolean admin     = ctx.sender().hasPermission(AP_ADMIN_PERMISSION);

        String input = ctx.getInputString().trim();
        String[] rawArgs = input.isEmpty() ? new String[0] : input.split("\\s+");
        // getInputString() kann "ap ausgabe" oder nur "ausgabe" liefern – defensiv normieren
        String[] args = (rawArgs.length > 0 && "ap".equalsIgnoreCase(rawArgs[0]))
                ? java.util.Arrays.copyOfRange(rawArgs, 1, rawArgs.length)
                : rawArgs;

        LOGGER.at(Level.INFO).log("[CMD /ap] id=" + playerId + " input=\"" + input + "\"");
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
            try {
                sendEditorText(ctx, plugin.onPlayerOpenLatestNewspaper(playerId));
            } catch (IOException | RuntimeException e) {
                LOGGER.at(Level.WARNING).withCause(e).log("Zeitung konnte nicht geöffnet werden");
                sendEditorText(ctx, "Fehler beim Öffnen der Zeitung: " + e.getMessage());
            }
            return;
        }

        String sub = args[0].toLowerCase();

        // Artikelnummer-Navigation: /ap 1, /ap 2, ...
        try {
            int articleNumber = Integer.parseInt(sub);
            sendEditorText(ctx, plugin.onPlayerSelectArticle(playerId, articleNumber));
            return;
        } catch (NumberFormatException ignored) {
            // kein Artikelindex – weiter zum Switch
        }

        switch (sub) {
            case "redaktion" ->
                startArticleEditor(ctx, plugin, playerId, playerName, admin);

            case "ausgabe" ->
                startIssueEditor(ctx, plugin, playerId, playerName, admin);

            case "weiter", "next" -> {
                try {
                    sendEditorText(ctx, plugin.onPlayerOpenNewspaper(
                            playerId, plugin.getOpenIssueId(playerId)));
                } catch (IOException | RuntimeException e) {
                    LOGGER.at(Level.WARNING).withCause(e).log("Seitennavigation fehlgeschlagen");
                    sendEditorText(ctx, "Navigation fehlgeschlagen: " + e.getMessage());
                }
            }

            case "zurueck", "zurück", "back" ->
                sendEditorText(ctx, plugin.onPlayerRequestOverview(playerId));

            case "schliessen", "schließen", "close" -> {
                plugin.onPlayerCloseNewspaper(playerId);
                sendEditorText(ctx, "Zeitung geschlossen.");
            }

            case "kamera", "camera" -> giveCamera(ctx);

            default -> {
                if (!admin) {
                    sendEditorText(ctx, "Keine Berechtigung. Benötigt: " + AP_ADMIN_PERMISSION);
                    return;
                }
                try {
                    String result = plugin.handleChefRedakteurCommand(args);
                    sendEditorText(ctx, result);
                } catch (IOException | RuntimeException e) {
                    LOGGER.at(Level.WARNING).withCause(e).log("Chef-Befehl fehlgeschlagen");
                    sendEditorText(ctx, "Befehl konnte nicht ausgeführt werden: " + e.getMessage());
                }
            }
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

    private void giveCamera(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendEditorText(ctx, "Dieser Befehl ist nur für Spieler.");
            return;
        }
        var playerRef = ctx.senderAsPlayerRef();
        var store     = playerRef.getStore();
        // ECS-Zugriff erfordert den WorldThread – über World als Executor dispatchen
        var world = store.getExternalData().getWorld();
        world.execute(() -> {
            var hotbar = store.getComponent(playerRef, InventoryComponent.Hotbar.getComponentType());
            var camera = new com.hypixel.hytale.server.core.inventory.ItemStack(CAMERA_ITEM_ID, 1);
            hotbar.getInventory().addItemStack(camera);
        });
        sendEditorText(ctx, "Kamera erhalten.");
    }

    private void sendEditorText(CommandContext ctx, String text) {
        if (text == null || text.isBlank()) return;
        LOGGER.at(Level.INFO).log("[AP→Spieler] " + text.replace("\n", "\\n"));
        ctx.sendMessage(Message.raw(text));
    }
}
