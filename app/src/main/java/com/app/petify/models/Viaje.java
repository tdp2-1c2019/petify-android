package com.app.petify.models;

import java.io.Serializable;

public class Viaje implements Serializable {
    public static final int CARGADO = 0;
    public static final int CHOFER_ASIGNADO = 1;
    public static final int CHOFER_YENDO = 2;
    public static final int CHOFER_EN_PUERTA = 3;
    public static final int EN_CURSO = 4;
    public static final int FINALIZADO = 5;
    public static final int RECHAZADO = 999;
    public static final int CANCELADO = 20;
    public static final int CANCELADO_GRUPO = 90;

    public String id;

    public String origin_address;
    public Double origin_latitude;
    public Double origin_longitude;
    public String destination_address;
    public Double destination_latitude;
    public Double destination_longitude;
    public String pasajero;
    public String chofer;
    public String cantidadMascotas;
    public boolean viajaAcompanante;
    public String formaPago;
    public String observaciones;
    public int estado;
    public String eta;
    public int puntaje_chofer;
    public int puntaje_pasajero;
    public int duracion;
}
