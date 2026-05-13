package pro.jeti.athenapress.service;

import java.util.Comparator;
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

        if (matchesArgument(argument, STATUS_ARGUMENTS)) {
            return DemoCommand.statusOverview();
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
        help.append(" AthenaPressDemo                         Zeigt die Standardausgabe ")
                .append(DEFAULT_ISSUE_ID)
                .append("\n");
        help.append(" AthenaPressDemo <issueId>               Zeigt eine bestimmte Ausgabe\n");
        help.append(" AthenaPressDemo --list | --liste        Listet veröffentlichte Ausgaben\n");
        help.append(" AthenaPressDemo --validate | --pruefen <issueId> Prüft eine Ausgabe ohne Preview\n");
        help.append(" AthenaPressDemo --status | --uebersicht Zeigt eine kompakte Statusübersicht\n");
        help.append(" AthenaPressDemo --help | --hilfe | -h | /? Zeigt diese Hilfe\n");
        help.append("\n");
        help.append("Beispiele:\n");
        help.append(" AthenaPressDemo issue_0002\n");
        help.append(" AthenaPressDemo --list\n");
        help.append(" AthenaPressDemo --validate issue_0002\n");
        help.append(" AthenaPressDemo --status\n");
        help.append("\n");
        help.append("Ohne issueId wird ")
                .append(DEFAULT_ISSUE_ID)
                .append(" verwendet.\n");
        help.append("\n");

        return help.toString();
    }

    public String createPublishedIssuesText(List<Issue> issues) {
        StringBuilder text = new StringBuilder();

        text.append("\n");
        text.append("Veröffentlichte Ausgaben:\n");
        text.append("----------------------------------------\n");

        List<Issue> safeIssues = safeIssueList(issues);

        if (safeIssues.isEmpty()) {
            text.append("- Keine veröffentlichten Ausgaben gefunden\n");
            text.append("\n");
            return text.toString();
        }

        for (Issue issue : safeIssues) {
            appendCompactIssueLine(text, issue);
        }

        text.append("\n");

        return text.toString();
    }

    public String createStatusText(
            List<Article> articles,
            List<Issue> issues,
            List<Subscriber> subscribers,
            List<Category> categories,
            ValidationResult validationResult
    ) {
        List<Article> safeArticles = safeArticleList(articles);
        List<Issue> safeIssues = safeIssueList(issues);
        List<Subscriber> safeSubscribers = subscribers == null ? List.of() : subscribers;
        List<Category> safeCategories = categories == null ? List.of() : categories;

        long activeSubscribers = safeSubscribers.stream()
                .filter(Subscriber::subscribed)
                .count();

        long enabledCategories = safeCategories.stream()
                .filter(Category::enabled)
                .count();

        StringBuilder text = new StringBuilder();

        text.append("\n");
        text.append("AthenaPress Status\n");
        text.append("----------------------------------------\n");
        text.append("Artikel: ")
                .append(safeArticles.size())
                .append("\n");
        text.append("Ausgaben: ")
                .append(safeIssues.size())
                .append("\n");
        text.append("Abonnenten: ")
                .append(safeSubscribers.size())
                .append(" (aktiv: ")
                .append(activeSubscribers)
                .append(")\n");
        text.append("Kategorien: ")
                .append(safeCategories.size())
                .append(" (aktiv: ")
                .append(enabledCategories)
                .append(")\n");

        text.append("\n");
        appendIssueSummary(text, safeIssues);

        text.append("\n");
        appendArticleSummary(text, safeArticles);

        text.append("\n");
        appendValidationSummary(text, validationResult);
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

    private void appendIssueSummary(StringBuilder text, List<Issue> issues) {
        text.append("Ausgabenliste\n");
        text.append("----------------------------------------\n");

        if (issues.isEmpty()) {
            text.append("- Keine Ausgaben gefunden\n");
            return;
        }

        appendIssuesByStatus(text, "Veröffentlichte Ausgaben", issues, "published");
        appendIssuesByStatus(text, "Entwürfe", issues, "draft");
        appendIssuesByStatus(text, "Archivierte Ausgaben", issues, "archived");
        appendIssuesByStatus(text, "Ausgaben mit anderem Status", issues, null);
    }

    private void appendIssuesByStatus(
            StringBuilder text,
            String title,
            List<Issue> issues,
            String status
    ) {
        List<Issue> matchingIssues = issues.stream()
                .filter(issue -> matchesIssueStatus(issue, status))
                .sorted(issueComparator())
                .toList();

        if (matchingIssues.isEmpty()) {
            return;
        }

        text.append(title).append(":\n");

        for (Issue issue : matchingIssues) {
            appendDetailedIssueLine(text, issue);
        }

        text.append("\n");
    }

    private void appendArticleSummary(StringBuilder text, List<Article> articles) {
        text.append("Artikelliste\n");
        text.append("----------------------------------------\n");

        if (articles.isEmpty()) {
            text.append("- Keine Artikel gefunden\n");
            return;
        }

        appendArticlesByStatus(text, "Veröffentlichte Artikel", articles, "published");
        appendArticlesByStatus(text, "Entwürfe", articles, "draft");
        appendArticlesByStatus(text, "Archivierte Artikel", articles, "archived");
        appendArticlesByStatus(text, "Artikel mit anderem Status", articles, null);
    }

    private void appendArticlesByStatus(
            StringBuilder text,
            String title,
            List<Article> articles,
            String status
    ) {
        List<Article> matchingArticles = articles.stream()
                .filter(article -> matchesArticleStatus(article, status))
                .sorted(articleComparator())
                .toList();

        if (matchingArticles.isEmpty()) {
            return;
        }

        text.append(title).append(":\n");

        for (Article article : matchingArticles) {
            appendDetailedArticleLine(text, article);
        }

        text.append("\n");
    }

    private boolean matchesIssueStatus(Issue issue, String status) {
        String issueStatus = issue == null ? null : issue.status();

        if (status == null) {
            return !equalsStatus(issueStatus, "published")
                    && !equalsStatus(issueStatus, "draft")
                    && !equalsStatus(issueStatus, "archived");
        }

        return equalsStatus(issueStatus, status);
    }

    private boolean matchesArticleStatus(Article article, String status) {
        String articleStatus = article == null ? null : article.status();

        if (status == null) {
            return !equalsStatus(articleStatus, "published")
                    && !equalsStatus(articleStatus, "draft")
                    && !equalsStatus(articleStatus, "archived");
        }

        return equalsStatus(articleStatus, status);
    }

    private boolean equalsStatus(String value, String expected) {
        if (value == null || expected == null) {
            return false;
        }

        return value.equalsIgnoreCase(expected);
    }

    private void appendCompactIssueLine(StringBuilder text, Issue issue) {
        String issueNumber = issue.issueNumber() == null ? "" : " #" + issue.issueNumber();

        text.append("- ")
                .append(safeText(issue.id()))
                .append(issueNumber)
                .append(" | ")
                .append(safeText(issue.title()))
                .append("\n");
    }

    private void appendDetailedIssueLine(StringBuilder text, Issue issue) {
        int articleCount = issue.articles() == null ? 0 : issue.articles().size();
        String issueNumber = issue.issueNumber() == null ? "-" : "#" + issue.issueNumber();
        String coverText = issue.cover() == null ? "nein" : "ja";

        text.append("- ")
                .append(safeText(issue.id()))
                .append(" | ")
                .append(issueNumber)
                .append(" | ")
                .append(safeText(issue.title()))
                .append(" | ")
                .append(safeText(issue.status()))
                .append(" | Artikel: ")
                .append(articleCount)
                .append(" | Cover: ")
                .append(coverText)
                .append("\n");

        if (issue.subtitle() != null && !issue.subtitle().isBlank()) {
            text.append("  ")
                    .append(issue.subtitle())
                    .append("\n");
        }
    }

    private void appendDetailedArticleLine(StringBuilder text, Article article) {
        String imageText = hasArticleImage(article) ? "ja" : "nein";

        text.append("- ")
                .append(safeText(article.id()))
                .append(" | ")
                .append(safeText(article.categoryId()))
                .append(" | ")
                .append(safeText(article.title()))
                .append(" | ")
                .append(safeText(article.status()))
                .append(" | Bild: ")
                .append(imageText)
                .append("\n");

        String summary = firstNonBlank(article.summary(), article.teaser(), article.subtitle());

        if (summary != null) {
            text.append("  ")
                    .append(summary)
                    .append("\n");
        }
    }

    private boolean hasArticleImage(Article article) {
        if (article == null || article.image() == null) {
            return false;
        }

        return article.image().file() != null && !article.image().file().isBlank();
    }

    private String firstNonBlank(String first, String second, String third) {
        if (first != null && !first.isBlank()) {
            return first;
        }

        if (second != null && !second.isBlank()) {
            return second;
        }

        if (third != null && !third.isBlank()) {
            return third;
        }

        return null;
    }

    private void appendValidationSummary(StringBuilder text, ValidationResult validationResult) {
        if (validationResult == null || validationResult.isValid()) {
            text.append("Validierung: OK - Keine Fehler gefunden.\n");
            return;
        }

        int errorCount = validationResult.errors().size();
        String problemText = errorCount == 1 ? "Problem" : "Probleme";

        text.append("Validierung: FEHLER - ")
                .append(errorCount)
                .append(" ")
                .append(problemText)
                .append(" gefunden.\n");

        for (String error : validationResult.errors()) {
            text.append("- ").append(error).append("\n");
        }
    }

    private List<Issue> safeIssueList(List<Issue> issues) {
        if (issues == null) {
            return List.of();
        }

        return issues.stream()
                .filter(issue -> issue != null)
                .sorted(issueComparator())
                .toList();
    }

    private List<Article> safeArticleList(List<Article> articles) {
        if (articles == null) {
            return List.of();
        }

        return articles.stream()
                .filter(article -> article != null)
                .sorted(articleComparator())
                .toList();
    }

    private Comparator<Issue> issueComparator() {
        return Comparator
                .comparingInt(this::issueStatusRank)
                .thenComparing(issue -> safeText(issue.id()));
    }

    private Comparator<Article> articleComparator() {
        return Comparator
                .comparingInt(this::articleStatusRank)
                .thenComparing(article -> safeText(article.id()));
    }

    private int issueStatusRank(Issue issue) {
        if (issue == null || issue.status() == null) {
            return 99;
        }

        return switch (issue.status().toLowerCase()) {
            case "published" -> 1;
            case "draft" -> 2;
            case "archived" -> 3;
            default -> 90;
        };
    }

    private int articleStatusRank(Article article) {
        if (article == null || article.status() == null) {
            return 99;
        }

        return switch (article.status().toLowerCase()) {
            case "published" -> 1;
            case "draft" -> 2;
            case "archived" -> 3;
            default -> 90;
        };
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
        SHOW_STATUS,
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

        public static DemoCommand statusOverview() {
            return new DemoCommand(DemoCommandType.SHOW_STATUS, null);
        }

        public static DemoCommand previewIssue(String issueId) {
            return new DemoCommand(DemoCommandType.PREVIEW_ISSUE, issueId);
        }

        public static DemoCommand validateIssue(String issueId) {
            return new DemoCommand(DemoCommandType.VALIDATE_ISSUE, issueId);
        }
    }
}