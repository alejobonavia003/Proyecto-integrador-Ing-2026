1. (Requirements) Describir su proyecto:
	- Problema que se quiere resolver : 
		- Se quiere sistematizar la administración académica de una institución mediante un software. El sistema permitirá gestionar de manera segura un sistema de roles donde se podrá tanto organizar el personal docente, como también la gestión de los alumnos y además permitirá a los administradores visualizar y modificar toda la información. (==CONSULTAR==)
	- Usuarios del sistema:
		- Los administradores , alumnos y profesores.
	- Funcionalidades principales: 
		- registro de los usuarios del sistema y la gestión de cada rol
		- gestión de materias y sus correlatividades 
		- gestión de planes de estudio
		- administración académica 
		- seguimiento  de desempeño 
		- gestión de tareas y planificación 
		- reportes y análisis 
	- Restricciones técnicas :
		- Plataformas y entornos de ejecución: El sistema debe ser compatible con cualquier navegadores web.
		- Infraestructura y Hardware:
		- El sistema debe poder ejecutarse en un hardware : el servidor deberá correr 24 7 en un servidor 
		- Lenguaje de programación y frameworks:
		- Vamos a utilizar Java para nuestro lenguaje, Mustache como motor de plantillas, maven como herramienta de gestión y automatización de compilación. También el uso de mySql para la base de datos y como orm usamos jdbc pero estamos  analisando usar spring y springBot 
	- tamaño del equipo: 5 personas
	- Tecnologías elegidas: la notebook personal de cada uno
	- plazo estimado : no definido todavía 
	- cambios de alcance ocurridos: adición de nuevas funcionalidades, cambios en el cronograma, reducción de recursos
		- problema encontrados : se irán registrando durante el desarrollo 
	- Forma de organización del equipo: metodología Kanban, git, gestión mediante issues  


2.# 📊 Matriz de Riesgos del Proyecto

| Tipo de Riesgo | Descripción                                                                                   | Probabilidad | Impacto | Identificado por |
| -------------- | --------------------------------------------------------------------------------------------- | ------------ | ------- | ---------------- |
| Planificación  | Incumplimiento de plazos por falta de roadmap definido, afectando la priorización del backlog | Alta         | Alto    | -                |
| Técnico        | Curva de aprendizaje en Spring/Spring Boot durante el desarrollo                              | Alta         | Alto    | -                |
| Humano         | Dependencia de notebooks personales; pérdida de un equipo reduce la capacidad operativa       | Media        | Alto    | -                |
| Planificación  | Inestabilidad del alcance (scope creep) por incorporación continua de funcionalidades         | Alta         | Crítico | -                |
| Técnico        | Fallas en la lógica de correlatividades que invaliden inscripciones                           | Media        | Crítico | -                |
| Técnico        | Vulnerabilidades en la seguridad de datos sensibles (notas, datos personales)                 | Media        | Crítico | -                |
| Organizacional | Problemas de usabilidad que generen errores operativos en usuarios administrativos            | Media        | Medio   | -                |
| Organizacional | Falta de mantenimiento post-entrega del sistema                                               | Alta         | Alto    | -                |
| Técnico        | Escalabilidad limitada ante crecimiento de usuarios o carreras                                | Media        | Alto    | -                |
| Humano         | Falta de disponibilidad de un integrante clave del equipo                                     | Baja         | Crítico | -                |
| Organizacional | Desalineación en prioridades de tareas dentro del equipo                                      | Media        | Medio   | -                |
| Planificación  | Desviación en estimación de historias complejas                                               | Alta         | Alto    | -                |
	b) Riesgos del producto:
		- interfaz poco intuitiva
		- caídas del sistema
		- errores en el calculo o carga de notas
		Riesgos del organizacionales:
		- falta de comunicación en el equipo
		- mala asignación de tareas
		- falta de coordinación
		- falta de seguimiento del proyecto
		Riesgos del proyecto
		- Requerimientos pocos claros
		- incumplimiento con el plazo
		- cambios de los requerimientos
		
				

2)  
	   # 📊 Matriz de Riesgos (Clasificación General)

| Tipo de Riesgo | Descripción                                                       | Probabilidad | Impacto | Identificado por |
| -------------- | ----------------------------------------------------------------- | ------------ | ------- | ---------------- |
| Técnico        | Interfaz poco intuitiva que dificulte el uso del sistema          | Media        | Medio   | -                |
| Técnico        | Caídas del sistema por problemas de estabilidad o infraestructura | Media        | Alto    | -                |
| Técnico        | Errores en el cálculo o carga de notas                            | Media        | Crítico | -                |
| Organizacional | Falta de comunicación dentro del equipo                           | Alta         | Alto    | -                |
| Organizacional | Mala asignación de tareas                                         | Media        | Alto    | -                |
| Organizacional | Falta de coordinación entre integrantes                           | Media        | Medio   | -                |
| Organizacional | Falta de seguimiento del proyecto                                 | Alta         | Alto    | -                |
| Planificación  | Requerimientos poco claros o ambiguos                             | Alta         | Crítico | -                |
| Planificación  | Incumplimiento de plazos establecidos                             | Alta         | Alto    | -                |
| Planificación  | Cambios constantes en los requerimientos                          | Alta         | Crítico | -                |