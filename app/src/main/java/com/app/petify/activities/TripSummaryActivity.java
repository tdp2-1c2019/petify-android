package com.app.petify.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.app.petify.R;
import com.app.petify.models.Viaje;
import com.facebook.Profile;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.ArrayList;

import static com.app.petify.models.Viaje.RESERVA_CANCELADA;

public class TripSummaryActivity extends FragmentActivity implements OnMapReadyCallback {
    private DatabaseReference mDatabase;

    Viaje viaje;
    TextView from;
    TextView price;
    TextView to;
    TextView fecha;
    private Button[] starButtons = new Button[5];
    private int puntajeStars = 3;
    Button calificar;
    Boolean admin = false;
    GoogleMap mMap;
    Polyline trip;
    LinearLayout llstars;
    Button cancelarReserva;
    TextView reservaCancelada;
    FirebaseFunctions mFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_summary);
        Intent i = getIntent();
        viaje = (Viaje) i.getSerializableExtra("viaje");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.sumMap);
        mapFragment.getMapAsync(this);
        cancelarReserva = findViewById(R.id.cancelar_reserva);
        mFunctions = FirebaseFunctions.getInstance();
        reservaCancelada = findViewById(R.id.reserva_cancelada);
        from = findViewById(R.id.summary_from);
        from.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dot, 0, 0, 0);
        from.setText(viaje.origin_address);
        to = findViewById(R.id.summary_to);
        to.setCompoundDrawablesWithIntrinsicBounds(R.drawable.place, 0, 0, 0);
        to.setText(viaje.destination_address);
        fecha = findViewById(R.id.summary_fecha);
        fecha.setCompoundDrawablesWithIntrinsicBounds(R.drawable.date, 0, 0, 0);
        fecha.setText(viaje.fecha);
        price = findViewById(R.id.summary_price);
        price.setCompoundDrawablesWithIntrinsicBounds(R.drawable.money, 0, 0, 0);
        price.setText(String.valueOf(viaje.precio));
        calificar = findViewById(R.id.sumCalificar);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        starButtons[0] = findViewById(R.id.sumStar1);
        starButtons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marcarEstrellas(1, admin);
            }
        });
        starButtons[1] = findViewById(R.id.sumStar2);
        starButtons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marcarEstrellas(2, admin);
            }
        });
        starButtons[2] = findViewById(R.id.sumStar3);
        starButtons[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marcarEstrellas(3, admin);
            }
        });
        starButtons[3] = findViewById(R.id.sumStar4);
        starButtons[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marcarEstrellas(4, admin);
            }
        });
        starButtons[4] = findViewById(R.id.sumStar5);
        starButtons[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marcarEstrellas(5, admin);
            }
        });
        llstars = findViewById(R.id.sumllstars);
        if (!viaje.reserva)
            initStars();
        else {
            llstars.setVisibility(View.GONE);
            if (viaje.pasajero.equals(Profile.getCurrentProfile().getId())) {
                cancelarReserva.setVisibility(View.VISIBLE);
                cancelarReserva.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelarReserva.setVisibility(View.GONE);
                        reservaCancelada.setVisibility(View.VISIBLE);
//                        Map<String, Object> data = new HashMap<>();
//                        data.put("viajeid", viaje.id);
//                        mFunctions.getHttpsCallable("cancelarReserva").call(data).continueWith(new Continuation<HttpsCallableResult, Object>() {
//                            @Override
//                            public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
//                                return null;
//                            }
//                        });
                        mDatabase.child("viajes").child(viaje.id).child("estado").setValue(Viaje.RESERVA_CANCELADA);
                    }
                });
            }
        }
    }

    private void marcarEstrellas(int n, boolean admin) {
        if (admin) {
            puntajeStars = n;
            for (int i = 0; i < n; i++)
                starButtons[i].setBackgroundDrawable(getResources().getDrawable(R.drawable.stari));
            for (int i = n; i < 5; i++)
                starButtons[i].setBackgroundDrawable(getResources().getDrawable(R.drawable.stardisabled));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ContextCompat.checkSelfPermission(TripSummaryActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(TripSummaryActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        mMap = googleMap;
        mMap.clear();
        try {
            LatLng current = new LatLng((viaje.origin_latitude + viaje.destination_latitude) / 2, (viaje.origin_longitude + viaje.destination_longitude) / 2);
            dibujarCamino(new LatLng(viaje.origin_latitude, viaje.origin_longitude), new LatLng(viaje.destination_latitude, viaje.destination_longitude));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(current)
                    .zoom(13)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dibujarCamino(final LatLng origin, final LatLng destination) {
        GoogleDirection.withServerKey(getString(R.string.google_maps_key))
                .from(origin).to(destination)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if (direction.isOK()) {
                            Leg leg = direction.getRouteList().get(0).getLegList().get(0);
                            ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(TripSummaryActivity.this, directionPositionList, 5, Color.BLUE);
                            if (trip != null) trip.remove();
                            trip = mMap.addPolyline(polylineOptions);
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

    private void initStars() {
        if (viaje.chofer.equals(Profile.getCurrentProfile().getId())) {
            if (viaje.puntaje_pasajero != 0)
                marcarEstrellas(viaje.puntaje_pasajero, true);
            else {
                admin = true;
                marcarEstrellas(3, true);
                calificar.setVisibility(View.VISIBLE);
                calificar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDatabase.child("viajes").child(viaje.id).child("puntaje_pasajero").setValue(puntajeStars);
                        mDatabase.child("calificaciones").child(viaje.pasajero).child(viaje.id).child("puntaje").setValue(puntajeStars);
                        calificar.setVisibility(View.GONE);
                    }
                });
            }
        } else {
            if (viaje.puntaje_chofer != 0)
                marcarEstrellas(viaje.puntaje_chofer, true);
            else {
                admin = true;
                marcarEstrellas(3, true);
                calificar.setVisibility(View.VISIBLE);
                calificar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDatabase.child("viajes").child(viaje.id).child("puntaje_chofer").setValue(puntajeStars);
                        mDatabase.child("calificaciones").child(viaje.chofer).child(viaje.id).child("puntaje").setValue(puntajeStars);
                        calificar.setVisibility(View.GONE);
                    }
                });
            }
        }
    }
}
