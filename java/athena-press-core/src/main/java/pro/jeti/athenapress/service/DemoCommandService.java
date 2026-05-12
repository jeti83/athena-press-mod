package pro.jeti.athenapress.service;

import java.util.List;

import pro.jeti.athenapress.model.Issue;

public class DemoCommandService {

    public static final String DEFAULT_ISSUE_ID = "issue_0002";

    public DemoCommand parse(String[] args) {
        if (args == null || args.length == 0) {
            return DemoCommand.previewIssue(DEFAULT_ISSUE_ID);
        }

        String argument = args[0];

        if (isHelpArgument(argument)) {
            return DemoCommand.showHelp();
        }

        if ("--list".equalsIgnoreCase(argument)) {
            return DemoCommand.listPublishedIssues();
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
        help.append("  AthenaPressDemo <issueId>       Zeigt eine bestimmte Ausgabe\n");
        help.append("  AthenaPressDemo --list          Listet veröffentlichte Ausgaben\n");
        help.append("  AthenaPressDemo --help          Zeigt diese Hilfe\n");
        help.append("\n");
        help.append("Beispiele:\n");
        help.append("  AthenaPressDemo issue_0002\n");
        help.append("  AthenaPressDemo --list\n");
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

    private boolean isHelpArgument(String argument) {
        return "--help".equalsIgnoreCase(argument)
                || "-h".equalsIgnoreCase(argument)
                || "/?".equalsIgnoreCase(argument);
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
        PREVIEW_ISSUE
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
    }
}