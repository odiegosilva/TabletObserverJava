package com.project.tabletobserverjava.ui.theme;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tabletobserverjava.R;
import com.project.tabletobserverjava.data.local.AppDatabase;
import com.project.tabletobserverjava.data.model.EventLog;
import com.project.tabletobserverjava.data.repository.EventLogRepository;
import com.project.tabletobserverjava.viewModel.EventLogViewModel;
import com.project.tabletobserverjava.viewModel.EventLogViewModelFactory;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Fragment que exibe os logs armazenados em uma RecyclerView.
 * Observa mudanças no ViewModel e atualiza a interface automaticamente.
 */
public class EventLogFragment extends Fragment {

    private Handler handler = new Handler();
    private Runnable logUpdateRunnable;
    private EventLogViewModel viewModel;
    private EventLogAdapter adapter;

    // Variáveis para calcular o consumo de dados
    private long initialRxBytes = 0;
    private long initialTxBytes = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Configuração do RecyclerView e Adapter
            RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

            // Configuração inicial do Adapter
            adapter = new EventLogAdapter(new ArrayList<>());
            recyclerView.setAdapter(adapter);

            // Configuração do ViewModel
            EventLogRepository repository = new EventLogRepository(
                    AppDatabase.getInstance(requireContext()).eventLogDao()
            );
            EventLogViewModelFactory factory = new EventLogViewModelFactory(repository, requireContext());
            viewModel = new ViewModelProvider(this, factory).get(EventLogViewModel.class);

            // Verificação de inicialização do ViewModel
            if (viewModel != null) {
                Log.d("EventLogFragment", "ViewModel inicializado com sucesso.");
            } else {
                Log.e("EventLogFragment", "Falha ao inicializar o ViewModel.");
            }

            // Observa mudanças nos logs
            viewModel.getLiveLogs().observe(getViewLifecycleOwner(), logs -> adapter.updateLogs(logs));

            // Inicializa os valores de tráfego de dados
            initialRxBytes = TrafficStats.getTotalRxBytes();
            initialTxBytes = TrafficStats.getTotalTxBytes();

            // Adiciona logs iniciais
            addInitialLogs();


            // Timer para atualizar logs de conexão
            logUpdateRunnable = () -> {
                updateConnectionLog(); // Atualiza apenas o log de conexão
                updateDataUsageLog();  // Atualiza o log de consumo de dados
                updateMemoryUsageLog(); // Atualiza o log de exibição de uso de memória e CPU.
                viewModel.updateStorageLogs(); // Atualiza os logs de armazenamento
                viewModel.testLatency("https://www.google.com");
                handler.postDelayed(logUpdateRunnable, 5000); // Reexecuta a cada 5 segundos
            };
            handler.post(logUpdateRunnable);

        } catch (Exception e) {
            Log.e("EventLogFragment", "Erro durante a inicialização: " + e.getMessage(), e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pausa o Timer quando o Fragment não está visível
        handler.removeCallbacks(logUpdateRunnable);

        // Remove a flag para restaurar o comportamento padrão
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Restaura o brilho padrão do sistema
        setScreenBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Retoma o Timer quando o Fragment volta a ser visível
        handler.post(logUpdateRunnable);

        // Mantém a tela ligada e ajusta o brilho para um nível mínimo constante
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setScreenBrightness(0.3f); // Ajuste o brilho para 30%
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove o Timer ao destruir a View
        handler.removeCallbacks(logUpdateRunnable);
    }

    /**
     * Adiciona logs iniciais fixos na primeira inicialização.
     */
    private void addInitialLogs() {
        try {
            // Logs fixos
            viewModel.insertLog(new EventLog(
                    System.currentTimeMillis(),
                    "INFO",
                    "Aplicativo iniciado com sucesso."
            ));
            viewModel.insertLog(new EventLog(
                    System.currentTimeMillis(),
                    "DEBUG",
                    "Monitoramento iniciado."
            ));
        } catch (Exception e) {
            Log.e("EventLogFragment", "Erro ao adicionar logs iniciais: " + e.getMessage(), e);
        }
    }

    /**
     * Atualiza o log de uso de memória.
     */
    private void updateMemoryUsageLog() {
        try {
            ActivityManager activityManager = (ActivityManager) requireContext().getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);

            long totalMemory = memoryInfo.totalMem / (1024 * 1024); // Total de memória em MB
            long usedMemory = (memoryInfo.totalMem - memoryInfo.availMem) / (1024 * 1024); // Memória usada em MB
            long usedPercentage = (usedMemory * 100) / totalMemory; // Porcentagem de memória usada

            Log.d("EventLogFragment", String.format("Memória utilizada: %d MB de %d MB (%d%%)", usedMemory, totalMemory, usedPercentage));

            // Atualiza o log de memória no ViewModel
            viewModel.insertLog(new EventLog(
                    System.currentTimeMillis(),
                    "MEMORY_USAGE",
                    String.format("Memória utilizada: %d MB de %d MB (%d%%)", usedMemory, totalMemory, usedPercentage)
            ));

            // Alerta se o uso de memória estiver alto
            if (usedPercentage > 80) {
                viewModel.insertLog(new EventLog(
                        System.currentTimeMillis(),
                        "WARNING",
                        "Uso de memória acima de 80%"
                ));
            }
        } catch (Exception e) {
            Log.e("EventLogFragment", "Erro ao atualizar log de uso de memória: " + e.getMessage(), e);
        }
    }


    /**
     * Atualiza o log de conexão de acordo com o estado atual.
     */
    private void updateConnectionLog() {
        boolean isConnected = isConnectedToInternet();
        String connectionStatus = isConnected ? "Dispositivo Conectado" : "Erro de conexão detectado";

        viewModel.insertLog(new EventLog(
                System.currentTimeMillis(),
                "CONNECTION", // Tipo fixo para identificar logs de conexão
                connectionStatus
        ));
    }

    /**
     * Atualiza o log de consumo de dados Wi-Fi.
     */
    private void updateDataUsageLog() {
        try {
            long currentRxBytes = TrafficStats.getTotalRxBytes();
            long currentTxBytes = TrafficStats.getTotalTxBytes();

            // Calcula o consumo total em MB desde a inicialização
            long totalBytes = (currentRxBytes - initialRxBytes) + (currentTxBytes - initialTxBytes);
            double totalMB = totalBytes / (1024.0 * 1024.0); // Converte para MB

            // Atualiza o log com o consumo de dados
            viewModel.insertLog(new EventLog(
                    System.currentTimeMillis(),
                    "DATA_USAGE", // Tipo fixo para identificar logs de consumo de dados
                    String.format("Consumo de dados Wi-Fi: %.2f MB", totalMB)
            ));
        } catch (Exception e) {
            Log.e("EventLogFragment", "Erro ao atualizar log de consumo de dados: " + e.getMessage(), e);
        }
    }


    /**
     * Verifica se o dispositivo está conectado à internet.
     *
     * @return true se conectado, false caso contrário.
     */
    private boolean isConnectedToInternet() {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected();
            }
        } catch (Exception e) {
            Log.e("EventLogFragment", "Erro ao verificar conexão com a internet: " + e.getMessage(), e);
        }
        return false;
    }

    private void setScreenBrightness(float brightness) {
        Window window = requireActivity().getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.screenBrightness = brightness; // Valor entre 0.0f e 1.0f
        window.setAttributes(layoutParams);
    }
}