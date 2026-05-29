package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

      row.put("content_reference", s.getString("content_reference"));
      row.put("submitted_at", s.getString("submitted_at"));
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

  /**
     * Obtiene las entregas de una tarea específica y las empaqueta 
     * junto con los datos del estudiante para la vista del profesor.
     */
    public java.util.List<java.util.Map<String, Object>> getSubmissionsForTaskView(Long taskId) {
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        
        // Buscar todas las entregas asociadas a esta tarea
        java.util.List<models.Submission> submissions = models.Submission.where("assignment_id = ?", taskId);
        
        for (models.Submission sub : submissions) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", sub.getId());
            map.put("submission_path", sub.getString("submission_path"));
            
            // Evitar nulos en la nota
            Object grade = sub.get("grade");
            map.put("grade", grade != null ? grade.toString() : null);
            map.put("comment", sub.getString("comment"));
            
            // Buscar la información personal del alumno
            models.User student = models.User.findById(sub.getLong("student_id"));
            if (student != null) {
                map.put("student_name", student.getString("name"));
                map.put("student_dni", student.getString("dni"));
            } else {
                map.put("student_name", "Usuario Desconocido");
                map.put("student_dni", "---");
            }
            
            list.add(map);
        }
        
        return list;
    }
}
