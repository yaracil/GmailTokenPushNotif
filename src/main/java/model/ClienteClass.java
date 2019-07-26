/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author yoelkys.hernandez-h
 */
public abstract class ClienteClass {

    String pasaporte;
    String nombre;
    String apellidos1;
    String apellidos2;    
    TipoCliente tipoCliente;
    int prioridad;
    List<String> cuentasUsandolo;

    public ClienteClass(String pasaporte, String nombre, String apellidos1, String apellidos2, TipoCliente tipoCliente, String prioridad, List<String> cuentasUsandolo) {
        this.pasaporte = pasaporte;
        this.nombre = nombre;
        this.apellidos1 = apellidos1;
        this.apellidos2 = apellidos2;
        this.cuentasUsandolo = cuentasUsandolo;
        this.tipoCliente = tipoCliente;
        this.prioridad = Integer.parseInt(prioridad);
    }

//    public boolean requestClient(EmailAccount account) {
//        if (cuentasUsandolo.contains(account.email)) {
//            return false;
//        } else {
//            cuentasUsandolo.add(account.email);
//            return true;
//        }
//    }
    public boolean releaseClient(EmailAccount account) {
        if (!cuentasUsandolo.contains(account.email)) {
            return false;
        } else {
            cuentasUsandolo.remove(account.email);
            return true;
        }
    }

    public String getApellidos2() {
        return apellidos2;
    }

    public String getApellidos1() {
        return apellidos1;
    }

    public int getCantCuentasUsandolo() {
        return cuentasUsandolo.size();
    }

    public String getNombre() {
        return nombre;
    }

    public String getPasaporte() {
        return pasaporte;
    }

    public void setApellidos(String apellidos) {
        this.apellidos1 = apellidos;
    }

    public void setCuentasUsandolo(LinkedList<String> cuentasUsandolo) {
        this.cuentasUsandolo = cuentasUsandolo;
    }

    public List<String> getCuentasUsandolo() {
        return cuentasUsandolo;
    }

    public String getCuentasUsandoloToString() {
        String mails = cuentasUsandolo.size() > 0 ? cuentasUsandolo.get(0) : "";

        for (int i = 1; i < cuentasUsandolo.size(); i++) {
            mails += "," + cuentasUsandolo.get(i);
        }
        return mails;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPasaporte(String pasaporte) {
        this.pasaporte = pasaporte;
    }

    public int getPrioridad() {
        return prioridad;
    }

}
