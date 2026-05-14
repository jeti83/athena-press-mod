package pro.jeti.athenapress.integration;

public class PlayerNewspaperVisualInputMapper {

    public PlayerNewspaperUiCommand toUiCommand(PlayerNewspaperInputEvent event) {
        if (event == null) {
            return null;
        }

        String command = normalize(event.command());

        return switch (command) {
            case "open_issue", "open_visual_issue", "visual_open_issue",
                    "open", "oeffnen", "öffnen", "ap", "/ap" ->
                    PlayerNewspaperUiCommand.openIssue(event.value());

            case "visual_current_spread", "current_spread", "current",
                    "refresh", "aktualisieren" ->
                    PlayerNewspaperUiCommand.custom("visual_current_spread", event.value());

            case "visual_next_spread", "next_spread", "next", "weiter", "vor" ->
                    PlayerNewspaperUiCommand.custom("visual_next_spread", event.value());

            case "visual_previous_spread", "previous_spread", "previous",
                    "prev", "back", "zurueck", "zurück" ->
                    PlayerNewspaperUiCommand.custom("visual_previous_spread", event.value());

            case "close", "close_issue", "close_visual_issue",
                    "schliessen", "schließen" ->
                    PlayerNewspaperUiCommand.closeIssue();

            default -> null;
        };
    }

    private String normalize(String command) {
        if (command == null) {
            return "";
        }

        return command.trim().toLowerCase();
    }
}
