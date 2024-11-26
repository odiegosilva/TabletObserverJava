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

        // Verifica se um log do mesmo tipo já existe
        boolean logExists = false;
        for (int i = 0; i < currentLogs.size(); i++) {
            if (currentLogs.get(i).getEventType().equals(log.getEventType())) {
                currentLogs.set(i, log); // Atualiza o log existente
                logExists = true;
                break;
            }
        }

        // Se o log não existir, adiciona à lista
        if (!logExists) {
            currentLogs.add(0, log);
        }

        liveLogs.setValue(currentLogs); // Atualiza os dados observados
    }
}