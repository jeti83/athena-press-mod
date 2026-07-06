package pro.jeti.athenapress.integration;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class NewspaperUiViewFactory {

    @Deprecated
    public NewspaperUiView fromResponse(PlayerNewspaperResponse response) {
        if (response == null) {
            return NewspaperUiView.error(
                    null,
                    "Zeitungsansicht konnte nicht erzeugt werden.\n"
            );
        }

        if (response.closeRequested()) {
            return NewspaperUiView.closed(response.playerId(), response.text());
        }

        NewspaperUiScreenType screenType = screenTypeFor(response);
        List<NewspaperUiButton> buttons = buttonsFor(response, screenType);

        return new NewspaperUiView(
                response.playerId(),
                screenType,
                titleFor(screenType),
                response.text(),
                response.openIssueId(),
                response.newspaperOpen(),
                false,
                buttons
        );
    }

    private NewspaperUiScreenType screenTypeFor(PlayerNewspaperResponse response) {
        if (!response.newspaperOpen()) {
            return NewspaperUiScreenType.MESSAGE;
        }

        if (response.action() == PlayerNewspaperAction.SELECT_ARTICLE_BY_NUMBER
                || response.action() == PlayerNewspaperAction.SELECT_ARTICLE_BY_ID) {
            return NewspaperUiScreenType.ARTICLE;
        }

        if (response.action() == PlayerNewspaperAction.OPEN_ISSUE
                || response.action() == PlayerNewspaperAction.SHOW_OVERVIEW) {
            return NewspaperUiScreenType.OVERVIEW;
        }

        return NewspaperUiScreenType.MESSAGE;
    }

    private String titleFor(NewspaperUiScreenType screenType) {
        return switch (screenType) {
            case CLOSED -> "AthenaPress";
            case ERROR -> "AthenaPress Fehler";
            case OVERVIEW -> "Zeitungsübersicht";
            case ARTICLE -> "Zeitungsartikel";
            case MESSAGE -> "AthenaPress";
        };
    }

    private List<NewspaperUiButton> buttonsFor(
            PlayerNewspaperResponse response,
            NewspaperUiScreenType screenType
    ) {
        List<NewspaperUiButton> buttons = new ArrayList<>();

        if (!response.newspaperOpen()) {
            buttons.add(NewspaperUiButton.danger(
                    "Schließen",
                    PlayerNewspaperUiCommand.closeIssue()
            ));
            return buttons;
        }

        if (screenType == NewspaperUiScreenType.ARTICLE) {
            buttons.add(NewspaperUiButton.secondary(
                    "Zur Übersicht",
                    PlayerNewspaperUiCommand.showOverview()
            ));
        }

        buttons.add(NewspaperUiButton.danger(
                "Zeitung schließen",
                PlayerNewspaperUiCommand.closeIssue()
        ));

        return buttons;
    }
}