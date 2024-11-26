package com.project.tabletobserverjava.ui.theme;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Configuração básica do RecyclerView e ViewModel
            RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new EventLogAdapter(new ArrayList<>());
            recyclerView.setAdapter(adapter);

            EventLogRepository repository = new EventLogRepository(
                    AppDatabase.getInstance(requireContext()).eventLogDao()
            );
            EventLogViewModelFactory factory = new EventLogViewModelFactory(repository);
            viewModel = new ViewModelProvider(this, factory).get(EventLogViewModel.class);

            // Atualiza a RecyclerView em tempo real
            viewModel.getLiveLogs().observe(getViewLifecycleOwner(), logs -> {
                adapter.updateLogs(logs);
            });
            // Timer para adicionar logs em tempo real
            logUpdateRunnable = () -> {
                addInitialLogs(); // Atualiza os logs
                handler.postDelayed(logUpdateRunnable, 5000); // Reexecuta após 5 segundos
            };
            handler.post(logUpdateRunnable); // Inicia o Timer

        } catch (Exception e) {
            Log.e("EventLogFragment", "Erro durante a inicialização: " + e.getMessage(), e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove o Timer quando a view for destruída para evitar vazamentos de memória
        handler.removeCallbacks(logUpdateRunnable);
    }


    /**
     * Adiciona logs iniciais ao banco de dados para simular o comportamento real.
     * Verifica o estado da conexão antes de registrar erros de conexão.
     */
    private void addInitialLogs() {
        try {
            viewModel.insertLog(new EventLog(
                    System.currentTimeMillis(), // timestamp
                    "INFO", // eventType
                    "Aplicativo iniciado com sucesso." // description
            ));

            viewModel.insertLog(new EventLog(
                    System.currentTimeMillis(),
                    "DEBUG",
                    "Monitoramento iniciado."
            ));

            // Log dinâmico de conexão inicial
            updateConnectionLog();

            // Adiciona log de erro de conexão apenas se não houver conexão
            if (!isConnectedToInternet()) {
                viewModel.insertLog(new EventLog(
                        System.currentTimeMillis(),
                        "ERROR",
                        "Erro de conexão detectado."
                ));
            } else{
                viewModel.insertLog(new EventLog(
                        System.currentTimeMillis(),
                        "INFO",
                        "Dispositivo Conectado"));
            }

        } catch (Exception e) {
            Log.e("EventLogFragment", "Erro ao adicionar logs iniciais: " + e.getMessage(), e);
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


    private void updateConnectionLog() {
        boolean isConnected = isConnectedToInternet(); // Verifica o estado da conexão
        String connectionStatus = isConnected ? "Dispositivo Conectado" : "Erro de conexão detectado";

        // Insere ou atualiza o log de conexão
        viewModel.insertLog(new EventLog(
                System.currentTimeMillis(),
                "CONNECTION", // Tipo fixo para identificar o log de conexão
                connectionStatus
        ));
    }
}