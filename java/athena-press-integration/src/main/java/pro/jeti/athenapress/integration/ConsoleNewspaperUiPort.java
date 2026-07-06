package pro.jeti.athenapress.integration;

@Deprecated
public class ConsoleNewspaperUiPort implements PlayerNewspaperUiPort {

    @Deprecated
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

    @Deprecated
    @Override
    public void show(NewspaperUiView view) {
        if (view == null) {
            return;
        }

        if (view.closeRequested()) {
            close(view.playerId());
            return;
        }

        System.out.println();
        System.out.println("== " + view.title() + " ==");
        System.out.print(view.body());

        if (!view.buttons().isEmpty()) {
            System.out.println();
            System.out.println("Aktionen:");
            for (NewspaperUiButton button : view.buttons()) {
                System.out.println("- " + button.label());
            }
        }
    }

    @Deprecated
    @Override
    public void close(String playerId) {
        System.out.println("Zeitungs-UI geschlossen für Spieler: " + playerId);
    }
}