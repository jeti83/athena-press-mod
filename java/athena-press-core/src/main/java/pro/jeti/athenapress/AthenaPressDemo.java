package pro.jeti.athenapress;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import pro.jeti.athenapress.model.DeliveryTarget;
import pro.jeti.athenapress.model.Issue;
import pro.jeti.athenapress.model.ResolvedIssue;
import pro.jeti.athenapress.repository.ArticleRepository;
import pro.jeti.athenapress.repository.IssueRepository;
import pro.jeti.athenapress.repository.SubscriberRepository;
import pro.jeti.athenapress.service.DeliveryService;
import pro.jeti.athenapress.service.PressService;
import pro.jeti.athenapress.service.PreviewService;
import pro.jeti.athenapress.service.ValidationResult;
import pro.jeti.athenapress.service.ValidationService;

public final class AthenaPressDemo {

    private static final String DEFAULT_ISSUE_ID = "issue_0002";

    private AthenaPressDemo() {
    }

    public static void main(String[] args) throws IOException {
        Path dataRoot = findDataRoot();

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
        PreviewService previewService = new PreviewService();

        if (args.length > 0 && isHelpArgument(args[0])) {
            printHelp();
            return;
        }

        if (args.length > 0 && "--list".equalsIgnoreCase(args[0])) {
            printPublishedIssues(pressService.findPublishedIssues());
            return;
        }

        String issueId = args.length > 0 ? args[0] : DEFAULT_ISSUE_ID;

        ValidationResult validationResult = validationService.validateIssueForDelivery(issueId);
        ResolvedIssue resolvedIssue = pressService.resolveIssue(issueId);
        List<DeliveryTarget> deliveryTargets = deliveryService.createDeliveryPlan(issueId);

        String preview = previewService.createTextPreview(
                resolvedIssue,
                validationResult,
                deliveryTargets
        );

        System.out.print(preview);
    }

    private static boolean isHelpArgument(String argument) {
        return "--help".equalsIgnoreCase(argument)
                || "-h".equalsIgnoreCase(argument)
                || "/?".equalsIgnoreCase(argument);
    }

    private static void printHelp() {
        System.out.println();
        System.out.println("AthenaPress Demo");
        System.out.println();
        System.out.println("Verwendung:");
        System.out.println("  AthenaPressDemo                 Zeigt die Standardausgabe " + DEFAULT_ISSUE_ID);
        System.out.println("  AthenaPressDemo <issueId>       Zeigt eine bestimmte Ausgabe");
        System.out.println("  AthenaPressDemo --list          Listet veröffentlichte Ausgaben");
        System.out.println("  AthenaPressDemo --help          Zeigt diese Hilfe");
        System.out.println();
        System.out.println("Beispiele:");
        System.out.println("  AthenaPressDemo issue_0002");
        System.out.println("  AthenaPressDemo --list");
        System.out.println();
    }

    private static void printPublishedIssues(List<Issue> issues) {
        System.out.println();
        System.out.println("Veröffentlichte Ausgaben:");
        System.out.println();

        if (issues == null || issues.isEmpty()) {
            System.out.println("- Keine veröffentlichten Ausgaben gefunden");
            System.out.println();
            return;
        }

        for (Issue issue : issues) {
            String issueNumber = issue.issueNumber() == null
                    ? ""
                    : " #" + issue.issueNumber();

            System.out.println("- "
                    + safeText(issue.id())
                    + issueNumber
                    + " | "
                    + safeText(issue.title())
            );
        }

        System.out.println();
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

    private static String safeText(String value) {
        if (value == null || value.isBlank()) {
            return "(leer)";
        }

        return value;
    }
}