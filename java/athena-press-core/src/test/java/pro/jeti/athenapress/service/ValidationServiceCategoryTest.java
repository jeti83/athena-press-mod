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

class ValidationServiceCategoryTest {

    @TempDir
    Path tempDir;

    @Test
    void validateArticleCategoriesAcceptsKnownEnabledCategory() throws IOException {
        writeCategories("""
                {
                  "categories": [
                    {
                      "id": "build_projects",
                      "name": "Bauprojekte",
                      "description": "Bauprojekte",
                      "defaultImage": "placeholders/no_image.png",
                      "enabled": true
                    }
                  ]
                }
                """);

        writeDraftArticle("""
                {
                  "id": "article_test",
                  "status": "draft",
                  "categoryId": "build_projects",
                  "title": "Testartikel"
                }
                """);

        ValidationResult result = createValidationService().validateArticleCategories();

        assertTrue(result.isValid());
    }

    @Test
    void validateArticleCategoriesRejectsUnknownCategory() throws IOException {
        writeCategories("""
                {
                  "categories": [
                    {
                      "id": "build_projects",
                      "name": "Bauprojekte",
                      "description": "Bauprojekte",
                      "defaultImage": "placeholders/no_image.png",
                      "enabled": true
                    }
                  ]
                }
                """);

        writeDraftArticle("""
                {
                  "id": "article_test",
                  "status": "draft",
                  "categoryId": "unknown_category",
                  "title": "Testartikel"
                }
                """);

        ValidationResult result = createValidationService().validateArticleCategories();

        assertFalse(result.isValid());
        assertTrue(result.errors().contains(
                "Article article_test has unknown or disabled categoryId: unknown_category"
        ));
    }

    @Test
    void validateArticleCategoriesRejectsDisabledCategory() throws IOException {
        writeCategories("""
                {
                  "categories": [
                    {
                      "id": "dating",
                      "name": "Herzblatt der Farmwelt",
                      "description": "Dating",
                      "defaultImage": "placeholders/dating.png",
                      "enabled": false
                    }
                  ]
                }
                """);

        writeDraftArticle("""
                {
                  "id": "article_test",
                  "status": "draft",
                  "categoryId": "dating",
                  "title": "Testartikel"
                }
                """);

        ValidationResult result = createValidationService().validateArticleCategories();

        assertFalse(result.isValid());
        assertTrue(result.errors().contains(
                "Article article_test has unknown or disabled categoryId: dating"
        ));
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
                categoryRepository
        );
    }

    private void writeCategories(String json) throws IOException {
        Path templatesFolder = tempDir.resolve("templates");
        Files.createDirectories(templatesFolder);
        Files.writeString(templatesFolder.resolve("categories.json"), json);
    }

    private void writeDraftArticle(String json) throws IOException {
        Path draftFolder = tempDir.resolve("articles").resolve("draft");
        Files.createDirectories(draftFolder);
        Files.writeString(draftFolder.resolve("article_test.json"), json);
    }
}