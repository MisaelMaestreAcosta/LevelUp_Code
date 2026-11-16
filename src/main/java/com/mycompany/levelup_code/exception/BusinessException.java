package com.mycompany.levelup_code.exception;

/**
 * Excepción base para errores de validación y reglas de negocio en la plataforma.
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

