package controllers;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import services.FinalExamService;
import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class FinalExamController {

    public static void init(MustacheTemplateEngine engine) {
        FinalExamService finalExamService = new FinalExamService();

        // [ADMIN] POST para crear el final
        post("/admin/exams/create", (req, res) -> {
            Long subjectId = Long.parseLong(req.queryParams("subject_id"));
            String registrationStart = req.queryParams("registration_start");
            String registrationEnd = req.queryParams("registration_end");
            String examDate = req.queryParams("exam_date"); // Ej: 2026-12-15T10:00

            try {
                finalExamService.createExamInstance(subjectId, registrationStart, registrationEnd, examDate);
                res.redirect("/admin/dashboard?success=" + java.net.URLEncoder.encode("Mesa creada correctamente.", "UTF-8"));
            } catch (Exception e) {
                String raw = e.getMessage() != null ? e.getMessage() : "";
                String msg;
                if (e instanceof IllegalStateException) {
                    // Errores de negocio (por ejemplo: no existe titular)
                    msg = raw;
                } else if (raw.toLowerCase().contains("no such table") || raw.toLowerCase().contains("sqlite")) {
                    msg = "Error interno: la base de datos no está inicializada correctamente.";
                } else {
                    msg = "No se pudo crear la mesa de examen.";
                }
                res.redirect("/admin/dashboard?error=" + java.net.URLEncoder.encode(msg, "UTF-8"));
            }
            return null;
        });

        // [STUDENT] POST para inscribirse
        post("/student/exams/enroll", (req, res) -> {
            Long studentId = Long.valueOf(req.session().attribute("userId").toString()); // Asumiendo que el ID está en sesión
            Long finalExamId = Long.parseLong(req.queryParams("final_exam_id"));
            
            try {
                finalExamService.enrollStudentInFinal(studentId, finalExamId);
                res.redirect("/student/dashboard?success=inscripto");
            } catch (Exception e) {
                res.redirect("/student/dashboard?error=" + URLEncoder.encode(e.getMessage(), "UTF-8"));
            }
            return null;
        });

        // [TEACHER] GET para ver listado
        get("/teacher/exams/:exam_id/students", (req, res) -> {
            Long teacherId = Long.valueOf(req.session().attribute("userId").toString());
            Long examId = Long.parseLong(req.params(":exam_id"));
            
            Map<String, Object> model = new HashMap<>();
            model.put("exam_id", examId);
            model.put("students", finalExamService.getEnrolledStudentsForTeacher(teacherId, examId));
            
            return engine.render(new ModelAndView(model, "teacher_exam_students.mustache"));
        });

        // [TEACHER] POST para cargar el resultado del alumno
        post("/teacher/exams/:exam_id/students/:enrollment_id/result", (req, res) -> {
            Long teacherId = Long.valueOf(req.session().attribute("userId").toString());
            Long examId = Long.parseLong(req.params(":exam_id"));
            Long enrollmentId = Long.parseLong(req.params(":enrollment_id"));
            String gradeStr = req.queryParams("grade");
            String status = req.queryParams("status");
            Double grade = null;
            if (gradeStr != null && !gradeStr.trim().isEmpty()) {
                grade = Double.parseDouble(gradeStr);
            }

            try {
                finalExamService.loadFinalResult(teacherId, enrollmentId, grade, status);
                res.redirect("/teacher/exams/" + examId + "/students?success=resultados_cargados");
            } catch (Exception e) {
                res.redirect("/teacher/exams/" + examId + "/students?error=" + URLEncoder.encode(e.getMessage(), "UTF-8"));
            }
            return null;
        });

        // [ADMIN] GET para mostrar el formulario de creación de finales
        get("/admin/exams/new", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            // Obtenemos todas las materias para mostrarlas en el <select>
            model.put("subjects", models.Subject.findAll()); 
            return engine.render(new ModelAndView(model, "admin_exam_new.mustache"));
        });

        // [STUDENT] GET para ver las mesas disponibles para anotarse y sus inscripciones
        get("/student/exams", (req, res) -> {
            Long studentId = Long.valueOf(req.session().attribute("userId").toString()); // Capturamos el ID del alumno en sesión
            Map<String, Object> model = new HashMap<>();
            
            String error = req.queryParams("error");
            String success = req.queryParams("success");
            if (error != null) model.put("errorMessage", error);
            if (success != null) model.put("successMessage", success);
            
            // Inyectamos las dos listas (Disponibles e Inscriptas)
            model.put("availableExams", finalExamService.getAvailableExamsForStudent(studentId));
            model.put("myExams", finalExamService.getStudentExamEnrollments(studentId));
            
            return engine.render(new ModelAndView(model, "student_exams.mustache"));
        });
        // [TEACHER] GET para ver la lista de mesas de examen y elegir una
        get("/teacher/exams", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            // Le pasamos la lista de exámenes para que el profesor elija
            model.put("exams", finalExamService.getAllExamsView());
            return engine.render(new ModelAndView(model, "teacher_exams_list.mustache"));
        });
    }
}