package com.mycompany.levelup_code.business.service;

import com.mycompany.levelup_code.data.dao.UserRepository;

import com.mycompany.levelup_code.domain.model.Role;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.util.PasswordHasher;
import java.util.Locale;
import java.util.Optional;

/**
 * Reglas de negocio para registrar usuarios e iniciar sesión.
 *
 * <p>GRASP: actúa como Controller del caso de uso de autenticación y delega
 * la persistencia al repositorio. SOLID: depende de una abstracción y mantiene
 * en esta clase las reglas coherentes de registro e inicio de sesión.</p>
 */
public class UserService {

    public static final int MAX_EMAIL_LENGTH = 120;

    private final UserRepository userRepository;
    private final Role studentRole;

    public UserService(UserRepository userRepository, Role studentRole) {
        if (userRepository == null || studentRole == null) {
            throw new IllegalArgumentException(
                    "El repositorio y el rol de estudiante son obligatorios."
            );
        }

        this.userRepository = userRepository;
        this.studentRole = studentRole;
    }

    /**
     * Registra un usuario como estudiante.
     * La contraseña se guarda como hash con salt, nunca como texto plano.
     */
    public User register(String username, String email, String password) {
        validateRegistration(username, email, password);

        String normalizedEmail = normalizeEmail(email);

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con ese correo."
            );
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(PasswordHasher.hash(password));
        user.setRole(studentRole);

        User savedUser = userRepository.save(user);
        if (savedUser == null) {
            throw new IllegalStateException(
                    "El repositorio no devolvió el usuario registrado."
            );
        }
        return savedUser;
    }

    /**
     * Autentica al usuario y devuelve un resultado vacío si las credenciales
     * no coinciden.
     */
    public Optional<User> authenticate(String email, String password) {
        if (email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()) {
            return Optional.empty();
        }

        Optional<User> userResult = userRepository.findByEmail(normalizeEmail(email));

        if (userResult == null) {
            throw new IllegalStateException(
                    "El repositorio devolvió un resultado inválido."
            );
        }

        if (!userResult.isPresent()) {
            return Optional.empty();
        }

        User user = userResult.get();

        if (!PasswordHasher.verify(password, user.getPasswordHash())) {
            return Optional.empty();
        }

        return Optional.of(user);
    }

    private void validateRegistration(
            String username, String email, String password) {

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio."
            );
        }

        if (username.trim().length() > 80) {
            throw new IllegalArgumentException(
                    "El nombre de usuario no puede superar 80 caracteres."
            );
        }

        if (email == null || email.trim().length() > MAX_EMAIL_LENGTH
                || !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "El correo electrónico no es válido."
            );
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres."
            );
        }
    }

    public void updateProfile(int userId, String firstName, String lastName) {
        if (userId <= 0 || firstName == null || lastName == null || firstName.isBlank()
                || lastName.isBlank() || firstName.strip().length() > 50 || lastName.strip().length() > 50) {
            throw new IllegalArgumentException("Escribe nombre y apellido de hasta 50 caracteres cada uno.");
        }
        userRepository.updateProfile(userId, firstName.strip(), lastName.strip());
    }

    public void changePassword(int userId, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres.");
        }
        User fresh = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("La cuenta ya no está disponible."));
        String previousHash = fresh.getPasswordHash();
        if (!PasswordHasher.verify(currentPassword, previousHash)) {
            throw new IllegalArgumentException("La contraseña actual no coincide.");
        }
        if (!userRepository.compareAndSetPassword(userId, previousHash, PasswordHasher.hash(newPassword))) {
            throw new IllegalStateException("La contraseña cambió durante la operación. Vuelve a verificarla.");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

}
