package pro.jeti.athenapress.repository;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import pro.jeti.athenapress.model.Article;

class ArticleRepositoryTest {

    @Test
    void shouldReadExistingArticles() throws Exception {
        Path projectRoot = Path.of(System.getProperty("user.dir"))
                .getParent()
                .getParent();

        Path athenaPressRoot = projectRoot.resolve("AthenaPress");

        ArticleRepository repository = new ArticleRepository(athenaPressRoot);

        List<Article> articles = repository.findAll();

        assertFalse(articles.isEmpty(), "Expected at least one article to be loaded.");

        Article article0001 = repository.findById("article_0001");

        assertNotNull(article0001, "Expected article_0001 to exist.");
        assertEquals("article_0001", article0001.id());
    }
}