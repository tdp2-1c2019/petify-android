package com.app.petify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.app.petify.models.Client;
import com.app.petify.models.Viaje;
import com.app.petify.models.ViajeAdapter;
import com.app.petify.utils.LocalStorage;
import com.facebook.login.LoginManager;

import com.app.petify.R;

public class ClientHomeActivity extends AppCompatActivity {

    private Button logoutButton;
    private FloatingActionButton fabButton;
    private RecyclerView rv;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_client);
        rv = findViewById(R.id.rv);
        layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        Viaje[] data = {new Viaje("ASDFASDFQWIERY FHDKJS VCXJKHDSFR DHF DS EIUWER  HDFDHAKSKSJFf", true), new Viaje("ASDFASDFQWIERY FHDKJS VCXJKHDSFR DHF DS EIUWER  HDFDHAKSKSJFf", false)};
        adapter = new ViajeAdapter(data, this);
        rv.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(), DividerItemDecoration.VERTICAL);
        rv.addItemDecoration(dividerItemDecoration);
        logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                Intent navigationIntent = new Intent(ClientHomeActivity.this, MainActivity.class);
                startActivity(navigationIntent);
            }
        });

        fabButton = findViewById(R.id.floatingActionButton3);
        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ClientHomeActivity.this, NewTripActivity.class);
                startActivity(i);
            }
        });
    }
}
