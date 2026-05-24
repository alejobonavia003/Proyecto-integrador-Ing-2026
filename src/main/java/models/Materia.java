package models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * Modelo que representa la entidad Materia.
 * Mapea directamente con la tabla "materias" en la base de datos.
 */
@Table("materias")
public class Materia extends Model {

    static {
      
        validatePresenceOf("nombre", "codigo_materia", "carga_horaria", "modalidad")
            .message("Todos los campos de la materia son obligatorios.");
            
        
        validateNumericalityOf("carga_horaria")
            .greaterThan(0)
            .message("La carga horaria debe ser un número entero mayor a 0.");
    }
}