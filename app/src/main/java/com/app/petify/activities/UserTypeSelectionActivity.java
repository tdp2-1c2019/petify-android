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
import com.app.petify.models.Usuario;
import com.app.petify.models.responses.UserResponse;
import com.app.petify.services.AuthenticationService;
import com.app.petify.utils.LocalStorage;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserTypeSelectionActivity extends AppCompatActivity {
    private Usuario usuario;

    private Snackbar mSnackbar;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type_selection);

        ImageButton clientTypeButton = this.findViewById(R.id.client_type_button);
        clientTypeButton.setOnClickListener(new ClientTypeButtonHandler());

        ImageButton driverTypeButton = this.findViewById(R.id.driver_type_button);
        driverTypeButton.setOnClickListener(new DriverTypeButtonHandler());

        usuario = LocalStorage.getUsuario();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onStop() {
        super.onStop();
        mSnackbar.dismiss();
    }

    protected class ClientTypeButtonHandler implements View.OnClickListener {
        public void onClick(View v) {
            mSnackbar = Snackbar.make(findViewById(R.id.user_type_selection), "Registrando cliente...", Snackbar.LENGTH_INDEFINITE);
            mSnackbar.show();

            // Creamos el user en la db
            DatabaseReference customerReference = mDatabase.child("customers").child(usuario.fbid);
            customerReference.child("habilitado").setValue(usuario.disponible);
            customerReference.child("email").setValue(usuario.email);
            customerReference.child("name").setValue(usuario.name);

            usuario.isCustomer = true;

            // Vamos al home del usuario
            Intent navigationIntent = new Intent(UserTypeSelectionActivity.this, CargarPerfilActivity.class);
            startActivity(navigationIntent);
        }
    }

    protected class DriverTypeButtonHandler implements View.OnClickListener {
        public void onClick(View v) {
            mSnackbar = Snackbar.make(findViewById(R.id.user_type_selection), "Registrando chofer...", Snackbar.LENGTH_INDEFINITE);
            mSnackbar.show();

            // Creamos el user en la db
            DatabaseReference driverReference = mDatabase.child("drivers").child(usuario.fbid);
            driverReference.child("cargoAuto").setValue(usuario.cargoAuto);
            driverReference.child("cargoRegistro").setValue(usuario.cargoRegistro);
            driverReference.child("cargoSeguro").setValue(usuario.cargoSeguro);
            driverReference.child("disponible").setValue(usuario.disponible);
            driverReference.child("email").setValue(usuario.email);
            driverReference.child("habilitado").setValue(usuario.habilitado);
            driverReference.child("name").setValue(usuario.name);

            usuario.isDriver = true;

            // Vamos a cargar el perfil del chofer
            Intent navigationIntent = new Intent(UserTypeSelectionActivity.this, CargarPerfilActivity.class);
            startActivity(navigationIntent);
        }
    }
}
