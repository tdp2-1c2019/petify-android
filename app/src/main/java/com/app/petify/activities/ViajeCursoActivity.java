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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.app.petify.R;
import com.app.petify.models.Driver;
import com.app.petify.models.Viaje;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.app.petify.models.Viaje.CHOFER_YENDO;

public class ViajeCursoActivity extends FragmentActivity implements OnMapReadyCallback {
    private int LOCATION_PERMISSION = 2;

    private Viaje viaje;
    private String choferName = "";

    private CardView mPopup;
    private TextView mPopupText;
    private Button mPopupButton;
    private GoogleMap mMap;

    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference mDatabase;
    private Button mPopupButtonCalificar;
    private Button[] starButtons = new Button[5];
    private int puntajeStars = 3;
    private RelativeLayout pStarsLayout;
    private FirebaseFunctions mFunctions;
    private LinearLayout mLayContinuarBuscando;
    private Button mSeguirBuscando;
    private Button mCancelarBusqueda;
    private MarkerOptions p1;
    private Marker p1m;
    private MarkerOptions p2;
    private Marker p2m;
    private MarkerOptions p3;
    private Marker p3m;
    private Polyline tripYendo;
    private Polyline tripViaje;
    private boolean first = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viaje_curso);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.viajeCursoPasajeroMap);
        mapFragment.getMapAsync(this);
        mFunctions = FirebaseFunctions.getInstance();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mPopup = findViewById(R.id.popup);
        mPopupText = findViewById(R.id.popup_text);
        mPopupButton = findViewById(R.id.popup_button);
        mPopupButtonCalificar = findViewById(R.id.popup_button_calificar);
        pStarsLayout = findViewById(R.id.pStarLayout);
        mLayContinuarBuscando = findViewById(R.id.layoutContinuarBuscando);
        mSeguirBuscando = findViewById(R.id.buttonAceptarSeguirBuscando);
        mCancelarBusqueda = findViewById(R.id.buttonCancelarSeguirBuscando);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        final String viajeId = intent.getStringExtra("VIAJE_ID");

        mPopupButtonCalificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child("viajes").child(viajeId).child("puntaje_chofer").setValue(puntajeStars);
//                mDatabase.child("calificaciones").child(choferId).child(viajeId).child("puntaje").setValue(puntajeStars);
                Intent intent = new Intent(getBaseContext(), MapsActivity.class);
                startActivity(intent);
            }
        });

        mDatabase.child("viajes").child(viajeId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                viaje = dataSnapshot.getValue(Viaje.class);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                final LatLng o = new LatLng(viaje.origin_latitude, viaje.origin_longitude);
                final LatLng d = new LatLng(viaje.destination_latitude, viaje.destination_longitude);
                builder.include(o);
                builder.include(d);
                final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(), 300);
                mMap.animateCamera(cu);
                p1 = new MarkerOptions().position(o).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                p2 = new MarkerOptions().position(d).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                p1m = mMap.addMarker(p1);
                p2m = mMap.addMarker(p2);
                mPopup.setVisibility(View.VISIBLE);

                switch (viaje.estado) {
                    case Viaje.CARGADO:
                        mPopupText.setText("Estamos buscando un chofer para tu viaje");
                        mPopupButton.setVisibility(View.VISIBLE);
                        mPopupButton.setText("Cancelar");
                        mPopupButton.setBackgroundColor(Color.RED);
                        pStarsLayout.setVisibility(View.GONE);
                        mPopupButtonCalificar.setVisibility(View.GONE);
                        mLayContinuarBuscando.setVisibility(View.GONE);
                        mPopupButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Map<String, String> data = new HashMap<>();
                                data.put("viajeid", viaje.id);
                                mFunctions.getHttpsCallable("cancelarViaje").call(data).continueWith(new Continuation<HttpsCallableResult, Object>() {
                                    @Override
                                    public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                        Intent intent = new Intent(getBaseContext(), MapsActivity.class);
                                        startActivity(intent);
                                        return null;
                                    }
                                });
                            }
                        });
                        dibujarCamino(o, d, 2);
                        break;
                    case CHOFER_YENDO:
                        mDatabase.child("drivers").child(viaje.chofer).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Driver dr = dataSnapshot.getValue(Driver.class);
                                LatLng driver = new LatLng(dr.lat, dr.lng);
                                if (p3m != null) p3m.remove();
                                p3 = new MarkerOptions().position(driver).icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
                                p3m = mMap.addMarker(p3);
                                if (first) {
                                    dibujarCamino(driver, o, 1);
                                    first = false;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        mDatabase.child("drivers").child(viaje.chofer).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                choferName = dataSnapshot.getValue(String.class);
                                mPopupText.setText(choferName + " llegara en " + viaje.eta + " hacia el origen");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        pStarsLayout.setVisibility(View.GONE);
                        mPopupButtonCalificar.setVisibility(View.GONE);
                        mPopupButton.setVisibility(View.VISIBLE);
                        mPopupButton.setText("Cancelar");
                        mPopupButton.setBackgroundColor(Color.RED);
                        break;
                    case Viaje.CHOFER_EN_PUERTA:
                        mPopupText.setText(choferName + " esta en el punto de encuentro");
                        pStarsLayout.setVisibility(View.GONE);
                        mPopupButtonCalificar.setVisibility(View.GONE);
                        mPopupButton.setVisibility(View.GONE);
                        limpiarMapa(1);
                        p1m.remove();
                        break;
                    case Viaje.EN_CURSO:
                        mPopupText.setText(choferName + " llegara a destino en " + viaje.eta + " minutos");
                        pStarsLayout.setVisibility(View.GONE);
                        mPopupButtonCalificar.setVisibility(View.GONE);
                        mPopupButton.setVisibility(View.GONE);
                        break;
                    case Viaje.FINALIZADO:
                        mPopupText.setText("Califica a " + choferName);
                        pStarsLayout.setVisibility(View.VISIBLE);
                        mPopupButtonCalificar.setVisibility(View.VISIBLE);
                        mPopupButton.setVisibility(View.GONE);
                        break;
                    case Viaje.RECHAZADO:
                        Intent intent = new Intent(getBaseContext(), MapsActivity.class);
                        startActivity(intent);
                        break;
                    case Viaje.CANCELADO_GRUPO:
                        mPopupText.setText("No se encontraron choferes");
                        mPopupButton.setVisibility(View.GONE);
                        mLayContinuarBuscando.setVisibility(View.VISIBLE);
                    default:
                        break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        starButtons[0] = findViewById(R.id.pStar1);
        starButtons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marcarEstrellas(1);
            }
        });
        starButtons[1] = findViewById(R.id.pStar2);
        starButtons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marcarEstrellas(2);
            }
        });
        starButtons[2] = findViewById(R.id.pStar3);
        starButtons[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marcarEstrellas(3);
            }
        });
        starButtons[3] = findViewById(R.id.pStar4);
        starButtons[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marcarEstrellas(4);
            }
        });
        starButtons[4] = findViewById(R.id.pStar5);
        starButtons[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marcarEstrellas(5);
            }
        });
        marcarEstrellas(3);

        mSeguirBuscando.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> data = new HashMap<>();
                data.put("viajeid", viaje.id);
                mFunctions.getHttpsCallable("seguirBuscando").call(data).continueWith(new Continuation<HttpsCallableResult, Object>() {
                    @Override
                    public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        return null;
                    }
                });
            }
        });
        mCancelarBusqueda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), MapsActivity.class);
                startActivity(intent);
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

    private void marcarEstrellas(int n) {
        puntajeStars = n;
        for (int i = 0; i < n; i++)
            starButtons[i].setBackgroundDrawable(getResources().getDrawable(R.drawable.stari));
        for (int i = n; i < 5; i++)
            starButtons[i].setBackgroundDrawable(getResources().getDrawable(R.drawable.stardisabled));
    }

    private void dibujarCamino(final LatLng origin, final LatLng destination, final int ida) {
        GoogleDirection.withServerKey(getString(R.string.google_maps_key))
                .from(origin).to(destination)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if (direction.isOK()) {
                            Leg leg = direction.getRouteList().get(0).getLegList().get(0);
                            ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(ViajeCursoActivity.this, directionPositionList, 5, Color.BLUE);
                            // Dibujamos el recorrido
                            if (ida == 1) {
                                if (tripYendo != null) tripYendo.remove();
                                tripYendo = mMap.addPolyline(polylineOptions);
                            } else {
                                if (tripViaje != null) tripViaje.remove();
                                tripViaje = mMap.addPolyline(polylineOptions);
                            }

                            // Zoomeamos el recorrido
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(origin);
                            builder.include(destination);
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                    }
                });
    }

    private void limpiarMapa(final int ida) {
        if (tripYendo != null && ida == 1) {
            tripYendo.remove();
        }
        if (tripViaje != null && ida == 2) {
            tripViaje.remove();
        }
    }
}
