package services;

import java.util.List;

import org.javalite.activejdbc.Model;

import models.Career;
import models.StudyPlan;

public class CareerService {

    public void createCareer(String name, String code, Integer duration) {
        
        // VALIDACIÓN NUEVA: Comprobar si el código ya existe
        if (Career.findFirst("code = ?", code) != null) {
            throw new IllegalArgumentException("Ese código de carrera ya existe. Por favor, ingresa uno distinto.");
        }

        Career career = new Career();
        career.set("name", name, "code", code, "duration", duration);
        
        if (!career.save()) {
            throw new IllegalArgumentException("Error de validación: " + career.errors().toString());
        }
    }

    public List<Model> getAllCareers() {
        return Career.findAll();
    }

    public Career getCareerById(Long id) {
        return Career.findById(id);
    }

    public List<Model> getStudyPlansByCareer(Long careerId) {
        return StudyPlan.where("career_id = ?", careerId);
    }

    // NUEVO MÉTODO: Crear un plan de estudio asociado a una carrera
    public void createStudyPlan(String name, String code, String version, Long careerId) {
        StudyPlan plan = new StudyPlan();
        plan.set("name", name);
        plan.set("code", code);
        plan.set("version", version);
        plan.set("career_id", careerId);
        
        if (!plan.save()) {
            throw new IllegalArgumentException("Error al guardar el plan de estudio: " + plan.errors());
        }
    }
}