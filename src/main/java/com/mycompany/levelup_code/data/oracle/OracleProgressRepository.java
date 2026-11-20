package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.data.dao.ProgressRepository;
import com.mycompany.levelup_code.domain.enums.ProgressStatus;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.LessonProgress;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.exception.BusinessException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación Oracle JDBC de {@link ProgressRepository} sobre la tabla {@code Progreso_Leccion}.
 * Ejecuta la finalización de lecciones de forma atómica mediante {@code PKG_GAMIFICACION.SP_COMPLETAR_LECCION}.
 */
public class OracleProgressRepository implements ProgressRepository {

    private final UserDAO userDAO = new UserDAO();
    private final OracleContentRepository contentRepository = new OracleContentRepository();

    @Override
    public boolean managesGamificationTransactionally() {
        return true;
    }

    @Override
    public Optional<LessonProgress> findByUserAndLesson(int userId, int lessonId) {
        try (Connection conn = DatabaseConnection.getConnection();
             var stmt = OracleCalls.cursor(conn, "PKG_SEGUIMIENTO.progreso_leccion", userId, lessonId)) {
            try (ResultSet rs = OracleCalls.rows(stmt)) {
                if (rs.next()) {
                    User user = userDAO.findById(userId).orElse(null);
                    Lesson lesson = contentRepository.findLessonById(lessonId).orElse(null);
                    ProgressStatus status = mapStatusFromDb(rs.getString("estado"));
                    Timestamp ts = rs.getTimestamp("fecha_completado");
                    LessonProgress lp = new LessonProgress(
                            lessonId,
                            user,
                            lesson,
                            status,
                            ts != null ? ts.toLocalDateTime() : null
                    );
                    return Optional.of(lp);
                }
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al consultar progreso de lección en Oracle: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Map<Integer, ProgressStatus> findStatusesByUser(int userId) {
        Map<Integer, ProgressStatus> statuses = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             var stmt = OracleCalls.cursor(conn, "PKG_SEGUIMIENTO.estados_lecciones", userId)) {
            try (ResultSet rs = OracleCalls.rows(stmt)) {
                while (rs.next()) {
                    statuses.put(rs.getInt("id_leccion"), mapStatusFromDb(rs.getString("estado")));
                }
            }
            return statuses;
        } catch (SQLException e) {
            throw new BusinessException("Error al consultar progreso de lecciones en Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public LessonProgress saveOrUpdate(LessonProgress progress) {
        int userId = progress.getUser().getId();
        int lessonId = progress.getLesson().getId();

        if (progress.getStatus() == ProgressStatus.IN_PROGRESS) {
            String plsql = "{call PKG_GAMIFICACION.SP_INICIAR_LECCION(?, ?)}";
            try (Connection conn = DatabaseConnection.getConnection();
                 CallableStatement stmt = conn.prepareCall(plsql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, lessonId);
                stmt.execute();
            } catch (SQLException e) {
                throw new BusinessException("Error al iniciar la lección en Oracle.", e);
            }
            return findByUserAndLesson(userId, lessonId).orElse(progress);
        }

        if (progress.getStatus() == ProgressStatus.COMPLETED) {
            String plsql = "{call PKG_GAMIFICACION.SP_COMPLETAR_LECCION(?, ?, ?, ?)}";
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                try (CallableStatement cstmt = conn.prepareCall(plsql)) {
                    cstmt.setInt(1, userId);
                    cstmt.setInt(2, lessonId);
                    cstmt.registerOutParameter(3, Types.NUMERIC); // p_xp_otorgada
                    cstmt.registerOutParameter(4, Types.NUMERIC); // p_nuevo_nivel
                    cstmt.execute();

                    int xpOtorgada = cstmt.getInt(3);
                    int nuevoNivel = cstmt.getInt(4);

                    conn.commit();

                    if (progress.getUser() != null) {
                        if (xpOtorgada > 0) {
                            progress.getUser().addExperience(xpOtorgada);
                        }
                        progress.getUser().setLevel(nuevoNivel);
                        userDAO.findById(userId).ifPresent(fresh -> {
                            progress.getUser().setExperience(fresh.getExperience());
                            progress.getUser().setCurrentStreak(fresh.getCurrentStreak());
                            progress.getUser().setMaxStreak(fresh.getMaxStreak());
                        });
                    }
                    return progress;
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                throw new BusinessException(
                        "Error al completar lección en PKG_GAMIFICACION (verifica que 03_plsql_gamificacion.sql esté compilado): "
                        + e.getMessage(), e);
            }
        }

        String dbState = mapStatusToDb(progress.getStatus());
        Timestamp completedTs = progress.getApprovedAt() != null
                ? Timestamp.valueOf(progress.getApprovedAt())
                : null;

        try (Connection conn = DatabaseConnection.getConnection()) {
            OracleCalls.execute(conn, "PKG_SEGUIMIENTO.guardar_estado_leccion", userId, lessonId, dbState, completedTs);
            return progress;
        } catch (SQLException e) {
            throw new BusinessException("Error al guardar progreso de lección en Oracle: " + e.getMessage(), e);
        }
    }

    private ProgressStatus mapStatusFromDb(String dbState) {
        if ("COMPLETADA".equalsIgnoreCase(dbState)) {
            return ProgressStatus.COMPLETED;
        }
        if ("EN_CURSO".equalsIgnoreCase(dbState)) {
            return ProgressStatus.IN_PROGRESS;
        }
        return ProgressStatus.PENDING;
    }

    private String mapStatusToDb(ProgressStatus status) {
        if (status == null) {
            return "PENDIENTE";
        }
        switch (status) {
            case COMPLETED:
                return "COMPLETADA";
            case IN_PROGRESS:
                return "EN_CURSO";
            case PENDING:
            default:
                return "PENDIENTE";
        }
    }
}
