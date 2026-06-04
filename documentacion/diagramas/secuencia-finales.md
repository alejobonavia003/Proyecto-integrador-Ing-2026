``` mermaid
sequenceDiagram

actor Admin
actor Alumno
actor Profesor

participant FinalExamService
participant CorrelativityService
participant FinalExam
participant FinalExamEnrollment
participant Enrollment

%% =========================
%% CREACION DEL FINAL
%% =========================

Admin->>FinalExamService: Crear mesa de examen

FinalExamService->>FinalExamService: Verificar titular de la materia

alt No existe titular
    FinalExamService-->>Admin: Error
else Existe titular
    FinalExamService->>FinalExam: Crear mesa
    Note over FinalExam: subject_id<br/>teacher_id<br/>registration_start<br/>registration_end<br/>exam_date
    FinalExam-->>Admin: Mesa creada
end

%% =========================
%% INSCRIPCION DEL ALUMNO
%% =========================

Alumno->>FinalExamService: Consultar mesas disponibles

FinalExamService->>FinalExam: Buscar mesas

FinalExam-->>FinalExamService: Lista de mesas

FinalExamService-->>Alumno: Mostrar mesas abiertas

Alumno->>FinalExamService: Inscribirse

FinalExamService->>FinalExamService: Validar período inscripción

alt Fuera de período
    FinalExamService-->>Alumno: Error
else Dentro de período

    FinalExamService->>CorrelativityService: Verificar correlativas

    alt No cumple correlativas
        CorrelativityService-->>Alumno: Rechazar inscripción
    else Cumple correlativas

        FinalExamService->>FinalExamEnrollment: Crear inscripción

        Note over FinalExamEnrollment: status = INSCRIPTO

        FinalExamEnrollment-->>Alumno: Inscripción exitosa
    end
end

%% =========================
%% DIA DEL EXAMEN
%% =========================

Profesor->>FinalExamService: Ver alumnos inscriptos

FinalExamService->>FinalExamEnrollment: Obtener inscriptos

FinalExamEnrollment-->>Profesor: Listado alumnos

%% =========================
%% CARGA DE RESULTADO
%% =========================

Profesor->>FinalExamService: Cargar resultado

alt Alumno ausente

    FinalExamService->>FinalExamEnrollment: status = AUSENTE

    FinalExamEnrollment-->>Profesor: Resultado guardado

else Alumno rindió

    Profesor->>FinalExamService: Ingresar nota

    alt Nota >= 5

        FinalExamService->>FinalExamEnrollment: grade = nota
        FinalExamService->>FinalExamEnrollment: status = APROBADO

        FinalExamService->>Enrollment: Marcar materia aprobada

        Note over Enrollment: La materia queda aprobada<br/>para futuras correlativas

        Enrollment-->>Profesor: Materia aprobada

    else Nota < 5

        FinalExamService->>FinalExamEnrollment: grade = nota
        FinalExamService->>FinalExamEnrollment: status = DESAPROBADO

        FinalExamEnrollment-->>Profesor: Resultado guardado

    end
end

%% =========================
%% FUTURAS CORRELATIVAS
%% =========================

Alumno->>CorrelativityService: Intentar cursar/rendir otra materia

CorrelativityService->>Enrollment: Consultar materias aprobadas

Enrollment-->>CorrelativityService: Historial académico

CorrelativityService-->>Alumno: Correlativas habilitadas

```

