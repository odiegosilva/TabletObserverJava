package com.project.tabletobserverjava;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.project.tabletobserverjava.data.model.EventLog;
import com.project.tabletobserverjava.mock.MockEventLogRepository;
import com.project.tabletobserverjava.ui.theme.EventLogViewModel;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

public class EventLogViewModelTest {

    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock
    private Context mockContext; // Mock para o Context

    private EventLogViewModel viewModel;
    private MockEventLogRepository mockRepository; // Mock do repositório para simulação

    @Before
    public void setup() {
        mockRepository = new MockEventLogRepository();
        viewModel = new EventLogViewModel(mockRepository, mockContext);
    }

    @Test
    public void testAddInitialLogs() {
        // Adiciona logs fixos
        viewModel.insertLog(new EventLog(System.currentTimeMillis(), "INFO", "Aplicativo iniciado com sucesso."));
        viewModel.insertLog(new EventLog(System.currentTimeMillis(), "DEBUG", "Monitoramento iniciado."));

        // Verifica se os logs fixos estão presentes
        List<EventLog> logs = viewModel.getLiveLogs().getValue();
        assertEquals(2, logs.size());

        // Valida a ordem inversa (mais recente primeiro)
        assertEquals("Monitoramento iniciado.", logs.get(0).getDescription());
        assertEquals("Aplicativo iniciado com sucesso.", logs.get(1).getDescription());
    }

    @Test
    public void testUpdateConnectionLog() {
        // Simula "Dispositivo Conectado"
        viewModel.insertLog(new EventLog(System.currentTimeMillis(), "CONNECTION", "Dispositivo Conectado"));

        // Verifica o log de conexão
        List<EventLog> logs = viewModel.getLiveLogs().getValue();
        assertEquals(1, logs.size());
        assertEquals("Dispositivo Conectado", logs.get(0).getDescription());

        // Simula "Erro de conexão detectado"
        viewModel.insertLog(new EventLog(System.currentTimeMillis(), "CONNECTION", "Erro de conexão detectado"));

        // Verifica a atualização do log de conexão
        logs = viewModel.getLiveLogs().getValue();
        assertEquals(1, logs.size()); // Apenas 1 log de conexão deve existir
        assertEquals("Erro de conexão detectado", logs.get(0).getDescription());
    }

    @Test
    public void testUpdateDataUsageLog() {
        // Simula log inicial de consumo de dados
        viewModel.insertLog(new EventLog(System.currentTimeMillis(), "DATA_USAGE", "Consumo de dados Wi-Fi: 0.00 MB"));

        // Atualiza o log de consumo de dados
        viewModel.insertLog(new EventLog(System.currentTimeMillis(), "DATA_USAGE", "Consumo de dados Wi-Fi: 5.25 MB"));

        // Verifica se o log foi atualizado corretamente
        List<EventLog> logs = viewModel.getLiveLogs().getValue();
        assertEquals(1, logs.size()); // Apenas 1 log de consumo deve existir
        assertEquals("Consumo de dados Wi-Fi: 5.25 MB", logs.get(0).getDescription());
    }
}

