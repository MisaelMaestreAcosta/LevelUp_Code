package com.mycompany.levelup_code.presentation.controller;

import com.mycompany.levelup_code.business.service.UserService;
import com.mycompany.levelup_code.domain.model.User;
import java.util.Optional;

/**
 * Controlador de presentación que coordina las acciones de autenticación entre la vista y {@link UserService}.
 */
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        if (userService == null) {
            throw new IllegalArgumentException("El servicio de usuarios es obligatorio.");
        }
        this.userService = userService;
    }

    public Optional<User> login(String email, String password) {
        return userService.authenticate(email, password);
    }

    public User register(String username, String email, String password) {
        return userService.register(username, email, password);
    }
}
