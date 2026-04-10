<<<<<<< HEAD
=======

```mermaid
classDiagram
    class Persona {
        - nombre : String
        - apellido : String
        - dni : int
        - email : String
        - telefono : String
        - direccion : String
    }

    class Profesor {
        - id_doc : String
    }

    class Estudiante {
        - estado : Estado
    }

    class Materia {
        - cod_Mat : int
        - nombre : String
    }

    class Mat_Apro {
        - codigo : int
    }

    class Nota_Final {
        - nota : int
        - condicion : Condicion
    }

    class Plan_Estudio {
        - id_p : int
        - ano_plan : fecha
        - version : int
    }

    class Carrera {
        - cod_carrera : int
        - nombre : String
        - duracion : int
    }

    class Periodo {
        - ano : int
        - periodo : int
        - cargo : Cargo
        - par : Participacion
    }

    class Cargo {
        <<enumeration>>
        responsable_cat
        jtp
        ayudante
    }

    class Participacion {
        <<enumeration>>
        responsable
        colaborador
    }

    class Condicion {
        <<enumeration>>
        Aprobado
        Regular
        Libre
    }

    class Estado {
        <<enumeration>>
        ingresante
        avanzado
    }

    class periodo {
        <<enumeration>>
        cuatrimestral
        anual
    }

    Persona <|-- Profesor
    Persona <|-- Estudiante

    Profesor "1" -- "1..*" Periodo
    Materia "0..*" -- "1..*" Periodo

    Materia "1..*" --> "0..*" Materia : correlatividad
    Materia "1" -- "1..*" Mat_Apro

    Estudiante "0..*" -- "0..*" Materia : cursa
    Estudiante "0..*" -- "0..*" Materia : rindo
    Estudiante "1..*" -- "0..*" Nota_Final
    Materia "1..*" -- "0..*" Nota_Final

    Carrera "1" -- "1" Plan_Estudio : vigente
    Plan_Estudio "1" -- "1..*" Materia
    Carrera "1" -- "1..*" Estudiante
```

>>>>>>> fd48374dc852c7830073feb68a4ca3fb79ad763d
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

<<<<<<< HEAD
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
=======
  

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
>>>>>>> fd48374dc852c7830073feb68a4ca3fb79ad763d
```

#### Diagrama Entidad relacion 
``` mermaid
erDiagram

%% ======================
%% USUARIOS Y ROLES
%% ======================
USUARIO {
  int id PK
  string nombre
  string dni
  string email
  string password
  int rol_id FK
}

ROL {
  int id PK
  string nombre
}

USUARIO }o--|| ROL : pertenece

%% ======================
%% PLANES Y MATERIAS
%% ======================
PLAN_ESTUDIO {
  int id PK
  string nombre
  string version
}

MATERIA {
  int id PK
  string nombre
  string codigo
  int carga_horaria
  string modalidad
}

PLAN_MATERIA {
  int id PK
  int plan_id FK
  int materia_id FK
  int anio
  int cuatrimestre
}

PLAN_ESTUDIO ||--o{ PLAN_MATERIA : contiene
MATERIA ||--o{ PLAN_MATERIA : pertenece

ALUMNO_PLAN {
  int id PK
  int alumno_id FK
  int plan_id FK
}

USUARIO ||--o{ ALUMNO_PLAN : alumno
PLAN_ESTUDIO ||--o{ ALUMNO_PLAN : asignado

%% ======================
%% CORRELATIVAS
%% ======================
CORRELATIVA {
  int id PK
  int materia_id FK
  int materia_correlativa_id FK
}

MATERIA ||--o{ CORRELATIVA : tiene
MATERIA ||--o{ CORRELATIVA : es_requisito

%% ======================
%% COMISIONES
%% ======================
COMISION {
  int id PK
  string nombre
  int materia_id FK
  int docente_id FK
}

MATERIA ||--o{ COMISION : tiene
USUARIO ||--o{ COMISION : docente

%% ======================
%% CURSADAS
%% ======================
INSCRIPCION_CURSADA {
  int id PK
  int alumno_id FK
  int comision_id FK
  date fecha
}

USUARIO ||--o{ INSCRIPCION_CURSADA : alumno
COMISION ||--o{ INSCRIPCION_CURSADA : cursada

%% ======================
%% EXAMENES
%% ======================
EXAMEN_FINAL {
  int id PK
  int materia_id FK
  date fecha
}

INSCRIPCION_EXAMEN {
  int id PK
  int alumno_id FK
  int examen_id FK
  date fecha
}

MATERIA ||--o{ EXAMEN_FINAL : tiene
EXAMEN_FINAL ||--o{ INSCRIPCION_EXAMEN : incluye
USUARIO ||--o{ INSCRIPCION_EXAMEN : alumno

%% ======================
%% CALIFICACIONES
%% ======================
CALIFICACION {
  int id PK
  int alumno_id FK
  int materia_id FK
  float nota
  string tipo
  string condicion
}

USUARIO ||--o{ CALIFICACION : recibe
MATERIA ||--o{ CALIFICACION : evalua

%% ======================
%% ASISTENCIA
%% ======================
ASISTENCIA {
  int id PK
  int alumno_id FK
  int comision_id FK
  date fecha
  boolean presente
}

USUARIO ||--o{ ASISTENCIA : alumno
COMISION ||--o{ ASISTENCIA : registra

%% ======================
%% TAREAS
%% ======================
TAREA {
  int id PK
  int materia_id FK
  int docente_id FK
  string descripcion
  date fecha_entrega
}

ENTREGA {
  int id PK
  int tarea_id FK
  int alumno_id FK
  string archivo
  date fecha
  float nota
  string comentario
}

MATERIA ||--o{ TAREA : tiene
USUARIO ||--o{ TAREA : docente
TAREA ||--o{ ENTREGA : recibe
USUARIO ||--o{ ENTREGA : alumno

%% ======================
%% NOTIFICACIONES
%% ======================
NOTIFICACION {
  int id PK
  int usuario_id FK
  string mensaje
  date fecha
}

USUARIO ||--o{ NOTIFICACION : recibe
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
flowchart TB

%% ======================
%% CLIENTE
%% ======================
Cliente["Cliente (Browser)"]

%% ======================
%% SERVIDOR
%% ======================
subgraph Servidor

Controller["Controllers (Spark Routes)"]
View["Views (Mustache)"]

subgraph Logica
Service["Services"]
end

subgraph Datos
DAO["DAO (JDBC)"]
DBConn["DB Connection"]
end

end

%% ======================
%% BASE DE DATOS
%% ======================
DB["PostgreSQL"]

%% ======================
%% RELACIONES
%% ======================
Cliente --> Controller
Controller --> Service
Controller --> View

Service --> DAO
DAO --> DBConn
DBConn --> DB

View --> Cliente
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

├── dto/            (TRANSFERENCIA)
│   ├── UsuarioDTO.java
│   ├── LoginDTO.java
│   ├── InscripcionDTO.java
│   ├── TareaDTO.java

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

#### Flujo completo por ej login
1. Usuario envía formulario (frontend)
2. Controller recibe request
3. Controller crea DTO
4. Service valida reglas:
    - correlativas
    - estado del alumno
5. DAO ejecuta SQL
6. DB responde
7. DAO transforma → objeto
8. Service devuelve resultado
9. Controller renderiza vista
## EJEMPLO DE FLUJO DE DATOS
alta modificacion y registro de un usuario 
```mermaid 
sequenceDiagram

actor Admin
participant Controller
participant Service
participant DAO
participant DB

%% ======================
%% ALTA DE USUARIO
%% ======================
Admin->>Controller: POST /usuarios (datos)
Controller->>Service: crearUsuario(UsuarioDTO)
Service->>Service: validarDatos()
Service->>DAO: insertarUsuario(dto)
DAO->>DB: INSERT INTO usuario
DB-->>DAO: OK
DAO-->>Service: usuario creado
Service-->>Controller: resultado OK
Controller-->>Admin: Usuario creado

%% ======================
%% MODIFICACIÓN
%% ======================
Admin->>Controller: PUT /usuarios/{id}
Controller->>Service: actualizarUsuario(dto)
Service->>DAO: buscarUsuario(id)
DAO->>DB: SELECT usuario
DB-->>DAO: datos
DAO-->>Service: Usuario

Service->>Service: validarCambios()

Service->>DAO: updateUsuario(dto)
DAO->>DB: UPDATE usuario
DB-->>DAO: OK
DAO-->>Service: actualizado
Service-->>Controller: OK
Controller-->>Admin: Usuario actualizado

%% ======================
%% BAJA
%% ======================
Admin->>Controller: DELETE /usuarios/{id}
Controller->>Service: eliminarUsuario(id)

Service->>DAO: deleteUsuario(id)
DAO->>DB: DELETE FROM usuario
DB-->>DAO: OK

DAO-->>Service: eliminado
Service-->>Controller: OK
Controller-->>Admin: Usuario eliminado
```

