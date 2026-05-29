package models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("users")
public class User extends Model {

    // --- Getters y Setters Básicos ---

    public String getDni() {
        return getString("dni");
    }

    public void setDni(String dni) {
        set("dni", dni);
    }

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    public String getEmail() {
        return getString("email");
    }

    public void setEmail(String email) {
        set("email", email);
    }

    public String getPassword() {
        return getString("password");
    }

    public void setPassword(String password) {
        set("password", password);
    }

    // --- Relación con Roles (Clave Foránea) ---

    public Long getRoleId() {
        return getLong("role_id");
    }

    public void setRoleId(Long roleId) {
        set("role_id", roleId);
    }

    /**
     * no hace falta ... Obtiene el nombre del rol en MAYÚSCULAS basado en el número de role_id. 0 =
     * ADMIN, 1 = TEACHER, 2 = STUDENT
     * 
     * public String getRoleName() { Integer roleId = getInteger("role_id");
     * 
     * if (roleId == null) { return "UNKNOWN"; // Por si hay algún usuario corrupto sin rol }
     * 
     * switch (roleId) { case 0: return "ADMIN"; case 1: return "TEACHER"; case 2: return "STUDENT";
     * default: return "UNKNOWN"; } }
     */


    /**
     * Obtiene el objeto Role asociado a este usuario. ActiveJDBC deduce la relación gracias a la
     * columna 'role_id'.
     */
    public Role getRole() {
        return parent(Role.class);
    }
}
