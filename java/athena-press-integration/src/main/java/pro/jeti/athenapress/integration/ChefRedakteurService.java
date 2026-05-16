package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.util.List;

import pro.jeti.athenapress.model.Article;
import pro.jeti.athenapress.model.Issue;
import pro.jeti.athenapress.service.ArticleStatusService;
import pro.jeti.athenapress.service.IssueWriteService;
import pro.jeti.athenapress.service.ValidationService;
import pro.jeti.athenapress.repository.ArticleRepository;
import pro.jeti.athenapress.repository.IssueRepository;

public class ChefRedakteurService {

    private final ArticleStatusService articleStatusService;
    private final IssueWriteService issueWriteService;
    private final ValidationService validationService;
    private final ArticleRepository articleRepository;
    private final IssueRepository issueRepository;
    private final ChefRedakteurCommandService commandService;

    public ChefRedakteurService(
            ArticleStatusService articleStatusService,
            IssueWriteService issueWriteService,
            ValidationService validationService,
            ArticleRepository articleRepository,
            IssueRepository issueRepository
    ) {
        this.articleStatusService = articleStatusService;
        this.issueWriteService    = issueWriteService;
        this.validationService    = validationService;
        this.articleRepository    = articleRepository;
        this.issueRepository      = issueRepository;
        this.commandService       = new ChefRedakteurCommandService();
    }

    public String handle(String[] args) throws IOException {
        ChefRedakteurCommand command = commandService.parse(args);
        return switch (command.type()) {
            case PUBLISH  -> handlePublish(command);
            case ARCHIVE  -> handleArchive(command);
            case DELIVER  -> handleDeliver(command);
            case DELETE   -> handleDelete(command);
            case VALIDATE -> handleValidate(command);
            case STATUS   -> handleStatus();
            case LIST     -> handleList();
            case HELP     -> commandService.helpText();
            case UNKNOWN  -> "Unbekannter Befehl: " + command.targetId()
                             + "\n" + commandService.helpText();
        };
    }

    public String helpText() {
        return commandService.helpText();
    }

    private String handlePublish(ChefRedakteurCommand command) throws IOException {
        if (command.targetId() == null) return "Fehler: ID fehlt. Beispiel: /ap publish article_0001";
        if (command.targetsArticle()) return articleStatusService.publishArticle(command.targetId());
        if (command.targetsIssue())   return issueWriteService.publishIssue(command.targetId());
        return "Unbekannte ID: " + command.targetId() + " (erwartet article_* oder issue_*)";
    }

    private String handleArchive(ChefRedakteurCommand command) throws IOException {
        if (command.targetId() == null) return "Fehler: ID fehlt. Beispiel: /ap archive issue_0002";
        if (command.targetsArticle()) return articleStatusService.archiveArticle(command.targetId());
        if (command.targetsIssue())   return issueWriteService.archiveIssue(command.targetId());
        return "Unbekannte ID: " + command.targetId();
    }

    private String handleDeliver(ChefRedakteurCommand command) throws IOException {
        if (command.targetId() == null) return "Fehler: ID fehlt. Beispiel: /ap deliver issue_0003";
        if (command.targetsIssue()) return issueWriteService.deliverIssue(command.targetId());
        return "Zustellen ist nur fuer Ausgaben (issue_*) moeglich.";
    }

    private String handleDelete(ChefRedakteurCommand command) throws IOException {
        if (command.targetId() == null) return "Fehler: ID fehlt. Beispiel: /ap delete article_0016";
        if (command.targetsArticle()) return articleStatusService.deleteDraft(command.targetId());
        if (command.targetsIssue())   return issueWriteService.deleteDraft(command.targetId());
        return "Unbekannte ID: " + command.targetId();
    }

    private String handleValidate(ChefRedakteurCommand command) throws IOException {
        if (command.targetId() == null) return "Fehler: ID fehlt. Beispiel: /ap validate issue_0003";
        var result = validationService.validateIssueForDelivery(command.targetId());
        if (result.isValid()) return "OK – Keine Fehler gefunden fuer: " + command.targetId();
        StringBuilder sb = new StringBuilder("Fehler in ").append(command.targetId()).append(":\n");
        result.errors().forEach(e -> sb.append("- ").append(e).append("\n"));
        return sb.toString();
    }

    private String handleStatus() throws IOException {
        List<Article> articles = articleRepository.findAll();
        List<Issue>   issues   = issueRepository.findAll();

        long pubArticles  = articles.stream().filter(a -> "published".equals(a.status())).count();
        long draftArticles= articles.stream().filter(a -> "draft".equals(a.status())).count();
        long pubIssues    = issues.stream().filter(i -> "published".equals(i.status())).count();
        long draftIssues  = issues.stream().filter(i -> "draft".equals(i.status())).count();

        return "\nAthena Botenblatt – Status\n" +
               "---------------------------\n" +
               "Artikel:  " + articles.size() + " gesamt  (" + pubArticles + " veroeffentlicht, " + draftArticles + " Entwurf)\n" +
               "Ausgaben: " + issues.size()   + " gesamt  (" + pubIssues   + " veroeffentlicht, " + draftIssues   + " Entwurf)\n";
    }

    private String handleList() throws IOException {
        List<Article> articles = articleRepository.findAll();
        List<Issue>   issues   = issueRepository.findAll();

        StringBuilder sb = new StringBuilder("\nArtikel:\n");
        articles.stream()
                .sorted((a, b) -> a.id().compareTo(b.id()))
                .forEach(a -> sb.append("  ").append(a.id())
                        .append(" [").append(statusLabel(a.status())).append("] ")
                        .append(a.title()).append("\n"));

        sb.append("\nAusgaben:\n");
        issues.stream()
                .sorted((a, b) -> a.id().compareTo(b.id()))
                .forEach(i -> sb.append("  ").append(i.id())
                        .append(" [").append(statusLabel(i.status())).append("] ")
                        .append(i.title()).append("\n"));

        return sb.toString();
    }

    private String statusLabel(String status) {
        return switch (status) {
            case "published" -> "veröffentlicht";
            case "draft"     -> "Entwurf";
            case "archived"  -> "archiviert";
            default          -> status;
        };
    }
}
