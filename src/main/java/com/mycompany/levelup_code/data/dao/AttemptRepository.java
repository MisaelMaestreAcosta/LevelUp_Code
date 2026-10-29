package com.mycompany.levelup_code.data.dao;

import com.mycompany.levelup_code.domain.model.Attempt;
import java.util.List;

/** Puerto de negocio para almacenar intentos y consultar el historial. */
public interface AttemptRepository {

    /** Indica si ya existe un intento correcto del usuario para el reto. */
    boolean hasSuccessfulAttempt(int userId, int challengeId);

    /** Guarda el intento y devuelve la entidad registrada. */
    Attempt save(Attempt attempt);

    /** Devuelve los intentos del usuario para el reto, en orden cronológico. */
    List<Attempt> findByUserAndChallenge(int userId, int challengeId);
}
