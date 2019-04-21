package com.app.petify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.app.petify.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CargarViajeActivity extends AppCompatActivity {

    private Button mButton;

    String origin_address;
    Double origin_latitude;
    Double origin_longitude;
    String destination_address;
    Double destination_latitude;
    Double destination_longitude;
    private DatabaseReference mDatabase;
    private Spinner choferes;
    private Map<String, String> mapUsers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargar_viaje);
        choferes = findViewById(R.id.spinnerChofer);
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

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("drivers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    if ((Boolean) d.child("disponible").getValue()) {
                        mapUsers.put((String) d.child("name").getValue(), d.getKey());
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), R.layout.spinner_layout, mapUsers.keySet().toArray(new String[mapUsers.size()]));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                choferes.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
