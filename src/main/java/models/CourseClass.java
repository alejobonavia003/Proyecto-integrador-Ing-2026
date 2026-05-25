package models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("course_classes")
public class CourseClass extends Model {

    static {

        validatePresenceOf(
                "name",
                "subject_id",
                "teacher_id"
        ).message("Todos los campos obligatorios deben estar completos.");

        validateNumericalityOf("capacity")
                .greaterThan(0)
                .allowNull(true)
                .message("La capacidad debe ser mayor a 0.");
    }
}