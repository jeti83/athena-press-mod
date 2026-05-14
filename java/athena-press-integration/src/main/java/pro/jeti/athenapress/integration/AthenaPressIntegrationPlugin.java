package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.nio.file.Path;

import pro.jeti.athenapress.AthenaPressCore;

public class AthenaPressIntegrationPlugin {
    private final IntegrationPluginMetadata metadata;
    private final NewspaperIntegrationGateway gateway;

    public AthenaPressIntegrationPlugin(Path athenaPressRoot) {
        this(
                IntegrationPluginMetadata.current(),
                new NewspaperIntegrationGateway(new AthenaPressCore(athenaPressRoot))
        );
    }

    public AthenaPressIntegrationPlugin(
            IntegrationPluginMetadata metadata,
            NewspaperIntegrationGateway gateway
    ) {
        if (metadata == null) {
            throw new IllegalArgumentException("metadata must not be null");
        }

        if (gateway == null) {
            throw new IllegalArgumentException("gateway must not be null");
        }

        this.metadata = metadata;
        this.gateway = gateway;
    }

    public IntegrationPluginMetadata metadata() {
        return metadata;
    }

    public String onPlayerOpenNewspaper(String playerId, String issueId) throws IOException {
        return gateway.openIssueForPlayer(playerId, issueId);
    }

    public String onPlayerRequestOverview(String playerId) {
        return gateway.showOverviewForPlayer(playerId);
    }

    public String onPlayerSelectArticle(String playerId, int articleNumber) {
        return gateway.showArticleForPlayer(playerId, articleNumber);
    }

    public String onPlayerSelectArticle(String playerId, String articleId) {
        return gateway.showArticleForPlayer(playerId, articleId);
    }

    public void onPlayerCloseNewspaper(String playerId) {
        gateway.closeIssueForPlayer(playerId);
    }

    public boolean hasOpenNewspaper(String playerId) {
        return gateway.hasOpenIssueForPlayer(playerId);
    }

    public String getOpenIssueId(String playerId) {
        return gateway.getOpenIssueIdForPlayer(playerId);
    }
}