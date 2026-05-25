package pro.jeti.athenapress.plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import pro.jeti.athenapress.integration.HytaleNewspaperVisualRuntime;
import pro.jeti.athenapress.integration.HytaleNewspaperVisualUiBridge;
import pro.jeti.athenapress.integration.HytalePlayerContext;
import pro.jeti.athenapress.integration.PlayerNewspaperVisualView;
import pro.jeti.athenapress.plugin.ui.NewspaperPage;

/**
 * Bindet das AthenaPress Visual-System an die Hytale-UI.
 *
 * openOrUpdate() öffnet bzw. aktualisiert die ingame NewspaperPage für
 * den jeweiligen Spieler über den WorldThread-Executor (World.execute()).
 */
public class NewspaperVisualBridge implements HytaleNewspaperVisualUiBridge {

    private final Map<String, Object> playerRefs = new ConcurrentHashMap<>();
    private volatile HytaleNewspaperVisualRuntime<?> runtime;

    // Wird von AthenaPressPlugin.setup() nach Runtime-Konstruktion gesetzt
    public void setRuntime(HytaleNewspaperVisualRuntime<?> runtime) {
        this.runtime = runtime;
    }

    public void registerPlayer(String playerId, Object playerRef) {
        playerRefs.put(playerId, playerRef);
    }

    public void unregisterPlayer(String playerId) {
        playerRefs.remove(playerId);
    }

    @Override
    public void openOrUpdate(HytalePlayerContext player, PlayerNewspaperVisualView view) {
        if (runtime == null) return;
        Object rawRef = playerRefs.get(player.playerId());
        if (rawRef == null) return;

        // Der echte Hytale PlayerRef – zur Compile-Zeit als Object gespeichert,
        // zur Laufzeit immer com.hypixel.hytale.server.core.universe.PlayerRef.
        PlayerRef hytaleRef = (PlayerRef) rawRef;
        var entityRef = hytaleRef.getReference();
        if (entityRef == null) return;
        var store     = entityRef.getStore();
        if (store == null || store.getExternalData() == null) return;
        var world     = store.getExternalData().getWorld();
        if (world == null) return;

        world.execute(() -> {
            Player hytalePlayer = store.getComponent(entityRef, Player.getComponentType());
            if (hytalePlayer == null) return;

            NewspaperPage page = new NewspaperPage(
                    hytaleRef, player, view,
                    (cmd, val) -> runtime.onUiButton(player, cmd, val)
            );
            hytalePlayer.getPageManager().openCustomPage(entityRef, store, page);
        });
    }

    @Override
    public void close(HytalePlayerContext player) {
        Object rawRef = playerRefs.get(player.playerId());
        if (rawRef == null) return;

        PlayerRef hytaleRef = (PlayerRef) rawRef;
        var entityRef = hytaleRef.getReference();
        if (entityRef == null) return;
        var store     = entityRef.getStore();
        if (store == null || store.getExternalData() == null) return;
        var world     = store.getExternalData().getWorld();
        if (world == null) return;

        world.execute(() -> {
            Player hytalePlayer = store.getComponent(entityRef, Player.getComponentType());
            if (hytalePlayer == null) return;

            var currentPage = hytalePlayer.getPageManager().getCustomPage();
            if (currentPage instanceof NewspaperPage np) {
                np.requestClose();
            }
        });
    }
}
