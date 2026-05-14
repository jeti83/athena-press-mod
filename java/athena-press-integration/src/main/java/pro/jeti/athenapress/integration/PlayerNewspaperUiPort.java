package pro.jeti.athenapress.integration;

public interface PlayerNewspaperUiPort {

    void show(PlayerNewspaperResponse response);

    void close(String playerId);
}