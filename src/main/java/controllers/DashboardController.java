package controllers;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import services.CareerService;
import services.SubjectService;
import services.UserService;
import spark.ModelAndView;
import spark.Request;
import static spark.Spark.get;
import spark.template.mustache.MustacheTemplateEngine;

/**
 * Controlador independiente encargado de la distribución y renderizado
 * de los paneles de control (Dashboards) según el rol asignado al usuario.
 */
public class DashboardController {

    private static final Logger logger = Logger.getLogger(DashboardController.class.getName());
    private static final UserService userService = new UserService();   
    private static final CareerService careerService = new CareerService();
    private static final SubjectService subjectService = new SubjectService();
    public static void init(MustacheTemplateEngine engine) {

        /**
         * Ruta de desvío unificada (/dashboard).
         * Analiza el rol en sesión y redirige hacia la sección correspondiente del sistema.
         */
        get("/dashboard", (req, res) -> {
            if (!isLoggedIn(req)) {
                res.redirect("/?error=Debes iniciar sesión para acceder a esta página.");
                return null;
            }

            String role = req.session().attribute("user_role");
            logger.info("Redirigiendo usuario a su panel correspondiente basado en su rol: " + role);

            if ("ADMIN".equals(role)) {
                res.redirect("/admin/dashboard");
            } else if ("TEACHER".equals(role)) {
                res.redirect("/teacher/dashboard");
            } else if ("STUDENT".equals(role)) {
                res.redirect("/student/dashboard");
            } else {
                logger.warning("Usuario posee un rol desconocido o inválido. Forzando logout.");
                res.redirect("/logout");
            }
            return null;
        });
        /**
        * Vista del Panel de Administrador.
        */
        get("/admin/dashboard", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("username", req.session().attribute("username"));
            model.put("role", "Administrador");
            
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

        
        //Vista del Panel de Docentes.
         
        get("/teacher/dashboard", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("username", req.session().attribute("username"));
            model.put("role", "Docente");
            
            return engine.render(new ModelAndView(model, "dashboard_teacher.mustache"));
        });

       /**
         * Vista del Panel de Alumnos (CON DATOS REALES)
         */
        get("/student/dashboard", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("username", req.session().attribute("username"));
            model.put("role", "Alumno");
            
            // Si vienes de inscribirte con éxito
            if (req.queryParams("success") != null) {
                model.put("successMessage", req.queryParams("success"));
            }

            try {
                Long studentId = Long.valueOf(req.session().attribute("userId").toString());
                models.User student = models.User.findById(studentId);

                if (student != null) {
                    Long planId = student.getLong("study_plan_id");
                    
                    // Verificamos si el alumno ya está inscripto en un Plan/Carrera
                    if (planId != null) {
                        model.put("hasCareer", true);
                        models.StudyPlan plan = models.StudyPlan.findById(planId);
                        
                        if (plan != null) {
                            model.put("planName", plan.getString("name"));
                            models.Career career = models.Career.findById(plan.getLong("career_id"));
                            if (career != null) {
                                model.put("careerName", career.getString("name"));
                            }
                        }
                        
                        // Contamos en cuántas materias (comisiones) está inscripto actualmente
                        long count = models.Enrollment.count("student_id = ?", studentId);
                        model.put("enrollmentsCount", count);
                    } else {
                        // No tiene carrera asignada todavía
                        model.put("hasCareer", false);
                    }
                }
            } catch (Exception e) {
                logger.severe("Error al cargar datos del dashboard de alumno: " + e.getMessage());
            }

            return engine.render(new ModelAndView(model, "dashboard_student.mustache"));
        });
    }

    private static boolean isLoggedIn(Request req) {
        Boolean loggedIn = req.session().attribute("loggedIn");
        return loggedIn != null && loggedIn;
    }
}