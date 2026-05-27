package models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * modelo que reprecenta MATERIAS
 */
@Table("subjects")
public class Subject extends Model {

    static {

        validatePresenceOf(
                "name",
                "code",
                "weekly_hours"
        ).message("Todos los campos obligatorios deben estar completos.");

        validateNumericalityOf("weekly_hours")
                .greaterThan(0)
                .message("La carga horaria debe ser mayor a 0.");
    }

    public String getName() {
        return getString("name");
    }

    public String getCode() {
        return getString("code");
    }
}