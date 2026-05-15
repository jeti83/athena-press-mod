package pro.jeti.athenapress.integration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class HytaleNewspaperVisualUiPortTest {

    @Test
    void rejectsMissingBridge() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new HytaleNewspaperVisualUiPort(null)
        );
    }

    @Test
    void registersAndUnregistersPlayers() {
        CapturingVisualBridge bridge = new CapturingVisualBridge();
        HytaleNewspaperVisualUiPort port = new HytaleNewspaperVisualUiPort(bridge);

        port.registerPlayer(new HytalePlayerContext("player-1", "Jeti"));
        port.unregisterPlayer("player-1");

        assertEquals(0, port.registeredPlayerCount());
    }

    @Test
    void opensOrUpdatesVisualViewForRegisteredPlayer() {
        CapturingVisualBridge bridge = new CapturingVisualBridge();
        HytaleNewspaperVisualUiPort port = new HytaleNewspaperVisualUiPort(bridge);
        HytalePlayerContext player = new HytalePlayerContext("player-1", "Jeti");

        port.registerPlayer(player);
        port.show(visualView("player-1"));

        assertEquals(player, bridge.lastPlayer);
        assertEquals("issue_visual", bridge.lastView.issueId());
    }

    @Test
    void ignoresVisualViewForUnknownPlayer() {
        CapturingVisualBridge bridge = new CapturingVisualBridge();
        HytaleNewspaperVisualUiPort port = new HytaleNewspaperVisualUiPort(bridge);

        port.show(visualView("player-1"));

        assertNull(bridge.lastView);
    }

    @Test
    void closesRegisteredPlayerView() {
        CapturingVisualBridge bridge = new CapturingVisualBridge();
        HytaleNewspaperVisualUiPort port = new HytaleNewspaperVisualUiPort(bridge);
        HytalePlayerContext player = new HytalePlayerContext("player-1", "Jeti");

        port.registerPlayer(player);
        port.close("player-1");

        assertEquals(player, bridge.closedPlayer);
    }

    private PlayerNewspaperVisualView visualView(String playerId) {
        return new PlayerNewspaperVisualView(
                playerId,
                "issue_visual",
                "Athena Sichtblatt",
                0,
                1,
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
                true,
                false,
                false,
                "",
                List.of(),
                List.of()
        );
    }

    private static class CapturingVisualBridge implements HytaleNewspaperVisualUiBridge {
        private HytalePlayerContext lastPlayer;
        private PlayerNewspaperVisualView lastView;
        private HytalePlayerContext closedPlayer;

        @Override
        public void openOrUpdate(
                HytalePlayerContext player,
                PlayerNewspaperVisualView view
        ) {
            this.lastPlayer = player;
            this.lastView = view;
        }

        @Override
        public void close(HytalePlayerContext player) {
            this.closedPlayer = player;
        }
    }
}
