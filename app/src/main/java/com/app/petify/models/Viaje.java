package com.app.petify.models;

public class Viaje {
    private String text;
    private Boolean reserva;

    public Viaje(String t, Boolean r) {
        text = t;
        reserva = r;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getReserva() {
        return reserva;
    }

    public void setReserva(Boolean reserva) {
        this.reserva = reserva;
    }
}
