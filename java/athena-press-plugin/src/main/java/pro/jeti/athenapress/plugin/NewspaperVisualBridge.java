package pro.jeti.athenapress.plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// TODO: Paketnamen gegen HytaleServer.jar prüfen
// import com.hypixel.hytale.server.core.entity.player.Player;
// import com.flecs.Ref; // oder com.hypixel.hytale.ecs.Ref

import pro.jeti.athenapress.integration.HytaleNewspaperVisualUiBridge;
import pro.jeti.athenapress.integration.HytalePlayerContext;
import pro.jeti.athenapress.integration.PlayerNewspaperVisualView;

/**
 * Bindet das AthenaPress Visual-System an die Hytale-UI.
 *
 * Jede Methode markiert genau einen Schritt der NoesisGUI-Anbindung –
 * der umgebende AthenaPress-Code ist vollständig, nur der UI-Aufruf
 * muss gegen die reale Hytale-API ausgefüllt werden.
 */
public class NewspaperVisualBridge implements HytaleNewspaperVisualUiBridge {

    // TODO: Ersetzen durch Map<String, Ref<EntityStore>> sobald
    //       Player-Objekte aus Events extrahiert werden können.
    //       Wird in AthenaPressPlugin beim Connect-Event befüllt.
    private final Map<String, Object> playerRefs = new ConcurrentHashMap<>();

    public void registerPlayer(String playerId, Object playerRef) {
        playerRefs.put(playerId, playerRef);
    }

    public void unregisterPlayer(String playerId) {
        playerRefs.remove(playerId);
    }

    @Override
    public void openOrUpdate(HytalePlayerContext player, PlayerNewspaperVisualView view) {
        Object ref = playerRefs.get(player.playerId());
        if (ref == null) return;

        // TODO: Echte NoesisGUI-Implementierung:
        //
        // World world = /* aus player-ref holen */;
        // world.execute(() -> {
        //     Page page = buildNewspaperPage(view);
        //     player.getPageManager().openCustomPage(ref, store, page);
        // });
        //
        // buildNewspaperPage(view) muss PlayerNewspaperVisualView
        // in eine Hytale-seitige Page-Klasse übersetzen:
        // - view.leftPage() und view.rightPage() → linke/rechte Seite
        // - view.navigationState() → Weiter/Zurück-Buttons
        // - view.issueTitle() → Seitentitel

        // Vorläufig: Konsolenausgabe (funktionale Brücke ohne natives UI)
        System.out.printf("[AthenaPress] %s – Seite %d/%d%n",
                view.issueTitle(),
                view.spreadIndex() + 1,
                view.totalSpreads());
    }

    @Override
    public void close(HytalePlayerContext player) {
        Object ref = playerRefs.get(player.playerId());
        if (ref == null) return;

        // TODO: Echte Implementierung:
        // world.execute(() -> player.getPageManager().closePage(ref, store));

        System.out.printf("[AthenaPress] Zeitung geschlossen für %s%n",
                player.playerName());
    }
}
