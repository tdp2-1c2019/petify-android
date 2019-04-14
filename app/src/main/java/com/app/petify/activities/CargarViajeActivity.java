package com.app.petify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.app.petify.R;

public class CargarViajeActivity extends AppCompatActivity {

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargar_viaje);
        button = findViewById(R.id.solicitar_viaje);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), ViajeCursoActivity.class);
                startActivity(i);
            }
        });
    }
}
