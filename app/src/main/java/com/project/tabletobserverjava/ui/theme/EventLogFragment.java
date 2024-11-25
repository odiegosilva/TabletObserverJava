package com.project.tabletobserverjava.ui.theme;

import android.os.Bundle;
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
            // Configura RecyclerView
            RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

            // Configura o adapter
            adapter = new EventLogAdapter(new ArrayList<>());
            recyclerView.setAdapter(adapter);

            // Configura o repositório e o ViewModel
            EventLogRepository repository = new EventLogRepository(
                    AppDatabase.getInstance(requireContext()).eventLogDao()
            );
            EventLogViewModelFactory factory = new EventLogViewModelFactory(repository);
            viewModel = new ViewModelProvider(this, factory).get(EventLogViewModel.class);

            // Insere logs iniciais
            addInitialLogs();

            // Observa mudanças nos logs
            viewModel.getAllLogs().observe(getViewLifecycleOwner(), logs -> {
                adapter.updateLogs(logs); // Atualiza a RecyclerView
            });

            // Configura o botão para adicionar logs
            Button addLogButton = view.findViewById(R.id.btn_add_log);
            addLogButton.setOnClickListener(v -> {
                try {
                    viewModel.insertLog(new EventLog(
                            System.currentTimeMillis(), // timestamp
                            "DEBUG", // eventType
                            "Log adicionado manualmente." // description
                    ));
                } catch (Exception e) {
                    Log.e("EventLogFragment", "Erro ao adicionar log manualmente: " + e.getMessage(), e);
                }
            });

        } catch (Exception e) {
            Log.e("EventLogFragment", "Erro durante a inicialização: " + e.getMessage(), e);
        }
    }



    /**
     * Adiciona logs iniciais ao banco de dados para simular o comportamento real.
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
                    "WARNING",
                    "Uso de memória alto."
            ));
            viewModel.insertLog(new EventLog(
                    System.currentTimeMillis(),
                    "ERROR",
                    "Erro de conexão detectado."
            ));
        } catch (Exception e) {
            Log.e("EventLogFragment", "Erro ao adicionar logs iniciais: " + e.getMessage(), e);
        }
    }
}