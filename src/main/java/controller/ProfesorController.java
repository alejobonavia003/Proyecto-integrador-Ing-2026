package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.persona.PersonaAbs;
import models.persona.PersonaConcreta;
import models.persona.Profesor;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

public class ProfesorController {

    public static ModelAndView mostrarPanel(Request req, Response res) {
        Map<String, Object> model = new HashMap<>(); 
        String currentUsername = req.session().attribute("currentUserUsername");
        Boolean loggedIn = req.session().attribute("loggedIn");
        
        if (currentUsername == null || loggedIn == null || !loggedIn) {
            System.out.println("DEBUG: Acceso no autorizado a /dashboard. Redirigiendo a /login.");
            res.redirect("?error=Debes iniciar sesion para acceder a esta pagina.");
            return null; 
        }

        model.put("username", currentUsername);
        return new ModelAndView(new HashMap<>(), "profesor.mustache");
    }

    public static ModelAndView mostrarAlta(Request req, Response res) {
        Map<String, Object> model = new HashMap<>(); 
        Boolean loggedIn = req.session().attribute("loggedIn");
        
        if ( loggedIn == null || !loggedIn) {
            System.out.println("DEBUG: Acceso no autorizado a /dashboard. Redirigiendo a /login.");
            res.redirect("?error=Debes iniciar sesion para acceder a esta pagina.");
            return null; 
        }

        String successMessage = req.queryParams("message");
        if (successMessage != null && !successMessage.isEmpty()) {
             System.out.println("DEBUGGG :::::::::::::::"+successMessage);
            model.put("successMessage", successMessage);
        }

        String errorMessage = req.queryParams("error");
        if (errorMessage != null && !errorMessage.isEmpty()) {
            System.out.println("DEBUGGG :::::::::::::::"+errorMessage);
            model.put("errorMessage", errorMessage);
        }
        return new ModelAndView(model, "alta_profesor.mustache");
    }

    public static String procesarAlta(Request req, Response res) {
        res.type("application/json"); 
        String name = req.queryParams("nombre");
        String apellido = req.queryParams("apellido");
        String dniS = req.queryParams("dni");
        String telefono = req.queryParams("telefono");
        String direccion = req.queryParams("direccion");
        String email = req.queryParams("email");

        if (name == null || name.isEmpty()|| apellido == null|| apellido.isEmpty() || email == null || email.isEmpty() || dniS == null || dniS.isEmpty() || telefono == null || telefono.isEmpty() || direccion == null || direccion.isEmpty()) {
            res.redirect("/alta-profesor?error=Debes rellenar todos los campos");
            return "";
        }
        
        Integer dni = Integer.parseInt(dniS); 
        PersonaAbs dniExistente = PersonaConcreta.findFirst("dni = ?", dni);
        PersonaAbs emailExistente = PersonaConcreta.findFirst("dni = ?", email);
        
        if (dniExistente != null || emailExistente != null) {
            res.redirect("/alta-profesor?error=DNI o EMAIL ya existente!!!");
            return "";
        }

        try {
            PersonaAbs persona = new PersonaConcreta();
            persona.setDni(dni);
            persona.setNombre(name);
            persona.setApellido(apellido);
            persona.setTelefono(telefono);
            persona.setDireccion(direccion);
            persona.setEmail(email);
            persona.saveIt();
            
            Profesor profesor = new Profesor();
            profesor.setDni(dni); 
            profesor.saveIt();

            res.redirect("/alta-profesor?message=Profesor " + name + " agregado exitosamente!");
            return "";

        } catch (Exception e) {
            System.err.println("Error al registrar profesor: " + e.getMessage());
            e.printStackTrace(); 
            res.redirect("/alta-profesor?error=Error interno al crear el profesor. Intente de nuevo.");
            return "";
        }
    }

    public static ModelAndView listarProfesores(Request req, Response res) {
        Map<String, Object> model = new HashMap<>(); 
        Boolean loggedIn = req.session().attribute("loggedIn");
        
        if ( loggedIn == null || !loggedIn) {
            System.out.println("DEBUG: Acceso no autorizado a /dashboard. Redirigiendo a /login.");
            res.redirect("?error=Debes iniciar sesion para acceder a esta pagina.");
            return null; 
        }

        try {
            List<PersonaConcreta> profesores = PersonaConcreta.findAll();
            List<Map<String, Object>> listaProfesores = new ArrayList<>();   
            
            for (PersonaConcreta p : profesores) {
                Map<String, Object> profMap = new HashMap<>();
                profMap.put("id", p.getDni());
                profMap.put("nombre", p.getNombre());
                profMap.put("apellido", p.getApellido());
                profMap.put("dni", p.getDni());
                listaProfesores.add(profMap);
                System.out.println("DEBUG::::::::::::::::::     "+p.getInteger("dni"));
            }
            model.put("profesores", listaProfesores);

        } catch (Exception e) {
            System.err.println("Error al listar profesores: " + e.getMessage());
            res.redirect("/error?error=error al listar profesores");
        }

        return new ModelAndView(model, "table_profesor.mustache");
    }
}