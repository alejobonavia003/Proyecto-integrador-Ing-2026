package controllers;

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
            String examDate = req.queryParams("exam_date"); // Ej: 2026-12-15 10:00:00
            
            finalExamService.createExamInstance(subjectId, examDate);
            res.redirect("/admin/dashboard?success=examen_creado");
            return null;
        });

        // [STUDENT] POST para inscribirse
        post("/student/exams/enroll", (req, res) -> {
            Long studentId = req.session().attribute("user_id"); // Asumiendo que el ID está en sesión
            Long finalExamId = Long.parseLong(req.queryParams("final_exam_id"));
            
            try {
                finalExamService.enrollStudentInFinal(studentId, finalExamId);
                res.redirect("/student/dashboard?success=inscripto");
            } catch (Exception e) {
                // Si falla la correlatividad, mandamos el mensaje a la vista
                res.redirect("/student/dashboard?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            }
            return null;
        });

        // [TEACHER] GET para ver listado
        get("/teacher/exams/:exam_id/students", (req, res) -> {
            Long teacherId = req.session().attribute("user_id");
            Long examId = Long.parseLong(req.params(":exam_id"));
            
            Map<String, Object> model = new HashMap<>();
            model.put("students", finalExamService.getEnrolledStudentsForTeacher(teacherId, examId));
            
            return engine.render(new ModelAndView(model, "teacher_exam_students.mustache"));
        });


        // [ADMIN] GET para mostrar el formulario de creación de finales
        get("/admin/exams/new", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            // Obtenemos todas las materias para mostrarlas en el <select>
            model.put("subjects", models.Subject.findAll()); 
            return engine.render(new ModelAndView(model, "admin_exam_new.mustache"));
        });


        // [STUDENT] GET para ver las mesas disponibles para anotarse
        get("/student/exams", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            
            // Capturar posibles errores o éxitos que vengan por la URL
            String error = req.queryParams("error");
            String success = req.queryParams("success");
            if (error != null) model.put("errorMessage", error);
            if (success != null) model.put("successMessage", success);
            
            model.put("exams", finalExamService.getAllExamsView());
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