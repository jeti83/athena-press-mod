package pro.jeti.athenapress.integration;

public class PlayerNewspaperLifecycleHandler {

    private final AthenaPressIntegrationPlugin plugin;
    private final PlayerNewspaperUiPort uiPort;
    private final PlayerNewspaperVisualUiPort visualUiPort;

    public PlayerNewspaperLifecycleHandler(
            AthenaPressIntegrationPlugin plugin,
            PlayerNewspaperUiPort uiPort
    ) {
        this(plugin, uiPort, null);
    }

    public PlayerNewspaperLifecycleHandler(
            AthenaPressIntegrationPlugin plugin,
            PlayerNewspaperUiPort uiPort,
            PlayerNewspaperVisualUiPort visualUiPort
    ) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin must not be null");
        }

        if (uiPort == null) {
            throw new IllegalArgumentException("uiPort must not be null");
        }

        this.plugin = plugin;
        this.uiPort = uiPort;
        this.visualUiPort = visualUiPort;
    }

    public void handle(PlayerNewspaperLifecycleEvent event) {
        if (event == null || event.eventType() == null) {
            return;
        }

        switch (event.eventType()) {
            case PLAYER_CONNECTED -> handlePlayerConnected();
            case PLAYER_DISCONNECTED -> handlePlayerDisconnected(event.playerId());
            case SESSION_TIMEOUT -> handleSessionTimeout(event.playerId());
            case SERVER_SHUTDOWN -> handleServerShutdown();
        }
    }

    private void handlePlayerConnected() {
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

        closeVisualNewspaperForDisconnectedPlayer(playerId);
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

        closeVisualNewspaperForTimeout(playerId);
    }

    private void handleServerShutdown() {
        plugin.closeAllNewspapers();
        plugin.closeAllVisualNewspapers();
    }

    private void closeVisualNewspaperForDisconnectedPlayer(String playerId) {
        if (visualUiPort == null) {
            return;
        }

        if (plugin.hasOpenVisualNewspaper(playerId)) {
            plugin.onPlayerCloseVisualNewspaper(playerId);
        }

        visualUiPort.releasePlayer(playerId);
    }

    private void closeVisualNewspaperForTimeout(String playerId) {
        if (visualUiPort == null || !plugin.hasOpenVisualNewspaper(playerId)) {
            return;
        }

        plugin.onPlayerCloseVisualNewspaper(playerId);
        visualUiPort.close(playerId);
    }
}
