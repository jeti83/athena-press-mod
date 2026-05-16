package pro.jeti.athenapress.integration;

@Deprecated
public class PlayerNewspaperInputMapper {

    public PlayerNewspaperUiCommand toUiCommand(PlayerNewspaperInputEvent event) {
        if (event == null) {
            return null;
        }

        String command = normalize(event.command());

        return switch (command) {
            case "open_issue", "open", "oeffnen", "öffnen" ->
                    PlayerNewspaperUiCommand.openIssue(event.value());

            case "overview", "show_overview", "zurueck", "zurück", "back" ->
                    PlayerNewspaperUiCommand.showOverview();

            case "close", "close_issue", "schliessen", "schließen" ->
                    PlayerNewspaperUiCommand.closeIssue();

            case "select_article", "article", "artikel" ->
                    selectArticle(event.value());

            default -> null;
        };
    }

    private PlayerNewspaperUiCommand selectArticle(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmedValue = value.trim();

        try {
            return PlayerNewspaperUiCommand.selectArticle(Integer.parseInt(trimmedValue));
        } catch (NumberFormatException exception) {
            return PlayerNewspaperUiCommand.selectArticle(trimmedValue);
        }
    }

    private String normalize(String command) {
        if (command == null) {
            return "";
        }

        return command.trim().toLowerCase();
    }
}