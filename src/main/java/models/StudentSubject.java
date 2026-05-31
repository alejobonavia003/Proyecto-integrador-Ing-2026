package models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("student_subjects")
public class StudentSubject extends Model {
    static {
        validatePresenceOf("student_id", "subject_id").message("El alumno y la materia son obligatorios para un registro de materia aprobada.");
    }
}
