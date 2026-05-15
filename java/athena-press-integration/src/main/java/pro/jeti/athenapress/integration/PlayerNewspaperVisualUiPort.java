package pro.jeti.athenapress.integration;

public interface PlayerNewspaperVisualUiPort {

    void show(PlayerNewspaperVisualView view);

    void close(String playerId);

    default void releasePlayer(String playerId) {
        close(playerId);
    }
}
