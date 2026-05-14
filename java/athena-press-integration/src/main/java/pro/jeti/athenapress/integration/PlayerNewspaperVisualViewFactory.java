package pro.jeti.athenapress.integration;

import java.util.ArrayList;
import java.util.List;

public class PlayerNewspaperVisualViewFactory {

    public PlayerNewspaperVisualView fromResponse(PlayerNewspaperVisualResponse response) {
        if (response == null) {
            return messageView(
                    null,
                    "Visuelle Zeitungsansicht konnte nicht erzeugt werden."
            );
        }

        if (!response.newspaperOpen() || !response.hasSpread()) {
            return messageView(response.playerId(), response.message());
        }

        NewspaperPreviewSpread spread = response.spread();

        return new PlayerNewspaperVisualView(
                response.playerId(),
                response.issueId(),
                response.title(),
                response.spreadIndex(),
                response.totalSpreadCount(),
                spread.leftPage(),
                spread.rightPage(),
                true,
                response.hasPreviousSpread(),
                response.hasNextSpread(),
                "",
                buttonsFor(response)
        );
    }

    private PlayerNewspaperVisualView messageView(
            String playerId,
            String message
    ) {
        return new PlayerNewspaperVisualView(
                playerId,
                null,
                "AthenaPress",
                0,
                0,
                null,
                null,
                false,
                false,
                false,
                hasText(message) ? message : "Diese Ausgabe ist nicht verfügbar.",
                List.of(NewspaperUiButton.danger(
                        "Schließen",
                        PlayerNewspaperUiCommand.closeIssue()
                ))
        );
    }

    private List<NewspaperUiButton> buttonsFor(PlayerNewspaperVisualResponse response) {
        List<NewspaperUiButton> buttons = new ArrayList<>();

        if (response.hasPreviousSpread()) {
            buttons.add(NewspaperUiButton.secondary(
                    "Zurückblättern",
                    NewspaperVisualUiCommands.previousSpread()
            ));
        }

        if (response.hasNextSpread()) {
            buttons.add(NewspaperUiButton.primary(
                    "Weiterblättern",
                    NewspaperVisualUiCommands.nextSpread()
            ));
        }

        buttons.add(NewspaperUiButton.secondary(
                "Übersicht",
                PlayerNewspaperUiCommand.showOverview()
        ));
        buttons.add(NewspaperUiButton.danger(
                "Zeitung schließen",
                PlayerNewspaperUiCommand.closeIssue()
        ));

        return buttons;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
