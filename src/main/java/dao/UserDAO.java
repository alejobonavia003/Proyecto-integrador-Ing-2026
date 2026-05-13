// Archivo: src/main/java/dao/UserDAO.java
package dao;

import config.DBConnection;
import models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Data Access Object (DAO): Capa de persistencia.
 * Su única responsabilidad es traducir filas de la base de datos a objetos Java (User) y viceversa.
 */
public class UserDAO {

    /**
     * Busca un usuario por su nombre en la base de datos.
     * @param name Nombre del usuario a buscar.
     * @return Un Optional que contiene el usuario si se encuentra, o vacío si no.
     */
    public Optional<User> findByName(String name) {
        // Definición de la consulta SQL parametrizada para evitar SQL Injection
        String sql = "SELECT id, name, password FROM users WHERE name = ?";

        // Bloque try-with-resources: Garantiza el cierre automático de Connection y PreparedStatement
        // incluso si ocurre una excepción, evitando fugas de memoria (memory leaks).
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Inyección segura del parámetro
            ps.setString(1, name);

            // Segundo try-with-resources para el ResultSet (el cursor de resultados)
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Mapeo: Transformamos la fila de la DB en un objeto de dominio (User)
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setName(rs.getString("name"));
                    user.setPassword(rs.getString("password"));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            // Conversión de excepción: Transformamos la SQLException (chequeada) en RuntimeException.
            // Esto permite que el Service no esté obligado a manejar errores de infraestructura.
            throw new RuntimeException("Error al buscar usuario por nombre: " + name, e);
        }

        return Optional.empty();
    }

    /**
     * Inserta un nuevo usuario en la base de datos y actualiza el objeto con el ID generado.
     * @param user Objeto usuario con los datos a persistir.
     * @return El mismo objeto User con su ID asignado por la base de datos.
     */
    public User save(User user) {
        String sql = "INSERT INTO users (name, password) VALUES (?, ?)";

        // Solicitamos a JDBC que nos devuelva las llaves generadas automáticamente (ID auto-incremental)
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, user.getName());
            ps.setString(2, user.getPassword());
            ps.executeUpdate();

            // Recuperamos el ID que la base de datos asignó a este nuevo registro
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    // Seteamos el ID en el objeto para que el resto de la app sepa cuál es su identificador único
                    user.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al persistir el usuario: " + user.getName(), e);
        }

        return user;
    }
}