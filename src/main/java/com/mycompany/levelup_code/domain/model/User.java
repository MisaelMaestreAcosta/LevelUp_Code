package com.mycompany.levelup_code.domain.model;

import java.time.LocalDateTime;

/**
 * Entidad que representa a un Usuario del sistema (Tabla Usuarios).
 * Puede ser Estudiante o Administrador según su rol.
 * Contiene información de identificación, autenticación y estado de gamificación
 * (experiencia, nivel, rachas y vidas disponibles).
 */
public class User {

    // Identificador único del usuario (id_usuario)
    private int id;

    // Nombre completo o alias para visualización rápida
    private String username;

    // Campos desglosados del Diccionario de Datos (Tabla 2: Usuarios)
    private String firstName;
    private String middleName;
    private String lastName;
    private String secondLastName;

    // Correo electrónico utilizado para iniciar sesión (correo_electronico)
    private String email;

    // Contraseña encriptada (password_hash)
    private String passwordHash;

    // Rol del usuario dentro del sistema (id_rol -> Roles)
    private Role role;

    // Puntos de experiencia acumulados (exp_total)
    private int experience;

    // Nivel actual del usuario (nivel)
    private int level;

    // Racha actual de días consecutivos resolviendo retos (racha_actual)
    private int currentStreak;

    // Racha máxima histórica del usuario (racha_maxima)
    private int maxStreak;

    // Vidas o intentos disponibles (vidas)
    private int lives;

    // Fecha de registro del usuario (fecha_registro)
    private LocalDateTime registrationDate;

    // Última actividad registrada en la plataforma (ultima_actividad)
    private LocalDateTime lastActivity;

    /**
     * Constructor por defecto
     */
    public User() {
        this.experience = 0;
        this.level = 1;
        this.currentStreak = 0;
        this.maxStreak = 0;
        this.lives = 5;
        this.registrationDate = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
    }

    /**
     * Constructor con parámetros básicos
     */
    public User(int id, String username, String email, String passwordHash, Role role) {
        this();
        this.id = id;
        setUsername(username);
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
        if (username != null && !username.trim().isEmpty()) {
            return username;
        }
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(lastName);
        }
        return sb.length() > 0 ? sb.toString() : "Estudiante";
    }

    public void setUsername(String username) {
        this.username = username;
        if (username != null && !username.trim().isEmpty()) {
            String[] parts = username.trim().split("\\s+");
            if (parts.length == 1) {
                this.firstName = parts[0];
                if (this.lastName == null || this.lastName.isEmpty()) {
                    this.lastName = "Usuario";
                }
            } else if (parts.length == 2) {
                this.firstName = parts[0];
                this.lastName = parts[1];
            } else if (parts.length == 3) {
                this.firstName = parts[0];
                this.middleName = parts[1];
                this.lastName = parts[2];
            } else {
                this.firstName = parts[0];
                this.middleName = parts[1];
                this.lastName = parts[2];
                this.secondLastName = parts[3];
            }
        }
    }

    public String getFirstName() {
        return firstName != null && !firstName.isEmpty() ? firstName : getUsername();
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName != null && !lastName.isEmpty() ? lastName : "Usuario";
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSecondLastName() {
        return secondLastName;
    }

    public void setSecondLastName(String secondLastName) {
        this.secondLastName = secondLastName;
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

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = Math.max(0, lives);
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
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
