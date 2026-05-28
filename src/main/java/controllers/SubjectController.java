package controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import services.CorrelativityService;
import services.SubjectService;

import spark.ModelAndView;
import spark.Request;

import static spark.Spark.get;
import static spark.Spark.post;

import spark.template.mustache.MustacheTemplateEngine;

public class SubjectController {

    private static final Logger logger =
            Logger.getLogger(SubjectController.class.getName());

    private static final SubjectService subjectService =
            new SubjectService();

    private static final CorrelativityService correlativityService =
            new CorrelativityService();

    public static void init(MustacheTemplateEngine engine) {

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

            logger.info("Cargando listado de materias.");

            Map<String, Object> model =
                    new HashMap<>();

            model.put(
                    "subjects",
                    subjectService.getAllSubjectsView()
            );

            model.put("subjectsActive", true);

            model.put(
                    "username",
                    req.session().attribute("username")
            );

            model.put(
                    "role",
                    req.session().attribute("user_role")
            );

            model.put(
                    "subjectCount",
                    subjectService.getTotalSubjectsCount()
            );

            return engine.render(
                    new ModelAndView(
                            model,
                            "subjects.mustache"
                    )
            );
        });

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
                                req.queryParams("weekly_hours")
                        );

                String modality =
                        req.queryParams("modality");

                Long studyPlanId = null;

                logger.info(
                        "Intentando crear materia: " + name
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

        post("/admin/subjects/:id/correlativities", (req, res) -> {

            Long subjectId =
                    Long.parseLong(req.params("id"));

            Long requiredSubjectId =
                    Long.parseLong(
                            req.queryParams("required_subject_id")
                    );

            boolean requiresApproved =
                    req.queryParams("requires_approved") != null;

            correlativityService.addCorrelativity(
                    subjectId,
                    requiredSubjectId,
                    requiresApproved
            );

            res.redirect("/admin/subjects/" + subjectId);

            return null;
        });
    }

    private static boolean isLoggedIn(Request req) {

        Boolean loggedIn =
                req.session().attribute("loggedIn");

        return loggedIn != null && loggedIn;
    }
}