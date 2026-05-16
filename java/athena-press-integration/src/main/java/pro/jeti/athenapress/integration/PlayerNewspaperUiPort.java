package pro.jeti.athenapress.integration;

@Deprecated
public interface PlayerNewspaperUiPort {

    void show(PlayerNewspaperResponse response);

    void show(NewspaperUiView view);

    void close(String playerId);
}