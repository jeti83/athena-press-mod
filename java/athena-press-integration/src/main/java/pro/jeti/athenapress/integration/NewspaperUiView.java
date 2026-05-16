package pro.jeti.athenapress.integration;

import java.util.List;

@Deprecated
public record NewspaperUiView(
        String playerId,
        NewspaperUiScreenType screenType,
        String title,
        String body,
        String openIssueId,
        boolean newspaperOpen,
        boolean closeRequested,
        List<NewspaperUiButton> buttons
) {

    public NewspaperUiView {
        buttons = buttons == null ? List.of() : List.copyOf(buttons);
    }

    public static NewspaperUiView closed(String playerId, String body) {
        return new NewspaperUiView(
                playerId,
                NewspaperUiScreenType.CLOSED,
                "AthenaPress",
                body,
                null,
                false,
                true,
                List.of()
        );
    }

    public static NewspaperUiView error(String playerId, String body) {
        return new NewspaperUiView(
                playerId,
                NewspaperUiScreenType.ERROR,
                "AthenaPress Fehler",
                body,
                null,
                false,
                false,
                List.of(NewspaperUiButton.danger(
                        "Schließen",
                        PlayerNewspaperUiCommand.closeIssue()
                ))
        );
    }

    public static NewspaperUiView message(
            String playerId,
            String title,
            String body,
            String openIssueId,
            boolean newspaperOpen,
            List<NewspaperUiButton> buttons
    ) {
        return new NewspaperUiView(
                playerId,
                NewspaperUiScreenType.MESSAGE,
                title,
                body,
                openIssueId,
                newspaperOpen,
                false,
                buttons
        );
    }
}