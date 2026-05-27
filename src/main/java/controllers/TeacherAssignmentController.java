package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Career;
import models.StudyPlan;
import models.Subject;
import models.TeacherCareer;
import models.TeacherSubject;
import models.User;
import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class TeacherAssignmentController {

    public static void init(MustacheTemplateEngine engine) {

        // 1. Vista principal de gestión
        get("/admin/teacher-assignments", (req, res) -> {
            List<User> teachers = User.where("role_id = ?", 2);
            List<Career> careers = Career.findAll();

            Map<String, Object> model = new HashMap<>();
            model.put("teachers", teachers);
            model.put("careers", careers);
            
            if (req.queryParams("successCareer") != null) model.put("successCareer", true);
            if (req.queryParams("successSubject") != null) model.put("successSubject", true);
            if (req.queryParams("error") != null) model.put("error", req.queryParams("error"));
            
            return new ModelAndView(model, "admin_teacher_assignment.mustache");
        }, engine);

        // 2.NUEVO ENDPOINT: Devuelve las materias filtradas por las carreras del profesor en JSON
        get("/admin/teacher-assignments/allowed-subjects", (req, res) -> {
            String teacherId = req.queryParams("teacher_id");
            res.type("application/json");
            
            if (teacherId == null || teacherId.trim().isEmpty()) {
                return "[]";
            }

            // CORRECCIÓN AQUÍ: Declaramos explícitamente como LazyList de ActiveJDBC
            org.javalite.activejdbc.LazyList<Subject> allowedSubjects = Subject.findBySQL(
                "SELECT s.* FROM subjects s " +
                "INNER JOIN study_plans sp ON s.study_plan_id = sp.id " +
                "INNER JOIN teacher_careers tc ON sp.career_id = tc.career_id " +
                "WHERE tc.teacher_id = ?", 
                teacherId
            );

            // Ahora el método toJson() funcionará perfectamente
            return allowedSubjects.toJson(false, "id", "name", "code");
        });
        // 3. POST: Asignar Docente a Carrera
        post("/admin/teacher-assignments/career", (req, res) -> {
            String teacherId = req.queryParams("teacher_id");
            String careerId = req.queryParams("career_id");

            try {
                long exist = TeacherCareer.count("teacher_id = ? AND career_id = ?", teacherId, careerId);
                if(exist > 0) {
                    res.redirect("/admin/teacher-assignments?error=" + java.net.URLEncoder.encode("El docente ya está asignado a esta carrera.", "UTF-8"));
                    return null;
                }

                TeacherCareer tc = new TeacherCareer();
                tc.set("teacher_id", teacherId);
                tc.set("career_id", careerId);
                
                if (tc.saveIt()) {
                    res.redirect("/admin/teacher-assignments?successCareer=true");
                } else {
                    res.redirect("/admin/teacher-assignments?error=ValidationError");
                }
            } catch (Exception e) {
                res.redirect("/admin/teacher-assignments?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            }
            return null;
        });

        // 4. POST: Asignar Docente a Materia
        post("/admin/teacher-assignments/subject", (req, res) -> {
            String teacherId = req.queryParams("teacher_id");
            String subjectId = req.queryParams("subject_id");
            String roleCharge = req.queryParams("role_charge");
            String academicYear = req.queryParams("academic_year");
            String academicPeriod = req.queryParams("academic_period");

            try {
                Subject subject = Subject.findById(subjectId);
                if (subject == null || subject.get("study_plan_id") == null) {
                    res.redirect("/admin/teacher-assignments?error=" + java.net.URLEncoder.encode("La materia seleccionada no es válida.", "UTF-8"));
                    return null;
                }

                StudyPlan plan = StudyPlan.findById(subject.get("study_plan_id"));
                String careerId = plan.getString("career_id");

                // Doble chequeo de seguridad en el Backend
                long isEnrolledInCareer = TeacherCareer.count("teacher_id = ? AND career_id = ?", teacherId, careerId);
                if (isEnrolledInCareer == 0) {
                    res.redirect("/admin/teacher-assignments?error=" + java.net.URLEncoder.encode("Acción denegada: El docente no pertenece a la carrera de esta materia.", "UTF-8"));
                    return null;
                }

                TeacherSubject ts = new TeacherSubject();
                ts.set("teacher_id", teacherId);
                ts.set("subject_id", subjectId);
                ts.set("role_charge", roleCharge);
                ts.set("academic_year", academicYear);
                ts.set("academic_period", academicPeriod);
                
                if (ts.saveIt()) {
                    res.redirect("/admin/teacher-assignments?successSubject=true");
                } else {
                    res.redirect("/admin/teacher-assignments?error=ValidationError");
                }
            } catch (Exception e) {
                res.redirect("/admin/teacher-assignments?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            }
            return null;
        });
    }
}