package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NewspaperVisualRuntimeCache {

    private final Map<String, NewspaperPreviewIssue> previewIssuesByIssueId =
            new ConcurrentHashMap<>();

    public NewspaperPreviewIssue getOrCreatePreview(
            String issueId,
            PreviewIssueLoader loader
    ) throws IOException {
        if (!hasText(issueId)) {
            return null;
        }

        NewspaperPreviewIssue cachedPreview = previewIssuesByIssueId.get(issueId);
        if (cachedPreview != null) {
            return cachedPreview;
        }

        if (loader == null) {
            return null;
        }

        NewspaperPreviewIssue loadedPreview = loader.load(issueId);
        if (loadedPreview == null || !loadedPreview.hasSpreads()) {
            return loadedPreview;
        }

        NewspaperPreviewIssue existingPreview = previewIssuesByIssueId.putIfAbsent(
                issueId,
                loadedPreview
        );
        return existingPreview == null ? loadedPreview : existingPreview;
    }

    public void invalidateIssue(String issueId) {
        if (!hasText(issueId)) {
            return;
        }

        previewIssuesByIssueId.remove(issueId);
    }

    public void clear() {
        previewIssuesByIssueId.clear();
    }

    public int cachedPreviewCount() {
        return previewIssuesByIssueId.size();
    }

    public boolean hasCachedPreview(String issueId) {
        return hasText(issueId) && previewIssuesByIssueId.containsKey(issueId);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @FunctionalInterface
    public interface PreviewIssueLoader {
        NewspaperPreviewIssue load(String issueId) throws IOException;
    }
}
