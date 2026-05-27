package models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("teacher_subjects")
public class TeacherSubject extends Model {
    
    static {
        validatePresenceOf("teacher_id", "subject_id", "role_charge", "academic_year", "academic_period")
                .message("Todos los campos obligatorios deben estar completos.");
                
        validateNumericalityOf("academic_year")
                .greaterThan(1900)
                .message("El año académico debe ser válido.");
    }
}