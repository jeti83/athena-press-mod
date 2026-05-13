package pro.jeti.athenapress.service;

import java.util.List;

import pro.jeti.athenapress.model.Issue;

public class DemoCommandService {

    public static final String DEFAULT_ISSUE_ID = "issue_0002";

    private static final List<String> HELP_ARGUMENTS = List.of("--help", "--hilfe", "-h", "/?");
    private static final List<String> LIST_ARGUMENTS = List.of("--list", "--liste");
    private static final List<String> VALIDATE_ARGUMENTS = List.of("--validate", "--pruefen");

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

        return DemoCommand.previewIssue(argument);
    }

    public String createHelpText() {
        StringBuilder help = new StringBuilder();

        help.append("\n");
        help.append("AthenaPress Demo\n");
        help.append("\n");
        help.append("Verwendung:\n");
        help.append("  AthenaPressDemo                 Zeigt die Standardausgabe ")
                .append(DEFAULT_ISSUE_ID)
                .append("\n");
        help.append("  AthenaPressDemo <issueId>                        Zeigt eine bestimmte Ausgabe\n");
        help.append("  AthenaPressDemo --list|--liste                   Listet veröffentlichte Ausgaben\n");
        help.append("  AthenaPressDemo --validate|--pruefen <issueId>   Prüft eine Ausgabe ohne Preview\n");
        help.append("  AthenaPressDemo --help|--hilfe                   Zeigt diese Hilfe\n");
        help.append("\n");
        help.append("Beispiele:\n");
        help.append("  AthenaPressDemo issue_0002\n");
        help.append("  AthenaPressDemo --list\n");
        help.append("  AthenaPressDemo --validate issue_0002\n");
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
        StringBuilder text = new StringBuilder();

        text.append("\n");
        text.append("Validierung für ").append(safeText(issueId)).append("\n");
        text.append("----------------------------------------\n");

        if (validationResult == null || validationResult.isValid()) {
            text.append("OK - Keine Fehler gefunden.\n");
            text.append("\n");
            return text.toString();
        }

        int errorCount = validationResult.errors().size();
        String problemText = errorCount == 1 ? "Problem" : "Probleme";

        text.append("FEHLER - ")
                .append(errorCount)
                .append(" ")
                .append(problemText)
                .append(" gefunden.\n");

        for (String error : validationResult.errors()) {
            text.append("- ").append(error).append("\n");
        }

        text.append("\n");
        return text.toString();
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
        VALIDATE_ISSUE
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
    }
}