package pro.jeti.athenapress.integration;

public class ConsoleNewspaperUiPort implements PlayerNewspaperUiPort {

    @Override
    public void show(PlayerNewspaperResponse response) {
        if (response == null) {
            return;
        }

        if (response.closeRequested()) {
            close(response.playerId());
            return;
        }

        System.out.print(response.text());
    }

    @Override
    public void close(String playerId) {
        System.out.println("Zeitungs-UI geschlossen für Spieler: " + playerId);
    }
}