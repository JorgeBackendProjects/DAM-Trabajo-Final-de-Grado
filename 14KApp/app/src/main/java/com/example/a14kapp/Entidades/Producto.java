package com.example.a14kapp.Entidades;

import java.util.ArrayList;
import java.util.List;

public class Producto {

    private String nombre, descripcion, precio, imagen;

    public Producto() {
        this.nombre = "";
        this.descripcion = "";
        this.precio = "";
        this.imagen = "";
    }

    public Producto(String nombre, String descripcion, String precio, String imagenUrl) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imagen = imagenUrl;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}
