package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.data.dao.AchievementRepository;
import com.mycompany.levelup_code.domain.model.Achievement;
import com.mycompany.levelup_code.domain.model.ChallengeCountAchievement;
import com.mycompany.levelup_code.domain.model.ExperienceAchievement;
import com.mycompany.levelup_code.domain.model.StreakAchievement;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.domain.model.UserAchievement;
import com.mycompany.levelup_code.domain.model.UserStats;
import com.mycompany.levelup_code.exception.BusinessException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación Oracle JDBC de {@link AchievementRepository} sobre las tablas
 * especializadas {@code Logros}, {@code Logro_Racha}, {@code Logro_EXP},
 * {@code Logro_Retos} y {@code Usuario_Logro}.
 */
public class OracleAchievementRepository implements AchievementRepository {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public Optional<UserStats> findStatsByUserId(int userId) {
        try (Connection conn = DatabaseConnection.getConnection();
             var stmt = OracleCalls.cursor(conn, "PKG_SEGUIMIENTO.estadisticas_logros", userId)) {
            try (ResultSet rs = OracleCalls.rows(stmt)) {
                if (rs.next()) {
                    return Optional.of(new UserStats(
                            rs.getInt("racha_actual"),
                            rs.getInt("racha_maxima"),
                            rs.getInt("exp_total"),
                            rs.getInt("retos_aprobados")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al consultar estadísticas de logros en Oracle: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Achievement> findNotUnlockedByUser(int userId) {
        List<Achievement> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             var stmt = OracleCalls.cursor(conn, "PKG_SEGUIMIENTO.logros_pendientes", userId)) {
            try (ResultSet rs = OracleCalls.rows(stmt)) {
                while (rs.next()) {
                    list.add(mapAchievement(rs));
                }
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al consultar logros pendientes en Oracle: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Optional<UserAchievement> saveIfNotUnlocked(UserAchievement userAchievement) {
        int userId = userAchievement.getUser().getId();
        int achId = userAchievement.getAchievement().getId();
        LocalDateTime unlockedAt = userAchievement.getUnlockedAt() != null
                ? userAchievement.getUnlockedAt()
                : LocalDateTime.now();

        try (Connection conn = DatabaseConnection.getConnection()) {
            int affected = OracleCalls.number(conn, "PKG_SEGUIMIENTO.otorgar_logro", userId, achId, Timestamp.valueOf(unlockedAt));
            if (affected > 0) {
                return Optional.of(userAchievement);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new BusinessException("Error al desbloquear logro en Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public List<UserAchievement> findUnlockedByUser(int userId) {
        List<UserAchievement> list = new ArrayList<>();
        User user = userDAO.findById(userId).orElse(null);
        try (Connection conn = DatabaseConnection.getConnection();
             var stmt = OracleCalls.cursor(conn, "PKG_SEGUIMIENTO.logros_obtenidos", userId)) {
            try (ResultSet rs = OracleCalls.rows(stmt)) {
                while (rs.next()) {
                    Achievement ach = mapAchievement(rs);
                    Timestamp ts = rs.getTimestamp("fecha_desbloqueado");
                    list.add(new UserAchievement(
                            ach.getId(),
                            user,
                            ach,
                            ts != null ? ts.toLocalDateTime() : LocalDateTime.now()
                    ));
                }
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al listar logros desbloqueados en Oracle: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Achievement> findAllCatalog() {
        List<Achievement> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             var stmt = OracleCalls.cursor(conn, "PKG_SEGUIMIENTO.catalogo_logros");
             ResultSet rs = OracleCalls.rows(stmt)) {
            while (rs.next()) {
                list.add(mapAchievement(rs));
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al listar catálogo de logros en Oracle: " + e.getMessage(), e);
        }
        return list;
    }

    private Achievement mapAchievement(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_logro");
        String name = rs.getString("nombre_insignia");
        String desc = rs.getString("descripcion");
        String type = rs.getString("tipo_condicion");

        if ("RACHA".equalsIgnoreCase(type)) {
            return new StreakAchievement(id, name, desc, "FLAME", rs.getInt("dias_requeridos"));
        }
        if ("EXP".equalsIgnoreCase(type)) {
            return new ExperienceAchievement(id, name, desc, "BOLT", rs.getInt("exp_requerida"));
        }
        return new ChallengeCountAchievement(id, name, desc, "CODE", rs.getInt("retos_requeridos"));
    }
}

