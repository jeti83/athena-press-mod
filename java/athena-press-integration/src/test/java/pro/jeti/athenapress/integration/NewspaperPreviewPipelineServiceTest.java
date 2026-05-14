package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import pro.jeti.athenapress.AthenaPressCore;

class NewspaperPreviewPipelineServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void rejectsMissingGameViewService() {
        assertThrows(IllegalArgumentException.class, () -> new NewspaperPreviewPipelineService(null));
    }

    @Test
    void createsPreviewFromPublishedIssueData() throws IOException {
        createPreviewDataSet();
        AthenaPressCore core = new AthenaPressCore(tempDir);
        NewspaperPreviewPipelineService service =
                new NewspaperPreviewPipelineService(core.getGameViewService());

        NewspaperPreviewIssue previewIssue = service.createPreview("issue_preview");

        assertTrue(previewIssue.hasSpreads());
        assertTrue(previewIssue.spreads().getFirst().leftPage().blocks().stream()
                .anyMatch(block -> block.type() == NewspaperVisualBlockType.HEADLINE));
        assertTrue(previewIssue.spreads().stream()
                .flatMap(spread -> java.util.stream.Stream.of(spread.leftPage(), spread.rightPage()))
                .filter(page -> page != null)
                .flatMap(page -> page.blocks().stream())
                .anyMatch(block -> block.content().contains("Der Marktstand blockiert")));
    }

    @Test
    void rendersPreviewTextFromPublishedIssueData() throws IOException {
        createPreviewDataSet();
        AthenaPressCore core = new AthenaPressCore(tempDir);
        NewspaperPreviewPipelineService service =
                new NewspaperPreviewPipelineService(core.getGameViewService());

        String text = service.renderPreviewText("issue_preview");

        assertTrue(text.contains("Athena Abendblatt Preview"));
        assertTrue(text.contains("FRONT_COVER"));
        assertTrue(text.contains("SUBHEADLINE"));
        assertTrue(text.contains("Der Marktstand blockiert"));
    }

    @Test
    void returnsHelpfulPreviewTextForMissingIssue() throws IOException {
        AthenaPressCore core = new AthenaPressCore(tempDir);
        NewspaperPreviewPipelineService service =
                new NewspaperPreviewPipelineService(core.getGameViewService());

        NewspaperPreviewIssue previewIssue = service.createPreview("missing");
        String text = service.renderPreviewText("missing");

        assertFalse(previewIssue.hasSpreads());
        assertTrue(text.contains("Diese Ausgabe ist nicht verfügbar."));
    }

    private void createPreviewDataSet() throws IOException {
        createFolders();

        Files.writeString(
                tempDir.resolve("articles").resolve("published").resolve("article_main.json"),
                """
                {
                  "id": "article_main",
                  "status": "published",
                  "categoryId": "stadtklatsch",
                  "title": "Der Marktstand blockiert den Ratssaal",
                  "subtitle": "Eine ernste Lage mit sehr dekorativem Verlauf",
                  "teaser": "Niemand kam vorbei.",
                  "summary": "Der Marktstand blockiert seit Sonnenaufgang den Eingang.",
                  "body": "Der Marktstand blockiert weiter den Ratssaal und zwingt alle Beteiligten zu ungeplanter Gelassenheit."
                }
                """
        );

        Files.writeString(
                tempDir.resolve("issues").resolve("published").resolve("issue_preview.json"),
                """
                {
                  "id": "issue_preview",
                  "status": "published",
                  "issueNumber": 12,
                  "title": "Athena Abendblatt",
                  "subtitle": "Serioes im Auftreten, frech im Inhalt",
                  "articles": [
                    "article_main"
                  ],
                  "cover": {
                    "mainArticleId": "article_main",
                    "image": "placeholders/market_stand.png"
                  }
                }
                """
        );
    }

    private void createFolders() throws IOException {
        Files.createDirectories(tempDir.resolve("articles").resolve("draft"));
        Files.createDirectories(tempDir.resolve("articles").resolve("published"));
        Files.createDirectories(tempDir.resolve("articles").resolve("archived"));

        Files.createDirectories(tempDir.resolve("issues").resolve("draft"));
        Files.createDirectories(tempDir.resolve("issues").resolve("published"));
        Files.createDirectories(tempDir.resolve("issues").resolve("archived"));
    }
}
