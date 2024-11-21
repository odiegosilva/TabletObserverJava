package com.project.tabletobserverjava.data.repository;


import androidx.lifecycle.LiveData;

import com.project.tabletobserverjava.data.dao.EventLogDao;
import com.project.tabletobserverjava.data.model.EventLog;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Repositório para gerenciar as operações relacionadas a EventLog.
 * Centraliza o acesso ao DAO e gerencia threads para operações assíncronas.
 */
public class EventLogRepository {
    private final EventLogDao eventLogDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    /**
     * Construtor para inicializar o DAO.
     *
     * @param eventLogDao DAO para acesso ao banco de dados.
     */
    public EventLogRepository(EventLogDao eventLogDao) {
        this.eventLogDao = eventLogDao;
    }

    /**
     * Obtém todos os logs como LiveData.
     *
     * @return LiveData com a lista de logs.
     */
    public LiveData<List<EventLog>> getAllLogs() {
        return eventLogDao.getAllLogs();
    }

    /**
     * Insere um log no banco de dados.
     *
     * @param log Instância de EventLog a ser inserida.
     */
    public void insertLog(EventLog log) {
        executor.execute(() -> eventLogDao.insertLog(log));
    }
}
