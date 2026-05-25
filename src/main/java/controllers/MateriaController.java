package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Materia; // Importante para consultas SQL si se necesitan
import spark.ModelAndView;
import spark.Request;
import spark.Response;

public class MateriaController {

    // GET /materias o /admin/courses
    public static ModelAndView getMaterias(Request req, Response res) {
        Map<String, Object> model = new HashMap<>();
        try {
            // Cargamos todas las materias
            List<Materia> materias = Materia.findAll();
            
            // Enviamos la lista a materias_lista.mustache
            // ActiveJDBC automáticamente mapeará atributos como 'modalidad' si existen en tu base de datos
            model.put("materias", materias);
            
            return new ModelAndView(model, "materias_lista.mustache"); 
        } catch (Exception e) {
            model.put("error", "Error interno al cargar las materias del sistema.");
            return new ModelAndView(model, "error.mustache");
        }
    }
    
    // GET para mostrar el formulario de creación
    public static ModelAndView mostrarFormulario(Request req, Response res) {
        Map<String, Object> model = new HashMap<>();
        return new ModelAndView(model, "materia_form.mustache"); 
    }

    // POST /materias/nueva
    public static ModelAndView crearMateria(Request req, Response res) {
        Map<String, Object> model = new HashMap<>();
        try {
            Materia nuevaMateria = new Materia();
            nuevaMateria.set("nombre", req.queryParams("nombre"));
            nuevaMateria.set("codigo_materia", req.queryParams("codigo_materia"));
            nuevaMateria.set("carga_horaria", Integer.parseInt(req.queryParams("carga_horaria")));

            if (nuevaMateria.save()) {
                res.redirect("/materias");
                return null;
            } else {
                model.put("error", "Los datos ingresados no cumplen con las reglas académicas.");
                return new ModelAndView(model, "error.mustache");
            }
        } catch (NumberFormatException e) {
            model.put("error", "La carga horaria debe ser un valor numérico válido.");
            return new ModelAndView(model, "error.mustache");
        } catch (Exception e) {
            model.put("error", "Excepción académica: No se pudo registrar la materia. Verifique que el código no exista.");
            return new ModelAndView(model, "error.mustache");
        }
    }

    // GET /materias/:id/correlativas
    public static ModelAndView getCorrelativas(Request req, Response res) {
        Map<String, Object> model = new HashMap<>();
        try {
            Materia materia = Materia.findById(req.params(":id"));
            
            if (materia == null) {
                res.status(404);
                model.put("error", "La dependencia académica solicitada no existe.");
                return new ModelAndView(model, "error.mustache");
            }

            model.put("materia", materia);
            
            // 1. Aquí puedes buscar las correlativas actuales de la materia
            // List<Materia> correlativasActuales = materia.getAll(Materia.class); // (Depende de tu setup en ActiveJDBC)
            // model.put("correlativas_actuales", correlativasActuales);
            
            // 2. Buscamos todas las demás materias para poblar el <select multiple>
            // Excluimos la materia actual para que una materia no sea correlativa de sí misma
            List<Materia> otrasMaterias = Materia.where("id != ?", materia.getId());
            model.put("otras_materias", otrasMaterias);
            
            return new ModelAndView(model, "materias_correlativas.mustache");
        } catch (Exception e) {
            model.put("error", "Error al consultar las dependencias académicas.");
            return new ModelAndView(model, "error.mustache");
        }
    }

    // POST /materias/:id/correlativas -> ESTE ES EL MÉTODO QUE SOLUCIONA EL ERROR EN APP.JAVA
    public static ModelAndView asignarCorrelativas(Request req, Response res) {
        Map<String, Object> model = new HashMap<>();
        try {
            Materia materia = Materia.findById(req.params(":id"));
            if (materia == null) {
                res.status(404);
                model.put("error", "Materia no encontrada.");
                return new ModelAndView(model, "error.mustache");
            }

            // Obtenemos los IDs de las materias seleccionadas en el form múltiple
            // req.queryParamsValues obtiene un Array de Strings cuando el select es 'multiple'
            String[] correlativasSeleccionadas = req.queryParamsValues("correlativas");

            /*
             * -------------------------------------------------------------
             * LÓGICA DE GUARDADO (Depende de tu base de datos)
             * -------------------------------------------------------------
             * Aquí deberías guardar la relación M:N. Te dejo un ejemplo
             * usando SQL directo por si no tienes un modelo ActiveJDBC
             * específico para la tabla intermedia "materias_correlativas".
             */
             
            // Paso 1: Borrar dependencias previas de esta materia
            // Base.exec("DELETE FROM materias_correlativas WHERE materia_id = ?", materia.getId());
            
            // Paso 2: Insertar las nuevas dependencias elegidas
            // if (correlativasSeleccionadas != null) {
            //     for (String idCorrelativa : correlativasSeleccionadas) {
            //         Base.exec("INSERT INTO materias_correlativas (materia_id, correlativa_id) VALUES (?, ?)", 
            //                   materia.getId(), idCorrelativa);
            //     }
            // }

            // Redirigimos al catálogo al terminar
            res.redirect("/materias");
            return null;

        } catch (Exception e) {
            model.put("error", "Error al asignar las correlatividades: " + e.getMessage());
            return new ModelAndView(model, "error.mustache");
        }
    }
}