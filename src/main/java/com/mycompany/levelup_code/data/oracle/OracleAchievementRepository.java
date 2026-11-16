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
import java.sql.PreparedStatement;
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

    private static final String SELECT_ACHIEVEMENT_SQL =
            "SELECT l.id_logro, l.nombre_insignia, l.descripcion, l.tipo_condicion, "
            + "lr.dias_requeridos, le.exp_requerida, lc.retos_requeridos "
            + "FROM Logros l "
            + "LEFT JOIN Logro_Racha lr ON lr.id_logro = l.id_logro "
            + "LEFT JOIN Logro_EXP le   ON le.id_logro = l.id_logro "
            + "LEFT JOIN Logro_Retos lc ON lc.id_logro = l.id_logro ";

    private final UserDAO userDAO = new UserDAO();

    @Override
    public Optional<UserStats> findStatsByUserId(int userId) {
        String sql = "SELECT u.racha_actual, u.racha_maxima, u.exp_total, "
                + "(SELECT COUNT(DISTINCT i.id_reto) FROM Intentos i WHERE i.id_usuario = u.id_usuario AND i.es_correcto = 1) AS retos_aprobados "
                + "FROM Usuarios u WHERE u.id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
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
        String sql = SELECT_ACHIEVEMENT_SQL
                + "WHERE NOT EXISTS ("
                + "  SELECT 1 FROM Usuario_Logro ul WHERE ul.id_usuario = ? AND ul.id_logro = l.id_logro"
                + ") ORDER BY l.id_logro ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
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

        String sql = "INSERT INTO Usuario_Logro (id_usuario, id_logro, fecha_desbloqueado) "
                + "SELECT ?, ?, ? FROM dual "
                + "WHERE NOT EXISTS ("
                + "  SELECT 1 FROM Usuario_Logro WHERE id_usuario = ? AND id_logro = ?"
                + ")";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, achId);
            stmt.setTimestamp(3, Timestamp.valueOf(unlockedAt));
            stmt.setInt(4, userId);
            stmt.setInt(5, achId);
            int affected = stmt.executeUpdate();
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
        String sql = "SELECT ul.id_usuario, ul.id_logro, ul.fecha_desbloqueado, "
                + "l.nombre_insignia, l.descripcion, l.tipo_condicion, "
                + "lr.dias_requeridos, le.exp_requerida, lc.retos_requeridos "
                + "FROM Usuario_Logro ul "
                + "JOIN Logros l ON l.id_logro = ul.id_logro "
                + "LEFT JOIN Logro_Racha lr ON lr.id_logro = l.id_logro "
                + "LEFT JOIN Logro_EXP le   ON le.id_logro = l.id_logro "
                + "LEFT JOIN Logro_Retos lc ON lc.id_logro = l.id_logro "
                + "WHERE ul.id_usuario = ? ORDER BY ul.fecha_desbloqueado DESC";

        User user = userDAO.findById(userId).orElse(null);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
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
        String sql = SELECT_ACHIEVEMENT_SQL + "ORDER BY l.id_logro ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
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

