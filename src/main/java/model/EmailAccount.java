/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.LinkedList;

/**
 *
 * @author yoelkys.hernandez-h
 */
public class EmailAccount {

    String email;
    String contrasenna;
    boolean enUso;
    int posicionEnDatos;

    String cliente;

    public EmailAccount(String email, String contrasenna, String enUso, int posicionEnDatos) {
        this.email = email;
        this.contrasenna = contrasenna;
        this.enUso = enUso.toLowerCase().contains("n") ? false : true;
        this.posicionEnDatos = posicionEnDatos;
        cliente = null;
    }

    public void setEnUso(boolean enUso) {
        this.enUso = enUso;
    }

    public String getEmail() {
        return email;
    }

    public int getPosicionEnDatos() {
        return posicionEnDatos;
    }

    public String getContrasenna() {
        return contrasenna;
    }

    public boolean getEnUso() {
        return enUso;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getCliente() {
        return cliente;
    }

}
