# API Endpoints

Fecha: 2026-05-29

Este documento lista las rutas HTTP expuestas por la aplicación, los parámetros esperados y notas rápidas.

Formato: METHOD  PATH  —  descripción

---

## Autenticación

- GET  /  — Página de login. Query params: `error`, `successMessage`.
- GET  /user/create  — Formulario de registro de usuario. Query params: `error`, `message`.
- GET  /logout  — Cierra sesión.
- POST /user/new  — Registrar nuevo usuario (HEADS UP: actualmente fuerza `role = 1` en backend).
    - Form params: `name`, `password`, `email`, `dni`.
- POST /login  — Iniciar sesión.
    - Form params: `username`, `password`.

## Usuarios (Admin)

- GET  /admin/users  — Listado de usuarios.
    - Query params: `roleFilter` ("2"=Docente, "3"=Alumno, "ALL").
- GET  /admin/users/new  — Formulario nuevo usuario (admin view).
- POST /admin/users/new  — Crear usuario por admin.
    - Form params: `name`, `password`, `email`, `dni`, `role` (numeric id).

## Carreras y Planes

- GET  /admin/careers  — Listado de carreras.
- GET  /admin/careers/new  — Form nuevo carrera.
- POST /admin/careers/new  — Crear carrera.
    - Form params: `name`, `code`, `duration` (int, opcional).
- GET  /admin/careers/:id  — Detalle de carrera (muestra planes asociados).

### Planes

- GET  /admin/careers/:id/plans/new  — Form crear plan para la carrera `:id`.
- POST /admin/careers/:id/plans/new  — Crear plan.
    - Form params: `name`, `code`, `version`.
- GET  /admin/careers/:career_id/plans/:plan_id/subjects  — Gestión de materias del plan.
- POST /admin/careers/:career_id/plans/:plan_id/subjects/assign  — Asignar materias al plan.
    - Form params: `subject_ids` (array de ids seleccionadas).
- POST /admin/careers/:career_id/plans/:plan_id/subjects/new  — Crear materia dentro del plan.
    - Form params: `name`, `code`, `weekly_hours`, `modality`, optional `required_subject_id`, `requires_approved`.
- GET  /admin/careers/:career_id/plans/:plan_id  — Ver detalle del plan y sus materias.

## Materias

- GET  /admin/subjects  — Listado de materias.
- GET  /admin/subjects/new  — Form crear materia.
- POST /admin/subjects/new  — Crear materia.
    - Form params: `name`, `code`, `weekly_hours`, `modality`, optional `study_plan_id`.
- POST /admin/subjects/:id/correlativities  — Añadir correlativa a la materia `:id`.
    - Form params: `required_subject_id`, optional `requires_approved` (flag).

## Correlatividades

- Lógica expuesta vía controller/servicio: `CorrelativityService.addCorrelativity(subjectId, requiredSubjectId, requiresApproved)` — usada por rutas de materias/planes.

## Inscripciones (Estudiantes)

- GET  /student/mis-inscripciones  — Listado de inscripciones del estudiante (vista de comprobante/estado).
- GET  /student/inscripciones/nueva  — Form de inscripción (lista materias disponibles según plan).
    - Query params: `error` (mensaje), session attr `errorMateriasFaltantes` para mostrar faltantes.
- GET  /student/inscripciones/comprobante  — Ver comprobante. Query params: `id` (receipt id).
- GET  /student/elegir-carrera  — Form para elegir/confirmar plan de estudios.
- POST /student/carrera/confirmar  — Asignar plan al estudiante.
    - Form params: `plan_id`.
- POST /student/inscripciones/confirmar  — Confirmar inscripción definitiva.
    - Form params: `subject_id`, `course_class_id`.
    - Validaciones: pertenece al plan (`belongsToStudentCareer`), verifica correlativas, verifica cupo via `enrollStudent`.

## Comisiones / Docentes

- GET  /teacher/comisiones  — Vista de comisiones y alumnos filtrados para el docente.
    - Query params (filtros): `career_id`, `subject_id`, `course_class_id`.
- POST /teacher/comisiones/crear  — Crear una comisión (si el docente es titular de la materia).
    - Form params: `subject_id`, `name`, `quota` (capacity).

## Asignación de docentes (Admin)

- GET  /admin/teacher-assignments  — Vista de asignaciones docentes.
- GET  /admin/teacher-assignments/allowed-subjects  — JSON de materias permitidas para `teacher_id`.
    - Query params: `teacher_id`.
- POST /admin/teacher-assignments/career  — Asignar docente a carrera.
    - Form params: `teacher_id`, `career_id`.
- POST /admin/teacher-assignments/subject  — Asignar docente a materia.
    - Form params: `teacher_id`, `subject_id`, `academic_year`, `academic_period`, `role_charge`.

## Dashboard

- GET  /dashboard  — Redirige según rol en sesión a `/admin/dashboard`, `/teacher/dashboard` o `/student/dashboard`.
- GET  /admin/dashboard  — Dashboard admin con métricas (userCount, careerCount, subjectCount).
- GET  /teacher/dashboard — Dashboard docente con métricas (materiasCount, comisionesCount).
- GET  /student/dashboard — Dashboard alumno (planName, careerName, enrollmentsCount).

## Errores

- GET  /error  — Página de error genérica. Query params: `type`, `message`.
- GET  /error/403  — Página 403 acceso denegado.

## Notas técnicas y observaciones

- Muchos endpoints esperan params por `req.queryParams` (form/url-encoded). No hay JSON body parsing en los controladores.
- Seguridad: `SecurityController` define filtros `before` para proteger rutas; revisar lista blanca en `before("/*")`.
- Rutas JSON: `GET /admin/teacher-assignments/allowed-subjects` devuelve JSON.
- Endpoint `POST /user/new` actualmente fuerza `role = 1` (crear ADMIN) — revisar si es intencional.
- Se eliminó el endpoint duplicado `POST /student/enroll` durante la limpieza; la confirmación ahora se hace en `/student/inscripciones/confirmar`.

---

Si querés, puedo:
- Añadir ejemplos de request (curl) para cada endpoint.
- Extraer automáticamente los parámetros desde el código y generar una tabla CSV.
- Generar pruebas de integración básicas que ejerciten los endpoints críticos.

¿Qué prefieres que haga a continuación?
