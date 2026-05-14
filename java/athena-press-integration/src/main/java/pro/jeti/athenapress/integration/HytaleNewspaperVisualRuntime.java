package pro.jeti.athenapress.integration;

public class HytaleNewspaperVisualRuntime<TPlayer> {

    private final AthenaPressIntegrationPlugin plugin;
    private final HytaleNewspaperVisualUiPort visualUiPort;
    private final PlayerNewspaperVisualUiController visualUiController;
    private final PlayerNewspaperVisualInputDispatcher visualInputDispatcher;
    private final HytaleNewspaperVisualInputAdapter visualInputAdapter;
    private final PlayerNewspaperLifecycleHandler lifecycleHandler;
    private final HytaleNewspaperLifecycleAdapter<TPlayer> lifecycleAdapter;

    public HytaleNewspaperVisualRuntime(
            AthenaPressIntegrationPlugin plugin,
            PlayerNewspaperUiPort textUiPort,
            HytaleNewspaperVisualUiBridge visualUiBridge,
            HytalePlayerContextResolver<TPlayer> playerContextResolver
    ) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin must not be null");
        }

        if (textUiPort == null) {
            throw new IllegalArgumentException("textUiPort must not be null");
        }

        if (visualUiBridge == null) {
            throw new IllegalArgumentException("visualUiBridge must not be null");
        }

        if (playerContextResolver == null) {
            throw new IllegalArgumentException("playerContextResolver must not be null");
        }

        this.plugin = plugin;
        this.visualUiPort = new HytaleNewspaperVisualUiPort(visualUiBridge);
        this.visualUiController =
                new PlayerNewspaperVisualUiController(plugin, visualUiPort);
        this.visualInputDispatcher =
                new PlayerNewspaperVisualInputDispatcher(visualUiController);
        this.visualInputAdapter =
                new HytaleNewspaperVisualInputAdapter(visualInputDispatcher);
        this.lifecycleHandler =
                new PlayerNewspaperLifecycleHandler(plugin, textUiPort, visualUiPort);
        this.lifecycleAdapter =
                new HytaleNewspaperLifecycleAdapter<>(
                        lifecycleHandler,
                        playerContextResolver,
                        visualUiPort
                );
    }

    public AthenaPressIntegrationPlugin plugin() {
        return plugin;
    }

    public HytaleNewspaperVisualUiPort visualUiPort() {
        return visualUiPort;
    }

    public PlayerNewspaperVisualUiController visualUiController() {
        return visualUiController;
    }

    public PlayerNewspaperVisualInputDispatcher visualInputDispatcher() {
        return visualInputDispatcher;
    }

    public HytaleNewspaperVisualInputAdapter visualInputAdapter() {
        return visualInputAdapter;
    }

    public PlayerNewspaperLifecycleHandler lifecycleHandler() {
        return lifecycleHandler;
    }

    public HytaleNewspaperLifecycleAdapter<TPlayer> lifecycleAdapter() {
        return lifecycleAdapter;
    }

    public void onPlayerConnected(TPlayer player) {
        lifecycleAdapter.onPlayerConnected(player);
    }

    public void onPlayerDisconnected(TPlayer player) {
        lifecycleAdapter.onPlayerDisconnected(player);
    }

    public void onSessionTimeout(TPlayer player) {
        lifecycleAdapter.onSessionTimeout(player);
    }

    public void onServerShutdown() {
        lifecycleAdapter.onServerShutdown();
    }

    public void onNpcInteraction(HytalePlayerContext player, String issueId) {
        visualInputAdapter.onNpcInteraction(player, issueId);
    }

    public void onItemUse(HytalePlayerContext player, String issueId) {
        visualInputAdapter.onItemUse(player, issueId);
    }

    public void onUiButton(HytalePlayerContext player, String command, String value) {
        visualInputAdapter.onUiButton(player, command, value);
    }

    public void onChatCommand(HytalePlayerContext player, String command, String value) {
        visualInputAdapter.onChatCommand(player, command, value);
    }

    public void onKeyBind(HytalePlayerContext player, String command, String value) {
        visualInputAdapter.onKeyBind(player, command, value);
    }

    public void onServerTrigger(HytalePlayerContext player, String command, String value) {
        visualInputAdapter.onServerTrigger(player, command, value);
    }
}
