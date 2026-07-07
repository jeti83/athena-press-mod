package pro.jeti.athenapress.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import pro.jeti.athenapress.repository.ArticleRepository;
import pro.jeti.athenapress.repository.CategoryRepository;
import pro.jeti.athenapress.repository.IssueRepository;
import pro.jeti.athenapress.repository.SubscriberRepository;

class ValidationServiceImageTest {

    @TempDir
    Path tempDir;

    @Test
    void validateArticleImagesAcceptsExistingPlaceholderImage() throws IOException {
        writeImageFile("placeholders/no_image.png");

        writeDraftArticle("""
                {
                  "id": "article_test",
                  "status": "draft",
                  "categoryId": "server_news",
                  "title": "Testartikel",
                  "image": {
                    "file": "placeholders/no_image.png",
                    "sourceType": "placeholder"
                  }
                }
                """);

        ValidationResult result = createValidationService().validateArticleImages();

        assertTrue(result.isValid());
    }

    @Test
    void validateArticleImagesRejectsInvalidSourceType() throws IOException {
        writeDraftArticle("""
                {
                  "id": "article_test",
                  "status": "draft",
                  "categoryId": "server_news",
                  "title": "Testartikel",
                  "image": {
                    "file": "placeholders/no_image.png",
                    "sourceType": "magic_scroll"
                  }
                }
                """);

        ValidationResult result = createValidationService().validateArticleImages();

        assertFalse(result.isValid());
        assertTrue(result.errors().contains(
                "Article article_test has image with invalid sourceType: magic_scroll"
        ));
    }

    @Test
    void validateArticleImagesRejectsMissingLocalImageFile() throws IOException {
        writeDraftArticle("""
                {
                  "id": "article_test",
                  "status": "draft",
                  "categoryId": "server_news",
                  "title": "Testartikel",
                  "image": {
                    "file": "placeholders/missing.png",
                    "sourceType": "placeholder"
                  }
                }
                """);

        ValidationResult result = createValidationService().validateArticleImages();

        assertFalse(result.isValid());
        assertTrue(result.errors().contains(
                "Article article_test references missing image file: placeholders/missing.png"
        ));
    }

    @Test
    void validateArticleImagesAcceptsArticleWithoutImageField() throws IOException {
        writeDraftArticle("""
                {
                  "id": "article_test",
                  "status": "draft",
                  "categoryId": "server_news",
                  "title": "Testartikel"
                }
                """);

        ValidationResult result = createValidationService().validateArticleImages();

        assertTrue(result.isValid());
    }

    @Test
    void validateArticleImagesAcceptsCameraMarkerWithoutLocalFile() throws IOException {
        writeDraftArticle("""
                {
                  "id": "article_test",
                  "status": "draft",
                  "categoryId": "server_news",
                  "title": "Testartikel",
                  "image": {
                    "sourceType": "camera_marker"
                  }
                }
                """);

        ValidationResult result = createValidationService().validateArticleImages();

        assertTrue(result.isValid());
    }

    private ValidationService createValidationService() {
        ArticleRepository articleRepository = new ArticleRepository(tempDir);
        IssueRepository issueRepository = new IssueRepository(tempDir);
        SubscriberRepository subscriberRepository = new SubscriberRepository(tempDir);
        CategoryRepository categoryRepository = new CategoryRepository(tempDir);

        return new ValidationService(
                articleRepository,
                issueRepository,
                subscriberRepository,
                categoryRepository,
                tempDir
        );
    }

    private void writeDraftArticle(String json) throws IOException {
        Path draftFolder = tempDir.resolve("articles").resolve("draft");
        Files.createDirectories(draftFolder);
        Files.writeString(draftFolder.resolve("article_test.json"), json);
    }

    private void writeImageFile(String relativePath) throws IOException {
        Path imagePath = tempDir.resolve("images").resolve(relativePath);
        Files.createDirectories(imagePath.getParent());
        Files.writeString(imagePath, "placeholder image");
    }
}