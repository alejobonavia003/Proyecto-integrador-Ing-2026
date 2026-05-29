package controllers;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import services.TeacherAssignmentService;

import spark.ModelAndView;

import static spark.Spark.get;
import static spark.Spark.post;

import spark.template.mustache.MustacheTemplateEngine;

public class TeacherAssignmentController {

        private static final TeacherAssignmentService teacherAssignmentService =
                        new TeacherAssignmentService();

        public static void init(MustacheTemplateEngine engine) {

                get("/admin/teacher-assignments", (req, res) -> {

                        Map<String, Object> model = new HashMap<>();

                        model.putAll(teacherAssignmentService.getTeacherAssignmentViewData());

                        if (req.queryParams("successCareer") != null) {
                                model.put("successCareer", true);
                        }

                        if (req.queryParams("successSubject") != null) {
                                model.put("successSubject", true);
                        }

                        if (req.queryParams("error") != null) {
                                model.put("error", req.queryParams("error"));
                        }

                        return engine.render(new ModelAndView(model,
                                        "admin_teacher_assignment.mustache"));
                });

                get("/admin/teacher-assignments/allowed-subjects", (req, res) -> {

                        String teacherId = req.queryParams("teacher_id");

                        res.type("application/json");

                        return teacherAssignmentService.getAllowedSubjectsJson(teacherId);
                });

                post("/admin/teacher-assignments/career", (req, res) -> {

                        try {

                                Long teacherId = Long.parseLong(req.queryParams("teacher_id"));

                                Long careerId = Long.parseLong(req.queryParams("career_id"));

                                teacherAssignmentService.assignTeacherToCareer(teacherId, careerId);

                                res.redirect("/admin/teacher-assignments?successCareer=true");

                        } catch (IllegalArgumentException e) {

                                res.redirect("/admin/teacher-assignments?error="
                                                + URLEncoder.encode(e.getMessage(), "UTF-8"));

                        } catch (Exception e) {

                                res.redirect("/admin/teacher-assignments?error=" + URLEncoder
                                                .encode("Error al asignar docente a carrera.",
                                                                "UTF-8"));
                        }

                        return null;
                });

                post("/admin/teacher-assignments/subject", (req, res) -> {

                        try {

                                Long teacherId = Long.parseLong(req.queryParams("teacher_id"));

                                Long subjectId = Long.parseLong(req.queryParams("subject_id"));

                                String academicYear = req.queryParams("academic_year");

                                String academicPeriod = req.queryParams("academic_period");

                                String roleCharge = req.queryParams("role_charge");

                                teacherAssignmentService.assignTeacherToSubject(teacherId,
                                                subjectId, academicYear, academicPeriod,
                                                roleCharge);

                                res.redirect("/admin/teacher-assignments?successSubject=true");

                        } catch (IllegalArgumentException e) {

                                res.redirect("/admin/teacher-assignments?error="
                                                + URLEncoder.encode(e.getMessage(), "UTF-8"));

                        } catch (Exception e) {

                                res.redirect("/admin/teacher-assignments?error=" + URLEncoder
                                                .encode("Error al asignar docente a materia.",
                                                                "UTF-8"));
                        }

                        return null;
                });
        }
}
