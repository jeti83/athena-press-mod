package pro.jeti.athenapress.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GameNewspaperSessionServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void opensPublishedIssueAndShowsOverview() throws IOException {
        createMinimalDataSet();

        GameNewspaperSessionService session = createSession();

        String overview = session.openIssue("issue_test");

        assertTrue(session.hasOpenIssue());
        assertEquals("issue_test", session.getOpenIssueId());
        assertTrue(overview.contains("Athena Testausgabe"));
        assertTrue(overview.contains("[1] Erster Spielartikel"));
        assertTrue(overview.contains("Kurzfassung für die Artikelliste"));
    }

    @Test
    void showsArticleByNumberAfterOpeningIssue() throws IOException {
        createMinimalDataSet();

        GameNewspaperSessionService session = createSession();

        session.openIssue("issue_test");
        String articleText = session.showArticleByNumber(1);

        assertTrue(articleText.contains("Erster Spielartikel"));
        assertTrue(articleText.contains("Untertitel im Artikel"));
        assertTrue(articleText.contains("Dies ist der lesbare Artikeltext."));
    }

    @Test
    void showsArticleByIdAfterOpeningIssue() throws IOException {
        createMinimalDataSet();

        GameNewspaperSessionService session = createSession();

        session.openIssue("issue_test");
        String articleText = session.showArticleById("article_test");

        assertTrue(articleText.contains("Erster Spielartikel"));
        assertTrue(articleText.contains("Dies ist der lesbare Artikeltext."));
    }

    @Test
    void canReturnFromArticleToOverview() throws IOException {
        createMinimalDataSet();

        GameNewspaperSessionService session = createSession();

        session.openIssue("issue_test");
        session.showArticleByNumber(1);
        String overview = session.showOverview();

        assertTrue(overview.contains("Athena Testausgabe"));
        assertTrue(overview.contains("[1] Erster Spielartikel"));
        assertTrue(overview.contains("Wähle einen Artikel, um ihn zu lesen."));
    }

    @Test
    void returnsHelpfulTextWhenNoIssueIsOpen() {
        GameNewspaperSessionService session = createSession();

        String overview = session.showOverview();
        String articleText = session.showArticleByNumber(1);

        assertFalse(session.hasOpenIssue());
        assertNull(session.getOpenIssueId());
        assertTrue(overview.contains("Diese Ausgabe ist nicht verfügbar."));
        assertTrue(articleText.contains("Diese Ausgabe ist nicht verfügbar."));
    }

    @Test
    void closesOpenIssue() throws IOException {
        createMinimalDataSet();

        GameNewspaperSessionService session = createSession();

        session.openIssue("issue_test");
        session.closeIssue();

        assertFalse(session.hasOpenIssue());
        assertNull(session.getOpenIssueId());
        assertTrue(session.showOverview().contains("Diese Ausgabe ist nicht verfügbar."));
    }

    @Test
    void doesNotOpenMissingIssue() throws IOException {
        createFolders();

        GameNewspaperSessionService session = createSession();

        String overview = session.openIssue("does_not_exist");

        assertFalse(session.hasOpenIssue());
        assertNull(session.getOpenIssueId());
        assertTrue(overview.contains("Diese Ausgabe ist nicht verfügbar."));
    }

    private GameNewspaperSessionService createSession() {
        PressService pressService = new PressService(tempDir);
        GameViewService gameViewService = new GameViewService(pressService);
        GameTextRendererService gameTextRendererService = new GameTextRendererService();

        return new GameNewspaperSessionService(gameViewService, gameTextRendererService);
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