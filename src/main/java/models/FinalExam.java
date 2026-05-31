package models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("final_exams")
public class FinalExam extends Model {
    static {
        validatePresenceOf("subject_id", "teacher_id", "registration_start", "registration_end", "exam_date")
                .message("Materia, titular, fecha de inscripción y fecha del examen son obligatorias.");
    }
}