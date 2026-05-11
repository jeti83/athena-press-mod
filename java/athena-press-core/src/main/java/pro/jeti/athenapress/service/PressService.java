package pro.jeti.athenapress.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import pro.jeti.athenapress.model.Article;
import pro.jeti.athenapress.model.Issue;
import pro.jeti.athenapress.model.ResolvedIssue;
import pro.jeti.athenapress.model.Subscriber;
import pro.jeti.athenapress.repository.ArticleRepository;
import pro.jeti.athenapress.repository.IssueRepository;
import pro.jeti.athenapress.repository.SubscriberRepository;

public class PressService {

    private final ArticleRepository articleRepository;
    private final IssueRepository issueRepository;
    private final SubscriberRepository subscriberRepository;

    public PressService(Path athenaPressRoot) {
        this.articleRepository = new ArticleRepository(athenaPressRoot);
        this.issueRepository = new IssueRepository(athenaPressRoot);
        this.subscriberRepository = new SubscriberRepository(athenaPressRoot);
    }

    public List<Issue> findPublishedIssues() throws IOException {
        return issueRepository.findPublishedIssues();
    }

    public Issue findIssueById(String issueId) throws IOException {
        return issueRepository.findById(issueId);
    }

    public List<Article> findArticlesForIssue(String issueId) throws IOException {
        Issue issue = issueRepository.findById(issueId);

        if (issue == null || issue.articles() == null) {
            return List.of();
        }

        List<Article> articles = new ArrayList<>();

        for (String articleId : issue.articles()) {
            Article article = articleRepository.findById(articleId);

            if (article != null) {
                articles.add(article);
            }
        }

        return articles;
    }

    public List<Subscriber> findActiveSubscribers() throws IOException {
        return subscriberRepository.findActiveSubscribers();
    }

    public Subscriber findSubscriberByPlayerName(String playerName) throws IOException {
        return subscriberRepository.findByPlayerName(playerName);
    }

    public boolean isIssueUnreadForPlayer(String playerName, String issueId) throws IOException {
        Subscriber subscriber = subscriberRepository.findByPlayerName(playerName);

        if (subscriber == null || subscriber.unreadIssues() == null) {
            return false;
        }

        return subscriber.unreadIssues().contains(issueId);
    }

    public boolean hasPlayerReadIssue(String playerName, String issueId) throws IOException {
        Subscriber subscriber = subscriberRepository.findByPlayerName(playerName);

        if (subscriber == null) {
            return false;
        }

        return issueId.equals(subscriber.lastReadIssueId());
    }
    public ResolvedIssue resolveIssue(String issueId) throws IOException {
        Issue issue = issueRepository.findById(issueId);

        if (issue == null) {
        return null;
        }

        List<Article> articles = findArticlesForIssue(issueId);

        return new ResolvedIssue(issue, articles);
    }
}