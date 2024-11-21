package com.project.tabletobserverjava;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Criação de um TextView para exibir uma mensagem
        TextView textView = new TextView(this);
        textView.setText("Olá, mundo! Bem-vindo ao meu app.");
        textView.setTextSize(20);
        textView.setPadding(16, 16, 16, 16);

        // Configuração do layout principal da Activity
        setContentView(textView);
    }
}
