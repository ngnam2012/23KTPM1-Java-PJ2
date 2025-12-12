package com.client;

import com.common.*;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.*;
public class DirectoryWatcher implements Runnable {
    private final Path watchDirectory;
    private final Consumer<FileChangeEvents> onChange;
    private final String clientId;
    public DirectoryWatcher(String directory, String clientId, Consumer<FileChangeEvents> onChange ) {
        this.watchDirectory = Paths.get(directory);
        this.clientId = clientId;
        this.onChange = onChange;
    }
    @Override
    public void run() {

    }
}
