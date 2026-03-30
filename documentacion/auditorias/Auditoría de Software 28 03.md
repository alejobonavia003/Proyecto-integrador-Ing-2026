---
fecha: 2026-03-28
tipo: Auditoría de Software
proyecto: Sistema de Administración Académica
estado: Análisis de Riesgos Inicial
---
---

# 📋 Auditoría de Stack: Ecosistema Spring Boot

> [!SUCCESS] Evolución del Proyecto
> La transición de JDBC puro a **Spring Data JPA** y la inclusión de **Spring Security** profesionaliza la arquitectura y mitiga riesgos de seguridad previos.

---

## 1. Nueva Matriz de Riesgos (Stack Actualizado)

| Tipo de Riesgo    | Descripción                                                                                                                             | Probabilidad | Impacto    |
| :---------------- | :-------------------------------------------------------------------------------------------------------------------------------------- | :----------- | :--------- |
| **Técnico**       | **Indecisión Frontend:** Elegir entre React (SPA) o Thymeleaf (Monolito) cambia radicalmente la estructura del Controller.              | 🔴 Alta      | 🟠 Alto    |
| **Técnico**       | **Configuración de Spring Security:** La gestión de roles y permisos puede volverse compleja y bloquear el acceso si no se testea bien. | 🟡 Media     | 💀 Crítico |
| **Técnico**       | **Mapeo Hibernate:** Riesgo de "N+1 select" o problemas de performance en relaciones de Base de Datos complejas.                        | 🟢 Baja      | 🟡 Medio   |
| **Planificación** | **Curva de Aprendizaje:** El equipo de 5 debe dominar las anotaciones de Spring y el ciclo de vida de los Beans.                        | 🔴 Alta      | 🟠 Alto    |
| **Planificación** | **Configuración de Entorno:** Unificar versiones de Java/Maven en las 5 notebooks para evitar "en mi máquina funciona".                 | 🟡 Media     | 🟡 Medio   |

---

## 2. Análisis del Auditor: React vs. Thymeleaf

> [!INFO] Decisión Arquitectónica
> Esta es la mayor incógnita actual. Aquí la comparativa para tu equipo:

| Criterio          | **Thymeleaf** (Recomendado para velocidad)          | **React** (Recomendado para UX moderna)                  |
| :---------------- | :-------------------------------------------------- | :------------------------------------------------------- |
| **Esfuerzo**      | Menor. Todo vive dentro del proyecto Maven.         | Mayor. Requiere manejar un proyecto separado (Node/npm). |
| **Curva**         | Baja si saben HTML/CSS.                             | Alta (Hooks, State Management, JSX).                     |
| **Arquitectura**  | Monolito clásico (MVC tradicional).                 | Desacoplado (Backend REST API + Frontend).               |
| **Recomendación** | Ideal si el plazo del cuatrimestre es muy ajustado. | Ideal si el equipo ya tiene experiencia previa en JS.    |

---

## 3. Estructura de Datos para IA (JSON v2)
*Este bloque permite a una IA analizar la madurez de tu stack.*

```json
{
  "project_id": "sys_admin_academica_2026",
  "stack_update": {
    "core": "Spring Boot",
    "persistence": "Spring Data JPA / Hibernate",
    "security": "Spring Security (RBAC)",
    "build_tool": "Maven",
    "web_layer": "Spring Web REST"
  },
  "critical_alerts": [
    "Frontend choice pending: React vs Thymeleaf",
    "Security implementation complexity",
    "Learning curve for 5-person team"
  ]
}