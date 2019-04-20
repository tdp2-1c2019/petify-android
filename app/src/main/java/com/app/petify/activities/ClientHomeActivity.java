package com.app.petify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.app.petify.R;
import com.app.petify.models.Client;
import com.app.petify.utils.LocalStorage;
import com.facebook.login.LoginManager;

public class ClientHomeActivity extends AppCompatActivity {

    private Button logoutButton;
    private FloatingActionButton fabButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_client);

        Client client = LocalStorage.getClient();

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
