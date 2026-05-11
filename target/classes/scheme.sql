-- Elimina la tabla 'users' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS users;

-- Crea la tabla 'users' con los campos originales, adaptados para SQLite
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- Clave primaria autoincremental para SQLite
    name TEXT NOT NULL UNIQUE,          -- Nombre de usuario (TEXT es el tipo de cadena recomendado para SQLite), con restricción UNIQUE
    password TEXT NOT NULL           -- Contraseña hasheada (TEXT es el tipo de cadena recomendado para SQLite)
);


-- Tabla base de personas
CREATE TABLE Persona (
    dni INTEGER PRIMARY KEY,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    telefono TEXT,
    direccion TEXT,
    email TEXT
);

-- Estudiante hereda de Persona (1 a 1)
CREATE TABLE Estudiante (
    dni INTEGER PRIMARY KEY,
    legajo TEXT,
    FOREIGN KEY (dni) REFERENCES Persona(dni)
);

-- Profesor hereda de Persona (1 a 1)
CREATE TABLE Profesor (
    dni INTEGER PRIMARY KEY,
    id_doc TEXT AUTOINCREMENT
    ,
    FOREIGN KEY (dni) REFERENCES Persona(dni)
);

-- Carrera
CREATE TABLE Carrera (
    cod_carrera INTEGER PRIMARY KEY,
    duracion INTEGER,
    nombre TEXT
);

-- Plan de Estudio
CREATE TABLE PlanEstudio (
    id INTEGER PRIMARY KEY,
    anio_plan INTEGER,
    version INTEGER,
    cod_carrera INTEGER NOT NULL,
    FOREIGN KEY (cod_carrera) REFERENCES Carrera(cod_carrera)
);

-- Relación vigente (Carrera - PlanEstudio)
CREATE TABLE Vigente (
    cod_carrera INTEGER NOT NULL,
    id_plan INTEGER NOT NULL,
    PRIMARY KEY (cod_carrera, id_plan),
    FOREIGN KEY (cod_carrera) REFERENCES Carrera(cod_carrera),
    FOREIGN KEY (id_plan) REFERENCES PlanEstudio(id)
);

-- Materia
CREATE TABLE Materia (
    codigo INTEGER PRIMARY KEY,
    nombre TEXT,
    id_plan INTEGER NOT NULL,
    FOREIGN KEY (id_plan) REFERENCES PlanEstudio(id)
);

-- Relación recursiva: correlativas de una materia
CREATE TABLE Correlativa (
    codigo_materia INTEGER NOT NULL,
    codigo_correlativa INTEGER NOT NULL,
    PRIMARY KEY (codigo_materia, codigo_correlativa),
    FOREIGN KEY (codigo_materia) REFERENCES Materia(codigo),
    FOREIGN KEY (codigo_correlativa) REFERENCES Materia(codigo)
);

-- Relación Estudiante - Materia (Cursa)
CREATE TABLE Cursa (
    dni INTEGER NOT NULL,
    codigo_materia INTEGER NOT NULL,
    PRIMARY KEY (dni, codigo_materia),
    FOREIGN KEY (dni) REFERENCES Estudiante(dni),
    FOREIGN KEY (codigo_materia) REFERENCES Materia(codigo)
);

-- Relación Estudiante - Materia (Rindió)
CREATE TABLE Rindio (
    dni INTEGER NOT NULL,
    codigo_materia INTEGER NOT NULL,
    nota INTEGER NOT NULL,
    condicion TEXT NOT NULL,
    PRIMARY KEY (dni, codigo_materia),
    FOREIGN KEY (dni) REFERENCES Estudiante(dni),
    FOREIGN KEY (codigo_materia) REFERENCES Materia(codigo)
);

-- Relación Profesor - Materia (Dicta)
CREATE TABLE Dicta (
    dni_prof INTEGER NOT NULL,
    codigo_materia INTEGER NOT NULL,
    PRIMARY KEY (dni_prof, codigo_materia),
    FOREIGN KEY (dni_prof) REFERENCES Profesor(dni),
    FOREIGN KEY (codigo_materia) REFERENCES Materia(codigo)
);
