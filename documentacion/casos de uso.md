## Actores del Sistema

- Administrador
- Docente
- Alumno
##  Casos de Uso Principales

### Autenticación

####  01 Iniciar sesión (solo arreglar) 
**Actor:** Usuario  
**Descripción:** Permite al usuario autenticarse en el sistema.  
**Flujo básico:**
1. El usuario ingresa dni y contraseña
2. El sistema valida las credenciales
3. Se crea una sesión si son correctas

---

#### 02 Cerrar sesión
**Actor:** Usuario  
**Descripción:** Finaliza la sesión activa del usuario.

---

#### 03: Registrar usuario (solo arreglar)
**Actor:** Administrador  
**Descripción:** Permite crear nuevos usuarios en el sistema.  
**Datos mínimos:**
- Nombre
- Email
- Contraseña
- dni 
- Rol
---
### 04 crear una carrera
**Actor:** administrador 
**Descripcion** un administrador crea la carrera y carga 
1. codigo 
2. duracion 
3. nombre 
4. le crea un plan de estudio o le asigna uno 

### 05 crear un plan de estudio 
**Actor** administrador 
**descripcion** se crea un plan de estudio, para crear un plan de estudio tenemos que entrar a la informacion de una carrera y de ahi se tiene que crear el plan para que podamos asignar ese plan  a la carrera 
luego tambien hay que agregar una lista de materias al plan de estudio 

#### 06: Crear materia
**Actor:** Administrador  
**Descripción:** se registra una nueva materia y esta materia se puede asignar a cualquier plan de estudio .
se carga de la materia 
1. codigo 
2. nombre 
3. carga horaria 

---

#### 07 Listar materias
**Actor:** Todos  
**Descripción:** Visualización de materias disponibles.

---

#### 08 Editar materia
**Actor:** Administrador  
**Descripción:** Modificación de datos de una materia.

---

#### UC-10: Eliminar materia
**Actor:** Administrador  
**Descripción:** Eliminación de una materia.

---

### 3.4 Gestión de Comisiones

#### UC-11: Crear comisión
**Actor:** docente(tiene que ser el asignado a esa materia)  
**Descripción:** Permite crear una comisión asociada a una materia.
esto se tiene que poder crear despues de entrar a la informacion de una materia 

---

#### UC-12: Asignar docente a comisión
**Actor:** Administrador  
**Descripción:** Relaciona un docente con una comisión.

---

#### UC-13: Ver comisiones
**Actor:** Todos  
**Descripción:** Permite visualizar las comisiones disponibles.

---

### 3.5 Inscripción a Cursadas

#### UC-14: Inscribirse a materia
**Actor:** Alumno  
**Descripción:** Permite al alumno inscribirse en una materia.  
**Flujo básico:**
1. El alumno visualiza materias/comisiones
2. Selecciona una materia
3. Se registra la inscripción

---

#### UC-15: Ver inscripciones
**Actor:** Alumno  
**Descripción:** Permite consultar las materias en las que está inscripto.

---

### 3.6 Calificaciones

#### UC-16: Cargar nota
**Actor:** Docente  
**Descripción:** Permite registrar calificaciones de alumnos.

---

#### UC-17: Ver notas
**Actor:** Alumno  
**Descripción:** Permite visualizar sus calificaciones.

---

#### UC-18: Ver alumnos de comisión
**Actor:** Docente  
**Descripción:** Lista los alumnos inscriptos en una comisión.

---

### 3.7 Tareas (Versión Simplificada)

#### UC-19: Crear tarea
**Actor:** Docente  
**Descripción:** Permite publicar una tarea.

---

#### UC-20: Ver tareas
**Actor:** Alumno  
**Descripción:** Permite visualizar tareas asignadas.

---

#### UC-21: Entregar tarea
**Actor:** Alumno  
**Descripción:** Permite enviar una entrega (archivo o referencia).

---

#### UC-22: Calificar tarea
**Actor:** Docente  
**Descripción:** Permite asignar una nota a una entrega.

---

### 3.8 Notificaciones (Opcional en MVP)

#### UC-23: Ver notificaciones
**Actor:** Usuario  
**Descripción:** Permite visualizar mensajes del sistema.

---

## 4. Roadmap de Implementación

### Fase 1 – Base del sistema
- UC-01 Iniciar sesión
- UC-02 Cerrar sesión
- UC-03 Registrar usuario
- UC-08 Listar materias

---

### Fase 2 – Funcionalidad operativa básica
- UC-07 Crear materia
- UC-11 Crear comisión
- UC-14 Inscribirse a materia
- UC-15 Ver inscripciones

---

### Fase 3 – Gestión académica
- UC-16 Cargar nota
- UC-17 Ver notas
- UC-18 Ver alumnos de comisión

---

### Fase 4 – Gestión de tareas
- UC-19 Crear tarea
- UC-21 Entregar tarea
- UC-22 Calificar tarea

---

### Fase 5 – Funcionalidades avanzadas (fuera del MVP)
- Correlatividades
- Planes de estudio
- Reportes
- Métricas y analítica

---

## 5. Consideraciones Técnicas

- Se recomienda implementar roles básicos sin sistema de permisos complejo en esta etapa.
- Evitar la implementación de correlatividades en la primera versión debido a su complejidad.
- Mantener modelos simples y escalables.
- Priorizar funcionalidades que permitan flujo completo de usuario:
  login → inscripción → evaluación.

---

## 6. Ejemplo de Historia de Usuario

Historia: Como alumno quiero inscribirme a una materia para poder cursarla.

Criterios de aceptación:
- Debo poder visualizar las materias disponibles
- Debo poder elegir una comisión
- La inscripción debe guardarse en la base de datos
- No debo poder inscribirme dos veces a la misma comisión

---