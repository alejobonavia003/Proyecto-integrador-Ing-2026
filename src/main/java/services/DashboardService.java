package services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Career;
import models.CourseClass;
import models.Enrollment;
import models.StudyPlan;
import models.TeacherSubject;
import models.User;

public class DashboardService {

    public Map<String, Object> getTeacherDashboardData(Long teacherId) {

        Map<String, Object> data = new HashMap<>();

        long materiasCount = TeacherSubject.count(
                "teacher_id = ?",
                teacherId
        );

        data.put("materiasCount", materiasCount);

        List<TeacherSubject> teacherSubjects = TeacherSubject.where(
                "teacher_id = ?",
                teacherId
        );

        long comisionesCount = 0;

        for (TeacherSubject teacherSubject : teacherSubjects) {

            comisionesCount += CourseClass.count(
                    "subject_id = ?",
                    teacherSubject.get("subject_id")
            );
        }

        data.put("comisionesCount", comisionesCount);

        return data;
    }

    public Map<String, Object> getStudentDashboardData(Long studentId) {

        Map<String, Object> data = new HashMap<>();

        User student = User.findById(studentId);

        if (student == null) {
            return data;
        }

        Long planId = student.getLong("study_plan_id");

        if (planId == null) {

            data.put("hasCareer", false);
            return data;
        }

        data.put("hasCareer", true);

        StudyPlan plan = StudyPlan.findById(planId);

        if (plan != null) {

            data.put("planName", plan.getString("name"));

            Career career = Career.findById(
                    plan.getLong("career_id")
            );

            if (career != null) {
                data.put("careerName", career.getString("name"));
            }
        }

        long enrollmentsCount = Enrollment.count(
                "student_id = ?",
                studentId
        );

        data.put("enrollmentsCount", enrollmentsCount);

        return data;
    }
}