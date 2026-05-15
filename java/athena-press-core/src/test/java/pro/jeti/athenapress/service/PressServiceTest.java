package pro.jeti.athenapress.service;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import pro.jeti.athenapress.model.Article;
import pro.jeti.athenapress.model.Issue;
import pro.jeti.athenapress.model.ResolvedIssue;
import pro.jeti.athenapress.model.Subscriber;

class PressServiceTest {

    private PressService createService() {
        Path projectRoot = Path.of(System.getProperty("user.dir"))
                .getParent()
                .getParent();

        Path athenaPressRoot = projectRoot.resolve("AthenaPress");

        return new PressService(athenaPressRoot);
    }

    @Test
    void shouldFindPublishedIssue0002() throws Exception {
        PressService service = createService();

        Issue issue = service.findIssueById("issue_0002");

        assertNotNull(issue, "Expected issue_0002 to exist.");
        assertEquals("published", issue.status());
        assertEquals(2, issue.issueNumber());
        assertEquals("Athena Botenblatt", issue.title());
    }

    @Test
    void shouldResolveArticlesForIssue0002() throws Exception {
        PressService service = createService();

        List<Article> articles = service.findArticlesForIssue("issue_0002");

        assertEquals(4, articles.size(), "Expected issue_0002 to contain four resolvable articles.");

        assertTrue(
                articles.stream().anyMatch(article -> "article_0001".equals(article.id())),
                "Expected article_0001 to be resolved."
        );

        assertTrue(
                articles.stream().anyMatch(article -> "article_0002".equals(article.id())),
                "Expected article_0002 to be resolved."
        );

        assertTrue(
                articles.stream().anyMatch(article -> "article_0004".equals(article.id())),
                "Expected article_0004 to be resolved."
        );

        assertTrue(
                articles.stream().anyMatch(article -> "article_0005".equals(article.id())),
                "Expected article_0005 to be resolved."
        );
    }

    @Test
    void shouldFindActiveSubscribers() throws Exception {
        PressService service = createService();

        List<Subscriber> activeSubscribers = service.findActiveSubscribers();

        assertTrue(
                activeSubscribers.stream().anyMatch(subscriber -> "Jeti".equals(subscriber.playerName())),
                "Expected Jeti to be an active subscriber."
        );

        assertTrue(
                activeSubscribers.stream().anyMatch(subscriber -> "HF_jeti83".equals(subscriber.playerName())),
                "Expected HF_jeti83 to be an active subscriber."
        );

        assertFalse(
                activeSubscribers.stream().anyMatch(subscriber -> "TestUser".equals(subscriber.playerName())),
                "Expected TestUser not to be an active subscriber."
        );
    }

    @Test
    void shouldDetectReadAndUnreadIssues() throws Exception {
        PressService service = createService();

        assertTrue(
                service.isIssueUnreadForPlayer("Jeti", "issue_0002"),
                "Expected issue_0002 to be unread for Jeti."
        );

        assertFalse(
                service.isIssueUnreadForPlayer("HF_jeti83", "issue_0002"),
                "Expected issue_0002 not to be unread for HF_jeti83."
        );

        assertTrue(
                service.hasPlayerReadIssue("HF_jeti83", "issue_0002"),
                "Expected HF_jeti83 to have read issue_0002."
        );
    }

    @Test
    void shouldResolveIssue0002WithArticles() throws Exception {
        PressService service = createService();

        ResolvedIssue resolvedIssue = service.resolveIssue("issue_0002");

        assertNotNull(resolvedIssue, "Expected issue_0002 to be resolved.");
        assertEquals("issue_0002", resolvedIssue.issue().id());
        assertEquals(4, resolvedIssue.articles().size(), "Expected issue_0002 to contain four resolved articles.");

        assertTrue(
                resolvedIssue.articles().stream().anyMatch(article -> "article_0001".equals(article.id())),
                "Expected article_0001 to be resolved."
        );

        assertTrue(
                resolvedIssue.articles().stream().anyMatch(article -> "article_0002".equals(article.id())),
                "Expected article_0002 to be resolved."
        );

        assertTrue(
                resolvedIssue.articles().stream().anyMatch(article -> "article_0004".equals(article.id())),
                "Expected article_0004 to be resolved."
        );

        assertTrue(
                resolvedIssue.articles().stream().anyMatch(article -> "article_0005".equals(article.id())),
                "Expected article_0005 to be resolved."
        );
    }
}
