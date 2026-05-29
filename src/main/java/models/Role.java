package models;

import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("roles")
public class Role extends Model {

    public String getName() {
        return getString("name"); // Devolverá "ADMIN", "TEACHER" o "STUDENT"
    }

    public void setName(String name) {
        set("name", name);
    }

    /**
     * Obtiene la lista de usuarios que pertenecen a este rol.
     */
    public List<User> getUsers() {
        return getAll(User.class);
    }
}
