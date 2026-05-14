package pro.jeti.athenapress.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class PlayerNewspaperVisualInputMapperTest {

    private final PlayerNewspaperVisualInputMapper mapper =
            new PlayerNewspaperVisualInputMapper();

    @Test
    void mapsNpcInteractionToOpenIssue() {
        PlayerNewspaperUiCommand command = mapper.toUiCommand(
                PlayerNewspaperInputEvent.npcInteraction("player-1", "issue-1")
        );

        assertEquals(PlayerNewspaperAction.OPEN_ISSUE, command.action());
        assertEquals("issue-1", command.value());
    }

    @Test
    void mapsChatCommandShortcutToOpenIssue() {
        PlayerNewspaperUiCommand command = mapper.toUiCommand(
                PlayerNewspaperInputEvent.chatCommand("player-1", "/ap", "issue-1")
        );

        assertEquals(PlayerNewspaperAction.OPEN_ISSUE, command.action());
        assertEquals("issue-1", command.value());
    }

    @Test
    void mapsNextSpreadAliasesToVisualCommand() {
        PlayerNewspaperUiCommand command = mapper.toUiCommand(
                PlayerNewspaperInputEvent.uiButton("player-1", "weiter", null)
        );

        assertEquals(NewspaperVisualUiCommands.NEXT_SPREAD, command.uiCommand());
    }

    @Test
    void mapsPreviousSpreadAliasesToVisualCommand() {
        PlayerNewspaperUiCommand command = mapper.toUiCommand(
                PlayerNewspaperInputEvent.keyBind("player-1", "zurück", null)
        );

        assertEquals(NewspaperVisualUiCommands.PREVIOUS_SPREAD, command.uiCommand());
    }

    @Test
    void mapsCloseCommand() {
        PlayerNewspaperUiCommand command = mapper.toUiCommand(
                PlayerNewspaperInputEvent.uiButton("player-1", "schließen", null)
        );

        assertEquals(PlayerNewspaperAction.CLOSE_ISSUE, command.action());
    }

    @Test
    void ignoresUnknownCommand() {
        assertNull(mapper.toUiCommand(
                PlayerNewspaperInputEvent.uiButton("player-1", "unknown", null)
        ));
    }
}
