package pro.jeti.athenapress.integration;

import java.io.IOException;

@Deprecated
public class PlayerNewspaperUiController {

    private final PlayerNewspaperInteractionService interactionService;
    private final PlayerNewspaperUiPort uiPort;
    private final NewspaperUiViewFactory viewFactory;

    public PlayerNewspaperUiController(
            PlayerNewspaperInteractionService interactionService,
            PlayerNewspaperUiPort uiPort
    ) {
        this(interactionService, uiPort, new NewspaperUiViewFactory());
    }

    public PlayerNewspaperUiController(
            PlayerNewspaperInteractionService interactionService,
            PlayerNewspaperUiPort uiPort,
            NewspaperUiViewFactory viewFactory
    ) {
        if (interactionService == null) {
            throw new IllegalArgumentException("interactionService must not be null");
        }

        if (uiPort == null) {
            throw new IllegalArgumentException("uiPort must not be null");
        }

        if (viewFactory == null) {
            throw new IllegalArgumentException("viewFactory must not be null");
        }

        this.interactionService = interactionService;
        this.uiPort = uiPort;
        this.viewFactory = viewFactory;
    }

    public void openIssue(String playerId, String issueId) {
        handleCommand(playerId, PlayerNewspaperUiCommand.openIssue(issueId));
    }

    public void showOverview(String playerId) {
        handleCommand(playerId, PlayerNewspaperUiCommand.showOverview());
    }

    public void selectArticle(String playerId, int articleNumber) {
        handleCommand(playerId, PlayerNewspaperUiCommand.selectArticle(articleNumber));
    }

    public void selectArticle(String playerId, String articleId) {
        handleCommand(playerId, PlayerNewspaperUiCommand.selectArticle(articleId));
    }

    public void closeIssue(String playerId) {
        handleCommand(playerId, PlayerNewspaperUiCommand.closeIssue());
    }

    public void handleCommand(
            String playerId,
            PlayerNewspaperUiCommand command
    ) {
        try {
            PlayerNewspaperResponse response =
                    interactionService.handleUiCommand(playerId, command);

            uiPort.show(viewFactory.fromResponse(response));

        } catch (IOException exception) {

            uiPort.show(NewspaperUiView.error(
                    playerId,
                    "Zeitung konnte nicht geladen werden.\n"
            ));
        }
    }
}