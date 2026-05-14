package pro.jeti.athenapress.service;

import java.io.IOException;
import java.util.List;

import pro.jeti.athenapress.model.Article;
import pro.jeti.athenapress.model.CoverInfo;
import pro.jeti.athenapress.model.GameArticleView;
import pro.jeti.athenapress.model.GameIssueView;
import pro.jeti.athenapress.model.Issue;
import pro.jeti.athenapress.model.ResolvedIssue;

public class GameViewService {
    private final PressService pressService;

    public GameViewService(PressService pressService) {
        this.pressService = pressService;
    }

    public GameIssueView createPublishedIssueView(String issueId) throws IOException {
        ResolvedIssue resolvedIssue = pressService.resolveIssue(issueId);

        if (resolvedIssue == null || resolvedIssue.issue() == null) {
            return null;
        }

        Issue issue = resolvedIssue.issue();

        if (!isPublished(issue)) {
            return null;
        }

        List<GameArticleView> articles = resolvedIssue.articles().stream()
                .map(this::toGameArticleView)
                .toList();

        CoverInfo cover = issue.cover();

        return new GameIssueView(
                issue.id(),
                issue.issueNumber(),
                issue.title(),
                issue.subtitle(),
                cover == null ? null : cover.mainArticleId(),
                cover == null ? null : cover.image(),
                articles
        );
    }

    private GameArticleView toGameArticleView(Article article) {
        return new GameArticleView(
                article.id(),
                article.categoryId(),
                article.title(),
                article.subtitle(),
                article.teaser(),
                article.summary(),
                article.body()
        );
    }

    private boolean isPublished(Issue issue) {
        return issue.status() != null && issue.status().equalsIgnoreCase("published");
    }
}