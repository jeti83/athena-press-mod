package pro.jeti.athenapress.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArticleWriteServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void createDraftWritesJsonFile() throws IOException {
        ArticleWriteService service = new ArticleWriteService(tempDir);

        String id = service.createDraft(ArticleDraftRequest.of("Jeti", "Testtitel", "server_news", "Testinhalt"));

        assertNotNull(id);
        assertTrue(id.startsWith("article_"));
        Path file = tempDir.resolve("articles/draft/" + id + ".json");
        assertTrue(Files.exists(file));
    }

    @Test
    void createDraftGeneratesSequentialIds() throws IOException {
        ArticleWriteService service = new ArticleWriteService(tempDir);

        String id1 = service.createDraft(ArticleDraftRequest.of("Jeti", "Artikel 1", "server_news", "Text"));
        String id2 = service.createDraft(ArticleDraftRequest.of("Jeti", "Artikel 2", "server_news", "Text"));

        assertTrue(id1.compareTo(id2) < 0);
    }

    @Test
    void createdDraftContainsTitleAndAuthor() throws IOException {
        ArticleWriteService service = new ArticleWriteService(tempDir);

        String id = service.createDraft(ArticleDraftRequest.of("Mira_Baut", "Mein Titel", "build_projects", "Inhalt"));

        Path file = tempDir.resolve("articles/draft/" + id + ".json");
        String content = Files.readString(file);
        assertTrue(content.contains("Mein Titel"));
        assertTrue(content.contains("Mira_Baut"));
        assertTrue(content.contains("build_projects"));
        assertTrue(content.contains("\"status\" : \"draft\""));
    }

    @Test
    void createDraftWithImageSetsSourceType() throws IOException {
        ArticleWriteService service = new ArticleWriteService(tempDir);
        ArticleDraftRequest request = new ArticleDraftRequest(
                "Jeti", "unknown", "Foto-Artikel", null,
                "server_news", "Text mit Foto",
                "uploaded/cam_Jeti_123.png", "Das Rathaus", "camera_marker"
        );

        String id = service.createDraft(request);

        Path file = tempDir.resolve("articles/draft/" + id + ".json");
        String content = Files.readString(file);
        assertTrue(content.contains("camera_marker"));
        assertTrue(content.contains("cam_Jeti_123.png"));
    }
}
