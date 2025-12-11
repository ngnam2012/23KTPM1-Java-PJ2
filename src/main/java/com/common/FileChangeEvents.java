package com.common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class FileChangeEvents implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum ChangeType {
        CREATED("Tạo mới", "[+]"),
        MODIFIED("Chỉnh sửa", "[~]"),
        DELETED("Xóa", "[-]");

        private final String displayName;
        private final String icon;

        ChangeType(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
    }

    private final String filePath;
    private final ChangeType changeType;
    private final LocalDateTime timestamp;
    private final String clientId;
    private final String watchedDirectory;

    public FileChangeEvents(String filePath, ChangeType changeType,
                           String clientId, String watchedDirectory) {
        this.filePath = filePath;
        this.changeType = changeType;
        this.clientId = clientId;
        this.watchedDirectory = watchedDirectory;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getFilePath() { return filePath; }
    public ChangeType getChangeType() { return changeType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getClientId() { return clientId; }
    public String getWatchedDirectory() { return watchedDirectory; }

    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %s:  %s",
                getFormattedTime(),
                changeType.getIcon(),
                changeType.getDisplayName(),
                filePath);
    }
}