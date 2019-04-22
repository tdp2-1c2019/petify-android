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
import android.widget.SeekBar;
import android.widget.TextView;

import com.app.petify.R;
import com.app.petify.models.Driver;
import com.app.petify.models.Viaje;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
    private Button mPopupButtonCalificar;
    private SeekBar mSeekbar;

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
        mPopupButtonCalificar = findViewById(R.id.popup_button_calificar);

        mSeekbar = findViewById(R.id.seekBarCalificacion);
        mSeekbar.incrementProgressBy(1);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        final String viajeId = intent.getStringExtra("VIAJE_ID");
        final String choferId = intent.getStringExtra("CHOFER_ID");

        mPopupButtonCalificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child("viajes").child(viajeId).child("puntaje_chofer").setValue(mSeekbar.getProgress());
                mDatabase.child("calificaciones").child(choferId).child(viajeId).child("puntaje").setValue(mSeekbar.getProgress());
            }
        });

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
                        mSeekbar.setVisibility(View.GONE);
                        mPopupButtonCalificar.setVisibility(View.GONE);
                        mPopupButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDatabase.child("viajes").child(viaje.id).child("estado").setValue(Viaje.CANCELADO);
                            }
                        });
                        break;
                    case Viaje.CHOFER_ASIGNADO:
                        mPopupText.setText("Asignamos al chofer " + choferName + " para tu viaje");
                        mPopupButton.setVisibility(View.VISIBLE);
                        mPopupButton.setText("Cancelar");
                        mPopupButton.setBackgroundColor(Color.RED);
                        mSeekbar.setVisibility(View.GONE);
                        mPopupButtonCalificar.setVisibility(View.GONE);
                        mPopupButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDatabase.child("viajes").child(viaje.id).child("estado").setValue(Viaje.CANCELADO);
                            }
                        });
                        break;
                    case Viaje.CHOFER_YENDO:
                        mPopupText.setText(choferName + " llegara en " + viaje.eta + " hacia el origen");
                        mSeekbar.setVisibility(View.GONE);
                        mPopupButtonCalificar.setVisibility(View.GONE);
                        mPopupButton.setVisibility(View.VISIBLE);
                        mPopupButton.setText("Cancelar");
                        mPopupButton.setBackgroundColor(Color.RED);
                        break;
                    case Viaje.CHOFER_EN_PUERTA:
                        mPopupText.setText(choferName + " esta en el punto de encuentro");
                        mSeekbar.setVisibility(View.GONE);
                        mPopupButtonCalificar.setVisibility(View.GONE);
                        mPopupButton.setVisibility(View.GONE);
                        break;
                    case Viaje.EN_CURSO:
                        mPopupText.setText(choferName + " llegara a destino en " + viaje.eta + " minutos");
                        mSeekbar.setVisibility(View.GONE);
                        mPopupButtonCalificar.setVisibility(View.GONE);
                        mPopupButton.setVisibility(View.GONE);
                        break;
                    case Viaje.FINALIZADO:
                        mPopupText.setText("Califica a " + choferName);
                        mSeekbar.setVisibility(View.VISIBLE);
                        mPopupButtonCalificar.setVisibility(View.VISIBLE);
                        mPopupButton.setVisibility(View.GONE);
                        break;
                    case Viaje.RECHAZADO:
                        mPopupText.setText("Tu viaje fue rechazado");
                        mPopupButtonCalificar.setVisibility(View.GONE);
                        mSeekbar.setVisibility(View.GONE);
                        mPopupButton.setVisibility(View.GONE);
                        break;
                    case Viaje.CANCELADO:
                        mPopupText.setText("Cancelaste tu viaje");
                        mPopupButtonCalificar.setVisibility(View.GONE);
                        mSeekbar.setVisibility(View.GONE);
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
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        mDatabase.child("drivers").child(choferId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (viaje != null && viaje.estado > 0) {
                    mMap.clear();
                    LatLng ll = new LatLng((double) dataSnapshot.child("lat").getValue(), (double) dataSnapshot.child("lng").getValue());
                    mMap.addMarker(new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }
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
