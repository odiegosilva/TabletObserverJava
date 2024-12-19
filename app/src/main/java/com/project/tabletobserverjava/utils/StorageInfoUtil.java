package com.project.tabletobserverjava.utils;

import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class StorageInfoUtil {

    private static final String TAG = "StorageInfoUtil";
    private final Context context;

    public StorageInfoUtil(Context context) {
        this.context = context;
    }

    /**
     * Calcula o armazenamento total usando StatFs.
     */
    public static long getTotalStorage() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        return stat.getBlockSizeLong() * stat.getBlockCountLong();
    }

    /**
     * Calcula o armazenamento livre usando StatFs.
     */
    public static long getAvailableStorage() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        return stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
    }

    /**
     * Calcula o uso detalhado do armazenamento usando StorageStatsManager.
     */
    public static long getDetailedUsedStorage(Context context) {
        long usedStorage = 0;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) { // API 26+
            try {
                StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
                PackageManager packageManager = context.getPackageManager();
                StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                UUID storageUuid = storageManager.getUuidForPath(Environment.getDataDirectory());

                // Itera sobre os pacotes instalados
                List<PackageInfo> packages = packageManager.getInstalledPackages(0);
                for (PackageInfo packageInfo : packages) {
                    String packageName = packageInfo.packageName;
                    StorageStats stats = storageStatsManager.queryStatsForPackage(storageUuid, packageName, UserHandle.getUserHandleForUid(android.os.Process.myUid()));
                    usedStorage += stats.getAppBytes() + stats.getCacheBytes();
                }

            } catch (Exception e) {
                Log.e(TAG, "Erro ao calcular o uso de armazenamento detalhado: " + e.getMessage(), e);
            }
        }

        return usedStorage;
    }
}

