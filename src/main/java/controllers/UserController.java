package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import services.AuthService;
import services.UserService;
import spark.ModelAndView;
import spark.Request;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

/**
 * Controlador encargado de la gestión de usuarios por parte del Admin.
 */
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    private static final UserService userService = new UserService();
    private static final AuthService authService = new AuthService();

    public static void init(MustacheTemplateEngine engine) {

        get("/admin/users", (req, res) -> {
            if (!isLoggedIn(req)) {
                res.redirect("/error?type=AuthError&message=Debes iniciar sesión.");
                return null;
            }

            // Capturar el filtro enviado por la URL
            String roleFilter = req.queryParams("roleFilter");

            Map<String, Object> model = new HashMap<>();
            List<Map<String, Object>> usersList = userService.getUsersView(roleFilter);
            
            model.put("users", usersList);
            model.put("username", req.session().attribute("username"));
            model.put("role", req.session().attribute("user_role"));
            model.put("userCount", usersList.size());

            // Mantener seleccionada la opción actual en el combobox de la vista
            if (roleFilter != null) {
                if (roleFilter.equals("2")) model.put("filterProfesor", true);
                if (roleFilter.equals("3")) model.put("filterAlumno", true);
                if (roleFilter.equals("ALL")) model.put("filterAll", true);
            } else {
                model.put("filterAll", true);
            }

            // Atrapamos el éxito de forma segura sin caracteres raros
            if ("true".equals(req.queryParams("success"))) {
                model.put("successMessage", "El usuario ha sido registrado exitosamente.");
            }

            return engine.render(new ModelAndView(model, "users.mustache"));
        });

       // UserController.java - Método GET /admin/users/new
        get("/admin/users/new", (req, res) -> {
        // ... validación de sesión ...
        Map<String, Object> model = new HashMap<>();
        
        if ("true".equals(req.queryParams("error"))) {
                model.put("errorMessage", "Error al crear el usuario. Verifique los datos ingresados."); 
        }

        // Aseguramos que siempre usa la vista de admin
        return engine.render(new ModelAndView(model, "admin_user_form.mustache"));
        });
        post("/admin/users/new", (req, res) -> {
            if (!isLoggedIn(req)) {
                res.redirect("/error?type=AuthError&message=Debes iniciar sesión.");
                return null;
            }

            String name = req.queryParams("name");
            try {
                String password = req.queryParams("password");
                String email = req.queryParams("email");
                String dni = req.queryParams("dni");
                
                Long role = Long.parseLong(req.queryParams("role"));

                authService.registerUser(name, dni, email, password, role);

                logger.log(Level.INFO, "Admin registró nuevo usuario: [DNI: {0}, Nombre: {1}, Rol: {2}]", new Object[]{dni, name, role});
                
                // Enviamos solo 'true' para que el navegador no colapse con los espacios
                res.redirect("/admin/users?success=true");
                return "";
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error al crear usuario desde Admin: {0}", e.getMessage());
                res.redirect("/admin/users/new?error=true");
                return "";
            }
        });
    }

    private static boolean isLoggedIn(Request req) {
        Boolean loggedIn = req.session().attribute("loggedIn");
        return loggedIn != null && loggedIn;
    }
}