package com.app.petify.models;

import java.io.Serializable;
import java.util.HashMap;

public class Usuario implements Serializable {
    public String fbid;
    public String name;
    public String email;
    public String telefono;
    public String telefonoEmergencia;
    public String direccion;
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
        name = (String) (map.containsKey("name") ? map.get("name") : null);
        email = (String) (map.containsKey("email") ? map.get("email") : null);
        telefono = (String) (map.containsKey("telefono") ? map.get("telefono") : null);
        telefonoEmergencia = (String) (map.containsKey("telefonoEmergencia") ? map.get("telefonoEmergencia") : null);
        direccion = (String) (map.containsKey("direccion") ? map.get("direccion") : null);
        habilitado = (boolean) (map.containsKey("habilitado") ? map.get("habilitado") : false);
        disponible = (boolean) (map.containsKey("disponible") ? map.get("disponible") : false);
        cargoAuto = (boolean) (map.containsKey("cargoAuto") ? map.get("cargoAuto") : false);
        cargoRegistro = (boolean) (map.containsKey("cargoRegistro") ? map.get("cargoRegistro") : false);
        cargoSeguro = (boolean) (map.containsKey("cargoSeguro") ? map.get("cargoSeguro") : false);
    }

    public boolean estaRegistrado() {
        return (isCustomer || isDriver);
    }

    public boolean tienePerfilCompleto() {
        return (name != null && email != null && telefono != null && telefonoEmergencia != null && direccion != null);
    }

    public boolean cargoImagenes() {
        return (cargoAuto && cargoRegistro && cargoSeguro);
    }
}
