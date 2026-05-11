package services;

import config.DBConnection;
import dao.UserDAO;
import models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public User registerUser(String name, String password) throws SQLException {
        validateNotBlank(name, "Nombre");
        validateNotBlank(password, "Contraseña");

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Optional<User> existing = userDAO.findByName(conn, name);
                if (existing.isPresent()) {
                    throw new IllegalArgumentException("Ya existe un usuario con ese nombre.");
                }

                User user = new User();
                user.setName(name);
                user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));

                userDAO.save(conn, user);
                conn.commit();
                return user;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public Optional<User> login(String username, String plainPassword) throws SQLException {
        validateNotBlank(username, "Nombre de usuario");
        validateNotBlank(plainPassword, "Contraseña");

        try (Connection conn = DBConnection.getConnection()) {
            Optional<User> userOpt = userDAO.findByName(conn, username);

            if (userOpt.isEmpty()) {
                return Optional.empty();
            }

            User user = userOpt.get();

            if (BCrypt.checkpw(plainPassword, user.getPassword())) {
                return Optional.of(user);
            }

            return Optional.empty();
        }
    }

    private void validateNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " es requerido.");
        }
    }
}