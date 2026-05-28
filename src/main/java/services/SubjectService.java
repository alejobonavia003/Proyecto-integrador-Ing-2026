package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import models.Correlativity;
import models.Subject;

/**
 * Capa de Servicio encargada de la lógica de negocio
 * relacionada con las materias.
 */
public class SubjectService {

    /**
     * Registra una nueva materia.
     */
    public Subject createSubject(
            String name,
            String code,
            Integer weeklyHours,
            String modality,
            Long studyPlanId) {

        validateNotBlank(name, "Nombre");
        validateNotBlank(code, "Código");
        validateNotBlank(modality, "Modalidad");

        if (weeklyHours == null || weeklyHours <= 0) {
            throw new IllegalArgumentException(
                    "La carga horaria debe ser mayor a 0.");
        }

        // Verificamos código repetido
        Optional<Subject> existing = Optional.ofNullable(
                Subject.findFirst("code = ?", code));

        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe una materia con ese código.");
        }

        Subject subject = new Subject();

        subject.set("name", name);
        subject.set("code", code);
        subject.set("weekly_hours", weeklyHours);
        subject.set("modality", modality);
        subject.set("study_plan_id", studyPlanId);

        subject.saveIt();

        return subject;
    }

    public List<Map<String, Object>> getAllSubjectsView() {
        List<Map<String, Object>> subjectsView = new ArrayList<>();

        for (Subject subject : getAllSubjects()) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", subject.getId());
            row.put("name", subject.getString("name"));
            row.put("code", subject.getString("code"));
            row.put("weekly_hours", subject.getInteger("weekly_hours"));
            row.put("modality", subject.getString("modality"));
            subjectsView.add(row);
        }

        return subjectsView;
    }

    public List<Subject> getAllSubjects() {
        return Subject.findAll();
    }

    public Subject getSubjectById(Long id) {
        return Subject.findById(id);
    }

    public List<Subject> getSubjectsByPlanId(Long planId) {
        return Subject.where("study_plan_id = ?", planId);
    }

    public List<Map<String, Object>> getPlanSubjectsOptions(Long planId) {
        List<Map<String, Object>> subjects = new ArrayList<>();
        for (Subject subject : getSubjectsByPlanId(planId)) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", subject.getId());
            row.put("code", subject.getString("code"));
            row.put("name", subject.getString("name"));
            subjects.add(row);
        }
        return subjects;
    }

    public Long createSubjectReturningId(
            String name,
            String code,
            Integer weeklyHours,
            String modality,
            Long studyPlanId) {
        Subject subject = createSubject(name, code, weeklyHours, modality, studyPlanId);
        return subject.getLong("id");
    }

    public List<Map<String, Object>> getPlanCoursesView(Long planId) {
        List<Map<String, Object>> coursesView = new ArrayList<>();
        for (Subject subject : getSubjectsByPlanId(planId)) {
            Map<String, Object> row = new HashMap<>();
            row.put("code", subject.getString("code"));
            row.put("name", subject.getString("name"));
            row.put("weekly_hours", subject.get("weekly_hours"));
            List<String> codes = getDependencyCodes(subject.getLong("id"));
            row.put("dependencies", codes.isEmpty() ? "Ninguna" : String.join(", ", codes));
            coursesView.add(row);
        }
        return coursesView;
    }

    public List<Map<String, Object>> getAllSubjectsViewForPlan(Long planId) {
        List<Subject> allSubjects = getAllSubjects();
        List<Subject> currentPlanSubjects = getSubjectsByPlanId(planId);
        HashSet<Long> assignedIds = new HashSet<>();

        for (Subject assigned : currentPlanSubjects) {
            assignedIds.add(assigned.getLong("id"));
        }

        List<Map<String, Object>> allSubjectsView = new ArrayList<>();

        for (Subject subject : allSubjects) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", subject.getId());
            row.put("name", subject.getString("name"));
            row.put("code", subject.getString("code"));
            row.put("weekly_hours", subject.get("weekly_hours"));
            row.put("modality", subject.getString("modality"));
            row.put("selected", assignedIds.contains(subject.getLong("id")));
            List<String> dependencies = getDependencyCodes(subject.getLong("id"));
            row.put("dependencies", dependencies.isEmpty() ? "Ninguna" : String.join(", ", dependencies));
            allSubjectsView.add(row);
        }

        return allSubjectsView;
    }

    public void assignSubjectsToPlan(Long planId, String[] subjectIds) {
        if (subjectIds == null) {
            return;
        }

        for (String idString : subjectIds) {
            if (idString == null || idString.trim().isEmpty()) {
                continue;
            }
            Subject subject = getSubjectById(Long.parseLong(idString));
            if (subject != null) {
                subject.set("study_plan_id", planId);
                subject.saveIt();
            }
        }
    }

    private List<String> getDependencyCodes(Long subjectId) {
        List<String> codes = new ArrayList<>();
        List<Correlativity> correlativities = Correlativity.where("subject_id = ?", subjectId);

        for (Correlativity correlativity : correlativities) {
            Subject requiredSubject = Subject.findById(correlativity.get("required_subject_id"));
            if (requiredSubject != null) {
                codes.add(requiredSubject.getString("code"));
            }
        }

        return codes;
    }

    /**
     * Valida campos vacíos.
     */
    private void validateNotBlank(
            String value,
            String fieldName) {

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " es requerido.");
        }
    }

    /**
     * Obtiene la cantidad total de materias registradas en el sistema.
     */
    public long getTotalSubjectsCount() {
        return models.Subject.count();
    }
}