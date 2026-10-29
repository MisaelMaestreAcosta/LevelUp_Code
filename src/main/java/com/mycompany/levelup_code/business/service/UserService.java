package com.mycompany.levelup_code.business.service;

import com.mycompany.levelup_code.data.dao.UserRepository;

import com.mycompany.levelup_code.domain.model.Role;
import com.mycompany.levelup_code.domain.model.User;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Reglas de negocio para registrar usuarios e iniciar sesión.
 *
 * <p>GRASP: actúa como Controller del caso de uso de autenticación y delega
 * la persistencia al repositorio. SOLID: depende de una abstracción y mantiene
 * en esta clase las reglas coherentes de registro e inicio de sesión.</p>
 */
public class UserService {

    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final int ITERATIONS = 120_000;

    private final UserRepository userRepository;
    private final Role studentRole;
    private final SecureRandom secureRandom = new SecureRandom();

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
        user.setPasswordHash(hashPassword(password));
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

        if (!verifyPassword(password, user.getPasswordHash())) {
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

        if (email == null || email.trim().length() > 254
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

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String hashPassword(String password) {
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);

        byte[] hash = deriveKey(password.toCharArray(), salt, ITERATIONS);

        return "pbkdf2$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    private boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null) {
            return false;
        }

        try {
            String[] parts = storedHash.split("\\$");

            if (parts.length != 4 || !"pbkdf2".equals(parts[0])) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 1 || iterations > 2_000_000) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
            if (salt.length != SALT_BYTES || expectedHash.length != KEY_BITS / 8) {
                return false;
            }
            byte[] actualHash =
                    deriveKey(password.toCharArray(), salt, iterations);

            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private byte[] deriveKey(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "No fue posible procesar la contraseña.", exception
            );
        } finally {
            spec.clearPassword();
        }
    }

}
