package pro.jeti.athenapress.service;

import java.util.List;

import pro.jeti.athenapress.model.Article;
import pro.jeti.athenapress.model.Category;
import pro.jeti.athenapress.model.Issue;
import pro.jeti.athenapress.model.Subscriber;

public class DemoCommandService {

    public static final String DEFAULT_ISSUE_ID = "issue_0002";

    private static final List<String> HELP_ARGUMENTS = List.of("--help", "--hilfe", "-h", "/?");
    private static final List<String> LIST_ARGUMENTS = List.of("--list", "--liste");
    private static final List<String> VALIDATE_ARGUMENTS = List.of("--validate", "--pruefen");
    private static final List<String> STATUS_ARGUMENTS = List.of("--status", "--uebersicht");

    private final ValidationReportService validationReportService = new ValidationReportService();

    public DemoCommand parse(String[] args) {
        if (args == null || args.length == 0) {
            return DemoCommand.previewIssue(DEFAULT_ISSUE_ID);
        }

        String argument = args[0];

        if (matchesArgument(argument, HELP_ARGUMENTS)) {
            return DemoCommand.showHelp();
        }

        if (matchesArgument(argument, LIST_ARGUMENTS)) {
            return DemoCommand.listPublishedIssues();
        }

        if (matchesArgument(argument, VALIDATE_ARGUMENTS)) {
            String issueId = args.length >= 2 ? args[1] : DEFAULT_ISSUE_ID;
            return DemoCommand.validateIssue(issueId);
        }

        if (matchesArgument(argument, STATUS_ARGUMENTS)) {
            return DemoCommand.showStatus();
        }

        return DemoCommand.previewIssue(argument);
    }

    public String createHelpText() {
        StringBuilder help = new StringBuilder();

        help.append("\n");
        help.append("AthenaPress Demo\n");
        help.append("========================================\n");
        help.append("\n");

        help.append("Verwendung:\n");
        help.append("  AthenaPressDemo\n");
        help.append("      Zeigt die Standardausgabe ")
                .append(DEFAULT_ISSUE_ID)
                .append(" als Preview.\n");
        help.append("\n");

        help.append("  AthenaPressDemo <issueId>\n");
        help.append("      Zeigt eine bestimmte Ausgabe als Preview.\n");
        help.append("\n");

        help.append("  AthenaPressDemo --list | --liste\n");
        help.append("      Listet alle veröffentlichten Ausgaben.\n");
        help.append("\n");

        help.append("  AthenaPressDemo --validate | --pruefen <issueId>\n");
        help.append("      Prüft eine Ausgabe ohne Preview.\n");
        help.append("      Ohne issueId wird ")
                .append(DEFAULT_ISSUE_ID)
                .append(" verwendet.\n");
        help.append("\n");

        help.append("  AthenaPressDemo --status | --uebersicht\n");
        help.append("      Zeigt eine kompakte Übersicht über den Datenbestand.\n");
        help.append("\n");

        help.append("  AthenaPressDemo --help | --hilfe | -h | /?\n");
        help.append("      Zeigt diese Hilfe.\n");
        help.append("\n");

        help.append("Beispiele:\n");
        help.append("  AthenaPressDemo issue_0002\n");
        help.append("  AthenaPressDemo --list\n");
        help.append("  AthenaPressDemo --liste\n");
        help.append("  AthenaPressDemo --validate issue_0002\n");
        help.append("  AthenaPressDemo --pruefen issue_0002\n");
        help.append("  AthenaPressDemo --status\n");
        help.append("  AthenaPressDemo --uebersicht\n");
        help.append("\n");

        return help.toString();
    }

    public String createPublishedIssuesText(List<Issue> issues) {
        StringBuilder text = new StringBuilder();

        text.append("\n");
        text.append("Veröffentlichte Ausgaben:\n");
        text.append("\n");

        if (issues == null || issues.isEmpty()) {
            text.append("- Keine veröffentlichten Ausgaben gefunden\n");
            text.append("\n");
            return text.toString();
        }

        for (Issue issue : issues) {
            String issueNumber = issue.issueNumber() == null
                    ? ""
                    : " #" + issue.issueNumber();

            text.append("- ")
                    .append(safeText(issue.id()))
                    .append(issueNumber)
                    .append(" | ")
                    .append(safeText(issue.title()))
                    .append("\n");
        }

        text.append("\n");

        return text.toString();
    }

    public String createValidationText(String issueId, ValidationResult validationResult) {
        return validationReportService.createStandaloneValidationText(
                "Validierung für " + safeText(issueId),
                validationResult
        );
    }

    public String createStatusText(
            List<Article> articles,
            List<Issue> issues,
            List<Subscriber> subscribers,
            List<Category> categories,
            ValidationResult validationResult
    ) {
        StringBuilder text = new StringBuilder();

        text.append("\n");
        text.append("AthenaPress Status\n");
        text.append("----------------------------------------\n");
        text.append("Artikel: ").append(sizeOf(articles)).append("\n");
        text.append("Ausgaben: ").append(sizeOf(issues)).append("\n");
        text.append("Abonnenten: ")
                .append(sizeOf(subscribers))
                .append(" (aktiv: ")
                .append(countActiveSubscribers(subscribers))
                .append(")\n");
        text.append("Kategorien: ")
                .append(sizeOf(categories))
                .append(" (aktiv: ")
                .append(countEnabledCategories(categories))
                .append(")\n");
        text.append("\n");
        text.append(validationReportService.createInlineValidationText(validationResult));
        text.append("\n");

        return text.toString();
    }

    private int sizeOf(List<?> values) {
        if (values == null) {
            return 0;
        }

        return values.size();
    }

    private long countActiveSubscribers(List<Subscriber> subscribers) {
        if (subscribers == null) {
            return 0;
        }

        return subscribers.stream()
                .filter(Subscriber::subscribed)
                .count();
    }

    private long countEnabledCategories(List<Category> categories) {
        if (categories == null) {
            return 0;
        }

        return categories.stream()
                .filter(Category::enabled)
                .count();
    }

    private boolean matchesArgument(String argument, List<String> aliases) {
        return aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(argument));
    }

    private String safeText(String value) {
        if (value == null || value.isBlank()) {
            return "(leer)";
        }

        return value;
    }

    public enum DemoCommandType {
        SHOW_HELP,
        LIST_PUBLISHED_ISSUES,
        PREVIEW_ISSUE,
        VALIDATE_ISSUE,
        SHOW_STATUS
    }

    public record DemoCommand(
            DemoCommandType type,
            String issueId
    ) {
        public static DemoCommand showHelp() {
            return new DemoCommand(DemoCommandType.SHOW_HELP, null);
        }

        public static DemoCommand listPublishedIssues() {
            return new DemoCommand(DemoCommandType.LIST_PUBLISHED_ISSUES, null);
        }

        public static DemoCommand previewIssue(String issueId) {
            return new DemoCommand(DemoCommandType.PREVIEW_ISSUE, issueId);
        }

        public static DemoCommand validateIssue(String issueId) {
            return new DemoCommand(DemoCommandType.VALIDATE_ISSUE, issueId);
        }

        public static DemoCommand showStatus() {
            return new DemoCommand(DemoCommandType.SHOW_STATUS, null);
        }
    }
}