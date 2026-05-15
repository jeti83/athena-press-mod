package pro.jeti.athenapress.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class NewspaperVisualUiCommandsTest {

    @Test
    void createsCurrentSpreadCommand() {
        PlayerNewspaperUiCommand command = NewspaperVisualUiCommands.currentSpread();

        assertEquals(NewspaperVisualUiCommands.CURRENT_SPREAD, command.uiCommand());
    }

    @Test
    void createsNextSpreadCommand() {
        PlayerNewspaperUiCommand command = NewspaperVisualUiCommands.nextSpread("issue-1");

        assertEquals(NewspaperVisualUiCommands.NEXT_SPREAD, command.uiCommand());
        assertEquals("issue-1", command.value());
    }

    @Test
    void createsPreviousSpreadCommand() {
        PlayerNewspaperUiCommand command = NewspaperVisualUiCommands.previousSpread();

        assertEquals(NewspaperVisualUiCommands.PREVIOUS_SPREAD, command.uiCommand());
    }

    @Test
    void createsSelectSpreadCommand() {
        PlayerNewspaperUiCommand command = NewspaperVisualUiCommands.selectSpread(2);

        assertEquals(NewspaperVisualUiCommands.SELECT_SPREAD, command.uiCommand());
        assertEquals("2", command.value());
    }
}
