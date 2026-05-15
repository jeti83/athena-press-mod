package pro.jeti.athenapress.integration;

public class HytaleNewspaperLifecycleAdapter<TPlayer> {

    private final PlayerNewspaperLifecycleHandler lifecycleHandler;
    private final HytalePlayerContextResolver<TPlayer> playerContextResolver;
    private final HytaleNewspaperVisualUiPort visualUiPort;

    public HytaleNewspaperLifecycleAdapter(
            PlayerNewspaperLifecycleHandler lifecycleHandler,
            HytalePlayerContextResolver<TPlayer> playerContextResolver
    ) {
        this(lifecycleHandler, playerContextResolver, null);
    }

    public HytaleNewspaperLifecycleAdapter(
            PlayerNewspaperLifecycleHandler lifecycleHandler,
            HytalePlayerContextResolver<TPlayer> playerContextResolver,
            HytaleNewspaperVisualUiPort visualUiPort
    ) {
        if (lifecycleHandler == null) {
            throw new IllegalArgumentException("lifecycleHandler must not be null");
        }

        if (playerContextResolver == null) {
            throw new IllegalArgumentException("playerContextResolver must not be null");
        }

        this.lifecycleHandler = lifecycleHandler;
        this.playerContextResolver = playerContextResolver;
        this.visualUiPort = visualUiPort;
    }

    public void onPlayerConnected(TPlayer player) {
        HytalePlayerContext playerContext = resolve(player);
        if (playerContext == null) {
            return;
        }

        if (visualUiPort != null) {
            visualUiPort.registerPlayer(playerContext);
        }

        lifecycleHandler.handle(
                PlayerNewspaperLifecycleEvent.playerConnected(playerContext.playerId())
        );
    }

    public void onPlayerDisconnected(TPlayer player) {
        HytalePlayerContext playerContext = resolve(player);
        if (playerContext == null) {
            return;
        }

        lifecycleHandler.handle(
                PlayerNewspaperLifecycleEvent.playerDisconnected(playerContext.playerId())
        );
    }

    public void onSessionTimeout(TPlayer player) {
        HytalePlayerContext playerContext = resolve(player);
        if (playerContext == null) {
            return;
        }

        lifecycleHandler.handle(
                PlayerNewspaperLifecycleEvent.sessionTimeout(playerContext.playerId())
        );
    }

    public void onServerShutdown() {
        lifecycleHandler.handle(PlayerNewspaperLifecycleEvent.serverShutdown());
    }

    private HytalePlayerContext resolve(TPlayer player) {
        if (player == null) {
            return null;
        }

        return playerContextResolver.resolve(player);
    }
}
