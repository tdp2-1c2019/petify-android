package com.app.petify.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.app.petify.models.Client;
import com.app.petify.models.responses.ServiceResponse;
import com.app.petify.services.AuthenticationService;
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

    private LoginButton loginButton;

    private int INTERNET_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        if (isLoggedIn) useLoginInformation(accessToken);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void useLoginInformation(AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    try {
                        String facebookId = object.has("id") ? object.getString("id") : "";
                        String name = object.has("name") ? object.getString("name") : "";

                        new FacebookLoginClientTask().execute(facebookId, name);

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

    protected class FacebookLoginClientTask extends AsyncTask<String, Void, ServiceResponse<Client>> {
        private AuthenticationService authenticationService = new AuthenticationService();

        protected void onPreExecute() {
            Button fbLoginButton = findViewById(R.id.login_button);
            fbLoginButton.setEnabled(false);
        }

        protected ServiceResponse<Client> doInBackground(String... params) {
            return authenticationService.clientFacebookLogin(params[0], params[1]);
        }

        protected void onPostExecute(ServiceResponse<Client> response) {
            Button fbLoginButton = findViewById(R.id.login_button);
            fbLoginButton.setEnabled(true);

            TextView loginResult = findViewById(R.id.loginResult);

            ServiceResponse.ServiceStatusCode statusCode = response.getStatusCode();
            if (statusCode == ServiceResponse.ServiceStatusCode.SUCCESS){
                Intent navigationIntent = new Intent(MainActivity.this, ClientHomeActivity.class);
                startActivity(navigationIntent);
                //LoginManager.getInstance().logOut();
            } else {
                loginResult.setText(R.string.error_invalid_login);
            }
        }
    }

}

