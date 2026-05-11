package controllers;

import models.persona.PersonaConcreta;
import services.ProfesorService;
import spark.ModelAndView;
import spark.Request;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

public class ProfesorController {

    private static final ProfesorService profesorService = new ProfesorService();

    public static void init(MustacheTemplateEngine engine) {

        get("/profesor", (req, res) -> {
            if (!isLoggedIn(req)) {
                res.redirect("/?error=Debes iniciar sesión para acceder a esta página.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("username", req.session().attribute("currentUserUsername"));
            return new ModelAndView(model, "profesor.mustache");
        }, engine);

        get("/alta-profesor", (req, res) -> {
            if (!isLoggedIn(req)) {
                res.redirect("/?error=Debes iniciar sesión para acceder a esta página.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("username", req.session().attribute("currentUserUsername"));

            String successMessage = req.queryParams("message");
            String errorMessage = req.queryParams("error");

            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }

            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            return new ModelAndView(model, "alta_profesor.mustache");
        }, engine);

        post("/profesor", (req, res) -> {
            if (!isLoggedIn(req)) {
                res.redirect("/?error=Debes iniciar sesión para acceder a esta página.");
                return "";
            }

            try {
                String nombre = req.queryParams("nombre");
                String apellido = req.queryParams("apellido");
                String dniS = req.queryParams("dni");
                String telefono = req.queryParams("telefono");
                String direccion = req.queryParams("direccion");
                String email = req.queryParams("email");

                Integer dni = Integer.parseInt(dniS);

                String mensaje = profesorService.crearProfesor(
                        nombre, apellido, dni, telefono, direccion, email
                );

                res.redirect("/alta-profesor?message=" + mensaje);
                return "";

            } catch (NumberFormatException e) {
                res.redirect("/alta-profesor?error=El DNI debe ser numérico.");
                return "";
            } catch (IllegalArgumentException e) {
                res.redirect("/alta-profesor?error=" + e.getMessage());
                return "";
            } catch (Exception e) {
                res.redirect("/alta-profesor?error=Error interno al crear el profesor.");
                return "";
            }
        });

        get("/listar-profesores", (req, res) -> {
            if (!isLoggedIn(req)) {
                res.redirect("/?error=Debes iniciar sesión para acceder a esta página.");
                return null;
            }

            try {
                List<PersonaConcreta> profesores = profesorService.listarProfesores();

                Map<String, Object> model = new HashMap<>();
                model.put("profesores", profesores);
                model.put("username", req.session().attribute("currentUserUsername"));

                return new ModelAndView(model, "table_profesor.mustache");

            } catch (Exception e) {
                res.redirect("/error?error=Error al listar profesores.");
                return null;
            }
        }, engine);
    }

    private static boolean isLoggedIn(Request req) {
        Boolean loggedIn = req.session().attribute("loggedIn");
        String username = req.session().attribute("currentUserUsername");
        return loggedIn != null && loggedIn && username != null;
    }
}