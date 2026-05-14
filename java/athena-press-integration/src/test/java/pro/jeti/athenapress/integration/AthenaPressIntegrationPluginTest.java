package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import pro.jeti.athenapress.AthenaPressCore;

class AthenaPressIntegrationPluginTest {

    @TempDir
    Path tempDir;

    @Test
    void rejectsMissingMetadata() {
        NewspaperIntegrationGateway gateway = new NewspaperIntegrationGateway(new AthenaPressCore(tempDir));

        assertThrows(IllegalArgumentException.class, () -> new AthenaPressIntegrationPlugin(null, gateway));
    }

    @Test
    void rejectsMissingGateway() {
        IntegrationPluginMetadata metadata = IntegrationPluginMetadata.current();

        assertThrows(IllegalArgumentException.class, () -> new AthenaPressIntegrationPlugin(metadata, null));
    }

    @Test
    void exposesMetadata() {
        AthenaPressIntegrationPlugin plugin = new AthenaPressIntegrationPlugin(tempDir);

        assertEquals("athena_press", plugin.metadata().pluginId());
        assertEquals("2026.03.26-89796e57b", plugin.metadata().targetServerVersion());
    }

    @Test
    void handlesPlayerNewspaperFlow() throws IOException {
        createMinimalDataSet();

        AthenaPressIntegrationPlugin plugin = new AthenaPressIntegrationPlugin(tempDir);

        String overview = plugin.onPlayerOpenNewspaper("player-1", "issue_test");
        String article = plugin.onPlayerSelectArticle("player-1", 1);
        String overviewAgain = plugin.onPlayerRequestOverview("player-1");

        assertTrue(plugin.hasOpenNewspaper("player-1"));
        assertEquals("issue_test", plugin.getOpenIssueId("player-1"));
        assertTrue(overview.contains("Athena Testausgabe"));
        assertTrue(article.contains("Dies ist der lesbare Artikeltext."));
        assertTrue(overviewAgain.contains("[1] Erster Spielartikel"));

        plugin.onPlayerCloseNewspaper("player-1");

        assertFalse(plugin.hasOpenNewspaper("player-1"));
    }

    @Test
    void handlesArticleSelectionById() throws IOException {
        createMinimalDataSet();

        AthenaPressIntegrationPlugin plugin = new AthenaPressIntegrationPlugin(tempDir);

        plugin.onPlayerOpenNewspaper("player-1", "issue_test");
        String article = plugin.onPlayerSelectArticle("player-1", "article_test");

        assertTrue(article.contains("Erster Spielartikel"));
        assertTrue(article.contains("Dies ist der lesbare Artikeltext."));
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