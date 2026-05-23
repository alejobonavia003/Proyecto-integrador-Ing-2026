package models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import java.util.List;

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