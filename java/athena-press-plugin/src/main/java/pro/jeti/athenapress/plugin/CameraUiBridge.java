package pro.jeti.athenapress.plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import pro.jeti.athenapress.integration.HytaleCameraUiBridge;

/**
 * Bindet den Kamera-Aufnahme-Flow an die Hytale-UI.
 *
 * Der ScreenshotFileWatcher in CameraScreenshotService überwacht bereits
 * den Screenshots-Ordner und verarbeitet neue Dateien automatisch.
 * Diese Klasse muss nur noch HUD ein-/ausblenden und den Auslöser senden.
 */
public class CameraUiBridge implements HytaleCameraUiBridge {

    // TODO: Ersetzen durch Map<String, Ref<EntityStore>>
    private final Map<String, Object> playerRefs = new ConcurrentHashMap<>();

    public void registerPlayer(String playerId, Object playerRef) {
        playerRefs.put(playerId, playerRef);
    }

    public void unregisterPlayer(String playerId) {
        playerRefs.remove(playerId);
    }

    @Override
    public void hideHud(String playerId) {
        Object ref = playerRefs.get(playerId);
        if (ref == null) return;

        // TODO: Echte Implementierung – HUD für den Spieler ausblenden.
        //
        // Option A (Hytale-native, falls API verfügbar):
        //   world.execute(() -> player.setHudVisible(false));
        //
        // Option B (über Paket/Nachricht, falls kein direkter Aufruf):
        //   server.getPacketManager().send(ref, new HideHudPacket());
    }

    @Override
    public void showHud(String playerId) {
        Object ref = playerRefs.get(playerId);
        if (ref == null) return;

        // TODO: Echte Implementierung – HUD wieder einblenden.
        //   world.execute(() -> player.setHudVisible(true));
    }

    @Override
    public void triggerScreenshot(String playerId) {
        // Der ScreenshotFileWatcher reagiert automatisch auf neue PNGs
        // im Screenshots-Ordner. Diese Methode muss nur den Auslöser senden.
        //
        // TODO: Echte Implementierung:
        //
        // Option A (Hytale Machinima-API, falls verfügbar):
        //   machinimaTool.captureScreenshot(playerRef);
        //
        // Option B (Keyboard-Event senden, als Fallback):
        //   server.getInputManager().simulateKeyPress(ref, KeyCode.F12);
        //
        // Option C: Spieler über Chat-Nachricht informieren, manuell F12 drücken
        //   (kein automatisches Auslösen – einfachster Fallback)
    }
}
