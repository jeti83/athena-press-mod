package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import pro.jeti.athenapress.repository.ArticleRepository;
import pro.jeti.athenapress.service.IssueWriteService;

class IssueEditorServiceTest {

    @TempDir
    Path tempDir;

    private IssueEditorService service;

    @BeforeEach
    void setUp() throws IOException {
        writeTestArticles();
        service = new IssueEditorService(
                new IssueWriteService(tempDir),
                new ArticleRepository(tempDir)
        );
    }

    @Test
    void startEditingListsPublishedArticles() throws IOException {
        IssueEditorView view = service.startEditing("player1", "Redakteur", true);

        assertEquals(IssueEditorStep.SELECT_ARTICLES, view.step());
        assertTrue(view.prompt().contains("Stadtbericht"));
        assertTrue(view.prompt().contains("Marktfest"));
        assertTrue(service.hasActiveSession("player1"));
    }

    @Test
    void fullFlowWithNumbersSubmitsDraft() throws IOException {
        service.startEditing("player1", "Redakteur", true);

        IssueEditorView afterSelect = service.handleInput("player1", "1,2");
        assertEquals(IssueEditorStep.CHOOSE_MAIN_ARTICLE, afterSelect.step());

        IssueEditorView afterMain = service.handleInput("player1", "1");
        assertEquals(IssueEditorStep.ENTER_SUBTITLE, afterMain.step());

        IssueEditorView afterSubtitle = service.handleInput("player1", "Testausgabe Mai");
        assertEquals(IssueEditorStep.REVIEW, afterSubtitle.step());
        assertTrue(afterSubtitle.prompt().contains("Testausgabe Mai"));

        IssueEditorView result = service.handleInput("player1", "einreichen");
        assertEquals(IssueEditorStep.SUBMITTED, result.step());
        assertNotNull(result.draftId());
        assertTrue(result.draftId().startsWith("issue_"));
        assertFalse(service.hasActiveSession("player1"));
    }

    @Test
    void alleSelectsAllArticles() throws IOException {
        service.startEditing("player1", "Redakteur", true);
        IssueEditorView view = service.handleInput("player1", "alle");
        assertEquals(IssueEditorStep.CHOOSE_MAIN_ARTICLE, view.step());
        assertEquals(2, view.selectedArticleIds().size());
    }

    @Test
    void skipMainArticleUsesFirst() throws IOException {
        service.startEditing("player1", "Redakteur", true);
        service.handleInput("player1", "1,2");
        IssueEditorView view = service.handleInput("player1", "weiter");
        assertEquals(IssueEditorStep.ENTER_SUBTITLE, view.step());
        assertNotNull(view.mainArticleId());
    }

    @Test
    void skipSubtitleLeavesItNull() throws IOException {
        service.startEditing("player1", "Redakteur", true);
        service.handleInput("player1", "1");
        service.handleInput("player1", "weiter");
        IssueEditorView view = service.handleInput("player1", "weiter");
        assertEquals(IssueEditorStep.REVIEW, view.step());
        assertTrue(view.prompt().contains("kein Untertitel"));
    }

    @Test
    void cancelAbortsMidFlow() throws IOException {
        service.startEditing("player1", "Redakteur", true);
        service.handleInput("player1", "1,2");
        IssueEditorView result = service.handleInput("player1", "abbrechen");
        assertEquals(IssueEditorStep.CANCELLED, result.step());
        assertFalse(service.hasActiveSession("player1"));
    }

    @Test
    void invalidNumberReturnsError() throws IOException {
        service.startEditing("player1", "Redakteur", true);
        IssueEditorView view = service.handleInput("player1", "99");
        assertEquals(IssueEditorStep.SELECT_ARTICLES, view.step());
        assertNotNull(view.message());
        assertTrue(view.message().contains("Fehler"));
    }

    @Test
    void unknownIdReturnsError() throws IOException {
        service.startEditing("player1", "Redakteur", true);
        IssueEditorView view = service.handleInput("player1", "article_9999");
        assertEquals(IssueEditorStep.SELECT_ARTICLES, view.step());
        assertNotNull(view.message());
    }

    @Test
    void draftFileIsWritten() throws IOException {
        service.startEditing("player1", "Redakteur", true);
        service.handleInput("player1", "1");
        service.handleInput("player1", "weiter");
        service.handleInput("player1", "Frühlingsausgabe");
        IssueEditorView result = service.handleInput("player1", "einreichen");

        Path draftFile = tempDir.resolve("issues").resolve("draft")
                .resolve(result.draftId() + ".json");
        assertTrue(Files.exists(draftFile));

        String content = Files.readString(draftFile);
        assertTrue(content.contains("Frühlingsausgabe"));
        assertTrue(content.contains("draft"));
    }

    @Test
    void startEditingAsAdminIncludesDraftArticles() throws IOException {
        writeDraftArticle();

        IssueEditorView view = service.startEditing("player1", "Redakteur", true);

        assertTrue(view.prompt().contains("Entwurfsartikel"));
    }

    @Test
    void startEditingAsNonAdminExcludesDraftArticles() throws IOException {
        writeDraftArticle();

        IssueEditorView view = service.startEditing("player1", "Redakteur", false);

        assertFalse(view.prompt().contains("Entwurfsartikel"));
    }

    @Test
    void activeSessionCountIsTracked() throws IOException {
        service.startEditing("player1", "Redakteur", true);
        service.startEditing("player2", "Chef", true);
        assertEquals(2, service.activeSessionCount());

        service.cancelSession("player1");
        assertEquals(1, service.activeSessionCount());
    }

    // --- helpers ---

    private void writeTestArticles() throws IOException {
        Path published = tempDir.resolve("articles").resolve("published");
        Files.createDirectories(published);

        Files.writeString(published.resolve("article_0001.json"), """
                {
                  "id": "article_0001",
                  "status": "published",
                  "categoryId": "server_news",
                  "title": "Stadtbericht",
                  "subtitle": "",
                  "teaser": "",
                  "summary": "",
                  "author": { "playerName": "Jeti", "playerUuid": "unknown" },
                  "body": "Inhalt des Stadtberichts.",
                  "image": { "file": "placeholders/no_image.png", "caption": "", "credit": "", "sourceType": "placeholder" },
                  "location": { "enabled": false, "world": "", "x": 0, "y": 0, "z": 0 },
                  "tags": [],
                  "createdAt": "2026-05-01T10:00:00+02:00",
                  "publishedAt": "2026-05-01T12:00:00+02:00"
                }
                """);

        Files.writeString(published.resolve("article_0002.json"), """
                {
                  "id": "article_0002",
                  "status": "published",
                  "categoryId": "economy",
                  "title": "Marktfest",
                  "subtitle": "",
                  "teaser": "",
                  "summary": "",
                  "author": { "playerName": "Jeti", "playerUuid": "unknown" },
                  "body": "Inhalt zum Marktfest.",
                  "image": { "file": "placeholders/no_image.png", "caption": "", "credit": "", "sourceType": "placeholder" },
                  "location": { "enabled": false, "world": "", "x": 0, "y": 0, "z": 0 },
                  "tags": [],
                  "createdAt": "2026-05-02T10:00:00+02:00",
                  "publishedAt": "2026-05-02T12:00:00+02:00"
                }
                """);
    }

    private void writeDraftArticle() throws IOException {
        Path draft = tempDir.resolve("articles").resolve("draft");
        Files.createDirectories(draft);

        Files.writeString(draft.resolve("article_0003.json"), """
                {
                  "id": "article_0003",
                  "status": "draft",
                  "categoryId": "server_news",
                  "title": "Entwurfsartikel",
                  "subtitle": "",
                  "teaser": "",
                  "summary": "",
                  "author": { "playerName": "Jeti", "playerUuid": "unknown" },
                  "body": "Noch nicht veroeffentlicht.",
                  "image": { "file": "placeholders/no_image.png", "caption": "", "credit": "", "sourceType": "placeholder" },
                  "location": { "enabled": false, "world": "", "x": 0, "y": 0, "z": 0 },
                  "tags": [],
                  "createdAt": "2026-05-03T10:00:00+02:00",
                  "publishedAt": null
                }
                """);
    }
}
