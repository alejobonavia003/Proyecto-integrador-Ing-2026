package services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Career;
import models.StudyPlan;
import models.Subject;
import models.TeacherCareer;
import models.TeacherSubject;
import models.User;

public class TeacherAssignmentService {

    public Map<String, Object> getTeacherAssignmentViewData() {

        Map<String, Object> model = new HashMap<>();

        model.put("teachers", getTeachers());
        model.put("careers", getAllCareers());

        return model;
    }

    public List<User> getTeachers() {

        return User.where("role_id = ?", 2);
    }

    public List<Career> getAllCareers() {

        return Career.findAll();
    }

    public String getAllowedSubjectsJson(String teacherId) {

        if (teacherId == null || teacherId.trim().isEmpty()) {
            return "[]";
        }

        org.javalite.activejdbc.LazyList<Subject> allowedSubjects =
                Subject.findBySQL("SELECT s.* FROM subjects s "
                        + "INNER JOIN study_plans sp ON s.study_plan_id = sp.id "
                        + "INNER JOIN teacher_careers tc ON sp.career_id = tc.career_id "
                        + "WHERE tc.teacher_id = ?", teacherId);

        return allowedSubjects.toJson(false, "id", "name", "code");
    }

    public void assignTeacherToCareer(Long teacherId, Long careerId) {

        long exist = TeacherCareer.count("teacher_id = ? AND career_id = ?", teacherId, careerId);

        if (exist > 0) {
            throw new IllegalArgumentException("El docente ya está asignado a esta carrera.");
        }

        TeacherCareer tc = new TeacherCareer();

        tc.set("teacher_id", teacherId);
        tc.set("career_id", careerId);

        if (!tc.saveIt()) {
            throw new IllegalArgumentException("No se pudo guardar la asignación.");
        }
    }

    public void assignTeacherToSubject(Long teacherId, Long subjectId, String academicYear,
            String academicPeriod, String roleCharge) {

        Subject subject = Subject.findById(subjectId);

        if (subject == null || subject.get("study_plan_id") == null) {

            throw new IllegalArgumentException("La materia seleccionada no es válida.");
        }

        StudyPlan plan = StudyPlan.findById(subject.get("study_plan_id"));

        Long careerId = plan.getLong("career_id");

        long isAssigned =
                TeacherCareer.count("teacher_id = ? AND career_id = ?", teacherId, careerId);

        if (isAssigned == 0) {

            throw new IllegalArgumentException(
                    "Acción denegada: El docente no pertenece a la carrera de esta materia.");
        }

        TeacherSubject ts = new TeacherSubject();

        ts.set("teacher_id", teacherId);
        ts.set("subject_id", subjectId);
        ts.set("role_charge", roleCharge.toUpperCase());
        ts.set("academic_year", academicYear);
        ts.set("academic_period", academicPeriod);

        if (!ts.saveIt()) {

            throw new IllegalArgumentException("No se pudo guardar la asignación.");
        }
    }
}
