package com.mycompany.levelup_code.service;

import com.mycompany.levelup_code.model.Challenge;
import java.util.Optional;

/** Contrato de consulta de retos requerido por la lógica de negocio. */
public interface ChallengeRepository {

    Optional<Challenge> findById(int challengeId);
}
