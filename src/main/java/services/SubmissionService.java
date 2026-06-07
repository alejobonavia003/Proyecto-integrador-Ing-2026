package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

import models.Submission;
import models.User;

public class SubmissionService {

  public Submission getSubmission(Long studentId, Long assignmentId) {
    return Submission.findFirst("student_id = ? AND assignment_id = ?", studentId, assignmentId);
  }

  public Submission getSubmissionById(Long submissionId) {
    return Submission.findById(submissionId);
  }

  public List<Map<String, Object>> getSubmissionsForAssignment(Long assignmentId) {
    List<Map<String, Object>> submissions = new ArrayList<>();

    // Traemos los resultados como la lista nativa de ActiveJDBC
    List<Submission> queryResults = Submission.where("assignment_id = ?", assignmentId);

    for (int i = 0; i < queryResults.size(); i++) {
      Submission s = (Submission) queryResults.get(i);
      Map<String, Object> row = new HashMap<>();
      row.put("id", s.getId());
      row.put("student_id", s.get("student_id"));

      User student = User.findById(s.get("student_id"));
      row.put("student_name", student != null ? student.getString("name") : "Alumno desconocido");

      // Formatea la fecha de entrega para mostrarla en formato dd/MM/yyyy
      Timestamp submittedAt = s.getTimestamp("submitted_at");
      String formattedSubmittedAt = submittedAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

      row.put("content_reference", s.getString("content_reference"));
      row.put("submitted_at", formattedSubmittedAt);
      row.put("grade", s.get("grade"));
      row.put("comment", s.getString("comment"));
      submissions.add(row);
    }
    return submissions;
  }

  public Submission createOrUpdateSubmission(Long studentId, Long assignmentId,
      String contentReference) {
    Submission s = getSubmission(studentId, assignmentId);
    if (s == null) {
      s = new Submission();
      s.set("student_id", studentId);
      s.set("assignment_id", assignmentId);
    }
    s.set("content_reference", contentReference);
    s.set("submitted_at", new java.sql.Timestamp(System.currentTimeMillis()));
    s.saveIt();
    return s;
  }


/**
   * Obtiene el listado completo de alumnos inscriptos junto con el estado cruzado de sus entregas
   * para todas las tareas del docente.
   */
  public List<Map<String, Object>> getStudentsTrackingForTeacher(Long teacherId) {
    String sql = "SELECT " +
                 "  u.name AS student_name, " +
                 "  sub.name AS subject_name, " +
                 "  cc.name AS class_name, " +
                 "  a.title AS assignment_title, " +
                 "  a.id AS assignment_id, " +
                 "  s.id AS submission_id, " +
                 "  s.grade AS grade, " +
                 "  CASE " +
                 "    WHEN s.id IS NULL THEN 'FALTA_ENTREGAR' " +
                 "    WHEN s.grade IS NULL THEN 'TODAVIA_NO_CORRIGIO' " +
                 "    ELSE 'YA_CORRIGIO' " +
                 "  END AS status_type " +
                 "FROM assignments a " +
                 "INNER JOIN course_classes cc ON a.course_class_id = cc.id " +
                 "INNER JOIN subjects sub ON cc.subject_id = sub.id " +
                 "INNER JOIN enrollments e ON cc.id = e.course_class_id " +
                 "INNER JOIN users u ON e.student_id = u.id " +
                 "LEFT JOIN submissions s ON a.id = s.assignment_id AND u.id = s.student_id " +
                 "WHERE a.teacher_id = ? " +
                 "ORDER BY sub.name ASC, cc.name ASC, u.name ASC";

    // Obtenemos los resultados de ActiveJDBC (Lista cruda)
    List<Map> rawResults = org.javalite.activejdbc.Base.findAll(sql, teacherId);
    
    // Convertimos al tipo estricto que exige Java
    List<Map<String, Object>> trackingList = new ArrayList<>();
    for (Map row : rawResults) {
        trackingList.add((Map<String, Object>) row);
    }
    
    return trackingList;
  }

  public Submission gradeSubmission(Long submissionId, Double grade, String comment) {
    Submission s = getSubmissionById(submissionId);
    if (s == null) {
      return null;
    }
    s.set("grade", grade);
    s.set("comment", comment);
    s.saveIt();
    return s;
  }
}
