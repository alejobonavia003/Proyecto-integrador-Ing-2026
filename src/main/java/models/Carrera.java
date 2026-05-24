package models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * Modelo que representa la entidad Materia (Course).
 * Mapea directamente con la tabla "courses" en inglés.
 */
@Table("courses")
public class Carrera extends Model {

   static {
        // Validación: Campos obligatorios
        validatePresenceOf("name", "code", "weekly_hours")
            .message("Name, code, and weekly hours are required fields.");

        // Validación: Carga horaria mayor a cero
        validateNumericalityOf("weekly_hours")
            .greaterThan(0)
            .message("Weekly hours must be a number greater than 0.");
    }
}