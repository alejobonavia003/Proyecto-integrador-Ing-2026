package services;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.FinalExam;
import models.FinalExamEnrollment;
import models.StudentSubject;
import models.Subject;
import models.TeacherSubject;
import models.User;

public class FinalExamService {

    private CorrelativityService correlativityService;

    public FinalExamService() {
        this.correlativityService = new CorrelativityService();
    }

    // ==========================================
    // ROL ADMIN: Crear instancia de examen
    // ==========================================
    public void createExamInstance(Long subjectId, String registrationStart, String registrationEnd,
            String examDate) {
        TeacherSubject titular = TeacherSubject.findFirst(
                "subject_id = ? AND role_charge = 'TITULAR'", subjectId);
        if (titular == null) {
            throw new IllegalStateException("No se puede crear la mesa porque no existe un titular para la materia.");
        }

        FinalExam exam = new FinalExam();
        exam.set("subject_id", subjectId);
        exam.set("teacher_id", titular.getLong("teacher_id"));
        exam.set("registration_start", Timestamp.valueOf(LocalDateTime.parse(registrationStart)));
        exam.set("registration_end", Timestamp.valueOf(LocalDateTime.parse(registrationEnd)));
        exam.set("exam_date", Timestamp.valueOf(LocalDateTime.parse(examDate)));

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

        if (!isWithinRegistrationPeriod(exam)) {
            throw new IllegalStateException("No puedes anotarte, el período de inscripción no está abierto.");
        }

        Long subjectId = exam.getLong("subject_id");

        // VERIFICACIÓN AUTOMÁTICA DE CORRELATIVIDADES
        List<Subject> faltantes = correlativityService.verificarCorrelativas(studentId, subjectId);

        if (!faltantes.isEmpty()) {
            StringBuilder msj = new StringBuilder(
                    "No puedes anotarte. Te faltan aprobar/regularizar las siguientes correlativas: ");
            for (Subject s : faltantes) {
                msj.append(s.getString("name")).append(" (").append(s.getString("code")).append("), ");
            }
            throw new Exception(msj.toString());
        }

        try {
            FinalExamEnrollment enrollment = new FinalExamEnrollment();
            enrollment.set("final_exam_id", finalExamId);
            enrollment.set("student_id", studentId);
            enrollment.set("status", FinalExamEnrollment.STATUS_INSCRIPTO);
            enrollment.saveIt();
        } catch (Exception e) {
            throw new Exception("Ya te encuentras inscripto en este examen.");
        }
    }

    // ==========================================
    // ROL PROFESOR: Ver listado de inscriptos
    // ==========================================
    public List<Map<String, Object>> getEnrolledStudentsForTeacher(Long teacherId, Long finalExamId) {
        FinalExam exam = FinalExam.findById(finalExamId);
        if (exam == null) {
            throw new IllegalArgumentException("El examen no existe.");
        }

        if (!teacherId.equals(exam.getLong("teacher_id"))) {
            throw new SecurityException("No tienes permiso para ver los inscriptos de este examen.");
        }

        List<Map<String, Object>> studentsData = new ArrayList<>();
        List<FinalExamEnrollment> enrollments = FinalExamEnrollment.where("final_exam_id = ?", finalExamId);

        for (FinalExamEnrollment enrollment : enrollments) {
            User student = User.findById(enrollment.get("student_id"));
            if (student != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("enrollment_id", enrollment.getId());
                map.put("name", student.getString("name"));
                map.put("dni", student.getString("dni"));
                map.put("enrolled_at", enrollment.getTimestamp("enrolled_at"));
                map.put("grade", enrollment.get("grade"));
                map.put("status", enrollment.getString("status"));
                studentsData.add(map);
            }
        }
        return studentsData;
    }

    // ==========================================
    // ROL PROFESOR: Cargar resultado de final
    // ==========================================
    public void loadFinalResult(Long teacherId, Long enrollmentId, Double grade, String status) {
        FinalExamEnrollment enrollment = FinalExamEnrollment.findById(enrollmentId);
        if (enrollment == null) {
            throw new IllegalArgumentException("La inscripción al examen no existe.");
        }

        FinalExam exam = FinalExam.findById(enrollment.getLong("final_exam_id"));
        if (exam == null) {
            throw new IllegalStateException("No se pudo encontrar la mesa de examen asociada.");
        }

        if (!teacherId.equals(exam.getLong("teacher_id"))) {
            throw new SecurityException("No tienes permiso para cargar resultados de este examen.");
        }

        if (status != null && FinalExamEnrollment.STATUS_AUSENTE.equalsIgnoreCase(status.trim())) {
            enrollment.set("status", FinalExamEnrollment.STATUS_AUSENTE);
            enrollment.set("grade", null);
            if (!enrollment.save()) {
                throw new RuntimeException("Error al guardar el resultado del examen: " + enrollment.errors());
            }
            return;
        }

        if (grade == null) {
            throw new IllegalArgumentException("Debe cargar una nota o marcar al alumno como AUSENTE.");
        }

        String computedStatus = FinalExamEnrollment.STATUS_DESAPROBADO;
        if (grade >= 5.0) {
            computedStatus = FinalExamEnrollment.STATUS_APROBADO;
        }

        if (status != null && !status.trim().isEmpty()) {
            String normalized = status.trim().toUpperCase();
            if (FinalExamEnrollment.STATUS_APROBADO.equals(normalized)
                    || FinalExamEnrollment.STATUS_DESAPROBADO.equals(normalized)) {
                computedStatus = normalized;
            }
        }

        enrollment.set("grade", grade);
        enrollment.set("status", computedStatus);

        if (!enrollment.save()) {
            throw new RuntimeException("Error al guardar el resultado del examen: " + enrollment.errors());
        }

        if (FinalExamEnrollment.STATUS_APROBADO.equals(computedStatus)) {
            markSubjectApproved(enrollment.getLong("student_id"), exam.getLong("subject_id"), exam.getLong("id"), grade);
        }
    }

    public void loadFinalResult(Long teacherId, Long enrollmentId, Double grade) {
        loadFinalResult(teacherId, enrollmentId, grade, null);
    }

    private void markSubjectApproved(Long studentId, Long subjectId, Long finalExamId, Double grade) {
        long already = StudentSubject.count("student_id = ? AND subject_id = ?", studentId, subjectId);
        if (already > 0) {
            return;
        }

        StudentSubject record = new StudentSubject();
        record.set("student_id", studentId);
        record.set("subject_id", subjectId);
        record.set("final_exam_id", finalExamId);
        record.set("grade", grade);
        record.saveIt();
    }

    private boolean isWithinRegistrationPeriod(FinalExam exam) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp start = exam.getTimestamp("registration_start");
        Timestamp end = exam.getTimestamp("registration_end");
        return start != null && end != null && !now.before(start) && !now.after(end);
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

    // ==========================================
    // ROL ALUMNO: Obtener mesas disponibles filtradas
    // ==========================================
    public List<Map<String, Object>> getAvailableExamsForStudent(Long studentId) {
        List<Map<String, Object>> result = new ArrayList<>();
        User student = User.findById(studentId);

        if (student == null || student.get("study_plan_id") == null) {
            return result;
        }

        Long planId = student.getLong("study_plan_id");
        List<FinalExam> allExams = FinalExam.findAll();

        for (FinalExam exam : allExams) {
            if (!isWithinRegistrationPeriod(exam)) {
                continue;
            }

            Subject subject = Subject.findById(exam.get("subject_id"));
            if (subject == null || subject.getLong("study_plan_id") == null
                    || !subject.getLong("study_plan_id").equals(planId)) {
                continue;
            }

            long count = FinalExamEnrollment.count("student_id = ? AND final_exam_id = ?", studentId, exam.getId());
            if (count > 0) {
                continue;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("id", exam.getId());
            map.put("exam_date", exam.getString("exam_date").replace("T", " "));
            map.put("subject_name", subject.getString("name"));
            map.put("subject_code", subject.getString("code"));
            result.add(map);
        }
        return result;
    }

    // ==========================================
    // ROL ALUMNO: Obtener sus inscripciones/historial de finales
    // ==========================================
    public List<Map<String, Object>> getStudentExamEnrollments(Long studentId) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<FinalExamEnrollment> enrollments = FinalExamEnrollment.where("student_id = ?", studentId);

        for (FinalExamEnrollment enrollment : enrollments) {
            FinalExam exam = FinalExam.findById(enrollment.get("final_exam_id"));
            if (exam != null) {
                Subject subject = Subject.findById(exam.get("subject_id"));
                if (subject != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("exam_date", exam.getString("exam_date").replace("T", " "));
                    map.put("subject_name", subject.getString("name"));
                    map.put("subject_code", subject.getString("code"));
                    map.put("enrolled_at", enrollment.getTimestamp("enrolled_at"));
                    map.put("grade", enrollment.get("grade"));
                    map.put("status", enrollment.getString("status"));
                    result.add(map);
                }
            }
        }
        return result;
    }
}
