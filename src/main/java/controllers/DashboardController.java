package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import services.AssignmentService;
import services.CareerService;
import services.DashboardService;
import services.SubjectService;
import services.UserService;
import spark.ModelAndView;
import spark.Request;
import static spark.Spark.get;
import spark.template.mustache.MustacheTemplateEngine;

public class DashboardController {

    private static final Logger logger = Logger.getLogger(DashboardController.class.getName());

    private static final UserService userService = new UserService();
    private static final CareerService careerService = new CareerService();
    private static final SubjectService subjectService = new SubjectService();
    private static final DashboardService dashboardService = new DashboardService();
    private static final AssignmentService assignmentService = new AssignmentService();

    public static void init(MustacheTemplateEngine engine) {

        get("/dashboard", (req, res) -> {

            if (!isLoggedIn(req)) {
                res.redirect("/?error=Debes iniciar sesión para acceder a esta página.");
                return null;
            }

            String role = req.session().attribute("user_role");

            if ("ADMIN".equals(role)) {
                res.redirect("/admin/dashboard");
            } else if ("TEACHER".equals(role)) {
                res.redirect("/teacher/dashboard");
            } else if ("STUDENT".equals(role)) {
                res.redirect("/student/dashboard");
            } else {
                res.redirect("/logout");
            }

            return null;
        });

        get("/admin/dashboard", (req, res) -> {

            Map<String, Object> model = new HashMap<>();

            String success = req.queryParams("success");
            String error = req.queryParams("error");
            if (success != null) model.put("successMessage", success);
            if (error != null) model.put("errorMessage", error);

            model.put("username", req.session().attribute("username"));
            model.put("role", "Administrador");

            model.put("dashboardActive", true);

            // Obtenemos los totales
            long userCount = userService.getTotalUsersCount();
            long careerCount = careerService.getTotalCareersCount();
            long subjectCount = subjectService.getTotalSubjectsCount();

            // Los pasamos al modelo
            model.put("userCount", userCount);
            model.put("careerCount", careerCount);
            model.put("subjectCount", subjectCount);

            return engine.render(new ModelAndView(model, "dashboard_admin.mustache"));
        });

        get("/teacher/dashboard", (req, res) -> {

            Map<String, Object> model = new HashMap<>();

            String success = req.queryParams("success");
            String error = req.queryParams("error");
            if (success != null) model.put("successMessage", success);
            if (error != null) model.put("errorMessage", error);

            model.put("username", req.session().attribute("username"));
            model.put("role", "Docente");

            try {

                Long teacherId = Long.valueOf(req.session().attribute("userId").toString());

                model.putAll(dashboardService.getTeacherDashboardData(teacherId));

                // Tareas creadas por el docente
                long tasksCount = assignmentService.getAssignmentsByTeacher(teacherId).size();
                model.put("tasksCount", tasksCount);

            } catch (Exception e) {
                logger.severe("Error dashboard docente: " + e.getMessage());
            }

            return engine.render(new ModelAndView(model, "dashboard_teacher.mustache"));
        });

        // ==========================================
        // MÓDULO ACTUALIZADO: CARGAR NOTAS Y SEGUIMIENTO
        // ==========================================
        get("/teacher/grades", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("username", req.session().attribute("username"));
            model.put("role", "Docente");

            try {
                Long teacherId = Long.valueOf(req.session().attribute("userId").toString());
                services.SubmissionService submissionService = new services.SubmissionService();
                
                List<Map<String, Object>> trackingList = submissionService.getStudentsTrackingForTeacher(teacherId);
                
                // Formatear condicionales lógicos para las etiquetas dentro de Mustache
                for (Map<String, Object> entry : trackingList) {
                    String status = entry.get("status_type").toString();
                    entry.put("isFaltaEntregar", "FALTA_ENTREGAR".equals(status));
                    entry.put("isTodaviaNoCorrigio", "TODAVIA_NO_CORRIGIO".equals(status));
                    entry.put("isYaCorrigio", "YA_CORRIGIO".equals(status));
                }
                
                model.put("trackingList", trackingList);
            } catch (Exception e) {
                logger.severe("Error en módulo de seguimiento de notas: " + e.getMessage());
            }

            return engine.render(new ModelAndView(model, "teacher_grades.mustache"));
        });

        get("/student/dashboard", (req, res) -> {

            Map<String, Object> model = new HashMap<>();

            model.put("username", req.session().attribute("username"));
            model.put("role", "Alumno");

            if (req.queryParams("success") != null) {
                model.put("successMessage", req.queryParams("success"));
            }
            if (req.queryParams("error") != null) {
                model.put("errorMessage", req.queryParams("error"));
            }

            try {

                Long studentId = Long.valueOf(req.session().attribute("userId").toString());

                model.putAll(dashboardService.getStudentDashboardData(studentId));

            } catch (Exception e) {
                logger.severe("Error dashboard alumno: " + e.getMessage());
            }

            return engine.render(new ModelAndView(model, "dashboard_student.mustache"));
        });
    }

    private static boolean isLoggedIn(Request req) {
        Boolean loggedIn = req.session().attribute("loggedIn");
        return loggedIn != null && loggedIn;
    }
}