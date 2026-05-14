package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PlayerNewspaperVisualNavigationService {
    private static final String MISSING_ISSUE_TEXT = "Diese Ausgabe ist nicht verfügbar.";

    private final NewspaperPreviewPipelineService previewPipelineService;
    private final Map<String, PlayerNewspaperVisualSession> sessionsByPlayerId =
            new HashMap<>();

    public PlayerNewspaperVisualNavigationService(
            NewspaperPreviewPipelineService previewPipelineService
    ) {
        if (previewPipelineService == null) {
            throw new IllegalArgumentException("previewPipelineService must not be null");
        }

        this.previewPipelineService = previewPipelineService;
    }

    public PlayerNewspaperVisualResponse openIssue(
            String playerId,
            String issueId
    ) throws IOException {
        if (!hasText(playerId) || !hasText(issueId)) {
            closeIssue(playerId);
            return PlayerNewspaperVisualResponse.missing(playerId, MISSING_ISSUE_TEXT);
        }

        NewspaperPreviewIssue previewIssue = previewPipelineService.createPreview(issueId);
        if (previewIssue == null || !previewIssue.hasSpreads()) {
            closeIssue(playerId);
            return PlayerNewspaperVisualResponse.missing(playerId, MISSING_ISSUE_TEXT);
        }

        PlayerNewspaperVisualSession session =
                new PlayerNewspaperVisualSession(issueId, 0);
        sessionsByPlayerId.put(playerId, session);

        return responseFor(playerId, session, previewIssue);
    }

    public PlayerNewspaperVisualResponse showCurrentSpread(String playerId) throws IOException {
        PlayerNewspaperVisualSession session = sessionFor(playerId);
        if (session == null) {
            return PlayerNewspaperVisualResponse.missing(playerId, MISSING_ISSUE_TEXT);
        }

        return responseFor(playerId, session);
    }

    public PlayerNewspaperVisualResponse showNextSpread(String playerId) throws IOException {
        PlayerNewspaperVisualSession session = sessionFor(playerId);
        if (session == null) {
            return PlayerNewspaperVisualResponse.missing(playerId, MISSING_ISSUE_TEXT);
        }

        NewspaperPreviewIssue previewIssue =
                previewPipelineService.createPreview(session.issueId());
        if (previewIssue == null || !previewIssue.hasSpreads()) {
            closeIssue(playerId);
            return PlayerNewspaperVisualResponse.missing(playerId, MISSING_ISSUE_TEXT);
        }

        int nextSpreadIndex = Math.min(
                session.spreadIndex() + 1,
                previewIssue.spreads().size() - 1
        );
        PlayerNewspaperVisualSession nextSession = session.atSpread(nextSpreadIndex);
        sessionsByPlayerId.put(playerId, nextSession);

        return responseFor(playerId, nextSession, previewIssue);
    }

    public PlayerNewspaperVisualResponse showPreviousSpread(String playerId) throws IOException {
        PlayerNewspaperVisualSession session = sessionFor(playerId);
        if (session == null) {
            return PlayerNewspaperVisualResponse.missing(playerId, MISSING_ISSUE_TEXT);
        }

        NewspaperPreviewIssue previewIssue =
                previewPipelineService.createPreview(session.issueId());
        if (previewIssue == null || !previewIssue.hasSpreads()) {
            closeIssue(playerId);
            return PlayerNewspaperVisualResponse.missing(playerId, MISSING_ISSUE_TEXT);
        }

        PlayerNewspaperVisualSession previousSession =
                session.atSpread(Math.max(0, session.spreadIndex() - 1));
        sessionsByPlayerId.put(playerId, previousSession);

        return responseFor(playerId, previousSession, previewIssue);
    }

    public void closeIssue(String playerId) {
        if (!hasText(playerId)) {
            return;
        }

        sessionsByPlayerId.remove(playerId);
    }

    public boolean hasOpenIssue(String playerId) {
        return hasText(playerId) && sessionsByPlayerId.containsKey(playerId);
    }

    public String getOpenIssueId(String playerId) {
        PlayerNewspaperVisualSession session = sessionFor(playerId);
        return session == null ? null : session.issueId();
    }

    public int getCurrentSpreadIndex(String playerId) {
        PlayerNewspaperVisualSession session = sessionFor(playerId);
        return session == null ? -1 : session.spreadIndex();
    }

    public int getOpenSessionCount() {
        return sessionsByPlayerId.size();
    }

    private PlayerNewspaperVisualResponse responseFor(
            String playerId,
            PlayerNewspaperVisualSession session
    ) throws IOException {
        NewspaperPreviewIssue previewIssue =
                previewPipelineService.createPreview(session.issueId());
        if (previewIssue == null || !previewIssue.hasSpreads()) {
            closeIssue(playerId);
            return PlayerNewspaperVisualResponse.missing(playerId, MISSING_ISSUE_TEXT);
        }

        return responseFor(playerId, session, previewIssue);
    }

    private PlayerNewspaperVisualResponse responseFor(
            String playerId,
            PlayerNewspaperVisualSession session,
            NewspaperPreviewIssue previewIssue
    ) {
        int safeSpreadIndex = Math.min(
                Math.max(0, session.spreadIndex()),
                previewIssue.spreads().size() - 1
        );
        NewspaperPreviewSpread spread = previewIssue.spreads().get(safeSpreadIndex);

        return new PlayerNewspaperVisualResponse(
                playerId,
                previewIssue.issueId(),
                previewIssue.title(),
                safeSpreadIndex,
                previewIssue.spreads().size(),
                spread,
                true,
                ""
        );
    }

    private PlayerNewspaperVisualSession sessionFor(String playerId) {
        if (!hasText(playerId)) {
            return null;
        }

        return sessionsByPlayerId.get(playerId);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
