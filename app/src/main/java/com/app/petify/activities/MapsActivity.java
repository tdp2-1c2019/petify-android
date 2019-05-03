package com.app.petify.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.app.petify.R;
import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.ArrayList;
import java.util.Arrays;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private int LOCATION_PERMISSION = 2;

    private FusedLocationProviderClient fusedLocationClient;

    private Marker choferMarker;
    private Place origin;
    private Place destination;
    private Polyline trip;
    private String duration;
    private String distance;

    private GoogleMap mMap;
    private AutocompleteSupportFragment mOriginAutocompleteFragment;
    private AutocompleteSupportFragment mDestinationAutocompleteFragment;
    private Button mCargarViaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (!Places.isInitialized())
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));

        mCargarViaje = findViewById(R.id.cargar_viaje);
        mCargarViaje.setVisibility(View.GONE);
        mCargarViaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), CargarViajeActivity.class);
                intent.putExtra("ORIGIN_ADDRESS", origin.getName());
                intent.putExtra("ORIGIN_LAT", origin.getLatLng().latitude);
                intent.putExtra("ORIGIN_LNG", origin.getLatLng().longitude);
                intent.putExtra("DESTINATION_ADDRESS", destination.getName());
                intent.putExtra("DESTINATION_LAT", destination.getLatLng().latitude);
                intent.putExtra("DESTINATION_LNG", destination.getLatLng().longitude);
                intent.putExtra("DURATION", duration);
                intent.putExtra("DISTANCE", distance);
                startActivity(intent);
            }
        });

        mOriginAutocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.origin_autocomplete_fragment);
        mOriginAutocompleteFragment.setCountry("AR");
        mOriginAutocompleteFragment.setHint("Origen");
        mOriginAutocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        mOriginAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                origin = place;
                loadTrip();

            }

            @Override
            public void onError(Status status) {
            }
        });

        mDestinationAutocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.destination_autocomplete_fragment);
        mDestinationAutocompleteFragment.setCountry("AR");
        mDestinationAutocompleteFragment.setHint("Destino");
        mDestinationAutocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        mDestinationAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                destination = place;
                loadTrip();

            }

            @Override
            public void onError(Status status) {
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mOriginAutocompleteFragment.setText("");
        mDestinationAutocompleteFragment.setText("");
        if (mMap != null) {
            mMap.clear();
            try {
                locateAndZoomUser();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadTrip() {
        if (origin != null && destination != null) {
            final LatLng originLatLng = origin.getLatLng();
            final LatLng destinationLatLng = destination.getLatLng();

            // Ponemos los markers sacando lo anterior
            mMap.clear();
            if (trip != null) trip.remove();
            mMap.addMarker(new MarkerOptions().position(originLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mMap.addMarker(new MarkerOptions().position(destinationLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            // Dibujamos el camino
            GoogleDirection.withServerKey(getString(R.string.google_maps_key))
                    .from(originLatLng).to(destinationLatLng)
                    .execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(Direction direction, String rawBody) {
                            if (direction.isOK()) {
                                Leg leg = direction.getRouteList().get(0).getLegList().get(0);
                                duration = leg.getDuration().getText();
                                distance = leg.getDistance().getText();
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
            mCargarViaje.setVisibility(View.VISIBLE);
        } else {
            mCargarViaje.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Check for Location permissions
        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }

        mMap = googleMap;

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
}
