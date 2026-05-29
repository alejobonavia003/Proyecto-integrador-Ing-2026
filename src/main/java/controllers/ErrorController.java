// Archivo: src/main/java/controllers/ErrorController.java
package controllers;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import static spark.Spark.get;
import spark.template.mustache.MustacheTemplateEngine;

public class ErrorController {

    public static void init(MustacheTemplateEngine engine) {

        // Esta ruta servirá para mostrar errores específicos en una página dedicada
        get("/error", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            String message = req.queryParams("message");
            String type = req.queryParams("type"); // Ej: "DatabaseError", "AuthError"

            model.put("errorMessage", message != null ? message : "Ocurrió un error inesperado.");
            model.put("errorType", type != null ? type : "Error General");

            // Podemos pasar un flag para mostrar un botón de "Volver atrás"
            model.put("showRetry", true);

            return new ModelAndView(model, "error.mustache");
        }, engine);

        // NUEVA RUTA: Manejo específico para el Error 403 (Acceso Denegado)
        get("/error/403", (req, res) -> {
            // Es buena práctica establecer el código HTTP real de error
            res.status(403);

            Map<String, Object> model = new HashMap<>();
            model.put("errorType", "403 - Acceso Denegado");
            model.put("errorMessage",
                    "No tienes los permisos necesarios para acceder a esta sección. Si crees que esto es un error, contacta al administrador.");

            // Renderizamos una plantilla específica para el 403
            return new ModelAndView(model, "error_403.mustache");
        }, engine);
    }
}
