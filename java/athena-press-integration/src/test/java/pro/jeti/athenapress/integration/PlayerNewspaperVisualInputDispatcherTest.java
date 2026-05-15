package pro.jeti.athenapress.integration;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PlayerNewspaperVisualInputDispatcherTest {

    @TempDir
    Path tempDir;

    @Test
    void rejectsMissingController() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PlayerNewspaperVisualInputDispatcher(null)
        );
    }

    @Test
    void opensVisualIssueFromInputEvent() {
        CapturingVisualUiPort uiPort = new CapturingVisualUiPort();
        PlayerNewspaperVisualInputDispatcher dispatcher = createDispatcher(uiPort);

        dispatcher.dispatch(
                PlayerNewspaperInputEvent.npcInteraction("player-1", "issue_visual")
        );

        assertNotNull(uiPort.lastView);
        assertEquals("issue_visual", uiPort.lastView.issueId());
        assertEquals(0, uiPort.lastView.spreadIndex());
    }

    @Test
    void showsNextSpreadFromInputEvent() {
        CapturingVisualUiPort uiPort = new CapturingVisualUiPort();
        PlayerNewspaperVisualInputDispatcher dispatcher = createDispatcher(uiPort);

        dispatcher.dispatch(
                PlayerNewspaperInputEvent.uiButton(
                        "player-1",
                        NewspaperVisualUiCommands.NEXT_SPREAD,
                        null
                )
        );

        assertNotNull(uiPort.lastView);
        assertEquals(1, uiPort.lastView.spreadIndex());
    }

    @Test
    void unknownInputFallsBackToCurrentSpread() {
        CapturingVisualUiPort uiPort = new CapturingVisualUiPort();
        PlayerNewspaperVisualInputDispatcher dispatcher = createDispatcher(uiPort);

        dispatcher.dispatch(
                PlayerNewspaperInputEvent.uiButton("player-1", "unknown", null)
        );

        assertNotNull(uiPort.lastView);
        assertEquals(0, uiPort.lastView.spreadIndex());
    }

    @Test
    void closesVisualIssueFromInputEvent() {
        CapturingVisualUiPort uiPort = new CapturingVisualUiPort();
        PlayerNewspaperVisualInputDispatcher dispatcher = createDispatcher(uiPort);

        dispatcher.dispatch(
                PlayerNewspaperInputEvent.uiButton("player-1", "close", null)
        );

        assertEquals("player-1", uiPort.closedPlayerId);
    }

    private PlayerNewspaperVisualInputDispatcher createDispatcher(
            CapturingVisualUiPort uiPort
    ) {
        return new PlayerNewspaperVisualInputDispatcher(
                new PlayerNewspaperVisualUiController(
                        new StubVisualPlugin(),
                        uiPort
                )
        );
    }

    private static class CapturingVisualUiPort implements PlayerNewspaperVisualUiPort {
        private PlayerNewspaperVisualView lastView;
        private String closedPlayerId;

        @Override
        public void show(PlayerNewspaperVisualView view) {
            this.lastView = view;
        }

        @Override
        public void close(String playerId) {
            this.closedPlayerId = playerId;
        }
    }

    private class StubVisualPlugin extends AthenaPressIntegrationPlugin {

        StubVisualPlugin() {
            super(tempDir);
        }

        @Override
        public PlayerNewspaperVisualResponse onPlayerOpenVisualNewspaper(
                String playerId,
                String issueId
        ) {
            return response(playerId, issueId, 0);
        }

        @Override
        public PlayerNewspaperVisualResponse onPlayerRequestCurrentVisualSpread(
                String playerId
        ) {
            return response(playerId, "issue_visual", 0);
        }

        @Override
        public PlayerNewspaperVisualResponse onPlayerRequestNextVisualSpread(
                String playerId
        ) {
            return response(playerId, "issue_visual", 1);
        }

        private PlayerNewspaperVisualResponse response(
                String playerId,
                String issueId,
                int spreadIndex
        ) {
            return new PlayerNewspaperVisualResponse(
                    playerId,
                    issueId,
                    "Athena Sichtblatt",
                    spreadIndex,
                    2,
                    new NewspaperPreviewSpread(
                            spreadIndex,
                            new NewspaperPreviewPage(
                                    spreadIndex + 1,
                                    "Seite " + (spreadIndex + 1),
                                    NewspaperPageRole.FRONT_COVER,
                                    NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                                    List.of(new NewspaperPreviewBlock(
                                            NewspaperVisualBlockType.HEADLINE,
                                            "Testseite",
                                            null,
                                            0,
                                            0,
                                            2,
                                            2
                                    ))
                            ),
                            null,
                            List.of()
                    ),
                    List.of(),
                    true,
                    ""
            );
        }
    }
}
