package controllers;

import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import services.AssignmentService;
import services.CareerService;
import services.SubjectService;
import services.SubmissionService;
import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class AssignmentController {

  private static final AssignmentService assignmentService = new AssignmentService();
  private static final CareerService careerService = new CareerService();
  private static final SubjectService subjectService = new SubjectService();
  private static final SubmissionService submissionService = new SubmissionService();

  public static void init(MustacheTemplateEngine engine) {

    // GET: Listar Tareas creadas por el Profesor
    get("/teacher/tasks", (req, res) -> {
      Long teacherId = Long.valueOf(req.session().attribute("userId").toString());
      Map<String, Object> model = new HashMap<>();
      model.put("username", req.session().attribute("username"));
      
      if (req.queryParams("success") != null) model.put("successMessage", req.queryParams("success"));
      
      model.put("tasks", assignmentService.mapAssignmentsToView(assignmentService.getAssignmentsByTeacher(teacherId)));
      return engine.render(new ModelAndView(model, "teacher_tasks.mustache"));
    });

    // GET: Mostrar formulario de creación de Tarea
    get("/teacher/tasks/new", (req, res) -> {
      Long teacherId = Long.valueOf(req.session().attribute("userId").toString());
      Map<String, Object> model = new HashMap<>();
      model.put("username", req.session().attribute("username"));
      
      if (req.queryParams("error") != null) {
          model.put("errorMessage", req.queryParams("error"));
      }

      model.put("careers", careerService.getAllCareers());
      
      // IMPORTANTE: Filtrar solo las materias donde el docente es TITULAR
      model.put("subjects", assignmentService.getSubjectsTaughtByTeacher(teacherId));
      
      return engine.render(new ModelAndView(model, "teacher_task_new.mustache"));
    });

    // Endpoint JSON para cargar las comisiones de una materia dinámicamente
    get("/teacher/tasks/classes", (req, res) -> {
      Long teacherId = Long.valueOf(req.session().attribute("userId").toString());
      Long subjectId = Long.parseLong(req.queryParams("subject_id"));
      List<Map<String, Object>> classes = assignmentService.getCourseClassesForTeacherAndSubject(teacherId, subjectId);
      
      res.type("application/json");
      StringBuilder json = new StringBuilder("[");
      for (int i = 0; i < classes.size(); i++) {
        Map<String, Object> cc = classes.get(i);
        json.append("{\"id\":").append(cc.get("id")).append(",\"name\":\"").append(cc.get("name")).append("\"}");
        if (i < classes.size() - 1) json.append(",");
      }
      json.append("]");
      return json.toString();
    });

    // POST: Crear una nueva tarea
    post("/teacher/tasks/new", (req, res) -> {
      try {
        Long teacherId = Long.valueOf(req.session().attribute("userId").toString());
        String title = req.queryParams("title");
        String description = req.queryParams("description");
        String dueDate = req.queryParams("due_date"); 
        String allComisions = req.queryParams("all_comisions");

        String careerIdStr = req.queryParams("career_id");
        String subjectIdStr = req.queryParams("subject_id");
        String courseClassIdStr = req.queryParams("course_class_id");

        Long careerId = careerIdStr != null && !careerIdStr.isEmpty() ? Long.parseLong(careerIdStr) : null;
        Long subjectId = subjectIdStr != null && !subjectIdStr.isEmpty() ? Long.parseLong(subjectIdStr) : null;
        Long courseClassId = courseClassIdStr != null && !courseClassIdStr.isEmpty() ? Long.parseLong(courseClassIdStr) : null;

        if (subjectId == null || !assignmentService.isTeacherTitular(teacherId, subjectId)) {
            res.redirect("/teacher/tasks/new?error=" + URLEncoder.encode("Acceso denegado: Solo los docentes TITULARES pueden publicar tareas en esta materia.", "UTF-8"));
            return null;
        }

        // SOLUCIÓN: Usar el temporal del sistema (Evita crash de directorios inexistentes)
        req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement(System.getProperty("java.io.tmpdir")));
        Part filePart = req.raw().getPart("file");

        String savedPath = null;
        if (filePart != null && filePart.getSize() > 0) {
          String fileName = System.currentTimeMillis() + "_" + filePart.getSubmittedFileName().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
          String uploadDir = "uploads/tasks";
          
          File dir = new File(uploadDir);
          if (!dir.exists()) dir.mkdirs();
          
          File file = new File(uploadDir, fileName);
          try (InputStream input = filePart.getInputStream()) {
            Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
          }
          savedPath = uploadDir + "/" + fileName;
        }

        // SOLUCIÓN AL BUCLE FANTASMA
        if (allComisions != null && allComisions.equals("on") && subjectId != null) {
          List<Map<String, Object>> classes = assignmentService.getCourseClassesForTeacherAndSubject(teacherId, subjectId);
          if (classes.isEmpty()) {
              // Si no hay comisiones creadas, creamos 1 general para la materia
              assignmentService.createAssignment(teacherId, title, description, savedPath, careerId, subjectId, null, dueDate);
          } else {
              for (Map<String, Object> cc : classes) {
                Long ccId = Long.valueOf(cc.get("id").toString());
                assignmentService.createAssignment(teacherId, title, description, savedPath, careerId, subjectId, ccId, dueDate);
              }
          }
        } else {
          assignmentService.createAssignment(teacherId, title, description, savedPath, careerId, subjectId, courseClassId, dueDate);
        }

        res.redirect("/teacher/tasks?success=" + URLEncoder.encode("Tarea creada exitosamente", "UTF-8"));
      } catch(Exception e) {
          e.printStackTrace(); 
          res.redirect("/teacher/tasks/new?error=" + URLEncoder.encode("Error interno al crear tarea. Revisa la consola.", "UTF-8"));
      }
      return null;
    });

    // GET: Mostrar formulario de edición
    get("/teacher/tasks/:id/edit", (req, res) -> {
      Long taskId = Long.parseLong(req.params(":id"));
      Map<String, Object> model = new HashMap<>();
      model.put("username", req.session().attribute("username"));
      
      models.Assignment task = assignmentService.getAssignmentById(taskId);
      if (task != null) {
        model.put("task_id", task.getId());
        model.put("title", task.getString("title"));
        model.put("description", task.getString("description"));
      }
      return engine.render(new ModelAndView(model, "teacher_task_edit.mustache"));
    });

    // POST: Guardar cambios de la edición
    post("/teacher/tasks/:id/edit", (req, res) -> {
      Long taskId = Long.parseLong(req.params(":id"));
      String title = req.queryParams("title");
      String description = req.queryParams("description");

      assignmentService.updateAssignment(taskId, title, description);
      res.redirect("/teacher/tasks?success=" + URLEncoder.encode("Tarea actualizada correctamente", "UTF-8"));
      return null;
    });

    // POST: Eliminar tarea
    post("/teacher/tasks/:id/delete", (req, res) -> {
      Long taskId = Long.parseLong(req.params(":id"));
      assignmentService.deleteAssignment(taskId);
      res.redirect("/teacher/tasks?success=" + URLEncoder.encode("Tarea eliminada del sistema", "UTF-8"));
      return null;
    });

    // GET: Ver entregas de una tarea específica
    get("/teacher/tasks/:id/submissions", (req, res) -> {
      Long taskId = Long.parseLong(req.params(":id"));
      Map<String, Object> model = new HashMap<>();
      model.put("username", req.session().attribute("username"));
      model.put("task_id", taskId);
      model.put("submissions", submissionService.getSubmissionsForTaskView(taskId));
      return engine.render(new ModelAndView(model, "teacher_submissions.mustache"));
    });

   // POST: Calificar y comentar una entrega de un alumno
    post("/teacher/submissions/:id/grade", (req, res) -> {
      Long submissionId = Long.parseLong(req.params(":id"));
      String gradeStr = req.queryParams("grade");
      String comment = req.queryParams("comment");
      
      // SOLUCIÓN: Convertir el texto a número decimal (Double)
      Double grade = null;
      try {
          if (gradeStr != null && !gradeStr.trim().isEmpty()) {
              grade = Double.parseDouble(gradeStr.trim().replace(",", "."));
          }
      } catch (NumberFormatException e) {
          System.err.println("La nota ingresada no es un número válido.");
      }
      
      submissionService.gradeSubmission(submissionId, grade, comment);
      
      models.Submission sub = models.Submission.findById(submissionId);
      if (sub != null) {
          res.redirect("/teacher/tasks/" + sub.get("assignment_id") + "/submissions");
      } else {
          res.redirect("/teacher/tasks");
      }
      return null;
    });
  }
}