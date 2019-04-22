package com.app.petify.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.app.petify.R;
import com.app.petify.models.Client;
import com.app.petify.models.Driver;
import com.app.petify.models.responses.UserResponse;
import com.app.petify.services.AuthenticationService;
import com.app.petify.utils.LocalStorage;

public class UserTypeSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type_selection);

        ImageButton clientTypeButton = this.findViewById(R.id.client_type_button);
        clientTypeButton.setOnClickListener(new ClientTypeButtonHandler());

        ImageButton driverTypeButton = this.findViewById(R.id.driver_type_button);
        driverTypeButton.setOnClickListener(new DriverTypeButtonHandler());
    }

    protected class ClientTypeButtonHandler implements View.OnClickListener {
        public void onClick(View v) {
            new ClientSignUpTask().execute();
        }
    }

    protected class DriverTypeButtonHandler implements View.OnClickListener {
        public void onClick(View v) {
            new DriverSignUpTask().execute();
        }
    }

    protected class ClientSignUpTask extends AsyncTask<String, Void, UserResponse<Client>> {
        private AuthenticationService authenticationService = new AuthenticationService();
        private Snackbar snackbar;

        protected void onPreExecute() {
            this.snackbar.show();
        }

        public ClientSignUpTask() {
            this.snackbar = Snackbar.make(findViewById(R.id.user_type_selection), "Registrando cliente...", Snackbar.LENGTH_INDEFINITE);
        }

        protected UserResponse<Client> doInBackground(String... params) {
            String facebookId = LocalStorage.getFacebookId();
            return authenticationService.registerClient(facebookId, "");
        }

        protected void onPostExecute(UserResponse<Client> response) {
            this.snackbar.dismiss();

            UserResponse.ServiceStatusCode statusCode = response.getStatusCode();
            if (statusCode == UserResponse.ServiceStatusCode.SUCCESS) {
                Client client = response.getServiceResponse();
                LocalStorage.setClient(client);
                Intent navigationIntent = new Intent(UserTypeSelectionActivity.this, MapsActivity.class);
                startActivity(navigationIntent);
            } else {
                this.snackbar = Snackbar.make(findViewById(R.id.main_layout), "Ocurrio un error registrando el cliente", Snackbar.LENGTH_SHORT);
            }
        }
    }

    protected class DriverSignUpTask extends AsyncTask<String, Void, UserResponse<Driver>> {
        private AuthenticationService authenticationService = new AuthenticationService();
        private Snackbar snackbar;

        protected void onPreExecute() {
            this.snackbar.show();
        }

        public DriverSignUpTask() {
            this.snackbar = Snackbar.make(findViewById(R.id.user_type_selection), "Registrando chofer...", Snackbar.LENGTH_INDEFINITE);
        }

        protected UserResponse<Driver> doInBackground(String... params) {
            String facebookId = LocalStorage.getFacebookId();
            return authenticationService.registerDriver(facebookId);
        }

        protected void onPostExecute(UserResponse<Driver> response) {
            this.snackbar.dismiss();

            UserResponse.ServiceStatusCode statusCode = response.getStatusCode();
            if (statusCode == UserResponse.ServiceStatusCode.SUCCESS) {
                Driver driver = response.getServiceResponse();
                LocalStorage.setDriver(driver);
                Intent navigationIntent = new Intent(UserTypeSelectionActivity.this, DriverPicturesActivity.class);
                startActivity(navigationIntent);
            } else {
                this.snackbar = Snackbar.make(findViewById(R.id.main_layout), "Ocurrio un error registrando el chofer", Snackbar.LENGTH_SHORT);
            }
        }
    }

}
