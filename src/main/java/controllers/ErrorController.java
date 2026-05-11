package controllers;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;

public class ErrorController {

    public static void init(MustacheTemplateEngine engine) {
        get("/error", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            } else {
                model.put("errorMessage", "Ocurrió un error inesperado.");
            }

            return new ModelAndView(model, "error.mustache");
        }, engine);
    }
}