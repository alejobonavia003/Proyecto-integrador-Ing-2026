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


2. (Auditoría) Análisis de riesgos con IA
		a)  Riesgos  del proyecto:  
			- **Incumplimiento de plazos por falta de Roadmap:** Al no tener un plazo estimado definido todavía , el equipo corre el riesgo de no priorizar correctamente las tareas en el backlog de GitHub.
			- **Curva de aprendizaje tecnológica:** La transición de **JDBC** a **Spring/Spring Boot** representa un riesgo alto, ya que el equipo debe aprender el framework mientras desarrolla, lo que suele causar desviaciones en las estimaciones
			- **Dependencia de recursos individuales:** El desarrollo depende de las notebooks personales de cada uno. Si un integrante sufre un desperfecto técnico o pierde su equipo, la capacidad operativa del grupo se reduce un **20%** inmediatamente.
			-  **Inestabilidad del alcance (Scope Creep):** Ya se han registrado cambios de alcance y adición de funcionalidades. Seguir sumando requerimientos sin cerrar los actuales puede llevar al colapso del proyecto.
			Riesgos del producto :
				**Falla en la integridad de las correlatividades:** Un error en la lógica de validación automática permitiría que alumnos se inscriban en materias sin cumplir los requisitos, invalidando el propósito académico del sistema.
				**Vulnerabilidad en la seguridad de datos:** Al gestionar información sensible como notas y datos personales, cualquier brecha de seguridad podría exponer la privacidad de los estudiantes y docentes.
				**Problemas de usabilidad:** Si la interfaz no es lo suficientemente "amigable" , el personal administrativo de la oficina de alumnos podría cometer errores en la carga de datos.
			Riesgos Empresariales o de la organización:
				**Falta de mantenimiento post-entrega:** Al ser un proyecto de cátedra, existe el riesgo de que el software quede "huérfano" una vez finalizado el año 2026, sin soporte para actualizaciones legales o técnicas.
				**Escalabilidad limitada:** Si el sistema no es escalable , podría dejar de funcionar correctamente cuando la universidad aumente su oferta académica o cantidad de alumnos.
		
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
		
				

		