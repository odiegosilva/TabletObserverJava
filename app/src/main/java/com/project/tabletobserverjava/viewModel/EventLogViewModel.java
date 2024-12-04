package com.project.tabletobserverjava.viewModel;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.project.tabletobserverjava.data.model.EventLog;
import com.project.tabletobserverjava.data.repository.EventLogRepository;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel para gerenciar os dados de EventLog.
 * Atua como uma ponte entre o repositório e a interface do usuário.
 */
public class EventLogViewModel extends ViewModel {

    private final EventLogRepository repository;
    private final MutableLiveData<List<EventLog>> liveLogs = new MutableLiveData<>(new ArrayList<>());
    private static final int MAX_LOGS = 20; // Limite máximo de logs na lista

    public EventLogViewModel(EventLogRepository repository) {
        this.repository = repository;
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

        // Limita o número máximo de logs
        if (currentLogs.size() > MAX_LOGS) {
            Log.d("EventLogViewModel", "Lista de logs excedeu o limite, truncando...");
            currentLogs = currentLogs.subList(0, MAX_LOGS);
        }

        liveLogs.postValue(currentLogs);// Atualiza os dados observados
        Log.d("EventLogViewModel", "Lista de logs atualizada. Total de logs: " + currentLogs.size());
    }

    /**
     * Retorna a capacidade total do armazenamento interno.
     */
    public long getTotalStorage() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getPath());
        long totalBytes = statFs.getBlockSizeLong() * statFs.getBlockCountLong();
        return totalBytes;
    }

    /**
     * Retorna o espaço livre no armazenamento interno.
     */
    public long getAvailableStorage() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getPath());
        long freeBytes = statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
        return freeBytes;
    }

    /**
     * Retorna o espaço usado no armazenamento interno.
     */
    public long getUsedStorage() {
        return getTotalStorage() - getAvailableStorage();
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
     * Atualiza os logs com informações sobre o armazenamento interno.
     */
    public void updateStorageLogs() {
        long total = getTotalStorage();
        long available = getAvailableStorage();
        long used = getUsedStorage();
        int percentage = getStorageUsagePercentage();

        insertLog(new EventLog(
                System.currentTimeMillis(),
                "STORAGE",
                String.format("Armazenamento total: %.2f GB, Usado: %.2f GB, Livre: %.2f GB (%d%%)",
                        total / (1024.0 * 1024.0 * 1024.0),
                        used / (1024.0 * 1024.0 * 1024.0),
                        available / (1024.0 * 1024.0 * 1024.0),
                        percentage)
        ));
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