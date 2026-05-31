package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.FinalExam;
import models.FinalExamEnrollment;
import models.Subject;
import models.User;

public class FinalExamService {

    private CorrelativityService correlativityService;

    public FinalExamService() {
        this.correlativityService = new CorrelativityService();
    }

    // ==========================================
    // ROL ADMIN: Crear instancia de examen
    // ==========================================
    public void createExamInstance(Long subjectId, String examDate) {
        FinalExam exam = new FinalExam();
        exam.set("subject_id", subjectId);
        exam.set("exam_date", examDate);
        if (!exam.save()) {
            throw new RuntimeException("Error al crear la mesa de examen: " + exam.errors());
        }
    }

    // ==========================================
    // ROL ALUMNO: Inscribirse a examen
    // ==========================================
    public void enrollStudentInFinal(Long studentId, Long finalExamId) throws Exception {
        FinalExam exam = FinalExam.findById(finalExamId);
        if (exam == null) {
            throw new IllegalArgumentException("El examen no existe.");
        }

        Long subjectId = exam.getLong("subject_id");

        // VERIFICACIÓN AUTOMÁTICA DE CORRELATIVIDADES
        // Usamos el servicio existente que devuelve la lista de materias faltantes
        List<Subject> faltantes = correlativityService.verificarCorrelativas(studentId, subjectId);

        if (!faltantes.isEmpty()) {
            StringBuilder msj = new StringBuilder("No puedes anotarte. Te faltan aprobar/regularizar las siguientes correlativas: ");
            for (Subject s : faltantes) {
                msj.append(s.getString("name")).append(" (").append(s.getString("code")).append("), ");
            }
            throw new Exception(msj.toString());
        }

        // Si la lista de faltantes está vacía, procede la inscripción.
        // La fecha de inscripción ('enrolled_at') se registra automáticamente por la BD.
        try {
            FinalExamEnrollment enrollment = new FinalExamEnrollment();
            enrollment.set("final_exam_id", finalExamId);
            enrollment.set("student_id", studentId);
            enrollment.saveIt();
        } catch (Exception e) {
            throw new Exception("Ya te encuentras inscripto en este examen.");
        }
    }

    // ==========================================
    // ROL PROFESOR: Ver listado de inscriptos
    // ==========================================
    public List<Map<String, Object>> getEnrolledStudentsForTeacher(Long teacherId, Long finalExamId) {
        List<Map<String, Object>> studentsData = new ArrayList<>();
        
        // Obtenemos los alumnos inscriptos al examen
        List<FinalExamEnrollment> enrollments = FinalExamEnrollment.where("final_exam_id = ?", finalExamId);
        
        for (FinalExamEnrollment enrollment : enrollments) {
            User student = User.findById(enrollment.get("student_id"));
            if (student != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", student.getString("name"));
                map.put("dni", student.getString("dni"));
                map.put("enrolled_at", enrollment.getTimestamp("enrolled_at")); // Mostramos la fecha automática
                studentsData.add(map);
            }
        }
        return studentsData;
    }


    // ==========================================
    // METODO COMPARTIDO: Obtener todas las mesas
    // ==========================================
    public List<Map<String, Object>> getAllExamsView() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<FinalExam> exams = FinalExam.findAll();
        
        for (FinalExam exam : exams) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", exam.getId());
            // Formatear un poco la fecha si viene con la T del input datetime-local
            map.put("exam_date", exam.getString("exam_date").replace("T", " ")); 
            
            Subject subject = Subject.findById(exam.get("subject_id"));
            if (subject != null) {
                map.put("subject_name", subject.getString("name"));
                map.put("subject_code", subject.getString("code"));
            }
            result.add(map);
        }
        return result;
    }
}