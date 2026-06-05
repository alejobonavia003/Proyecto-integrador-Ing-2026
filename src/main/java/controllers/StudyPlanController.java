package controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.logging.Logger;

import models.Subject;

import services.CareerService;
import services.CorrelativityService;
import services.SubjectService;
import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class StudyPlanController {

    private static final Logger logger = Logger.getLogger(StudyPlanController.class.getName());
    private static final CareerService careerService = new CareerService();
    private static final SubjectService subjectService = new SubjectService();
    private static final CorrelativityService correlativityService = new CorrelativityService();

    public static void init(MustacheTemplateEngine engine) {

        get("/admin/careers/:id/plans/new", (req, res) -> {
            Long careerId = Long.parseLong(req.params("id"));
            Map<String, Object> career = careerService.getCareerSummary(careerId);
            if (career == null) {
                res.redirect("/admin/careers");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            model.putAll(career);
            return engine.render(new ModelAndView(model, "plan_new.mustache"));
        });

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
                model.putAll(careerService.getCareerSummary(careerId));
                return engine.render(new ModelAndView(model, "plan_new.mustache"));
            }
            return null;
        });

        get("/admin/careers/:career_id/plans/:plan_id/subjects", (req, res) -> {
            Long careerId = Long.parseLong(req.params("career_id"));
            Long planId = Long.parseLong(req.params("plan_id"));

            Map<String, Object> career = careerService.getCareerSummary(careerId);
            Map<String, Object> plan = careerService.getPlanSummary(planId);

            if (career == null || plan == null) {
                res.redirect("/admin/careers");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            model.putAll(career);
            model.putAll(plan);

            if ("true".equals(req.queryParams("error"))) {
                model.put("errorMessage",
                        "Ocurrió un error al procesar la materia. Verifique los datos o si el código ya existe.");
            }

            model.put("allSubjects", subjectService.getAllSubjectsViewForPlan(planId));
            model.put("currentPlanSubjects", subjectService.getPlanSubjectsOptions(planId));

            return engine.render(new ModelAndView(model, "plan_subjects.mustache"));
        });

        post("/admin/careers/:career_id/plans/:plan_id/subjects/assign", (req, res) -> {
            Long careerId = Long.parseLong(req.params("career_id"));
            Long planId = Long.parseLong(req.params("plan_id"));

            String[] checkedSubjectIds = req.queryParamsValues("subject_ids");
            subjectService.assignSubjectsToPlan(planId, checkedSubjectIds);

            res.redirect("/admin/careers/" + careerId + "/plans/" + planId + "/subjects");
            return null;
        });

        post("/admin/careers/:career_id/plans/:plan_id/subjects/new", (req, res) -> {
            Long careerId = Long.parseLong(req.params("career_id"));
            Long planId = Long.parseLong(req.params("plan_id"));

            try {
                String name = req.queryParams("name");
                String code = req.queryParams("code");
                Integer weeklyHours = Integer.parseInt(req.queryParams("weekly_hours"));
                String modality = req.queryParams("modality");

                Long newSubjectId = subjectService.createSubjectReturningId(name, code, weeklyHours,
                        modality, planId);

                String reqSubIdStr = req.queryParams("required_subject_id");
                if (reqSubIdStr != null && !reqSubIdStr.isEmpty()) {
                    Long requiredSubjectId = Long.parseLong(reqSubIdStr);
                    boolean requiresApproved = req.queryParams("requires_approved") != null;
                    correlativityService.addCorrelativity(newSubjectId, requiredSubjectId,
                            requiresApproved);
                }

                res.redirect("/admin/careers/" + careerId + "/plans/" + planId + "/subjects");
            } catch (Exception e) {
                logger.warning("Error al crear la materia desde el plan: " + e.getMessage());
                res.redirect(
                        "/admin/careers/" + careerId + "/plans/" + planId + "/subjects?error=true");
            }
            return null;
        });

        get("/admin/careers/:career_id/plans/:plan_id", (req, res) -> {
            Long careerId = Long.parseLong(req.params("career_id"));
            Long planId = Long.parseLong(req.params("plan_id"));

            Map<String, Object> plan = careerService.getPlanSummary(planId);
            if (plan == null) {
                res.redirect("/admin/careers/" + careerId);
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            model.putAll(plan);
            model.put("plan_courses", subjectService.getPlanCoursesView(planId));

            return engine.render(new ModelAndView(model, "plan_detalle.mustache"));
        });


        get("/admin/plans/:id/students", (req, res) -> {

            Long planId = Long.parseLong(req.params("id"));

            Map<String, Object> plan =
                    careerService.getPlanSummary(planId);

            if (plan == null) {
                res.redirect("/admin/careers");
                return null;
            }

            Map<String, Object> model = new HashMap<>();

            model.putAll(plan);

            List<Map<String, Object>> students =
                    careerService.getStudentsByPlan(planId);

            model.put("students", students);
            model.put("hasStudents", !students.isEmpty());

            return engine.render(
                new ModelAndView(model,
                    "plan_students.mustache")
            );
        });


        get("/admin/subjects/:id/students", (req, res) -> {

            Long subjectId = Long.parseLong(req.params("id"));

            Subject subject = subjectService.getSubjectById(subjectId);

            if (subject == null) {
                res.redirect("/admin/careers");
                return null;
            }

            Map<String, Object> model = new HashMap<>();

            model.put("subject_name", subject.getString("name"));
            model.put("subject_code", subject.getString("code"));

            List<Map<String, Object>> students =
                    subjectService.getStudentsBySubject(subjectId);

            model.put("students", students);
            model.put("hasStudents", !students.isEmpty());

            return engine.render(
                    new ModelAndView(model, "subject_students.mustache"));
        });
    }
}
