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
