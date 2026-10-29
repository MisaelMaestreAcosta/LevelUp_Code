package com.mycompany.levelup_code.domain.enums;

/**
 * Enumeración que representa los resultados posibles tras evaluar un intento de resolución.
 * Según la especificación, los valores son CORRECT, INCORRECT, ERROR y TIMEOUT.
 */
public enum AttemptResult {
    /**
     * La solución pasó todos los casos de prueba o acertó la opción correcta.
     */
    CORRECT,

    /**
     * La solución arrojó un resultado distinto al esperado en uno o más casos de prueba.
     */
    INCORRECT,

    /**
     * Ocurrió un error en tiempo de compilación o de ejecución del código.
     */
    ERROR,

    /**
     * La ejecución excedió el límite de tiempo establecido para el reto.
     */
    TIMEOUT
}
