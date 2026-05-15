package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pro.jeti.athenapress.AthenaPressCore;
import pro.jeti.athenapress.service.GameNewspaperSessionService;

public class NewspaperIntegrationGateway {
    private static final String MISSING_ISSUE_TEXT = "Diese Ausgabe ist nicht verfügbar.\n";
    private static final String MISSING_ARTICLE_TEXT = "Dieser Artikel ist in der Ausgabe nicht vorhanden.\n";

    private final AthenaPressCore core;
    private final NewspaperPreviewPipelineService previewPipelineService;
    private final PlayerNewspaperVisualNavigationService visualNavigationService;
    private final Map<String, GameNewspaperSessionService> sessionsByPlayerId = new HashMap<>();

    public NewspaperIntegrationGateway(AthenaPressCore core) {
        if (core == null) {
            throw new IllegalArgumentException("core must not be null");
        }

        this.core = core;
        this.previewPipelineService = new NewspaperPreviewPipelineService(core.getGameViewService());
        this.visualNavigationService =
                new PlayerNewspaperVisualNavigationService(previewPipelineService);
    }

    public String openIssueForPlayer(String playerId, String issueId) throws IOException {
        if (!hasText(playerId)) {
            return MISSING_ISSUE_TEXT;
        }

        if (!hasText(issueId)) {
            closeIssueForPlayer(playerId);
            return MISSING_ISSUE_TEXT;
        }

        GameNewspaperSessionService session = createSession();
        String overview = session.openIssue(issueId);

        if (session.hasOpenIssue()) {
            sessionsByPlayerId.put(playerId, session);
        } else {
            sessionsByPlayerId.remove(playerId);
        }

        return overview;
    }

    public String showOverviewForPlayer(String playerId) {
        if (!hasText(playerId)) {
            return MISSING_ISSUE_TEXT;
        }

        return getOrCreateSession(playerId).showOverview();
    }

    public String showArticleForPlayer(String playerId, int articleNumber) {
        if (!hasText(playerId)) {
            return MISSING_ISSUE_TEXT;
        }

        if (articleNumber < 1) {
            return MISSING_ARTICLE_TEXT;
        }

        return getOrCreateSession(playerId).showArticleByNumber(articleNumber);
    }

    public String showArticleForPlayer(String playerId, String articleId) {
        if (!hasText(playerId)) {
            return MISSING_ISSUE_TEXT;
        }

        if (!hasText(articleId)) {
            return MISSING_ARTICLE_TEXT;
        }

        return getOrCreateSession(playerId).showArticleById(articleId);
    }

    public void closeIssueForPlayer(String playerId) {
        if (!hasText(playerId)) {
            return;
        }

        GameNewspaperSessionService session = sessionsByPlayerId.remove(playerId);

        if (session != null) {
            session.closeIssue();
        }
    }

    public void closeAllIssues() {
        sessionsByPlayerId.values().forEach(GameNewspaperSessionService::closeIssue);
        sessionsByPlayerId.clear();
    }

    public boolean hasOpenIssueForPlayer(String playerId) {
        if (!hasText(playerId)) {
            return false;
        }

        GameNewspaperSessionService session = sessionsByPlayerId.get(playerId);
        return session != null && session.hasOpenIssue();
    }

    public String getOpenIssueIdForPlayer(String playerId) {
        if (!hasText(playerId)) {
            return null;
        }

        GameNewspaperSessionService session = sessionsByPlayerId.get(playerId);

        if (session == null) {
            return null;
        }

        return session.getOpenIssueId();
    }

    public int getOpenSessionCount() {
        return sessionsByPlayerId.size();
    }

    public PlayerNewspaperVisualResponse openVisualIssueForPlayer(
            String playerId,
            String issueId
    ) throws IOException {
        return visualNavigationService.openIssue(playerId, issueId);
    }

    public PlayerNewspaperVisualResponse showCurrentVisualSpreadForPlayer(
            String playerId
    ) throws IOException {
        return visualNavigationService.showCurrentSpread(playerId);
    }

    public PlayerNewspaperVisualResponse showNextVisualSpreadForPlayer(
            String playerId
    ) throws IOException {
        return visualNavigationService.showNextSpread(playerId);
    }

    public PlayerNewspaperVisualResponse showPreviousVisualSpreadForPlayer(
            String playerId
    ) throws IOException {
        return visualNavigationService.showPreviousSpread(playerId);
    }

    public PlayerNewspaperVisualResponse showVisualSpreadForPlayer(
            String playerId,
            int spreadIndex
    ) throws IOException {
        return visualNavigationService.showSpread(playerId, spreadIndex);
    }

    public void closeVisualIssueForPlayer(String playerId) {
        visualNavigationService.closeIssue(playerId);
    }

    public void closeAllVisualIssues() {
        visualNavigationService.closeAllIssues();
    }

    public boolean hasOpenVisualIssueForPlayer(String playerId) {
        return visualNavigationService.hasOpenIssue(playerId);
    }

    public int getOpenVisualSessionCount() {
        return visualNavigationService.getOpenSessionCount();
    }

    public NewspaperPreviewIssue createPreviewForIssue(String issueId) throws IOException {
        return previewPipelineService.createPreview(issueId);
    }

    public String renderPreviewForIssue(String issueId) throws IOException {
        return previewPipelineService.renderPreviewText(issueId);
    }

    public void invalidatePreviewForIssue(String issueId) {
        previewPipelineService.invalidatePreview(issueId);
    }

    public void clearPreviewCache() {
        previewPipelineService.clearPreviewCache();
    }

    public int getCachedPreviewCount() {
        return previewPipelineService.cachedPreviewCount();
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
