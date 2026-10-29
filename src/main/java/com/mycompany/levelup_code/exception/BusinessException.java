package com.mycompany.levelup_code.exception;

/**
 * Excepción base para errores de validación y reglas de negocio en la plataforma.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
