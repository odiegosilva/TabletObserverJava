package com.project.tabletobserverjava.viewModel;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.project.tabletobserverjava.data.repository.EventLogRepository;
import com.project.tabletobserverjava.ui.theme.EventLogViewModel;

/**
 * Factory para criar instâncias de EventLogViewModel com as dependências necessárias.
 */
public class EventLogViewModelFactory implements ViewModelProvider.Factory {

    private final EventLogRepository repository;
    private final Context context;


    /**
     * Construtor da Factory.
     *
     * @param repository Instância do repositório a ser injetada no ViewModel.
     */
    public EventLogViewModelFactory(EventLogRepository repository, Context context) {
        this.repository = repository;
        this.context = context;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EventLogViewModel.class)) {
            return (T) new EventLogViewModel(repository, context);
        }
        throw new IllegalArgumentException("Classe desconhecida: " + modelClass.getName());
    }
}
