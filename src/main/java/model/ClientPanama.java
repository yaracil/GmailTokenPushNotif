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
public class ClientPanama extends ClienteClass {

    String fechaNacimiento;
    char sexo;
    String carnet;
    boolean sacado;

    public ClientPanama(String fechaNacimiento, String sexo, String carnet, String pasaporte, String nombre, String apellidos1, String apellidos2, TipoCliente tipoCliente, String prioridad, List<String> cuentasUsandolo, String sacado) {
        super(pasaporte, nombre, apellidos1, apellidos2, tipoCliente, prioridad, cuentasUsandolo);
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo.contains("M") ? 'M' : 'F';
        this.carnet = carnet;
        this.sacado = sacado.contains("-") ? false : true;
    }

    public char getSexo() {
        return sexo;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getCarnet() {
        return carnet;
    }

    public boolean getSacado() {
        return sacado;
    }

}
