package pro.jeti.athenapress.integration;

public interface PlayerNewspaperUiPort {

    void show(PlayerNewspaperResponse response);

    void show(NewspaperUiView view);

    void close(String playerId);
}