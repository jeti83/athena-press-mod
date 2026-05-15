package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class NewspaperVisualRuntimeCacheTest {

    @Test
    void cachesLoadedPreviewByIssueId() throws IOException {
        NewspaperVisualRuntimeCache cache = new NewspaperVisualRuntimeCache();
        AtomicInteger loadCount = new AtomicInteger();

        NewspaperPreviewIssue first = cache.getOrCreatePreview("issue_test", issueId -> {
            loadCount.incrementAndGet();
            return previewIssue(issueId);
        });
        NewspaperPreviewIssue second = cache.getOrCreatePreview("issue_test", issueId -> {
            loadCount.incrementAndGet();
            return previewIssue(issueId);
        });

        assertSame(first, second);
        assertEquals(1, loadCount.get());
        assertEquals(1, cache.cachedPreviewCount());
        assertTrue(cache.hasCachedPreview("issue_test"));
    }

    @Test
    void doesNotCacheEmptyPreview() throws IOException {
        NewspaperVisualRuntimeCache cache = new NewspaperVisualRuntimeCache();
        AtomicInteger loadCount = new AtomicInteger();

        cache.getOrCreatePreview("missing", issueId -> {
            loadCount.incrementAndGet();
            return emptyPreview();
        });
        cache.getOrCreatePreview("missing", issueId -> {
            loadCount.incrementAndGet();
            return emptyPreview();
        });

        assertEquals(2, loadCount.get());
        assertEquals(0, cache.cachedPreviewCount());
        assertFalse(cache.hasCachedPreview("missing"));
    }

    @Test
    void invalidatesSingleIssuePreview() throws IOException {
        NewspaperVisualRuntimeCache cache = new NewspaperVisualRuntimeCache();

        cache.getOrCreatePreview("issue_test", this::previewIssue);
        cache.invalidateIssue("issue_test");

        assertEquals(0, cache.cachedPreviewCount());
        assertFalse(cache.hasCachedPreview("issue_test"));
    }

    @Test
    void clearsAllPreviewEntries() throws IOException {
        NewspaperVisualRuntimeCache cache = new NewspaperVisualRuntimeCache();

        cache.getOrCreatePreview("issue_one", this::previewIssue);
        cache.getOrCreatePreview("issue_two", this::previewIssue);
        cache.clear();

        assertEquals(0, cache.cachedPreviewCount());
    }

    private NewspaperPreviewIssue previewIssue(String issueId) {
        return new NewspaperPreviewIssue(
                issueId,
                "AthenaPress",
                NewspaperVisualTheme.defaultTheme(),
                NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                List.of(new NewspaperPreviewSpread(
                        0,
                        new NewspaperPreviewPage(
                                1,
                                "Titelseite",
                                NewspaperPageRole.FRONT_COVER,
                                NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                                List.of(new NewspaperPreviewBlock(
                                        NewspaperVisualBlockType.HEADLINE,
                                        "AthenaPress",
                                        null,
                                        0,
                                        0,
                                        2,
                                        2
                                ))
                        ),
                        null,
                        List.of()
                ))
        );
    }

    private NewspaperPreviewIssue emptyPreview() {
        return new NewspaperPreviewIssue(
                null,
                "AthenaPress",
                NewspaperVisualTheme.defaultTheme(),
                NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                List.of()
        );
    }
}
