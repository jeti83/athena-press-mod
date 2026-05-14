package pro.jeti.athenapress.integration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class PlayerNewspaperVisualViewFactoryTest {

    private final PlayerNewspaperVisualViewFactory viewFactory =
            new PlayerNewspaperVisualViewFactory();

    @Test
    void createsMessageViewForMissingResponse() {
        PlayerNewspaperVisualView view = viewFactory.fromResponse(null);

        assertFalse(view.newspaperOpen());
        assertTrue(view.hasMessage());
        assertEquals(1, view.buttons().size());
        assertEquals(NewspaperUiButtonStyle.DANGER, view.buttons().getFirst().style());
    }

    @Test
    void createsMessageViewForMissingSpread() {
        PlayerNewspaperVisualResponse response = PlayerNewspaperVisualResponse.missing(
                "player-1",
                "Diese Ausgabe ist nicht verfügbar."
        );

        PlayerNewspaperVisualView view = viewFactory.fromResponse(response);

        assertEquals("player-1", view.playerId());
        assertFalse(view.newspaperOpen());
        assertTrue(view.message().contains("Diese Ausgabe ist nicht verfügbar."));
    }

    @Test
    void createsVisualViewForSingleSpread() {
        PlayerNewspaperVisualResponse response = responseWithSpread(0, 1);

        PlayerNewspaperVisualView view = viewFactory.fromResponse(response);

        assertTrue(view.newspaperOpen());
        assertTrue(view.hasLeftPage());
        assertFalse(view.hasRightPage());
        assertFalse(view.hasPreviousSpread());
        assertFalse(view.hasNextSpread());
        assertEquals(2, view.buttons().size());
        assertTrue(view.buttons().stream()
                .anyMatch(button -> button.command().action() == PlayerNewspaperAction.SHOW_OVERVIEW));
    }

    @Test
    void createsNavigationButtonsForMiddleSpread() {
        PlayerNewspaperVisualResponse response = responseWithSpread(1, 3);

        PlayerNewspaperVisualView view = viewFactory.fromResponse(response);

        assertTrue(view.hasPreviousSpread());
        assertTrue(view.hasNextSpread());
        assertTrue(view.buttons().stream()
                .anyMatch(button -> "visual_previous_spread".equals(button.command().uiCommand())));
        assertTrue(view.buttons().stream()
                .anyMatch(button -> "visual_next_spread".equals(button.command().uiCommand())));
        assertEquals(4, view.buttons().size());
    }

    private PlayerNewspaperVisualResponse responseWithSpread(
            int spreadIndex,
            int totalSpreadCount
    ) {
        NewspaperPreviewSpread spread = new NewspaperPreviewSpread(
                spreadIndex,
                new NewspaperPreviewPage(
                        1,
                        "Titelseite",
                        NewspaperPageRole.FRONT_COVER,
                        NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                        List.of(new NewspaperPreviewBlock(
                                NewspaperVisualBlockType.HEADLINE,
                                "AthenaPress",
                                null,
                                0,
                                0,
                                2,
                                2
                        ))
                ),
                null,
                List.of()
        );

        return new PlayerNewspaperVisualResponse(
                "player-1",
                "issue_visual",
                "Athena Visualblatt",
                spreadIndex,
                totalSpreadCount,
                spread,
                true,
                ""
        );
    }
}
