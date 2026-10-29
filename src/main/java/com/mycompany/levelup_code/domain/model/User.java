package com.mycompany.levelup_code.domain.model;

import java.time.LocalDateTime;

/**
 * Entidad que representa a un Usuario del sistema.
 * Puede ser Estudiante o Administrador según su rol.
 * Contiene información de autenticación y estado de gamificación (racha, nivel, experiencia).
 */
public class User {

    // Identificador único del usuario
    private int id;

    // Nombre de usuario para mostrar en la interfaz
    private String username;

    // Correo electrónico utilizado para iniciar sesión
    private String email;

    // Contraseña encriptada (hash), según RNF-01
    private String passwordHash;

    // Rol del usuario dentro del sistema (Estudiante o Administrador)
    private Role role;

    // Puntos de experiencia acumulados (gamificación)
    private int experience;

    // Nivel actual del usuario (gamificación)
    private int level;

    // Racha actual de días consecutivos resolviendo retos
    private int currentStreak;

    // Racha máxima histórica del usuario
    private int maxStreak;
    
    // Fecha de registro del usuario
    private LocalDateTime registrationDate;

    /**
     * Constructor por defecto
     */
    public User() {
        this.experience = 0;
        this.level = 1;
        this.currentStreak = 0;
        this.maxStreak = 0;
        this.registrationDate = LocalDateTime.now();
    }

    /**
     * Constructor con parámetros
     */
    public User(int id, String username, String email, String passwordHash, Role role) {
        this();
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // --- Getters y Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public void setMaxStreak(int maxStreak) {
        this.maxStreak = maxStreak;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    /**
     * Consulta si el usuario tiene asignado el rol de Administrador.
     *
     * @return true si el rol es Administrador, false en caso contrario
     */
    public boolean isAdministrator() {
        return role != null && role.isAdministrator();
    }

    /**
     * Suma experiencia al acumulado del usuario; rechaza valores no positivos.
     *
     * @param amount Puntos de experiencia a sumar
     */
    public void addExperience(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("La cantidad de experiencia a sumar debe ser positiva.");
        }
        this.experience += amount;
    }

    public int getTotalExp() {
        return experience;
    }

    public void setTotalExp(int totalExp) {
        this.experience = totalExp;
    }
}
