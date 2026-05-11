package pro.jeti.athenapress.repository;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import pro.jeti.athenapress.model.Issue;

class IssueRepositoryTest {

    @Test
    void shouldReadExistingIssue0002() throws Exception {
        Path projectRoot = Path.of(System.getProperty("user.dir"))
                .getParent()
                .getParent();

        Path athenaPressRoot = projectRoot.resolve("AthenaPress");

        IssueRepository repository = new IssueRepository(athenaPressRoot);

        Issue issue0002 = repository.findById("issue_0002");

        assertNotNull(issue0002, "Expected issue_0002 to exist.");
        assertEquals("issue_0002", issue0002.id());
        assertEquals("published", issue0002.status());
        assertEquals(2, issue0002.issueNumber());
        assertNotNull(issue0002.articles(), "Expected issue_0002 to contain article references.");
        assertTrue(issue0002.articles().contains("article_0001"));
        assertTrue(issue0002.articles().contains("article_0002"));
    }
}