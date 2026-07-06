package pro.jeti.athenapress.integration;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@SuppressWarnings("deprecation") // testet den deprecateten Text-Fallback bewusst
class PlayerNewspaperLifecycleHandlerTest {

    @TempDir
    Path tempDir;

    @Test
    void rejectsMissingPlugin() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PlayerNewspaperLifecycleHandler(null, new CapturingUiPort())
        );
    }

    @Test
    void rejectsMissingTextUiPort() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PlayerNewspaperLifecycleHandler(new StubPlugin(), null)
        );
    }

    @Test
    void disconnectClosesTextAndVisualNewspapers() {
        StubPlugin plugin = new StubPlugin();
        plugin.textOpen = true;
        plugin.visualOpen = true;
        CapturingUiPort uiPort = new CapturingUiPort();
        CapturingVisualUiPort visualUiPort = new CapturingVisualUiPort();
        PlayerNewspaperLifecycleHandler handler =
                new PlayerNewspaperLifecycleHandler(plugin, uiPort, visualUiPort);

        handler.handle(PlayerNewspaperLifecycleEvent.playerDisconnected("player-1"));

        assertFalse(plugin.textOpen);
        assertFalse(plugin.visualOpen);
        assertEquals("player-1", uiPort.closedPlayerId);
        assertEquals("player-1", visualUiPort.releasedPlayerId);
    }

    @Test
    void disconnectReleasesVisualPlayerEvenWithoutOpenVisualIssue() {
        StubPlugin plugin = new StubPlugin();
        CapturingVisualUiPort visualUiPort = new CapturingVisualUiPort();
        PlayerNewspaperLifecycleHandler handler =
                new PlayerNewspaperLifecycleHandler(
                        plugin,
                        new CapturingUiPort(),
                        visualUiPort
                );

        handler.handle(PlayerNewspaperLifecycleEvent.playerDisconnected("player-1"));

        assertEquals("player-1", visualUiPort.releasedPlayerId);
    }

    @Test
    void sessionTimeoutClosesVisualIssueWithoutReleasingPlayerContext() {
        StubPlugin plugin = new StubPlugin();
        plugin.visualOpen = true;
        CapturingVisualUiPort visualUiPort = new CapturingVisualUiPort();
        PlayerNewspaperLifecycleHandler handler =
                new PlayerNewspaperLifecycleHandler(
                        plugin,
                        new CapturingUiPort(),
                        visualUiPort
                );

        handler.handle(PlayerNewspaperLifecycleEvent.sessionTimeout("player-1"));

        assertFalse(plugin.visualOpen);
        assertEquals("player-1", visualUiPort.closedPlayerId);
        assertNull(visualUiPort.releasedPlayerId);
    }

    @Test
    void hytaleVisualPortReleaseClosesAndUnregistersPlayer() {
        CapturingVisualBridge bridge = new CapturingVisualBridge();
        HytaleNewspaperVisualUiPort port = new HytaleNewspaperVisualUiPort(bridge);
        HytalePlayerContext player = new HytalePlayerContext("player-1", "Jeti");

        port.registerPlayer(player);
        port.releasePlayer("player-1");

        assertEquals(player, bridge.closedPlayer);
        assertFalse(port.hasRegisteredPlayer("player-1"));
    }

    @Test
    void serverShutdownClosesAllTextAndVisualSessions() {
        StubPlugin plugin = new StubPlugin();
        plugin.textOpen = true;
        plugin.visualOpen = true;
        PlayerNewspaperLifecycleHandler handler =
                new PlayerNewspaperLifecycleHandler(
                        plugin,
                        new CapturingUiPort(),
                        new CapturingVisualUiPort()
                );

        handler.handle(PlayerNewspaperLifecycleEvent.serverShutdown());

        assertFalse(plugin.textOpen);
        assertFalse(plugin.visualOpen);
    }

    private class StubPlugin extends AthenaPressIntegrationPlugin {
        private boolean textOpen;
        private boolean visualOpen;

        StubPlugin() {
            super(tempDir);
        }

        @Override
        public void onPlayerCloseNewspaper(String playerId) {
            textOpen = false;
        }

        @Override
        public boolean hasOpenNewspaper(String playerId) {
            return textOpen;
        }

        @Override
        public void onPlayerCloseVisualNewspaper(String playerId) {
            visualOpen = false;
        }

        @Override
        public boolean hasOpenVisualNewspaper(String playerId) {
            return visualOpen;
        }

        @Override
        public void closeAllNewspapers() {
            textOpen = false;
        }

        @Override
        public void closeAllVisualNewspapers() {
            visualOpen = false;
        }
    }

    private static class CapturingUiPort implements PlayerNewspaperUiPort {
        private String closedPlayerId;

        @Override
        public void show(PlayerNewspaperResponse response) {
        }

        @Override
        public void show(NewspaperUiView view) {
        }

        @Override
        public void close(String playerId) {
            this.closedPlayerId = playerId;
        }
    }

    private static class CapturingVisualUiPort implements PlayerNewspaperVisualUiPort {
        private String closedPlayerId;
        private String releasedPlayerId;

        @Override
        public void show(PlayerNewspaperVisualView view) {
        }

        @Override
        public void close(String playerId) {
            this.closedPlayerId = playerId;
        }

        @Override
        public void releasePlayer(String playerId) {
            this.releasedPlayerId = playerId;
        }
    }

    private static class CapturingVisualBridge implements HytaleNewspaperVisualUiBridge {
        private HytalePlayerContext closedPlayer;

        @Override
        public void openOrUpdate(
                HytalePlayerContext player,
                PlayerNewspaperVisualView view
        ) {
        }

        @Override
        public void close(HytalePlayerContext player) {
            this.closedPlayer = player;
        }
    }
}
