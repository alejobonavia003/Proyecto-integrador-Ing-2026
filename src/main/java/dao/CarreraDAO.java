package dao;

import java.util.List;
import java.util.Optional; // <-- Solo necesitamos esta excepción ahora

import org.javalite.activejdbc.DBException;

import models.Carrera;

public class CarreraDAO {

    public Optional<Carrera> findByCodigo(String codigo) {
        try {
            Carrera carrera = Carrera.findFirst("codigo_materia = ?", codigo);
            return Optional.ofNullable(carrera);
        } catch (DBException e) {
            System.err.println("Error de base de datos al buscar la carrera: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Carrera> getAll() {
        try {
            return Carrera.findAll();
        } catch (DBException e) {
            System.err.println("Error al obtener las carreras: " + e.getMessage());
            return List.of(); 
        }
    }

    public boolean save(Carrera carrera) {
        try {
            // Usamos save() en lugar de saveIt()
            boolean guardadoExitoso = carrera.save();
            
            if (guardadoExitoso) {
                return true; // Se guardó correctamente
            } else {
                // Si save() devuelve false, significa que falló una validación del Model
                System.err.println("Error de validación al guardar la carrera: " + carrera.errors());
                return false;
            }
        } catch (DBException e) {
            // Esto atrapa errores a nivel de Base de Datos (ej: si intentas guardar un código duplicado)
            System.err.println("Error en la base de datos: " + e.getMessage());
            return false;
        }
    }
}