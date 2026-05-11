package pro.jeti.athenapress.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import pro.jeti.athenapress.model.Article;

public class ArticleRepository {

    private final Path articlesRoot;
    private final ObjectMapper objectMapper;

    public ArticleRepository(Path athenaPressRoot) {
        this.articlesRoot = athenaPressRoot.resolve("articles");
        this.objectMapper = new ObjectMapper();
    }

    public List<Article> findAll() throws IOException {
        List<Article> articles = new ArrayList<>();

        articles.addAll(readArticlesFromFolder("draft"));
        articles.addAll(readArticlesFromFolder("published"));
        articles.addAll(readArticlesFromFolder("archived"));

        return articles;
    }

    public List<Article> findDraftArticles() throws IOException {
        return readArticlesFromFolder("draft");
    }

    public List<Article> findPublishedArticles() throws IOException {
        return readArticlesFromFolder("published");
    }

    public List<Article> findArchivedArticles() throws IOException {
        return readArticlesFromFolder("archived");
    }

    public Article findById(String articleId) throws IOException {
        for (Article article : findAll()) {
            if (articleId.equals(article.id())) {
                return article;
            }
        }

        return null;
    }

    private List<Article> readArticlesFromFolder(String folderName) throws IOException {
        Path folder = articlesRoot.resolve(folderName);

        if (!Files.exists(folder)) {
            return List.of();
        }

        try (var stream = Files.list(folder)) {
            return stream
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(this::readArticleUnchecked)
                    .toList();
        }
    }

    private Article readArticleUnchecked(Path path) {
        try {
            return objectMapper.readValue(path.toFile(), Article.class);
        } catch (IOException exception) {
            throw new RuntimeException("Could not read article JSON: " + path, exception);
        }
    }
}