package controller;

import java.util.HashMap;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

import com.fasterxml.jackson.databind.ObjectMapper;

import models.User;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

public class UsuarioController {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ModelAndView mostrarFormulario(Request req, Response res) {
        Map<String, Object> model = new HashMap<>(); 
        String successMessage = req.queryParams("message");
        if (successMessage != null && !successMessage.isEmpty()) {
            model.put("successMessage", successMessage);
        }
        String errorMessage = req.queryParams("error");
        if (errorMessage != null && !errorMessage.isEmpty()) {
            model.put("errorMessage", errorMessage);
        }
        return new ModelAndView(model, "user_form.mustache");
    }

    public static ModelAndView mostrarFormularioNuevo(Request req, Response res) {
        return new ModelAndView(new HashMap<>(), "user_form.mustache"); 
    }

    public static String procesarNuevoUsuario(Request req, Response res) {
        String name = req.queryParams("name");
        String password = req.queryParams("password");

        if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
            res.status(400); 
            res.redirect("/user/create?error=Nombre y contraseña son requeridos.");
            return ""; 
        }

        try {
            User ac = new User(); 
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            ac.set("name", name); 
            ac.set("password", hashedPassword); 
            ac.saveIt(); 

            res.status(201); 
            res.redirect("/user/create?message=Cuenta creada exitosamente para " + name + "!");
            return ""; 

        } catch (Exception e) {
            System.err.println("Error al registrar la cuenta: " + e.getMessage());
            e.printStackTrace(); 
            res.status(500); 
            res.redirect("/user/create?error=Error interno al crear la cuenta. Intente de nuevo.");
            return ""; 
        }
    }

    public static String apiRegistrarUsuario(Request req, Response res) {
        res.type("application/json"); 
        String name = req.queryParams("name");
        String password = req.queryParams("password");

        if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
            res.status(400); 
            try {
                return objectMapper.writeValueAsString(Map.of("error", "Nombre y contraseña son requeridos."));
            } catch (Exception e) { return ""; }
        }

        try {
            User newUser = new User(); 
            newUser.set("name", name); 
            newUser.set("password", password); 
            newUser.saveIt(); 

            res.status(201); 
            return objectMapper.writeValueAsString(Map.of("message", "Usuario '" + name + "' registrado con éxito.", "id", newUser.getId()));

        } catch (Exception e) {
            System.err.println("Error al registrar usuario: " + e.getMessage());
            e.printStackTrace(); 
            res.status(500); 
            try {
                return objectMapper.writeValueAsString(Map.of("error", "Error interno al registrar usuario: " + e.getMessage()));
            } catch (Exception ex) { return ""; }
        }
    }
}