package controller;

import java.util.HashMap;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

import models.User;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

public class AuthController {

    public static ModelAndView mostrarLogin(Request req, Response res) {
        Map<String, Object> model = new HashMap<>();
        String errorMessage = req.queryParams("error");
        if (errorMessage != null && !errorMessage.isEmpty()) {
            model.put("errorMessage", errorMessage);
        }
        String successMessage = req.queryParams("message");
        if (successMessage != null && !successMessage.isEmpty()) {
            model.put("successMessage", successMessage);
        }
        return new ModelAndView(model, "login.mustache");
    }

    public static ModelAndView procesarLogin(Request req, Response res) {
        Map<String, Object> model = new HashMap<>();
        String username = req.queryParams("username");
        String plainTextPassword = req.queryParams("password");

        if (username == null || username.isEmpty() || plainTextPassword == null || plainTextPassword.isEmpty()) {
            res.status(400); 
            model.put("errorMessage", "El nombre de usuario y la contraseña son requeridos.");
            return new ModelAndView(model, "login.mustache"); 
        }

        User ac = User.findFirst("name = ?", username);

        if (ac == null) {
            res.status(401); 
            model.put("errorMessage", "Usuario o contraseña incorrectos."); 
            return new ModelAndView(model, "login.mustache"); 
        }

        String storedHashedPassword = ac.getString("password");

        if (BCrypt.checkpw(plainTextPassword, storedHashedPassword)) {
            res.status(200); 
            req.session(true).attribute("currentUserUsername", username); 
            req.session().attribute("userId", ac.getId()); 
            req.session().attribute("loggedIn", true); 

            System.out.println("DEBUG: Login exitoso para la cuenta: " + username);
            System.out.println("DEBUG: ID de Sesión: " + req.session().id());

            model.put("username", username); 
            return new ModelAndView(model, "dashboard.mustache");
        } else {
            res.status(401); 
            System.out.println("DEBUG: Intento de login fallido para: " + username);
            model.put("errorMessage", "Usuario o contraseña incorrectos."); 
            return new ModelAndView(model, "login.mustache"); 
        }
    }

    public static String procesarLogout(Request req, Response res) {
        req.session().invalidate();
        System.out.println("DEBUG: Sesión cerrada. Redirigiendo a /login.");
        res.redirect("/");
        return null;
    }

    public static ModelAndView mostrarDashboard(Request req, Response res) {
        Map<String, Object> model = new HashMap<>(); 
        String currentUsername = req.session().attribute("currentUserUsername");
        Boolean loggedIn = req.session().attribute("loggedIn");

        if (currentUsername == null || loggedIn == null || !loggedIn) {
            System.out.println("DEBUG: Acceso no autorizado a /dashboard. Redirigiendo a /login.");
            res.redirect("/login?error=Debes iniciar sesión para acceder a esta página.");
            return null; 
        }

        model.put("username", currentUsername);
        return new ModelAndView(model, "dashboard.mustache");
    }

    public static ModelAndView mostrarError(Request req, Response res) {
        Map<String, Object> model = new HashMap<>();
        String errorMessage = req.queryParams("error");
        if (errorMessage != null && !errorMessage.isEmpty()) {
            model.put("errorMessage", errorMessage);
        }
        return new ModelAndView(model, "error.mustache");
    }
}