package services;

import java.util.Optional;

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
            Long studyPlanId
    ) {

        validateNotBlank(name, "Nombre");
        validateNotBlank(code, "Código");
        validateNotBlank(modality, "Modalidad");

        if (weeklyHours == null || weeklyHours <= 0) {
            throw new IllegalArgumentException(
                    "La carga horaria debe ser mayor a 0."
            );
        }

     

        // Verificamos código repetido
        Optional<Subject> existing =
                Optional.ofNullable(
                        Subject.findFirst("code = ?", code)
                );

        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe una materia con ese código."
            );
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

    /**
     * Valida campos vacíos.
     */
    private void validateNotBlank(
            String value,
            String fieldName
    ) {

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " es requerido."
            );
        }
    }

    /**
     * Obtiene la cantidad total de materias registradas en el sistema.
     */
    public long getTotalSubjectsCount() {
        return models.Subject.count(); 
    }
}