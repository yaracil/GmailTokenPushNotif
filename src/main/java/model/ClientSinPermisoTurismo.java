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
public class ClientSinPermisoTurismo extends ClienteClass {

    String fechaNacimiento;
    char sexo;

    public ClientSinPermisoTurismo(String pasaporte, String nombre, String apellido1, String apellido2, TipoCliente tipoCliente, String prioridad, String fecha, String sexo, List<String> cuentasUsandolo) {
        super(pasaporte, nombre, apellido1, apellido2, tipoCliente, prioridad, cuentasUsandolo);
        this.fechaNacimiento = fecha;
        this.sexo = sexo.trim().charAt(0);
    }

    public char getSexo() {
        return sexo;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

}
