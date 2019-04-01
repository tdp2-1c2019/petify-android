package com.app.petify.services;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.app.petify.models.Client;
import com.app.petify.models.responses.ServiceResponse;
import com.app.petify.utils.Constants;
import com.app.petify.utils.LocalStorage;

public class AuthenticationService extends BaseService {

    private String URL = Constants.petifyBackendURI;

    public ServiceResponse<Client> clientFacebookLogin(String facebookId, String name){
        ServiceResponse<Client> clientResponse = findClient(facebookId);
        if (clientResponse.getStatusCode() == ServiceResponse.ServiceStatusCode.SUCCESS){
            return clientResponse;
        } else {
            return registerClient(facebookId, name);
        }
    }

    public ServiceResponse<Client> findClient(String facebookId) {

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

            return new ServiceResponse<>(ServiceResponse.ServiceStatusCode.SUCCESS, clientResult);
        } catch(Exception exception) {
            return new ServiceResponse<>(ServiceResponse.ServiceStatusCode.ERROR);
        } finally {
            if(client != null) {
                client.disconnect();
            }
        }
    }

    public ServiceResponse<Client> registerClient(String facebookId, String name) {
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

            return new ServiceResponse<>(ServiceResponse.ServiceStatusCode.SUCCESS, clientResult);
        } catch(Exception exception) {
            return new ServiceResponse<>(ServiceResponse.ServiceStatusCode.ERROR);
        } finally {
            if(client != null) {
                client.disconnect();
            }
        }
    }
}
