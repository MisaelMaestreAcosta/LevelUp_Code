package com.mycompany.levelup_code.service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/** Contrato para ejecutar el código contra una entrada de prueba. */
public interface CodeRunner {

    /**
     * Devuelve la salida estándar.
     * Lanza IOException si la ejecución falla y TimeoutException si excede el límite.
     */
    String run(
            String languageName,
            String sourceCode,
            String input,
            int timeoutSeconds)
            throws IOException, InterruptedException, TimeoutException;
}