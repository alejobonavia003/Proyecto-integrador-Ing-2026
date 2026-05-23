// Archivo: src/main/java/services/AuthService.java
package services;

import models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

// -------------------------------------------------------
// un detalle del servicio es que podriamos inyectarle el dao 
// con esto podriamos luego testear pasandole bocetos de dao con datos falsos para 
// facilitar el teste en produccion 
// -------------------------------------------------------


/**
 * Capa de Servicio: Orquestador de la lógica de negocio para Autenticación.
 * Esta capa actúa como mediadora entre los Controladores (Web) y el DAO (Persistencia).
 */
public class AuthService {


    /**
     * Registra un nuevo usuario aplicando reglas de validación y seguridad.
     * @param name Nombre elegido por el usuario.
     * @param password Contraseña en texto plano (será hasheada).
     * @return El objeto User persistido (con ID generado).
     */
    public User registerUser(String name, String dni, String email, String password, Long role) {
        // Validaciones preventivas: Aseguran que no entren datos vacíos a la lógica de negocio
        validateNotBlank(name, "Nombre");
        validateNotBlank(password, "Contraseña");
        validateNotBlank(dni, "dni");
        validateNotBlank(email, "email");
        

        //objeto que es null si no existe y uso el metodo del modelo para buscar la primera coincidencia
        Optional<User> existing = Optional.ofNullable(User.findFirst("dni = ?", dni));
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese dni.");
        }

        // Creación de la entidad User
        User user = new User();
        user.setName(name);
        user.setDni(dni);
        user.setEmail(email);
        user.setRoleId(role);
        
        // SEGURIDAD: Nunca guardamos la contraseña plana. 
        // BCrypt aplica un 'salt' aleatorio y genera un hash irreversible.
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));


        user.saveIt();
        // Delegamos la persistencia al DAO y retornamos el objeto resultante
        return user;
    }

    /**
     * Valida las credenciales de un usuario.
     * @param username Nombre de usuario ingresado.
     * @param plainPassword Contraseña ingresada en el formulario.
     * @return Un Optional conteniendo el Usuario si es exitoso, o vacío si falla.
     */
    public Optional<User> login(String username, String plainPassword) {
        // Validación de campos requeridos
        validateNotBlank(username, "Nombre de usuario");
        validateNotBlank(plainPassword, "Contraseña");

        // Buscamos al usuario en la base de datos por su nombre (desde el modelo)
        Optional<User> userOpt = Optional.ofNullable(User.findFirst("name = ?", username));

        // Si el usuario no existe, retornamos vacío inmediatamente (Fail-Fast)
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        // Si existe, extraemos el objeto del contenedor Optional
        User user = userOpt.get();

        // SEGURIDAD: Comparamos la clave plana contra el hash guardado en la DB.
        // BCrypt.checkpw se encarga de extraer el salt del hash y validar la coincidencia.
        if (BCrypt.checkpw(plainPassword, user.getPassword())) {
            return Optional.of(user); // Credenciales correctas
        }

        // Si la clave no coincide, retornamos vacío
        return Optional.empty();
    }

    /**
     * metodo privado para validar que no lleguen en blanco las casillas 
     * lo hiice metodo aparte para no tener que repetir el codigo en cada lugar que lo use 
     */
    private void validateNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " es requerido.");
        }
    }
}