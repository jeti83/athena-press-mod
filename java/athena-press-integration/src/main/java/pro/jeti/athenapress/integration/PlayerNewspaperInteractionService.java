package pro.jeti.athenapress.integration;

import java.io.IOException;

public class PlayerNewspaperInteractionService {

    private final AthenaPressIntegrationPlugin plugin;

    public PlayerNewspaperInteractionService(AthenaPressIntegrationPlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin must not be null");
        }

        this.plugin = plugin;
    }

    public String handleOpenIssue(String playerId, String issueId) throws IOException {
        return plugin.onPlayerOpenNewspaper(playerId, issueId);
    }

    public String handleShowOverview(String playerId) {
        return plugin.onPlayerRequestOverview(playerId);
    }

    public String handleSelectArticle(String playerId, int articleNumber) {
        return plugin.onPlayerSelectArticle(playerId, articleNumber);
    }

    public String handleSelectArticle(String playerId, String articleId) {
        return plugin.onPlayerSelectArticle(playerId, articleId);
    }

    public void handleCloseIssue(String playerId) {
        plugin.onPlayerCloseNewspaper(playerId);
    }

    public String handleAction(
            PlayerNewspaperAction action,
            String playerId,
            String value
    ) throws IOException {
        return handleActionResponse(action, playerId, value).text();
    }

    public PlayerNewspaperResponse handleActionResponse(
            PlayerNewspaperAction action,
            String playerId,
            String value
    ) throws IOException {
        if (action == null) {
            return PlayerNewspaperResponse.of(
                    playerId,
                    null,
                    "Aktion konnte nicht verarbeitet werden.\n",
                    plugin.hasOpenNewspaper(playerId),
                    plugin.getOpenIssueId(playerId)
            );
        }

        return switch (action) {
            case OPEN_ISSUE -> response(
                    playerId,
                    action,
                    handleOpenIssue(playerId, value)
            );
            case SHOW_OVERVIEW -> response(
                    playerId,
                    action,
                    handleShowOverview(playerId)
            );
            case SELECT_ARTICLE_BY_NUMBER -> response(
                    playerId,
                    action,
                    handleSelectArticle(playerId, parseArticleNumber(value))
            );
            case SELECT_ARTICLE_BY_ID -> response(
                    playerId,
                    action,
                    handleSelectArticle(playerId, value)
            );
            case CLOSE_ISSUE -> {
                handleCloseIssue(playerId);
                yield PlayerNewspaperResponse.closed(
                        playerId,
                        action,
                        "Zeitung geschlossen.\n"
                );
            }
        };
    }

    private PlayerNewspaperResponse response(
            String playerId,
            PlayerNewspaperAction action,
            String text
    ) {
        return PlayerNewspaperResponse.of(
                playerId,
                action,
                text,
                plugin.hasOpenNewspaper(playerId),
                plugin.getOpenIssueId(playerId)
        );
    }

    private int parseArticleNumber(String value) {
        if (value == null || value.isBlank()) {
            return -1;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }
}