# Listado de funcionalidades del proyecto

Fecha: 2026-05-29

Este documento lista las funcionalidades implementadas (o parcialmente implementadas) encontradas en el código.

1) Autenticación y gestión de sesión
	- Login (inicio de sesión), logout.
	- Registro de usuarios (endpoints para creación por admin y pública que actualmente fuerza rol ADMIN en `AuthController`).
	- Hash de contraseñas con BCrypt (`AuthService`).

2) Administración de usuarios
	- Listado de usuarios por rol, filtrado y conteo (`UserController`, `UserService`).
	- Creación de usuarios por Administrador (`/admin/users/new`).

3) Gestión de carreras y planes de estudio
	- Listado de carreras, creación de carrera (`CareerController`, `CareerService`).
	- Detalle de carrera con sus planes (`getCareerDetailView`).
	- Crear planes de estudio asociados a una carrera (`CareerService.createStudyPlan`, `StudyPlanController`).

4) Gestión de materias
	- Crear materia, listar materias, ver materias por plan (`SubjectController`, `SubjectService`).
	- Asignar materias a planes de estudio (`SubjectService.assignSubjectsToPlan`).
	- Vistas para mostrar materias en plantillas Mustache.

5) Correlatividades (requisitos entre materias)
	- Agregar correlatividades (`CorrelativityService.addCorrelativity`).
	- Verificar correlativas recursivamente para un alumno (`verificarCorrelativas`) con lógica que soporta requerir aprobado o solo cursada.

6) Inscripciones (módulo alumno)
	- Ver inscripciones del estudiante (`/student/mis-inscripciones`).
	- Formulario de nueva inscripción y listado de materias disponibles filtradas por plan (`getAvailableSubjectsForStudent`).
	- Confirmar inscripción (`/student/inscripciones/confirmar`) con validaciones: pertenece al plan, correlativas, cupo.
	- Generar comprobante de inscripción (`getEnrollmentReceipt`).
	- Asignar plan de estudios al estudiante (`/student/carrera/confirmar`).

7) Gestión de comisiones (módulo docente)
	- Docente: ver comisiones y crear comisiones (`EnrollmentTeacherController`, `EnrollmentService.createCommission`).
	- Filtrar estudiantes por carrera/materia/comisión (`EnrollmentService.getFilteredStudents`).

8) Asignaciones docentes (admin)
	- Asignar docente a carrera (`TeacherAssignmentService.assignTeacherToCareer`).
	- Asignar docente a materia con rol (Titular/auxiliar) y periodo/curso (`assignTeacherToSubject`).
	- Endpoints auxiliares JSON para materias permitidas por docente (`getAllowedSubjectsJson`).

9) Dashboard y métricas
	- Dashboard general que redirige por rol y dashboards específicos para Admin, Docente y Alumno (`DashboardController`, `DashboardService`).
	- Métricas simples: conteo de usuarios, carreras, materias, materias por docente, comisiones, inscripciones.

10) Seguridad / filtros middleware
	 - `SecurityController` con filtros `before` para proteger rutas `/admin/*`, `/teacher/*`, `/student/*` y un filtro general `/*` con lista blanca de rutas públicas.

11) Manejo de errores y vistas de error
	 - Rutas `/error` y `/error/403` con plantillas específicas.

12) Plantillas y vistas (UI)
	 - Mustache templates bajo `src/main/resources/templates` para login, dashboards, formularios y listados.

13) Persistencia y modelos
	 - Modelos ActiveJDBC: `User`, `Role`, `Career`, `StudyPlan`, `Subject`, `Correlativity`, `CourseClass`, `Enrollment`, `TeacherCareer`, `TeacherSubject`, etc.
	 - Esquema SQL en `src/main/resources/schema.sql`.

14) Empaquetado y ejecución
	 - Maven build y empaquetado en JAR (`mvn clean package` → `target/proye-is-1.0-SNAPSHOT.jar`).
	 - Clase principal y arranque embebido (Spark Java server) via `app.App`.

15) Tests
	 - Test(s) de ejemplo en `test/java/com/is1/proyecto/AppTest.java`.

16) Endpoints auxiliares y utilidades
	 - Varios helpers en servicios para obtener vistas/DTOs (por ejemplo `getTeacherCommissionViewData`, `getPlanCoursesView`).

Observaciones generales
- Hay código legacy y utilitarios no referenciados — conviene archivar o eliminar (`EnrollmentControllerVIEJO` fue removido en limpieza reciente).
- Flujos críticos (inscripción) fueron unificados/asegurados transaccionalmente; conviene añadir pruebas y mejorar bloqueo DB si la concurrencia es alta.
- Recomendado: añadir documentación de API, seeds para BD y tests unitarios para reglas críticas.

Si querés, puedo generar también un archivo `documentacion/api_endpoints.md` con la lista completa de rutas HTTP expuestas y sus parámetros. ¿Deseás que lo haga? 

