package pro.jeti.athenapress.plugin;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import pro.jeti.athenapress.integration.HytalePlayerContext;
import pro.jeti.athenapress.integration.HytalePlayerContextResolver;

/**
 * Verwaltet HytalePlayerContext-Objekte pro Spieler.
 * Wird bei PlayerConnectEvent befüllt und bei PlayerDisconnectEvent geleert.
 * TPlayer = String (Spieler-UUID als String)
 */
public class PlayerContextResolver implements HytalePlayerContextResolver<String> {

    private final Map<String, HytalePlayerContext> contexts = new ConcurrentHashMap<>();

    @Override
    public HytalePlayerContext resolve(String playerId) {
        return contexts.getOrDefault(
                playerId,
                new HytalePlayerContext(playerId, playerId)
        );
    }

    public void register(String playerId, String playerName) {
        contexts.put(playerId, new HytalePlayerContext(playerId, playerName));
    }

    public void unregister(String playerId) {
        contexts.remove(playerId);
    }

    public boolean isRegistered(String playerId) {
        return contexts.containsKey(playerId);
    }
}
