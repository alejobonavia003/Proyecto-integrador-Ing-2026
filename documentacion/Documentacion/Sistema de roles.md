**Resumen:** Al poner /admin/ en la url el back ya sabe que solo tiene que dejar pasar al Rol Admin 
# 🛡️ Guía de Seguridad, Roles y Convenciones de Rutas

Este documento detalla cómo funciona el sistema de autenticación, la protección de rutas basada en roles y las reglas que el equipo debe seguir al codificar nuevos controladores utilizando **Spark Java** y **ActiveJDBC**.

---

## 🔑 1. ¿Cómo funciona la Sesión?

Cuando un usuario inicia sesión de forma correcta a través del `AuthController` (`UC-01`), el servidor crea una sesión web persistente y almacena dos atributos clave:

* `req.session().attribute("user_id")` -> El identificador único (`id`) del usuario (tipo `Long`).
* `req.session().attribute("user_role")` -> Una cadena de texto (`String`) con el nombre del rol estrictamente en mayúsculas: `'ADMIN'`, `'TEACHER'`, o `'STUDENT'`.

---

## 🚏 2. Convención de Prefijos en URLs (Obligatorio)

Para que el filtro general de seguridad (`SecurityController`) pueda interceptar y validar los accesos de forma automática sin que tengamos que erescribir validaciones repetitivas en cada ruta, **todas las URLs del sistema deben respetar los siguientes prefijos**:

| Módulo / Rol | Prefijo de Ruta | Quién puede acceder | Ejemplos de uso |
| :--- | :--- | :--- | :--- |
| **Público** | `/login`, `/logout`, `/public/*` | Todos (Incluso sin loguear) | Pantalla de login, hojas de estilo CSS, JS. |
| **Administrador** | `/admin/*` | Únicamente usuarios con rol `ADMIN` | `/admin/courses/create`, `/admin/users/register` |
| **Docente** | `/teacher/*` | Únicamente usuarios con rol `TEACHER` | `/teacher/classes`, `/teacher/assignments/create` |
| **Alumno** | `/student/*` | Únicamente usuarios con rol `STUDENT` | `/student/enrollments`, `/student/submissions/submit` |

---

## 💻 3. Cómo crear un Controlador Seguro

Al desarrollar tu módulo asignado, debes estructurar las rutas dentro del método `init` de tu controlador respetando los prefijos mencionados arriba.

A continuación, se muestra un ejemplo real se debe estructurar el creador de materias para que **solo el administrador** pueda acceder:

```java
package controllers;

import models.Course;
import spark.template.mustache.MustacheTemplateEngine;
import java.util.HashMap;
import java.util.Map;
import static spark.Spark.*;

public class CourseController {

    public static void init(MustacheTemplateEngine engine) {

        // VISTA: Mostrar el formulario de creación (Solo accesible por ADMIN por llevar el prefijo /admin/)
        get("/admin/courses/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            // Pasamos datos si la plantilla mustache lo requiere
            return engine.render(new ModelAndView(model, "templates/courses/create.mustache"));
        });

        // ACCIÓN: Procesar el formulario de creación de materia (UC-06)
        post("/admin/courses/create", (req, res) -> {
            // Recolectamos parámetros del formulario HTML
            String code = req.queryParams("code");
            String name = req.queryParams("name");
            int hours = Integer.parseInt(req.queryParams("weekly_hours"));

            // Creación rápida usando ActiveJDBC
            Course course = new Course();
            course.set("code", code);
            course.set("name", name);
            course.set("weekly_hours", hours);
            course.saveIt(); // Guarda directo en SQLite

            // Redirigimos al listado general de materias
            res.redirect("/admin/courses");
            return null;
        });
    }
}
```


