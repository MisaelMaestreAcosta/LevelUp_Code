package com.mycompany.levelup_code.domain.enums;

/**
 * Enumeración que representa el estado de progreso de un usuario en una lección.
 * Según RF-05, los estados son: pendiente, en curso y completada.
 */
public enum ProgressStatus {
    /**
     * La lección aún no ha sido iniciada por el estudiante.
     */
    PENDING,

    /**
     * La lección está siendo desarrollada por el estudiante.
     */
    IN_PROGRESS,

    /**
     * La lección ha sido aprobada y completada exitosamente.
     */
    COMPLETED
}
