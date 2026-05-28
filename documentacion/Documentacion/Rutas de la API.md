# API Routes

| Método | Ruta                                                     | Descripción                                                 | Acceso              |
| ------ | -------------------------------------------------------- | ----------------------------------------------------------- | ------------------- |
| GET    | /                                                        | Pantalla principal de login                                 | Público             |
| GET    | /user/create                                             | Formulario de creación de usuario administrador             | Público             |
| GET    | /logout                                                  | Cerrar sesión actual                                        | Usuario autenticado |
| POST   | /user/new                                                | Crear nuevo usuario administrador                           | Público             |
| POST   | /login                                                   | Iniciar sesión                                              | Público             |
| GET    | /admin/careers                                           | Listado de carreras                                         | ADMIN               |
| GET    | /admin/careers/new                                       | Formulario de creación de carrera                           | ADMIN               |
| POST   | /admin/careers/new                                       | Crear nueva carrera                                         | ADMIN               |
| GET    | /admin/careers/:id                                       | Ver detalle de carrera y planes de estudio                  | ADMIN               |
| GET    | /dashboard                                               | Redirección automática según rol del usuario                | Usuario autenticado |
| GET    | /admin/dashboard                                         | Panel principal de administrador                            | ADMIN               |
| GET    | /teacher/dashboard                                       | Panel principal de docentes                                 | TEACHER             |
| GET    | /student/dashboard                                       | Panel principal de alumnos                                  | STUDENT             |
| GET    | /error                                                   | Página genérica de errores del sistema                      | Público             |
| GET    | /error/403                                               | Página de acceso denegado                                   | Público             |
| GET    | /admin/careers/:id/plans/new                             | Formulario de creación de plan de estudio                   | ADMIN               |
| POST   | /admin/careers/:id/plans/new                             | Crear nuevo plan de estudio                                 | ADMIN               |
| GET    | /admin/careers/:career_id/plans/:plan_id/subjects        | Gestión de materias de un plan de estudio                   | ADMIN               |
| POST   | /admin/careers/:career_id/plans/:plan_id/subjects/assign | Asignar materias existentes a un plan                       | ADMIN               |
| POST   | /admin/careers/:career_id/plans/:plan_id/subjects/new    | Crear nueva materia dentro de un plan                       | ADMIN               |
| GET    | /admin/careers/:career_id/plans/:plan_id                 | Ver detalle de un plan de estudio                           | ADMIN               |
| GET    | /admin/subjects                                          | Listado de materias                                         | ADMIN               |
| GET    | /admin/subjects/new                                      | Formulario de creación de materia                           | ADMIN               |
| POST   | /admin/subjects/new                                      | Crear nueva materia                                         | ADMIN               |
| POST   | /admin/subjects/:id/correlativities                      | Agregar correlatividad a una materia                        | ADMIN               |
| POST   | /student/enroll                                          | Inscripción de alumno a comisión/materia                    | STUDENT             |
| GET    | /admin/teacher-assignments                               | Panel de asignación docente                                 | ADMIN               |
| GET    | /admin/teacher-assignments/allowed-subjects              | Obtener materias permitidas para un docente en formato JSON | ADMIN               |
| POST   | /admin/teacher-assignments/career                        | Asignar docente a carrera                                   | ADMIN               |
| POST   | /admin/teacher-assignments/subject                       | Asignar docente a materia                                   | ADMIN               |
| GET    | /admin/users                                             | Listado y filtrado de usuarios                              | ADMIN               |
| GET    | /admin/users/new                                         | Formulario de creación de usuarios                          | ADMIN               |
| POST   | /admin/users/new                                         | Crear nuevo usuario desde panel administrador               | ADMIN               |