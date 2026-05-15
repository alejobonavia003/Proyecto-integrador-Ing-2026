-- Elimina las tablas si ya existen (en orden por dependencias)
DROP TABLE IF EXISTS Dicta;
DROP TABLE IF EXISTS Rindio;
DROP TABLE IF EXISTS Cursa;
DROP TABLE IF EXISTS Correlativa;
DROP TABLE IF EXISTS Materia;
DROP TABLE IF EXISTS Vigente;
DROP TABLE IF EXISTS PlanEstudio;
DROP TABLE IF EXISTS Carrera;
DROP TABLE IF EXISTS Profesor;
DROP TABLE IF EXISTS Estudiante;
DROP TABLE IF EXISTS Persona;
DROP TABLE IF EXISTS users;

-- =========================
-- Tabla de usuarios
-- =========================
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL
);

-- =========================
-- Tabla base Persona
-- =========================
CREATE TABLE IF NOT EXISTS Persona (
    dni INTEGER PRIMARY KEY,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    telefono TEXT,
    direccion TEXT,
    email TEXT
);

-- =========================
-- Estudiante (hereda Persona)
-- =========================
CREATE TABLE IF NOT EXISTS Estudiante (
    dni INTEGER PRIMARY KEY,
    legajo TEXT,
    FOREIGN KEY (dni) REFERENCES Persona(dni)
);

-- =========================
-- Profesor (hereda Persona)
-- =========================
CREATE TABLE IF NOT EXISTS Profesor (
    dni INTEGER PRIMARY KEY,
    id_doc TEXT,
    FOREIGN KEY (dni) REFERENCES Persona(dni)
);

-- =========================
-- Carrera
-- =========================
CREATE TABLE IF NOT EXISTS carreras (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    duracion INTEGER NOT NULL
);

-- =========================
-- Plan de Estudio
-- =========================
CREATE TABLE IF NOT EXISTS PlanEstudio (
    id INTEGER PRIMARY KEY,
    anio_plan INTEGER,
    version INTEGER,
    cod_carrera INTEGER NOT NULL,
    FOREIGN KEY (cod_carrera) REFERENCES Carrera(cod_carrera)
);

-- =========================
-- Relación Vigente
-- =========================
CREATE TABLE IF NOT EXISTS Vigente (
    cod_carrera INTEGER NOT NULL,
    id_plan INTEGER NOT NULL,
    PRIMARY KEY (cod_carrera, id_plan),
    FOREIGN KEY (cod_carrera) REFERENCES Carrera(cod_carrera),
    FOREIGN KEY (id_plan) REFERENCES PlanEstudio(id)
);

-- =========================
-- Materia
-- =========================
CREATE TABLE IF NOT EXISTS Materia (
    codigo INTEGER PRIMARY KEY,
    nombre TEXT NOT NULL,
    id_plan INTEGER NOT NULL,
    FOREIGN KEY (id_plan) REFERENCES PlanEstudio(id)
);

-- =========================
-- Correlativas
-- =========================
CREATE TABLE IF NOT EXISTS Correlativa (
    codigo_materia INTEGER NOT NULL,
    codigo_correlativa INTEGER NOT NULL,
    PRIMARY KEY (codigo_materia, codigo_correlativa),
    FOREIGN KEY (codigo_materia) REFERENCES Materia(codigo),
    FOREIGN KEY (codigo_correlativa) REFERENCES Materia(codigo)
);

-- =========================
-- Estudiante cursa materia
-- =========================
CREATE TABLE IF NOT EXISTS Cursa (
    dni INTEGER NOT NULL,
    codigo_materia INTEGER NOT NULL,
    PRIMARY KEY (dni, codigo_materia),
    FOREIGN KEY (dni) REFERENCES Estudiante(dni),
    FOREIGN KEY (codigo_materia) REFERENCES Materia(codigo)
);

-- =========================
-- Estudiante rindió materia
-- =========================
CREATE TABLE IF NOT EXISTS Rindio (
    dni INTEGER NOT NULL,
    codigo_materia INTEGER NOT NULL,
    nota INTEGER NOT NULL,
    condicion TEXT NOT NULL,
    PRIMARY KEY (dni, codigo_materia),
    FOREIGN KEY (dni) REFERENCES Estudiante(dni),
    FOREIGN KEY (codigo_materia) REFERENCES Materia(codigo)
);

-- =========================
-- Profesor dicta materia
-- =========================
CREATE TABLE IF NOT EXISTS Dicta (
    dni_prof INTEGER NOT NULL,
    codigo_materia INTEGER NOT NULL,
    PRIMARY KEY (dni_prof, codigo_materia),
    FOREIGN KEY (dni_prof) REFERENCES Profesor(dni),
    FOREIGN KEY (codigo_materia) REFERENCES Materia(codigo)
);