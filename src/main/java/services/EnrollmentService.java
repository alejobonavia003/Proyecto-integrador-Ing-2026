package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;

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
     * Obtiene las materias disponibles filtradas por el plan del alumno, excluyendo automáticamente
     * las materias que ya aprobó.
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
                    cMap.put("name", c.getString("name") != null ? c.getString("name")
                            : "Comisión " + c.getId());

                    cMap.put("subject_id", subject.getId());
                    // SQA: Calcular y mostrar claramente los cupos restantes
                    int cupoMaximo =
                            c.getInteger("capacity") != null ? c.getInteger("capacity") : 30;
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

    public Long getLastEnrollmentId(Long studentId, Long courseClassId) {

        List<Enrollment> enrollments =
                Enrollment.where("student_id = ? AND course_class_id = ?", studentId, courseClassId)
                        .orderBy("id DESC");

        if (enrollments.isEmpty()) {
            return null;
        }

        return enrollments.get(0).getLongId();
    }

    public void createCommission(Long teacherId, Long subjectId, String name, int capacity) {

        CourseClass cc = new CourseClass();

        cc.set("subject_id", subjectId);
        cc.set("teacher_id", teacherId);
        cc.set("name", name);
        cc.set("capacity", capacity);

        cc.saveIt();
    }

   public Map<String, Object> getTeacherCommissionViewData(Long teacherId, String careerIdStr, String subjectIdStr, String courseClassIdStr) {
        Map<String, Object> model = new HashMap<>();

        // 1. Materias Titular (para el formulario de creación)
        List<Map<String, Object>> materiasTitularView = new ArrayList<>();
        List<Subject> materiasTitulares = getSubjectsWhereTeacherIsTitular(teacherId);
        for (Subject s : materiasTitulares) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("name", s.getString("name"));
            map.put("code", s.getString("code"));
            materiasTitularView.add(map);
        }
        model.put("materiasTitular", materiasTitularView);

        // 2. Filtro Carreras
        List<Map<String, Object>> carrerasFiltro = new ArrayList<>();
        for (org.javalite.activejdbc.Model c : models.Career.findAll()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getString("name"));
            if (careerIdStr != null && careerIdStr.equals(c.getId().toString())) map.put("selected", true);
            carrerasFiltro.add(map);
        }
        model.put("carrerasFiltro", carrerasFiltro);

        // 3. Filtro Materias (Filtradas por la Carrera elegida)
        List<Map<String, Object>> materiasFiltro = new ArrayList<>();
        List<Subject> subjects;
        if (careerIdStr != null && !careerIdStr.isEmpty()) {
            subjects = Subject.findBySQL("SELECT s.* FROM subjects s JOIN study_plans sp ON s.study_plan_id = sp.id WHERE sp.career_id = ?", Long.parseLong(careerIdStr));
        } else {
            subjects = Subject.findAll();
        }
        for (Subject s : subjects) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("name", s.getString("name"));
            if (subjectIdStr != null && subjectIdStr.equals(s.getId().toString())) map.put("selected", true);
            materiasFiltro.add(map);
        }
        model.put("materiasFiltro", materiasFiltro);

        // 4. Filtro Comisiones (Filtradas por la Materia elegida)
        List<Map<String, Object>> comisionesFiltro = new ArrayList<>();
        List<CourseClass> classes;
        if (subjectIdStr != null && !subjectIdStr.isEmpty()) {
            classes = CourseClass.where("subject_id = ?", Long.parseLong(subjectIdStr));
        } else {
            classes = CourseClass.findAll();
        }
        for (CourseClass cc : classes) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cc.getId());
            map.put("name", cc.getString("name") != null ? cc.getString("name") : "Comisión " + cc.getId());
            if (courseClassIdStr != null && courseClassIdStr.equals(cc.getId().toString())) map.put("selected", true);
            comisionesFiltro.add(map);
        }
        model.put("comisionesFiltro", comisionesFiltro);

        return model;
    }
    public Map<String, Object> getFilteredStudents(String careerIdParam, String subjectIdParam, String courseClassIdParam) {
        Map<String, Object> result = new HashMap<>();
        List<String> filtrosText = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder(
            "SELECT DISTINCT u.* FROM users u " +
            "JOIN enrollments e ON u.id = e.student_id " +
            "JOIN course_classes cc ON e.course_class_id = cc.id " +
            "JOIN subjects s ON cc.subject_id = s.id " +
            "JOIN study_plans sp ON s.study_plan_id = sp.id " +
            "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (careerIdParam != null && !careerIdParam.isEmpty()) {
            sql.append("AND sp.career_id = ? ");
            params.add(Long.parseLong(careerIdParam));
            filtrosText.add("Carrera");
        }
        if (subjectIdParam != null && !subjectIdParam.isEmpty()) {
            sql.append("AND s.id = ? ");
            params.add(Long.parseLong(subjectIdParam));
            filtrosText.add("Materia");
        }
        if (courseClassIdParam != null && !courseClassIdParam.isEmpty()) {
            sql.append("AND cc.id = ? ");
            params.add(Long.parseLong(courseClassIdParam));
            filtrosText.add("Comisión");
        }

        List<User> alumnosResultado;
        if (params.isEmpty()) {
            alumnosResultado = User.findBySQL("SELECT DISTINCT u.* FROM users u JOIN enrollments e ON u.id = e.student_id");
        } else {
            alumnosResultado = User.findBySQL(sql.toString(), params.toArray());
        }

        List<Map<String, Object>> alumnosView = new ArrayList<>();
        for (User u : alumnosResultado) {
            Map<String, Object> map = new HashMap<>();
            map.put("dni", u.getString("dni"));
            map.put("name", u.getString("name"));
            map.put("email", u.getString("email"));
            alumnosView.add(map);
        }

        result.put("alumnos", alumnosView);
        result.put("filtroAplicado", filtrosText.isEmpty() ? "Ninguno (Muestra todos los estudiantes)" : String.join(" + ", filtrosText));

        return result;
    }

    public List<Map<String, Object>> getSubjectsWhereTeacherIsTitularView(Long teacherId) {
        // Método eliminado: view mapping moved/removed — use getSubjectsWhereTeacherIsTitular
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getCareersFilterView() {

        List<Map<String, Object>> careersView = new ArrayList<>();

        for (Model c : Career.findAll()) {

            Map<String, Object> map = new HashMap<>();

            map.put("id", c.getId());
            map.put("name", c.getString("name"));

            careersView.add(map);
        }

        return careersView;
    }

    public List<Map<String, Object>> getSubjectsFilterView() {

        List<Map<String, Object>> subjectsView = new ArrayList<>();

        for (Model s : Subject.findAll()) {

            Map<String, Object> map = new HashMap<>();

            map.put("id", s.getId());
            map.put("name", s.getString("name"));

            subjectsView.add(map);
        }

        return subjectsView;
    }

    public List<Map<String, Object>> getCourseClassesFilterView() {
        // Deprecated: view helper removed to reduce dead code. Use getTeacherCommissionViewData
        // instead.
        return new ArrayList<>();
    }

    public List<Map<String, Object>> mapStudentsView(List<User> students) {
        // Utility removed: mapStudentsView is unused. Return empty list to avoid breaking callers.
        return new ArrayList<>();
    }

    /**
     * Retorna los IDs de las materias aprobadas por el alumno. (Asume un estado 'APROBADA' o
     * 'APPROVED' en la tabla de inscripciones/historial)
     */
    public List<Long> getApprovedSubjectIds(Long studentId) {
        List<Long> aprobadas = new ArrayList<>();
        // Buscamos registros donde el estado sea aprobada
        List<Enrollment> registros =
                Enrollment.where("student_id = ? AND status = 'APROBADA'", studentId);
        for (Enrollment e : registros) {
            CourseClass cc = CourseClass.findById(e.get("course_class_id"));
            if (cc != null) {
                aprobadas.add(cc.getLong("subject_id"));
            }
        }
        return aprobadas;
    }

    public void enrollStudent(Long studentId, Long courseClassId) {
        // Intentar inscripción de forma transaccional para reducir race-conditions.
        Base.openTransaction();
        try {
            CourseClass c = CourseClass.findById(courseClassId);
            if (c == null) {
                Base.rollbackTransaction();
                throw new IllegalArgumentException("La comisión seleccionada no existe.");
            }

            // --- NUEVA VALIDACIÓN: Verificar si el alumno ya está inscripto a esta comisión ---
            long inscripcionesPrevias = Enrollment.count("student_id = ? AND course_class_id = ?", studentId, courseClassId);
            if (inscripcionesPrevias > 0) {
                Base.rollbackTransaction();
                // Lanzamos la misma excepción que tu controlador ya sabe cómo manejar en el bloque catch
                throw new IllegalStateException("Ya te encuentras inscripto en esta comisión.");
            }
            // ----------------------------------------------------------------------------------

            int cupoMaximo = c.getInteger("capacity") != null ? c.getInteger("capacity") : 30;
            long inscriptos = Enrollment.count("course_class_id = ?", courseClassId);

            if (inscriptos >= cupoMaximo) {
                Base.rollbackTransaction();
                throw new IllegalStateException("No quedan cupos disponibles en esta comisión.");
            }

            Enrollment inscripcion = new Enrollment();
            inscripcion.set("student_id", studentId);
            inscripcion.set("course_class_id", courseClassId);
            inscripcion.set("status", "REGULAR");
            inscripcion.saveIt();

            Base.commitTransaction();
        } catch (RuntimeException e) {
            Base.rollbackTransaction();
            throw e;
        } catch (Exception e) {
            Base.rollbackTransaction();
            throw new RuntimeException(e);
        }
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
                receipt.put("comision_name", cc.getString("name") != null ? cc.getString("name")
                        : "Comisión " + cc.getId());
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
        return TeacherSubject.count("teacher_id = ? AND subject_id = ? AND role_charge = 'TITULAR'",
                teacherId, subjectId) > 0;
    }

    /**
     * Lista las materias de las cuales el profesor es titular.
     */
    public List<Subject> getSubjectsWhereTeacherIsTitular(Long teacherId) {
        // CORREGIDO: Busca por 'teacher_id' y 'role_charge'
        List<TeacherSubject> asignaciones =
                TeacherSubject.where("teacher_id = ? AND role_charge = 'TITULAR'", teacherId);
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
            List<User> usersInPlan = User.where(
                    "study_plan_id = ? AND role_id = (SELECT id FROM roles WHERE name = 'STUDENT' OR name = 'Alumno' LIMIT 1)",
                    plan.getId());
            students.addAll(usersInPlan);
        }
        return students;
    }

    /**
     * Filtro 2: Alumnos anotados en una Materia específica (a través de cualquiera de sus
     * comisiones).
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
                map.put("comision_name", cc.getString("name") != null ? cc.getString("name")
                        : "Comisión " + cc.getId());
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
        if (student == null || subject == null || student.get("study_plan_id") == null
                || subject.get("study_plan_id") == null)
            return false;
        return subject.getLong("study_plan_id").equals(student.getLong("study_plan_id"));
    }


    // Verifica si el alumno ya tiene un plan de estudios asignado
    public boolean hasCareer(Long studentId) {

        User student = User.findById(studentId);

        return student != null
                && student.get("study_plan_id") != null;
    }


    
}
