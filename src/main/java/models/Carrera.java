package models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("carreras")
public class Carrera extends Model {

    static {
        // Validaciones nativas para garantizar la integridad de los datos
        validatePresenceOf("codigo_materia", "nombre", "duracion");
        
        // Validamos que la duración sea un número lógico (ej. años o cuatrimestres > 0)
        validateNumericalityOf("duracion").greaterThan(0);
    }

    // --- Getters y Setters ---
    // Aunque ActiveJDBC permite usar get() y set() directamente, crear wrappers 
    // tipados es una excelente práctica para facilitar la lectura y evitar errores 
    // en los Servicios y Controladores.

    public String getCodigoMateria() {
        return getString("codigo_materia");
    }

    public void setCodigoMateria(String codigoMateria) {
        set("codigo_materia", codigoMateria);
    }

    public String getNombre() {
        return getString("nombre");
    }

    public void setNombre(String nombre) {
        set("nombre", nombre);
    }

    public Integer getDuracion() {
        return getInteger("duracion");
    }

    public void setDuracion(Integer duracion) {
        set("duracion", duracion);
    }
}