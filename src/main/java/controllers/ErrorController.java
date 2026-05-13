// Archivo: src/main/java/controllers/ErrorController.java
package controllers;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;
import java.util.HashMap;
import java.util.Map;
import static spark.Spark.get;

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
    }
}