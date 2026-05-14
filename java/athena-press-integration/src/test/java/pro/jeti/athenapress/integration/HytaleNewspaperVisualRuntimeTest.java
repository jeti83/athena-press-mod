package pro.jeti.athenapress.integration;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HytaleNewspaperVisualRuntimeTest {

    @TempDir
    Path tempDir;

    @Test
    void rejectsMissingPlugin() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new HytaleNewspaperVisualRuntime<>(
                        null,
                        new NoopTextUiPort(),
                        new CapturingVisualBridge(),
                        new StubResolver()
                )
        );
    }

    @Test
    void rejectsMissingTextUiPort() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new HytaleNewspaperVisualRuntime<>(
                        new AthenaPressIntegrationPlugin(tempDir),
                        null,
                        new CapturingVisualBridge(),
                        new StubResolver()
                )
        );
    }

    @Test
    void rejectsMissingVisualBridge() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new HytaleNewspaperVisualRuntime<>(
                        new AthenaPressIntegrationPlugin(tempDir),
                        new NoopTextUiPort(),
                        null,
                        new StubResolver()
                )
        );
    }

    @Test
    void rejectsMissingPlayerResolver() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new HytaleNewspaperVisualRuntime<String>(
                        new AthenaPressIntegrationPlugin(tempDir),
                        new NoopTextUiPort(),
                        new CapturingVisualBridge(),
                        null
                )
        );
    }

    @Test
    void createsVisualRuntimeComponents() {
        AthenaPressIntegrationPlugin plugin = new AthenaPressIntegrationPlugin(tempDir);
        HytaleNewspaperVisualRuntime<String> runtime =
                new HytaleNewspaperVisualRuntime<>(
                        plugin,
                        new NoopTextUiPort(),
                        new CapturingVisualBridge(),
                        new StubResolver()
                );

        assertEquals(plugin, runtime.plugin());
        assertNotNull(runtime.visualUiPort());
        assertNotNull(runtime.visualUiController());
        assertNotNull(runtime.visualInputDispatcher());
        assertNotNull(runtime.visualInputAdapter());
        assertNotNull(runtime.lifecycleHandler());
        assertNotNull(runtime.lifecycleAdapter());
    }

    @Test
    void lifecycleAdapterRegistersPlayerInVisualPort() {
        HytaleNewspaperVisualRuntime<String> runtime =
                new HytaleNewspaperVisualRuntime<>(
                        new AthenaPressIntegrationPlugin(tempDir),
                        new NoopTextUiPort(),
                        new CapturingVisualBridge(),
                        new StubResolver()
                );

        runtime.lifecycleAdapter().onPlayerConnected("player-1");

        assertTrue(runtime.visualUiPort().hasRegisteredPlayer("player-1"));
    }

    @Test
    void runtimeConnectsPlayerThroughConvenienceMethod() {
        HytaleNewspaperVisualRuntime<String> runtime =
                new HytaleNewspaperVisualRuntime<>(
                        new AthenaPressIntegrationPlugin(tempDir),
                        new NoopTextUiPort(),
                        new CapturingVisualBridge(),
                        new StubResolver()
                );

        runtime.onPlayerConnected("player-1");

        assertTrue(runtime.visualUiPort().hasRegisteredPlayer("player-1"));
    }

    @Test
    void runtimeForwardsUiButtonThroughConvenienceMethod() {
        CapturingVisualBridge bridge = new CapturingVisualBridge();
        HytaleNewspaperVisualRuntime<String> runtime =
                new HytaleNewspaperVisualRuntime<>(
                        new StubPlugin(),
                        new NoopTextUiPort(),
                        bridge,
                        new StubResolver()
                );
        HytalePlayerContext player = new HytalePlayerContext("player-1", "Jeti");

        runtime.visualUiPort().registerPlayer(player);
        runtime.onUiButton(player, NewspaperVisualUiCommands.NEXT_SPREAD, null);

        assertEquals(1, bridge.lastView.spreadIndex());
    }

    private static class StubResolver implements HytalePlayerContextResolver<String> {
        @Override
        public HytalePlayerContext resolve(String player) {
            return new HytalePlayerContext(player, "Spieler " + player);
        }
    }

    private static class NoopTextUiPort implements PlayerNewspaperUiPort {
        @Override
        public void show(PlayerNewspaperResponse response) {
        }

        @Override
        public void show(NewspaperUiView view) {
        }

        @Override
        public void close(String playerId) {
        }
    }

    private static class CapturingVisualBridge implements HytaleNewspaperVisualUiBridge {
        private PlayerNewspaperVisualView lastView;

        @Override
        public void openOrUpdate(
                HytalePlayerContext player,
                PlayerNewspaperVisualView view
        ) {
            this.lastView = view;
        }

        @Override
        public void close(HytalePlayerContext player) {
        }
    }

    private class StubPlugin extends AthenaPressIntegrationPlugin {

        StubPlugin() {
            super(tempDir);
        }

        @Override
        public PlayerNewspaperVisualResponse onPlayerRequestNextVisualSpread(
                String playerId
        ) {
            return new PlayerNewspaperVisualResponse(
                    playerId,
                    "issue_visual",
                    "Athena Sichtblatt",
                    1,
                    2,
                    new NewspaperPreviewSpread(
                            1,
                            new NewspaperPreviewPage(
                                    2,
                                    "Seite 2",
                                    NewspaperPageRole.RIGHT_INNER,
                                    NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                                    java.util.List.of(new NewspaperPreviewBlock(
                                            NewspaperVisualBlockType.HEADLINE,
                                            "Weiter",
                                            null,
                                            0,
                                            0,
                                            2,
                                            2
                                    ))
                            ),
                            null,
                            java.util.List.of()
                    ),
                    true,
                    ""
            );
        }
    }
}
