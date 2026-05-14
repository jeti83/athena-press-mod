package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PlayerNewspaperInteractionServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void rejectsMissingPlugin() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PlayerNewspaperInteractionService(null)
        );
    }

    @Test
    void handlesCompletePlayerFlow() throws IOException {
        createMinimalDataSet();

        PlayerNewspaperInteractionService interactionService = createInteractionService();

        String overview = interactionService.handleAction(
                PlayerNewspaperAction.OPEN_ISSUE,
                "player-1",
                "issue_test"
        );

        String article = interactionService.handleAction(
                PlayerNewspaperAction.SELECT_ARTICLE_BY_NUMBER,
                "player-1",
                "1"
        );

        String overviewAgain = interactionService.handleAction(
                PlayerNewspaperAction.SHOW_OVERVIEW,
                "player-1",
                null
        );

        String closeText = interactionService.handleAction(
                PlayerNewspaperAction.CLOSE_ISSUE,
                "player-1",
                null
        );

        assertTrue(overview.contains("Athena Testausgabe"));
        assertTrue(article.contains("Dies ist der lesbare Artikeltext."));
        assertTrue(overviewAgain.contains("[1] Erster Spielartikel"));
        assertTrue(closeText.contains("Zeitung geschlossen."));
    }

    @Test
    void rejectsInvalidArticleNumber() throws IOException {
        createMinimalDataSet();

        PlayerNewspaperInteractionService interactionService = createInteractionService();

        interactionService.handleOpenIssue("player-1", "issue_test");

        String result = interactionService.handleAction(
                PlayerNewspaperAction.SELECT_ARTICLE_BY_NUMBER,
                "player-1",
                "banana"
        );

        assertTrue(result.contains("Dieser Artikel ist in der Ausgabe nicht vorhanden."));
    }

    @Test
    void rejectsMissingAction() throws IOException {
        createMinimalDataSet();

        PlayerNewspaperInteractionService interactionService = createInteractionService();

        String result = interactionService.handleAction(
                null,
                "player-1",
                null
        );

        assertTrue(result.contains("Aktion konnte nicht verarbeitet werden."));
    }

    @Test
    void closesPlayerSession() throws IOException {
        createMinimalDataSet();

        AthenaPressIntegrationPlugin plugin = new AthenaPressIntegrationPlugin(tempDir);

        PlayerNewspaperInteractionService interactionService =
                new PlayerNewspaperInteractionService(plugin);

        interactionService.handleOpenIssue("player-1", "issue_test");

        assertTrue(plugin.hasOpenNewspaper("player-1"));

        interactionService.handleCloseIssue("player-1");

        assertFalse(plugin.hasOpenNewspaper("player-1"));
    }

    private PlayerNewspaperInteractionService createInteractionService() {
        AthenaPressIntegrationPlugin plugin =
                new AthenaPressIntegrationPlugin(tempDir);

        return new PlayerNewspaperInteractionService(plugin);
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