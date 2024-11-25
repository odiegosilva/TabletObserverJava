package com.project.tabletobserverjava.viewModel;


import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.project.tabletobserverjava.data.repository.EventLogRepository;

/**
 * Factory para criar instâncias de EventLogViewModel com as dependências necessárias.
 */
public class EventLogViewModelFactory implements ViewModelProvider.Factory {

    private final EventLogRepository repository;

    /**
     * Construtor da Factory.
     *
     * @param repository Instância do repositório a ser injetada no ViewModel.
     */
    public EventLogViewModelFactory(EventLogRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EventLogViewModel.class)) {
            return (T) new EventLogViewModel(repository);
        }
        throw new IllegalArgumentException("Classe desconhecida: " + modelClass.getName());
    }
}
