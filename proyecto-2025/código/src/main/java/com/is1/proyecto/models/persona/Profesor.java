package com.is1.proyecto.models.persona;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("Profesor")
public class Profesor extends PersonaAbs {
    
    // No necesitas el campo idProfesor aquí, ActiveJDBC lo maneja automáticamente
    
    public String getIdProfesor() {
        return getString("id_doc");
    }

    public Profesor() {
        super();
    }
    
    // Constructor con parámetros
    public Profesor(String nombre, String apellido, String email, String direccion, String telefono, Integer dni) {
        setNombre(nombre);
        setApellido(apellido);
        setEmail(email);
        setDireccion(direccion);
        setTelefono(telefono);
        setDni(dni);
    }


}