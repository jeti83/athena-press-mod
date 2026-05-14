package pro.jeti.athenapress.integration;

public interface HytaleNewspaperVisualUiBridge {

    void openOrUpdate(HytalePlayerContext player, PlayerNewspaperVisualView view);

    void close(HytalePlayerContext player);
}
