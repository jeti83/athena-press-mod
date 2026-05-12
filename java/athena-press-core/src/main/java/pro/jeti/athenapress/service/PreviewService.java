package pro.jeti.athenapress.service;

import java.util.List;

import pro.jeti.athenapress.model.Article;
import pro.jeti.athenapress.model.DeliveryTarget;
import pro.jeti.athenapress.model.Issue;
import pro.jeti.athenapress.model.ResolvedIssue;

public class PreviewService {

    private static final String LINE = "========================================";
    private static final String SECTION_LINE = "----------------------------------------";

    public String createTextPreview(
            ResolvedIssue resolvedIssue,
            ValidationResult validationResult,
            List<DeliveryTarget> deliveryTargets
    ) {
        StringBuilder preview = new StringBuilder();

        appendHeader(preview);

        if (resolvedIssue == null) {
            preview.append("Ausgabe nicht gefunden.\n");
            appendFooter(preview);
            return preview.toString();
        }

        Issue issue = resolvedIssue.issue();

        appendIssueInfo(preview, issue);
        appendValidationSection(preview, validationResult);
        appendArticleSection(preview, resolvedIssue.articles());
        appendDeliverySection(preview, deliveryTargets);
        appendFooter(preview);

        return preview.toString();
    }

    private void appendHeader(StringBuilder preview) {
        preview.append("\n");
        preview.append(LINE).append("\n");
        preview.append("        ATHENA BOTENBLATT\n");
        preview.append(LINE).append("\n");
        preview.append("\n");
    }

    private void appendIssueInfo(StringBuilder preview, Issue issue) {
        preview.append("Ausgabe\n");
        preview.append(SECTION_LINE).append("\n");
        preview.append("Titel: ").append(safeText(issue.title())).append("\n");

        String subtitle = emptyIfBlank(issue.subtitle());
        if (!subtitle.isBlank()) {
            preview.append("Untertitel: ").append(subtitle).append("\n");
        }

        preview.append("Status: ").append(safeText(issue.status())).append("\n");
        preview.append("\n");
    }

    private void appendValidationSection(StringBuilder preview, ValidationResult validationResult) {
        preview.append("Pruefung\n");
        preview.append(SECTION_LINE).append("\n");
        appendValidation(preview, validationResult);
        preview.append("\n");
    }

    private void appendValidation(StringBuilder preview, ValidationResult validationResult) {
        if (validationResult == null || validationResult.isValid()) {
            preview.append("Validierung: OK\n");
            return;
        }

        preview.append("Validierung: FEHLER\n");

        for (String error : validationResult.errors()) {
            preview.append("- ").append(error).append("\n");
        }
    }

    private void appendArticleSection(StringBuilder preview, List<Article> articles) {
        preview.append("Artikel\n");
        preview.append(SECTION_LINE).append("\n");
        appendArticles(preview, articles);
        preview.append("\n");
    }

    private void appendArticles(StringBuilder preview, List<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            preview.append("- Keine Artikel eingetragen\n");
            return;
        }

        for (Article article : articles) {
            preview.append("- [")
                    .append(safeText(article.categoryId()))
                    .append("] ")
                    .append(safeText(article.title()))
                    .append("\n");

            String summary = emptyIfBlank(article.summary());
            if (!summary.isBlank()) {
                preview.append("  ")
                        .append(summary)
                        .append("\n");
            }
        }
    }

    private void appendDeliverySection(StringBuilder preview, List<DeliveryTarget> deliveryTargets) {
        preview.append("Zustellplan\n");
        preview.append(SECTION_LINE).append("\n");
        appendDeliveryTargets(preview, deliveryTargets);
    }

    private void appendDeliveryTargets(StringBuilder preview, List<DeliveryTarget> deliveryTargets) {
        if (deliveryTargets == null || deliveryTargets.isEmpty()) {
            preview.append("- Keine Empfänger\n");
            return;
        }

        for (DeliveryTarget target : deliveryTargets) {
            preview.append("- ")
                    .append(safeText(target.playerName()))
                    .append(" -> ")
                    .append(safeText(target.deliveryMode()))
                    .append(" -> unread ")
                    .append(target.unread())
                    .append("\n");
        }
    }

    private void appendFooter(StringBuilder preview) {
        preview.append("\n");
        preview.append(LINE).append("\n");
        preview.append("Demo abgeschlossen.\n");
        preview.append(LINE).append("\n");
        preview.append("\n");
    }

    private String safeText(String value) {
        if (value == null || value.isBlank()) {
            return "(leer)";
        }

        return value;
    }

    private String emptyIfBlank(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value;
    }
}