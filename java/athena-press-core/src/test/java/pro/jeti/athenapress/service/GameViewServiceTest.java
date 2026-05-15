package pro.jeti.athenapress.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import pro.jeti.athenapress.model.GameArticleView;
import pro.jeti.athenapress.model.GameIssueView;

class GameViewServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void createsGameIssueViewForPublishedIssue() throws IOException {
        createMinimalDataSet();

        GameViewService service = new GameViewService(new PressService(tempDir));

        GameIssueView view = service.createPublishedIssueView("issue_test");

        assertNotNull(view);
        assertEquals("issue_test", view.id());
        assertEquals(1, view.issueNumber());
        assertEquals("Athena Testausgabe", view.title());
        assertEquals("Spielnahe Vorschau", view.subtitle());
        assertEquals("article_test", view.coverMainArticleId());
        assertEquals("placeholders/no_image.png", view.coverImage());
        assertEquals(1, view.articles().size());

        GameArticleView article = view.findArticleById("article_test");

        assertNotNull(article);
        assertEquals("article_test", article.id());
        assertEquals("server_news", article.categoryId());
        assertEquals("Erster Spielartikel", article.title());
        assertEquals("Kurzfassung für die Artikelliste", article.summary());
        assertEquals("Dies ist der lesbare Artikeltext.", article.body());
        assertEquals("placeholders/article.png", article.image().file());
    }

    @Test
    void returnsNullForDraftIssue() throws IOException {
        createFolders();

        Files.writeString(
                tempDir.resolve("issues").resolve("draft").resolve("issue_draft.json"),
                """
                {
                  "id": "issue_draft",
                  "status": "draft",
                  "issueNumber": 2,
                  "title": "Noch nicht spielbereit",
                  "articles": []
                }
                """
        );

        GameViewService service = new GameViewService(new PressService(tempDir));

        GameIssueView view = service.createPublishedIssueView("issue_draft");

        assertNull(view);
    }

    @Test
    void returnsNullForMissingIssue() throws IOException {
        createFolders();

        GameViewService service = new GameViewService(new PressService(tempDir));

        GameIssueView view = service.createPublishedIssueView("does_not_exist");

        assertNull(view);
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
                  "image": {
                    "file": "placeholders/article.png",
                    "caption": "Der Artikel im Bild",
                    "sourceType": "local"
                  },
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
