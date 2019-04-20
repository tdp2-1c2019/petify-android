package com.app.petify.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.TextView;

import com.app.petify.R;
import com.app.petify.models.Client;
import com.app.petify.models.Driver;
import com.app.petify.models.User;
import com.app.petify.models.responses.UserResponse;
import com.app.petify.services.AuthenticationService;
import com.app.petify.utils.LocalStorage;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    public CallbackManager callbackManager;

    private int INTERNET_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LoginButton loginButton;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        // Check for Internet permissions
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION);
        }

        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));

        // Creating CallbackManager
        callbackManager = CallbackManager.Factory.create();

        // Registering CallbackManager with the LoginButton
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

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn) retrieveSession(accessToken);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void retrieveSession(AccessToken accessToken){
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String facebookId = object.has("id") ? object.getString("id") : "";
                    new FacebookRetrieveSessionTask().execute(facebookId);
                } catch (JSONException e) {
                    Log.e("Error Retrieve Session", e.getMessage());
                }
            }
        });

        // Parameter setting with Bundle
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void useLoginInformation(AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    try {
                        String facebookId = object.has("id") ? object.getString("id") : "";
                        String name = object.has("name") ? object.getString("name") : "";

                        new FacebookLoginTask().execute(facebookId, name);

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

    private class FacebookLoginTask extends AsyncTask<String, Void, UserResponse> {
        private AuthenticationService authenticationService = new AuthenticationService();
        private Snackbar snackbar;

        protected void onPreExecute() {
            this.snackbar.show();
            findViewById(R.id.login_button).setEnabled(false);
        }

        FacebookLoginTask() {
            this.snackbar = Snackbar.make(findViewById(R.id.main_layout), "Iniciando sesion en Petify...", Snackbar.LENGTH_INDEFINITE);
        }

        protected UserResponse doInBackground(String... params) {
            LocalStorage.setFacebookId(params[0]);
            return authenticationService.findUser(params[0]);
        }

        protected void onPostExecute(UserResponse response) {
            this.snackbar.dismiss();
            findViewById(R.id.login_button).setEnabled(true);

            UserResponse.ServiceStatusCode statusCode = response.getStatusCode();
            if (statusCode == UserResponse.ServiceStatusCode.SUCCESS){
                User userResponse = response.getServiceResponse();
                if (userResponse instanceof Client){
                    Intent navigationIntent = new Intent(MainActivity.this, MapsActivity.class);
                    startActivity(navigationIntent);
                } else if (userResponse instanceof Driver){
                    Intent navigationIntent = new Intent(MainActivity.this, DriverPicturesActivity.class);
                    startActivity(navigationIntent);
                }
            } else {
                Intent navigationIntent = new Intent(MainActivity.this, UserTypeSelectionActivity.class);
                startActivity(navigationIntent);
            }
        }
    }

    private class FacebookRetrieveSessionTask extends AsyncTask<String, Void, UserResponse> {
        private AuthenticationService authenticationService = new AuthenticationService();
        private Snackbar snackbar;

        protected void onPreExecute() {
            this.snackbar.show();
            findViewById(R.id.login_button).setEnabled(false);
        }

        FacebookRetrieveSessionTask() {
            this.snackbar = Snackbar.make(findViewById(R.id.main_layout), "Obteniendo sesion de Petify...", Snackbar.LENGTH_INDEFINITE);
        }

        protected UserResponse doInBackground(String... params) {
            return authenticationService.findUser(params[0]);
        }

        protected void onPostExecute(UserResponse response) {
            this.snackbar.dismiss();
            findViewById(R.id.login_button).setEnabled(true);

            UserResponse.ServiceStatusCode statusCode = response.getStatusCode();
            if (statusCode == UserResponse.ServiceStatusCode.SUCCESS){
                User userResponse = response.getServiceResponse();
                if (userResponse instanceof Client){
                    Intent navigationIntent = new Intent(MainActivity.this, MapsActivity.class);
                    startActivity(navigationIntent);
                } else if (userResponse instanceof Driver){
                    Intent navigationIntent = new Intent(MainActivity.this, DriverPicturesActivity.class);
                    startActivity(navigationIntent);
                }
            } else {
                this.snackbar = Snackbar.make(findViewById(R.id.main_layout), "Ocurrio un error obteniendo su sesion de Facebook", Snackbar.LENGTH_SHORT);
                LoginManager.getInstance().logOut();
            }
        }
    }

}

