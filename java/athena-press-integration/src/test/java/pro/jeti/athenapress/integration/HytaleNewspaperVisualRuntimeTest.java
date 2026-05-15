package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    void runtimeResolvesPlayerForUiButtonConvenienceMethod() {
        CapturingVisualBridge bridge = new CapturingVisualBridge();
        HytaleNewspaperVisualRuntime<String> runtime =
                new HytaleNewspaperVisualRuntime<>(
                        new StubPlugin(),
                        new NoopTextUiPort(),
                        bridge,
                        new StubResolver()
                );

        runtime.onPlayerConnected("player-1");
        runtime.onPlayerUiButton("player-1", NewspaperVisualUiCommands.NEXT_SPREAD, null);

        assertEquals(1, bridge.lastView.spreadIndex());
    }

    @Test
    void runtimeIgnoresMissingPlayerForInputConvenienceMethod() {
        CapturingVisualBridge bridge = new CapturingVisualBridge();
        HytaleNewspaperVisualRuntime<String> runtime =
                new HytaleNewspaperVisualRuntime<>(
                        new StubPlugin(),
                        new NoopTextUiPort(),
                        bridge,
                        new StubResolver()
                );

        runtime.onPlayerUiButton(null, NewspaperVisualUiCommands.NEXT_SPREAD, null);

        assertNull(bridge.lastView);
    }

    @Test
    void runtimeOpensRealVisualIssueFromPlayerChatCommand() throws IOException {
        createVisualDataSet();
        CapturingVisualBridge bridge = new CapturingVisualBridge();
        HytaleNewspaperVisualRuntime<String> runtime =
                new HytaleNewspaperVisualRuntime<>(
                        new AthenaPressIntegrationPlugin(tempDir),
                        new NoopTextUiPort(),
                        bridge,
                        new StubResolver()
                );

        runtime.onPlayerConnected("player-1");
        runtime.onPlayerChatCommand("player-1", "/ap", "issue_visual");

        assertNotNull(bridge.lastView);
        assertTrue(bridge.lastView.newspaperOpen());
        assertEquals("issue_visual", bridge.lastView.issueId());
        assertEquals(0, bridge.lastView.spreadIndex());
    }

    @Test
    void runtimeDisconnectClosesRealVisualIssueAndReleasesPlayer() throws IOException {
        createVisualDataSet();
        CapturingVisualBridge bridge = new CapturingVisualBridge();
        HytaleNewspaperVisualRuntime<String> runtime =
                new HytaleNewspaperVisualRuntime<>(
                        new AthenaPressIntegrationPlugin(tempDir),
                        new NoopTextUiPort(),
                        bridge,
                        new StubResolver()
                );

        runtime.onPlayerConnected("player-1");
        runtime.onPlayerChatCommand("player-1", "/ap", "issue_visual");
        runtime.onPlayerDisconnected("player-1");

        assertEquals("player-1", bridge.closedPlayer.playerId());
        assertTrue(!runtime.visualUiPort().hasRegisteredPlayer("player-1"));
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
        private HytalePlayerContext closedPlayer;

        @Override
        public void openOrUpdate(
                HytalePlayerContext player,
                PlayerNewspaperVisualView view
        ) {
            this.lastView = view;
        }

        @Override
        public void close(HytalePlayerContext player) {
            this.closedPlayer = player;
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

    private void createVisualDataSet() throws IOException {
        Files.createDirectories(tempDir.resolve("articles").resolve("draft"));
        Files.createDirectories(tempDir.resolve("articles").resolve("published"));
        Files.createDirectories(tempDir.resolve("articles").resolve("archived"));
        Files.createDirectories(tempDir.resolve("issues").resolve("draft"));
        Files.createDirectories(tempDir.resolve("issues").resolve("published"));
        Files.createDirectories(tempDir.resolve("issues").resolve("archived"));

        Files.writeString(
                tempDir.resolve("articles").resolve("published").resolve("article_visual.json"),
                """
                {
                  "id": "article_visual",
                  "status": "published",
                  "categoryId": "stadtklatsch",
                  "title": "Laterne auf dem Platz",
                  "subtitle": "Ein helles Ereignis",
                  "teaser": "Die Menge wurde aufmerksam.",
                  "summary": "Die Laterne berichtete ausführlich.",
                  "body": "Kurzer Artikeltext."
                }
                """
        );

        Files.writeString(
                tempDir.resolve("issues").resolve("published").resolve("issue_visual.json"),
                """
                {
                  "id": "issue_visual",
                  "status": "published",
                  "issueNumber": 7,
                  "title": "Athena Sichtblatt",
                  "subtitle": "Frisch geblättert",
                  "articles": [
                    "article_visual"
                  ],
                  "cover": {
                    "mainArticleId": "article_visual",
                    "image": "placeholders/lantern.png"
                  }
                }
                """
        );
    }
}
