package com.client;

import com.common.*;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
public class DirectoryWatcher implements Runnable {
    private final Path watchDirectory;
    private final Consumer<FileChangeEvents> onChange;
    private final String clientId;
    private volatile boolean running = true;
    private WatchService watchService;
    private final Map<WatchKey, Path> watchKeyPathMap = new HashMap<>();
    public DirectoryWatcher(String directory, String clientId, Consumer<FileChangeEvents> onChange ) {
        this.watchDirectory = Paths.get(directory);
        this.clientId = clientId;
        this.onChange = onChange;
    }

    private void registerDirectory(Path directory) throws IOException {
        WatchKey key = directory.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        watchKeyPathMap.put(key, directory);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                if(Files.isDirectory(path)) {
                    registerDirectory(path);
                }
            }
        }
    }

    private void processEvents(WatchEvent<?> event, Path directory) throws IOException {
        WatchEvent.Kind<?> kind = event.kind();
        if (kind == StandardWatchEventKinds.OVERFLOW) return;

        Path fileName = (Path) event.context();
        Path fullPath = directory.resolve(fileName);


        FileChangeEvents.ChangeType changeType = null;
        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            changeType = FileChangeEvents.ChangeType.CREATED;
            if(Files.isDirectory(fullPath)) {
                try { registerDirectory(fullPath); }
                catch (IOException e) {}
            }
        }
        else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            changeType = FileChangeEvents.ChangeType.DELETED;
            if(Files.isDirectory(fullPath)) {
                try { registerDirectory(fullPath); }
                catch (IOException e) {}
            }
        }
        else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            changeType = FileChangeEvents.ChangeType.MODIFIED;
            if(Files.isDirectory(fullPath)) {
                try { registerDirectory(fullPath); }
                catch (IOException e) {}
            }
        }
        if (changeType != null) {
            FileChangeEvents fileChangeEvents = new FileChangeEvents(
                    fullPath.toString(), changeType, clientId, watchDirectory.toString()
            );
            if (onChange != null) {
                onChange.accept(fileChangeEvents);
            }
        }
    }


    private void closeWatchService() {
        if(watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {}
        }
    }
    public void stop() {
        running = false;
        closeWatchService();
    }
    public boolean isRunning() {
        return running;
    }
    @Override
    public void run() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            registerDirectory(watchDirectory);
            System.out.println("Đang giám sát: " + watchDirectory);

            while (running) {
                WatchKey key = watchService.poll(
                        Constants.WATCH_POLL_INTERVAL,
                        TimeUnit.MILLISECONDS
                );

                if (key == null) continue;

                Path directory = watchKeyPathMap.get(key);
                if(directory == null) {
                    key.reset();
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    processEvents(event, directory);
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            if (running) {
                System.err.println("Lỗi giám sát: " + e.getMessage());
            }
        } finally {
            closeWatchService();
        }
    }
}
