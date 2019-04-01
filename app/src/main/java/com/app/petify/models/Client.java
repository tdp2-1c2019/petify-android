package com.app.petify.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Client {
    public String facebookId;
    public String name;

    public static Client fromJsonObject(JSONObject clientJson) throws JSONException {
        Client client = new Client();
        client.facebookId = clientJson.getString("facebook_id");
        client.name = clientJson.has("full_name") ? clientJson.getString("full_name") : "";
        return client;
    }

    public static JSONObject toJsonObject(Client client) throws JSONException {
        JSONObject clientJson = new JSONObject();
        clientJson.put("client_id", client.facebookId);
        clientJson.put("name", client.name);
        return clientJson;
    }
}
