package controllers;

import models.Subject;
import services.SubjectService;
import spark.ModelAndView;
import spark.Request;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.javalite.activejdbc.Model;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Controlador encargado de gestionar las vistas
 * y endpoints relacionados a Materias.
 *
 * IMPORTANTE:
 * Todas las rutas utilizan el prefijo /admin/
 * para que el SecurityController pueda validar
 * automáticamente el acceso por rol.
 */
public class SubjectController {

    private static final Logger logger =
            Logger.getLogger(SubjectController.class.getName());

    /**
     * Servicio encargado de la lógica de negocio.
     */
    private static final SubjectService subjectService =
            new SubjectService();

    public static void init(MustacheTemplateEngine engine) {

/**
 * =====================================================
 * LISTADO DE MATERIAS
 * SOLO ADMIN
 * =====================================================
 */
get("/admin/subjects", (req, res) -> {

    if (!isLoggedIn(req)) {

        logger.warning(
                "Intento de acceso sin sesión al listado de materias."
        );

        res.redirect(
                "/error?type=AuthError&message=Debes iniciar sesión."
        );

        return null;
    }

    logger.info(
            "Cargando listado de materias."
    );

    Map<String, Object> model =
            new HashMap<>();

    // Lista de materias

    List<Map<String, Object>> subjectsView =
        new ArrayList<>();

for (Model subject : Subject.findAll()) {

    Map<String, Object> row =
            new HashMap<>();

    row.put("id", subject.getId());
    row.put("name", subject.getString("name"));
    row.put("code", subject.getString("code"));
    row.put(
            "weekly_hours",
            subject.getInteger("weekly_hours")
    );
    row.put(
            "modality",
            subject.getString("modality")
    );

    subjectsView.add(row);
}

model.put("subjects", subjectsView);

    // Datos de sesión
    model.put(
            "username",
            req.session().attribute("username")
    );

    model.put(
            "role",
            req.session().attribute("user_role")
    );

    // Contador útil para estadísticas simples
    model.put(
            "subjectCount",
            Subject.count()
    );

    return engine.render(
            new ModelAndView(
                    model,
                    "subjects.mustache"
            )
    );
});

        /**
         * =====================================================
         * FORMULARIO NUEVA MATERIA
         * SOLO ADMIN
         * =====================================================
         */
        get("/admin/subjects/new", (req, res) -> {

            if (!isLoggedIn(req)) {

                logger.warning(
                        "Intento de acceso sin sesión al formulario de materia."
                );

                res.redirect(
                        "/error?type=AuthError&message=Debes iniciar sesión."
                );

                return null;
            }

            logger.info(
                    "Renderizando formulario de creación de materia."
            );

            Map<String, Object> model =
                    new HashMap<>();

            model.put(
                    "username",
                    req.session().attribute("username")
            );

            return engine.render(
                    new ModelAndView(
                            model,
                            "subject_new.mustache"
                    )
            );
        });

        /**
         * =====================================================
         * CREAR MATERIA
         * SOLO ADMIN
         * =====================================================
         */
        post("/admin/subjects/new", (req, res) -> {

            if (!isLoggedIn(req)) {

                logger.warning(
                        "Intento de creación de materia sin sesión."
                );

                res.redirect(
                        "/error?type=AuthError&message=Debes iniciar sesión."
                );

                return null;
            }

            try {

                String name =
                        req.queryParams("name");

                String code =
                        req.queryParams("code");

                Integer weeklyHours =
                        Integer.parseInt(
                                req.queryParams(
                                        "weekly_hours"
                                )
                        );

                String modality =
                        req.queryParams("modality");

                Long studyPlanId =
                        Long.parseLong(
                                req.queryParams(
                                        "study_plan_id"
                                )
                        );

                logger.info(
                        "Intentando crear materia: "
                                + name
                );

                subjectService.createSubject(
                        name,
                        code,
                        weeklyHours,
                        modality,
                        studyPlanId
                );

                logger.info(
                        "Materia creada correctamente."
                );

                res.redirect("/dashboard");

            } catch (IllegalArgumentException e) {

                logger.warning(
                        "Error de validación al crear materia: "
                                + e.getMessage()
                );

                Map<String, Object> model =
                        new HashMap<>();

                model.put(
                        "errorMessage",
                        e.getMessage()
                );

                return engine.render(
                        new ModelAndView(
                                model,
                                "subject_new.mustache"
                        )
                );

            } catch (Exception e) {

                logger.severe(
                        "Error inesperado al crear materia: "
                                + e.getMessage()
                );

                res.redirect(
                        "/error?type=DatabaseError&message=No se pudo crear la materia."
                );
            }

            return null;
        });
    }

    /**
     * =====================================================
     * HELPER - VALIDAR SESIÓN
     * =====================================================
     */
    private static boolean isLoggedIn(Request req) {

        Boolean loggedIn =
                req.session().attribute("loggedIn");

        return loggedIn != null && loggedIn;
    }
}