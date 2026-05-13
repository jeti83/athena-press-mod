package pro.jeti.athenapress.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import pro.jeti.athenapress.model.Article;
import pro.jeti.athenapress.model.DeliveryTarget;
import pro.jeti.athenapress.model.Issue;
import pro.jeti.athenapress.model.ResolvedIssue;

class PreviewServiceTest {

    private final PreviewService previewService = new PreviewService();

    @Test
    void createTextPreviewContainsHeaderIssueArticlesValidationAndDeliveryPlan() {
        Issue issue = new Issue(
                "issue_test",
                "published",
                1,
                "Athena Botenblatt Testausgabe",
                "Die erste sichtbare Java-Core-Ausgabe",
                List.of("article_test"),
                null,
                null,
                null,
                null,
                null,
                null
        );

                Article article = new Article(
                "article_test",
                "published",
                "server_news",
                "Spawnplatz feierlich eingeweiht",
                null,
                null,
                "Kurze Meldung vom Spawnplatz.",
                null,
                "Heute wurde am Spawnplatz viel Konfetti getestet.",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        ResolvedIssue resolvedIssue = new ResolvedIssue(
                issue,
                List.of(article)
        );

        DeliveryTarget deliveryTarget = new DeliveryTarget(
                "issue_test",
                "Jeti",
                "unknown",
                "item_and_notification",
                true
        );

        String preview = previewService.createTextPreview(
                resolvedIssue,
                ValidationResult.valid(),
                List.of(deliveryTarget)
        );

        assertTrue(preview.contains("ATHENA BOTENBLATT"));
        assertTrue(preview.contains("Athena Botenblatt Testausgabe"));
        assertTrue(preview.contains("Die erste sichtbare Java-Core-Ausgabe"));
        assertTrue(preview.contains("Status: published"));
        assertTrue(preview.contains("Validierung: OK - Keine Fehler gefunden."));
        assertTrue(preview.contains("- [server_news] Spawnplatz feierlich eingeweiht"));
        assertTrue(preview.contains("  Kurze Meldung vom Spawnplatz."));
        assertTrue(preview.contains("- Jeti -> item_and_notification -> unread true"));
        assertTrue(preview.contains("Demo abgeschlossen."));
    }

    @Test
    void createTextPreviewContainsValidationErrors() {
        Issue issue = new Issue(
                "issue_broken",
                "draft",
                2,
                "Kaputte Testausgabe",
                null,
                List.of("missing_article"),
                null,
                null,
                null,
                null,
                null,
                null
        );

        ResolvedIssue resolvedIssue = new ResolvedIssue(
                issue,
                List.of()
        );

        ValidationResult validationResult = ValidationResult.invalid(List.of(
                "Issue issue_broken references missing article: missing_article"
        ));

        String preview = previewService.createTextPreview(
                resolvedIssue,
                validationResult,
                List.of()
        );

        assertTrue(preview.contains("Kaputte Testausgabe"));
        assertTrue(preview.contains("Validierung: FEHLER - 1 Problem gefunden."));
        assertTrue(preview.contains("- Issue issue_broken references missing article: missing_article"));
        assertTrue(preview.contains("- Keine Artikel eingetragen"));
        assertTrue(preview.contains("- Keine Empfänger"));
    }

    @Test
    void createTextPreviewHandlesMissingIssue() {
        String preview = previewService.createTextPreview(
                null,
                ValidationResult.invalid(List.of("Issue not found: issue_missing")),
                List.of()
        );

        assertTrue(preview.contains("ATHENA BOTENBLATT"));
        assertTrue(preview.contains("Ausgabe nicht gefunden."));
        assertTrue(preview.contains("Demo abgeschlossen."));
    }
}