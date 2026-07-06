package pro.jeti.athenapress.integration;

import java.io.IOException;

@Deprecated
public class PlayerNewspaperUiController {

    private final PlayerNewspaperInteractionService interactionService;
    private final PlayerNewspaperUiPort uiPort;
    private final NewspaperUiViewFactory viewFactory;

    @Deprecated
    public PlayerNewspaperUiController(
            PlayerNewspaperInteractionService interactionService,
            PlayerNewspaperUiPort uiPort
    ) {
        this(interactionService, uiPort, new NewspaperUiViewFactory());
    }

    @Deprecated
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

    @Deprecated
    public void openIssue(String playerId, String issueId) {
        handleCommand(playerId, PlayerNewspaperUiCommand.openIssue(issueId));
    }

    @Deprecated
    public void showOverview(String playerId) {
        handleCommand(playerId, PlayerNewspaperUiCommand.showOverview());
    }

    @Deprecated
    public void selectArticle(String playerId, int articleNumber) {
        handleCommand(playerId, PlayerNewspaperUiCommand.selectArticle(articleNumber));
    }

    @Deprecated
    public void selectArticle(String playerId, String articleId) {
        handleCommand(playerId, PlayerNewspaperUiCommand.selectArticle(articleId));
    }

    @Deprecated
    public void closeIssue(String playerId) {
        handleCommand(playerId, PlayerNewspaperUiCommand.closeIssue());
    }

    @Deprecated
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