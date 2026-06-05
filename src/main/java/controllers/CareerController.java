package controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.logging.Logger;

import services.CareerService;
import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class CareerController {

    private static final Logger logger = Logger.getLogger(CareerController.class.getName());
    private static final CareerService careerService = new CareerService();

    public static void init(MustacheTemplateEngine engine) {

        /**
         * =====================================================
         * LISTADO DE CARRERAS (SOLO ADMIN)ss
         * =====================================================
         */
        get("/admin/careers", (req, res) -> {

            logger.info("Cargando listado de carreras.");

            Map<String, Object> model = new HashMap<>();

            model.put("careers", careerService.getAllCareersView());
            model.put("careersActive", true);

            return engine.render(
                    new ModelAndView(model, "careers_list.mustache")
            );
        });

        /**
         * =====================================================
         * FORMULARIO NUEVA CARRERA (SOLO ADMIN)
         * =====================================================
         */
        get("/admin/careers/new", (req, res) -> {
            return engine.render(new ModelAndView(new HashMap<>(), "career_new.mustache"));
        });

        /**
         * =====================================================
         * CREAR CARRERA (SOLO ADMIN)
         * =====================================================
         */
        post("/admin/careers/new", (req, res) -> {
            try {
                String name = req.queryParams("name");
                String code = req.queryParams("code");
                Integer duration = null;

                if (req.queryParams("duration") != null && !req.queryParams("duration").isEmpty()) {
                    duration = Integer.parseInt(req.queryParams("duration"));
                }

                careerService.createCareer(name, code, duration);
                res.redirect("/admin/careers");

            } catch (IllegalArgumentException e) {
                logger.warning("Error de validación: " + e.getMessage());
                Map<String, Object> model = new HashMap<>();
                model.put("errorMessage", e.getMessage());
                return engine.render(new ModelAndView(model, "career_new.mustache"));
            }
            return null;
        });

        /**
         * =====================================================
         * DETALLE DE CARRERA Y SUS PLANES DE ESTUDIO
         * =====================================================
         */
        get("/admin/careers/:id", (req, res) -> {
            Long careerId = Long.parseLong(req.params("id"));
            Map<String, Object> model = careerService.getCareerDetailView(careerId);

            if (model == null) {
                res.redirect("/admin/careers");
                return null;
            }

            return engine.render(new ModelAndView(model, "career_detail.mustache"));
        });



        get("/admin/careers/:id/students", (req, res) -> {

            Long careerId = Long.parseLong(req.params("id"));

            Map<String, Object> career =
                    careerService.getCareerSummary(careerId);

            if (career == null) {
                res.redirect("/admin/careers");
                return null;
            }

            Map<String, Object> model = new HashMap<>();

            model.putAll(career);

            List<Map<String, Object>> students =
                    careerService.getStudentsByCareer(careerId);

            model.put("students", students);
            model.put("hasStudents", !students.isEmpty());

            return engine.render(
                new ModelAndView(model, "career_students.mustache")
            );
        });
    }

}