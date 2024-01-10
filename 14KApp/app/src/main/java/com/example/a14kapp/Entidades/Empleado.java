package com.example.a14kapp.Entidades;

public class Empleado {

    private int idEmpleado, idTienda;
    private String username, password;

    public Empleado() {
        this.idEmpleado = 0;
        this.idTienda = 0;
        this.username = "";
        this.password = "";
    }

    public Empleado(int idEmpleado, int idTienda, String username, String password) {
        this.idEmpleado = idEmpleado;
        this.idTienda = idTienda;
        this.username = username;
        this.password = password;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public int getIdTienda() {
        return idTienda;
    }

    public void setIdTienda(int idTienda) {
        this.idTienda = idTienda;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
