package com.app.petify.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.app.petify.models.Client;
import com.app.petify.utils.LocalStorage;
import com.facebook.login.LoginManager;

public class ClientHomeActivity extends AppCompatActivity {

    private TextView displayName;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_client);

        Client client = LocalStorage.getClient();

        displayName = findViewById(R.id.display_name);
        displayName.setText(client.name);

        logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                finish();
            }
        });
    }
}
