package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pro.jeti.athenapress.AthenaPressCore;
import pro.jeti.athenapress.service.GameNewspaperSessionService;

public class NewspaperIntegrationGateway {
    private final AthenaPressCore core;
    private final Map<String, GameNewspaperSessionService> sessionsByPlayerId = new HashMap<>();

    public NewspaperIntegrationGateway(AthenaPressCore core) {
        this.core = core;
    }

    public String openIssueForPlayer(String playerId, String issueId) throws IOException {
        GameNewspaperSessionService session = createSession();
        sessionsByPlayerId.put(playerId, session);

        return session.openIssue(issueId);
    }

    public String showOverviewForPlayer(String playerId) {
        return getOrCreateSession(playerId).showOverview();
    }

    public String showArticleForPlayer(String playerId, int articleNumber) {
        return getOrCreateSession(playerId).showArticleByNumber(articleNumber);
    }

    public String showArticleForPlayer(String playerId, String articleId) {
        return getOrCreateSession(playerId).showArticleById(articleId);
    }

    public void closeIssueForPlayer(String playerId) {
        GameNewspaperSessionService session = sessionsByPlayerId.remove(playerId);

        if (session != null) {
            session.closeIssue();
        }
    }

    public boolean hasOpenIssueForPlayer(String playerId) {
        GameNewspaperSessionService session = sessionsByPlayerId.get(playerId);
        return session != null && session.hasOpenIssue();
    }

    public String getOpenIssueIdForPlayer(String playerId) {
        GameNewspaperSessionService session = sessionsByPlayerId.get(playerId);

        if (session == null) {
            return null;
        }

        return session.getOpenIssueId();
    }

    private GameNewspaperSessionService getOrCreateSession(String playerId) {
        return sessionsByPlayerId.computeIfAbsent(playerId, ignored -> createSession());
    }

    private GameNewspaperSessionService createSession() {
        return new GameNewspaperSessionService(
                core.getGameViewService(),
                core.getGameTextRendererService()
        );
    }
}