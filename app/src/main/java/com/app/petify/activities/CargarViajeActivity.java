package com.app.petify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.app.petify.R;
import com.app.petify.models.Viaje;
import com.facebook.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CargarViajeActivity extends AppCompatActivity {

    private Spinner mChofer;
    private Spinner mCantidadMascotas;
    private Switch mViajaAcompanante;
    private Spinner mFormaPago;
    private EditText mObservaciones;
    private Button mSolicitarViaje;

    private String origin_address;
    private Double origin_latitude;
    private Double origin_longitude;
    private String destination_address;
    private Double destination_latitude;
    private Double destination_longitude;
    private String duration;
    private String distance;

    private DatabaseReference mDatabase;
    private Map<String, String> mapUsers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargar_viaje);
        mChofer = findViewById(R.id.chofer);
        mSolicitarViaje = findViewById(R.id.solicitar_viaje);
        mSolicitarViaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Viaje viaje = new Viaje();
                viaje.id = UUID.randomUUID().toString();
                viaje.pasajero = Profile.getCurrentProfile().getId();
                viaje.chofer = mapUsers.get(mChofer.getSelectedItem().toString());
                viaje.estado = Viaje.CHOFER_ASIGNADO; // TODO en chofer se asigna despues
                viaje.origin_address = origin_address;
                viaje.origin_latitude = origin_latitude;
                viaje.origin_longitude = origin_longitude;
                viaje.destination_address = destination_address;
                viaje.destination_latitude = destination_latitude;
                viaje.destination_longitude = destination_longitude;
                viaje.cantidadMascotas = mCantidadMascotas.getSelectedItem().toString();
                viaje.viajaAcompanante = mViajaAcompanante.isChecked();
                viaje.formaPago = mFormaPago.getSelectedItem().toString();
                viaje.observaciones = mObservaciones.getText().toString();
                mDatabase.child("viajes").child(viaje.id).setValue(viaje);

                Intent i = new Intent(getBaseContext(), ViajeCursoActivity.class);
                i.putExtra("VIAJE_ID", viaje.id);
                i.putExtra("CHOFER_ID", viaje.chofer);
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
        duration = intent.getStringExtra("DURATION");
        distance = intent.getStringExtra("DISTANCE");


        TextView mOriginText = findViewById(R.id.origin_text);
        mOriginText.setText("Origen: " + origin_address);
        TextView mDestinationText = findViewById(R.id.destination_text);
        mDestinationText.setText("Destino: " + destination_address);
        TextView mDurationText = findViewById(R.id.duration_text);
        mDurationText.setText("Tiempo estimado: " + duration);
        TextView mDistanceText = findViewById(R.id.distance_text);
        mDistanceText.setText("Distancia estimada: " + distance);


        mCantidadMascotas = findViewById(R.id.cantidad_mascotas);
        mViajaAcompanante = findViewById(R.id.viaja_acompanante);
        mFormaPago = findViewById(R.id.forma_pago);
        mObservaciones = findViewById(R.id.observaciones);

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
                mChofer.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
