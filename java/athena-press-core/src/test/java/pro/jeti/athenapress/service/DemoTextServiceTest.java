package pro.jeti.athenapress.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import pro.jeti.athenapress.model.Article;
import pro.jeti.athenapress.model.Category;
import pro.jeti.athenapress.model.Issue;
import pro.jeti.athenapress.model.Subscriber;

class DemoTextServiceTest {

    private final DemoTextService demoTextService = new DemoTextService();

    @Test
    void createHelpTextContainsUsageExamples() {
        String helpText = demoTextService.createHelpText();

        assertTrue(helpText.contains("AthenaPress Demo"));
        assertTrue(helpText.contains("Verwendung:"));
        assertTrue(helpText.contains("AthenaPressDemo <issueId>"));
        assertTrue(helpText.contains("AthenaPressDemo --list | --liste"));
        assertTrue(helpText.contains("AthenaPressDemo --articles | --artikel"));
        assertTrue(helpText.contains("AthenaPressDemo --validate | --pruefen <issueId>"));
        assertTrue(helpText.contains("AthenaPressDemo --status | --uebersicht"));
        assertTrue(helpText.contains("AthenaPressDemo --help | --hilfe | -h | /?"));
        assertTrue(helpText.contains("Ohne issueId wird " + DemoCommandService.DEFAULT_ISSUE_ID + " verwendet."));
    }

    @Test
    void createPublishedIssuesTextListsIssues() {
        Issue issue = new Issue(
                "issue_test", "published", 7, "Testausgabe", "Untertitel",
                List.of("article_test"), null, null, null, null, null, null
        );

        String text = demoTextService.createPublishedIssuesText(List.of(issue));

        assertTrue(text.contains("Veröffentlichte Ausgaben:"));
        assertTrue(text.contains("- issue_test #7 | Testausgabe"));
    }

    @Test
    void createPublishedIssuesTextHandlesEmptyList() {
        String text = demoTextService.createPublishedIssuesText(List.of());

        assertTrue(text.contains("Veröffentlichte Ausgaben:"));
        assertTrue(text.contains("- Keine veröffentlichten Ausgaben gefunden"));
    }

    @Test
    void createPublishedIssuesTextHandlesNullList() {
        String text = demoTextService.createPublishedIssuesText(null);

        assertTrue(text.contains("Veröffentlichte Ausgaben:"));
        assertTrue(text.contains("- Keine veröffentlichten Ausgaben gefunden"));
    }

    @Test
    void createArticleListTextListsArticles() {
        Article article = new Article(
                "article_test", "published", "server_news", "Testartikel",
                null, null, "Kurze Testzusammenfassung", null, "Testinhalt",
                null, null, List.of(), null, null, null, null, null
        );

        String text = demoTextService.createArticleListText(List.of(article));

        assertTrue(text.contains("Artikelliste"));
        assertTrue(text.contains("Veröffentlichte Artikel:"));
        assertTrue(text.contains("- article_test | server_news | Testartikel | published | Bild: nein"));
        assertTrue(text.contains("Kurze Testzusammenfassung"));
    }

    @Test
    void createArticleListTextHandlesEmptyList() {
        String text = demoTextService.createArticleListText(List.of());

        assertTrue(text.contains("Artikelliste"));
        assertTrue(text.contains("- Keine Artikel gefunden"));
    }

    @Test
    void createArticleListTextHandlesNullList() {
        String text = demoTextService.createArticleListText(null);

        assertTrue(text.contains("Artikelliste"));
        assertTrue(text.contains("- Keine Artikel gefunden"));
    }

    @Test
    void createValidationTextShowsSuccessMessage() {
        String text = demoTextService.createValidationText("issue_0002", ValidationResult.valid());

        assertTrue(text.contains("Validierung für issue_0002"));
        assertTrue(text.contains("OK - Keine Fehler gefunden."));
    }

    @Test
    void createValidationTextShowsErrorCount() {
        String text = demoTextService.createValidationText(
                "issue_0002", ValidationResult.invalid(List.of("Testfehler"))
        );

        assertTrue(text.contains("FEHLER - 1 Problem gefunden."));
        assertTrue(text.contains("- Testfehler"));
    }

    @Test
    void createStatusTextShowsCountsAndValidationResult() {
        Article article = new Article(
                "article_test", "published", "server_news", "Testartikel",
                null, null, null, null, "Testinhalt",
                null, null, List.of(), null, null, null, null, null
        );

        Issue issue = new Issue(
                "issue_test", "published", 7, "Testausgabe", "Untertitel",
                List.of("article_test"), null, null, null, null, null, null
        );

        Subscriber activeSubscriber = new Subscriber(
                "HF_jeti83", null, true, "mailbox",
                null, null, null, null, null, null, null, null, List.of(), null
        );

        Subscriber inactiveSubscriber = new Subscriber(
                "TestUser", null, false, "notification_only",
                null, null, null, null, null, null, null, null, List.of(), null
        );

        Category enabledCategory = new Category("server_news", "Server News", "Neuigkeiten", null, true);
        Category disabledCategory = new Category("old_news", "Alte News", "Archiviert", null, false);

        String text = demoTextService.createStatusText(
                List.of(article), List.of(issue),
                List.of(activeSubscriber, inactiveSubscriber),
                List.of(enabledCategory, disabledCategory),
                ValidationResult.valid()
        );

        assertTrue(text.contains("AthenaPress Status"));
        assertTrue(text.contains("Artikel: 1"));
        assertTrue(text.contains("Ausgaben: 1"));
        assertTrue(text.contains("Abonnenten: 2 (aktiv: 1)"));
        assertTrue(text.contains("Kategorien: 2 (aktiv: 1)"));
        assertTrue(text.contains("Validierung: OK - Keine Fehler gefunden."));
    }

    @Test
    void createStatusTextHandlesNullLists() {
        String text = demoTextService.createStatusText(
                null, null, null, null, ValidationResult.valid()
        );

        assertTrue(text.contains("Artikel: 0"));
        assertTrue(text.contains("Ausgaben: 0"));
        assertTrue(text.contains("Abonnenten: 0 (aktiv: 0)"));
        assertTrue(text.contains("Kategorien: 0 (aktiv: 0)"));
        assertTrue(text.contains("Validierung: OK - Keine Fehler gefunden."));
    }
}
