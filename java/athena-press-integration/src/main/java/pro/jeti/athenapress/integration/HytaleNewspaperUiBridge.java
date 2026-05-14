package pro.jeti.athenapress.integration;

public interface HytaleNewspaperUiBridge {

    void openOrUpdate(HytalePlayerContext player, NewspaperUiView view);

    void close(HytalePlayerContext player);
}