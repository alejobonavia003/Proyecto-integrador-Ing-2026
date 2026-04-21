package com.is1.proyecto.models.carrera;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("Materia")
public class Materia extends Model {
    
    public Integer getCodigo() {
        return getInteger("codigo");
    }

    public void setCodigo(Integer codigo) {
        set("codigo", codigo);
    }
    
    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    public Integer getIdPlan() {
        return getInteger("id_plan");
    }

    public void setIdPlan(Integer id_plan) {
        set("id_plan", id_plan);
    }





}
