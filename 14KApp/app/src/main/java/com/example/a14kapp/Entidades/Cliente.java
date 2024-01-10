package com.example.a14kapp.Entidades;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class Cliente{

    private String nombre, ciudad, telefono, email, password;
    private ArrayList<Cita> citas;

    public Cliente() {
        this.nombre = "";
        this.ciudad = "";
        this.telefono = "";
        this.email = "";
        this.password = "";
        this.citas = new ArrayList<Cita>();
    }

    public Cliente(String nombre, String ciudad, String telefono, String email, String password) {
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.telefono = telefono;
        this.email = email;
        this.password = password;
        this.citas = new ArrayList<Cita>();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public ArrayList<Cita> getCitas() {
        return citas;
    }

    public void setCitas(Cita cita) {
        this.citas.add(cita);
    }
}
