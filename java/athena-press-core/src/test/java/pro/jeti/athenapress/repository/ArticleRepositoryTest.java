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
        ArticleRepository repository = new ArticleRepository(findAthenaPressRoot());

        List<Article> articles = repository.findAll();

        assertFalse(articles.isEmpty(), "Expected at least one article to be loaded.");

        Article article0001 = repository.findById("article_0001");

        assertNotNull(article0001, "Expected article_0001 to exist.");
        assertEquals("article_0001", article0001.id());
    }

    @Test
    void shouldReadBodyAndImageFromExistingArticleJson() throws Exception {
        ArticleRepository repository = new ArticleRepository(findAthenaPressRoot());

        Article article0002 = repository.findById("article_0002");

        assertNotNull(article0002, "Expected article_0002 to exist.");
        assertNotNull(article0002.body(), "Expected article body to be loaded.");
        assertFalse(article0002.body().isBlank(), "Expected article body not to be blank.");

        assertNotNull(article0002.image(), "Expected article image to be loaded.");
        assertEquals("placeholders/no_image.png", article0002.image().file());
        assertEquals("Foto: HF_jeti83", article0002.image().credit());
        assertEquals("placeholder", article0002.image().sourceType());
    }

    private Path findAthenaPressRoot() {
        Path projectRoot = Path.of(System.getProperty("user.dir"))
                .getParent()
                .getParent();

        return projectRoot.resolve("AthenaPress");
    }
}