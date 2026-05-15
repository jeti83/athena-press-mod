package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.function.Consumer;

public class ScreenshotFileWatcher {

    private final Path watchDirectory;
    private final Consumer<Path> onNewScreenshot;
    private volatile boolean running = false;
    private Thread watchThread;

    public ScreenshotFileWatcher(Path watchDirectory, Consumer<Path> onNewScreenshot) {
        this.watchDirectory = watchDirectory;
        this.onNewScreenshot = onNewScreenshot;
    }

    public void start() throws IOException {
        if (running) return;
        if (!Files.isDirectory(watchDirectory)) {
            Files.createDirectories(watchDirectory);
        }

        running = true;
        WatchService watchService = FileSystems.getDefault().newWatchService();
        watchDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

        watchThread = new Thread(() -> {
            while (running) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path filename = (Path) event.context();
                        String name = filename.toString().toLowerCase();
                        if (name.endsWith(".png") || name.endsWith(".jpg")) {
                            Path fullPath = watchDirectory.resolve(filename);
                            onNewScreenshot.accept(fullPath);
                        }
                    }
                }

                if (!key.reset()) break;
            }

            try {
                watchService.close();
            } catch (IOException ignored) {
            }
        }, "athena-press-screenshot-watcher");

        watchThread.setDaemon(true);
        watchThread.start();
    }

    public void stop() {
        running = false;
        if (watchThread != null) {
            watchThread.interrupt();
        }
    }

    public boolean isRunning() {
        return running;
    }
}
