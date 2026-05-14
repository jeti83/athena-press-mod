package pro.jeti.athenapress.integration;

public record PlayerNewspaperLifecycleEvent(
        String playerId,
        PlayerNewspaperLifecycleEventType eventType
) {

    public static PlayerNewspaperLifecycleEvent playerConnected(String playerId) {
        return new PlayerNewspaperLifecycleEvent(
                playerId,
                PlayerNewspaperLifecycleEventType.PLAYER_CONNECTED
        );
    }

    public static PlayerNewspaperLifecycleEvent playerDisconnected(String playerId) {
        return new PlayerNewspaperLifecycleEvent(
                playerId,
                PlayerNewspaperLifecycleEventType.PLAYER_DISCONNECTED
        );
    }

    public static PlayerNewspaperLifecycleEvent sessionTimeout(String playerId) {
        return new PlayerNewspaperLifecycleEvent(
                playerId,
                PlayerNewspaperLifecycleEventType.SESSION_TIMEOUT
        );
    }

    public static PlayerNewspaperLifecycleEvent serverShutdown() {
        return new PlayerNewspaperLifecycleEvent(
                null,
                PlayerNewspaperLifecycleEventType.SERVER_SHUTDOWN
        );
    }
}