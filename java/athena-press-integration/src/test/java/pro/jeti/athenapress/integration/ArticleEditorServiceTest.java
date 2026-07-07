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

import pro.jeti.athenapress.repository.CategoryRepository;
import pro.jeti.athenapress.repository.PlayerAlbumRepository;
import pro.jeti.athenapress.service.ArticleWriteService;
import pro.jeti.athenapress.service.PlayerAlbumService;

class ArticleEditorServiceTest {

    @TempDir
    Path tempDir;

    private ArticleEditorService service;

    @BeforeEach
    void setUp() throws IOException {
        writeCategoriesJson();
        service = new ArticleEditorService(
                new ArticleWriteService(tempDir),
                new CategoryRepository(tempDir),
                new PlayerAlbumService(new PlayerAlbumRepository(tempDir))
        );
    }

    @Test
    void startEditingReturnsEnterTitleView() {
        ArticleEditorView view = service.startEditing("player1", "Jeti", false);

        assertEquals(ArticleEditorStep.ENTER_TITLE, view.step());
        assertTrue(service.hasActiveSession("player1"));
    }

    @Test
    void fullFlowWithoutImageSubmitsDraft() throws IOException {
        service.startEditing("player1", "Jeti", false);
        service.handleInput("player1", "Mein Testartikel");
        service.handleInput("player1", "server_news");
        service.handleInput("player1", "Das ist der Artikeltext.");
        service.handleInput("player1", "weiter");
        ArticleEditorView result = service.handleInput("player1", "einreichen");

        assertEquals(ArticleEditorStep.SUBMITTED, result.step());
        assertNotNull(result.draftId());
        assertTrue(result.draftId().startsWith("article_"));
        assertFalse(service.hasActiveSession("player1"));

        Path draftFile = tempDir.resolve("articles/draft/" + result.draftId() + ".json");
        assertTrue(Files.exists(draftFile));
        assertFalse(Files.readString(draftFile).contains("\"image\""));
    }

    @Test
    void cancelDuringEditingEndsSession() throws IOException {
        service.startEditing("player1", "Jeti", false);
        service.handleInput("player1", "Titel");
        ArticleEditorView result = service.handleInput("player1", "abbrechen");

        assertEquals(ArticleEditorStep.CANCELLED, result.step());
        assertFalse(service.hasActiveSession("player1"));
    }

    @Test
    void emptyTitleReturnsError() throws IOException {
        service.startEditing("player1", "Jeti", false);
        ArticleEditorView result = service.handleInput("player1", "   ");

        assertEquals(ArticleEditorStep.ENTER_TITLE, result.step());
        assertNotNull(result.message());
        assertTrue(result.message().contains("Fehler"));
    }

    @Test
    void invalidCategoryReturnsError() throws IOException {
        service.startEditing("player1", "Jeti", false);
        service.handleInput("player1", "Titel");
        ArticleEditorView result = service.handleInput("player1", "ungueltige_kategorie");

        assertEquals(ArticleEditorStep.ENTER_CATEGORY, result.step());
        assertNotNull(result.message());
        assertTrue(result.message().contains("Fehler"));
    }

    @Test
    void attachCameraImageAdvancesToReview() throws IOException {
        service.startEditing("player1", "Jeti", false);
        service.handleInput("player1", "Foto-Artikel");
        service.handleInput("player1", "server_news");
        service.handleInput("player1", "Text");

        ArticleEditorView result = service.attachCameraImage(
                "player1", "uploaded/cam_Jeti_123.png", "Das Rathaus"
        );

        assertEquals(ArticleEditorStep.REVIEW, result.step());
        assertTrue(result.currentImage().contains("camera_marker"));
    }

    @Test
    void nonAdminCannotUseExternalImage() {
        service.startEditing("player1", "Jeti", false);
        ArticleContentPolicy policy = new ArticleContentPolicy();

        assertFalse(policy.isImageSourceAllowed("external", false));
        assertFalse(policy.isImageSourceAllowed("uploaded", false));
        assertTrue(policy.isImageSourceAllowed("camera_marker", false));
        assertTrue(policy.isImageSourceAllowed("placeholder", false));
    }

    @Test
    void adminCanUseAllImageSources() {
        ArticleContentPolicy policy = new ArticleContentPolicy();

        assertTrue(policy.isImageSourceAllowed("external", true));
        assertTrue(policy.isImageSourceAllowed("uploaded", true));
        assertTrue(policy.isImageSourceAllowed("camera_marker", true));
    }

    private void writeCategoriesJson() throws IOException {
        Path templatesDir = tempDir.resolve("templates");
        Files.createDirectories(templatesDir);
        Files.writeString(templatesDir.resolve("categories.json"), """
                {
                  "categories": [
                    {"id": "server_news", "name": "Server-News", "description": "News", "defaultImage": null, "enabled": true},
                    {"id": "build_projects", "name": "Bauprojekte", "description": "Bau", "defaultImage": null, "enabled": true},
                    {"id": "classifieds", "name": "Kleinanzeigen", "description": "Anzeigen", "defaultImage": null, "enabled": true},
                    {"id": "disabled_cat", "name": "Alt", "description": "Deaktiviert", "defaultImage": null, "enabled": false}
                  ]
                }
                """);
    }
}
