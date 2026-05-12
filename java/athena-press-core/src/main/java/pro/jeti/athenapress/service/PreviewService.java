package pro.jeti.athenapress.service;

import java.util.List;

import pro.jeti.athenapress.model.Article;
import pro.jeti.athenapress.model.DeliveryTarget;
import pro.jeti.athenapress.model.Issue;
import pro.jeti.athenapress.model.ResolvedIssue;

public class PreviewService {

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

        preview.append(safeText(issue.title())).append("\n");

        String subtitle = emptyIfBlank(issue.subtitle());
        if (!subtitle.isBlank()) {
            preview.append(subtitle).append("\n");
        }

        preview.append("\n");
        preview.append("Status: ").append(safeText(issue.status())).append("\n");

        appendValidation(preview, validationResult);

        preview.append("\n");
        preview.append("Artikel:\n");
        appendArticles(preview, resolvedIssue.articles());

        preview.append("\n");
        preview.append("Zustellplan:\n");
        appendDeliveryTargets(preview, deliveryTargets);

        appendFooter(preview);

        return preview.toString();
    }

    private void appendHeader(StringBuilder preview) {
        preview.append("\n");
        preview.append("========================================\n");
        preview.append("        ATHENA BOTENBLATT\n");
        preview.append("========================================\n");
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
        preview.append("========================================\n");
        preview.append("Demo abgeschlossen.\n");
        preview.append("========================================\n");
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