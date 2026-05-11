package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.User;
import services.AuthService;
import spark.ModelAndView;
import spark.Request;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static spark.Spark.get;
import static spark.Spark.post;

public class AuthController {

    private static final AuthService authService = new AuthService();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void init(MustacheTemplateEngine engine) {

        get("/", (req, res) -> renderLogin(req), engine);

        get("/user/create", (req, res) -> renderUserForm(req), engine);

        get("/user/new", (req, res) -> renderUserForm(req), engine);

        get("/dashboard", (req, res) -> {
            if (!isLoggedIn(req)) {
                res.redirect("/?error=Debes iniciar sesión para acceder a esta página.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("username", req.session().attribute("currentUserUsername"));
            return new ModelAndView(model, "dashboard.mustache");
        }, engine);

        get("/logout", (req, res) -> {
            req.session().invalidate();
            res.redirect("/");
            return null;
        });

        post("/user/new", (req, res) -> {
            try {
                String name = req.queryParams("name");
                String password = req.queryParams("password");

                authService.registerUser(name, password);

                res.redirect("/user/create?message=Cuenta creada exitosamente para " + name + "!");
                return "";
            } catch (IllegalArgumentException e) {
                res.redirect("/user/create?error=" + e.getMessage());
                return "";
            } catch (Exception e) {
                res.redirect("/user/create?error=Error interno al crear la cuenta.");
                return "";
            }
        });

        post("/login", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            try {
                String username = req.queryParams("username");
                String password = req.queryParams("password");

                Optional<User> userOpt = authService.login(username, password);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    req.session(true).attribute("currentUserUsername", user.getName());
                    req.session().attribute("userId", user.getId());
                    req.session().attribute("loggedIn", true);

                    model.put("username", user.getName());
                    return new ModelAndView(model, "dashboard.mustache");
                }

                res.status(401);
                model.put("errorMessage", "Usuario o contraseña incorrectos.");
                return new ModelAndView(model, "login.mustache");

            } catch (IllegalArgumentException e) {
                res.status(400);
                model.put("errorMessage", e.getMessage());
                return new ModelAndView(model, "login.mustache");
            } catch (Exception e) {
                res.status(500);
                model.put("errorMessage", "Error interno del servidor.");
                return new ModelAndView(model, "login.mustache");
            }
        }, engine);

        post("/add_users", (req, res) -> {
            res.type("application/json");

            Map<String, Object> response = new HashMap<>();

            try {
                String name = req.queryParams("name");
                String password = req.queryParams("password");

                User user = authService.registerUser(name, password);

                res.status(201);
                response.put("message", "Usuario '" + user.getName() + "' registrado con éxito.");
                response.put("id", user.getId());
                return objectMapper.writeValueAsString(response);

            } catch (IllegalArgumentException e) {
                res.status(400);
                response.put("error", e.getMessage());
                return objectMapper.writeValueAsString(response);

            } catch (Exception e) {
                res.status(500);
                response.put("error", "Error interno al registrar usuario.");
                return objectMapper.writeValueAsString(response);
            }
        });
    }

    private static ModelAndView renderLogin(Request req) {
        Map<String, Object> model = new HashMap<>();

        String errorMessage = req.queryParams("error");
        String successMessage = req.queryParams("message");

        if (errorMessage != null && !errorMessage.isEmpty()) {
            model.put("errorMessage", errorMessage);
        }

        if (successMessage != null && !successMessage.isEmpty()) {
            model.put("successMessage", successMessage);
        }

        return new ModelAndView(model, "login.mustache");
    }

    private static ModelAndView renderUserForm(Request req) {
        Map<String, Object> model = new HashMap<>();

        String errorMessage = req.queryParams("error");
        String successMessage = req.queryParams("message");

        if (errorMessage != null && !errorMessage.isEmpty()) {
            model.put("errorMessage", errorMessage);
        }

        if (successMessage != null && !successMessage.isEmpty()) {
            model.put("successMessage", successMessage);
        }

        return new ModelAndView(model, "user_form.mustache");
    }

    private static boolean isLoggedIn(Request req) {
        Boolean loggedIn = req.session().attribute("loggedIn");
        String username = req.session().attribute("currentUserUsername");
        return loggedIn != null && loggedIn && username != null;
    }
}