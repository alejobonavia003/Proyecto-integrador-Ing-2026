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