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

    public String handleOpenIssue(
            String playerId,
            String issueId
    ) throws IOException {
        return plugin.onPlayerOpenNewspaper(playerId, issueId);
    }

    public String handleShowOverview(String playerId) {
        return plugin.onPlayerRequestOverview(playerId);
    }

    public String handleSelectArticle(
            String playerId,
            int articleNumber
    ) {
        return plugin.onPlayerSelectArticle(playerId, articleNumber);
    }

    public String handleSelectArticle(
            String playerId,
            String articleId
    ) {
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
        if (action == null) {
            return "Aktion konnte nicht verarbeitet werden.\n";
        }

        return switch (action) {
            case OPEN_ISSUE ->
                    handleOpenIssue(playerId, value);

            case SHOW_OVERVIEW ->
                    handleShowOverview(playerId);

            case SELECT_ARTICLE_BY_NUMBER ->
                    handleSelectArticle(playerId, parseArticleNumber(value));

            case SELECT_ARTICLE_BY_ID ->
                    handleSelectArticle(playerId, value);

            case CLOSE_ISSUE -> {
                handleCloseIssue(playerId);
                yield "Zeitung geschlossen.\n";
            }
        };
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