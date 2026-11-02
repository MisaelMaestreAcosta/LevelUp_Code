package com.mycompany.levelup_code.app;

import com.mycompany.levelup_code.presentation.fx.LevelUpFxApp;

/**
 * Punto de entrada principal de la aplicación LevelUp Code.
 * Inicializa la conexión con Oracle 18c XE ({@link AppContext}) y lanza la interfaz gráfica
 * unificada en JavaFX 21 + CSS ({@code /styles/levelup.css}).
 */
public class Main {

    public static void main(String[] args) {
        // Pre-inicializa el contenedor de servicios y verifica la conexión a Oracle XEPDB1
        AppContext.getInstance();

        // Lanza la interfaz gráfica completa en JavaFX + CSS
        LevelUpFxApp.launchApp(args);
    }
}
