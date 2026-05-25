package models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;


/**
 * modelo que reprecenta los planes de estudio 
 */
@Table("study_plans")
public class StudyPlan extends Model {

    static {

        validatePresenceOf(
                "name",
                "code",
                "version",
                "career_id"
        ).message("Todos los campos del plan son obligatorios.");
    }
}