package com.project.tabletobserverjava.ui.theme;

import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.project.tabletobserverjava.data.model.EventLog;
import com.project.tabletobserverjava.data.repository.EventLogRepository;
import com.project.tabletobserverjava.utils.StorageInfoUtil;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ViewModel para gerenciar os dados de EventLog.
 * Atua como uma ponte entre o repositório e a interface do usuário.
 */
public class EventLogViewModel extends ViewModel {

    private static final int MAX_LOGS = 20; // Limite máximo de logs na lista
    private final StorageInfoUtil storageInfoUtil; // Instância de StorageUtil
    private final EventLogRepository repository;
    private final MutableLiveData<List<EventLog>> liveLogs = new MutableLiveData<>(new ArrayList<>());

    private Context context;


    public EventLogViewModel(EventLogRepository repository, Context context){
        this.repository = repository;
        this.context = context;  // Armazene o contexto aqui
        this.storageInfoUtil = new StorageInfoUtil(context); // Inicializa corretamente a variável
    }


    private int getAndroidVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    public LiveData<List<EventLog>> getLiveLogs() {
        return liveLogs;
    }

    /**
     * Insere ou atualiza um log na lista observável.
     *
     * @param log Instância do log a ser inserida ou atualizada.
     */
    public void insertLog(EventLog log) {
        Log.d("EventLogViewModel", "Tentando inserir log: " + log.getEventType() + " - " + log.getDescription());

        List<EventLog> currentLogs = liveLogs.getValue();
        if (currentLogs == null) {
            currentLogs = new ArrayList<>();
            Log.d("EventLogViewModel", "Lista de logs inicializada.");
        }

        boolean logUpdated = false;

        // Verifica se um log do mesmo tipo já existe
        for (int i = 0; i < currentLogs.size(); i++) {
            if (currentLogs.get(i).getEventType().equals(log.getEventType())) {
                Log.d("EventLogViewModel", "Atualizando log existente: " + log.getEventType());
                currentLogs.set(i, log); // Atualiza o log existente
                logUpdated = true;
                break;
            }
        }

        // Se o log não existir, adiciona à lista
        if (!logUpdated) {
            Log.d("EventLogViewModel", "Adicionando novo log: " + log.getEventType());
            currentLogs.add(0, log);
        }

        // **Limita o número máximo de logs**
        if (currentLogs.size() >= MAX_LOGS) {
            Log.d("EventLogViewModel", "Lista de logs excedeu o limite, removendo o log mais antigo...");
            currentLogs.remove(currentLogs.size() - 1); // Remove o último log
        }

        // Atualiza a lista no LiveData
        liveLogs.postValue(currentLogs);
        Log.d("EventLogViewModel", "Lista de logs atualizada. Total de logs: " + currentLogs.size());
    }


    /**
     * Método auxiliar: Retorna o total de armazenamento usando StatFs.
     */
    private long getTotalStorage() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        return stat.getBlockSizeLong() * stat.getBlockCountLong();
    }

    /**
     * Converte bytes para GB.
     */
    private double toGB(long bytes) {
        return bytes / (1024.0 * 1024.0 * 1024.0);
    }




    /**
     * Método auxiliar: Retorna o espaço disponível usando StatFs.
     */
    private long getAvailableStorage() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        return stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
    }



    /**
     * Retorna o espaço usado no armazenamento interno, incluindo cache de aplicativos.
     */
    public long getUsedStorage() {
        return getTotalStorage() - getAvailableStorage();
    }

    /**
     * Retorna o tamanho do cache dos aplicativos (opcional).
     */
    public long getCacheSize(Context context) {
        long cacheSize = 0;

        // Cache interno;
        File internalCache = context.getCacheDir();
        cacheSize += getFolderSize(internalCache);

        // Cache externo (se disponível)
        File externalCache = context.getExternalCacheDir();
        if (externalCache != null) {
            cacheSize += getFolderSize(externalCache);
        }

        return cacheSize;
    }

    /**
     * Calcula o tamanho de uma pasta e todos os seus arquivos.
     */
    private long getFolderSize(File dir) {
        long size = 0;
        if (dir != null && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    size += getFolderSize(file);
                } else {
                    size += file.length();
                }
            }
        }
        return size;
    }


    /**
     * Calcula o tamanho de um diretório e seus subdiretórios.
     */
    private long getDirectorySize(File dir) {
        if (dir == null || !dir.exists()) {
            return 0;
        }

        long size = 0;
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                size += getDirectorySize(file);
            } else {
                size += file.length();
            }
        }
        return size;
    }

    /**
     * Calcula o percentual de uso do armazenamento interno.
     */
    public int getStorageUsagePercentage() {
        long total = getTotalStorage();
        long used = getUsedStorage();
        return (int) ((used * 100) / total);
    }

    /**
     * Atualiza os logs com informações sobre o armazenamento interno,
     * considerando o sistema operacional e cache.
     */

    /**
     * Método principal para atualizar logs de armazenamento.
     * Verifica a versão do Android e escolhe o método correto.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateStorageLogs(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
                StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                PackageManager packageManager = context.getPackageManager();
                UUID storageUuid = storageManager.getUuidForPath(Environment.getDataDirectory());

                long totalStorage = getTotalStorage(); // Total do armazenamento
                long availableStorage = getAvailableStorage(); // Espaço livre
                long appBytes = 0; // Bytes usados por aplicativos
                long cacheBytes = 0; // Bytes usados como cache

                // Itera por todos os pacotes instalados para calcular appBytes e cacheBytes
                for (PackageInfo packageInfo : packageManager.getInstalledPackages(0)) {
                    String packageName = packageInfo.packageName;

                    // Obtém as estatísticas de armazenamento do pacote
                    StorageStats stats = storageStatsManager.queryStatsForPackage(
                            storageUuid,
                            packageName,
                            UserHandle.getUserHandleForUid(android.os.Process.myUid())
                    );

                    appBytes += stats.getAppBytes();
                    cacheBytes += stats.getCacheBytes();
                }

                // Ajusta o cálculo de espaço usado (sem duplicar valores)
                long usedStorage = totalStorage - availableStorage;

                // Calcula o percentual de uso
                int usedPercentage = (int) ((usedStorage * 100) / totalStorage);

                // Adiciona um log detalhado
                insertLog(new EventLog(
                        System.currentTimeMillis(),
                        "STORAGE_STATS",
                        String.format("Total: %.2f GB, Usado: %.2f GB, Livre: %.2f GB (%d%%)",
                                totalStorage / (1024.0 * 1024.0 * 1024.0),
                                usedStorage / (1024.0 * 1024.0 * 1024.0),
                                availableStorage / (1024.0 * 1024.0 * 1024.0),
                                usedPercentage)
                ));

                // Aviso se o uso ultrapassar 90%
                if (usedPercentage > 90) {
                    insertLog(new EventLog(
                            System.currentTimeMillis(),
                            "WARNING",
                            "Uso de armazenamento acima de 90%."
                    ));
                }

            } catch (Exception e) {
                Log.e("EventLogViewModel", "Erro ao usar StorageStatsManager: " + e.getMessage(), e);
                insertLog(new EventLog(
                        System.currentTimeMillis(),
                        "STORAGE_STATS",
                        "Erro ao obter informações de armazenamento com StorageStatsManager."
                ));
            }
        } else {
            // Fallback para versões anteriores ao Android 8.0
            updateStorageForLegacyStorage(context);
        }
    }

    /**
     * Atualiza os logs para versões API >= 26 (Android 8+)
     */
    private void updateStorageForApi26AndAbove() {
        try {
            // Gerenciadores necessários
            StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            PackageManager packageManager = context.getPackageManager();

            // UUID do armazenamento principal
            UUID storageUuid = storageManager.getUuidForPath(Environment.getDataDirectory());

            long totalStorage = getTotalStorage(); // Capacidade total do armazenamento
            long appBytes = 0;                     // Espaço usado por apps
            long cacheBytes = 0;                   // Espaço usado pelo cache

            // Itera por todos os pacotes instalados
            List<PackageInfo> packages = packageManager.getInstalledPackages(0);
            for (PackageInfo pkg : packages) {
                String packageName = pkg.packageName;

                // Coleta dados de armazenamento do app
                StorageStats stats = storageStatsManager.queryStatsForPackage(
                        storageUuid,
                        packageName,
                        UserHandle.getUserHandleForUid(android.os.Process.myUid()) // Usando meu UID
                );
                appBytes += stats.getAppBytes();     // Dados do app
                cacheBytes += stats.getCacheBytes(); // Dados de cache
            }

            // Cálculo final de espaço
            long usedStorage = appBytes + cacheBytes;
            long availableStorage = totalStorage - usedStorage;
            int usedPercentage = (int) ((usedStorage * 100) / totalStorage);

            // Log do armazenamento atualizado
            insertLog(new EventLog(
                    System.currentTimeMillis(),
                    "STORAGE_STATS",
                    String.format("Total: %.2f GB, Usado: %.2f GB, Livre: %.2f GB (%d%%)",
                            totalStorage / (1024.0 * 1024.0 * 1024.0),
                            usedStorage / (1024.0 * 1024.0 * 1024.0),
                            availableStorage / (1024.0 * 1024.0 * 1024.0),
                            usedPercentage)
            ));

            // Alerta se o uso ultrapassar 90%
            if (usedPercentage > 90) {
                insertLog(new EventLog(
                        System.currentTimeMillis(),
                        "WARNING",
                        "Uso de armazenamento acima de 90% (StorageStatsManager)"
                ));
            }

        } catch (Exception e) {
            Log.e("EventLogViewModel", "Erro ao usar StorageStatsManager: " + e.getMessage(), e);
            insertLog(new EventLog(
                    System.currentTimeMillis(),
                    "STORAGE_STATS",
                    "Erro ao obter informações de armazenamento com StorageStatsManager."
            ));
        }
    }

    /**
     * Fallback: Atualiza os logs para versões API < 26 usando StatFs.
     */
    private void updateStorageForLegacyDevices() {
        try {
            long totalStorage = getTotalStorage();
            long availableStorage = getAvailableStorage();
            long usedStorage = totalStorage - availableStorage;
            int usedPercentage = (int) ((usedStorage * 100) / totalStorage);

            insertLog(new EventLog(
                    System.currentTimeMillis(),
                    "STORAGE_LEGACY",
                    String.format("Total: %.2f GB, Usado: %.2f GB, Livre: %.2f GB (%d%%)",
                            totalStorage / (1024.0 * 1024.0 * 1024.0),
                            usedStorage / (1024.0 * 1024.0 * 1024.0),
                            availableStorage / (1024.0 * 1024.0 * 1024.0),
                            usedPercentage)
            ));

        } catch (Exception e) {
            Log.e("EventLogViewModel", "Erro ao calcular armazenamento legado: " + e.getMessage(), e);
        }
    }


    /**
     * Atualiza o armazenamento para Android 8+ usando StorageStatsManager.
     */
    private void updateStorageUsingStatsManager() {
        try {
            StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            PackageManager packageManager = context.getPackageManager();
            UUID storageUuid = storageManager.getUuidForPath(Environment.getDataDirectory());

            long totalStorage = getTotalStorage(); // Capacidade total
            long appBytes = 0; // Armazenamento ocupado por apps
            long cacheBytes = 0; // Armazenamento ocupado pelo cache

            // Itera pelos pacotes instalados para obter os detalhes
            List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
            for (String packageName : installedPackages.stream()
                    .map(p -> p.packageName)
                    .collect(Collectors.toList())) {
                StorageStats stats = storageStatsManager.queryStatsForPackage(
                        storageUuid,
                        packageName,
                        UserHandle.getUserHandleForUid(android.os.Process.myUid())
                );

                appBytes += stats.getAppBytes();
                cacheBytes += stats.getCacheBytes();
            }

            long usedStorage = appBytes + cacheBytes; // Inclui o cache no uso
            long availableStorage = totalStorage - usedStorage;
            int usedPercentage = (int) ((usedStorage * 100) / totalStorage);

            // Insere log
            insertLog(new EventLog(
                    System.currentTimeMillis(),
                    "STORAGE",
                    String.format("Total: %.2f GB, Usado: %.2f GB, Livre: %.2f GB (%d%%)",
                            toGB(totalStorage), toGB(usedStorage), toGB(availableStorage), usedPercentage)
            ));

            // Alerta se o uso ultrapassar 90%
            if (usedPercentage > 90) {
                insertLog(new EventLog(
                        System.currentTimeMillis(),
                        "WARNING",
                        "Uso de armazenamento acima de 90% (StorageStatsManager)"
                ));
            }

        } catch (Exception e) {
            Log.e("EventLogViewModel", "Erro ao usar StorageStatsManager: " + e.getMessage(), e);
        }
    }


    /**
     * Fallback para versões do Android < 8.0.
     */
    private void updateStorageUsingStatFs() {
        try {
            long totalStorage = getTotalStorage();
            long availableStorage = getAvailableStorage();
            long usedStorage = totalStorage - availableStorage;
            int usedPercentage = (int) ((usedStorage * 100) / totalStorage);

            insertLog(new EventLog(
                    System.currentTimeMillis(),
                    "STORAGE",
                    String.format("Total: %.2f GB, Usado: %.2f GB, Livre: %.2f GB (%d%%)",
                            toGB(totalStorage), toGB(usedStorage), toGB(availableStorage), usedPercentage)
            ));

            if (usedPercentage > 90) {
                insertLog(new EventLog(
                        System.currentTimeMillis(),
                        "WARNING",
                        "Uso de armazenamento acima de 90% (StatFs)"
                ));
            }

        } catch (Exception e) {
            Log.e("EventLogViewModel", "Erro ao usar StatFs: " + e.getMessage(), e);
        }
    }



    private void updateStorageForLegacyStorage(Context context) {
        try {
            // Total e disponível usando StatFs
            File dataPath = Environment.getDataDirectory();
            StatFs statFs = new StatFs(dataPath.getPath());
            long totalStorage = statFs.getBlockSizeLong() * statFs.getBlockCountLong();
            long availableStorage = statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();

            // Espaço usado
            long usedStorage = totalStorage - availableStorage;

            // Inclui cache interno e externo
            long cacheSize = getCacheSize(context);
            usedStorage += cacheSize;

            int usedPercentage = (int) ((usedStorage * 100) / totalStorage);

            // Log detalhado
            insertLog(new EventLog(
                    System.currentTimeMillis(),
                    "STORAGE_LEGACY",
                    String.format("Total: %.2f GB, Usado: %.2f GB (Inclui cache), Livre: %.2f GB (%d%%)",
                            totalStorage / (1024.0 * 1024.0 * 1024.0),
                            usedStorage / (1024.0 * 1024.0 * 1024.0),
                            availableStorage / (1024.0 * 1024.0 * 1024.0),
                            usedPercentage)
            ));

            // Alerta se o uso ultrapassar 90%
            if (usedPercentage > 90) {
                insertLog(new EventLog(
                        System.currentTimeMillis(),
                        "WARNING",
                        "Uso de armazenamento acima de 90% (Legacy Storage)"
                ));
            }

        } catch (Exception e) {
            Log.e("EventLogViewModel", "Erro ao calcular armazenamento legado: " + e.getMessage(), e);
            insertLog(new EventLog(
                    System.currentTimeMillis(),
                    "ERROR",
                    "Erro ao calcular armazenamento com StatFs (Legacy)"
            ));
        }
    }

    private void updateStorageForScopedStorage(Context context) {
        try {
            StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            PackageManager packageManager = context.getPackageManager();
            UUID storageUuid = storageManager.getUuidForPath(Environment.getDataDirectory());

            long totalStorage = getTotalStorage();
            long appBytes = 0;
            long cacheBytes = 0;

            // Itera por todos os pacotes instalados para calcular uso de armazenamento
            List<PackageInfo> packages = packageManager.getInstalledPackages(0); // Obter pacotes instalados
            for (PackageInfo packageInfo : packages) {
                String packageName = packageInfo.packageName;

                // Usar android.os.Process.myUid() diretamente em vez de getIdentifier
                StorageStats stats = storageStatsManager.queryStatsForPackage(
                        storageUuid,
                        packageName,
                        UserHandle.getUserHandleForUid(android.os.Process.myUid())
                );

                appBytes += stats.getAppBytes();
                cacheBytes += stats.getCacheBytes();
            }

            long availableStorage = totalStorage - appBytes - cacheBytes;
            long usedStorage = totalStorage - availableStorage;
            int usedPercentage = (int) ((usedStorage * 100) / totalStorage);

            insertLog(new EventLog(
                    System.currentTimeMillis(),
                    "STORAGE_STATS",
                    String.format("Total: %.2f GB, Usado: %.2f GB, Livre: %.2f GB (%d%%)",
                            totalStorage / (1024.0 * 1024.0 * 1024.0),
                            usedStorage / (1024.0 * 1024.0 * 1024.0),
                            availableStorage / (1024.0 * 1024.0 * 1024.0),
                            usedPercentage)
            ));

            if (usedPercentage > 90) {
                insertLog(new EventLog(
                        System.currentTimeMillis(),
                        "WARNING",
                        "Uso de armazenamento acima de 90% (StorageStatsManager)"
                ));
            }
        } catch (Exception e) {
            Log.e("EventLogViewModel", "Erro ao calcular armazenamento Scoped Storage: " + e.getMessage(), e);
            insertLog(new EventLog(
                    System.currentTimeMillis(),
                    "STORAGE_STATS",
                    "Erro ao obter informações de armazenamento com StorageStatsManager."
            ));
        }
    }





    /**
     * Testa a latência da rede ativa.
     *
     * @param serverURL URL do servidor para teste (ex.: "https://www.google.com").
     */
    public void testLatency(String serverURL) {
        new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis();

                // Envia uma requisição HEAD para o servidor
                HttpURLConnection connection = (HttpURLConnection) new URL(serverURL).openConnection();
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(3000); // Timeout de 3 segundos
                connection.setReadTimeout(3000);

                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    long latency = System.currentTimeMillis() - startTime;
                    connection.disconnect();

                    // Log para conexão rápida ou lenta
                    String message = (latency < 300)
                            ? "Conexão rápida: " + latency + "ms"
                            : "Conexão lenta: " + latency + "ms";

                    // Atualiza ou substitui o log de latência
                    insertLog(new EventLog(System.currentTimeMillis(), "LATENCY", message));
                } else {
                    connection.disconnect();
                    // Atualiza ou substitui o log de erro de latência
                    insertLog(new EventLog(System.currentTimeMillis(), "LATENCY", "Falha ao medir latência"));
                }
            } catch (Exception e) {
                // Atualiza ou substitui o log de erro de latência
                insertLog(new EventLog(System.currentTimeMillis(), "LATENCY", "Erro ao medir latência: Internet foi deconectada "));
            }
        }).start();
    }
}