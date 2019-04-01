package com.app.petify.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.app.petify.models.Client;
import com.app.petify.utils.LocalStorage;

public class ClientHomeActivity extends AppCompatActivity {

    private TextView displayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_client);

        displayName = findViewById(R.id.display_name);

        Client client = LocalStorage.getClient();

        displayName.setText(client.name);
    }
}
