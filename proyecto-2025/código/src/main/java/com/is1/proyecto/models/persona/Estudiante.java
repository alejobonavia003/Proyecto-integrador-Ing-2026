package com.is1.proyecto.models.persona;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("Estudiante")
public class Estudiante extends PersonaAbs {
    public enum Estado {
        AVANZADO,
        INGRESANTE,
    }

    Estado estado;

    public Estado getEstado() {
        String estadoStr = getString("estado");
        if (estadoStr == null) return null;
        return Estado.valueOf(estadoStr);
    }

    public void setEstado(Estado estado) {
        set("estado", estado.name());
        this.estado = estado;
    }
}
