package controllers;

import java.io.File;
import java.io.InputStream;
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

    get("/teacher/tasks", (req, res) -> {
      Long teacherId = Long.valueOf(req.session().attribute("userId").toString());
      Map<String, Object> model = new HashMap<>();
      model.put("username", req.session().attribute("username"));
      model.put("tasks", assignmentService
          .mapAssignmentsToView(assignmentService.getAssignmentsByTeacher(teacherId)));
      return engine.render(new ModelAndView(model, "teacher_tasks.mustache"));
    });

    get("/teacher/tasks/new", (req, res) -> {
      Map<String, Object> model = new HashMap<>();
      Long teacherId = Long.valueOf(req.session().attribute("userId").toString());

      // Mostrar solo las materias que dicta el docente
      model.put("subjects", assignmentService.getSubjectsTaughtByTeacher(teacherId));

      // Opcional: lista de carreras para crear tarea global por carrera
      model.put("careers", careerService.getAllCareersView());
      return engine.render(new ModelAndView(model, "teacher_task_new.mustache"));
    });

    // Endpoint para obtener comisiones (course_classes) para una materia concreta y docente
    get("/teacher/tasks/classes", (req, res) -> {
      res.type("application/json");
      String subjectIdStr = req.queryParams("subject_id");
      Long teacherId = Long.valueOf(req.session().attribute("userId").toString());
      if (subjectIdStr == null || subjectIdStr.isEmpty()) {
        return "[]";
      }
      Long subjectId = Long.parseLong(subjectIdStr);
      List<Map<String, Object>> classes =
          assignmentService.getCourseClassesForTeacherAndSubject(teacherId, subjectId);

      // Build simple JSON array
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      for (int i = 0; i < classes.size(); i++) {
        Map<String, Object> c = classes.get(i);
        sb.append("{\"id\":").append(c.get("id")).append(",\"name\":\"").append(c.get("name"))
            .append("\"}");
        if (i < classes.size() - 1)
          sb.append(",");
      }
      sb.append("]");
      return sb.toString();
    });

    // GET: Ver todas las entregas de una tarea específica
    get("/teacher/tasks/:id/submissions", (req, res) -> {
      Long assignmentId = Long.parseLong(req.params(":id"));
      Map<String, Object> model = new HashMap<>();
      model.put("username", req.session().attribute("username"));

      List<Map<String, Object>> submissions =
          submissionService.getSubmissionsForAssignment(assignmentId);

      // Inyectamos el assignment_id en cada mapa para usarlo fácilmente dentro del bucle de
      // Mustache
      for (Map<String, Object> sub : submissions) {
        sub.put("assignment_id", assignmentId);
      }

      model.put("submissions", submissions);
      return engine.render(new ModelAndView(model, "teacher_submissions.mustache"));
    });

    // POST: Guardar la nota y el comentario de una entrega
    post("/teacher/submissions/:id/grade", (req, res) -> {
      Long submissionId = Long.parseLong(req.params(":id"));
      String gradeStr = req.queryParams("grade");
      String comment = req.queryParams("comment");
      String assignmentIdStr = req.queryParams("assignment_id");

      Double grade =
          (gradeStr != null && !gradeStr.isEmpty()) ? Double.parseDouble(gradeStr) : null;

      submissionService.gradeSubmission(submissionId, grade, comment);

      // Redireccionamos de vuelta a la lista de entregas de esa tarea
      res.redirect("/teacher/tasks/" + assignmentIdStr + "/submissions?success=true");
      return null;
    });

    post("/teacher/tasks/new", (req, res) -> {
      // Habilitar multipart
      req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("./uploads"));

      String title = req.queryParams("title");
      String description = req.queryParams("description");
      String careerIdStr = req.queryParams("career_id");
      String subjectIdStr = req.queryParams("subject_id");
      String courseClassIdStr = req.queryParams("course_class_id");
      String allComisions = req.queryParams("all_comisions");

      Long teacherId = Long.valueOf(req.session().attribute("userId").toString());

      Long careerId =
          careerIdStr != null && !careerIdStr.isEmpty() ? Long.parseLong(careerIdStr) : null;
      Long subjectId =
          subjectIdStr != null && !subjectIdStr.isEmpty() ? Long.parseLong(subjectIdStr) : null;
      Long courseClassId =
          courseClassIdStr != null && !courseClassIdStr.isEmpty() ? Long.parseLong(courseClassIdStr)
              : null;

      String savedPath = null;
      try {
        Part filePart = req.raw().getPart("file");
        if (filePart != null && filePart.getSize() > 0) {
          File uploadDir = new File("uploads/tasks");
          if (!uploadDir.exists())
            uploadDir.mkdirs();

          String submitted = filePart.getSubmittedFileName();
          String target = System.currentTimeMillis() + "_" + submitted;
          File out = new File(uploadDir, target);
          try (InputStream in = filePart.getInputStream()) {
            Files.copy(in, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
          }
          savedPath = out.getPath();
        }
      } catch (Exception e) {
        // Ignorar fallos de subida y continuar con null
        savedPath = null;
      }

      // Si se pidió aplicar a todas las comisiones de la materia, crear una tarea por cada comision
      if (allComisions != null && allComisions.equals("on") && subjectId != null) {
        List<Map<String, Object>> classes =
            assignmentService.getCourseClassesForTeacherAndSubject(teacherId, subjectId);
        for (Map<String, Object> cc : classes) {
          Long ccId = Long.valueOf(cc.get("id").toString());
          assignmentService.createAssignment(teacherId, title, description, savedPath, careerId,
              subjectId, ccId);
        }
      } else {
        assignmentService.createAssignment(teacherId, title, description, savedPath, careerId,
            subjectId, courseClassId);
      }

      res.redirect("/teacher/tasks?success=true");
      return null;
    });
  }
}
