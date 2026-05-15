package dao;

import java.util.Optional;

import org.javalite.activejdbc.DBException;

import models.User;

public class UserDAO {

    public Optional<User> findByName(String name) {
        try {
            // Reemplaza toda la lógica de Connection, PreparedStatement y ResultSet
            User user = User.findFirst("name = ?", name);
            return Optional.ofNullable(user);
        } catch (DBException e) {
            throw new RuntimeException("Error al buscar usuario: " + name, e);
        }
    }

    public User save(User user) {
        try {
            // Reemplaza el INSERT manual y la recuperación de ID (GeneratedKeys)
            user.saveIt(); 
            return user;
        } catch (DBException e) {
            throw new RuntimeException("Error al persistir el usuario: " + user.get("name"), e);
        }
    }
}