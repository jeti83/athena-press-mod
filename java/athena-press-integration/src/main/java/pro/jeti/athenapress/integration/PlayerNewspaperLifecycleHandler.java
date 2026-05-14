package pro.jeti.athenapress.integration;

public class PlayerNewspaperLifecycleHandler {

    private final AthenaPressIntegrationPlugin plugin;
    private final PlayerNewspaperUiPort uiPort;

    public PlayerNewspaperLifecycleHandler(
            AthenaPressIntegrationPlugin plugin,
            PlayerNewspaperUiPort uiPort
    ) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin must not be null");
        }

        if (uiPort == null) {
            throw new IllegalArgumentException("uiPort must not be null");
        }

        this.plugin = plugin;
        this.uiPort = uiPort;
    }

    public void handle(PlayerNewspaperLifecycleEvent event) {
        if (event == null || event.eventType() == null) {
            return;
        }

        switch (event.eventType()) {
            case PLAYER_CONNECTED -> handlePlayerConnected(event.playerId());
            case PLAYER_DISCONNECTED -> handlePlayerDisconnected(event.playerId());
            case SESSION_TIMEOUT -> handleSessionTimeout(event.playerId());
            case SERVER_SHUTDOWN -> handleServerShutdown();
        }
    }

    private void handlePlayerConnected(String playerId) {
        // Noch kein automatisches Öffnen.
        // Später: ungelesene Zeitung, Tagesausgabe oder NPC-Hinweis.
    }

    private void handlePlayerDisconnected(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            return;
        }

        if (plugin.hasOpenNewspaper(playerId)) {
            plugin.onPlayerCloseNewspaper(playerId);
            uiPort.close(playerId);
        }
    }

    private void handleSessionTimeout(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            return;
        }

        if (plugin.hasOpenNewspaper(playerId)) {
            plugin.onPlayerCloseNewspaper(playerId);
            uiPort.show(NewspaperUiView.closed(
                    playerId,
                    "Zeitungssitzung wegen Inaktivität geschlossen.\n"
            ));
        }
    }

    private void handleServerShutdown() {
        // Noch kein globales Session-Cleanup im Plugin verfügbar.
        // Später: alle offenen Spieler-Sessions schließen.
    }
}