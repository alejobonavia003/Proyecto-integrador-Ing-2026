# Sistema de Gestión Estudiantil

---

## 1. Descripción del Proyecto

### Problema que se quiere resolver
Se quiere sistematizar la administración académica de una institución mediante un software. El sistema permitirá gestionar de manera segura un sistema de roles donde se podrá tanto organizar el personal docente, como también la gestión de los alumnos y además permitirá a los administradores visualizar y modificar toda la información.

### Usuarios del sistema
- Administradores
- Alumnos
- Profesores

---

## 2. Funcionalidades Principales

### 2.1. Registro y gestión de usuarios y roles
Permite administrar todas las personas que interactúan con el sistema, asignando permisos según su función.

- **Registro de usuarios**: alta, baja y modificación de datos personales (nombre, DNI, correo, contraseña).
- **Roles del sistema**:
  - *Administrador*: acceso total al sistema.
  - *Docente*: carga de notas, asistencia y tareas.
  - *Alumno*: consulta de su información académica y tareas.
- **Gestión de permisos**: cada rol solo puede ver y operar las secciones habilitadas para él.
- **Autenticación**: inicio de sesión seguro con manejo de sesiones.

### 2.2. Gestión de materias y correlatividades
Permite definir las materias de la carrera y las dependencias entre ellas.

- **Alta/baja/modificación de materias**: nombre, código, carga horaria, modalidad (presencial/virtual).
- **Correlatividades**: definición de qué materias deben estar aprobadas/cursadas para poder inscribirse en otra.
- **Visualización del árbol de correlativas**: representación gráfica o listado de dependencias entre materias.
- **Validación automática**: el sistema verifica si un alumno cumple las condiciones para inscribirse.

### 2.3. Gestión de planes de estudio
Permite organizar las materias en un plan formal estructurado por año y cuatrimestre.

- **Creación y edición de planes**: asociar materias a años y cuatrimestres.
- **Versiones del plan**: soporte para múltiples versiones (ej. Plan 2018, Plan 2023) y la coexistencia de alumnos en distintos planes.
- **Asignación de alumnos a un plan**: cada alumno queda vinculado al plan vigente al momento de su inscripción.
- **Equivalencias entre planes**: posibilidad de reconocer materias de un plan anterior como equivalentes en el nuevo.

### 2.4. Administración académica
Cubre los procesos formales del ciclo académico de cada alumno.

- **Inscripción a cursadas**: registro de alumnos en comisiones de materias, validando correlatividades.
- **Inscripción a exámenes finales**: habilitación de turnos de examen y registro de inscriptos.
- **Registro de calificaciones**: carga de notas de parciales, finales y condición (regular, libre, aprobado).
- **Historial académico del alumno**: acceso al legajo con todas las materias cursadas, notas y estado.

### 2.5. Seguimiento de desempeño
Permite monitorear el progreso académico de los alumnos a lo largo del tiempo.

- **Estado de avance en la carrera**: porcentaje de materias aprobadas sobre el total del plan.
- **Alertas automáticas**: notificaciones cuando un alumno tiene baja asistencia, notas insuficientes o materias en riesgo.
- **Comparación con promedios**: el alumno puede ver su desempeño respecto al promedio del curso.
- **Historial de notas por materia**: evolución de calificaciones a lo largo del cursado.

### 2.6. Gestión de tareas y planificación
Facilita la organización del trabajo académico tanto para docentes como para alumnos.

- **Publicación de tareas**: el docente crea tareas con descripción, fecha de entrega y archivos adjuntos.
- **Entrega de tareas**: el alumno sube su resolución desde el sistema.
- **Calificación de entregas**: el docente puntúa y deja comentarios sobre cada entrega.
- **Calendario académico**: visualización de fechas de parciales, finales, entregas y eventos importantes.
- **Notificaciones**: avisos automáticos por vencimientos próximos o nuevas publicaciones.

### 2.7. Reportes y análisis
Genera información consolidada para la toma de decisiones académicas e institucionales.

- **Reportes de rendimiento**: promedio general por materia, comisión o carrera en un período.
- **Reportes de asistencia**: listados por materia con alumnos que no alcanzan el mínimo requerido.
- **Estadísticas de avance**: cantidad de alumnos por año de carrera, tasa de aprobación por materia.
- **Exportación de datos**: descarga de reportes en formato PDF o Excel.
- **Panel de control (dashboard)**: resumen visual con indicadores clave para administradores y coordinadores.

---

## 3. Restricciones Técnicas

- **Plataformas y entornos de ejecución**: el sistema debe ser compatible con cualquier navegador web.
- **Infraestructura y hardware**: el servidor deberá estar en funcionamiento 24/7.
- **Lenguaje de programación y frameworks**: Java como lenguaje principal, Mustache como motor de plantillas, Maven como herramienta de gestión y automatización de compilación, MySQL como base de datos y JDBC como ORM.
- **Dispositivos de desarrollo**: notebook personal de cada integrante del equipo.

---

## 4. Organización del Proyecto

- **Tamaño del equipo**: 5 personas
- **Plazo estimado**: no definido todavía
- **Metodología de trabajo**: Kanban, Git, gestión mediante issues
- **Cambios de alcance ocurridos**: adición de nuevas funcionalidades, cambios en el cronograma, reducción de recursos
- **Problemas encontrados**: se irán registrando durante el desarrollo

---
### Lenguajes y Frameworks
*Spark*
- Lenguaje: Java  
- Motor de plantillas: Mustache  
- Gestión y automatización: Maven  
- Base de datos: postgresql  
- ORM: JDBC  
- frontend: mustache
---

