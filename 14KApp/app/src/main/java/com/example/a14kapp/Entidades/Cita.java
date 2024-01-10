package com.example.a14kapp.Entidades;

public class Cita {

    private String mes, dia, hora, descripcion, tienda;

    public Cita() {
        this.mes = "";
        this.dia = "";
        this.hora = "";
        this.descripcion = "";
        this.tienda = "";
    }

    public Cita(String mes, String dia, String hora, String descripcion, String tienda) {
        this.mes = mes;
        this.dia = dia;
        this.hora = hora;
        this.descripcion = descripcion;
        this.tienda = tienda;
    }

    public Cita(Cita other) {
        this.mes = other.mes;
        this.dia = other.dia;
        this.hora = other.hora;
        this.descripcion = other.descripcion;
        this.tienda = other.tienda;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTienda() {
        return tienda;
    }

    public void setTienda(String tienda) {
        this.tienda = tienda;
    }
}
