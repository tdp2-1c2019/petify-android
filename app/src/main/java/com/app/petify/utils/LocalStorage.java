package com.app.petify.utils;

import com.app.petify.models.Client;
import com.app.petify.models.Driver;

public class LocalStorage {

    private static Client client;
    private static Driver driver;


    public static void setClient(Client client) {
        LocalStorage.client = client;
    }

    public static Client getClient() {
        return LocalStorage.client;
    }

    public static void setDriver(Driver driver) {
        LocalStorage.driver = driver;
    }

    public static Driver getDriver() {
        return LocalStorage.driver;
    }
}
