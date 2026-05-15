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
                spreadStatusFor(response),
                response.spreadSignatures(),
                spreadMenuItemsFor(response),
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
                new NewspaperSpreadStatus(0, 0, "Doppelseite 1", "", 0, 0, false, false, false),
                List.of(),
                List.of(),
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

    private List<NewspaperSpreadMenuItem> spreadMenuItemsFor(
            PlayerNewspaperVisualResponse response
    ) {
        return response.spreadSignatures().stream()
                .map(signature -> new NewspaperSpreadMenuItem(
                        signature.spreadIndex(),
                        signature.label(),
                        signature.hint(),
                        signature.spreadIndex() == response.spreadIndex(),
                        NewspaperVisualUiCommands.selectSpread(signature.spreadIndex())
                ))
                .toList();
    }

    private NewspaperSpreadStatus spreadStatusFor(PlayerNewspaperVisualResponse response) {
        NewspaperSpreadSignature currentSignature = response.spreadSignatures().stream()
                .filter(signature -> signature.spreadIndex() == response.spreadIndex())
                .findFirst()
                .orElseGet(() -> response.spread() == null
                        ? new NewspaperSpreadSignature(
                                response.spreadIndex(),
                                "Doppelseite " + (response.spreadIndex() + 1),
                                "",
                                0,
                                0,
                                false,
                                false
                        )
                        : response.spread().signature());

        return new NewspaperSpreadStatus(
                response.spreadIndex(),
                response.totalSpreadCount(),
                currentSignature.label(),
                currentSignature.hint(),
                currentSignature.leftPageNumber(),
                currentSignature.rightPageNumber(),
                currentSignature.frontCover(),
                currentSignature.backCover(),
                !response.spreadSignatures().isEmpty()
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
