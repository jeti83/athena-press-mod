package pro.jeti.athenapress.integration;

import java.util.List;

public class ChefRedakteurCommandService {

    private static final List<String> PUBLISH_ARGS  = List.of("publish", "veroeffentlichen");
    private static final List<String> ARCHIVE_ARGS  = List.of("archive", "archivieren");
    private static final List<String> DELIVER_ARGS  = List.of("deliver", "zustellen");
    private static final List<String> DELETE_ARGS   = List.of("delete", "loeschen");
    private static final List<String> VALIDATE_ARGS = List.of("validate", "pruefen");
    private static final List<String> STATUS_ARGS   = List.of("status", "uebersicht");
    private static final List<String> LIST_ARGS     = List.of("list", "liste");

    public ChefRedakteurCommand parse(String[] args) {
        if (args == null || args.length == 0) {
            return new ChefRedakteurCommand(ChefRedakteurCommandType.STATUS, null);
        }

        String verb = args[0].toLowerCase();
        String id   = args.length >= 2 ? args[1] : null;

        if (matches(verb, PUBLISH_ARGS))  return new ChefRedakteurCommand(ChefRedakteurCommandType.PUBLISH,   id);
        if (matches(verb, ARCHIVE_ARGS))  return new ChefRedakteurCommand(ChefRedakteurCommandType.ARCHIVE,   id);
        if (matches(verb, DELIVER_ARGS))  return new ChefRedakteurCommand(ChefRedakteurCommandType.DELIVER,   id);
        if (matches(verb, DELETE_ARGS))   return new ChefRedakteurCommand(ChefRedakteurCommandType.DELETE,    id);
        if (matches(verb, VALIDATE_ARGS)) return new ChefRedakteurCommand(ChefRedakteurCommandType.VALIDATE,  id);
        if (matches(verb, STATUS_ARGS))   return new ChefRedakteurCommand(ChefRedakteurCommandType.STATUS,    null);
        if (matches(verb, LIST_ARGS))     return new ChefRedakteurCommand(ChefRedakteurCommandType.LIST,      null);

        return new ChefRedakteurCommand(ChefRedakteurCommandType.UNKNOWN, args[0]);
    }

    public String helpText() {
        return """

                AthenaPress – Chef-Redakteur-Befehle
                -------------------------------------
                /ap publish <id>        / veroeffentlichen   Artikel oder Ausgabe veroeffentlichen
                /ap archive <id>        / archivieren        Artikel oder Ausgabe archivieren
                /ap deliver <id>        / zustellen          Ausgabe an Abonnenten zustellen
                /ap delete <id>         / loeschen           Entwurf loeschen (nur drafts)
                /ap validate <id>       / pruefen            Ausgabe validieren
                /ap status              / uebersicht         Kurzuebersicht
                /ap list                / liste              Alle Inhalte auflisten

                ID-Format: article_0001 fuer Artikel, issue_0002 fuer Ausgaben
                """;
    }

    private boolean matches(String input, List<String> aliases) {
        return aliases.stream().anyMatch(input::equalsIgnoreCase);
    }
}
