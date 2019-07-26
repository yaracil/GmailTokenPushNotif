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
public class ClientConPer extends ClienteClass {

    String nut;

    public ClientConPer(String pasaporte, String nombre, String apellido1, String apellido2, String nut, TipoCliente tipoCliente, String prioridad, List<String> cuentasUsandolo) {
        super(pasaporte, nombre, apellido1, apellido2, tipoCliente, prioridad, cuentasUsandolo);
        this.nut = nut;
    }

    public String getNut() {
        return nut;
    }

}
