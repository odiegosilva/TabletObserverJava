package com.project.tabletobserverjava.utils;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.content.Context;
import java.io.File;

public class StorageUtil {

    private Context context;

    public StorageUtil(Context context) {
        this.context = context;
    }

    // Método para obter o armazenamento total
    public long getTotalStorage() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getPath());
        long totalBytes = statFs.getBlockSizeLong() * statFs.getBlockCountLong();
        return totalBytes;
    }

    // Método para obter o espaço disponível
    public long getAvailableStorage() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getPath());
        long freeBytes = statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
        return freeBytes;
    }

    // Método para calcular o espaço usado
    public long getUsedStorage() {
        return getTotalStorage() - getAvailableStorage();
    }

    // Método para obter o espaço de cache do aplicativo
    public long getAppCacheSize() {
        File cacheDir = context.getCacheDir();
        return getDirectorySize(cacheDir);
    }

    // Método para calcular o espaço total usado pelo sistema
    public long getSystemUsedSpace() {
        long usedSpace = getUsedStorage();

        // Adiciona o espaço de cache e arquivos temporários
        usedSpace += getAppCacheSize();

        return usedSpace;
    }

    // Método para calcular o percentual de uso
    public int getStorageUsagePercentage() {
        long total = getTotalStorage();
        long used = getSystemUsedSpace();
        return (int) ((used * 100) / total);
    }

    // Método auxiliar para calcular o tamanho total de um diretório
    private long getDirectorySize(File dir) {
        long size = 0;
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        size += getDirectorySize(file);
                    } else {
                        size += file.length();
                    }
                }
            }
        }
        return size;
    }

    // Método para logar informações sobre o armazenamento
    public void updateStorageLogs() {
        long total = getTotalStorage();
        long available = getAvailableStorage();
        long used = getSystemUsedSpace();
        int percentage = getStorageUsagePercentage();

        Log.d("StorageUtil", String.format("Armazenamento total: %.2f GB, Usado: %.2f GB, Livre: %.2f GB (%d%%)",
                total / (1024.0 * 1024.0 * 1024.0),
                used / (1024.0 * 1024.0 * 1024.0),
                available / (1024.0 * 1024.0 * 1024.0),
                percentage));
    }
}

