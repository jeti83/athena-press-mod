package pro.jeti.athenapress;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import pro.jeti.athenapress.model.Article;
import pro.jeti.athenapress.model.DeliveryTarget;
import pro.jeti.athenapress.model.Issue;
import pro.jeti.athenapress.model.ResolvedIssue;
import pro.jeti.athenapress.repository.ArticleRepository;
import pro.jeti.athenapress.repository.IssueRepository;
import pro.jeti.athenapress.repository.SubscriberRepository;
import pro.jeti.athenapress.service.DeliveryService;
import pro.jeti.athenapress.service.PressService;
import pro.jeti.athenapress.service.ValidationResult;
import pro.jeti.athenapress.service.ValidationService;

public final class AthenaPressDemo {

    private AthenaPressDemo() {
    }

    public static void main(String[] args) throws IOException {
        Path dataRoot = findDataRoot();
        String issueId = args.length > 0 ? args[0] : "issue_0002";

        ArticleRepository articleRepository = new ArticleRepository(dataRoot);
        IssueRepository issueRepository = new IssueRepository(dataRoot);
        SubscriberRepository subscriberRepository = new SubscriberRepository(dataRoot);

        PressService pressService = new PressService(dataRoot);
        DeliveryService deliveryService = new DeliveryService(dataRoot);
        ValidationService validationService = new ValidationService(
                articleRepository,
                issueRepository,
                subscriberRepository
        );

        ValidationResult validation = validationService.validateIssueForDelivery(issueId);
        ResolvedIssue resolvedIssue = pressService.resolveIssue(issueId);

        printHeader();

        if (resolvedIssue == null) {
            System.out.println("Ausgabe nicht gefunden: " + issueId);
            printFooter();
            return;
        }

        Issue issue = resolvedIssue.issue();

        System.out.println(issue.title());

        String subtitle = safeText(issue.subtitle());
        if (!subtitle.isBlank()) {
            System.out.println(subtitle);
        }

        System.out.println();
        System.out.println("Status: " + safeText(issue.status()));

        if (validation.isValid()) {
            System.out.println("Validierung: OK");
        } else {
            System.out.println("Validierung: FEHLER");
            for (String error : validation.errors()) {
                System.out.println("- " + error);
            }
        }

        System.out.println();
        System.out.println("Artikel:");
        printArticles(resolvedIssue.articles());

        System.out.println();
        System.out.println("Zustellplan:");
        printDeliveryPlan(deliveryService.createDeliveryPlan(issueId));

        printFooter();
    }

    private static Path findDataRoot() {
        List<Path> candidates = List.of(
                Path.of("AthenaPress"),
                Path.of("..", "AthenaPress"),
                Path.of("..", "..", "AthenaPress")
        );

        for (Path candidate : candidates) {
            Path normalized = candidate.toAbsolutePath().normalize();

            if (Files.isDirectory(normalized)) {
                return normalized;
            }
        }

        throw new IllegalStateException("AthenaPress-Datenordner wurde nicht gefunden.");
    }

    private static void printHeader() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("        ATHENA BOTENBLATT");
        System.out.println("========================================");
        System.out.println();
    }

    private static void printArticles(List<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            System.out.println("- Keine Artikel eingetragen");
            return;
        }

        for (Article article : articles) {
            System.out.println("- [" + safeText(article.categoryId()) + "] " + safeText(article.title()));
        }
    }

    private static void printDeliveryPlan(List<DeliveryTarget> deliveryTargets) {
        if (deliveryTargets == null || deliveryTargets.isEmpty()) {
            System.out.println("- Keine Empfänger");
            return;
        }

        for (DeliveryTarget target : deliveryTargets) {
            System.out.println("- "
                    + safeText(target.playerName())
                    + " -> "
                    + safeText(target.deliveryMode())
                    + " -> unread "
                    + target.unread()
            );
        }
    }

    private static void printFooter() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("Demo abgeschlossen.");
        System.out.println("========================================");
        System.out.println();
    }

    private static String safeText(String value) {
        if (value == null || value.isBlank()) {
            return "(leer)";
        }

        return value;
    }
}