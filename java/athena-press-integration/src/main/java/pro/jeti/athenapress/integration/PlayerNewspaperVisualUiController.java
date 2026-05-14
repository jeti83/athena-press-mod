package pro.jeti.athenapress.integration;

import java.io.IOException;

public class PlayerNewspaperVisualUiController {

    private static final String PREVIOUS_SPREAD_COMMAND = "visual_previous_spread";
    private static final String NEXT_SPREAD_COMMAND = "visual_next_spread";
    private static final String CURRENT_SPREAD_COMMAND = "visual_current_spread";

    private final AthenaPressIntegrationPlugin plugin;
    private final PlayerNewspaperVisualUiPort uiPort;
    private final PlayerNewspaperVisualViewFactory viewFactory;

    public PlayerNewspaperVisualUiController(
            AthenaPressIntegrationPlugin plugin,
            PlayerNewspaperVisualUiPort uiPort
    ) {
        this(plugin, uiPort, new PlayerNewspaperVisualViewFactory());
    }

    public PlayerNewspaperVisualUiController(
            AthenaPressIntegrationPlugin plugin,
            PlayerNewspaperVisualUiPort uiPort,
            PlayerNewspaperVisualViewFactory viewFactory
    ) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin must not be null");
        }

        if (uiPort == null) {
            throw new IllegalArgumentException("uiPort must not be null");
        }

        if (viewFactory == null) {
            throw new IllegalArgumentException("viewFactory must not be null");
        }

        this.plugin = plugin;
        this.uiPort = uiPort;
        this.viewFactory = viewFactory;
    }

    public void openIssue(String playerId, String issueId) {
        show(playerId, () -> plugin.onPlayerOpenVisualNewspaper(playerId, issueId));
    }

    public void showCurrentSpread(String playerId) {
        show(playerId, () -> plugin.onPlayerRequestCurrentVisualSpread(playerId));
    }

    public void showNextSpread(String playerId) {
        show(playerId, () -> plugin.onPlayerRequestNextVisualSpread(playerId));
    }

    public void showPreviousSpread(String playerId) {
        show(playerId, () -> plugin.onPlayerRequestPreviousVisualSpread(playerId));
    }

    public void closeIssue(String playerId) {
        plugin.onPlayerCloseVisualNewspaper(playerId);
        uiPort.close(playerId);
    }

    public void handleCommand(
            String playerId,
            PlayerNewspaperUiCommand command
    ) {
        if (command == null) {
            showCurrentSpread(playerId);
            return;
        }

        if (command.hasUiCommand()) {
            handleVisualCommand(playerId, command.uiCommand());
            return;
        }

        if (command.action() == PlayerNewspaperAction.OPEN_ISSUE) {
            openIssue(playerId, command.value());
            return;
        }

        if (command.action() == PlayerNewspaperAction.CLOSE_ISSUE) {
            closeIssue(playerId);
            return;
        }

        if (command.action() == PlayerNewspaperAction.SHOW_OVERVIEW) {
            showCurrentSpread(playerId);
            return;
        }

        showCurrentSpread(playerId);
    }

    private void handleVisualCommand(String playerId, String uiCommand) {
        String normalizedCommand = normalize(uiCommand);

        if (PREVIOUS_SPREAD_COMMAND.equals(normalizedCommand)) {
            showPreviousSpread(playerId);
            return;
        }

        if (NEXT_SPREAD_COMMAND.equals(normalizedCommand)) {
            showNextSpread(playerId);
            return;
        }

        if (CURRENT_SPREAD_COMMAND.equals(normalizedCommand)) {
            showCurrentSpread(playerId);
            return;
        }

        showCurrentSpread(playerId);
    }

    private void show(
            String playerId,
            VisualResponseLoader responseLoader
    ) {
        try {
            PlayerNewspaperVisualResponse response = responseLoader.load();
            uiPort.show(viewFactory.fromResponse(response));
        } catch (IOException exception) {
            uiPort.show(viewFactory.fromResponse(PlayerNewspaperVisualResponse.missing(
                    playerId,
                    "Visuelle Zeitung konnte nicht geladen werden."
            )));
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    @FunctionalInterface
    private interface VisualResponseLoader {
        PlayerNewspaperVisualResponse load() throws IOException;
    }
}
