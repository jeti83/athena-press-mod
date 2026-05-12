package pro.jeti.athenapress;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import pro.jeti.athenapress.model.DeliveryTarget;
import pro.jeti.athenapress.model.ResolvedIssue;
import pro.jeti.athenapress.repository.ArticleRepository;
import pro.jeti.athenapress.repository.IssueRepository;
import pro.jeti.athenapress.repository.SubscriberRepository;
import pro.jeti.athenapress.service.DeliveryService;
import pro.jeti.athenapress.service.DemoCommandService;
import pro.jeti.athenapress.service.DemoCommandService.DemoCommand;
import pro.jeti.athenapress.service.DemoCommandService.DemoCommandType;
import pro.jeti.athenapress.service.PressService;
import pro.jeti.athenapress.service.PreviewService;
import pro.jeti.athenapress.service.ValidationResult;
import pro.jeti.athenapress.service.ValidationService;

public final class AthenaPressDemo {

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
        DemoCommandService demoCommandService = new DemoCommandService();

        DemoCommand command = demoCommandService.parse(args);

        if (command.type() == DemoCommandType.SHOW_HELP) {
            System.out.print(demoCommandService.createHelpText());
            return;
        }

        if (command.type() == DemoCommandType.LIST_PUBLISHED_ISSUES) {
            System.out.print(demoCommandService.createPublishedIssuesText(
                    pressService.findPublishedIssues()
            ));
            return;
        }

        String issueId = command.issueId();

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