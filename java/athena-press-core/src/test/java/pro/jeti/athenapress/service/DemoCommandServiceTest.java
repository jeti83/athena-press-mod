package pro.jeti.athenapress.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

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
    void parseArticlesArgumentCreatesArticlesCommand() {
        DemoCommand command = demoCommandService.parse(new String[]{"--articles"});

        assertEquals(DemoCommandType.LIST_ARTICLES, command.type());
        assertNull(command.issueId());
    }

    @Test
    void parseGermanArticlesArgumentCreatesArticlesCommand() {
        DemoCommand command = demoCommandService.parse(new String[]{"--artikel"});

        assertEquals(DemoCommandType.LIST_ARTICLES, command.type());
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
}
