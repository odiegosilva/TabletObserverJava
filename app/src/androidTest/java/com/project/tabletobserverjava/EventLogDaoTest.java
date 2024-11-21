package com.project.tabletobserverjava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.util.Log;


import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.project.tabletobserverjava.data.dao.EventLogDao;
import com.project.tabletobserverjava.data.local.AppDatabase;
import com.project.tabletobserverjava.data.model.EventLog;
import com.project.tabletobserverjava.utils.LiveDataTestUtil;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Classe de teste para validar o comportamento do EventLogDao.
 * Este teste realiza operações de banco de dados, como inserção e recuperação de dados,
 * usando um banco de dados em memória para garantir isolamento.
 */
@RunWith(AndroidJUnit4.class)
public class EventLogDaoTest {

    private AppDatabase database;
    private EventLogDao eventLogDao;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();


    /**
     * Configuração inicial do teste.
     * Cria uma instância do banco de dados em memória e inicializa o DAO.
     */
    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        Log.d("Test", "Database initialized: " + (database != null));  // Log para verificar a inicialização
        eventLogDao = database.eventLogDao();
    }

    /**
     * Finaliza os recursos após cada teste.
     * Fecha a conexão com o banco de dados.
     */
    @After
    public void tearDown() {
        database.close();
    }

    /**
     * Teste de inserção e recuperação de logs.
     * Verifica se os logs inseridos são recuperados corretamente na ordem esperada.
     */
    @Test
    public void testInsertAndRetrieveLogs() throws InterruptedException {
        // Insere os logs no banco
        EventLog log1 = new EventLog(System.currentTimeMillis(), "ERROR", "Connection lost");
        EventLog log2 = new EventLog(System.currentTimeMillis(), "INFO", "System recovered");


        eventLogDao.insertLog(log1);
        eventLogDao.insertLog(log2);


        // Recupera os logs do LiveData
        List<EventLog> logs = LiveDataTestUtil.getValue(eventLogDao.getAllLogs());

        // Verifica os resultados
        assertNotNull(logs);
        assertEquals(2, logs.size());
        assertEquals("ERROR", logs.get(0).getEventType());
        assertEquals("INFO", logs.get(1).getEventType());
    }
}

