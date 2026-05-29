package controllers;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import services.AssignmentService;
import services.SubmissionService;
import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class StudentTaskController {

  private static final AssignmentService assignmentService = new AssignmentService();
  private static final SubmissionService submissionService = new SubmissionService();

  public static void init(MustacheTemplateEngine engine) {

    get("/student/tasks", (req, res) -> {
      Long studentId = Long.valueOf(req.session().attribute("userId").toString());
      Map<String, Object> model = new HashMap<>();
      model.put("username", req.session().attribute("username"));

      List<Map<String, Object>> tasks = assignmentService
          .mapAssignmentsToView(assignmentService.getAssignmentsForStudent(studentId));

      // Añadimos estado de entrega para cada tarea
      List<Map<String, Object>> enriched = new ArrayList<>();
      for (Map<String, Object> t : tasks) {
        Map<String, Object> copy = new HashMap<>(t);
        Long assignmentId = Long.valueOf(t.get("id").toString());
        models.Submission s = submissionService.getSubmission(studentId, assignmentId);
        if (s != null) {
          copy.put("submitted", true);
          copy.put("submission_path", s.getString("content_reference"));
          copy.put("submission_id", s.getId());
          // AGREGAR ESTO:
          copy.put("grade", s.get("grade"));
          copy.put("comment", s.getString("comment"));
        } else {
          copy.put("submitted", false);
        }
        enriched.add(copy);
      }

      model.put("tasks", enriched);
      return engine.render(new ModelAndView(model, "student_tasks.mustache"));
    });

    post("/student/tasks/:id/submit", (req, res) -> {
      req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("./uploads"));
      String idStr = req.params(":id");
      Long assignmentId = Long.parseLong(idStr);
      Long studentId = Long.valueOf(req.session().attribute("userId").toString());

      String savedPath = null;
      try {
        Part filePart = req.raw().getPart("file");
        if (filePart != null && filePart.getSize() > 0) {
          File uploadDir = new File("uploads/submissions");
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
        savedPath = null;
      }

      submissionService.createOrUpdateSubmission(studentId, assignmentId, savedPath);

      res.redirect("/student/tasks?submitted=true");
      return null;
    });
  }
}
