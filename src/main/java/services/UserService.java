package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Model;

import models.User;

/**
 * Servicio encargado de la lógica de negocio
 * relacionada a usuarios.
 */
public class UserService {

    /**
     * Obtiene los usuarios preparados para renderizar en Mustache,
     * aplicando un filtro por rol si es especificado.
     */
    public List<Map<String, Object>> getUsersView(String roleFilter) {

        List<Map<String, Object>> usersView = new ArrayList<>();
        List<Model> users;

        // Si elige un rol específico, lo filtramos estrictamente
        if (roleFilter != null && (roleFilter.equals("2") || roleFilter.equals("3"))) {
            users = User.where("role_id = ?", Integer.parseInt(roleFilter));
        } else {
            // Si es "ALL" o nulo, traemos solo alumnos y profesores (Ocultamos a los Admin ID 1)
            users = User.where("role_id IN (2, 3)"); 
        }

        for (Model user : users) {
            Map<String, Object> row = new HashMap<>();

            row.put("id", user.getId());
            row.put("dni", user.getString("dni"));
            row.put("name", user.getString("name"));
            row.put("email", user.getString("email"));
            
            Long roleId = user.getLong("role_id");
            row.put("role_id", roleId);
            
            String roleName = "Administrador";
            if (roleId == 2L) roleName = "Docente";
            if (roleId == 3L) roleName = "Alumno";
            row.put("role_name", roleName);

            usersView.add(row);
        }

        return usersView;

        
    }
    
    /**
     * Obtiene la cantidad total de usuarios registrados en el sistema.
     */
    public long getTotalUsersCount() {
        // User.count() es un método de ActiveJDBC que devuelve el total de registros en la tabla
        return User.count(); 
    }
}