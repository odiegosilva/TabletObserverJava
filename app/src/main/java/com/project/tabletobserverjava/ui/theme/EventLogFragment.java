package com.project.tabletobserverjava.ui.theme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tabletobserverjava.R;
import com.project.tabletobserverjava.data.local.AppDatabase;
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

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new EventLogAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Cria o repositório (normalmente obtido via injeção de dependência)
        EventLogRepository repository = new EventLogRepository(
                AppDatabase.getInstance(requireContext()).eventLogDao()
        );

        // Configura o ViewModel usando o Factory
        EventLogViewModelFactory factory = new EventLogViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(EventLogViewModel.class);

        // Observa as mudanças nos dados
        viewModel.getAllLogs().observe(getViewLifecycleOwner(), logs -> {
            adapter.updateLogs(logs); // Atualiza a lista exibida
        });
    }
}