package com.app.petify.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.app.petify.R;
import com.app.petify.models.Usuario;
import com.app.petify.utils.LocalStorage;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

public class CargarPerfilActivity extends AppCompatActivity implements OnMapReadyCallback {
    private int LOCATION_PERMISSION = 2;

    private Usuario usuario;
    private FusedLocationProviderClient fusedLocationClient;

    private EditText mNombre;
    private EditText mEmail;
    private EditText mTelefono;
    private AutocompleteSupportFragment mDireccion;
    private Place direccion;
    private Button mCargarPerfil;

    private GoogleMap mMap;
    private Snackbar mSnackbar;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargar_perfil);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (!Places.isInitialized())
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));

        mNombre = findViewById(R.id.perfil_nombre);
        mEmail = findViewById(R.id.perfil_email);
        mTelefono = findViewById(R.id.perfil_telefono);
        mDireccion = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.perfil_direccion);
        mDireccion.setCountry("AR");
        mDireccion.setHint("Direccion");
        mDireccion.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        mDireccion.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                direccion = place;
                usuario.direccion = place.getAddress();
                locateAndZoomDireccion();
            }

            @Override
            public void onError(Status status) {
            }
        });
        mCargarPerfil = findViewById(R.id.cargar_perfil);
        mCargarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSnackbar = Snackbar.make(findViewById(R.id.perfil_layout), "Guardando perfil...", Snackbar.LENGTH_INDEFINITE);
                mSnackbar.show();

                usuario.name = mNombre.getText().toString();
                usuario.email = mEmail.getText().toString();
                usuario.telefono = mTelefono.getText().toString();
                usuario.direccion = direccion.getName();

                // Cargamos el usuario en firebase
                DatabaseReference userReference = mDatabase.child((usuario.isCustomer ? "customers" : "drivers")).child(usuario.fbid);
                userReference.child("name").setValue(usuario.name);
                userReference.child("email").setValue(usuario.email);
                userReference.child("telefono").setValue(usuario.telefono);
                userReference.child("telefonoEmergencia").setValue(usuario.telefonoEmergencia);
                userReference.child("direccion").setValue(usuario.direccion);

                Intent navigationIntent;
                if (usuario.isCustomer) {
                    navigationIntent = new Intent(CargarPerfilActivity.this, MapsActivity.class);
                } else if (!usuario.cargoImagenes()) {
                    navigationIntent = new Intent(CargarPerfilActivity.this, DriverPicturesActivity.class);
                } else {
                    navigationIntent = new Intent(CargarPerfilActivity.this, DriverHomeActivity.class);
                }
                startActivity(navigationIntent);
            }
        });

        usuario = LocalStorage.getUsuario();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mNombre.setText(usuario.name);
        mEmail.setText(usuario.email);
        mTelefono.setText(usuario.telefono);
        mDireccion.setText(usuario.direccion);
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            mSnackbar.dismiss();
        } catch (Exception e) {
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Check for Location permissions
        if (ContextCompat.checkSelfPermission(CargarPerfilActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CargarPerfilActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }

        mMap = googleMap;

        try {
            locateAndZoomUser();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void locateAndZoomUser() {
        // Check for Location permissions
        if (ContextCompat.checkSelfPermission(CargarPerfilActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CargarPerfilActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }
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

    private void locateAndZoomDireccion() {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(direccion.getLatLng())
                .zoom(17)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
