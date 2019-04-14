package com.app.petify.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.app.petify.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private int LOCATION_PERMISSION = 2;

    private Geocoder geocoder;
    private FusedLocationProviderClient fusedLocationClient;

    private Marker choferMarker;
    private Address origin;
    private Address destination;
    private Polyline trip;

    private SearchAddress originSearch;
    private SearchAddress destinationSearch;

    private GoogleMap mMap;
    private EditText mOriginAdress;
    private EditText mDestinationAdress;
    private Button mCargarViaje;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        geocoder = new Geocoder(this);

        mOriginAdress = findViewById(R.id.origin_address);
        mDestinationAdress = findViewById(R.id.destination_address);
        mCargarViaje = findViewById(R.id.cargar_viaje);
        mCargarViaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), CargarViajeActivity.class);
                i.putExtra("from", 123);
                i.putExtra("to", 321);
                startActivity(i);
            }
        });

        mOriginAdress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cambia lo escrito por el usuario
                if (originSearch != null) originSearch.cancel(true);
                originSearch = new SearchAddress(true);
                originSearch.execute();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mDestinationAdress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cambia lo escrito por el usuario
                if (destinationSearch != null) destinationSearch.cancel(true);
                destinationSearch = new SearchAddress(false);
                destinationSearch.execute();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Conectamos a la db de firebase para tener las coordenadas del chofer
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private void loadTrip() {
        if (origin != null && destination != null) {
            final LatLng originLatLng = new LatLng(origin.getLatitude(), origin.getLongitude());
            final LatLng destinationLatLng = new LatLng(destination.getLatitude(), destination.getLongitude());

            // Ponemos los markers sacando lo anterior
            mMap.clear();
            if (trip != null) trip.remove();
            mMap.addMarker(new MarkerOptions().position(originLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mMap.addMarker(new MarkerOptions().position(destinationLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            // Dibujamos el camino
            GoogleDirection.withServerKey("AIzaSyB3DfG7c86Bt8RNSyiUIoctokes9zB-4Yc")
                    .from(originLatLng).to(destinationLatLng)
                    .execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(Direction direction, String rawBody) {
                            if (direction.isOK()) {
                                Leg leg = direction.getRouteList().get(0).getLegList().get(0);
                                ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                                PolylineOptions polylineOptions = DirectionConverter.createPolyline(MapsActivity.this, directionPositionList, 5, Color.BLUE);
                                // Dibujamos el recorrido
                                trip = mMap.addPolyline(polylineOptions);
                                // Zoomeamos el recorrido
                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                builder.include(originLatLng);
                                builder.include(destinationLatLng);
                                int padding = 300;
                                LatLngBounds bounds = builder.build();
                                final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                mMap.animateCamera(cu);
                            }
                        }

                        @Override
                        public void onDirectionFailure(Throwable t) {
                        }
                    });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Check for Location permissions
        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        // Obtenemos la ubicacion del usuario y lo zoomeamos ahi inicialmente
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

        ValueEventListener driverPositionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Double> coordinates = (HashMap<String, Double>) dataSnapshot.getValue();
                LatLng choferLatLng = new LatLng(coordinates.get("lat"), coordinates.get("lng"));
                if (choferMarker != null) choferMarker.remove();
                choferMarker = mMap.addMarker(new MarkerOptions().position(choferLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabase.child("ubicacion").addValueEventListener(driverPositionListener);
    }

    private class SearchAddress extends AsyncTask<Void, Void, Address> {
        private boolean isOrigin;
        private EditText editText;

        SearchAddress(boolean isOrigin) {
            this.isOrigin = isOrigin;
            if (isOrigin) editText = mOriginAdress;
            else editText = mDestinationAdress;
        }

        @Override
        protected void onPreExecute() {
            editText.setError(null);
        }

        @Override
        protected Address doInBackground(Void... nothing) {
            try {
                String addressString = editText.getText().toString();
                List<Address> results = geocoder.getFromLocationName(addressString, 1);
                if (results.size() == 0) {
                    return null;
                }
                Address address = results.get(0);
                if (isCancelled()) return null;
                return address;
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Address result) {
            if (result == null) editText.setError("Direccion invalida");
            else {
                if (isOrigin) origin = result;
                else destination = result;
                loadTrip();
            }
        }
    }
}
