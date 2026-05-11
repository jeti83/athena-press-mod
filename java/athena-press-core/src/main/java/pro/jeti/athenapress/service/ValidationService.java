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

            if (deliveryMode == null || deliveryMode.isBlank()) {
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
}