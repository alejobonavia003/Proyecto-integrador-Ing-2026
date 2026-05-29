package services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Model;

import models.Career;
import models.StudyPlan;

public class CareerService {

    public void createCareer(String name, String code, Integer duration) {

        // VALIDACIÓN NUEVA: Comprobar si el código ya existe
        if (Career.findFirst("code = ?", code) != null) {
            throw new IllegalArgumentException(
                    "Ese código de carrera ya existe. Por favor, ingresa uno distinto.");
        }

        Career career = new Career();
        career.set("name", name, "code", code, "duration", duration);

        if (!career.save()) {
            throw new IllegalArgumentException(
                    "Error de validación: " + career.errors().toString());
        }
    }

    public List<Model> getAllCareers() {
        return Career.findAll();
    }

    public List<Map<String, Object>> getAllCareersView() {
        List<Map<String, Object>> careers = new java.util.ArrayList<>();
        for (Model career : Career.findAll()) {
            Map<String, Object> row = new java.util.HashMap<>();
            row.put("id", career.getId());
            row.put("name", career.getString("name"));
            row.put("code", career.getString("code"));
            row.put("duration", career.getInteger("duration"));
            careers.add(row);
        }
        return careers;
    }

    public Map<String, Object> getCareerDetailView(Long careerId) {
        Career career = Career.findById(careerId);
        if (career == null) {
            return null;
        }

        List<Map<String, Object>> plansView = getStudyPlansViewByCareer(careerId);

        Map<String, Object> view = new HashMap<>();
        view.put("career_id", career.getId());
        view.put("career_name", career.getString("name"));
        view.put("career_code", career.getString("code"));
        view.put("plans", plansView);
        view.put("hasPlans", !plansView.isEmpty());

        return view;
    }

    public List<Map<String, Object>> getStudyPlansViewByCareer(Long careerId) {
        List<Map<String, Object>> plans = new java.util.ArrayList<>();
        for (Model plan : StudyPlan.where("career_id = ?", careerId)) {
            Map<String, Object> row = new java.util.HashMap<>();
            row.put("id", plan.getId());
            row.put("name", plan.getString("name"));
            row.put("code", plan.getString("code"));
            row.put("version", plan.getString("version"));
            row.put("career_id", careerId);
            plans.add(row);
        }
        return plans;
    }

    public Map<String, Object> getCareerSummary(Long careerId) {
        Career career = Career.findById(careerId);
        if (career == null) {
            return null;
        }
        Map<String, Object> summary = new HashMap<>();
        summary.put("career_id", career.getId());
        summary.put("career_name", career.getString("name"));
        return summary;
    }

    public Map<String, Object> getPlanSummary(Long planId) {
        StudyPlan plan = StudyPlan.findById(planId);
        if (plan == null) {
            return null;
        }
        Map<String, Object> summary = new HashMap<>();
        summary.put("plan_id", plan.getId());
        summary.put("plan_name", plan.getString("name"));
        summary.put("plan_code", plan.getString("code"));
        summary.put("career_id", plan.getLong("career_id"));
        return summary;
    }

    public Career getCareerById(Long id) {
        return Career.findById(id);
    }

    public StudyPlan getStudyPlanById(Long id) {
        return StudyPlan.findById(id);
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
            throw new IllegalArgumentException(
                    "Error al guardar el plan de estudio: " + plan.errors());
        }
    }

    /**
     * Obtiene la cantidad total de carreras registradas en el sistema.
     */
    public long getTotalCareersCount() {
        return models.Career.count();
    }
}
