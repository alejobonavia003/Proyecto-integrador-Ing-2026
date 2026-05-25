package services;

import models.User;
import org.javalite.activejdbc.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio encargado de la lógica de negocio
 * relacionada a usuarios.
 */
public class UserService {

    /**
     * Obtiene todos los usuarios preparados
     * para renderizar en Mustache.
     */
    public List<Map<String, Object>> getAllUsersView() {

        List<Map<String, Object>> usersView =
                new ArrayList<>();

        for (Model user : User.findAll()) {

            Map<String, Object> row =
                    new HashMap<>();

            row.put(
                    "id",
                    user.getId()
            );

            row.put(
                    "dni",
                    user.getString("dni")
            );

            row.put(
                    "name",
                    user.getString("name")
            );

            row.put(
                    "email",
                    user.getString("email")
            );

            row.put(
                    "role_id",
                    user.getLong("role_id")
            );

            usersView.add(row);
        }

        return usersView;
    }
}