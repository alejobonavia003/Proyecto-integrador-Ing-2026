package dao;

import models.persona.PersonaConcreta;
import models.persona.Profesor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProfesorDAO {

    public void save(Connection conn, Profesor profesor) throws SQLException {
        String sql = "INSERT INTO profesor (dni) VALUES (?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, profesor.getDni());
            ps.executeUpdate();
        }
    }

    public List<PersonaConcreta> findAll(Connection conn) throws SQLException {
        String sql = """
                SELECT p.dni, p.nombre, p.apellido, p.telefono, p.direccion, p.email
                FROM persona p
                INNER JOIN profesor pr ON pr.dni = p.dni
                ORDER BY p.apellido, p.nombre
                """;



        List<PersonaConcreta> profesores = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PersonaConcreta persona = new PersonaConcreta();
                persona.setDni(rs.getInt("dni"));
                persona.setNombre(rs.getString("nombre"));
                persona.setApellido(rs.getString("apellido"));
                persona.setTelefono(rs.getString("telefono"));
                persona.setDireccion(rs.getString("direccion"));
                persona.setEmail(rs.getString("email"));
                profesores.add(persona);
            }
        }

        return profesores;
    }
}