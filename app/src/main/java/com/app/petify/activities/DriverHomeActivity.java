package com.app.petify.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
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
import com.app.petify.models.Viaje;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.app.petify.models.Viaje.CHOFER_EN_PUERTA;
import static com.app.petify.models.Viaje.CHOFER_YENDO;
import static com.app.petify.models.Viaje.EN_CURSO;
import static com.app.petify.models.Viaje.FINALIZADO;

public class DriverHomeActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
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
    private TextView mPopupTitle;
    private TextView mPopupViajaA;
    private TextView mPopupCantM;
    private TextView mPopupObs;
    private TextView mPopupPrecio;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private Viaje viaje;

    private DatabaseReference mDatabase;
    private Polyline tripYendo;
    private Polyline tripViaje;
    private Client pasajero;
    private String pasajeroName;
    private String fbid;
    private FirebaseFunctions mFunctions;
    private String eta1;
    private String eta2;

    private NavigationView navView;
    private TextView drawerTitle;
    private MenuItem itemInicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_driver);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFunctions = FirebaseFunctions.getInstance();
        navView = findViewById(R.id.driver_nav_view);
        navView.setNavigationItemSelectedListener(this);
        drawerTitle = navView.getHeaderView(0).findViewById(R.id.nav_header_title);
        itemInicio = navView.getMenu().getItem(0);
        mDisponibleCard = findViewById(R.id.disponible_card);
        mDisponible = findViewById(R.id.disponible);
        mPopup = findViewById(R.id.popup);
        mPopupText = findViewById(R.id.popup_text);
        mPopupTitle = findViewById(R.id.popup_title);
        mPopupOrigen = findViewById(R.id.popup_origen);
        mPopupDestino = findViewById(R.id.popup_destino);
        mPopupButtonAceptar = findViewById(R.id.aceptar_viaje);
        mPopupButtonCancelar = findViewById(R.id.rechazar_viaje);
        mPopupButtonAvanzar = findViewById(R.id.proxima_etapa_viaje);
        mPopupViajaA = findViewById(R.id.popup_viajaA);
        mPopupCantM = findViewById(R.id.popup_cantM);
        mPopupPrecio = findViewById(R.id.popup_precio);
        mPopupObs = findViewById(R.id.popup_observaciones);
        chStarsLayout = findViewById(R.id.chStarLayout);
        mCalificar = findViewById(R.id.ch_popup_button_calificar);
        drawerTitle.setText("Hola " + Profile.getCurrentProfile().getFirstName() + "!");
        SpannableString s = new SpannableString(itemInicio.getTitle());
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), R.color.com_facebook_blue)), 0, s.length(), 0);
        itemInicio.setTitle(s);
        mCalificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child("viajes").child(viaje.id).child("puntaje_pasajero").setValue(puntajeStars);
                mDatabase.child("calificaciones").child(viaje.pasajero).child(viaje.id).child("puntaje").setValue(puntajeStars);
                mCalificar.setVisibility(View.GONE);
                chStarsLayout.setVisibility(View.GONE);
                mDisponibleCard.setVisibility(View.VISIBLE);
                viaje = null;
                mDatabase.child("drivers").child(fbid).child("viajeAsignado").removeValue();
                procesarViaje();
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
                            if (viaje.estado == Viaje.CARGADO) {
                                dibujarCamino(new LatLng(wayLatitude, wayLongitude), new LatLng(viaje.origin_latitude, viaje.origin_longitude), 1);
                                dibujarCamino(new LatLng(viaje.origin_latitude, viaje.origin_longitude), new LatLng(viaje.destination_latitude, viaje.destination_longitude), 2);
                            } else if (viaje.estado == Viaje.EN_CURSO || viaje.estado == Viaje.CHOFER_EN_PUERTA) {
                                dibujarCamino(new LatLng(viaje.origin_latitude, viaje.origin_longitude), new LatLng(viaje.destination_latitude, viaje.destination_longitude), 2);
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

        final ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    viaje = dataSnapshot.getValue(Viaje.class);
                    mDatabase.child("customers").child(viaje.pasajero).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            pasajero = dataSnapshot.getValue(Client.class);
                            pasajeroName = pasajero.name;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                    procesarViaje();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        final ValueEventListener velcancelado = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Viaje v = dataSnapshot.getValue(Viaje.class);
                if (v != null && v.estado == Viaje.CANCELADO) {
                    viaje = null;
                    limpiarMapa(1);
                    limpiarMapa(2);
                    procesarViaje();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabase.child("drivers").child(fbid).child("viajeAsignado").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mDatabase.child("viajes").child(dataSnapshot.getValue(String.class)).addListenerForSingleValueEvent(vel);
                    mDatabase.child("viajes").child(dataSnapshot.getValue(String.class)).addValueEventListener(velcancelado);
                } else {
                    if (viaje != null) {
                        mDatabase.child("viajes").child(viaje.id).removeEventListener(vel);
                        mDatabase.child("viajes").child(viaje.id).removeEventListener(velcancelado);
                    }
                    viaje = null;
                    procesarViaje();
                }
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

    private void procesarViaje() {
        if (viaje == null) {
            mPopup.setVisibility(View.GONE);
        } else if (viaje.estado == Viaje.CARGADO) {
            mPopupTitle.setText("Nuevo viaje");
            mPopupText.setVisibility(View.GONE);
            mDisponibleCard.setVisibility(View.VISIBLE);
            mPopupOrigen.setVisibility(View.VISIBLE);
            mPopupOrigen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dot, 0, 0, 0);
            mPopupDestino.setVisibility(View.VISIBLE);
            mPopupOrigen.setText("Origen: " + viaje.origin_address);
            mPopupOrigen.setBackgroundColor(Color.TRANSPARENT);
            mPopupDestino.setText("Destino: " + viaje.destination_address);
            mPopupDestino.setCompoundDrawablesWithIntrinsicBounds(R.drawable.place, 0, 0, 0);
            mPopupCantM.setText("Cantidad de mascotas: "+viaje.cantidadMascotas);
            if (viaje.viajaAcompanante) mPopupViajaA.setText("Viaja acompañante");
            else mPopupViajaA.setText("No viaja acompañante");
            mPopupViajaA.setVisibility(View.VISIBLE);
            mPopupViajaA.setCompoundDrawablesWithIntrinsicBounds(R.drawable.person, 0, 0, 0);
            mPopupCantM.setVisibility(View.VISIBLE);
            mPopupCantM.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pawi, 0, 0, 0);
            mPopupPrecio.setCompoundDrawablesWithIntrinsicBounds(R.drawable.money, 0, 0, 0);
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
            String precio = numberFormat.format(viaje.precio);
            mPopupPrecio.setText("Precio: "+precio+" ("+viaje.formaPago+")");
            mPopupPrecio.setVisibility(View.VISIBLE);
            if (!viaje.observaciones.trim().isEmpty()) {
                mPopupObs.setText("Observaciones: " + viaje.observaciones);
                mPopupObs.setVisibility(View.VISIBLE);
            } else mPopupObs.setVisibility(View.GONE);
            mPopupDestino.setBackgroundColor(Color.TRANSPARENT);
            mPopupButtonAceptar.setVisibility(View.VISIBLE);
            mPopupButtonAceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPopupTitle.setText("Viaje en curso");
                    mPopupCantM.setVisibility(View.GONE);
                    mPopupObs.setVisibility(View.GONE);
                    mPopupViajaA.setVisibility(View.GONE);
                    mPopupText.setVisibility(View.GONE);
                    mPopupOrigen.setText("Origen: " + viaje.origin_address);
                    mPopupOrigen.setBackgroundColor(Color.CYAN);
                    mPopupDestino.setText("Destino: " + viaje.destination_address);
                    mPopupDestino.setBackgroundColor(Color.TRANSPARENT);
                    mPopupButtonAceptar.setVisibility(View.GONE);
                    mPopupButtonCancelar.setVisibility(View.GONE);
                    mPopupButtonAvanzar.setText("Llegue");
                    mPopupButtonAvanzar.setVisibility(View.VISIBLE);
                    mCalificar.setVisibility(View.GONE);
                    chStarsLayout.setVisibility(View.GONE);
                    mPopupButtonAvanzar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
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
                                            mCalificar.setVisibility(View.VISIBLE);
                                            mDatabase.child("viajes").child(viaje.id).child("estado").setValue(Viaje.FINALIZADO);
                                            viaje.estado = FINALIZADO;
                                        }
                                    });
                                    mDatabase.child("viajes").child(viaje.id).child("eta").setValue(eta2).continueWith(new Continuation<Void, Object>() {
                                        @Override
                                        public Object then(@NonNull Task<Void> task) throws Exception {
                                            mDatabase.child("viajes").child(viaje.id).child("estado").setValue(Viaje.EN_CURSO);
                                            return null;
                                        }
                                    });
                                    viaje.estado = EN_CURSO;
                                    mPopup.setVisibility(View.VISIBLE);
                                    mDisponibleCard.setVisibility(View.INVISIBLE);
                                }
                            });
                            mPopup.setVisibility(View.VISIBLE);
                            mDisponibleCard.setVisibility(View.INVISIBLE);
                            viaje.estado = CHOFER_EN_PUERTA;
                            mDatabase.child("viajes").child(viaje.id).child("estado").setValue(Viaje.CHOFER_EN_PUERTA);
                        }
                    });
                    mPopup.setVisibility(View.VISIBLE);
                    mDisponibleCard.setVisibility(View.INVISIBLE);
                    mDatabase.child("viajes").child(viaje.id).child("chofer").setValue(fbid).continueWith(new Continuation<Void, Object>() {
                        @Override
                        public Object then(@NonNull Task<Void> task) throws Exception {
                            mDatabase.child("viajes").child(viaje.id).child("eta").setValue(eta1).continueWith(new Continuation<Void, Object>() {
                                @Override
                                public Object then(@NonNull Task<Void> task) throws Exception {
                                    viaje.estado = CHOFER_YENDO;
                                    mDatabase.child("viajes").child(viaje.id).child("estado").setValue(CHOFER_YENDO);
                                    return null;
                                }
                            });
                            return null;
                        }
                    });
                }
            });
            mPopupButtonCancelar.setVisibility(View.VISIBLE);
            mPopupButtonCancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, String> data = new HashMap<>();
                    data.put("viajeid", viaje.id);
                    viaje.estado = Viaje.RECHAZADO;
                    mFunctions.getHttpsCallable("rechazarViaje").call(data).continueWith(new Continuation<HttpsCallableResult, Object>() {
                        @Override
                        public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                            viaje = null;
                            procesarViaje();
                            limpiarMapa(1);
                            limpiarMapa(2);
                            mDisponibleCard.setVisibility(View.VISIBLE);
                            return null;
                        }
                    });
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
        // Check for Location permissions
        if (ContextCompat.checkSelfPermission(DriverHomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverHomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }
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

                            if (ida == 1) eta1 = leg.getDuration().getText();
                            else eta2 = leg.getDuration().getText();
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        Intent i = null;
        if (id == R.id.nav_item_perfil)
            i = new Intent(this, PerfilActivity.class);
        if (id == R.id.nav_item_viajes)
            i = new Intent(this, MyTrips.class);
        startActivity(i);
        return false;
    }
}
