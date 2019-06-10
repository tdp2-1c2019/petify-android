package com.app.petify.activities;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.app.petify.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewTripActivity extends AppCompatActivity implements OnMapReadyCallback {

    LocationManager locationManager;
    GoogleMap mMap;
    EditText tbDesde;
    EditText tbHasta;
    Button b;
    LatLng current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trip);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        tbDesde = findViewById(R.id.editText2);
        tbHasta = findViewById(R.id.editText);
        b = findViewById(R.id.button2);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                Geocoder geocoder = new Geocoder(NewTripActivity.this);
                List<Address> addresses = new ArrayList<>();
                LatLng from = null;
                LatLng to = null;
                if (!tbDesde.getText().toString().equals("Ubicacion actual")) {
                    try {
                        addresses = geocoder.getFromLocationName(tbDesde.getText().toString(), 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addresses.size() > 0) {
                        from = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                    }
                } else {
                    from = current;
                }
                try {
                    addresses = geocoder.getFromLocationName(tbHasta.getText().toString(), 1);
                    if (addresses.size() > 0) {
                        to = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                LatLng mid = new LatLng((from.latitude + to.latitude) / 2, (from.longitude + to.longitude) / 2);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mid));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mid, 15);
                mMap.animateCamera(cameraUpdate);
                mMap.addMarker(new MarkerOptions().position(from));
                mMap.addMarker(new MarkerOptions().position(to));
                GoogleDirection.withServerKey("AIzaSyB3DfG7c86Bt8RNSyiUIoctokes9zB-4Yc")
                        .from(from)
                        .to(to)
                        .avoid(AvoidType.FERRIES)
                        .avoid(AvoidType.HIGHWAYS)
                        .execute(new DirectionCallback() {
                            @Override
                            public void onDirectionSuccess(Direction direction, String rawBody) {
                                if (direction.isOK()) {
                                    Leg leg = direction.getRouteList().get(0).getLegList().get(0);
                                    ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                                    PolylineOptions polylineOptions = DirectionConverter.createPolyline(NewTripActivity.this, directionPositionList, 5, Color.BLACK);
                                    mMap.addPolyline(polylineOptions);
                                } else {
                                    // Do something
                                }
                            }

                            @Override
                            public void onDirectionFailure(Throwable t) {
                                // Do something
                            }
                        });
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        Criteria criteria = new Criteria();
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        current = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(current, 15);
        mMap.animateCamera(cameraUpdate);
        mMap.addMarker(new MarkerOptions().position(current));
        tbDesde.setText("Ubicacion actual");
    }
}
