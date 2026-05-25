package models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * modelo que reprecenta CARRERAS
 */
@Table("careers")
public class Career extends Model {

    static {

        validatePresenceOf(
                "name",
                "code"
        ).message("Nombre y código son obligatorios.");

        validateNumericalityOf("duration")
                .greaterThan(0)
                .allowNull(true)
                .message("La duración debe ser mayor a 0.");
    }
}