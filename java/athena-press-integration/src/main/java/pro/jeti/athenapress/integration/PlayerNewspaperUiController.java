package pro.jeti.athenapress.integration;

import java.io.IOException;

public class PlayerNewspaperUiController {

    private final PlayerNewspaperInteractionService interactionService;
    private final PlayerNewspaperUiPort uiPort;

    public PlayerNewspaperUiController(
            PlayerNewspaperInteractionService interactionService,
            PlayerNewspaperUiPort uiPort
    ) {
        if (interactionService == null) {
            throw new IllegalArgumentException("interactionService must not be null");
        }

        if (uiPort == null) {
            throw new IllegalArgumentException("uiPort must not be null");
        }

        this.interactionService = interactionService;
        this.uiPort = uiPort;
    }

    public void handleCommand(
            String playerId,
            PlayerNewspaperUiCommand command
    ) {
        try {
            PlayerNewspaperResponse response =
                    interactionService.handleUiCommand(playerId, command);

            uiPort.show(response);

        } catch (IOException exception) {

            uiPort.show(
                    PlayerNewspaperResponse.of(
                            playerId,
                            null,
                            "Zeitung konnte nicht geladen werden.\n",
                            false,
                            null
                    )
            );
        }
    }
}