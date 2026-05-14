package pro.jeti.athenapress.integration;

import java.util.HashMap;
import java.util.Map;

public class HytaleNewspaperVisualUiPort implements PlayerNewspaperVisualUiPort {

    private final HytaleNewspaperVisualUiBridge uiBridge;
    private final Map<String, HytalePlayerContext> playersById = new HashMap<>();

    public HytaleNewspaperVisualUiPort(HytaleNewspaperVisualUiBridge uiBridge) {
        if (uiBridge == null) {
            throw new IllegalArgumentException("uiBridge must not be null");
        }

        this.uiBridge = uiBridge;
    }

    public void registerPlayer(HytalePlayerContext player) {
        if (player == null) {
            return;
        }

        playersById.put(player.playerId(), player);
    }

    public void unregisterPlayer(String playerId) {
        if (!hasText(playerId)) {
            return;
        }

        playersById.remove(playerId);
    }

    public boolean hasRegisteredPlayer(String playerId) {
        return hasText(playerId) && playersById.containsKey(playerId);
    }

    public int registeredPlayerCount() {
        return playersById.size();
    }

    @Override
    public void show(PlayerNewspaperVisualView view) {
        if (view == null || !hasText(view.playerId())) {
            return;
        }

        HytalePlayerContext player = playersById.get(view.playerId());
        if (player == null) {
            return;
        }

        uiBridge.openOrUpdate(player, view);
    }

    @Override
    public void close(String playerId) {
        if (!hasText(playerId)) {
            return;
        }

        HytalePlayerContext player = playersById.get(playerId);
        if (player != null) {
            uiBridge.close(player);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
