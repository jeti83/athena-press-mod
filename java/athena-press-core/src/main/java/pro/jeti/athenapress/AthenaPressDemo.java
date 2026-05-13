package pro.jeti.athenapress;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import pro.jeti.athenapress.model.Article;
import pro.jeti.athenapress.model.Category;
import pro.jeti.athenapress.model.DeliveryTarget;
import pro.jeti.athenapress.model.Issue;
import pro.jeti.athenapress.model.ResolvedIssue;
import pro.jeti.athenapress.model.Subscriber;
import pro.jeti.athenapress.service.DemoCommandService;
import pro.jeti.athenapress.service.DemoCommandService.DemoCommand;
import pro.jeti.athenapress.service.DemoCommandService.DemoCommandType;
import pro.jeti.athenapress.service.ValidationResult;

public final class AthenaPressDemo {

    private AthenaPressDemo() {
    }

    public static void main(String[] args) throws IOException {
        Path dataRoot = findDataRoot();

        AthenaPressCore core = new AthenaPressCore(dataRoot);
        DemoCommandService demoCommandService = new DemoCommandService();
        DemoCommand command = demoCommandService.parse(args);

        if (command.type() == DemoCommandType.SHOW_HELP) {
            System.out.print(demoCommandService.createHelpText());
            return;
        }

        if (command.type() == DemoCommandType.LIST_PUBLISHED_ISSUES) {
            System.out.print(demoCommandService.createPublishedIssuesText(
                    core.getPressService().findPublishedIssues()
            ));
            return;
        }

        if (command.type() == DemoCommandType.SHOW_STATUS) {
            List<Article> articles = core.getArticleRepository().findAll();
            List<Issue> issues = core.getIssueRepository().findAll();
            List<Subscriber> subscribers = core.getSubscriberRepository().findAll();
            List<Category> categories = core.getCategoryRepository().findAll();
            ValidationResult validationResult = core.getValidationService().validate();

            System.out.print(demoCommandService.createStatusText(
                    articles,
                    issues,
                    subscribers,
                    categories,
                    validationResult
            ));
            return;
        }

        if (command.type() == DemoCommandType.VALIDATE_ISSUE) {
            ValidationResult validationResult = core.getValidationService()
                    .validateIssueForDelivery(command.issueId());

            System.out.print(demoCommandService.createValidationText(
                    command.issueId(),
                    validationResult
            ));
            return;
        }

        String issueId = command.issueId();
        ValidationResult validationResult = core.getValidationService().validateIssueForDelivery(issueId);
        ResolvedIssue resolvedIssue = core.getPressService().resolveIssue(issueId);
        List<DeliveryTarget> deliveryTargets = core.getDeliveryService().createDeliveryPlan(issueId);

        String preview = core.getPreviewService().createTextPreview(
                resolvedIssue,
                validationResult,
                deliveryTargets
        );

        System.out.print(preview);
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

        throw new IllegalStateException(createMissingDataRootMessage(candidates));
    }

    private static String createMissingDataRootMessage(List<Path> candidates) {
        StringBuilder message = new StringBuilder();

        message.append("AthenaPress-Datenordner wurde nicht gefunden.\n");
        message.append("\n");
        message.append("Gesuchte Pfade:\n");

        for (Path candidate : candidates) {
            message.append("- ")
                    .append(candidate.toAbsolutePath().normalize())
                    .append("\n");
        }

        message.append("\n");
        message.append("Starte die Demo bitte aus dem Maven-Modulordner:\n");
        message.append("java/athena-press-core\n");
        message.append("\n");
        message.append("Oder stelle sicher, dass der Ordner AthenaPress relativ erreichbar ist.");

        return message.toString();
    }
}