package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.CourseClass;
import models.Enrollment; 
import models.Subject;
import models.User; 

public class EnrollmentService {

    public List<Map<String, Object>> getStudentEnrollments(Long studentId) {
        List<Map<String, Object>> inscripcionesView = new ArrayList<>();
        List<Enrollment> registros = Enrollment.where("student_id = ?", studentId);
        
        for (Enrollment e : registros) {
            Map<String, Object> row = new HashMap<>();
            Long courseClassId = e.getLong("course_class_id");
            CourseClass comision = CourseClass.findById(courseClassId);
            
            if (comision != null) {
                Subject materia = Subject.findById(comision.getLong("subject_id"));
                row.put("comision_id", courseClassId);
                row.put("materia_name", materia != null ? materia.getString("name") : "Desconocida");
            }
            inscripcionesView.add(row);
        }
        return inscripcionesView;
    }

    public List<Map<String, Object>> getAvailableSubjectsForStudent(Long studentId) {
        List<Map<String, Object>> disponibles = new ArrayList<>();
        User student = User.findById(studentId);
        
        if (student != null && student.get("study_plan_id") != null) {
            Long planId = student.getLong("study_plan_id");
            List<Subject> materiasPlan = Subject.where("study_plan_id = ?", planId);
            
            for(Subject subject : materiasPlan) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", subject.getId());
                row.put("name", subject.getString("name"));
                row.put("code", subject.getString("code"));
                
                List<CourseClass> comisiones = CourseClass.where("subject_id = ?", subject.getId());
                List<Map<String, Object>> comisionesList = new ArrayList<>();
                for (CourseClass c : comisiones) {
                    Map<String, Object> cMap = new HashMap<>();
                    cMap.put("id", c.getId());
                    cMap.put("name", "Comisión " + c.getId()); 
                    comisionesList.add(cMap);
                }
                
                if (!comisionesList.isEmpty()) {
                    row.put("hasComisiones", true);
                    row.put("comisiones", comisionesList);
                }
                disponibles.add(row);
            }
        }
        return disponibles;
    }

    public boolean belongsToStudentCareer(Long studentId, Long subjectId) {
        User student = User.findById(studentId);
        Subject subject = Subject.findById(subjectId);
        if (student == null || subject == null || student.get("study_plan_id") == null || subject.get("study_plan_id") == null) return false;
        return subject.getLong("study_plan_id").equals(student.getLong("study_plan_id"));
    }
    
    public void enrollStudent(Long studentId, Long courseClassId) {
        Enrollment inscripcion = new Enrollment();
        inscripcion.set("student_id", studentId);
        inscripcion.set("course_class_id", courseClassId);
        inscripcion.saveIt();
    }


    /**
     * Trae todos los planes de estudio junto con el nombre de su carrera.
     */
    public List<Map<String, Object>> getAllStudyPlans() {
        List<Map<String, Object>> planesView = new ArrayList<>();
        List<models.StudyPlan> planes = models.StudyPlan.findAll();
        
        for (models.StudyPlan plan : planes) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", plan.getId());
            row.put("name", plan.getString("name"));
            
            models.Career career = models.Career.findById(plan.getLong("career_id"));
            if (career != null) {
                row.put("career_name", career.getString("name"));
            } else {
                row.put("career_name", "Carrera no asignada");
            }
            planesView.add(row);
        }
        return planesView;
    }

    /**
     * Guarda el plan de estudio seleccionado en el perfil del usuario (alumno).
     */
    public void assignPlanToStudent(Long studentId, Long planId) {
        models.User student = models.User.findById(studentId);
        if (student != null) {
            student.set("study_plan_id", planId);
            student.saveIt();
        }
    }
}