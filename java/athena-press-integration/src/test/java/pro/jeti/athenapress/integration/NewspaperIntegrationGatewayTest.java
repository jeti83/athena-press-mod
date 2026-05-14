package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import pro.jeti.athenapress.AthenaPressCore;

class NewspaperIntegrationGatewayTest {

    @TempDir
    Path tempDir;

    @Test
    void rejectsMissingCore() {
        assertThrows(IllegalArgumentException.class, () -> new NewspaperIntegrationGateway(null));
    }

    @Test
    void opensIssueForPlayerAndShowsArticle() throws IOException {
        createMinimalDataSet();

        NewspaperIntegrationGateway gateway = createGateway();

        String overview = gateway.openIssueForPlayer("player-1", "issue_test");
        String article = gateway.showArticleForPlayer("player-1", 1);

        assertTrue(gateway.hasOpenIssueForPlayer("player-1"));
        assertEquals("issue_test", gateway.getOpenIssueIdForPlayer("player-1"));
        assertEquals(1, gateway.getOpenSessionCount());
        assertTrue(overview.contains("Athena Testausgabe"));
        assertTrue(overview.contains("[1] Erster Spielartikel"));
        assertTrue(article.contains("Dies ist der lesbare Artikeltext."));
    }

    @Test
    void keepsPlayerSessionsSeparate() throws IOException {
        createMinimalDataSet();

        NewspaperIntegrationGateway gateway = createGateway();

        gateway.openIssueForPlayer("player-1", "issue_test");

        assertTrue(gateway.hasOpenIssueForPlayer("player-1"));
        assertFalse(gateway.hasOpenIssueForPlayer("player-2"));
        assertNull(gateway.getOpenIssueIdForPlayer("player-2"));
        assertEquals(1, gateway.getOpenSessionCount());
    }

    @Test
    void closesIssueForSinglePlayer() throws IOException {
        createMinimalDataSet();

        NewspaperIntegrationGateway gateway = createGateway();

        gateway.openIssueForPlayer("player-1", "issue_test");
        gateway.closeIssueForPlayer("player-1");

        assertFalse(gateway.hasOpenIssueForPlayer("player-1"));
        assertNull(gateway.getOpenIssueIdForPlayer("player-1"));
        assertEquals(0, gateway.getOpenSessionCount());
    }

    @Test
    void returnsHelpfulTextWhenPlayerHasNoOpenIssue() {
        NewspaperIntegrationGateway gateway = createGateway();

        String overview = gateway.showOverviewForPlayer("player-1");
        String article = gateway.showArticleForPlayer("player-1", 1);

        assertTrue(overview.contains("Diese Ausgabe ist nicht verfügbar."));
        assertTrue(article.contains("Diese Ausgabe ist nicht verfügbar."));
    }

    @Test
    void rejectsBlankPlayerIdWithoutCreatingSession() throws IOException {
        createMinimalDataSet();

        NewspaperIntegrationGateway gateway = createGateway();

        String overview = gateway.openIssueForPlayer(" ", "issue_test");
        String article = gateway.showArticleForPlayer("", 1);

        assertTrue(overview.contains("Diese Ausgabe ist nicht verfügbar."));
        assertTrue(article.contains("Diese Ausgabe ist nicht verfügbar."));
        assertEquals(0, gateway.getOpenSessionCount());
    }

    @Test
    void rejectsBlankIssueIdAndClosesExistingSession() throws IOException {
        createMinimalDataSet();

        NewspaperIntegrationGateway gateway = createGateway();

        gateway.openIssueForPlayer("player-1", "issue_test");
        String overview = gateway.openIssueForPlayer("player-1", " ");

        assertTrue(overview.contains("Diese Ausgabe ist nicht verfügbar."));
        assertFalse(gateway.hasOpenIssueForPlayer("player-1"));
        assertEquals(0, gateway.getOpenSessionCount());
    }

    @Test
    void rejectsInvalidArticleNumber() throws IOException {
        createMinimalDataSet();

        NewspaperIntegrationGateway gateway = createGateway();

        gateway.openIssueForPlayer("player-1", "issue_test");

        String article = gateway.showArticleForPlayer("player-1", 0);

        assertTrue(article.contains("Dieser Artikel ist in der Ausgabe nicht vorhanden."));
    }

    @Test
    void rejectsBlankArticleId() throws IOException {
        createMinimalDataSet();

        NewspaperIntegrationGateway gateway = createGateway();

        gateway.openIssueForPlayer("player-1", "issue_test");

        String article = gateway.showArticleForPlayer("player-1", " ");

        assertTrue(article.contains("Dieser Artikel ist in der Ausgabe nicht vorhanden."));
    }

    @Test
    void rendersVisualPreviewForIssue() throws IOException {
        createMinimalDataSet();

        NewspaperIntegrationGateway gateway = createGateway();

        NewspaperPreviewIssue previewIssue = gateway.createPreviewForIssue("issue_test");
        String text = gateway.renderPreviewForIssue("issue_test");

        assertTrue(previewIssue.hasSpreads());
        assertTrue(text.contains("Athena Testausgabe Preview"));
        assertTrue(text.contains("FRONT_COVER"));
        assertTrue(text.contains("Erster Spielartikel"));
    }

    private NewspaperIntegrationGateway createGateway() {
        AthenaPressCore core = new AthenaPressCore(tempDir);
        return new NewspaperIntegrationGateway(core);
    }

    private void createMinimalDataSet() throws IOException {
        createFolders();

        Files.writeString(
                tempDir.resolve("articles").resolve("published").resolve("article_test.json"),
                """
                {
                  "id": "article_test",
                  "status": "published",
                  "categoryId": "server_news",
                  "title": "Erster Spielartikel",
                  "subtitle": "Untertitel im Artikel",
                  "teaser": "Kurzer Teaser",
                  "summary": "Kurzfassung für die Artikelliste",
                  "body": "Dies ist der lesbare Artikeltext."
                }
                """
        );

        Files.writeString(
                tempDir.resolve("issues").resolve("published").resolve("issue_test.json"),
                """
                {
                  "id": "issue_test",
                  "status": "published",
                  "issueNumber": 1,
                  "title": "Athena Testausgabe",
                  "subtitle": "Spielnahe Vorschau",
                  "articles": [
                    "article_test"
                  ],
                  "cover": {
                    "mainArticleId": "article_test",
                    "image": "placeholders/no_image.png"
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
