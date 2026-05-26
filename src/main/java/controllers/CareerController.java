package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.javalite.activejdbc.Model;

import models.Career;
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
         * LISTADO DE CARRERAS (SOLO ADMIN)
         * =====================================================
         */
        get("/admin/careers", (req, res) -> {
            logger.info("Cargando listado de carreras.");
            Map<String, Object> model = new HashMap<>();

            List<Map<String, Object>> careersView = new ArrayList<>();
            for (Model career : careerService.getAllCareers()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", career.getId());
                row.put("name", career.getString("name"));
                row.put("code", career.getString("code"));
                row.put("duration", career.getInteger("duration"));
                careersView.add(row);
            }

            model.put("careers", careersView);
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
            Long id = Long.parseLong(req.params("id"));
            Career career = careerService.getCareerById(id);

            if (career == null) {
                res.redirect("/admin/careers");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("career_id", career.getId());
            model.put("career_name", career.getString("name"));
            model.put("career_code", career.getString("code"));

            // Obtener planes de estudio de esta carrera
            List<Model> plans = careerService.getStudyPlansByCareer(id);
            List<Map<String, Object>> plansView = new ArrayList<>();
            
            for (Model p : plans) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", p.getId());
                row.put("name", p.getString("name"));
                row.put("code", p.getString("code"));
                row.put("version", p.getString("version"));
                plansView.add(row);
            }

            model.put("plans", plansView);
            model.put("hasPlans", !plansView.isEmpty()); // Booleano para saber si tiene planes

            return engine.render(new ModelAndView(model, "career_detail.mustache"));
        });
    }
}