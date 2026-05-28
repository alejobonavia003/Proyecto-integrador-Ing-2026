package controllers;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import models.Career;
import models.CourseClass;
import models.Enrollment;
import models.Subject;
import models.User;
import services.CorrelativityService;
import services.EnrollmentService;
import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class EnrollmentController {

    private static final Logger logger = Logger.getLogger(EnrollmentController.class.getName());
    private static final CorrelativityService correlativityService = new CorrelativityService();
    private static final EnrollmentService enrollmentService = new EnrollmentService();

    public static void init(MustacheTemplateEngine engine) {

        // --- RUTAS DEL ALUMNO ---

        get("/student/mis-inscripciones", (req, res) -> {
            Long studentId = Long.valueOf(req.session().attribute("userId").toString());
            Map<String, Object> model = new HashMap<>();
            model.put("username", req.session().attribute("username"));

            if (req.queryParams("success") != null) model.put("successMessage", req.queryParams("success"));
            if (req.queryParams("error") != null) model.put("errorMessage", req.queryParams("error"));

            model.put("inscripciones", enrollmentService.getStudentEnrollments(studentId));
            return engine.render(new ModelAndView(model, "mis_inscripciones.mustache"));
        });

        /**
         * GET /student/inscripciones/nueva
         * Muestra el formulario simplificado con materias filtradas según el avance.
         */
        get("/student/inscripciones/nueva", (req, res) -> {
            Long studentId = Long.valueOf(req.session().attribute("userId").toString());
            Map<String, Object> model = new HashMap<>();
            model.put("username", req.session().attribute("username"));

            if (req.queryParams("error") != null) {
                model.put("errorMessage", req.queryParams("error"));
                Object faltantesObj = req.session().attribute("errorMateriasFaltantes");
                if (faltantesObj != null) {
                    @SuppressWarnings("unchecked")
                    List<Subject> faltantes = (List<Subject>) faltantesObj;
                    if (!faltantes.isEmpty()) {
                        model.put("faltantes", faltantes);
                        model.put("hasFaltantes", true);
                    }
                    req.session().removeAttribute("errorMateriasFaltantes");
                }
            }

            model.put("materiasDisponibles", enrollmentService.getAvailableSubjectsForStudent(studentId));
            return engine.render(new ModelAndView(model, "inscripcion_form.mustache"));
        });

        /**
         * POST /student/inscripciones/confirmar
         */
        post("/student/inscripciones/confirmar", (req, res) -> {
            try {
                Long studentId = Long.valueOf(req.session().attribute("userId").toString());
                Long subjectId = Long.parseLong(req.queryParams("subject_id"));
                Long courseClassId = Long.parseLong(req.queryParams("course_class_id"));

                if (!enrollmentService.belongsToStudentCareer(studentId, subjectId)) {
                    res.redirect("/student/inscripciones/nueva?error=" + URLEncoder.encode("Esta materia no pertenece a tu plan de estudios.", "UTF-8"));
                    return null;
                }

                List<Subject> faltantes = correlativityService.verificarCorrelativas(studentId, subjectId);
                if (!faltantes.isEmpty()) {
                    req.session().attribute("errorMateriasFaltantes", faltantes);
                    res.redirect("/student/inscripciones/nueva?error=" + URLEncoder.encode("No cumples con las correlativas necesarias.", "UTF-8"));
                    return null;
                }

                enrollmentService.enrollStudent(studentId, courseClassId);
                
                // Obtener el ID de la última inscripción guardada para el comprobante
                Enrollment last = (Enrollment) Enrollment.where("student_id = ? AND course_class_id = ?", studentId, courseClassId).orderBy("id DESC").get(0);
                res.redirect("/student/inscripciones/comprobante?id=" + last.getId());
            } catch (IllegalStateException e) {
                res.redirect("/student/inscripciones/nueva?error=" + URLEncoder.encode(e.getMessage(), "UTF-8"));
            } catch (Exception e) {
                res.redirect("/student/inscripciones/nueva?error=" + URLEncoder.encode("Error interno al procesar tu solicitud.", "UTF-8"));
            }
            return null;
        });

        /**
         * GET /student/inscripciones/comprobante
         * Renderiza la vista del comprobante de inscripción.
         */
        get("/student/inscripciones/comprobante", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("username", req.session().attribute("username"));
            Long receiptId = Long.parseLong(req.queryParams("id"));
            
            model.put("receipt", enrollmentService.getEnrollmentReceipt(receiptId));
            return engine.render(new ModelAndView(model, "comprobante_inscripcion.mustache"));
        });

        // --- RUTAS DEL PROFESOR / ADMINISTRACIÓN ---

        /**
         * GET /teacher/comisiones
         * Permite al profesor ver sus materias asignadas y ver las listas con filtros.
         */
        /**
         * GET /teacher/comisiones
         * Permite al profesor ver sus materias asignadas y ver las listas con filtros.
         */
/**
         * GET /teacher/comisiones
         * Permite al profesor ver sus materias asignadas y ver las listas con filtros.
         */
        get("/teacher/comisiones", (req, res) -> {
            Long teacherId = Long.valueOf(req.session().attribute("userId").toString());
            Map<String, Object> model = new HashMap<>();
            model.put("username", req.session().attribute("username"));

            if (req.queryParams("success") != null) model.put("successMessage", req.queryParams("success"));
            if (req.queryParams("error") != null) model.put("errorMessage", req.queryParams("error"));

            // 1. Mapeo para el selector de materias titulares
            List<Map<String, Object>> materiasTitularView = new ArrayList<>();
            List<Subject> materiasTitulares = enrollmentService.getSubjectsWhereTeacherIsTitular(teacherId); // Asignación previa
            for(Subject s : materiasTitulares) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", s.getId());
                map.put("name", s.getString("name"));
                map.put("code", s.getString("code"));
                materiasTitularView.add(map);
            }
            model.put("materiasTitular", materiasTitularView);
            
            // 2. Mapeos para los selectores de Filtros
            List<Map<String, Object>> carrerasFiltro = new ArrayList<>();
            List<Career> careers = Career.findAll(); // Asignación previa
            for(Career c : careers) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", c.getId());
                map.put("name", c.getString("name"));
                carrerasFiltro.add(map);
            }
            model.put("carrerasFiltro", carrerasFiltro);

            List<Map<String, Object>> materiasFiltro = new ArrayList<>();
            List<Subject> subjects = Subject.findAll(); // Asignación previa
            for(Subject s : subjects) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", s.getId());
                map.put("name", s.getString("name"));
                materiasFiltro.add(map);
            }
            model.put("materiasFiltro", materiasFiltro);

            List<Map<String, Object>> comisionesFiltro = new ArrayList<>();
            List<CourseClass> courseClasses = CourseClass.findAll(); // Asignación previa
            for(CourseClass cc : courseClasses) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", cc.getId());
                map.put("name", cc.getString("name") != null ? cc.getString("name") : "Comisión " + cc.getId());
                comisionesFiltro.add(map);
            }
            model.put("comisionesFiltro", comisionesFiltro);

            // 3. Procesamiento de Filtros Solicitados
            List<User> alumnosResultado = new ArrayList<>();
            String filtroAplicado = "Ninguno (Muestra todos los estudiantes)";

            if (req.queryParams("career_id") != null && !req.queryParams("career_id").isEmpty()) {
                Long cId = Long.parseLong(req.queryParams("career_id"));
                alumnosResultado = enrollmentService.getStudentsByCareer(cId);
                filtroAplicado = "Por Carrera";
            } else if (req.queryParams("subject_id") != null && !req.queryParams("subject_id").isEmpty()) {
                Long sId = Long.parseLong(req.queryParams("subject_id"));
                alumnosResultado = enrollmentService.getStudentsBySubject(sId);
                filtroAplicado = "Por Materia";
            } else if (req.queryParams("course_class_id") != null && !req.queryParams("course_class_id").isEmpty()) {
                Long ccId = Long.parseLong(req.queryParams("course_class_id"));
                alumnosResultado = enrollmentService.getStudentsByCommission(ccId);
                filtroAplicado = "Por Comisión";
            }

            // 4. Mapeo seguro de la tabla de alumnos encontrados
            List<Map<String, Object>> alumnosView = new ArrayList<>();
            for (User u : alumnosResultado) {
                Map<String, Object> map = new HashMap<>();
                map.put("dni", u.getString("dni"));
                map.put("name", u.getString("name"));
                map.put("email", u.getString("email"));
                alumnosView.add(map);
            }
            
            model.put("alumnos", alumnosView);
            model.put("filtroAplicado", filtroAplicado);

            return engine.render(new ModelAndView(model, "comisiones_gestion.mustache"));
        });
/**
         * POST /teacher/comisiones/crear
         * El profesor crea una nueva comisión si es titular de la materia.
         */
        post("/teacher/comisiones/crear", (req, res) -> {
            try {
                Long teacherId = Long.valueOf(req.session().attribute("userId").toString());
                Long subjectId = Long.parseLong(req.queryParams("subject_id"));
                String name = req.queryParams("name");
                int capacidad = Integer.parseInt(req.queryParams("quota")); // Viene del HTML como quota

                // Validar regla de negocio esencial
                if (!enrollmentService.isTeacherTitular(teacherId, subjectId)) {
                    res.redirect("/teacher/comisiones?error=" + URLEncoder.encode("No puedes crear comisiones en materias donde no eres Titular.", "UTF-8"));
                    return null;
                }

                CourseClass cc = new CourseClass();
                cc.set("subject_id", subjectId);
                cc.set("teacher_id", teacherId);   // <--- CORRECCIÓN 1: Faltaba guardar el ID del profesor
                cc.set("name", name);
                cc.set("capacity", capacidad);     // <--- CORRECCIÓN 2: La columna en BD se llama 'capacity', no 'quota'
                cc.saveIt();

                res.redirect("/teacher/comisiones?success=" + URLEncoder.encode("Comisión académica creada exitosamente.", "UTF-8"));
            } catch (Exception e) {
                logger.severe("Error al crear la comisión: " + e.getMessage());
                res.redirect("/teacher/comisiones?error=" + URLEncoder.encode("Error al procesar la creación de la comisión.", "UTF-8"));
            }
            return null;
        });

        // --- RUTAS DE CARRERA (ALUMNO) ---

        get("/student/elegir-carrera", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("username", req.session().attribute("username"));
            if (req.queryParams("error") != null) model.put("errorMessage", req.queryParams("error"));
            model.put("planes", enrollmentService.getAllStudyPlans());
            return engine.render(new ModelAndView(model, "elegir_carrera.mustache"));
        });

        post("/student/carrera/confirmar", (req, res) -> {
            try {
                Long studentId = Long.valueOf(req.session().attribute("userId").toString());
                Long planId = Long.parseLong(req.queryParams("plan_id"));
                enrollmentService.assignPlanToStudent(studentId, planId);
                res.redirect("/student/dashboard?success=" + URLEncoder.encode("Te has inscripto en la carrera exitosamente.", "UTF-8"));
            } catch (Exception e) {
                res.redirect("/student/elegir-carrera?error=" + URLEncoder.encode("Error al asignar la carrera.", "UTF-8"));
            }
            return null;
        });
    }
}