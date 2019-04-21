package com.app.petify.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Driver implements User{
    public boolean disponible;
    public double lat;
    public double lng;
    public String facebookId;
    public String name;

    public static Driver fromJsonObject(JSONObject driverJson) throws JSONException {
        Driver driver = new Driver();
        driver.facebookId = driverJson.getString("facebook_id");
        driver.name = driverJson.has("name") ? driverJson.getString("name") : "";
        return driver;
    }

    public static JSONObject toJsonObject(Driver driver) throws JSONException {
        JSONObject driverJson = new JSONObject();
        driverJson.put("driver_id", driver.facebookId);
        driverJson.put("name", driver.name);
        return driverJson;
    }
}
