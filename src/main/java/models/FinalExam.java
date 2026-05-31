package models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("final_exams")
public class FinalExam extends Model {
    static {
        validatePresenceOf("subject_id", "exam_date").message("La materia y la fecha son obligatorias.");
    }
}