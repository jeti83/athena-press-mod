package pro.jeti.athenapress.integration;

import java.util.HashMap;
import java.util.Map;

public class HytaleNewspaperUiPort implements PlayerNewspaperUiPort {

    private final HytaleNewspaperUiBridge uiBridge;
    private final Map<String, HytalePlayerContext> playersById = new HashMap<>();

    public HytaleNewspaperUiPort(HytaleNewspaperUiBridge uiBridge) {
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
        if (playerId == null || playerId.isBlank()) {
            return;
        }

        playersById.remove(playerId);
    }

    @Override
    public void show(PlayerNewspaperResponse response) {
        if (response == null) {
            return;
        }

        show(new NewspaperUiViewFactory().fromResponse(response));
    }

    @Override
    public void show(NewspaperUiView view) {
        if (view == null || view.playerId() == null) {
            return;
        }

        HytalePlayerContext player = playersById.get(view.playerId());
        if (player == null) {
            return;
        }

        if (view.closeRequested()) {
            uiBridge.close(player);
            return;
        }

        uiBridge.openOrUpdate(player, view);
    }

    @Override
    public void close(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            return;
        }

        HytalePlayerContext player = playersById.get(playerId);
        if (player != null) {
            uiBridge.close(player);
        }
    }
}