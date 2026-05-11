package pro.jeti.athenapress.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import pro.jeti.athenapress.model.Article;
import pro.jeti.athenapress.model.Issue;
import pro.jeti.athenapress.model.Subscriber;
import pro.jeti.athenapress.repository.ArticleRepository;
import pro.jeti.athenapress.repository.IssueRepository;
import pro.jeti.athenapress.repository.SubscriberRepository;

public class ValidationService {

    private static final Set<String> VALID_DELIVERY_MODES = Set.of(
            "notification_only",
            "item_only",
            "item_and_notification",
            "mailbox"
    );

    private static final Set<String> VALID_CONTENT_STATUSES = Set.of(
            "draft",
            "published",
            "archived"
    );

    private final ArticleRepository articleRepository;
    private final IssueRepository issueRepository;
    private final SubscriberRepository subscriberRepository;

    public ValidationService(
            ArticleRepository articleRepository,
            IssueRepository issueRepository,
            SubscriberRepository subscriberRepository
    ) {
        this.articleRepository = articleRepository;
        this.issueRepository = issueRepository;
        this.subscriberRepository = subscriberRepository;
    }

    public ValidationResult validateIssueForDelivery(String issueId) {
        List<String> errors = new ArrayList<>();

        addErrors(errors, validateIssueRequiredFields(issueId));
        addErrors(errors, validateIssueStatus(issueId));
        addErrors(errors, validateIssueArticleReferences(issueId));
        addErrors(errors, validateSubscriberDeliveryModes());

        if (errors.isEmpty()) {
            return ValidationResult.valid();
        }

        return ValidationResult.invalid(errors);
    }

    public ValidationResult validateIssueArticleReferences(String issueId) {
        List<String> errors = new ArrayList<>();

        Issue issue;

        try {
            issue = issueRepository.findById(issueId);
        } catch (Exception exception) {
            errors.add("Could not read issue " + issueId + ": " + exception.getMessage());
            return ValidationResult.invalid(errors);
        }

        if (issue == null) {
            errors.add("Issue not found: " + issueId);
            return ValidationResult.invalid(errors);
        }

        for (String articleId : issue.articles()) {
            Article article;

            try {
                article = articleRepository.findById(articleId);
            } catch (Exception exception) {
                errors.add("Could not read article " + articleId + ": " + exception.getMessage());
                continue;
            }

            if (article == null) {
                errors.add("Issue " + issueId + " references missing article: " + articleId);
            }
        }

        if (errors.isEmpty()) {
            return ValidationResult.valid();
        }

        return ValidationResult.invalid(errors);
    }

    public ValidationResult validateSubscriberDeliveryModes() {
        List<String> errors = new ArrayList<>();

        List<Subscriber> subscribers;

        try {
            subscribers = subscriberRepository.findAll();
        } catch (Exception exception) {
            errors.add("Could not read subscribers: " + exception.getMessage());
            return ValidationResult.invalid(errors);
        }

        for (Subscriber subscriber : subscribers) {
            String playerName = subscriber.playerName();
            String deliveryMode = subscriber.deliveryMode();

            if (isBlank(deliveryMode)) {
                errors.add("Subscriber " + playerName + " has missing deliveryMode");
                continue;
            }

            if (!VALID_DELIVERY_MODES.contains(deliveryMode)) {
                errors.add("Subscriber " + playerName + " has invalid deliveryMode: " + deliveryMode);
            }
        }

        if (errors.isEmpty()) {
            return ValidationResult.valid();
        }

        return ValidationResult.invalid(errors);
    }

    public ValidationResult validateArticleStatus(String articleId) {
        List<String> errors = new ArrayList<>();

        Article article;

        try {
            article = articleRepository.findById(articleId);
        } catch (Exception exception) {
            errors.add("Could not read article " + articleId + ": " + exception.getMessage());
            return ValidationResult.invalid(errors);
        }

        if (article == null) {
            errors.add("Article not found: " + articleId);
            return ValidationResult.invalid(errors);
        }

        String status = article.status();

        if (isBlank(status)) {
            errors.add("Article " + articleId + " has missing status");
            return ValidationResult.invalid(errors);
        }

        if (!VALID_CONTENT_STATUSES.contains(status)) {
            errors.add("Article " + articleId + " has invalid status: " + status);
            return ValidationResult.invalid(errors);
        }

        return ValidationResult.valid();
    }

    public ValidationResult validateIssueStatus(String issueId) {
        List<String> errors = new ArrayList<>();

        Issue issue;

        try {
            issue = issueRepository.findById(issueId);
        } catch (Exception exception) {
            errors.add("Could not read issue " + issueId + ": " + exception.getMessage());
            return ValidationResult.invalid(errors);
        }

        if (issue == null) {
            errors.add("Issue not found: " + issueId);
            return ValidationResult.invalid(errors);
        }

        String status = issue.status();

        if (isBlank(status)) {
            errors.add("Issue " + issueId + " has missing status");
            return ValidationResult.invalid(errors);
        }

        if (!VALID_CONTENT_STATUSES.contains(status)) {
            errors.add("Issue " + issueId + " has invalid status: " + status);
            return ValidationResult.invalid(errors);
        }

        return ValidationResult.valid();
    }

    public ValidationResult validateArticleRequiredFields(String articleId) {
        List<String> errors = new ArrayList<>();

        Article article;

        try {
            article = articleRepository.findById(articleId);
        } catch (Exception exception) {
            errors.add("Could not read article " + articleId + ": " + exception.getMessage());
            return ValidationResult.invalid(errors);
        }

        if (article == null) {
            errors.add("Article not found: " + articleId);
            return ValidationResult.invalid(errors);
        }

        if (isBlank(article.id())) {
            errors.add("Article " + articleId + " has missing id");
        }

        if (isBlank(article.title())) {
            errors.add("Article " + articleId + " has missing title");
        }

        if (isBlank(article.categoryId())) {
            errors.add("Article " + articleId + " has missing categoryId");
        }

        if (errors.isEmpty()) {
            return ValidationResult.valid();
        }

        return ValidationResult.invalid(errors);
    }

    public ValidationResult validateIssueRequiredFields(String issueId) {
        List<String> errors = new ArrayList<>();

        Issue issue;

        try {
            issue = issueRepository.findById(issueId);
        } catch (Exception exception) {
            errors.add("Could not read issue " + issueId + ": " + exception.getMessage());
            return ValidationResult.invalid(errors);
        }

        if (issue == null) {
            errors.add("Issue not found: " + issueId);
            return ValidationResult.invalid(errors);
        }

        if (isBlank(issue.id())) {
            errors.add("Issue " + issueId + " has missing id");
        }

        if (isBlank(issue.title())) {
            errors.add("Issue " + issueId + " has missing title");
        }

        if (issue.articles() == null) {
            errors.add("Issue " + issueId + " has missing articles");
        }

        if (errors.isEmpty()) {
            return ValidationResult.valid();
        }

        return ValidationResult.invalid(errors);
    }

    private void addErrors(List<String> errors, ValidationResult result) {
        if (!result.isValid()) {
            errors.addAll(result.errors());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}