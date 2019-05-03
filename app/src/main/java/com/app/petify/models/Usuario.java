package com.app.petify.models;

import java.io.Serializable;
import java.util.HashMap;

public class Usuario implements Serializable {
    public String fbid;
    public String name;
    public String email;
    public boolean isCustomer;
    public boolean isDriver;
    public boolean habilitado;
    public boolean disponible;
    public boolean cargoAuto;
    public boolean cargoRegistro;
    public boolean cargoSeguro;

    public Usuario(HashMap<String, Object> map) {
        // Obligatorios
        fbid = (String) map.get("fbid");
        isCustomer = (boolean) map.get("isCustomer");
        isDriver = (boolean) map.get("isDriver");
        // Pueden no estar, ponemos defaults
        name = (String) (map.containsKey("name") ? map.get("name") : "");
        email = (String) (map.containsKey("email") ? map.get("email") : "");
        habilitado = (boolean) (map.containsKey("habilitado") ? map.get("habilitado") : false);
        disponible = (boolean) (map.containsKey("disponible") ? map.get("disponible") : false);
        cargoAuto = (boolean) (map.containsKey("cargoAuto") ? map.get("cargoAuto") : false);
        cargoRegistro = (boolean) (map.containsKey("cargoRegistro") ? map.get("cargoRegistro") : false);
        cargoSeguro = (boolean) (map.containsKey("cargoSeguro") ? map.get("cargoSeguro") : false);
    }
}
