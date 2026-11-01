package com.mycompany.levelup_code.app;

import com.mycompany.levelup_code.presentation.component.LoginFrame;
import com.mycompany.levelup_code.presentation.fx.LevelUpFxApp;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada principal de la aplicación LevelUp Code.
 * Inicializa la conexión con Oracle 18c XE ({@link AppContext}) y lanza la interfaz gráfica.
 */
public class Main {

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Pre-inicializa el contenedor de servicios y verifica la conexión a Oracle XEPDB1
        AppContext.getInstance();

        if (args != null && args.length > 0 && "--javafx".equalsIgnoreCase(args[0])) {
            try {
                LevelUpFxApp.launchApp(args);
                return;
            } catch (Throwable ignored) {
                // Si el entorno no incluye module-path de JavaFX, continúa con la interfaz principal
            }
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
