package pro.jeti.athenapress.integration;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HytaleNewspaperVisualInputAdapterTest {

    @TempDir
    Path tempDir;

    @Test
    void rejectsMissingDispatcher() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new HytaleNewspaperVisualInputAdapter(null)
        );
    }

    @Test
    void forwardsUiButtonAsVisualInputEvent() {
        CapturingVisualInputDispatcher dispatcher = new CapturingVisualInputDispatcher();
        HytaleNewspaperVisualInputAdapter adapter =
                new HytaleNewspaperVisualInputAdapter(dispatcher);

        adapter.onUiButton(new HytalePlayerContext("player-1", "Jeti"), "weiter", null);

        assertEquals("player-1", dispatcher.lastEvent.playerId());
        assertEquals(PlayerNewspaperInputType.UI_BUTTON, dispatcher.lastEvent.inputType());
        assertEquals("weiter", dispatcher.lastEvent.command());
    }

    @Test
    void ignoresMissingPlayer() {
        CapturingVisualInputDispatcher dispatcher = new CapturingVisualInputDispatcher();
        HytaleNewspaperVisualInputAdapter adapter =
                new HytaleNewspaperVisualInputAdapter(dispatcher);

        adapter.onUiButton(null, "weiter", null);

        assertNull(dispatcher.lastEvent);
    }

    private class CapturingVisualInputDispatcher
            extends PlayerNewspaperVisualInputDispatcher {

        private PlayerNewspaperInputEvent lastEvent;

        CapturingVisualInputDispatcher() {
            super(new PlayerNewspaperVisualUiController(
                    new AthenaPressIntegrationPlugin(tempDir),
                    new NoopVisualUiPort()
            ));
        }

        @Override
        public void dispatch(PlayerNewspaperInputEvent event) {
            this.lastEvent = event;
        }
    }

    private static class NoopVisualUiPort implements PlayerNewspaperVisualUiPort {
        @Override
        public void show(PlayerNewspaperVisualView view) {
        }

        @Override
        public void close(String playerId) {
        }
    }
}
