package com.mpontus.dictio.ui.shared;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class DictioViewModelFactory implements ViewModelProvider.Factory {

    private final Map<Class<? extends ViewModel>, Provider<ViewModel>> creators;

    @Inject
    public DictioViewModelFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> creators) {
        this.creators = creators;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        Provider<? extends ViewModel> viewModelProvider = creators.get(modelClass);

        if (viewModelProvider == null) {
            throw new IllegalArgumentException("Unknown model class: " + modelClass);
        }

        try {
            return (T) viewModelProvider.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
