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

            model.put("dashboardActive",true);
            
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
         * Vista del Panel de Alumnos.
         */
        get("/student/dashboard", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("username", req.session().attribute("username"));
            model.put("role", "Alumno");
            
            return engine.render(new ModelAndView(model, "dashboard_student.mustache"));
        });
    }

    private static boolean isLoggedIn(Request req) {
        Boolean loggedIn = req.session().attribute("loggedIn");
        return loggedIn != null && loggedIn;
    }
}