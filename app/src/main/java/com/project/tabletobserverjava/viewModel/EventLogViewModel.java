package com.project.tabletobserverjava.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.project.tabletobserverjava.data.model.EventLog;
import com.project.tabletobserverjava.data.repository.EventLogRepository;

import java.util.List;

/**
 * ViewModel para gerenciar os dados de EventLog.
 * Atua como uma ponte entre o repositório e a interface do usuário.
 */
public class EventLogViewModel extends ViewModel {

    private final EventLogRepository repository;

    /**
     * Construtor para inicializar o repositório.
     *
     * @param repository Instância do repositório para acesso aos dados.
     */
    public EventLogViewModel(EventLogRepository repository) {
        this.repository = repository;
    }

    /**
     * Obtém todos os logs armazenados como LiveData.
     *
     * @return LiveData com a lista de logs.
     */
    public LiveData<List<EventLog>> getAllLogs() {
        return repository.getAllLogs();
    }

    /**
     * Adiciona um novo log ao banco de dados.
     *
     * @param log Instância de EventLog a ser adicionada.
     */
    public void insertLog(EventLog log) {
        repository.insertLog(log);
    }
}