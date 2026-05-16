package pro.jeti.athenapress.integration;

@Deprecated
public interface HytaleNewspaperUiBridge {

    void openOrUpdate(HytalePlayerContext player, NewspaperUiView view);

    void close(HytalePlayerContext player);
}