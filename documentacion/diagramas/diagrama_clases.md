#### Diagrama de clases 
```mermaid
classDiagram
    %% ======================
    %% ENUMERADOS (Indispensables para la lógica)
    %% ======================
    class Modalidad {
        <<enumeration>>
        PRESENCIAL
        VIRTUAL
        HIBRIDA
    }
    class EstadoInscripcion {
        <<enumeration>>
        PENDIENTE
        ACEPTADA
        RECHAZADA
        CANCELADA
    }
    class TipoCalificacion {
        <<enumeration>>
        PARCIAL
        FINAL
        TRABAJO_PRACTICO
    }
    class CondicionAlumno {
        <<enumeration>>
        REGULAR
        LIBRE
        PROMOCIONADO
    }
    class EstadoEntrega {
        <<enumeration>>
        PENDIENTE
        ENTREGADO
        CALIFICADO
        FUERA_DE_TERMINO
    }
    class TipoReporte {
        <<enumeration>>
        RENDIMIENTO_ACADEMICO
        ASISTENCIA_GENERAL
        OCUPACION_COMISIONES
    }

    %% ======================
    %% USUARIOS Y ROLES
    %% ======================
    class Usuario {
        +int id
        +String nombre
        +String dni
        +String email
        +String password
        +login(String email, String password) boolean
        +logout () void
    }

    class Rol {
        +int id
        +String nombre
        +List~String~ permisos
    }

    class Administrador {
        +crearUsuario(Usuario u) void
        +generarReporte(TipoReporte tipo) Reporte
    }

    class Docente {
        +crearTarea(Tarea t) void
        +cargarNota(Calificacion c) void
    }

    class Alumno {
        +String legajo
        +verHistorial() List~Calificacion~
    }

    Usuario "1" --> "1" Rol
    Administrador --|> Usuario
    Docente --|> Usuario
    Alumno --|> Usuario

    %% ======================
    %% MATERIA Y PLAN
    %% ======================
    class Materia {
        +int id
        +String nombre
        +String codigo
        +Modalidad modalidad
    }

    class Correlatividad {
        +int id
        +boolean requiereAprobada
    }

    class PlanEstudio {
        +int id
        +String nombre
        +String version
    }


    Materia "1" *-- "0..*" Correlatividad : tiene
    Correlatividad "1" --> "1" Materia : materiaRequisito
    PlanEstudio "1" *-- "1..*" Materia
    PlanEstudio "0..*" --> "1" Materia
    Alumno "0..*" --> "1" PlanEstudio

    %% ======================
    %% ADMINISTRACIÓN ACADÉMICA
    %% ======================
    class Comision {
        +int id
        +String nombre
        +int capacidad
    }

    class InscripcionCursada {
        +Date fecha
        +EstadoInscripcion estado
    }

    class ExamenFinal {
        +Date fecha
        +cerrarInscripcion() void
    }

    class InscripcionExamen {
        +Date fecha
        +EstadoInscripcion estado
    }

    class Calificacion {
        +float nota
        +TipoCalificacion tipo
        +CondicionAlumno condicion
    }

    class Asistencia {
        +Date fecha
        +boolean presente
    }
    Alumno "1" --> "0..*" Asistencia
    Asistencia "0..*" --> "1" Materia
    Comision "0..*" --> "1" Materia
    Docente "1..*" --> "1..*" Comision
    Alumno "1" --> "0..*" InscripcionCursada
    InscripcionCursada "0..*" --> "1" Comision
    Alumno "1" --> "0..*" InscripcionExamen
    InscripcionExamen "0..*" --> "1" ExamenFinal
    ExamenFinal "0..*" --> "1" Materia
    InscripcionCursada "1" *-- "0..*" Calificacion
  

    %% ======================
    %% TAREAS Y ENTREGAS
    %% ======================
    class Tarea {
        +int id
        +String descripcion
        +Date fechaEntrega
    }

    class Entrega {
        +String archivo
        +Date fecha
        +float nota
        +EstadoEntrega estado
    }

    Docente "1" --> "0..*" Tarea
    Tarea "0..*" --> "1" Materia
    Alumno "1" --> "0..*" Entrega
    Entrega "0..*" --> "1" Tarea

    %% ======================
    %% OTROS
    %% ======================
    class Notificacion {
        +String mensaje
        +Date fecha
    }

    class Reporte {
        +TipoReporte tipo
        +generar() void
    }

    Usuario "1" --> "0..*" Notificacion
    Administrador "1" --> "0..*" Reporte

  

%% ======================

%% USUARIOS Y ROLES

%% ======================

class Usuario {

  +id: int

  +nombre: String

  +dni: String

  +email: String

  +password: String

  +login()

  +logout()

}

  

class Administrador

class Docente

class Alumno {

  +legajo: String

}

  

Administrador --|> Usuario

Docente --|> Usuario

Alumno --|> Usuario

  

%% ======================

%% MATERIAS Y CORRELATIVAS

%% ======================

class Materia {

  +id: int

  +nombre: String

  +codigo: String

  +cargaHoraria: int

  +modalidad: String

}

  

Materia --> "0..*" Materia : correlativas

  
  
  

%% ======================

%% PLAN DE ESTUDIO

%% ======================

class PlanEstudio {

  +id: int

  +nombre: String

  +version: String

}

  

class Carrera {

  +codigoCarrera: int

  +duracion: time

  +nombre: String

}

  
  

Alumno -->"1..*" Carrera : cursa

  
  

Carrera "1" *-- "1" PlanEstudio : vigente

Carrera "1" *-- "1..*" PlanEstudio

PlanEstudio "1" --> "1..*" Materia : tiene

  
  

class Equivalencia {

  +id: int

}

  

Equivalencia --> Materia : origen

Equivalencia --> Materia : destino

  

%% ======================

%% ADMINISTRACIÓN ACADÉMICA

%% ======================

class Comision {

  +id: int

  +nombre: String

}

  

Materia --> Comision

Docente --> "1..*" Comision

Docente  "1" --> "1..*" Materia : responsable

Docente  "1..*" --> "1..*" Materia : docenteComun

  

class Periodo {

    +anio : int

    +periodo : Tperiodo

    +cargo : cargo

    +participacion : Tparticipacion

}

  

Docente --> Periodo

Materia --> Periodo

  

class InscripcionCursada {

  +fecha: Date

}

  

Alumno --> "0..*" InscripcionCursada

InscripcionCursada --> Comision

  

class ExamenFinal {

  +fecha: Date

  +nota: float

}

  

class InscripcionExamen {

  +fecha: Date

}

  

Alumno --> "0..*" InscripcionExamen

InscripcionExamen --> ExamenFinal

ExamenFinal --> Materia

  

class Calificacion {

  +nota: float

  +tipo: String

  +condicion: String

}

  

Alumno --> "0..*" Calificacion

Calificacion --> Materia

  
  

%% ======================

%% TAREAS

%% ======================

class Tarea {

  +id: int

  +descripcion: String

  +fechaEntrega: Date

}

  

class Entrega {

  +archivo: String

  +fecha: Date

  +nota: float

  +comentario: String

}

  

Docente --> "0..*" Tarea

Tarea --> Materia

Alumno --> "0..*" Entrega

Entrega --> Tarea

  

%% ======================

%% NOTIFICACIONES

%% ======================

class Notificacion {

  +mensaje: String

  +fecha: Date

}

  

Usuario --> "0..*" Notificacion

Notificacion --> "1..*" Usuario

  

%% ======================

%% REPORTES

%% ======================

class Reporte {

  +tipo: String

  +generar()

}

  

Administrador --> "0..*" Reporte

Administrador --> Carrera
```

#### Diagrama Entidad relacion 
db
```mermaid
erDiagram

    roles {
        INTEGER id PK
        VARCHAR name UK
    }

    users {
        INTEGER id PK
        VARCHAR dni UK
        VARCHAR name
        VARCHAR email UK
        VARCHAR password
        INTEGER role_id FK
    }

    careers {
        INTEGER id PK
        VARCHAR code UK
        VARCHAR name
        INTEGER duration
    }

    study_plans {
        INTEGER id PK
        VARCHAR code UK
        VARCHAR name
        VARCHAR version
        INTEGER career_id FK
    }

    subjects {
        INTEGER id PK
        VARCHAR code UK
        VARCHAR name
        INTEGER weekly_hours
        VARCHAR modality
        INTEGER study_plan_id FK
    }

    correlativities {
        INTEGER id PK
        INTEGER subject_id FK
        INTEGER required_subject_id FK
        BOOLEAN requires_approved
    }

    course_classes {
        INTEGER id PK
        VARCHAR name
        INTEGER capacity
        INTEGER subject_id FK
        INTEGER teacher_id FK
    }

    enrollments {
        INTEGER id PK
        INTEGER student_id FK
        INTEGER course_class_id FK
        TIMESTAMP enrolled_at
    }

    grades {
        INTEGER id PK
        INTEGER enrollment_id FK
        VARCHAR exam_name
        DECIMAL score
        VARCHAR status
    }

    assignments {
        INTEGER id PK
        INTEGER course_class_id FK
        VARCHAR title
        TEXT description
        TIMESTAMP due_date
    }

    submissions {
        INTEGER id PK
        INTEGER assignment_id FK
        INTEGER student_id FK
        TEXT content_reference
        DECIMAL grade
        TIMESTAMP submitted_at
    }

    %% =====================================
    %% RELACIONES
    %% =====================================

    roles ||--o{ users : "has"

    careers ||--o{ study_plans : "contains"

    study_plans ||--o{ subjects : "includes"

    subjects ||--o{ correlativities : "has"
    subjects ||--o{ correlativities : "required_by"

    subjects ||--o{ course_classes : "opens"

    users ||--o{ course_classes : "teaches"

    users ||--o{ enrollments : "enrolls"

    course_classes ||--o{ enrollments : "contains"

    enrollments ||--o{ grades : "receives"

    course_classes ||--o{ assignments : "publishes"

    assignments ||--o{ submissions : "receives"

    users ||--o{ submissions : "submits"
```



#### Diagrama de casos de uso

``` mermaid
flowchart LR

  

%% ======================

%% ACTORES

%% ======================

Admin[Administrador]

Docente[Docente]

Alumno[Alumno]

Usuario[Usuario]

  

Usuario --> Docente

Usuario --> Admin

Usuario --> Alumno

  
  

%% ======================

%% SISTEMA

%% ======================

subgraph Sistema de Gestión Estudiantil

  

%% --- USUARIOS ---

UC1[Gestionar usuarios]

UC2[Autenticarse]

UC23[alta, baja y modificación de datos personales]

  

%% --- MATERIAS ---

UC3[Gestionar materias]

UC4[consultar correlatividades]

UC5[Visualizar correlativas]

  

%% --- PLANES ---

UC6[Gestionar planes de estudio]

UC7[Asignar alumno a plan]

UC8[Definir equivalencias]

  

%% --- ACADÉMICO ---

UC9[Inscribirse a materia]

UC10[Inscribirse a examen]

UC11[Registrar calificaciones]

UC13[Consultar historial académico]

  

%% --- DESEMPEÑO ---

UC14[Ver progreso académico]

UC15[Recibir notificaciones]

  

%% --- TAREAS ---

UC16[Publicar tareas]

UC17[Entregar tareas]

UC18[Calificar entregas]

UC19[Ver calendario]

  

%% --- REPORTES ---

UC20[Generar reportes]

  

end

  

%% ======================

%% RELACIONES ACTORES

%% ======================

  

Usuario --> UC23

  

Admin --> UC1

Admin --> UC3

Admin --> UC6

Admin --> UC8

Admin --> UC20

  
  

Docente --> UC11

Docente --> UC16

Docente --> UC18

Docente --> UC5

  

Alumno --> UC9

Alumno --> UC10

Alumno --> UC13

Alumno --> UC14

Alumno --> UC15

Alumno --> UC17

Alumno --> UC19

Alumno --> UC5

  

%% ======================

%% RELACIONES INCLUDE

%% ======================

  

UC9 --> UC4

UC9 --> UC7

  

UC10 --> UC4

  

UC17 --> UC16

  

UC18 --> UC17

  

UC14 --> UC13
```

### Diagrama de componentes
``` mermaid
flowchart TD

    subgraph Cliente
        Browser["Cliente (Browser)"]
    end

    subgraph Servidor
        subgraph Presentacion
            Controllers["Controllers (Spark Routes)"]
            Views["Views (Mustache)"]
        end

        subgraph Logica
            Services["Services"]
        end

        subgraph Modelo
            Models["Models (Usuario, Materia, etc.)"]
        end

        subgraph Datos
            DBConn["DB Connection"]
        end
    end

    DB[(sqlite)]

    Browser --> Controllers
    Controllers --> Services
    Controllers --> Views

    Services --> DBConn
    Services --> Models

    DBConn --> DB
```
#### Estructura de las carpetas
``` folders
src/main/java/com/gestionestudiantil/

├── Main.java

├── config/
│   ├── Routes.java
│   ├── AppConfig.java

├── controller/
│   ├── AuthController.java
│   ├── UsuarioController.java
│   ├── MateriaController.java
│   ├── InscripcionController.java
│   ├── TareaController.java

├── service/
│   ├── AuthService.java
│   ├── UsuarioService.java
│   ├── MateriaService.java
│   ├── InscripcionService.java
│   ├── TareaService.java

├── dao/
│   ├── UsuarioDAO.java
│   ├── MateriaDAO.java
│   ├── InscripcionDAO.java
│   ├── TareaDAO.java
│   ├── impl/
│       ├── UsuarioDAOImpl.java
│       ├── MateriaDAOImpl.java

├── model/          (ENTIDADES)
│   ├── Usuario.java
│   ├── Rol.java
│   ├── Alumno.java
│   ├── Materia.java
│   ├── PlanEstudio.java
│   ├── Calificacion.java
│   ├── Tarea.java

├── utils/
│   ├── DBConnection.java
│   ├── PasswordHasher.java

├── exception/
│   ├── BusinessException.java

└── resources/
    ├── templates/  (Mustache)
    │   ├── login.mustache
    │   ├── dashboard.mustache
    │   ├── materias.mustache
    │
    └── public/     (CSS, JS)
```

