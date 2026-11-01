package com.mycompany.levelup_code.app;

import com.mycompany.levelup_code.presentation.component.LoginFrame;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada principal de la aplicación LevelUp Code.
 * Configura el renderizado tipográfico suavizado e inicia el flujo de presentación.
 */
public class Main {

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
