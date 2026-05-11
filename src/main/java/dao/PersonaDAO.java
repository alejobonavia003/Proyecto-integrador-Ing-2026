package dao;

import models.persona.PersonaConcreta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PersonaDAO {

    public void save(Connection conn, PersonaConcreta persona) throws SQLException {
        String sql = "INSERT INTO persona (dni, nombre, apellido, telefono, direccion, email) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, persona.getDni());
            ps.setString(2, persona.getNombre());
            ps.setString(3, persona.getApellido());
            ps.setString(4, persona.getTelefono());
            ps.setString(5, persona.getDireccion());
            ps.setString(6, persona.getEmail());
            ps.executeUpdate();
        }
    }

    public Optional<PersonaConcreta> findByDni(Connection conn, Integer dni) throws SQLException {
        String sql = "SELECT dni, nombre, apellido, telefono, direccion, email FROM persona WHERE dni = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dni);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }

        return Optional.empty();
    }

    public Optional<PersonaConcreta> findByEmail(Connection conn, String email) throws SQLException {
        String sql = "SELECT dni, nombre, apellido, telefono, direccion, email FROM persona WHERE email = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }

        return Optional.empty();
    }

    public List<PersonaConcreta> findAll(Connection conn) throws SQLException {
        String sql = "SELECT dni, nombre, apellido, telefono, direccion, email FROM persona ORDER BY apellido, nombre";
        List<PersonaConcreta> personas = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                personas.add(mapRow(rs));
            }
        }

        return personas;
    }

    private PersonaConcreta mapRow(ResultSet rs) throws SQLException {
        PersonaConcreta persona = new PersonaConcreta();
        persona.setDni(rs.getInt("dni"));
        persona.setNombre(rs.getString("nombre"));
        persona.setApellido(rs.getString("apellido"));
        persona.setTelefono(rs.getString("telefono"));
        persona.setDireccion(rs.getString("direccion"));
        persona.setEmail(rs.getString("email"));
        return persona;
    }
}