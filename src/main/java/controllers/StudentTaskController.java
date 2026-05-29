package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // GET /student/tasks -> Mostrar lista de tareas
        get("/student/tasks", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            Long studentId = Long.valueOf(req.session().attribute("userId").toString());
            model.put("username", req.session().attribute("username"));

            if (req.queryParams("success") != null) model.put("successMessage", req.queryParams("success"));
            if (req.queryParams("error") != null) model.put("errorMessage", req.queryParams("error"));

            // 1. Obtener a qué comisiones y materias está inscripto el alumno
            List<models.Enrollment> enrollments = models.Enrollment.where("student_id = ? AND status = 'REGULAR'", studentId);
            List<Long> misComisiones = new java.util.ArrayList<>();
            List<Long> misMaterias = new java.util.ArrayList<>();
            
            for (models.Enrollment e : enrollments) {
                misComisiones.add(e.getLong("course_class_id"));
                models.CourseClass cc = models.CourseClass.findById(e.getLong("course_class_id"));
                if (cc != null) misMaterias.add(cc.getLong("subject_id"));
            }

            // 2. Filtrar tareas y verificar fechas límites
            List<Map<String, Object>> tasksView = new java.util.ArrayList<>();
            
            // ---> CORRECCIÓN: SEPARAR LA LISTA PARA QUE JAVA COMPILE BIEN <---
            List<models.Assignment> todasLasTareas = models.Assignment.findAll();
            
            for (models.Assignment task : todasLasTareas) {
                Long cId = task.getLong("course_class_id");
                Long sId = task.getLong("subject_id");
                
                // ¿Me corresponde esta tarea? (Filtro)
                boolean isMine = (cId != null && misComisiones.contains(cId)) || (cId == null && misMaterias.contains(sId));

                if (isMine) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", task.getId());
                    map.put("title", task.getString("title"));
                    map.put("description", task.getString("description"));
                    map.put("file_path", task.getString("file_path"));
                    
                    // Lógica de Vencimiento de Fecha
                    String dueDateStr = task.getString("due_date");
                    boolean isExpired = false;
                    if (dueDateStr != null && !dueDateStr.trim().isEmpty()) {
                        try {
                            String cleanDate = dueDateStr.replace("T", " ");
                            if (cleanDate.length() == 16) cleanDate += ":00";
                            // Parseo 100% seguro contra SQL Timestamp
                            java.time.LocalDateTime dueDate = java.sql.Timestamp.valueOf(cleanDate).toLocalDateTime();
                            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                            map.put("formatted_due_date", dueDate.format(formatter));
                            
                            java.time.LocalDateTime now = java.time.LocalDateTime.now();
                            if (now.isAfter(dueDate)) {
                                isExpired = true;
                            } else {
                                java.time.Duration duration = java.time.Duration.between(now, dueDate);
                                long days = duration.toDays();
                                long hours = duration.toHours() % 24;
                                map.put("time_remaining_str", "Faltan " + days + " días y " + hours + " hs");
                                if (days <= 2) map.put("is_ending_soon", true);
                            }
                        } catch (Exception e) {
                            map.put("formatted_due_date", "Fecha inválida");
                        }
                    } else {
                        map.put("formatted_due_date", "Sin límite de entrega");
                    }
                    map.put("is_expired", isExpired);
                    // Revisar si ya entregó
                    models.Submission sub = models.Submission.findFirst("assignment_id = ? AND student_id = ?", task.getId(), studentId);
                    if (sub != null) {
                        map.put("submitted", true);
                        map.put("submission_path", sub.getString("submission_path"));
                        map.put("grade", sub.getString("grade"));
                        map.put("comment", sub.getString("comment"));
                    } else {
                        map.put("submitted", false);
                    }
                    
                    tasksView.add(map);
                }
            }
            
            model.put("tasks", tasksView);
            return engine.render(new ModelAndView(model, "student_tasks.mustache"));
        });

        // POST /student/tasks/:id/submit -> Procesar Entrega y Re-entrega
        post("/student/tasks/:id/submit", (req, res) -> {
            Long studentId = Long.valueOf(req.session().attribute("userId").toString());
            Long taskId = Long.parseLong(req.params(":id"));
            
            models.Assignment task = models.Assignment.findById(taskId);
            

            // 1. Validar que no haya expirado
            String dueDateStr = task.getString("due_date");
            if (dueDateStr != null && !dueDateStr.trim().isEmpty()) {
                try {
                    String cleanDate = dueDateStr.replace("T", " ");
                    if (cleanDate.length() == 16) cleanDate += ":00";
                    java.time.LocalDateTime dueDate = java.sql.Timestamp.valueOf(cleanDate).toLocalDateTime();
                    if (java.time.LocalDateTime.now().isAfter(dueDate)) {
                        res.redirect("/student/tasks?error=" + java.net.URLEncoder.encode("Acción rechazada: El plazo de entrega ha vencido.", "UTF-8"));
                        return null;
                    }
                } catch (Exception e) { }
            }

            // 2. Procesar el archivo (Corrigiendo el directorio tmp)
            req.attribute("org.eclipse.jetty.multipartConfig", new javax.servlet.MultipartConfigElement(System.getProperty("java.io.tmpdir")));
            javax.servlet.http.Part filePart = req.raw().getPart("file");
            
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = System.currentTimeMillis() + "_" + filePart.getSubmittedFileName().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
                String uploadDir = "uploads/submissions";
                java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
                if (!java.nio.file.Files.exists(uploadPath)) java.nio.file.Files.createDirectories(uploadPath);
                
                java.io.File file = new java.io.File(uploadDir, fileName);
                try (java.io.InputStream input = filePart.getInputStream()) {
                    java.nio.file.Files.copy(input, file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                
                String submissionPath = uploadDir + "/" + fileName;

                // 3. Buscar si ya entregó antes para ACTUALIZAR en vez de crear otro
                models.Submission sub = models.Submission.findFirst("assignment_id = ? AND student_id = ?", taskId, studentId);
                if (sub == null) {
                    sub = new models.Submission();
                    sub.set("assignment_id", taskId);
                    sub.set("student_id", studentId);
                }
                sub.set("submission_path", submissionPath);
                sub.saveIt();

                res.redirect("/student/tasks?success=" + java.net.URLEncoder.encode("Archivo entregado correctamente.", "UTF-8"));
            } else {
                 res.redirect("/student/tasks?error=" + java.net.URLEncoder.encode("No se detectó ningún archivo.", "UTF-8"));
            }
            return null;
        });
  }
}