package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Assignment;
import models.StudyPlan;
import models.User;

public class AssignmentService {

  public Assignment createAssignment(Long teacherId, String title, String description,
      String filePath, Long careerId, Long subjectId, Long courseClassId) {

    Assignment a = new Assignment();
    a.set("title", title);
    a.set("description", description);
    a.set("file_path", filePath);
    a.set("teacher_id", teacherId);
    a.set("career_id", careerId);
    a.set("subject_id", subjectId);
    a.set("course_class_id", courseClassId);

    a.saveIt();
    return a;
  }

  public List<Assignment> getAssignmentsByTeacher(Long teacherId) {
    return Assignment.where("teacher_id = ?", teacherId);
  }

    /**
     * Obtiene solo las materias donde el docente es TITULAR.
     */
    public List<Map<String, Object>> getSubjectsTaughtByTeacher(Long teacherId) {
      List<Map<String, Object>> out = new ArrayList<>();
      // Filtramos cruzando con teacher_subjects exigiendo rol TITULAR
      List<models.Subject> subjects = models.Subject.findBySQL(
          "SELECT s.* FROM subjects s INNER JOIN teacher_subjects ts ON s.id = ts.subject_id WHERE ts.teacher_id = ? AND ts.role_charge = 'TITULAR'",
          teacherId);

      for (models.Subject s : subjects) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", s.getId());
        m.put("name", s.getString("name"));
        out.add(m);
      }
      return out;
    }

    /**
     * Valida estrictamente si el profesor es titular de la materia en la base de datos.
     */
    public boolean isTeacherTitular(Long teacherId, Long subjectId) {
        long count = models.TeacherSubject.count("teacher_id = ? AND subject_id = ? AND role_charge = 'TITULAR'", teacherId, subjectId);
        return count > 0;
    }
  public List<Map<String, Object>> getCourseClassesForTeacherAndSubject(Long teacherId,
      Long subjectId) {
    List<Map<String, Object>> out = new ArrayList<>();
    List<models.CourseClass> classes =
        models.CourseClass.where("teacher_id = ? AND subject_id = ?", teacherId, subjectId);
    for (models.CourseClass cc : classes) {
      Map<String, Object> m = new HashMap<>();
      m.put("id", cc.getId());
      m.put("name", cc.getString("name"));
      m.put("capacity", cc.get("capacity"));
      out.add(m);
    }
    return out;
  }

  public List<Assignment> getAssignmentsForStudent(Long studentId) {
    User student = User.findById(studentId);
    if (student == null || student.get("study_plan_id") == null) {
      return new ArrayList<>();
    }

    Long planId = student.getLong("study_plan_id");
    StudyPlan plan = StudyPlan.findById(planId);
    Long careerId = plan != null ? plan.getLong("career_id") : null;

    if (careerId == null) {
      return new ArrayList<>();
    }

    // Buscar assignments por carrera o por comisión relacionada al plan
    List<Assignment> assignments = Assignment.findBySQL(
        "SELECT a.* FROM assignments a WHERE a.career_id = ? OR a.course_class_id IN (SELECT id FROM course_classes WHERE subject_id IN (SELECT id FROM subjects WHERE study_plan_id = ?))",
        careerId, planId);

    return assignments;
  }

  public List<Map<String, Object>> mapAssignmentsToView(List<Assignment> list) {
    List<Map<String, Object>> out = new ArrayList<>();
    for (Assignment a : list) {
      Map<String, Object> m = new HashMap<>();
      m.put("id", a.getId());
      m.put("title", a.getString("title"));
      m.put("description", a.getString("description"));
      m.put("file_path", a.getString("file_path"));
      m.put("subject_id", a.get("subject_id"));
      m.put("course_class_id", a.get("course_class_id"));
      out.add(m);
    }
    return out;
  }

  // Obtener una tarea específica por ID
  public Assignment getAssignmentById(Long id) {
    return Assignment.findById(id);
  }

  // Modificar el título y descripción de una tarea
  public void updateAssignment(Long id, String title, String description) {
    Assignment a = Assignment.findById(id);
    if (a != null) {
      a.set("title", title);
      a.set("description", description);
      a.saveIt();
    }
  }

  // Eliminar una tarea
  public void deleteAssignment(Long id) {
    Assignment a = Assignment.findById(id);
    if (a != null) {
      // Opcional: Aquí podrías agregar lógica para borrar el archivo físico del servidor
      a.delete();
    }
  }
}
