package com.project.tabletobserverjava.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.project.tabletobserverjava.data.model.EventLog;
import com.project.tabletobserverjava.data.repository.EventLogRepository;

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
        List<EventLog> currentLogs = liveLogs.getValue();
        if (currentLogs == null) currentLogs = new ArrayList<>();

        boolean logUpdated = false;

        // Verifica se um log do mesmo tipo já existe
        for (int i = 0; i < currentLogs.size(); i++) {
            if (currentLogs.get(i).getEventType().equals(log.getEventType())) {
                currentLogs.set(i, log); // Atualiza o log existente
                logUpdated = true;
                break;
            }
        }

        // Se o log não existir, adiciona à lista
        if (!logUpdated) {
            currentLogs.add(0, log);
        }

        // Limita o número máximo de logs
        if (currentLogs.size() > MAX_LOGS) {
            currentLogs = currentLogs.subList(0, MAX_LOGS);
        }

        liveLogs.postValue(currentLogs); // Atualiza os dados observados
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