package pro.jeti.athenapress.integration;

import java.io.IOException;

import pro.jeti.athenapress.model.GameIssueView;
import pro.jeti.athenapress.service.GameViewService;

public class NewspaperPreviewPipelineService {
    private static final String MISSING_PREVIEW_TEXT =
            "AthenaPress Preview\nDiese Ausgabe ist nicht verfügbar.";

    private final GameViewService gameViewService;
    private final NewspaperArticleCompositionService articleCompositionService;
    private final NewspaperPreviewService previewService;
    private final NewspaperPreviewTextRenderer previewTextRenderer;
    private final NewspaperVisualRuntimeCache runtimeCache;

    public NewspaperPreviewPipelineService(GameViewService gameViewService) {
        this(
                gameViewService,
                new NewspaperArticleCompositionService(),
                new NewspaperPreviewService(),
                new NewspaperPreviewTextRenderer(),
                new NewspaperVisualRuntimeCache()
        );
    }

    public NewspaperPreviewPipelineService(
            GameViewService gameViewService,
            NewspaperArticleCompositionService articleCompositionService,
            NewspaperPreviewService previewService,
            NewspaperPreviewTextRenderer previewTextRenderer
    ) {
        this(
                gameViewService,
                articleCompositionService,
                previewService,
                previewTextRenderer,
                new NewspaperVisualRuntimeCache()
        );
    }

    public NewspaperPreviewPipelineService(
            GameViewService gameViewService,
            NewspaperArticleCompositionService articleCompositionService,
            NewspaperPreviewService previewService,
            NewspaperPreviewTextRenderer previewTextRenderer,
            NewspaperVisualRuntimeCache runtimeCache
    ) {
        if (gameViewService == null) {
            throw new IllegalArgumentException("gameViewService must not be null");
        }

        this.gameViewService = gameViewService;
        this.articleCompositionService = articleCompositionService == null
                ? new NewspaperArticleCompositionService()
                : articleCompositionService;
        this.previewService = previewService == null
                ? new NewspaperPreviewService()
                : previewService;
        this.previewTextRenderer = previewTextRenderer == null
                ? new NewspaperPreviewTextRenderer()
                : previewTextRenderer;
        this.runtimeCache = runtimeCache == null
                ? new NewspaperVisualRuntimeCache()
                : runtimeCache;
    }

    public NewspaperPreviewIssue createPreview(String issueId) throws IOException {
        if (!hasText(issueId)) {
            return emptyPreview();
        }

        NewspaperPreviewIssue previewIssue = runtimeCache.getOrCreatePreview(
                issueId,
                this::loadPreview
        );
        return previewIssue == null ? emptyPreview() : previewIssue;
    }

    public String renderPreviewText(String issueId) throws IOException {
        NewspaperPreviewIssue previewIssue = createPreview(issueId);
        if (!previewIssue.hasSpreads()) {
            return MISSING_PREVIEW_TEXT;
        }

        return previewTextRenderer.render(previewIssue);
    }

    public void invalidatePreview(String issueId) {
        runtimeCache.invalidateIssue(issueId);
    }

    public void clearPreviewCache() {
        runtimeCache.clear();
    }

    public int cachedPreviewCount() {
        return runtimeCache.cachedPreviewCount();
    }

    public boolean hasCachedPreview(String issueId) {
        return runtimeCache.hasCachedPreview(issueId);
    }

    private NewspaperPreviewIssue loadPreview(String issueId) throws IOException {
        GameIssueView issueView = gameViewService.createPublishedIssueView(issueId);
        if (issueView == null) {
            return emptyPreview();
        }

        NewspaperVisualIssue visualIssue = articleCompositionService.compose(issueView);
        return previewService.createPreview(visualIssue);
    }

    private NewspaperPreviewIssue emptyPreview() {
        return new NewspaperPreviewIssue(
                null,
                "AthenaPress",
                NewspaperVisualTheme.defaultTheme(),
                NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                java.util.List.of()
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
