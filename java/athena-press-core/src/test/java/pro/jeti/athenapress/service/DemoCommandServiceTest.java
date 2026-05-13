package pro.jeti.athenapress.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import pro.jeti.athenapress.model.Article;
import pro.jeti.athenapress.model.Category;
import pro.jeti.athenapress.model.Issue;
import pro.jeti.athenapress.model.Subscriber;
import pro.jeti.athenapress.service.DemoCommandService.DemoCommand;
import pro.jeti.athenapress.service.DemoCommandService.DemoCommandType;

class DemoCommandServiceTest {

    private final DemoCommandService demoCommandService = new DemoCommandService();

    @Test
    void parseWithoutArgumentsUsesDefaultIssue() {
        DemoCommand command = demoCommandService.parse(new String[0]);

        assertEquals(DemoCommandType.PREVIEW_ISSUE, command.type());
        assertEquals(DemoCommandService.DEFAULT_ISSUE_ID, command.issueId());
    }

    @Test
    void parseNullArgumentsUsesDefaultIssue() {
        DemoCommand command = demoCommandService.parse(null);

        assertEquals(DemoCommandType.PREVIEW_ISSUE, command.type());
        assertEquals(DemoCommandService.DEFAULT_ISSUE_ID, command.issueId());
    }

    @Test
    void parseIssueIdCreatesPreviewCommand() {
        DemoCommand command = demoCommandService.parse(new String[]{"issue_1234"});

        assertEquals(DemoCommandType.PREVIEW_ISSUE, command.type());
        assertEquals("issue_1234", command.issueId());
    }

    @Test
    void parseHelpArgumentsCreatesHelpCommand() {
        for (String argument : List.of("--help", "--hilfe", "-h", "/?")) {
            DemoCommand command = demoCommandService.parse(new String[]{argument});

            assertEquals(DemoCommandType.SHOW_HELP, command.type());
            assertNull(command.issueId());
        }
    }

    @Test
    void parseListArgumentCreatesListCommand() {
        DemoCommand command = demoCommandService.parse(new String[]{"--list"});

        assertEquals(DemoCommandType.LIST_PUBLISHED_ISSUES, command.type());
        assertNull(command.issueId());
    }

    @Test
    void parseGermanListArgumentCreatesListCommand() {
        DemoCommand command = demoCommandService.parse(new String[]{"--liste"});

        assertEquals(DemoCommandType.LIST_PUBLISHED_ISSUES, command.type());
        assertNull(command.issueId());
    }

    @Test
    void parseValidateArgumentCreatesValidateCommand() {
        DemoCommand command = demoCommandService.parse(new String[]{"--validate", "issue_0002"});

        assertEquals(DemoCommandType.VALIDATE_ISSUE, command.type());
        assertEquals("issue_0002", command.issueId());
    }

    @Test
    void parseValidateArgumentWithoutIssueUsesDefaultIssue() {
        DemoCommand command = demoCommandService.parse(new String[]{"--validate"});

        assertEquals(DemoCommandType.VALIDATE_ISSUE, command.type());
        assertEquals(DemoCommandService.DEFAULT_ISSUE_ID, command.issueId());
    }

    @Test
    void parsePruefenArgumentCreatesValidateCommand() {
        DemoCommand command = demoCommandService.parse(new String[]{"--pruefen", "issue_0002"});

        assertEquals(DemoCommandType.VALIDATE_ISSUE, command.type());
        assertEquals("issue_0002", command.issueId());
    }

    @Test
    void parseStatusArgumentCreatesStatusCommand() {
        DemoCommand command = demoCommandService.parse(new String[]{"--status"});

        assertEquals(DemoCommandType.SHOW_STATUS, command.type());
        assertNull(command.issueId());
    }

    @Test
    void parseUebersichtArgumentCreatesStatusCommand() {
        DemoCommand command = demoCommandService.parse(new String[]{"--uebersicht"});

        assertEquals(DemoCommandType.SHOW_STATUS, command.type());
        assertNull(command.issueId());
    }

    @Test
    void createHelpTextContainsUsageExamples() {
        String helpText = demoCommandService.createHelpText();

        assertTrue(helpText.contains("AthenaPress Demo"));
        assertTrue(helpText.contains("Verwendung:"));
        assertTrue(helpText.contains("AthenaPressDemo <issueId>"));
        assertTrue(helpText.contains("AthenaPressDemo --list | --liste"));
        assertTrue(helpText.contains("AthenaPressDemo --validate | --pruefen <issueId>"));
        assertTrue(helpText.contains("AthenaPressDemo --status | --uebersicht"));
        assertTrue(helpText.contains("AthenaPressDemo --help | --hilfe | -h | /?"));
        assertTrue(helpText.contains("Ohne issueId wird " + DemoCommandService.DEFAULT_ISSUE_ID + " verwendet."));
    }

    @Test
    void createPublishedIssuesTextListsIssues() {
        Issue issue = new Issue(
                "issue_test",
                "published",
                7,
                "Testausgabe",
                "Untertitel",
                List.of("article_test"),
                null,
                null,
                null,
                null,
                null,
                null
        );

        String text = demoCommandService.createPublishedIssuesText(List.of(issue));

        assertTrue(text.contains("Veröffentlichte Ausgaben:"));
        assertTrue(text.contains("- issue_test #7 | Testausgabe"));
    }

    @Test
    void createPublishedIssuesTextHandlesEmptyList() {
        String text = demoCommandService.createPublishedIssuesText(List.of());

        assertTrue(text.contains("Veröffentlichte Ausgaben:"));
        assertTrue(text.contains("- Keine veröffentlichten Ausgaben gefunden"));
    }

    @Test
    void createPublishedIssuesTextHandlesNullList() {
        String text = demoCommandService.createPublishedIssuesText(null);

        assertTrue(text.contains("Veröffentlichte Ausgaben:"));
        assertTrue(text.contains("- Keine veröffentlichten Ausgaben gefunden"));
    }

    @Test
    void createValidationTextShowsSuccessMessage() {
        String text = demoCommandService.createValidationText(
                "issue_0002",
                ValidationResult.valid()
        );

        assertTrue(text.contains("Validierung für issue_0002"));
        assertTrue(text.contains("OK - Keine Fehler gefunden."));
    }

    @Test
    void createValidationTextShowsErrorCount() {
        String text = demoCommandService.createValidationText(
                "issue_0002",
                ValidationResult.invalid(List.of("Testfehler"))
        );

        assertTrue(text.contains("FEHLER - 1 Problem gefunden."));
        assertTrue(text.contains("- Testfehler"));
    }

    @Test
    void createStatusTextShowsCountsAndValidationResult() {
        Article article = new Article(
                "article_test",
                "published",
                "server_news",
                "Testartikel",
                null,
                null,
                null,
                null,
                "Testinhalt",
                null,
                null,
                List.of(),
                null,
                null,
                null,
                null,
                null
        );

        Issue issue = new Issue(
                "issue_test",
                "published",
                7,
                "Testausgabe",
                "Untertitel",
                List.of("article_test"),
                null,
                null,
                null,
                null,
                null,
                null
        );

        Subscriber activeSubscriber = new Subscriber(
                "HF_jeti83",
                null,
                true,
                "mailbox",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                null
        );

        Subscriber inactiveSubscriber = new Subscriber(
                "TestUser",
                null,
                false,
                "notification_only",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                null
        );

        Category enabledCategory = new Category(
                "server_news",
                "Server News",
                "Neuigkeiten vom Server",
                null,
                true
        );

        Category disabledCategory = new Category(
                "old_news",
                "Alte News",
                "Archivierte Kategorie",
                null,
                false
        );

        String text = demoCommandService.createStatusText(
                List.of(article),
                List.of(issue),
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
        String text = demoCommandService.createStatusText(
                null,
                null,
                null,
                null,
                ValidationResult.valid()
        );

        assertTrue(text.contains("Artikel: 0"));
        assertTrue(text.contains("Ausgaben: 0"));
        assertTrue(text.contains("Abonnenten: 0 (aktiv: 0)"));
        assertTrue(text.contains("Kategorien: 0 (aktiv: 0)"));
        assertTrue(text.contains("Validierung: OK - Keine Fehler gefunden."));
    }
}