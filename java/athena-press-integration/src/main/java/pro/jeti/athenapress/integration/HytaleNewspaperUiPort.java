package pro.jeti.athenapress.integration;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class HytaleNewspaperUiPort implements PlayerNewspaperUiPort {

    private final HytaleNewspaperUiBridge uiBridge;
    private final Map<String, HytalePlayerContext> playersById = new HashMap<>();

    @Deprecated
    public HytaleNewspaperUiPort(HytaleNewspaperUiBridge uiBridge) {
        if (uiBridge == null) {
            throw new IllegalArgumentException("uiBridge must not be null");
        }

        this.uiBridge = uiBridge;
    }

    @Deprecated
    public void registerPlayer(HytalePlayerContext player) {
        if (player == null) {
            return;
        }

        playersById.put(player.playerId(), player);
    }

    @Deprecated
    public void unregisterPlayer(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            return;
        }

        playersById.remove(playerId);
    }

    @Deprecated
    @Override
    public void show(PlayerNewspaperResponse response) {
        if (response == null) {
            return;
        }

        show(new NewspaperUiViewFactory().fromResponse(response));
    }

    @Deprecated
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

    @Deprecated
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