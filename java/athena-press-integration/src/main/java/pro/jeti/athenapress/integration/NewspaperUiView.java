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

    @Deprecated
    public NewspaperUiView {
        buttons = buttons == null ? List.of() : List.copyOf(buttons);
    }

    @Deprecated
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

    @Deprecated
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

    @Deprecated
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