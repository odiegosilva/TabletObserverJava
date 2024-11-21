package com.project.tabletobserverjava.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Utilitário para testes com LiveData.
 * Permite obter os valores do LiveData sincronamente em testes unitários.
 */
public class LiveDataTestUtil {

    /**
     * Obtém o valor do LiveData.
     *
     * @param liveData Instância do LiveData a ser observada.
     * @param <T>      Tipo do dado retornado pelo LiveData.
     * @return Valor atual do LiveData.
     * @throws InterruptedException Caso a espera exceda o limite de tempo.
     */
    public static <T> T getValue(LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        CountDownLatch latch = new CountDownLatch(1);

        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T t) {
                data[0] = t;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };

        liveData.observeForever(observer);

        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw new InterruptedException("LiveData value was never set.");
        }

        return (T) data[0];
    }
}
