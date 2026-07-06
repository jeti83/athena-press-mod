package pro.jeti.athenapress.integration;

@SuppressWarnings("deprecation") // Methoden nutzen den deprecateten Text-Fallback (PlayerNewspaperResponse/NewspaperUiView)
public interface PlayerNewspaperUiPort {

    void show(PlayerNewspaperResponse response);

    void show(NewspaperUiView view);

    void close(String playerId);
}