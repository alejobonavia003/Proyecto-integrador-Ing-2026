# Proyecto Integrador — Ingeniería 2026

Resumen
-------
Aplicación web Java para la gestión académica (carreras, planes, materias, inscripciones, usuarios y asignaciones de docentes). Usa una arquitectura MVC ligera con plantillas Mustache y acceso a datos mediante ActiveJDBC/ORM; el artefacto se empaqueta como un JAR ejecutable.

Características principales
- Gestión de carreras, planes de estudio y materias.
- Módulos de inscripción y dashboard para roles (admin, docente, estudiante).
- Autenticación y control de roles.
- Plantillas Mustache para vistas y rutas REST/HTML.

Requisitos
- Java 17 (JDK)
- Maven 3.x
- (Opcional) Navegador web para acceder a la UI en `http://localhost:8080`

Estructura relevante
- Código fuente: `src/main/java`
- Vistas/plantillas: `src/main/resources/templates`
- Esquema SQL: `src/main/resources/schema.sql`
- Empaquetado: `target/` (jar generado)

Instalación y ejecución (rápido)
1. Clonar el repositorio:

	git clone <repo-url>
	cd Proyecto-integrador-Ing-2026

2. Preparar base de datos local:

	- Crear carpeta `db/` en la raíz del proyecto si no existe.

3. Compilar y empaquetar con Maven:

	mvn clean package

	Esto genera `target/proye-is-1.0-SNAPSHOT.jar`.

4. Ejecutar la aplicación:

	java -jar target/proye-is-1.0-SNAPSHOT.jar

	Por omisión la app se sirve en `http://localhost:8080`.
