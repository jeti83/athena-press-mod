package pro.jeti.athenapress.service;

import java.util.List;

public class DemoCommandService {

    public static final String DEFAULT_ISSUE_ID = "issue_0002";

    private static final List<String> HELP_ARGUMENTS = List.of("--help", "--hilfe", "-h", "/?");
    private static final List<String> LIST_ARGUMENTS = List.of("--list", "--liste");
    private static final List<String> VALIDATE_ARGUMENTS = List.of("--validate", "--pruefen");
    private static final List<String> STATUS_ARGUMENTS = List.of("--status", "--uebersicht");
    private static final List<String> ARTICLE_ARGUMENTS = List.of("--articles", "--artikel");

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

        if (matchesArgument(argument, ARTICLE_ARGUMENTS)) {
            return DemoCommand.listArticles();
        }

        if (matchesArgument(argument, STATUS_ARGUMENTS)) {
            return DemoCommand.statusOverview();
        }

        if (matchesArgument(argument, VALIDATE_ARGUMENTS)) {
            String issueId = args.length >= 2 ? args[1] : DEFAULT_ISSUE_ID;
            return DemoCommand.validateIssue(issueId);
        }

        return DemoCommand.previewIssue(argument);
    }

    private boolean matchesArgument(String argument, List<String> aliases) {
        return aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(argument));
    }
}
