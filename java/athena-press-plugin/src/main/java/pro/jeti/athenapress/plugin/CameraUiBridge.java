package pro.jeti.athenapress.plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import pro.jeti.athenapress.integration.HytaleCameraUiBridge;

/**
 * Bindet den Kamera-Aufnahme-Flow an die Hytale-UI.
 *
 * triggerScreenshot() schickt dem Spieler eine Chatnachricht mit der
 * Aufforderung, F12 zu drücken. Der ScreenshotFileWatcher registriert
 * die Aufnahme dann automatisch im Album.
 */
public class CameraUiBridge implements HytaleCameraUiBridge {

    private final Map<String, Object> playerRefs = new ConcurrentHashMap<>();

    public void registerPlayer(String playerId, Object playerRef) {
        playerRefs.put(playerId, playerRef);
    }

    public void unregisterPlayer(String playerId) {
        playerRefs.remove(playerId);
    }

    @Override
    public void triggerScreenshot(String playerId) {
        Object rawRef = playerRefs.get(playerId);
        if (rawRef == null) return;

        PlayerRef hytaleRef = (PlayerRef) rawRef;
        hytaleRef.sendMessage(Message.raw(
                "§e[AthenaPress Kamera] §fBereit! Druecke §eF12 §ffuer die Aufnahme. " +
                "Das Foto wird automatisch in deinem Album gespeichert."
        ));
    }

    @Override
    public void hideHud(String playerId) {
        // Hytale blendet das HUD bei F12-Screenshots je nach Einstellung
        // automatisch aus — kein expliziter Aufruf notwendig.
    }

    @Override
    public void showHud(String playerId) {
        // no-op, analog zu hideHud
    }
}
