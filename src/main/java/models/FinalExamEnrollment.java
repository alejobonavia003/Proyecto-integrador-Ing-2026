package models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("final_exam_enrollments")
public class FinalExamEnrollment extends Model {
    public static final String STATUS_INSCRIPTO = "INSCRIPTO";
    public static final String STATUS_AUSENTE = "AUSENTE";
    public static final String STATUS_DESAPROBADO = "DESAPROBADO";
    public static final String STATUS_APROBADO = "APROBADO";
}