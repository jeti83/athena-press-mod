package pro.jeti.athenapress.integration;

public class ConsoleNewspaperVisualUiPort implements PlayerNewspaperVisualUiPort {

    @Override
    public void show(PlayerNewspaperVisualView view) {
        if (view == null) {
            return;
        }

        if (!view.newspaperOpen()) {
            System.out.println();
            System.out.println("== " + view.title() + " ==");
            System.out.println(view.message());
            return;
        }

        System.out.println();
        System.out.println("== " + view.title() + " ==");
        System.out.println("Doppelseite " + (view.spreadIndex() + 1)
                + " / " + view.totalSpreadCount());
        printPage("Links", view.leftPage());
        printPage("Rechts", view.rightPage());

        if (!view.buttons().isEmpty()) {
            System.out.println("Aktionen:");
            for (NewspaperUiButton button : view.buttons()) {
                System.out.println("- " + button.label());
            }
        }
    }

    @Override
    public void close(String playerId) {
        System.out.println("Visuelle Zeitungs-UI geschlossen für Spieler: " + playerId);
    }

    private void printPage(String side, NewspaperPreviewPage page) {
        if (page == null) {
            System.out.println(side + ": [leer]");
            return;
        }

        System.out.println(side + ": Seite " + page.pageNumber() + " " + page.title());
    }
}
