package pro.jeti.athenapress.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import pro.jeti.athenapress.model.Issue;
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
        for (String argument : List.of("--help", "-h", "/?")) {
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
    void createHelpTextContainsUsageExamples() {
        String helpText = demoCommandService.createHelpText();

        assertTrue(helpText.contains("AthenaPress Demo"));
        assertTrue(helpText.contains("Verwendung:"));
        assertTrue(helpText.contains("AthenaPressDemo <issueId>"));
        assertTrue(helpText.contains("AthenaPressDemo --list|--liste"));
        assertTrue(helpText.contains("AthenaPressDemo --validate|--pruefen <issueId>"));
        assertTrue(helpText.contains("AthenaPressDemo --help|--hilfe"));
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
    void parseGermanListArgumentCreatesListCommand() {
        DemoCommand command = demoCommandService.parse(new String[]{"--liste"});

        assertEquals(DemoCommandType.LIST_PUBLISHED_ISSUES, command.type());
        assertNull(command.issueId());
    }
}