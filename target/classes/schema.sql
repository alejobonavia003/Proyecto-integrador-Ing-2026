-- =========================================
-- HABILITAR FOREIGN KEYS
-- =========================================

PRAGMA foreign_keys = ON;

-- =========================================
-- DROP TABLES
-- =========================================

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

    FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE RESTRICT
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

    study_plan_id INTEGER NOT NULL,

    FOREIGN KEY (study_plan_id)
        REFERENCES study_plans(id)
        ON DELETE CASCADE
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

    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(student_id, course_class_id),

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

    course_class_id INTEGER NOT NULL,

    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,

    due_date TIMESTAMP NOT NULL,

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
-- DATOS INICIALES
-- =========================================

-- Datos iniciales obligatorios
INSERT INTO roles (name) VALUES ('ADMIN'), ('TEACHER'), ('STUDENT');