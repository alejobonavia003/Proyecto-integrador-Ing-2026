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
}
