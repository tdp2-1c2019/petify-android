package com.app.petify.services;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public abstract class BaseService {

    protected JSONObject getResponseResult(HttpURLConnection client) throws IOException, JSONException {
        BufferedReader br;
        if (200 <= client.getResponseCode() && client.getResponseCode() <= 299) {
            br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(client.getErrorStream()));
        }

        // Convert result to string
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }

        String stringResult = sb.toString();

        // Convert result string to JSON object
        JSONObject result = new JSONObject(stringResult);

        if (result.has("message")) {
            // There was an error
            throw new IllegalStateException(result.getString("message"));
        }

        return result;
    }
}
