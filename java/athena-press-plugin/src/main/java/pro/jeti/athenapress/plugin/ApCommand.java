package pro.jeti.athenapress.plugin;

import java.util.concurrent.CompletableFuture;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import pro.jeti.athenapress.integration.HytaleNewspaperVisualRuntime;

/**
 * Verarbeitet den Ingame-Befehl /ap.
 *
 * Routing:
 *   /ap                     → neueste veröffentlichte Ausgabe öffnen
 *   /ap redaktion           → Artikel-Editor starten
 *   /ap ausgabe             → Ausgaben-Editor starten
 *   /ap publish|archive|... → Chef-Redakteur-Befehle (nur Admins)
 */
public class ApCommand extends AbstractCommand {

    private final HytaleNewspaperVisualRuntime<String> runtime;

    public ApCommand(HytaleNewspaperVisualRuntime<String> runtime) {
        super("ap", "Zeitung öffnen", false);
        this.runtime = runtime;
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        String playerId = ctx.sender().getUuid().toString();
        // TODO: Verifizieren ob getInputString() nur die Argumente (ohne "ap") liefert
        String input = ctx.getInputString().trim();
        String[] args = input.isEmpty() ? new String[0] : input.split("\\s+");

        handleCommand(playerId, args);
        return CompletableFuture.completedFuture(null);
    }

    private void handleCommand(String playerId, String[] args) {
        if (args.length == 0) {
            runtime.onPlayerChatCommand(playerId, "open", null);
            return;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "redaktion" ->
                runtime.onPlayerChatCommand(playerId, "editorial", null);

            case "ausgabe" ->
                runtime.onPlayerChatCommand(playerId, "issue-editor", null);

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
}
