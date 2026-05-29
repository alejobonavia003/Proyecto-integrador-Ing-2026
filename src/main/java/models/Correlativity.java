package models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("correlativities")
public class Correlativity extends Model {
    static {
        validatePresenceOf("subject_id", "required_subject_id", "requires_approved")
                .message("Los identificadores de las materias y la condición son obligatorios.");
    }
}
