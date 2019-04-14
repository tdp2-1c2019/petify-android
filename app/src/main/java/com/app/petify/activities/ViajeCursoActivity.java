package com.app.petify.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.app.petify.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

public class ViajeCursoActivity extends FragmentActivity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viaje_curso);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}
