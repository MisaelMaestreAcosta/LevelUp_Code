package com.mycompany.levelup_code.model;

/**
 * Entidad que representa un Rol de usuario dentro de la plataforma.
 * Según RF-01, un rol (Estudiante o Administrador) determina los permisos del usuario.
 * Se modela como clase y no como enum porque, según RF-12, el Administrador
 * debe poder gestionar (crear, editar) los roles desde el panel de administración.
 */
public class Role {

    // Identificador único del rol
    private int id;

    // Nombre del rol (ej. Estudiante, Administrador)
    private String name;

    // Descripción de los permisos o propósito del rol
    private String description;

    /**
     * Constructor por defecto
     */
    public Role() {
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param name Nombre del rol
     * @param description Descripción del rol
     */
    public Role(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // --- Getters y Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
