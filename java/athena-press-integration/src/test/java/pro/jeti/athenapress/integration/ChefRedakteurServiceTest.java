package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import pro.jeti.athenapress.repository.ArticleRepository;
import pro.jeti.athenapress.repository.CategoryRepository;
import pro.jeti.athenapress.repository.IssueRepository;
import pro.jeti.athenapress.repository.SubscriberRepository;
import pro.jeti.athenapress.service.ArticleStatusService;
import pro.jeti.athenapress.service.IssueWriteService;
import pro.jeti.athenapress.service.ValidationService;

class ChefRedakteurServiceTest {

    @TempDir
    Path tempDir;

    private ChefRedakteurService service;

    @BeforeEach
    void setUp() throws IOException {
        writeTestData();
        service = new ChefRedakteurService(
                new ArticleStatusService(tempDir),
                new IssueWriteService(tempDir),
                new ValidationService(
                        new ArticleRepository(tempDir),
                        new IssueRepository(tempDir),
                        new SubscriberRepository(tempDir),
                        new CategoryRepository(tempDir),
                        tempDir),
                new ArticleRepository(tempDir),
                new IssueRepository(tempDir)
        );
    }

    @Test
    void publishArticleMovesDraftToPublished() throws IOException {
        String result = service.handle(new String[]{"publish", "article_0001"});

        assertTrue(result.contains("article_0001"));
        assertFalse(Files.exists(tempDir.resolve("articles/draft/article_0001.json")));
        assertTrue(Files.exists(tempDir.resolve("articles/published/article_0001.json")));
    }

    @Test
    void publishArticleWithGermanAlias() throws IOException {
        String result = service.handle(new String[]{"veroeffentlichen", "article_0001"});
        assertTrue(result.contains("article_0001"));
    }

    @Test
    void publishIssueMoveDraftToPublished() throws IOException {
        String result = service.handle(new String[]{"publish", "issue_0001"});

        assertTrue(result.contains("issue_0001"));
        assertFalse(Files.exists(tempDir.resolve("issues/draft/issue_0001.json")));
        assertTrue(Files.exists(tempDir.resolve("issues/published/issue_0001.json")));
    }

    @Test
    void archivePublishedArticle() throws IOException {
        service.handle(new String[]{"publish", "article_0001"});
        String result = service.handle(new String[]{"archive", "article_0001"});

        assertTrue(result.contains("article_0001"));
        assertTrue(Files.exists(tempDir.resolve("articles/archived/article_0001.json")));
    }

    @Test
    void deliverIssueMarksAsDelivered() throws IOException {
        service.handle(new String[]{"publish", "issue_0001"});
        String result = service.handle(new String[]{"deliver", "issue_0001"});

        assertTrue(result.contains("issue_0001"));
        String content = Files.readString(tempDir.resolve("issues/published/issue_0001.json"));
        assertTrue(content.contains("\"deliveredToSubscribers\" : true"));
    }

    @Test
    void deleteArticleDraft() throws IOException {
        String result = service.handle(new String[]{"delete", "article_0001"});

        assertTrue(result.contains("article_0001"));
        assertFalse(Files.exists(tempDir.resolve("articles/draft/article_0001.json")));
    }

    @Test
    void statusReturnsOverview() throws IOException {
        String result = service.handle(new String[]{"status"});

        assertTrue(result.contains("Artikel"));
        assertTrue(result.contains("Ausgaben"));
    }

    @Test
    void listReturnsAllContent() throws IOException {
        String result = service.handle(new String[]{"liste"});

        assertTrue(result.contains("article_0001"));
        assertTrue(result.contains("issue_0001"));
    }

    @Test
    void unknownCommandReturnsHelp() throws IOException {
        String result = service.handle(new String[]{"unknown_cmd"});
        assertTrue(result.contains("publish") || result.contains("veroeffentlichen"));
    }

    @Test
    void commandParserRecognisesAllAliases() {
        var parser = new ChefRedakteurCommandService();

        assertTrue(parser.parse(new String[]{"publish",         "x"}).type() == ChefRedakteurCommandType.PUBLISH);
        assertTrue(parser.parse(new String[]{"veroeffentlichen","x"}).type() == ChefRedakteurCommandType.PUBLISH);
        assertTrue(parser.parse(new String[]{"archive",         "x"}).type() == ChefRedakteurCommandType.ARCHIVE);
        assertTrue(parser.parse(new String[]{"archivieren",     "x"}).type() == ChefRedakteurCommandType.ARCHIVE);
        assertTrue(parser.parse(new String[]{"deliver",         "x"}).type() == ChefRedakteurCommandType.DELIVER);
        assertTrue(parser.parse(new String[]{"zustellen",       "x"}).type() == ChefRedakteurCommandType.DELIVER);
        assertTrue(parser.parse(new String[]{"delete",          "x"}).type() == ChefRedakteurCommandType.DELETE);
        assertTrue(parser.parse(new String[]{"loeschen",        "x"}).type() == ChefRedakteurCommandType.DELETE);
        assertTrue(parser.parse(new String[]{"validate",        "x"}).type() == ChefRedakteurCommandType.VALIDATE);
        assertTrue(parser.parse(new String[]{"pruefen",         "x"}).type() == ChefRedakteurCommandType.VALIDATE);
        assertTrue(parser.parse(new String[]{"status"            }).type() == ChefRedakteurCommandType.STATUS);
        assertTrue(parser.parse(new String[]{"uebersicht"        }).type() == ChefRedakteurCommandType.STATUS);
        assertTrue(parser.parse(new String[]{"list"              }).type() == ChefRedakteurCommandType.LIST);
        assertTrue(parser.parse(new String[]{"liste"             }).type() == ChefRedakteurCommandType.LIST);
    }

    private void writeTestData() throws IOException {
        Files.createDirectories(tempDir.resolve("articles/draft"));
        Files.createDirectories(tempDir.resolve("issues/draft"));
        Files.createDirectories(tempDir.resolve("templates"));
        Files.createDirectories(tempDir.resolve("subscriptions"));

        Files.writeString(tempDir.resolve("articles/draft/article_0001.json"), """
                {"id":"article_0001","status":"draft","categoryId":"server_news",
                 "title":"Testartikel","body":"Text","author":{"playerName":"Jeti","playerUuid":"unknown"},
                 "createdAt":"2026-05-15T10:00:00+02:00","updatedAt":"2026-05-15T10:00:00+02:00"}
                """);

        Files.writeString(tempDir.resolve("issues/draft/issue_0001.json"), """
                {"id":"issue_0001","status":"draft","issueNumber":1,
                 "title":"Testausgabe","articles":["article_0001"],
                 "deliveredToSubscribers":false,
                 "createdAt":"2026-05-15T10:00:00+02:00","updatedAt":"2026-05-15T10:00:00+02:00"}
                """);

        Files.writeString(tempDir.resolve("templates/categories.json"), """
                {"categories":[{"id":"server_news","name":"Server-News","description":"News","defaultImage":null,"enabled":true}]}
                """);

        Files.writeString(tempDir.resolve("subscriptions/subscribers.json"), """
                {"subscribers":[]}
                """);
    }
}
