package controllers;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.CourseClass;
import models.Subject;
import services.CorrelativityService;
import services.EnrollmentService;
import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class EnrollmentStudentController {

        private static final EnrollmentService enrollmentService = new EnrollmentService();

        private static final CorrelativityService correlativityService = new CorrelativityService();

        public static void init(MustacheTemplateEngine engine) {

                get("/student/mis-inscripciones", (req, res) -> {

                        Long studentId = Long.valueOf(req.session().attribute("userId").toString());

                        Map<String, Object> model = new HashMap<>();

                        model.put("username", req.session().attribute("username"));

                        if (req.queryParams("success") != null) {
                                model.put(
                                                "successMessage",
                                                req.queryParams("success"));
                        }

                        if (req.queryParams("error") != null) {
                                model.put(
                                                "errorMessage",
                                                req.queryParams("error"));
                        }

                        model.put(
                                        "inscripciones",
                                        enrollmentService.getStudentEnrollments(studentId));

                        return engine.render(new ModelAndView(model, "mis_inscripciones.mustache"));
                });

                get("/student/inscripciones/nueva", (req, res) -> {

                        Long studentId = Long.valueOf(req.session().attribute("userId").toString());

                        Map<String, Object> model = new HashMap<>();

                        model.put(
                                        "username",
                                        req.session().attribute("username"));

                        if (req.queryParams("error") != null) {

                                model.put(
                                                "errorMessage",
                                                req.queryParams("error"));

                                Object faltantesObj = req.session()
                                                .attribute("errorMateriasFaltantes");

                                if (faltantesObj != null) {

                                        @SuppressWarnings("unchecked")
                                        List<Subject> faltantes = (List<Subject>) faltantesObj;

                                        if (!faltantes.isEmpty()) {

                                                model.put("faltantes", faltantes);
                                                model.put("hasFaltantes", true);
                                        }

                                        req.session()
                                                        .removeAttribute("errorMateriasFaltantes");
                                }
                        }

                        model.put(
                                        "materiasDisponibles",
                                        enrollmentService
                                                        .getAvailableSubjectsForStudent(studentId));

                        return engine.render(
                                        new ModelAndView(
                                                        model,
                                                        "inscripcion_form.mustache"));
                });

                get("/student/inscripciones/comprobante", (req, res) -> {

                        Map<String, Object> model = new HashMap<>();

                        model.put(
                                        "username",
                                        req.session().attribute("username"));

                        Long receiptId = Long.parseLong(
                                        req.queryParams("id"));

                        model.put(
                                        "receipt",
                                        enrollmentService.getEnrollmentReceipt(receiptId));

                        return engine.render(
                                        new ModelAndView(
                                                        model,
                                                        "comprobante_inscripcion.mustache"));
                });

                get("/student/elegir-carrera", (req, res) -> {

                        Map<String, Object> model = new HashMap<>();

                        model.put(
                                        "username",
                                        req.session().attribute("username"));

                        if (req.queryParams("error") != null) {

                                model.put(
                                                "errorMessage",
                                                req.queryParams("error"));
                        }

                        model.put(
                                        "planes",
                                        enrollmentService.getAllStudyPlans());

                        return engine.render(
                                        new ModelAndView(
                                                        model,
                                                        "elegir_carrera.mustache"));
                });

                post("/student/carrera/confirmar", (req, res) -> {

                        try {

                                Long studentId = Long.valueOf(req.session().attribute("userId").toString());

                                Long planId = Long.parseLong(req.queryParams("plan_id"));

                                enrollmentService.assignPlanToStudent(
                                                studentId,
                                                planId);

                                res.redirect(
                                                "/student/dashboard?success="
                                                                + URLEncoder.encode(
                                                                                "Te has inscripto en la carrera exitosamente.",
                                                                                "UTF-8"));

                        } catch (Exception e) {

                                res.redirect(
                                                "/student/elegir-carrera?error="
                                                                + URLEncoder.encode(
                                                                                "Error al asignar la carrera.",
                                                                                "UTF-8"));
                        }

                        return null;
                });

                post("/student/enroll", (req, res) -> {

                        Long studentId = req.session().attribute("userId");

                        Long courseClassId = Long.parseLong(
                                        req.queryParams("course_class_id"));

                        CourseClass comision = CourseClass.findById(courseClassId);

                        Long subjectId = comision.getLong("subject_id");

                        List<Subject> faltantes = correlativityService.verificarCorrelativas(
                                        studentId,
                                        subjectId);

                        if (!faltantes.isEmpty()) {

                                req.session().attribute(
                                                "errorMateriasFaltantes",
                                                faltantes);

                                res.redirect(
                                                "/student/enroll?error=No cumples con las correlativas");

                                return null;
                        }

                        return null;
                });

                post("/student/inscripciones/confirmar", (req, res) -> {

                        try {

                                Long studentId = Long.valueOf(req.session().attribute("userId").toString());

                                Long subjectId = Long.parseLong(
                                                req.queryParams("subject_id"));

                                Long courseClassId = Long.parseLong(
                                                req.queryParams("course_class_id"));

                                if (!enrollmentService.belongsToStudentCareer(
                                                studentId,
                                                subjectId)) {

                                        res.redirect(
                                                        "/student/inscripciones/nueva?error="
                                                                        + URLEncoder.encode(
                                                                                        "Esta materia no pertenece a tu plan de estudios.",
                                                                                        "UTF-8"));

                                        return null;
                                }

                                List<Subject> faltantes = correlativityService
                                                .verificarCorrelativas(
                                                                studentId,
                                                                subjectId);

                                if (!faltantes.isEmpty()) {

                                        req.session().attribute(
                                                        "errorMateriasFaltantes",
                                                        faltantes);

                                        res.redirect(
                                                        "/student/inscripciones/nueva?error="
                                                                        + URLEncoder.encode(
                                                                                        "No cumples con las correlativas necesarias.",
                                                                                        "UTF-8"));

                                        return null;
                                }

                                enrollmentService.enrollStudent(
                                                studentId,
                                                courseClassId);

                                Long receiptId = enrollmentService.getLastEnrollmentId(
                                                studentId,
                                                courseClassId);

                                res.redirect(
                                                "/student/inscripciones/comprobante?id="
                                                                + receiptId);

                        } catch (IllegalStateException e) {

                                res.redirect(
                                                "/student/inscripciones/nueva?error="
                                                                + URLEncoder.encode(
                                                                                e.getMessage(),
                                                                                "UTF-8"));

                        } catch (Exception e) {

                                res.redirect(
                                                "/student/inscripciones/nueva?error="
                                                                + URLEncoder.encode(
                                                                                "Error interno al procesar tu solicitud.",
                                                                                "UTF-8"));
                        }

                        return null;
                });
        }
}