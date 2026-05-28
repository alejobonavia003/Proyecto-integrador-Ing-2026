package controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import models.Career;

import java.util.List;
import java.util.ArrayList;

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

            List<Map<String, Object>> careersView = new ArrayList<>();
            for (Career career : careerService.getAllCareers()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", career.getId());
                row.put("name", career.getString("name"));
                row.put("code", career.getString("code"));
                row.put("duration", career.getInteger("duration"));
                careersView.add(row);
            }

            model.put("careers", careersView);
            model.put("careersActive",true);
            return engine.render(new ModelAndView(model, "careers_list.mustache"));
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
    }
}