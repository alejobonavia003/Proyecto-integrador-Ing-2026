-- =========================================
-- HABILITAR FOREIGN KEYS
-- =========================================

PRAGMA foreign_keys = ON;

-- =========================================
-- DROP TABLES
-- =========================================

DROP TABLE IF EXISTS teacher_subjects;
DROP TABLE IF EXISTS teacher_careers;
DROP TABLE IF EXISTS submissions;
DROP TABLE IF EXISTS assignments;
DROP TABLE IF EXISTS grades;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS course_classes;
DROP TABLE IF EXISTS correlativities;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS study_plans;
DROP TABLE IF EXISTS careers;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
-- =========================================
-- TABLE: roles
-- =========================================

CREATE TABLE roles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- =========================================
-- TABLE: users
-- =========================================

CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    dni VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,

    role_id INTEGER NOT NULL,
    study_plan_id INTEGER,

    FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE RESTRICT

    FOREIGN KEY (study_plan_id)
        REFERENCES study_plans(id)
        ON DELETE SET NULL
);

-- =========================================
-- TABLE: careers
-- Carreras
-- =========================================

CREATE TABLE careers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    duration INTEGER
);

-- =========================================
-- TABLE: study_plans
-- Planes de estudio
-- =========================================

CREATE TABLE study_plans (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    version VARCHAR(20) NOT NULL,

    career_id INTEGER NOT NULL,

    FOREIGN KEY (career_id)
        REFERENCES careers(id)
        ON DELETE CASCADE
);

-- =========================================
-- TABLE: subjects
-- Materias
-- =========================================

CREATE TABLE subjects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,

    weekly_hours INTEGER NOT NULL,
    modality VARCHAR(20),

    study_plan_id INTEGER,

    FOREIGN KEY (study_plan_id) REFERENCES study_plans(id) ON DELETE SET NULL
);

-- =========================================
-- TABLE: correlativities
-- Correlativas entre materias
-- =========================================

CREATE TABLE correlativities (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    subject_id INTEGER NOT NULL,
    required_subject_id INTEGER NOT NULL,

    requires_approved BOOLEAN NOT NULL,

    FOREIGN KEY (subject_id)
        REFERENCES subjects(id)
        ON DELETE CASCADE,

    FOREIGN KEY (required_subject_id)
        REFERENCES subjects(id)
        ON DELETE CASCADE
);

-- =========================================
-- TABLE: course_classes
-- Comisiones
-- =========================================

CREATE TABLE course_classes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    name VARCHAR(50) NOT NULL,
    capacity INTEGER,

    subject_id INTEGER NOT NULL,
    teacher_id INTEGER NOT NULL,

    FOREIGN KEY (subject_id)
        REFERENCES subjects(id)
        ON DELETE CASCADE,

    FOREIGN KEY (teacher_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
);

-- =========================================
-- TABLE: enrollments
-- Inscripciones a cursadas
-- =========================================
CREATE TABLE enrollments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    course_class_id INTEGER NOT NULL,
    status VARCHAR(50) DEFAULT 'REGULAR', -- <-- NUEVA COLUMNA

    FOREIGN KEY (student_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
        
    FOREIGN KEY (course_class_id)
        REFERENCES course_classes(id)
        ON DELETE CASCADE
);

-- =========================================
-- TABLE: grades
-- Calificaciones
-- =========================================

CREATE TABLE grades (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    enrollment_id INTEGER NOT NULL,

    exam_name VARCHAR(100) NOT NULL,
    score DECIMAL(4,2) NOT NULL,
    status VARCHAR(20) NOT NULL,

    FOREIGN KEY (enrollment_id)
        REFERENCES enrollments(id)
        ON DELETE CASCADE
);

-- =========================================
-- TABLE: assignments
-- Tareas
-- =========================================

CREATE TABLE assignments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,

    file_path TEXT,

    teacher_id INTEGER NOT NULL,
    career_id INTEGER,
    subject_id INTEGER,
    course_class_id INTEGER,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (teacher_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    FOREIGN KEY (career_id)
        REFERENCES careers(id)
        ON DELETE SET NULL,

    FOREIGN KEY (subject_id)
        REFERENCES subjects(id)
        ON DELETE SET NULL,

    FOREIGN KEY (course_class_id)
        REFERENCES course_classes(id)
        ON DELETE CASCADE
);

-- =========================================
-- TABLE: submissions
-- Entregas
-- =========================================

CREATE TABLE submissions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    assignment_id INTEGER NOT NULL,
    student_id INTEGER NOT NULL,

    content_reference TEXT NOT NULL,

    grade DECIMAL(4,2),
    comment TEXT,

    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(student_id, assignment_id),

    FOREIGN KEY (assignment_id)
        REFERENCES assignments(id)
        ON DELETE CASCADE,

    FOREIGN KEY (student_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);


-- =========================================
-- NUEVAS TABLAS PARA INSCRIPCIÓN DE DOCENTES
-- =========================================

-- Relación Docente - Carrera (Para saber en qué carreras está registrado)
CREATE TABLE teacher_careers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    teacher_id INTEGER NOT NULL,
    career_id INTEGER NOT NULL,
    UNIQUE(teacher_id, career_id),
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (career_id) REFERENCES careers(id) ON DELETE CASCADE
);

-- Relación Docente - Materia (Inscripción con rol y año)
-- =========================================
-- TABLE: teacher_subjects (Actualizada con Periodo Académico)
-- =========================================
CREATE TABLE teacher_subjects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    teacher_id INTEGER NOT NULL,
    subject_id INTEGER NOT NULL,
    role_charge VARCHAR(50) NOT NULL,      -- Ej: Titular, Adjunto, JTP
    academic_year INTEGER NOT NULL,        -- Año académico (Ej: 2026)
    academic_period VARCHAR(50) NOT NULL,  -- Periodo académico (Ej: '1° Cuatrimestre')
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);
-- =========================================
-- DATOS INICIALES
-- =========================================

-- Datos iniciales obligatorios
INSERT INTO roles (name) VALUES ('ADMIN'), ('TEACHER'), ('STUDENT');

-- =========================================
-- 1. CREAR USUARIO ADMIN
-- =========================================
-- Asignamos role_id = 1 (que corresponde a 'ADMIN' según tus inserts iniciales)

INSERT INTO users (dni, name, email, password, role_id) 
VALUES (
    '144899798', 
    'admin', 
    'bonaviaalejo@gmail.com', 
    '$2a$10$FXc.mc7s3ATkgZcnPSQBY.Cx6p5pZgGorQy33WqPOH46GB0o/wvmO', 
    1
);

-- =========================================
-- 2. CREAR CARRERA DE COMPUTACIÓN
-- =========================================
-- Asumimos que esta es la primera carrera, por lo que tendrá id = 1

INSERT INTO careers (code, name, duration) 
VALUES ('COMP', 'Analista en Ciencias de la Computación', 3);

-- =========================================
-- 3. CREAR 2 PLANES DE ESTUDIO PARA LA CARRERA
-- =========================================
-- Asignamos career_id = 1 (la carrera que acabamos de crear)

INSERT INTO study_plans (code, name, version, career_id) 
VALUES 
    ('PLAN-2019', 'Plan 2019 - Ciencias de la Computación', '2019', 1),
    ('PLAN-2024', 'Plan 2024 - Analista (Actualizado)', '2024', 1);

-- =========================================
-- 4. CREAR MATERIAS PARA LOS PLANES
-- =========================================
-- Las primeras materias van al Plan 2019 (id = 1), las otras al Plan 2024 (id = 2)

INSERT INTO subjects (code, name, weekly_hours, modality, study_plan_id) 
VALUES 
    -- Materias del Plan 2019 (study_plan_id = 1)
    ('ALG-01', 'Algoritmos y Estructuras de Datos', 6, 'PRESENCIAL', 1),
    ('ARQ-01', 'Arquitectura de Computadoras', 4, 'PRESENCIAL', 1),
    ('BD-01', 'Bases de Datos I', 6, 'PRESENCIAL', 1),
    
    -- Materias del Plan 2024 (study_plan_id = 2)
    ('PAR-02', 'Paradigmas de Programación', 6, 'PRESENCIAL', 2),
    ('SOR-02', 'Sistemas Operativos y Redes', 8, 'PRESENCIAL', 2),
    ('IS2-02', 'Ingeniería de Software II', 6, 'PRESENCIAL', 2),
    ('TFI-02', 'Trabajo Final Integrador', 4, 'VIRTUAL', 2);

-- =========================================
-- 5. AGREGAR ALGUNAS CORRELATIVIDADES (OPCIONAL)
-- =========================================
-- Esto es un extra para que pruebes bien las relaciones.
-- Hacemos que "Ingeniería de Software II" (id=6) requiera "Paradigmas de Programación" (id=4)
-- y que "Paradigmas..." requiera estar aprobada (requires_approved = 1 o true en SQLite).

INSERT INTO correlativities (subject_id, required_subject_id, requires_approved) 
VALUES (6, 4, 1);


-- =========================================
-- 6. CREAR USUARIO DOCENTE Y ASIGNAR CARRERA
-- =========================================
-- El role_id = 2 corresponde a 'TEACHER'
INSERT INTO users (dni, name, email, password, role_id) 
VALUES (
    '22222222', 
    'Profesor', 
    'profe@gmail.com', 
    '$2a$10$FXc.mc7s3ATkgZcnPSQBY.Cx6p5pZgGorQy33WqPOH46GB0o/wvmO', 
    2
);

-- Inscribimos automáticamente al docente (id=2) en la carrera de Computación (id=1)
INSERT INTO teacher_careers (teacher_id, career_id) VALUES (2, 1);

-- =========================================
-- 7. CREAR USUARIO ALUMNO
-- =========================================
-- El role_id = 3 corresponde a 'STUDENT'
INSERT INTO users (dni, name, email, password, role_id) 
VALUES (
    '33333333', 
    'Alumno', 
    'alumno@gmail.com', 
    '$2a$10$FXc.mc7s3ATkgZcnPSQBY.Cx6p5pZgGorQy33WqPOH46GB0o/wvmO', 
    3
);