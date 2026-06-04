-- =========================================
-- HABILITAR FOREIGN KEYS
-- =========================================

PRAGMA foreign_keys = ON;

-- =========================================
-- DROP TABLES
-- =========================================
DROP TABLE IF EXISTS final_exam_enrollments;
DROP TABLE IF EXISTS final_exams;
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
-- TABLE: final_exams (Instancias de Finales)
-- Creadas por el ADMIN
-- =========================================
CREATE TABLE final_exams (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,
    teacher_id INTEGER NOT NULL,
    registration_start TIMESTAMP NOT NULL,
    registration_end TIMESTAMP NOT NULL,
    exam_date TIMESTAMP NOT NULL, -- Fecha en la que se rendirá el examen
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- =========================================
-- TABLE: final_exam_enrollments (Inscripción a Finales)
-- =========================================
CREATE TABLE final_exam_enrollments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    final_exam_id INTEGER NOT NULL,
    student_id INTEGER NOT NULL,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha automática de inscripción
    grade DECIMAL(4,2), -- Calificación (para cargar luego del examen)
    status VARCHAR(20) NOT NULL DEFAULT 'INSCRIPTO',
    UNIQUE(final_exam_id, student_id), -- Evita que un alumno se anote dos veces
    FOREIGN KEY (final_exam_id) REFERENCES final_exams(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =========================================
-- TABLE: student_subjects (Materias aprobadas definitivamente)
-- =========================================
CREATE TABLE student_subjects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    subject_id INTEGER NOT NULL,
    final_exam_id INTEGER,
    grade DECIMAL(4,2),
    approved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(student_id, subject_id),
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (final_exam_id) REFERENCES final_exams(id) ON DELETE SET NULL
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


-- =========================================
-- DATOS DE PRUEBA: CARRERAS (2 Carreras)
-- =========================================
INSERT INTO careers (id, code, name, duration) VALUES 
(10, 'ISW', 'Ingeniería en Software', 5),
(11, 'CDA', 'Licenciatura en Ciencia de Datos', 4);

-- =========================================
-- DATOS DE PRUEBA: PLANES DE ESTUDIO (2 por carrera)
-- =========================================
INSERT INTO study_plans (id, code, name, version, career_id) VALUES 
(10, 'ISW-2020', 'Plan 2020 - Ingeniería en Software (Heredado)', '2020', 10),
(11, 'ISW-2024', 'Plan 2024 - Ingeniería en Software (Actual)', '2024', 10),
(12, 'CDA-2021', 'Plan 2021 - Ciencia de Datos (Heredado)', '2021', 11),
(13, 'CDA-2025', 'Plan 2025 - Ciencia de Datos (Actual)', '2025', 11);

-- =========================================
-- DATOS DE PRUEBA: MATERIAS (7 materias para armar un buen árbol)
-- =========================================
-- Materias del Plan ISW-2024 (ID 11)
INSERT INTO subjects (id, code, name, weekly_hours, modality, study_plan_id) VALUES 
(10, 'INT-01', 'Introducción a la Programación', 6, 'PRESENCIAL', 11),
(11, 'MAT-01', 'Matemática Discreta', 4, 'PRESENCIAL', 11),
(12, 'EST-02', 'Estructuras de Datos', 6, 'PRESENCIAL', 11),
(13, 'BDD-03', 'Bases de Datos Avanzadas', 6, 'PRESENCIAL', 11),
(14, 'ING-03', 'Ingeniería de Software I', 8, 'HIBRIDA', 11);

-- Materias del Plan CDA-2025 (ID 13)
INSERT INTO subjects (id, code, name, weekly_hours, modality, study_plan_id) VALUES 
(15, 'ANA-01', 'Análisis Exploratorio de Datos', 6, 'PRESENCIAL', 13),
(16, 'ML-02', 'Machine Learning', 8, 'VIRTUAL', 13);

-- =========================================
-- DATOS DE PRUEBA: CORRELATIVIDADES
-- =========================================
-- requires_approved = 1 (Necesita Final), requires_approved = 0 (Solo necesita Regularidad)
INSERT INTO correlativities (subject_id, required_subject_id, requires_approved) VALUES 
-- Estructuras de Datos requiere Introducción a la Programación (Aprobada)
(12, 10, 1),
-- Bases de Datos Avanzadas requiere Estructuras de Datos (Regular)
(13, 12, 0),
-- Ingeniería de Software I requiere Estructuras de Datos (Aprobada) y Matemática Discreta (Regular)
(14, 12, 1),
(14, 11, 0),
-- Machine Learning requiere Análisis de Datos (Aprobada)
(16, 15, 1);

-- =========================================
-- DATOS DE PRUEBA: PROFESORES (5 Profesores)
-- =========================================
INSERT INTO users (id, dni, name, email, password, role_id) VALUES 
(10, '10000001', 'Alan Turing', 'alan.turing@univ.edu', '$2a$10$FXc.mc7s3ATkgZcnPSQBY.Cx6p5pZgGorQy33WqPOH46GB0o/wvmO', 2),
(11, '10000002', 'Ada Lovelace', 'ada.lovelace@univ.edu', '$2a$10$FXc.mc7s3ATkgZcnPSQBY.Cx6p5pZgGorQy33WqPOH46GB0o/wvmO', 2),
(12, '10000003', 'Grace Hopper', 'grace.hopper@univ.edu', '$2a$10$FXc.mc7s3ATkgZcnPSQBY.Cx6p5pZgGorQy33WqPOH46GB0o/wvmO', 2),
(13, '10000004', 'Edsger Dijkstra', 'edsger.dijkstra@univ.edu', '$2a$10$FXc.mc7s3ATkgZcnPSQBY.Cx6p5pZgGorQy33WqPOH46GB0o/wvmO', 2),
(14, '10000005', 'Linus Torvalds', 'linus.torvalds@univ.edu', '$2a$10$FXc.mc7s3ATkgZcnPSQBY.Cx6p5pZgGorQy33WqPOH46GB0o/wvmO', 2);

-- Asignar Profesores a Carreras
INSERT INTO teacher_careers (teacher_id, career_id) VALUES 
(10, 10), (11, 10), (12, 10), -- ISW
(13, 11), (14, 11);           -- CDA

-- Asignar Profesores a Materias (Titulares para que puedan crear comisiones/finales)
INSERT INTO teacher_subjects (teacher_id, subject_id, role_charge, academic_year, academic_period) VALUES 
(10, 10, 'TITULAR', 2026, '1° Cuatrimestre'), -- Turing da Intro
(11, 11, 'TITULAR', 2026, '1° Cuatrimestre'), -- Lovelace da Matemática
(12, 12, 'TITULAR', 2026, '1° Cuatrimestre'), -- Hopper da Estructuras
(12, 13, 'TITULAR', 2026, '2° Cuatrimestre'), -- Hopper da Bases de Datos
(13, 15, 'TITULAR', 2026, '1° Cuatrimestre'), -- Dijkstra da Análisis
(14, 16, 'TITULAR', 2026, '2° Cuatrimestre'); -- Torvalds da ML

-- =========================================
-- DATOS DE PRUEBA: ALUMNOS (5 Alumnos)
-- =========================================
INSERT INTO users (id, dni, name, email, password, role_id, study_plan_id) VALUES 
(20, '40000001', 'Juan Pérez', 'juan.perez@alumno.edu', '$2a$10$FXc.mc7s3ATkgZcnPSQBY.Cx6p5pZgGorQy33WqPOH46GB0o/wvmO', 3, 11), -- ISW-2024
(21, '40000002', 'María Gómez', 'maria.gomez@alumno.edu', '$2a$10$FXc.mc7s3ATkgZcnPSQBY.Cx6p5pZgGorQy33WqPOH46GB0o/wvmO', 3, 11), -- ISW-2024
(22, '40000003', 'Carlos López', 'carlos.lopez@alumno.edu', '$2a$10$FXc.mc7s3ATkgZcnPSQBY.Cx6p5pZgGorQy33WqPOH46GB0o/wvmO', 3, 11), -- ISW-2024
(23, '40000004', 'Ana Martínez', 'ana.martinez@alumno.edu', '$2a$10$FXc.mc7s3ATkgZcnPSQBY.Cx6p5pZgGorQy33WqPOH46GB0o/wvmO', 3, 13), -- CDA-2025
(24, '40000005', 'Pedro Rodríguez', 'pedro.rodriguez@alumno.edu', '$2a$10$FXc.mc7s3ATkgZcnPSQBY.Cx6p5pZgGorQy33WqPOH46GB0o/wvmO', 3, 13); -- CDA-2025

-- =========================================
-- DATOS DE PRUEBA: COMISIONES (Course Classes)
-- =========================================
-- Para que los alumnos puedan cursar e intentar aprobar para el testeo
INSERT INTO course_classes (id, name, capacity, subject_id, teacher_id) VALUES 
(10, 'Comisión A - Mañana', 40, 10, 10),
(11, 'Comisión Única', 30, 11, 11),
(12, 'Comisión B - Tarde', 35, 12, 12),
(13, 'Comisión Única', 25, 15, 13);