package pro.jeti.athenapress.integration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HytaleNewspaperLifecycleAdapterTest {

    @TempDir
    Path tempDir;

    @Test
    void rejectsMissingLifecycleHandler() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new HytaleNewspaperLifecycleAdapter<>(
                        null,
                        new StubResolver()
                )
        );
    }

    @Test
    void rejectsMissingResolver() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new HytaleNewspaperLifecycleAdapter<String>(
                        new CapturingLifecycleHandler(),
                        null
                )
        );
    }

    @Test
    void connectsPlayerAndRegistersVisualContext() {
        CapturingLifecycleHandler handler = new CapturingLifecycleHandler();
        HytaleNewspaperVisualUiPort visualUiPort =
                new HytaleNewspaperVisualUiPort(new NoopVisualBridge());
        HytaleNewspaperLifecycleAdapter<String> adapter =
                new HytaleNewspaperLifecycleAdapter<>(
                        handler,
                        new StubResolver(),
                        visualUiPort
                );

        adapter.onPlayerConnected("player-1");

        assertEquals(PlayerNewspaperLifecycleEventType.PLAYER_CONNECTED,
                handler.lastEvent.eventType());
        assertEquals("player-1", handler.lastEvent.playerId());
        assertTrue(visualUiPort.hasRegisteredPlayer("player-1"));
    }

    @Test
    void disconnectsPlayer() {
        CapturingLifecycleHandler handler = new CapturingLifecycleHandler();
        HytaleNewspaperLifecycleAdapter<String> adapter =
                new HytaleNewspaperLifecycleAdapter<>(handler, new StubResolver());

        adapter.onPlayerDisconnected("player-1");

        assertEquals(PlayerNewspaperLifecycleEventType.PLAYER_DISCONNECTED,
                handler.lastEvent.eventType());
        assertEquals("player-1", handler.lastEvent.playerId());
    }

    @Test
    void dispatchesSessionTimeout() {
        CapturingLifecycleHandler handler = new CapturingLifecycleHandler();
        HytaleNewspaperLifecycleAdapter<String> adapter =
                new HytaleNewspaperLifecycleAdapter<>(handler, new StubResolver());

        adapter.onSessionTimeout("player-1");

        assertEquals(PlayerNewspaperLifecycleEventType.SESSION_TIMEOUT,
                handler.lastEvent.eventType());
        assertEquals("player-1", handler.lastEvent.playerId());
    }

    @Test
    void dispatchesServerShutdown() {
        CapturingLifecycleHandler handler = new CapturingLifecycleHandler();
        HytaleNewspaperLifecycleAdapter<String> adapter =
                new HytaleNewspaperLifecycleAdapter<>(handler, new StubResolver());

        adapter.onServerShutdown();

        assertEquals(PlayerNewspaperLifecycleEventType.SERVER_SHUTDOWN,
                handler.lastEvent.eventType());
    }

    @Test
    void ignoresMissingOrUnresolvablePlayer() {
        CapturingLifecycleHandler handler = new CapturingLifecycleHandler();
        HytaleNewspaperLifecycleAdapter<String> adapter =
                new HytaleNewspaperLifecycleAdapter<>(
                        handler,
                        player -> null
                );

        adapter.onPlayerConnected(null);
        adapter.onPlayerConnected("player-1");

        assertEquals(0, handler.events.size());
    }

    private class CapturingLifecycleHandler extends PlayerNewspaperLifecycleHandler {
        private final List<PlayerNewspaperLifecycleEvent> events = new ArrayList<>();
        private PlayerNewspaperLifecycleEvent lastEvent;

        CapturingLifecycleHandler() {
            super(
                    new AthenaPressIntegrationPlugin(tempDir),
                    new NoopUiPort()
            );
        }

        @Override
        public void handle(PlayerNewspaperLifecycleEvent event) {
            this.lastEvent = event;
            this.events.add(event);
        }
    }

    private static class StubResolver implements HytalePlayerContextResolver<String> {
        @Override
        public HytalePlayerContext resolve(String player) {
            return new HytalePlayerContext(player, "Spieler " + player);
        }
    }

    private static class NoopUiPort implements PlayerNewspaperUiPort {
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

    private static class NoopVisualBridge implements HytaleNewspaperVisualUiBridge {
        @Override
        public void openOrUpdate(
                HytalePlayerContext player,
                PlayerNewspaperVisualView view
        ) {
        }

        @Override
        public void close(HytalePlayerContext player) {
        }
    }
}
