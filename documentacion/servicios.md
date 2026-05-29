# Responsabilidades de los Servicios

Este archivo describe de forma breve la responsabilidad principal de cada servicio en `src/main/java/services`.

- `AuthService`
  - Registro y autenticación de usuarios.
  - Hash de contraseñas con BCrypt.
  - Validaciones básicas de campos.

- `UserService`
  - Preparar vistas/DTOs para listados de usuarios.
  - Conteos y transformaciones usadas por controladores.

- `CareerService`
  - CRUD y vistas relacionadas a Carreras y Planes de Estudio.
  - Validaciones de unicidad de código.

- `SubjectService`
  - CRUD de materias, asignación a planes y generación de vistas para plantillas.
  - Cálculo de dependencias (correlatividades).

- `CorrelativityService`
  - Gestión de correlatividades (alta) y verificación recursiva de requisitos.
  - Lógica para interpretar estados (`REGULAR`, `APROBADO`, etc.).

- `EnrollmentService`
  - Lógica de inscripciones (listado de materias disponibles, creación de comisiones, enrollments).
  - Verificación de cupos y generación de comprobantes.
  - Filtros para docentes (materias titular, listados de alumnos por carrera/materia/comisión).
  - NOTA: `enrollStudent` ahora ejecuta la operación en una transacción para reducir condiciones de carrera.

- `TeacherAssignmentService`
  - Asignación de docentes a carreras y materias; generación de vistas auxiliares.

- `DashboardService`
  - Provee métricas y resumenes para dashboards por rol (admin, teacher, student).

Recomendaciones:
- Mantener los servicios finos y con responsabilidad única (Single Responsibility Principle).
- Introducir inyección de dependencias (constructor injection) para facilitar pruebas unitarias.
- Añadir tests unitarios para `AuthService` y `EnrollmentService` (cobertura mínima para reglas críticas).