package com.app.petify.services;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.app.petify.models.Client;
import com.app.petify.models.Driver;
import com.app.petify.models.responses.UserResponse;
import com.app.petify.utils.Constants;
import com.app.petify.utils.LocalStorage;

public class AuthenticationService extends BaseService {

    private String URL = Constants.petifyBackendURI;

    public UserResponse<Client> clientFacebookLogin(String facebookId, String name){
        UserResponse<Client> clientResponse = findClient(facebookId);
        if (clientResponse.getStatusCode() == UserResponse.ServiceStatusCode.SUCCESS){
            return clientResponse;
        } else {
            return registerClient(facebookId, name);
        }
    }

    public UserResponse findUser(String facebookId) {

        HttpURLConnection client = null;

        try {
            URL url = new URL(URL + "/user?facebook_id=" + facebookId);
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("GET");
            client.setRequestProperty("Content-Type", "application/json");
            client.setRequestProperty("Accept", "application/json");

            client.connect();

            // Save the client information
            JSONObject result = this.getResponseResult(client);

            if (result.has("client")){
                Client clientResult = Client.fromJsonObject(result.getJSONObject("client"));
                LocalStorage.setClient(clientResult);
                return new UserResponse<>(UserResponse.ServiceStatusCode.SUCCESS, clientResult);
            } else if (result.has("driver")){
                Driver driverResult = Driver.fromJsonObject(result.getJSONObject("driver"));
                LocalStorage.setDriver(driverResult);
                return new UserResponse<>(UserResponse.ServiceStatusCode.SUCCESS, driverResult);
            } else {
                return new UserResponse<>(UserResponse.ServiceStatusCode.ERROR);
            }
        } catch(Exception exception) {
            return new UserResponse<>(UserResponse.ServiceStatusCode.ERROR);
        } finally {
            if(client != null) {
                client.disconnect();
            }
        }
    }

    public UserResponse<Client> findClient(String facebookId) {

        HttpURLConnection client = null;

        try {
            URL url = new URL(URL + "/client?facebook_id=" + facebookId);
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("GET");
            client.setRequestProperty("Content-Type", "application/json");
            client.setRequestProperty("Accept", "application/json");

            client.connect();

            // Save the client information
            JSONObject result = this.getResponseResult(client);

            Client clientResult = Client.fromJsonObject(result.getJSONObject("client"));
            LocalStorage.setClient(clientResult);

            return new UserResponse<>(UserResponse.ServiceStatusCode.SUCCESS, clientResult);

        } catch(Exception exception) {
            return new UserResponse<>(UserResponse.ServiceStatusCode.ERROR);
        } finally {
            if(client != null) {
                client.disconnect();
            }
        }
    }

    public UserResponse<Client> registerClient(String facebookId, String name) {
        HttpURLConnection client = null;

        try {
            URL url = new URL(URL + "/client");
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("POST");
            client.setRequestProperty("Content-Type", "application/json");
            client.setRequestProperty("Accept", "application/json");

            JSONObject credentials = new JSONObject();
            credentials.put("facebook_id",facebookId);
            credentials.put("full_name", name);

            OutputStream outputStream = client.getOutputStream();
            outputStream.write(credentials.toString().getBytes("UTF-8"));
            outputStream.close();

            client.connect();

            // Save the client information
            JSONObject result = this.getResponseResult(client);
            Client clientResult = Client.fromJsonObject(result.getJSONObject("client"));
            LocalStorage.setClient(clientResult);

            return new UserResponse<>(UserResponse.ServiceStatusCode.SUCCESS, clientResult);
        } catch(Exception exception) {
            return new UserResponse<>(UserResponse.ServiceStatusCode.ERROR);
        } finally {
            if(client != null) {
                client.disconnect();
            }
        }
    }

    public UserResponse<Driver> registerDriver(String facebookId) {
        HttpURLConnection client = null;

        try {
            URL url = new URL(URL + "/driver");
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("POST");
            client.setRequestProperty("Content-Type", "application/json");
            client.setRequestProperty("Accept", "application/json");

            JSONObject credentials = new JSONObject();
            credentials.put("facebook_id",facebookId);

            OutputStream outputStream = client.getOutputStream();
            outputStream.write(credentials.toString().getBytes("UTF-8"));
            outputStream.close();

            client.connect();

            // Save the client information
            JSONObject result = this.getResponseResult(client);
            Driver driverResult = Driver.fromJsonObject(result.getJSONObject("driver"));
            LocalStorage.setDriver(driverResult);

            return new UserResponse<>(UserResponse.ServiceStatusCode.SUCCESS, driverResult);
        } catch(Exception exception) {
            return new UserResponse<>(UserResponse.ServiceStatusCode.ERROR);
        } finally {
            if(client != null) {
                client.disconnect();
            }
        }
    }
}
