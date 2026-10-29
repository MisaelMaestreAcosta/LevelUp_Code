package com.mycompany.levelup_code.presentation.component;

import javax.swing.JFrame;

/**
 * Ventana de presentación para el inicio de sesión y registro de usuarios.
 */
public class LoginFrame extends JFrame {

    public LoginFrame() {
        super("LevelUp Code - Iniciar Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);
    }
}

