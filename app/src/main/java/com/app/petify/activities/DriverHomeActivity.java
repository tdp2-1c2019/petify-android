package com.app.petify.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.app.petify.R;
import com.app.petify.models.Client;
import com.app.petify.models.Driver;
import com.app.petify.models.Viaje;
import com.app.petify.utils.LocalStorage;
import com.facebook.Profile;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DriverHomeActivity extends AppCompatActivity implements OnMapReadyCallback {
    private int LOCATION_PERMISSION = 2;

    private GoogleMap mMap;
    private CardView mDisponibleCard;
    private Switch mDisponible;
    private CardView mPopup;
    private TextView mPopupText;
    private TextView mPopupOrigen;
    private TextView mPopupDestino;
    private Button mPopupButtonAceptar;
    private Button mPopupButtonCancelar;
    private Button mPopupButtonAvanzar;
    private Button[] starButtons = new Button[5];
    private int puntajeStars = 3;
    private RelativeLayout chStarsLayout;
    private Button mCalificar;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private String idTrip;
    private Viaje viaje;

    private DatabaseReference mDatabase;
    private Polyline tripYendo;
    private Polyline tripViaje;
    private Client pasajero;
    private String pasajeroName;
    private String fbid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_driver);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Driver driver = LocalStorage.getDriver();

        mDisponibleCard = findViewById(R.id.disponible_card);
        mDisponible = findViewById(R.id.disponible);
        mPopup = findViewById(R.id.popup);
        mPopupText = findViewById(R.id.popup_text);
        mPopupOrigen = findViewById(R.id.popup_origen);
        mPopupDestino = findViewById(R.id.popup_destino);
        mPopupButtonAceptar = findViewById(R.id.aceptar_viaje);
        mPopupButtonCancelar = findViewById(R.id.rechazar_viaje);
        mPopupButtonAvanzar = findViewById(R.id.proxima_etapa_viaje);
        chStarsLayout = findViewById(R.id.chStarLayout);
        mCalificar = findViewById(R.id.ch_popup_button_calificar);
        mCalificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child("viajes").child(viaje.id).child("puntaje_pasajero").setValue(puntajeStars);
                mDatabase.child("calificaciones").child(viaje.pasajero).child(viaje.id).child("puntaje").setValue(puntajeStars);
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Check for Location permissions
        if (ContextCompat.checkSelfPermission(DriverHomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverHomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }

        fbid = Profile.getCurrentProfile().getId();

        // Obtenemos la location del chofer y la actualizamos en Firebase
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                        try {
                            mDatabase.child("drivers").child(Profile.getCurrentProfile().getId()).child("lat").setValue(wayLatitude);
                            mDatabase.child("drivers").child(Profile.getCurrentProfile().getId()).child("lng").setValue(wayLongitude);
                        } catch (Exception e) {
                            // Sacamos el callback porque se cerro la sesion y me marco como no disponible
                            fusedLocationClient.removeLocationUpdates(locationCallback);
                            mDatabase.child("drivers").child(fbid).child("disponible").setValue(false);
                        }
                        if (viaje != null) {
                            if (viaje.estado == Viaje.CHOFER_ASIGNADO ||
                                    viaje.estado == Viaje.CHOFER_YENDO) {
                                dibujarCamino(new LatLng(wayLatitude, wayLongitude), new LatLng(viaje.origin_latitude, viaje.origin_longitude), 1);
                            } else {
                                limpiarMapa(1);
                            }
                        }
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

        mDatabase.child("drivers").child(fbid).child("disponible").setValue(mDisponible.isChecked());
        mDisponible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDatabase.child("drivers").child(fbid).child("disponible").setValue(mDisponible.isChecked());
            }
        });

        mDatabase.child("viajes").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Viaje newViaje = dataSnapshot.getValue(Viaje.class);
                procesarViaje(newViaje);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        starButtons[0] = findViewById(R.id.chStar1);
        starButtons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marcarEstrellas(1);
            }
        });
        starButtons[1] = findViewById(R.id.chStar2);
        starButtons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marcarEstrellas(2);
            }
        });
        starButtons[2] = findViewById(R.id.chStar3);
        starButtons[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marcarEstrellas(3);
            }
        });
        starButtons[3] = findViewById(R.id.chStar4);
        starButtons[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marcarEstrellas(4);
            }
        });
        starButtons[4] = findViewById(R.id.chStar5);
        starButtons[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marcarEstrellas(5);
            }
        });
        marcarEstrellas(3);
    }

    private void procesarViaje(Viaje newViaje) {
        if (newViaje.chofer.equals(fbid)) {
            viaje = newViaje;

            if (viaje.estado == Viaje.CHOFER_ASIGNADO ||
                    viaje.estado == Viaje.CHOFER_YENDO ||
                    viaje.estado == Viaje.CHOFER_EN_PUERTA ||
                    viaje.estado == Viaje.EN_CURSO) {
                LatLng origenLatLng = new LatLng(viaje.origin_latitude, viaje.origin_longitude);
                mMap.addMarker(new MarkerOptions().position(origenLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                LatLng destinoLatLng = new LatLng(viaje.destination_latitude, viaje.destination_longitude);
                mMap.addMarker(new MarkerOptions().position(destinoLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                dibujarCamino(origenLatLng, destinoLatLng, 2);
            } else {
                limpiarMapa(2);
            }

            mDatabase.child("viajes").child(viaje.id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    viaje = dataSnapshot.getValue(Viaje.class);
                    // Actualizamos el pasajero
                    mDatabase.child("pasajeros").child(viaje.pasajero).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            pasajero = dataSnapshot.getValue(Client.class);
                            pasajeroName = pasajero.name;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                    switch (viaje.estado) {
                        case Viaje.CHOFER_YENDO:
                            mPopupText.setVisibility(View.GONE);
                            mPopupOrigen.setText("Origen: " + viaje.origin_address);
                            mPopupOrigen.setBackgroundColor(Color.CYAN);
                            mPopupDestino.setText("Destino: " + viaje.destination_address);
                            mPopupDestino.setBackgroundColor(Color.TRANSPARENT);
                            mPopupButtonAceptar.setVisibility(View.GONE);
                            mPopupButtonCancelar.setVisibility(View.GONE);
                            mPopupButtonAvanzar.setText("Llegue");
                            mPopupButtonAvanzar.setVisibility(View.VISIBLE);
                            mPopupButtonAvanzar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mDatabase.child("viajes").child(viaje.id).child("estado").setValue(Viaje.CHOFER_EN_PUERTA);
                                }
                            });
                            mPopup.setVisibility(View.VISIBLE);
                            mDisponibleCard.setVisibility(View.INVISIBLE);
                            break;
                        case Viaje.CHOFER_EN_PUERTA:
                            limpiarMapa(1);
                            mPopupText.setVisibility(View.GONE);
                            mPopupOrigen.setText("Origen: " + viaje.origin_address);
                            mPopupOrigen.setBackgroundColor(Color.CYAN);
                            mPopupDestino.setText("Destino: " + viaje.destination_address);
                            mPopupDestino.setBackgroundColor(Color.TRANSPARENT);
                            mPopupButtonAceptar.setVisibility(View.GONE);
                            mPopupButtonCancelar.setVisibility(View.GONE);
                            mPopupButtonAvanzar.setText("Arrancamos");
                            mPopupButtonAvanzar.setVisibility(View.VISIBLE);
                            mPopupButtonAvanzar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mDatabase.child("viajes").child(viaje.id).child("estado").setValue(Viaje.EN_CURSO);
                                }
                            });
                            mPopup.setVisibility(View.VISIBLE);
                            mDisponibleCard.setVisibility(View.INVISIBLE);
                            break;
                        case Viaje.EN_CURSO:
                            limpiarMapa(1);
                            mPopupText.setVisibility(View.GONE);
                            mPopupOrigen.setText("Origen: " + viaje.origin_address);
                            mPopupOrigen.setBackgroundColor(Color.TRANSPARENT);
                            mPopupDestino.setText("Destino: " + viaje.destination_address);
                            mPopupDestino.setBackgroundColor(Color.CYAN);
                            mPopupButtonAceptar.setVisibility(View.GONE);
                            mPopupButtonCancelar.setVisibility(View.GONE);
                            mPopupButtonAvanzar.setVisibility(View.VISIBLE);
                            mPopupButtonAvanzar.setText("Termine");
                            mPopupButtonAvanzar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mDatabase.child("viajes").child(viaje.id).child("estado").setValue(Viaje.FINALIZADO);
                                }
                            });
                            mPopup.setVisibility(View.VISIBLE);
                            mDisponibleCard.setVisibility(View.INVISIBLE);
                            break;
                        case Viaje.FINALIZADO:
                            limpiarMapa(1);
                            limpiarMapa(2);
                            mDisponibleCard.setVisibility(View.INVISIBLE);
                            mPopup.setVisibility(View.VISIBLE);
                            mPopupOrigen.setVisibility(View.GONE);
                            mPopupDestino.setVisibility(View.GONE);
                            mPopupText.setVisibility(View.VISIBLE);
                            mPopupText.setText("Califica a " + pasajeroName);
                            mPopupButtonAceptar.setVisibility(View.GONE);
                            mPopupButtonAvanzar.setVisibility(View.GONE);
                            mPopupButtonCancelar.setVisibility(View.GONE);
                            chStarsLayout.setVisibility(View.VISIBLE);
                            break;
                        case Viaje.RECHAZADO:
                        case Viaje.CANCELADO:
                            limpiarMapa(1);
                            limpiarMapa(2);
                            mDisponibleCard.setVisibility(View.VISIBLE);
                            mPopup.setVisibility(View.INVISIBLE);
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            mPopupText.setVisibility(View.GONE);
            mPopupOrigen.setText("Origen: " + viaje.origin_address);
            mPopupOrigen.setBackgroundColor(Color.TRANSPARENT);
            mPopupDestino.setText("Destino: " + viaje.destination_address);
            mPopupDestino.setBackgroundColor(Color.TRANSPARENT);
            mPopupButtonAceptar.setVisibility(View.VISIBLE);
            mPopupButtonAceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDatabase.child("viajes").child(viaje.id).child("estado").setValue(Viaje.CHOFER_YENDO);
                }
            });
            mPopupButtonCancelar.setVisibility(View.VISIBLE);
            mPopupButtonCancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDatabase.child("viajes").child(viaje.id).child("estado").setValue(Viaje.RECHAZADO);
                }
            });
            mPopupButtonAvanzar.setVisibility(View.GONE);
            mPopup.setVisibility(View.VISIBLE);
            mDisponibleCard.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Check for Location permissions
        if (ContextCompat.checkSelfPermission(DriverHomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverHomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }

        mMap = googleMap;
        mMap.clear();

        try {
            locateAndZoomUser();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locateAndZoomUser();
        }
    }

    private void locateAndZoomUser() {
        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(current)
                            .zoom(17)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });
    }

    private void limpiarMapa(final int ida) {
        if (tripYendo != null && ida == 1) tripYendo.remove();
        if (tripViaje != null && ida == 2) {
            mMap.clear();
            tripViaje.remove();
        }
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
                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(DriverHomeActivity.this, directionPositionList, 5, Color.BLUE);
                            // Dibujamos el recorrido
                            if (ida == 1) {
                                if (tripYendo != null) tripYendo.remove();
                                tripYendo = mMap.addPolyline(polylineOptions);
                            } else {
                                if (tripViaje != null) tripViaje.remove();
                                tripViaje = mMap.addPolyline(polylineOptions);
                            }
                            // Informamos ETA
                            if (viaje != null &&
                                    ((ida == 1 && (viaje.estado == Viaje.CHOFER_ASIGNADO || viaje.estado == Viaje.CHOFER_YENDO)) ||
                                            (ida == 2 && viaje.estado == Viaje.EN_CURSO)))
                                mDatabase.child("viajes").child(viaje.id).child("eta").setValue(leg.getDuration().getText());
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

    private void marcarEstrellas(int n) {
        puntajeStars = n;
        for (int i = 0; i < n; i++)
            starButtons[i].setBackgroundDrawable(getResources().getDrawable(R.drawable.stari));
        for (int i = n; i < 5; i++)
            starButtons[i].setBackgroundDrawable(getResources().getDrawable(R.drawable.stardisabled));
    }
}
