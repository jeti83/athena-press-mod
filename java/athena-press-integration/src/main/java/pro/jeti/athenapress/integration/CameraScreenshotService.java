package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import pro.jeti.athenapress.service.PlayerAlbumService;

public class CameraScreenshotService {

    private final HytaleCameraUiBridge cameraBridge;
    private final PlayerAlbumService albumService;
    private final Path imagesUploadedRoot;
    private final ScreenshotFileWatcher fileWatcher;

    private final Map<String, CameraState> stateByPlayer = new ConcurrentHashMap<>();
    private final Map<String, String> pendingPlayerByContext = new HashMap<>();

    public CameraScreenshotService(
            HytaleCameraUiBridge cameraBridge,
            PlayerAlbumService albumService,
            Path athenaPressRoot,
            Path screenshotWatchDirectory
    ) throws IOException {
        this.cameraBridge = cameraBridge;
        this.albumService = albumService;
        this.imagesUploadedRoot = athenaPressRoot.resolve("images").resolve("uploaded");
        Files.createDirectories(imagesUploadedRoot);

        this.fileWatcher = new ScreenshotFileWatcher(
                screenshotWatchDirectory,
                this::onScreenshotDetected
        );
        this.fileWatcher.start();
    }

    /**
     * Called when the player left-clicks with the camera item equipped.
     * Sequence: hide HUD → screenshot → restore HUD → detect file → register in album.
     */
    public void onCameraItemUse(String playerId, String playerName) {
        if (stateByPlayer.getOrDefault(playerId, CameraState.IDLE) != CameraState.IDLE) {
            return;
        }

        stateByPlayer.put(playerId, CameraState.HIDING_HUD);
        pendingPlayerByContext.put(buildCaptureKey(playerName), playerName);

        cameraBridge.hideHud(playerId);
        stateByPlayer.put(playerId, CameraState.CAPTURING);
        cameraBridge.triggerScreenshot(playerId);
        stateByPlayer.put(playerId, CameraState.RESTORING_HUD);
        cameraBridge.showHud(playerId);
        stateByPlayer.put(playerId, CameraState.IDLE);
    }

    /**
     * Called by the ScreenshotFileWatcher when a new PNG appears in the watch directory.
     * Copies it to AthenaPress/images/uploaded/ and registers it in the player's album.
     */
    private void onScreenshotDetected(Path screenshotFile) {
        String pendingPlayer = findPendingPlayer();
        if (pendingPlayer == null) return;

        String timestamp = OffsetDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "cam_" + pendingPlayer + "_" + timestamp + ".png";
        Path destination = imagesUploadedRoot.resolve(filename);

        try {
            Files.copy(screenshotFile, destination, StandardCopyOption.REPLACE_EXISTING);
            albumService.addPhoto(pendingPlayer, filename, "Foto " + timestamp);
            clearPendingPlayer(pendingPlayer);
        } catch (IOException e) {
            clearPendingPlayer(pendingPlayer);
        }
    }

    public CameraState getState(String playerId) {
        return stateByPlayer.getOrDefault(playerId, CameraState.IDLE);
    }

    public void shutdown() {
        fileWatcher.stop();
    }

    private String findPendingPlayer() {
        return pendingPlayerByContext.values().stream().findFirst().orElse(null);
    }

    private void clearPendingPlayer(String playerName) {
        pendingPlayerByContext.entrySet().removeIf(e -> playerName.equals(e.getValue()));
    }

    private String buildCaptureKey(String playerName) {
        return playerName + "_" + System.currentTimeMillis();
    }
}
