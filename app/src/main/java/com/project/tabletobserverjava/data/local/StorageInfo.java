package com.project.tabletobserverjava.data.local;

public class StorageInfo {
    private final String type; // Ex.: "Interno", "Externo"
    private final long totalSpace; // Total em bytes
    private final long freeSpace; // Livre em bytes

    public StorageInfo(String type, long totalSpace, long freeSpace) {
        this.type = type;
        this.totalSpace = totalSpace;
        this.freeSpace = freeSpace;
    }

    public String getType() {
        return type;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public long getFreeSpace() {
        return freeSpace;
    }
}
