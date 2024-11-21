package com.project.tabletobserverjava.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.project.tabletobserverjava.data.model.EventLog;

import java.util.List;

/**
 O DAO define as operações no banco de dados, como inserir e consultar logs.
 */

@Dao
public interface EventLogDao {
    @Insert
    void insertLog(EventLog eventLog);

    @Query("SELECT * FROM event_logs ORDER BY timestamp DESC")
    LiveData<List<EventLog>> getAllLogs(); // Atualiza automaticamente ao alterar os dados
}
