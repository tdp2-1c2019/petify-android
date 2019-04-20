package com.app.petify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.app.petify.R;

public class CargarViajeActivity extends AppCompatActivity {

    private Button mButton;

    String origin_address;
    Double origin_latitude;
    Double origin_longitude;
    String destination_address;
    Double destination_latitude;
    Double destination_longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargar_viaje);
        mButton = findViewById(R.id.solicitar_viaje);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), ViajeCursoActivity.class);
                startActivity(i);
            }
        });

        Intent intent = getIntent();
        origin_address = intent.getStringExtra("ORIGIN_ADDRESS");
        origin_latitude = intent.getDoubleExtra("ORIGIN_LAT", 0);
        origin_longitude = intent.getDoubleExtra("ORIGIN_LNG", 0);
        destination_address = intent.getStringExtra("DESTINATION_ADDRESS");
        destination_latitude = intent.getDoubleExtra("DESTINATION_LAT", 0);
        destination_longitude = intent.getDoubleExtra("DESTINATION_LNG", 0);

        TextView mOriginText = findViewById(R.id.origin_text);
        mOriginText.setText("Origen: " + origin_address);
        TextView mDestinationText = findViewById(R.id.destination_text);
        mDestinationText.setText("Destino: " + destination_address);
    }
}
