package com.project.tabletobserverjava;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.project.tabletobserverjava.ui.theme.EventLogFragment;

/**
 * MainActivity é a atividade principal que hospeda o EventLogFragment.
 * Gerencia o ciclo de vida principal da aplicação.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Adiciona o fragment apenas na primeira criação
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new EventLogFragment())
                    .commit();
        }
    }
}