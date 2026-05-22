-- Habilitar el soporte de Claves Foráneas en SQLite (Ejecutar siempre al conectar)
PRAGMA foreign_keys = ON;

-- 1. Table: roles
CREATE TABLE roles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Table: users
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dni VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id INTEGER NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON RESTRICT
);

-- 3. Table: courses
CREATE TABLE courses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    weekly_hours INTEGER NOT NULL
);

-- 4. Table: course_classes (Comisiones)
CREATE TABLE course_classes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) NOT NULL,
    course_id INTEGER NOT NULL,
    teacher_id INTEGER NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON RESTRICT
);

-- 5. Table: enrollments (Inscripciones)
CREATE TABLE enrollments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    course_class_id INTEGER NOT NULL,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (student_id, course_class_id),
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_class_id) REFERENCES course_classes(id) ON DELETE CASCADE
);

-- 6. Table: grades (Calificaciones)
CREATE TABLE grades (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    enrollment_id INTEGER NOT NULL,
    exam_name VARCHAR(100) NOT NULL,
    score DECIMAL(4, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE CASCADE
);

-- 7. Table: assignments (Tareas)
CREATE TABLE assignments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    course_class_id INTEGER NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    due_date TIMESTAMP NOT NULL,
    FOREIGN KEY (course_class_id) REFERENCES course_classes(id) ON DELETE CASCADE
);

-- 8. Table: submissions (Entregas)
CREATE TABLE submissions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    assignment_id INTEGER NOT NULL,
    student_id INTEGER NOT NULL,
    content_reference TEXT NOT NULL,
    grade DECIMAL(4, 2),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (student_id, assignment_id),
    FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Datos iniciales obligatorios
INSERT INTO roles (name) VALUES ('ADMIN'), ('TEACHER'), ('STUDENT');