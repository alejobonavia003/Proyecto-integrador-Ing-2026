package services;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import models.Career;
import models.CourseClass;
import models.Enrollment;
import models.StudentSubject;
import models.StudyPlan;
import models.TeacherSubject;
import models.User;

public class DashboardService {

    private final AssignmentService assignmentService = new AssignmentService();

    public Map<String, Object> getTeacherDashboardData(Long teacherId) {

        Map<String, Object> data = new HashMap<>();

        long materiasCount = TeacherSubject.count("teacher_id = ?", teacherId);

        data.put("materiasCount", materiasCount);

        List<TeacherSubject> teacherSubjects = TeacherSubject.where("teacher_id = ?", teacherId);

        long comisionesCount = 0;

        for (TeacherSubject teacherSubject : teacherSubjects) {

            comisionesCount +=
                    CourseClass.count("subject_id = ?", teacherSubject.get("subject_id"));
        }

        data.put("comisionesCount", comisionesCount);

        return data;
    }

    public Map<String, Object> getStudentDashboardData(Long studentId) {

        Map<String, Object> data = new HashMap<>();

        User student = User.findById(studentId);

        if (student == null) {
            data.put("averageGrade", "--");
            return data;
        }

        Long planId = student.getLong("study_plan_id");

        if (planId == null) {

            data.put("hasCareer", false);
            data.put("averageGrade", "--");
            return data;
        }

        data.put("hasCareer", true);

        StudyPlan plan = StudyPlan.findById(planId);

        if (plan != null) {

            data.put("planName", plan.getString("name"));

            Career career = Career.findById(plan.getLong("career_id"));

            if (career != null) {
                data.put("careerName", career.getString("name"));
            }
        }

        long enrollmentsCount = Enrollment.count("student_id = ?", studentId);

        data.put("enrollmentsCount", enrollmentsCount);
        data.put("averageGrade", calculateAverageGrade(studentId));

        //Contadores de tareas
        Map<String, Long> taskCounters =
        assignmentService.getAssignmentCounters(studentId);

        data.put("pendingTasks", taskCounters.get("pendingTasks"));
        data.put("submittedTasks", taskCounters.get("submittedTasks"));
        data.put("gradedTasks", taskCounters.get("gradedTasks"));

        return data;

    }

    private String calculateAverageGrade(Long studentId) {
        List<StudentSubject> approvedSubjects = StudentSubject.where("student_id = ? AND grade IS NOT NULL", studentId);
        if (approvedSubjects.isEmpty()) {
            return "--";
        }

        double sum = 0.0;
        int count = 0;
        for (StudentSubject subject : approvedSubjects) {
            Object gradeValue = subject.get("grade");
            if (gradeValue instanceof Number) {
                sum += ((Number) gradeValue).doubleValue();
                count++;
            }
        }

        if (count == 0) {
            return "--";
        }

        double average = sum / count;
        return String.format(Locale.forLanguageTag("es-ES"), "%.2f", average);
    }
}
