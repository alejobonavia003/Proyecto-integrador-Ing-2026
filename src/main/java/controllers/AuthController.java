package controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import models.User;
import services.AuthService;
import spark.ModelAndView;
import spark.Request;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

/**
 * Controlador de autenticación + rutas de acceso inicial.
 * Maneja el ciclo de vida del inicio y cierre de sesión de los usuarios.
 */
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class.getName());
    private static final AuthService authService = new AuthService();

    public static void init(MustacheTemplateEngine engine) {

        get("/", (req, res) -> {
            if (isLoggedIn(req)) {
                res.redirect("/dashboard");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            
            String error = req.queryParams("error");
            if (error != null) {
                model.put("errorMessage", error); 
            }
            
            String success = req.queryParams("successMessage");
            if (success != null) {
                model.put("successMessage", success);
            }

            return new ModelAndView(model, "login.mustache");
        }, engine);

        get("/user/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            
            String error = req.queryParams("error");
            if (error != null) {
                model.put("errorMessage", error); 
            }
            
            String success = req.queryParams("message");
            if (success != null) {
                model.put("successMessage", success);
            }

            return new ModelAndView(model, "user_form.mustache");
        }, engine);

        get("/logout", (req, res) -> {
            String username = req.session().attribute("username");
            logger.log(Level.INFO, "Usuario [{0}] ha cerrado sesión de forma voluntaria.", username);
            
            req.session().invalidate(); 
            res.redirect("/");
            return null;
        });

        post("/user/new", (req, res) -> {
            String name = req.queryParams("name");
            try {
                String password = req.queryParams("password");
                String email = req.queryParams("email");
                String dni = req.queryParams("dni");
                
                // FORZAR EL ROL A 1 (ADMINISTRADOR) DESDE EL BACKEND
                Long role = 1L;

                authService.registerUser(name, dni, email, password, role);

                logger.log(Level.INFO, "Nuevo usuario ADMIN registrado exitosamente: [DNI: {0}, Nombre: {1}]", new Object[]{dni, name});
                res.redirect("/user/create?message=Cuenta Admin creada exitosamente para " + name + "!");
                return "";
            } catch (IllegalArgumentException e) {
                logger.log(Level.WARNING, "Fallo en validación al intentar registrar usuario [{0}]: {1}", new Object[]{name, e.getMessage()});
                res.redirect("/user/create?error=" + e.getMessage());
                return "";
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error crítico interno en registro de usuario: ", e);
                res.redirect("/user/create?error=Error interno al crear la cuenta: " + e.getMessage());
                return "";
            }
        });

        post("/login", (req, res) -> {
            String usernameInput = req.queryParams("username"); 
            try {
                String passwordInput = req.queryParams("password");

                Optional<User> userOpt = authService.login(usernameInput, passwordInput);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    req.session(true).attribute("username", user.getName());
                    req.session().attribute("userId", user.getId());
                    req.session().attribute("loggedIn", true);
                    req.session().attribute("role_id", user.getRoleId());
                    
                    String assignedRole = user.getRole().getName();
                    req.session().attribute("user_role", assignedRole);

                    logger.log(Level.INFO, "Inicio de sesión exitoso. Usuario: [{0}] con Rol: [{1}]", new Object[]{user.getName(), assignedRole});
                    
                    res.redirect("/dashboard");
                    return null;
                }

                logger.log(Level.WARNING, "Intento de inicio de sesión fallido para las credenciales: [{0}]", usernameInput);
                res.redirect("/?error=" + java.net.URLEncoder.encode("Usuario o contraseña incorrectos","UTF-8"));
                return null;

            } catch (IllegalArgumentException e) {
                logger.log(Level.WARNING, "Error de validación en login: {0}", e.getMessage());
                res.redirect("/?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
                return null;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Excepción crítica durante el proceso de login: ", e);
                res.redirect("/error?type=InternalError&message=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
                return null;
            }
        }, engine);
    }

    private static boolean isLoggedIn(Request req) {
        Boolean loggedIn = req.session().attribute("loggedIn");
        return loggedIn != null && loggedIn;
    }
}