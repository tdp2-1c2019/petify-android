package com.app.petify.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.app.petify.R;
import com.app.petify.models.Driver;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverHomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button logoutButton;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private int LOCATION_PERMISSION = 2;
    private Switch disponible;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_driver);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Driver driver = LocalStorage.getDriver();
        disponible = findViewById(R.id.switch2);

        logoutButton = findViewById(R.id.logout_button);
//        logoutButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                fusedLocationClient.removeLocationUpdates(locationCallback);
//                LoginManager.getInstance().logOut();
//                Intent navigationIntent = new Intent(DriverHomeActivity.this, MainActivity.class);
//                startActivity(navigationIntent);
//            }
//        });

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Check for Location permissions
        if (ContextCompat.checkSelfPermission(DriverHomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverHomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }

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
                        mDatabase.child("ubicacion").child("lat").setValue(wayLatitude);
                        mDatabase.child("ubicacion").child("lng").setValue(wayLongitude);
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

        mDatabase.child("disponibles").child(Profile.getCurrentProfile().getId()).setValue(disponible.isChecked());
        disponible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDatabase.child("disponibles").child(Profile.getCurrentProfile().getId()).setValue(disponible.isChecked());
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Check for Location permissions
        if (ContextCompat.checkSelfPermission(DriverHomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverHomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        mDatabase.child("disponibles").child(Profile.getCurrentProfile().getId()).setValue(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDatabase.child("disponibles").child(Profile.getCurrentProfile().getId()).setValue(true);
    }
}
