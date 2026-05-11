package services;

import config.DBConnection;
import dao.PersonaDAO;
import dao.ProfesorDAO;
import models.persona.PersonaConcreta;
import models.persona.Profesor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ProfesorService {

    private final PersonaDAO personaDAO = new PersonaDAO();
    private final ProfesorDAO profesorDAO = new ProfesorDAO();

    public String crearProfesor(String nombre, String apellido, Integer dni, String telefono, String direccion, String email) throws SQLException {
        validateNotBlank(nombre, "Nombre");
        validateNotBlank(apellido, "Apellido");
        validateNotBlank(telefono, "Teléfono");
        validateNotBlank(direccion, "Dirección");
        validateNotBlank(email, "Email");

        if (dni == null) {
            throw new IllegalArgumentException("DNI es requerido.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (personaDAO.findByDni(conn, dni).isPresent()) {
                    throw new IllegalArgumentException("DNI ya existente.");
                }

                if (personaDAO.findByEmail(conn, email).isPresent()) {
                    throw new IllegalArgumentException("EMAIL ya existente.");
                }

                PersonaConcreta persona = new PersonaConcreta();
                persona.setDni(dni);
                persona.setNombre(nombre);
                persona.setApellido(apellido);
                persona.setTelefono(telefono);
                persona.setDireccion(direccion);
                persona.setEmail(email);

                personaDAO.save(conn, persona);

                Profesor profesor = new Profesor();
                profesor.setDni(dni);
                profesorDAO.save(conn, profesor);

                conn.commit();
                return "Profesor " + nombre + " agregado exitosamente!";
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<PersonaConcreta> listarProfesores() throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return profesorDAO.findAll(conn);
        }
    }

    private void validateNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " es requerido.");
        }
    }
}