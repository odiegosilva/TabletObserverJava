package com.project.tabletobserverjava.mock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.tabletobserverjava.data.model.EventLog;
import com.project.tabletobserverjava.data.repository.EventLogRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock para simular o comportamento do EventLogRepository.
 */
public class MockEventLogRepository extends EventLogRepository {

    private final MutableLiveData<List<EventLog>> liveDataLogs = new MutableLiveData<>(new ArrayList<>());

    public MockEventLogRepository() {
        super(null); // Passa null porque o Room não será usado aqui
    }

    @Override
    public LiveData<List<EventLog>> getAllLogs() {
        return liveDataLogs;
    }

    @Override
    public void insertLog(EventLog log) {
        List<EventLog> currentLogs = liveDataLogs.getValue();
        if (currentLogs == null) currentLogs = new ArrayList<>();

        // Verifica se já existe um log do mesmo tipo
        boolean logExists = false;
        for (int i = 0; i < currentLogs.size(); i++) {
            if (currentLogs.get(i).getEventType().equals(log.getEventType())) {
                currentLogs.set(i, log); // Atualiza o log existente
                logExists = true;
                break;
            }
        }

        // Se o log não existir, adiciona
        if (!logExists) {
            currentLogs.add(0, log);
        }

        liveDataLogs.setValue(currentLogs); // Atualiza os logs simulados
    }
}