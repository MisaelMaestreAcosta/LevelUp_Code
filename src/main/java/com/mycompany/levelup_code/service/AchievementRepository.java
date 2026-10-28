package com.mycompany.levelup_code.service;

import com.mycompany.levelup_code.model.Achievement;
import com.mycompany.levelup_code.model.UserAchievement;
import com.mycompany.levelup_code.model.UserStats;
import java.util.List;
import java.util.Optional;

/** Contrato de consulta y registro de logros para la capa de negocio. */
public interface AchievementRepository {

    /** Obtiene las estadísticas actuales del usuario para evaluar criterios. */
    Optional<UserStats> findStatsByUserId(int userId);

    /** Devuelve los logros que el usuario todavía no ha desbloqueado. */
    List<Achievement> findNotUnlockedByUser(int userId);

    /**
     * Guarda el desbloqueo solo si aún no existe y devuelve el registro guardado.
     * La implementación debe realizar esta comprobación de forma atómica.
     */
    Optional<UserAchievement> saveIfNotUnlocked(UserAchievement userAchievement);

    /** Devuelve los logros ya desbloqueados por el usuario. */
    List<UserAchievement> findUnlockedByUser(int userId);
}
