package pro.jeti.athenapress.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GameIssueView(
        String id,
        Integer issueNumber,
        String title,
        String subtitle,
        String coverMainArticleId,
        String coverImage,
        List<GameArticleView> articles
) {
    public GameIssueView {
        articles = articles == null ? List.of() : List.copyOf(articles);
    }

    public GameArticleView findArticleById(String articleId) {
        if (articleId == null || articleId.isBlank()) {
            return null;
        }

        return articles.stream()
                .filter(article -> articleId.equals(article.id()))
                .findFirst()
                .orElse(null);
    }
}