package models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * Modelo que representa la entidad Plan de Estudio (Study Plan).
 * Mapea directamente con la tabla "study_plans" en inglés.
 */
@Table("study_plans")
public class PlanDeEstudio extends Model {

    static {
        // Validación: Campos obligatorios antes de persistir
        validatePresenceOf("name", "code")
            .message("Name and code are required fields.");
    }
}