package com.app.petify.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.app.petify.R;
import com.app.petify.models.Driver;
import com.app.petify.models.Viaje;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViajeCursoActivity extends FragmentActivity implements OnMapReadyCallback {
    private int LOCATION_PERMISSION = 2;

    private Viaje viaje;
    private String choferName = "";
    private Driver chofer;

    private CardView mPopup;
    private TextView mPopupText;
    private Button mPopupButton;
    private GoogleMap mMap;

    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viaje_curso);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.viajeCursoPasajeroMap);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mPopup = findViewById(R.id.popup);
        mPopupText = findViewById(R.id.popup_text);
        mPopupButton = findViewById(R.id.popup_button);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        String viajeId = intent.getStringExtra("VIAJE_ID");

        mDatabase.child("viajes").child(viajeId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                viaje = dataSnapshot.getValue(Viaje.class);
                if (mPopup.getVisibility() == View.INVISIBLE) mPopup.setVisibility(View.VISIBLE);

                switch (viaje.estado) {
                    case Viaje.CARGADO:
                        mPopupText.setText("Estamos buscando un chofer para tu viaje");
                        mPopupButton.setVisibility(View.VISIBLE);
                        mPopupButton.setText("Cancelar");
                        mPopupButton.setBackgroundColor(Color.RED);
                        break;
                    case Viaje.CHOFER_ASIGNADO:
                        mPopupText.setText("Asignamos al chofer "+choferName+" para tu viaje");
                        mPopupButton.setVisibility(View.VISIBLE);
                        mPopupButton.setText("Cancelar");
                        mPopupButton.setBackgroundColor(Color.RED);
                        break;
                    case Viaje.CHOFER_YENDO:
                        mPopupText.setText(choferName+" llegara en "+viaje.eta+" hacia el origen");
                        mPopupButton.setVisibility(View.VISIBLE);
                        mPopupButton.setText("Cancelar");
                        mPopupButton.setBackgroundColor(Color.RED);
                        break;
                    case Viaje.CHOFER_EN_PUERTA:
                        mPopupText.setText(choferName+" esta en el punto de encuentro");
                        mPopupButton.setVisibility(View.GONE);
                        break;
                    case Viaje.EN_CURSO:
                        mPopupText.setText(choferName+" llegara a destino en "+viaje.eta+" minutos");
                        mPopupButton.setVisibility(View.GONE);
                        break;
                    case Viaje.FINALIZADO:
                        mPopupText.setText("Califica a "+choferName);
                        mPopupButton.setVisibility(View.GONE);
                        break;
                    case Viaje.RECHAZADO:
                        mPopupText.setText("Tu viaje fue rechazado");
                        mPopupButton.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }


                // Actualizamos el driver
                mDatabase.child("drivers").child(viaje.chofer).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        chofer = dataSnapshot.getValue(Driver.class);
                        choferName = chofer.name;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Check for Location permissions
        if (ContextCompat.checkSelfPermission(ViajeCursoActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ViajeCursoActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }

        mMap = googleMap;

        // Aca deberiamos zoomear el viaje, al chofer y al usuario
    }
}
