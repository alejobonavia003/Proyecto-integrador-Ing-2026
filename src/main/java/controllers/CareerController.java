package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.javalite.activejdbc.Model;

import models.Career;
import models.Correlativity;
import models.StudyPlan;
import models.Subject;
import services.CareerService;
import services.CorrelativityService;
import services.SubjectService;
import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class CareerController {

    private static final Logger logger = Logger.getLogger(CareerController.class.getName());
    private static final CareerService careerService = new CareerService();
    private static final SubjectService subjectService = new SubjectService();
    private static final CorrelativityService correlativityService = new CorrelativityService();

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

            List<Model> plans = careerService.getStudyPlansByCareer(id);
            List<Map<String, Object>> plansView = new ArrayList<>();
            
            for (Model p : plans) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", p.getId());
                row.put("name", p.getString("name"));
                row.put("code", p.getString("code"));
                row.put("version", p.getString("version"));
                row.put("career_id", id); 
                plansView.add(row);
            }

            model.put("plans", plansView);
            model.put("hasPlans", !plansView.isEmpty());

            return engine.render(new ModelAndView(model, "career_detail.mustache"));
        });

        /**
         * =====================================================
         * FORMULARIO NUEVO PLAN DE ESTUDIO (POR CARRERA)
         * =====================================================
         */
        get("/admin/careers/:id/plans/new", (req, res) -> {
            Long careerId = Long.parseLong(req.params("id"));
            Career career = careerService.getCareerById(careerId);
            
            if (career == null) {
                res.redirect("/admin/careers");
                return null;
            }
            
            Map<String, Object> model = new HashMap<>();
            model.put("career_id", career.getId());
            model.put("career_name", career.getString("name"));
            
            return engine.render(new ModelAndView(model, "plan_new.mustache"));
        });

        /**
         * =====================================================
         * CREAR PLAN DE ESTUDIO (POR CARRERA)
         * =====================================================
         */
        post("/admin/careers/:id/plans/new", (req, res) -> {
            Long careerId = Long.parseLong(req.params("id"));
            try {
                String name = req.queryParams("name");
                String code = req.queryParams("code");
                String version = req.queryParams("version");

                careerService.createStudyPlan(name, code, version, careerId);
                res.redirect("/admin/careers/" + careerId);

            } catch (IllegalArgumentException e) {
                logger.warning("Error al crear plan: " + e.getMessage());
                Map<String, Object> model = new HashMap<>();
                model.put("errorMessage", "Error al crear el plan. Verifique los datos.");
                model.put("career_id", careerId);
                return engine.render(new ModelAndView(model, "plan_new.mustache"));
            }
            return null;
        });

        /**
         * =====================================================
         * PANTALLA DE GESTIÓN DE MATERIAS DE UN PLAN
         * =====================================================
         */
        get("/admin/careers/:career_id/plans/:plan_id/subjects", (req, res) -> {
            Long careerId = Long.parseLong(req.params("career_id"));
            Long planId = Long.parseLong(req.params("plan_id"));

            Career career = careerService.getCareerById(careerId);
            StudyPlan plan = StudyPlan.findById(planId);

            if (career == null || plan == null) {
                res.redirect("/admin/careers");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("career_id", careerId);
            model.put("career_name", career.getString("name"));
            model.put("plan_id", planId);
            model.put("plan_name", plan.getString("name"));
            model.put("plan_code", plan.getString("code"));

            if ("true".equals(req.queryParams("error"))) {
                model.put("errorMessage", "Ocurrió un error al procesar la materia. Verifique los datos o si el código ya existe.");
            }

            // Obtener TODAS las materias del sistema
            List<Model> allSubjectsFromDb = Subject.findAll();
            
            // Obtener materias asociadas a ESTE plan actualmente
            List<Model> currentPlanSubjects = Subject.where("study_plan_id = ?", planId);
            Set<Long> assignedIds = new HashSet<>();
            List<Map<String, Object>> currentPlanSubjectsView = new ArrayList<>();
            
            for (Model s : currentPlanSubjects) {
                assignedIds.add(Long.parseLong(s.getId().toString()));
                Map<String, Object> cRow = new HashMap<>();
                cRow.put("id", s.getId());
                cRow.put("code", s.getString("code"));
                cRow.put("name", s.getString("name"));
                currentPlanSubjectsView.add(cRow);
            }

            // Mapeo general para la tabla incluyendo sus dependencias
            List<Map<String, Object>> allSubjectsView = new ArrayList<>();
            for (Model subject : allSubjectsFromDb) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", subject.getId());
                row.put("name", subject.getString("name"));
                row.put("code", subject.getString("code"));
                row.put("weekly_hours", subject.get("weekly_hours"));
                row.put("modality", subject.getString("modality"));
                row.put("selected", assignedIds.contains(Long.parseLong(subject.getId().toString())));
                
                // Buscar códigos de las correlativas de la materia actual
                List<Correlativity> corrs = Correlativity.where("subject_id = ?", subject.getId());
                List<String> codes = new ArrayList<>();
                for (Correlativity c : corrs) {
                    Subject reqSub = Subject.findById(c.get("required_subject_id"));
                    if (reqSub != null) {
                        codes.add(reqSub.getString("code"));
                    }
                }
                row.put("dependencies", codes.isEmpty() ? "Ninguna" : String.join(", ", codes));
                
                allSubjectsView.add(row);
            }

            model.put("allSubjects", allSubjectsView);
            model.put("currentPlanSubjects", currentPlanSubjectsView); // Para el combobox del formulario
            return engine.render(new ModelAndView(model, "plan_subjects.mustache"));
        });

        /**
         * =====================================================
         * PROCESAR ASIGNACIÓN DE MATERIAS EXISTENTES
         * =====================================================
         */
        post("/admin/careers/:career_id/plans/:plan_id/subjects/assign", (req, res) -> {
            Long careerId = Long.parseLong(req.params("career_id"));
            Long planId = Long.parseLong(req.params("plan_id"));

            String[] checkedSubjectIds = req.queryParamsValues("subject_ids");
            if (checkedSubjectIds != null) {
                for (String sId : checkedSubjectIds) {
                    Subject subject = Subject.findById(Long.parseLong(sId));
                    if (subject != null) {
                        subject.set("study_plan_id", planId);
                        subject.saveIt();
                    }
                }
            }
            res.redirect("/admin/careers/" + careerId + "/plans/" + planId + "/subjects");
            return null;
        });

        /**
         * =====================================================
         * CREAR NUEVA MATERIA DESDE EL CONTEXTO DEL PLAN
         * =====================================================
         */
        post("/admin/careers/:career_id/plans/:plan_id/subjects/new", (req, res) -> {
            Long careerId = Long.parseLong(req.params("career_id"));
            Long planId = Long.parseLong(req.params("plan_id"));

            try {
                String name = req.queryParams("name");
                String code = req.queryParams("code");
                Integer weeklyHours = Integer.parseInt(req.queryParams("weekly_hours"));
                String modality = req.queryParams("modality"); 

                Subject newSubject = subjectService.createSubject(name, code, weeklyHours, modality, planId);

                // Procesamiento de la materia correlativa seleccionada
                String reqSubIdStr = req.queryParams("required_subject_id");
                if (reqSubIdStr != null && !reqSubIdStr.isEmpty()) {
                    Long requiredSubjectId = Long.parseLong(reqSubIdStr);
                    boolean requiresApproved = req.queryParams("requires_approved") != null;
                    correlativityService.addCorrelativity((Long) newSubject.getId(), requiredSubjectId, requiresApproved);
                }

                res.redirect("/admin/careers/" + careerId + "/plans/" + planId + "/subjects");
            } catch (Exception e) {
                logger.warning("Error al crear la materia desde el plan: " + e.getMessage());
                res.redirect("/admin/careers/" + careerId + "/plans/" + planId + "/subjects?error=true");
            }
            return null;
        });

        /**
         * =====================================================
         * DETALLE DE UN PLAN DE ESTUDIO (LISTAR MATERIAS)
         * =====================================================
         */
        get("/admin/careers/:career_id/plans/:plan_id", (req, res) -> {
            Long careerId = Long.parseLong(req.params("career_id"));
            Long planId = Long.parseLong(req.params("plan_id"));

            StudyPlan plan = StudyPlan.findById(planId);

            if (plan == null) {
                res.redirect("/admin/careers/" + careerId);
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            
            Map<String, Object> planData = new HashMap<>();
            planData.put("name", plan.getString("name"));
            planData.put("code", plan.getString("code"));
            model.put("plan", planData);

            List<Model> subjects = Subject.where("study_plan_id = ?", planId);
            List<Map<String, Object>> coursesView = new ArrayList<>();
            
            for (Model s : subjects) {
                Map<String, Object> row = new HashMap<>();
                row.put("code", s.getString("code"));
                row.put("name", s.getString("name"));
                row.put("weekly_hours", s.get("weekly_hours"));
                
                // Buscar códigos de las correlativas para la vista de detalle
                List<Correlativity> corrs = Correlativity.where("subject_id = ?", s.getId());
                List<String> codes = new ArrayList<>();
                for (Correlativity c : corrs) {
                    Subject reqSub = Subject.findById(c.get("required_subject_id"));
                    if (reqSub != null) {
                        codes.add(reqSub.getString("code"));
                    }
                }
                row.put("dependencies", codes.isEmpty() ? "Ninguna" : String.join(", ", codes));
                
                coursesView.add(row);
            }

            model.put("plan_courses", coursesView);

            return engine.render(new ModelAndView(model, "plan_detalle.mustache"));
        });
    }
}