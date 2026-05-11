package pro.jeti.athenapress.service;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import pro.jeti.athenapress.repository.ArticleRepository;
import pro.jeti.athenapress.repository.IssueRepository;
import pro.jeti.athenapress.repository.SubscriberRepository;

class ValidationServiceTest {

    private static final Path PROJECT_ROOT = Path.of("../..", "AthenaPress")
            .toAbsolutePath()
            .normalize();

    @Test
    void publishedIssueShouldNotReferenceMissingArticles() {
        ArticleRepository articleRepository = new ArticleRepository(PROJECT_ROOT);
        IssueRepository issueRepository = new IssueRepository(PROJECT_ROOT);
        SubscriberRepository subscriberRepository = new SubscriberRepository(PROJECT_ROOT);

        ValidationService validationService = new ValidationService(
                articleRepository,
                issueRepository,
                subscriberRepository
        );

        var result = validationService.validateIssueArticleReferences("issue_0002");

        assertTrue(
                result.isValid(),
                "issue_0002 should only reference existing articles, but got: " + result.errors()
        );
    }

    @Test
    void missingIssueShouldReturnValidationError() {
        ArticleRepository articleRepository = new ArticleRepository(PROJECT_ROOT);
        IssueRepository issueRepository = new IssueRepository(PROJECT_ROOT);
        SubscriberRepository subscriberRepository = new SubscriberRepository(PROJECT_ROOT);

        ValidationService validationService = new ValidationService(
                articleRepository,
                issueRepository,
                subscriberRepository
        );

        var result = validationService.validateIssueArticleReferences("issue_does_not_exist");

        assertTrue(
                !result.isValid(),
                "Missing issue should be invalid"
        );

        assertTrue(
                result.errors().stream().anyMatch(error -> error.contains("Issue not found")),
                "Expected 'Issue not found' error, but got: " + result.errors()
        );
    }

    @Test
    void issueReferencingMissingArticleShouldReturnValidationError(@TempDir Path tempDir) throws Exception {
        Path athenaPressRoot = tempDir.resolve("AthenaPress");

        Files.createDirectories(athenaPressRoot.resolve("issues/published"));
        Files.createDirectories(athenaPressRoot.resolve("articles/published"));
        Files.createDirectories(athenaPressRoot.resolve("subscriptions"));

        Files.writeString(
                athenaPressRoot.resolve("issues/published/issue_missing_article.json"),
                """
                {
                  "id": "issue_missing_article",
                  "status": "published",
                  "issueNumber": 999,
                  "title": "Testausgabe mit fehlendem Artikel",
                  "subtitle": "Nur fuer den Validator-Test",
                  "articles": [
                    "article_does_not_exist"
                  ]
                }
                """
        );

        Files.writeString(
                athenaPressRoot.resolve("subscriptions/subscribers.json"),
                """
                {
                  "subscribers": []
                }
                """
        );

        ArticleRepository articleRepository = new ArticleRepository(athenaPressRoot);
        IssueRepository issueRepository = new IssueRepository(athenaPressRoot);
        SubscriberRepository subscriberRepository = new SubscriberRepository(athenaPressRoot);

        ValidationService validationService = new ValidationService(
                articleRepository,
                issueRepository,
                subscriberRepository
        );

        var result = validationService.validateIssueArticleReferences("issue_missing_article");

        assertTrue(
                !result.isValid(),
                "Issue with missing article reference should be invalid"
        );

        assertTrue(
                result.errors().stream().anyMatch(error -> error.contains("article_does_not_exist")),
                "Expected missing article error, but got: " + result.errors()
        );
    }

    @Test
    void subscribersShouldOnlyUseValidDeliveryModes() {
        ArticleRepository articleRepository = new ArticleRepository(PROJECT_ROOT);
        IssueRepository issueRepository = new IssueRepository(PROJECT_ROOT);
        SubscriberRepository subscriberRepository = new SubscriberRepository(PROJECT_ROOT);

        ValidationService validationService = new ValidationService(
                articleRepository,
                issueRepository,
                subscriberRepository
        );

        var result = validationService.validateSubscriberDeliveryModes();

        assertTrue(
                result.isValid(),
                "Subscribers should only use valid deliveryMode values, but got: " + result.errors()
        );
    }

    @Test
    void subscriberWithInvalidDeliveryModeShouldReturnValidationError(@TempDir Path tempDir) throws Exception {
        Path athenaPressRoot = tempDir.resolve("AthenaPress");

        Files.createDirectories(athenaPressRoot.resolve("articles/published"));
        Files.createDirectories(athenaPressRoot.resolve("issues/published"));
        Files.createDirectories(athenaPressRoot.resolve("subscriptions"));

        Files.writeString(
                athenaPressRoot.resolve("subscriptions/subscribers.json"),
                """
                {
                  "subscribers": [
                    {
                      "playerName": "BrokenUser",
                      "playerUuid": "unknown",
                      "subscribed": true,
                      "deliveryMode": "carrier_pigeon",
                      "subscribedAt": "2026-05-10T14:49:47+02:00",
                      "updatedAt": "2026-05-10T15:36:33+02:00",
                      "lastReceivedIssueId": null,
                      "unreadIssues": []
                    }
                  ]
                }
                """
        );

        ArticleRepository articleRepository = new ArticleRepository(athenaPressRoot);
        IssueRepository issueRepository = new IssueRepository(athenaPressRoot);
        SubscriberRepository subscriberRepository = new SubscriberRepository(athenaPressRoot);

        ValidationService validationService = new ValidationService(
                articleRepository,
                issueRepository,
                subscriberRepository
        );

        var result = validationService.validateSubscriberDeliveryModes();

        assertTrue(
                !result.isValid(),
                "Subscriber with invalid deliveryMode should be invalid"
        );

        assertTrue(
                result.errors().stream().anyMatch(error -> error.contains("carrier_pigeon")),
                "Expected invalid deliveryMode error, but got: " + result.errors()
        );
    }

    @Test
    void existingArticleAndIssueShouldUseValidStatuses() {
        ArticleRepository articleRepository = new ArticleRepository(PROJECT_ROOT);
        IssueRepository issueRepository = new IssueRepository(PROJECT_ROOT);
        SubscriberRepository subscriberRepository = new SubscriberRepository(PROJECT_ROOT);

        ValidationService validationService = new ValidationService(
                articleRepository,
                issueRepository,
                subscriberRepository
        );

        var articleResult = validationService.validateArticleStatus("article_0001");
        var issueResult = validationService.validateIssueStatus("issue_0002");

        assertTrue(
                articleResult.isValid(),
                "article_0001 should have a valid status, but got: " + articleResult.errors()
        );

        assertTrue(
                issueResult.isValid(),
                "issue_0002 should have a valid status, but got: " + issueResult.errors()
        );
    }

    @Test
    void articleWithInvalidStatusShouldReturnValidationError(@TempDir Path tempDir) throws Exception {
        Path athenaPressRoot = tempDir.resolve("AthenaPress");

        Files.createDirectories(athenaPressRoot.resolve("articles/published"));
        Files.createDirectories(athenaPressRoot.resolve("issues/published"));
        Files.createDirectories(athenaPressRoot.resolve("subscriptions"));

        Files.writeString(
                athenaPressRoot.resolve("articles/published/article_invalid_status.json"),
                """
                {
                  "id": "article_invalid_status",
                  "status": "printed_on_cheese",
                  "categoryId": "server_news",
                  "title": "Artikel mit kaputtem Status"
                }
                """
        );

        Files.writeString(
                athenaPressRoot.resolve("subscriptions/subscribers.json"),
                """
                {
                  "subscribers": []
                }
                """
        );

        ArticleRepository articleRepository = new ArticleRepository(athenaPressRoot);
        IssueRepository issueRepository = new IssueRepository(athenaPressRoot);
        SubscriberRepository subscriberRepository = new SubscriberRepository(athenaPressRoot);

        ValidationService validationService = new ValidationService(
                articleRepository,
                issueRepository,
                subscriberRepository
        );

        var result = validationService.validateArticleStatus("article_invalid_status");

        assertTrue(
                !result.isValid(),
                "Article with invalid status should be invalid"
        );

        assertTrue(
                result.errors().stream().anyMatch(error -> error.contains("printed_on_cheese")),
                "Expected invalid article status error, but got: " + result.errors()
        );
    }

    @Test
    void issueWithInvalidStatusShouldReturnValidationError(@TempDir Path tempDir) throws Exception {
        Path athenaPressRoot = tempDir.resolve("AthenaPress");

        Files.createDirectories(athenaPressRoot.resolve("articles/published"));
        Files.createDirectories(athenaPressRoot.resolve("issues/published"));
        Files.createDirectories(athenaPressRoot.resolve("subscriptions"));

        Files.writeString(
                athenaPressRoot.resolve("issues/published/issue_invalid_status.json"),
                """
                {
                  "id": "issue_invalid_status",
                  "status": "lost_in_the_void",
                  "issueNumber": 1000,
                  "title": "Ausgabe mit kaputtem Status",
                  "subtitle": "Nur fuer den Validator-Test",
                  "articles": []
                }
                """
        );

        Files.writeString(
                athenaPressRoot.resolve("subscriptions/subscribers.json"),
                """
                {
                  "subscribers": []
                }
                """
        );

        ArticleRepository articleRepository = new ArticleRepository(athenaPressRoot);
        IssueRepository issueRepository = new IssueRepository(athenaPressRoot);
        SubscriberRepository subscriberRepository = new SubscriberRepository(athenaPressRoot);

        ValidationService validationService = new ValidationService(
                articleRepository,
                issueRepository,
                subscriberRepository
        );

        var result = validationService.validateIssueStatus("issue_invalid_status");

        assertTrue(
                !result.isValid(),
                "Issue with invalid status should be invalid"
        );

        assertTrue(
                result.errors().stream().anyMatch(error -> error.contains("lost_in_the_void")),
                "Expected invalid issue status error, but got: " + result.errors()
        );
    }

    @Test
    void existingArticleAndIssueShouldHaveRequiredFields() {
        ArticleRepository articleRepository = new ArticleRepository(PROJECT_ROOT);
        IssueRepository issueRepository = new IssueRepository(PROJECT_ROOT);
        SubscriberRepository subscriberRepository = new SubscriberRepository(PROJECT_ROOT);

        ValidationService validationService = new ValidationService(
                articleRepository,
                issueRepository,
                subscriberRepository
        );

        var articleResult = validationService.validateArticleRequiredFields("article_0001");
        var issueResult = validationService.validateIssueRequiredFields("issue_0002");

        assertTrue(
                articleResult.isValid(),
                "article_0001 should have required fields, but got: " + articleResult.errors()
        );

        assertTrue(
                issueResult.isValid(),
                "issue_0002 should have required fields, but got: " + issueResult.errors()
        );
    }

    @Test
    void articleWithMissingRequiredFieldsShouldReturnValidationError(@TempDir Path tempDir) throws Exception {
        Path athenaPressRoot = tempDir.resolve("AthenaPress");

        Files.createDirectories(athenaPressRoot.resolve("articles/published"));
        Files.createDirectories(athenaPressRoot.resolve("issues/published"));
        Files.createDirectories(athenaPressRoot.resolve("subscriptions"));

        Files.writeString(
                athenaPressRoot.resolve("articles/published/article_missing_required.json"),
                """
                {
                  "id": "article_missing_required",
                  "status": "published",
                  "title": "   "
                }
                """
        );

        Files.writeString(
                athenaPressRoot.resolve("subscriptions/subscribers.json"),
                """
                {
                  "subscribers": []
                }
                """
        );

        ArticleRepository articleRepository = new ArticleRepository(athenaPressRoot);
        IssueRepository issueRepository = new IssueRepository(athenaPressRoot);
        SubscriberRepository subscriberRepository = new SubscriberRepository(athenaPressRoot);

        ValidationService validationService = new ValidationService(
                articleRepository,
                issueRepository,
                subscriberRepository
        );

        var result = validationService.validateArticleRequiredFields("article_missing_required");

        assertTrue(
                !result.isValid(),
                "Article with missing required fields should be invalid"
        );

        assertTrue(
                result.errors().stream().anyMatch(error -> error.contains("missing title")),
                "Expected missing title error, but got: " + result.errors()
        );

        assertTrue(
                result.errors().stream().anyMatch(error -> error.contains("missing categoryId")),
                "Expected missing categoryId error, but got: " + result.errors()
        );
    }

    @Test
    void issueWithMissingRequiredFieldsShouldReturnValidationError(@TempDir Path tempDir) throws Exception {
        Path athenaPressRoot = tempDir.resolve("AthenaPress");

        Files.createDirectories(athenaPressRoot.resolve("articles/published"));
        Files.createDirectories(athenaPressRoot.resolve("issues/published"));
        Files.createDirectories(athenaPressRoot.resolve("subscriptions"));

        Files.writeString(
                athenaPressRoot.resolve("issues/published/issue_missing_required.json"),
                """
                {
                  "id": "issue_missing_required",
                  "status": "published",
                  "title": "   "
                }
                """
        );

        Files.writeString(
                athenaPressRoot.resolve("subscriptions/subscribers.json"),
                """
                {
                  "subscribers": []
                }
                """
        );

        ArticleRepository articleRepository = new ArticleRepository(athenaPressRoot);
        IssueRepository issueRepository = new IssueRepository(athenaPressRoot);
        SubscriberRepository subscriberRepository = new SubscriberRepository(athenaPressRoot);

        ValidationService validationService = new ValidationService(
                articleRepository,
                issueRepository,
                subscriberRepository
        );

        var result = validationService.validateIssueRequiredFields("issue_missing_required");

        assertTrue(
                !result.isValid(),
                "Issue with missing required fields should be invalid"
        );

        assertTrue(
                result.errors().stream().anyMatch(error -> error.contains("missing title")),
                "Expected missing title error, but got: " + result.errors()
        );

        assertTrue(
                result.errors().stream().anyMatch(error -> error.contains("missing articles")),
                "Expected missing articles error, but got: " + result.errors()
        );
    }
}