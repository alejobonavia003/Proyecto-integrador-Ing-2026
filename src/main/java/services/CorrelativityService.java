package services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.javalite.activejdbc.Base;

import models.Correlativity;
import models.Subject;

/**
 * Servicio encargado de la gestión y verificación de correlatividades.
 */
public class CorrelativityService {

    /**
     * ISSUE 2: Gestión de Correlativas.
     * Asocia una materia con su predecesora.
     * * @param subjectId ID de la materia principal
     * @param requiredSubjectId ID de la materia que actúa como requisito
     * @param requiresApproved true si exige final aprobado, false si solo cursada regular
     */
    public void addCorrelativity(Long subjectId, Long requiredSubjectId, boolean requiresApproved) {
        if (subjectId.equals(requiredSubjectId)) {
            throw new IllegalArgumentException("Una materia no puede ser correlativa de sí misma.");
        }
        
        // Evitar duplicados
        Correlativity existing = Correlativity.findFirst("subject_id = ? AND required_subject_id = ?", subjectId, requiredSubjectId);
        if (existing != null) {
            throw new IllegalArgumentException("Esta relación de correlatividad ya existe.");
        }

        Correlativity correlativity = new Correlativity();
        correlativity.set("subject_id", subjectId);
        correlativity.set("required_subject_id", requiredSubjectId);
        correlativity.set("requires_approved", requiresApproved);
        
        if (!correlativity.save()) {
            throw new RuntimeException("Error al guardar la correlatividad: " + correlativity.errors());
        }
    }

    public List<String> getDependencyCodesForSubject(Long subjectId) {
        List<String> codes = new ArrayList<>();
        List<Correlativity> dependencies = Correlativity.where("subject_id = ?", subjectId);

        for (Correlativity dependency : dependencies) {
            Subject required = Subject.findById(dependency.get("required_subject_id"));
            if (required != null) {
                codes.add(required.getString("code"));
            }
        }

        return codes;
    }

    /**
     * ISSUE 1: Verificación de requisitos del alumno (Soporta múltiples niveles / recursivo).
     * * @param alumnoId ID del usuario (student_id)
     * @param materiaId ID de la materia a la que se quiere inscribir
     * @return Lista de materias faltantes (Si la lista está vacía, cumple con todo).
     */
    public List<Subject> verificarCorrelativas(Long alumnoId, Long materiaId) {
        List<Subject> materiasFaltantes = new ArrayList<>();
        Set<Long> visitados = new HashSet<>(); // Para evitar ciclos infinitos
        
        verificarCorrelativasRecursivo(alumnoId, materiaId, materiasFaltantes, visitados);
        
        return materiasFaltantes;
    }

    private void verificarCorrelativasRecursivo(Long alumnoId, Long currentSubjectId, List<Subject> faltantes, Set<Long> visitados) {
        if (visitados.contains(currentSubjectId)) {
            return;
        }
        visitados.add(currentSubjectId);

        // Buscar todas las materias requeridas directas de la materia actual
        List<Correlativity> correlativas = Correlativity.where("subject_id = ?", currentSubjectId);

        for (Correlativity req : correlativas) {
            Long requiredSubjectId = req.getLong("required_subject_id");
            boolean requiresApproved = req.getBoolean("requires_approved");

            // 1. Verificamos el estado del alumno frente a esta materia requerida
            boolean cumple = checkStudentStatusForSubject(alumnoId, requiredSubjectId, requiresApproved);
            
            if (!cumple) {
                Subject s = Subject.findById(requiredSubjectId);
                // Evitamos agregarla 2 veces si múltiples ramas fallan en el mismo nodo
                if (s != null && !faltantes.contains(s)) {
                    faltantes.add(s);
                }
            }

            // 2. Llamada recursiva: Evaluamos el sub-árbol de la materia requerida
            verificarCorrelativasRecursivo(alumnoId, requiredSubjectId, faltantes, visitados);
        }
    }

    /**
     * Verifica en la base de datos si el alumno cumple con la condición (Regular o Aprobado).
     */
    private boolean checkStudentStatusForSubject(Long studentId, Long requiredSubjectId, boolean requiresApproved) {
        // Consultamos el 'status' en la tabla grades relacionando las inscripciones y comisiones
        String query = "SELECT g.status " +
                       "FROM grades g " +
                       "INNER JOIN enrollments e ON g.enrollment_id = e.id " +
                       "INNER JOIN course_classes cc ON e.course_class_id = cc.id " +
                       "WHERE e.student_id = ? AND cc.subject_id = ?";
        
        // ActiveJDBC Base nos permite extraer la primera columna de forma fácil
        List<String> statuses = Base.firstColumn(query, studentId, requiredSubjectId);

        boolean hasRegular = false;
        boolean hasAprobado = false;

        for (String status : statuses) {
            // Asumimos nomenclaturas comunes (ajústalas según tu lógica en 'grades.status')
            if ("APROBADO".equalsIgnoreCase(status) || "PROMOCIONADO".equalsIgnoreCase(status)) {
                hasAprobado = true;
            } else if ("REGULAR".equalsIgnoreCase(status)) {
                hasRegular = true;
            }
        }

        if (requiresApproved) {
            // Si la correlativa exige final aprobado, solo importa si hasAprobado es true
            return hasAprobado;
        } else {
            // Si solo exige tenerla cursada, sirve tanto REGULAR como APROBADA
            return hasAprobado || hasRegular;
        }
    }
}