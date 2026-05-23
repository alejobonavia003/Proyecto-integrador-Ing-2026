package controllers;

import static spark.Spark.before;
import static spark.Spark.halt;

import java.util.logging.Logger;

public class SecurityController {

    private static final Logger log = Logger.getLogger(SecurityController.class.getName());

    public static void init() {

        

        // 1. Proteger de forma general todo el sistema (excepto login y recursos públicos)
        before("/*", (req, res) -> {
            String path = req.pathInfo();

            // Permitir explícitamente rutas públicas de autenticación y archivos estáticos
            if (path.equals("/user/create") ||
                path.equals("/") || 
                path.equals("/login") ||
                path.equals("/user/new") || 
                path.equals("/dashboard") || 
                path.equals("/logout") || 
                path.startsWith("/public/")) {
                    log.info("Acceso a ruta publica ");
                return; 
            }

            // Verificar si hay un usuario logueado en la sesión
            Object userId = req.session().attribute("userId");
            if (userId == null) {
                log.warning("Un usuario hizo un intento de acceder a una ruta privada sin una session");
                res.redirect("/");
                halt();
            }
        });

        // 2. Filtro restrictivo para la sección de Administradores (Fase 1 y 2)
        // Protege la creación de carreras, planes de estudio, usuarios y edición de materias
        before("/admin/*", (req, res) -> {
            String roleName = req.session().attribute("user_role");
            
            if (!"ADMIN".equals(roleName)) {
                res.redirect("/error/403"); // O redirigir a una página de acceso denegado
                halt();
            }
        });

        // 3. Filtro restrictivo para la sección de Docentes (Fase 3 y 4)
        // Protege la carga de notas, ver alumnos y creación de tareas
        before("/teacher/*", (req, res) -> {
            String roleName = req.session().attribute("user_role");
            
            if (!"TEACHER".equals(roleName)) {
                res.redirect("/error/403");
                halt();
            }
        });

        // 4. Filtro restrictivo para la sección de Alumnos (Fase 2, 3 y 4)
        // Protege inscripciones, entregas de tareas y visualización de notas
        before("/student/*", (req, res) -> {
            String roleName = req.session().attribute("user_role");
            
            if (!"STUDENT".equals(roleName)) {
                res.redirect("/error/403");
                halt();
            }
        });
    }
}