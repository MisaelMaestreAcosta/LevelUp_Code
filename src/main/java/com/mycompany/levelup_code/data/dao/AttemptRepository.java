package com.mycompany.levelup_code.data.dao;

import com.mycompany.levelup_code.domain.model.Attempt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Puerto de negocio para almacenar intentos y consultar el historial. */
public interface AttemptRepository {

    /** Indica si ya existe un intento correcto del usuario para el reto. */
    boolean hasSuccessfulAttempt(int userId, int challengeId);

    /** Guarda el intento y devuelve la entidad registrada. */
    Attempt save(Attempt attempt);

    /** Devuelve los intentos del usuario para el reto, en orden cronológico. */
    List<Attempt> findByUserAndChallenge(int userId, int challengeId);

    /** Returns the newest attempts first, capped for responsive history views. */
    default List<Attempt> findRecentByUserAndChallenge(
            int userId, int challengeId, int limit) {
        if (limit <= 0) throw new IllegalArgumentException("El límite debe ser mayor que cero.");
        List<Attempt> chronological = new ArrayList<>(
                findByUserAndChallenge(userId, challengeId));
        Collections.reverse(chronological);
        return chronological.size() <= limit
                ? chronological
                : new ArrayList<>(chronological.subList(0, limit));
    }

    /**
     * Indica si el repositorio aplica de forma atómica las reglas de gamificación
     * (XP, nivel, racha, vidas y logros) al guardar un intento (ej. mediante PL/SQL).
     */
    default boolean managesGamificationTransactionally() {
        return false;
    }
}
