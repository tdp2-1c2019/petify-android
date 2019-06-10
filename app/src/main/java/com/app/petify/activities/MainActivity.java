package com.app.petify.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.app.petify.R;
import com.app.petify.models.Usuario;
import com.app.petify.utils.LocalStorage;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public CallbackManager callbackManager;
    private FirebaseFunctions mFunctions;

    private Snackbar snackbar;

    private int INTERNET_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check for Internet permissions
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION);
        }

        snackbar = Snackbar.make(findViewById(R.id.main_layout), "Iniciando sesion en Petify...", Snackbar.LENGTH_INDEFINITE);

        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));

        // Creating CallbackManager
        callbackManager = CallbackManager.Factory.create();

        // Registering CallbackManager with the LoginButton
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Log.i("RegisterCallback", "onSuccess");
                // Retrieving access token using the LoginResult
                AccessToken accessToken = loginResult.getAccessToken();
                useLoginInformation(accessToken);
            }

            @Override
            public void onCancel() {
                // App code
                Log.i("RegisterCallback", "onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.i("RegisterCallback", "onError");
            }
        });

        // Inicializamos firebase
        mFunctions = FirebaseFunctions.getInstance();

        // Vemos si ya esta logueado para ingresarlo automaticamente
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn) useLoginInformation(accessToken);
    }

    @Override
    public void onStart() {
        super.onStart();
        snackbar.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void useLoginInformation(AccessToken accessToken) {
        snackbar.show();
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    final String facebookId = object.has("id") ? object.getString("id") : "";
                    final String email = object.has("email") ? object.getString("email") : "";
                    final String name = object.has("name") ? object.getString("name") : "";

                    Map<String, String> data = new HashMap<>();
                    data.put("fbid", facebookId);

                    mFunctions.getHttpsCallable("findUser").call(data).continueWith(new Continuation<HttpsCallableResult, Usuario>() {
                        @Override
                        public Usuario then(@NonNull Task<HttpsCallableResult> task) {
                            try {
                                HashMap<String,Object> response = (HashMap<String,Object>) task.getResult().getData();
                                if (!response.containsKey("email")) response.put("email", email);
                                if (!response.containsKey("name")) response.put("name", name);

                                Usuario usuario = new Usuario(response);
                                LocalStorage.setUsuario(usuario);
                                Intent navigationIntent;

                                if (!usuario.estaRegistrado()) {
                                    navigationIntent = new Intent(MainActivity.this, UserTypeSelectionActivity.class);
                                } else if (!usuario.tienePerfilCompleto()) {
                                    navigationIntent = new Intent(MainActivity.this, CargarPerfilActivity.class);
                                } else if (usuario.isCustomer) {
                                    navigationIntent = new Intent(MainActivity.this, PasajeroActivity.class);
                                } else if (!usuario.cargoImagenes()) { // Si esta registrado y no es customer, es driver
                                    navigationIntent = new Intent(MainActivity.this, DriverPicturesActivity.class);
                                } else {
                                    navigationIntent = new Intent(MainActivity.this, DriverHomeActivity.class);
                                }
                                startActivity(navigationIntent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            snackbar.dismiss();
                            return null;
                        }
                    });
                } catch (JSONException e) {
                    Log.e("Error Facebook Login", e.getMessage());
                }
            }
        });

        // Parameter setting with Bundle
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,picture.width(200),email");
        request.setParameters(parameters);
        request.executeAsync();
    }
}

