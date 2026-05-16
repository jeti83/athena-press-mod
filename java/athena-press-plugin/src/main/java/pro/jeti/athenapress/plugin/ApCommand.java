package pro.jeti.athenapress.plugin;

import javax.annotation.Nonnull;

// TODO: Paketnamen gegen HytaleServer.jar prüfen
import com.hypixel.hytale.plugin.command.CommandBase;
import com.hypixel.hytale.plugin.command.CommandContext;

import pro.jeti.athenapress.integration.HytaleNewspaperVisualRuntime;
import pro.jeti.athenapress.integration.HytalePlayerContext;

/**
 * Verarbeitet den Ingame-Befehl /ap.
 *
 * Routing:
 *   /ap                     → neueste veröffentlichte Ausgabe öffnen
 *   /ap redaktion           → Artikel-Editor starten
 *   /ap ausgabe             → Ausgaben-Editor starten
 *   /ap publish|archive|... → Chef-Redakteur-Befehle (nur Admins)
 */
public class ApCommand extends CommandBase {

    private final HytaleNewspaperVisualRuntime<String> runtime;
    private final PlayerContextResolver resolver;

    public ApCommand(
            HytaleNewspaperVisualRuntime<String> runtime,
            PlayerContextResolver resolver
    ) {
        // TODO: Konstruktor-Signatur gegen HytaleServer.jar prüfen
        // Britakee-Guide: super("ap", "Zeitung öffnen", false)
        //   Parameter 3 = requiresOp (false = alle Spieler)
        super("ap", "Zeitung öffnen", false);
        this.runtime = runtime;
        this.resolver = resolver;
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        // TODO: Player-Objekt aus CommandContext extrahieren
        // Britakee-Guide: ctx.senderAs(Player.class)
        //
        // String playerId  = player.getUuid().toString();
        // String[] args    = ctx.getArgs();  // oder ctx.getArguments()

        // Bis zur Verifikation: Platzhalter-ID aus ctx.getSender()
        String playerId = ctx.getSender().toString();
        String[] args = ctx.getArgs();

        // TODO: world.execute() wrappen sobald World-Zugriff bekannt
        handleCommand(playerId, args);
    }

    private void handleCommand(String playerId, String[] args) {
        HytalePlayerContext player = resolver.resolve(playerId);

        if (args == null || args.length == 0) {
            // /ap → neueste Ausgabe öffnen
            runtime.onPlayerChatCommand(playerId, "open", null);
            return;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "redaktion" ->
                // /ap redaktion → Artikel-Editor
                runtime.onPlayerChatCommand(playerId, "editorial", null);

            case "ausgabe" ->
                // /ap ausgabe → Ausgaben-Editor
                runtime.onPlayerChatCommand(playerId, "issue-editor", null);

            case "weiter", "next" ->
                runtime.onPlayerChatCommand(playerId, "visual_next_spread", null);

            case "zurueck", "zurück", "back" ->
                runtime.onPlayerChatCommand(playerId, "visual_previous_spread", null);

            case "schliessen", "schließen", "close" ->
                runtime.onPlayerChatCommand(playerId, "visual_close", null);

            default -> {
                // Alle übrigen Argumente → Chef-Redakteur
                // /ap publish article_0001 / /ap archive issue_0002 etc.
                runtime.onPlayerChatCommand(playerId, "chef", String.join(" ", args));
            }
        }
    }
}
