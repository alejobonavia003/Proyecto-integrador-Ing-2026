package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Career;
import models.CourseClass;
import models.Enrollment; 
import models.StudyPlan;
import models.Subject;
import models.TeacherSubject;
import models.User;

public class EnrollmentService {

    // --- SECCIÓN ALUMNO ---

    /**
     * Obtiene las materias disponibles filtradas por el plan del alumno, 
     * excluyendo automáticamente las materias que ya aprobó.
     */
    public List<Map<String, Object>> getAvailableSubjectsForStudent(Long studentId) {
        List<Map<String, Object>> disponibles = new ArrayList<>();
        User student = User.findById(studentId);
        
        if (student != null && student.get("study_plan_id") != null) {
            Long planId = student.getLong("study_plan_id");
            List<Subject> materiasPlan = Subject.where("study_plan_id = ?", planId);
            
            // Obtener IDs de materias ya aprobadas por el alumno
            List<Long> aprobadasIds = getApprovedSubjectIds(studentId);
            
            for (Subject subject : materiasPlan) {
                // SQA: Filtrar automáticamente las materias aprobadas
                if (aprobadasIds.contains(subject.getLongId())) {
                    continue; 
                }

                Map<String, Object> row = new HashMap<>();
                row.put("id", subject.getId());
                row.put("name", subject.getString("name"));
                row.put("code", subject.getString("code"));
                
                List<CourseClass> comisiones = CourseClass.where("subject_id = ?", subject.getId());
                List<Map<String, Object>> comisionesList = new ArrayList<>();
                
                
                for (CourseClass c : comisiones) {
                    Map<String, Object> cMap = new HashMap<>();
                    cMap.put("id", c.getId());
                    cMap.put("name", c.getString("name") != null ? c.getString("name") : "Comisión " + c.getId());
                    
                    cMap.put("subject_id", subject.getId());
                    // SQA: Calcular y mostrar claramente los cupos restantes
                    int cupoMaximo = c.getInteger("capacity") != null ? c.getInteger("capacity") : 30;
                    long inscriptos = Enrollment.count("course_class_id = ?", c.getId());
                    long cuposRestantes = cupoMaximo - inscriptos;
                    
                    cMap.put("cupos_restantes", cuposRestantes);
                    cMap.put("has_cupo", cuposRestantes > 0);
                    
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

    /**
     * Retorna los IDs de las materias aprobadas por el alumno.
     * (Asume un estado 'APROBADA' o 'APPROVED' en la tabla de inscripciones/historial)
     */
    public List<Long> getApprovedSubjectIds(Long studentId) {
        List<Long> aprobadas = new ArrayList<>();
        // Buscamos registros donde el estado sea aprobada
        List<Enrollment> registros = Enrollment.where("student_id = ? AND status = 'APROBADA'", studentId);
        for (Enrollment e : registros) {
            CourseClass cc = CourseClass.findById(e.get("course_class_id"));
            if (cc != null) {
                aprobadas.add(cc.getLong("subject_id"));
            }
        }
        return aprobadas;
    }

    public void enrollStudent(Long studentId, Long courseClassId) {
        // Verificar cupo antes de guardar
        CourseClass c = CourseClass.findById(courseClassId);
        // CORREGIDO: Usar "capacity"
        int cupoMaximo = c.getInteger("capacity") != null ? c.getInteger("capacity") : 30;
        long inscriptos = Enrollment.count("course_class_id = ?", courseClassId);
        
        if (inscriptos >= cupoMaximo) {
            throw new IllegalStateException("No quedan cupos disponibles en esta comisión.");
        }

        Enrollment inscripcion = new Enrollment();
        inscripcion.set("student_id", studentId);
        inscripcion.set("course_class_id", courseClassId);
        inscripcion.set("status", "REGULAR");
        inscripcion.saveIt();
    }
    /**
     * Obtiene la información detallada para el Comprobante de Inscripción.
     */
    public Map<String, Object> getEnrollmentReceipt(Long enrollmentId) {
        Map<String, Object> receipt = new HashMap<>();
        Enrollment e = Enrollment.findById(enrollmentId);
        if (e != null) {
            receipt.put("id", e.getId());
            CourseClass cc = CourseClass.findById(e.get("course_class_id"));
            if (cc != null) {
                receipt.put("comision_name", cc.getString("name") != null ? cc.getString("name") : "Comisión " + cc.getId());
                Subject s = Subject.findById(cc.get("subject_id"));
                if (s != null) {
                    receipt.put("materia_name", s.getString("name"));
                    receipt.put("materia_code", s.getString("code"));
                }
            }
            User student = User.findById(e.get("student_id"));
            if (student != null) {
                receipt.put("student_name", student.getString("name"));
                receipt.put("student_dni", student.getString("dni"));
            }
        }
        return receipt;
    }
    // --- SECCIÓN PROFESOR / FILTROS ACADÉMICOS ---

    /**
     * Verifica si un usuario (profesor) es titular de una materia específica.
     */
    public boolean isTeacherTitular(Long teacherId, Long subjectId) {
        // CORREGIDO: Busca por 'teacher_id' y 'role_charge'
        return TeacherSubject.count("teacher_id = ? AND subject_id = ? AND role_charge = 'TITULAR'", teacherId, subjectId) > 0;
    }

    /**
     * Lista las materias de las cuales el profesor es titular.
     */
    public List<Subject> getSubjectsWhereTeacherIsTitular(Long teacherId) {
        // CORREGIDO: Busca por 'teacher_id' y 'role_charge'
        List<TeacherSubject> asignaciones = TeacherSubject.where("teacher_id = ? AND role_charge = 'TITULAR'", teacherId);
        List<Subject> materias = new ArrayList<>();
        
        for (TeacherSubject ts : asignaciones) {
            Subject s = Subject.findById(ts.get("subject_id"));
            if (s != null) {
                materias.add(s);
            }
        }
        return materias;
    }

    /**
     * Filtro 1: Alumnos inscriptos a una Carrera específica.
     */
    public List<User> getStudentsByCareer(Long careerId) {
        List<User> students = new ArrayList<>();
        List<StudyPlan> planes = StudyPlan.where("career_id = ?", careerId);
        for (StudyPlan plan : planes) {
            List<User> usersInPlan = User.where("study_plan_id = ? AND role_id = (SELECT id FROM roles WHERE name = 'STUDENT' OR name = 'Alumno' LIMIT 1)", plan.getId());
            students.addAll(usersInPlan);
        }
        return students;
    }

    /**
     * Filtro 2: Alumnos anotados en una Materia específica (a través de cualquiera de sus comisiones).
     */
    public List<User> getStudentsBySubject(Long subjectId) {
        List<User> students = new ArrayList<>();
        List<CourseClass> comisiones = CourseClass.where("subject_id = ?", subjectId);
        for (CourseClass cc : comisiones) {
            List<Enrollment> enrollments = Enrollment.where("course_class_id = ?", cc.getId());
            for (Enrollment e : enrollments) {
                User u = User.findById(e.get("student_id"));
                if (u != null && !students.contains(u)) {
                    students.add(u);
                }
            }
        }
        return students;
    }

    /**
     * Filtro 3: Alumnos anotados en una Comisión específica.
     */
    public List<User> getStudentsByCommission(Long courseClassId) {
        List<User> students = new ArrayList<>();
        List<Enrollment> enrollments = Enrollment.where("course_class_id = ?", courseClassId);
        for (Enrollment e : enrollments) {
            User u = User.findById(e.get("student_id"));
            if (u != null) {
                students.add(u);
            }
        }
        return students;
    }

    public List<Map<String, Object>> getStudentEnrollments(Long studentId) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Enrollment> records = Enrollment.where("student_id = ?", studentId);
        for (Enrollment e : records) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            CourseClass cc = CourseClass.findById(e.get("course_class_id"));
            if (cc != null) {
                Subject s = Subject.findById(cc.get("subject_id"));
                map.put("materia_name", s != null ? s.getString("name") : "Desconocida");
                map.put("comision_name", cc.getString("name") != null ? cc.getString("name") : "Comisión " + cc.getId());
            }
            list.add(map);
        }
        return list;
    }

    public List<Map<String, Object>> getAllStudyPlans() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<StudyPlan> plans = StudyPlan.findAll();
        for (StudyPlan p : plans) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getString("name"));
            Career c = Career.findById(p.get("career_id"));
            m.put("career_name", c != null ? c.getString("name") : "Sin Carrera");
            list.add(m);
        }
        return list;
    }

    public void assignPlanToStudent(Long studentId, Long planId) {
        User u = User.findById(studentId);
        if (u != null) {
            u.set("study_plan_id", planId);
            u.saveIt();
        }
    }
    
    public boolean belongsToStudentCareer(Long studentId, Long subjectId) {
        User student = User.findById(studentId);
        Subject subject = Subject.findById(subjectId);
        if (student == null || subject == null || student.get("study_plan_id") == null || subject.get("study_plan_id") == null) return false;
        return subject.getLong("study_plan_id").equals(student.getLong("study_plan_id"));
    }
}