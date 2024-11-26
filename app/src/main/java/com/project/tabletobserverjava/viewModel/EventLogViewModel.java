package com.project.tabletobserverjava.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.project.tabletobserverjava.data.model.EventLog;
import com.project.tabletobserverjava.data.repository.EventLogRepository;

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

    private boolean saveLogsEnabled = false; // Flag de controle de persistência

    public EventLogViewModel(EventLogRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<EventLog>> getLiveLogs() {
        return liveLogs;
    }

    public void insertLog(EventLog log) {
        List<EventLog> currentLogs = liveLogs.getValue();
        if (currentLogs == null) currentLogs = new ArrayList<>();

        boolean logHandled = false;

        // Verifica se o log já existe e se pode ser substituído
        for (int i = 0; i < currentLogs.size(); i++) {
            EventLog existingLog = currentLogs.get(i);

            // Substituir logs dinâmicos (ex.: conexão)
            if (existingLog.getEventType().equals(log.getEventType())) {
                if (log.getEventType().equals("CONNECTION")) {
                    currentLogs.set(i, log); // Atualiza o log de conexão
                    logHandled = true;
                }
                break;
            }
        }

        // Adiciona logs fixos apenas uma vez
        if (!logHandled && !isFixedLog(log.getEventType())) {
            currentLogs.add(0, log);
        }

        liveLogs.setValue(currentLogs); // Atualiza o LiveData
    }

    /**
     * Verifica se um log é fixo (não deve ser substituído após ser adicionado).
     *
     * @param eventType Tipo do log.
     * @return true se for fixo, false caso contrário.
     */
    private boolean isFixedLog(String eventType) {
        return eventType.equals("INFO") || eventType.equals("DEBUG");
    }
}