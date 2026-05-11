package dao;

import models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UserDAO {

    public Optional<User> findByName(Connection conn, String name) throws SQLException {
        String sql = "SELECT id, name, password FROM users WHERE name = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setName(rs.getString("name"));
                    user.setPassword(rs.getString("password"));
                    return Optional.of(user);
                }
            }
        }

        return Optional.empty();
    }

    public User save(Connection conn, User user) throws SQLException {
        String sql = "INSERT INTO users (name, password) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getPassword());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
        }

        return user;
    }
}