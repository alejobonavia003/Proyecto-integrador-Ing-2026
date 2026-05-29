package controllers;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import services.EnrollmentService;
import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class EnrollmentTeacherController {

        private static final Logger logger =
                        Logger.getLogger(EnrollmentTeacherController.class.getName());

        private static final EnrollmentService enrollmentService = new EnrollmentService();

        public static void init(MustacheTemplateEngine engine) {

                get("/teacher/comisiones", (req, res) -> {

                        Long teacherId = Long.valueOf(req.session().attribute("userId").toString());

                        Map<String, Object> model = new HashMap<>();

                        model.put("username", req.session().attribute("username"));

                        if (req.queryParams("success") != null) {

                                model.put("successMessage", req.queryParams("success"));
                        }

                        if (req.queryParams("error") != null) {

                                model.put("errorMessage", req.queryParams("error"));
                        }

                        model.putAll(enrollmentService.getTeacherCommissionViewData(teacherId));

                        model.putAll(enrollmentService.getFilteredStudents(
                                        req.queryParams("career_id"), req.queryParams("subject_id"),
                                        req.queryParams("course_class_id")));

                        return engine.render(
                                        new ModelAndView(model, "comisiones_gestion.mustache"));
                });

                post("/teacher/comisiones/crear", (req, res) -> {

                        try {

                                Long teacherId = Long.valueOf(
                                                req.session().attribute("userId").toString());

                                Long subjectId = Long.parseLong(req.queryParams("subject_id"));

                                String name = req.queryParams("name");

                                int capacity = Integer.parseInt(req.queryParams("quota"));

                                if (!enrollmentService.isTeacherTitular(teacherId, subjectId)) {

                                        res.redirect("/teacher/comisiones?error=" + URLEncoder
                                                        .encode("No puedes crear comisiones en materias donde no eres Titular.",
                                                                        "UTF-8"));

                                        return null;
                                }

                                enrollmentService.createCommission(teacherId, subjectId, name,
                                                capacity);

                                res.redirect("/teacher/comisiones?success=" + URLEncoder.encode(
                                                "Comisión académica creada exitosamente.",
                                                "UTF-8"));

                        } catch (Exception e) {

                                logger.severe("Error al crear comisión: " + e.getMessage());

                                res.redirect("/teacher/comisiones?error=" + URLEncoder.encode(
                                                "Error al procesar la creación de la comisión.",
                                                "UTF-8"));
                        }

                        return null;
                });
        }
}
