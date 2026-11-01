package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.data.dao.ProgressRepository;
import com.mycompany.levelup_code.domain.enums.ProgressStatus;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.LessonProgress;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.exception.BusinessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Optional;

/**
 * Implementación Oracle JDBC de {@link ProgressRepository} sobre la tabla {@code Progreso_Leccion}.
 */
public class OracleProgressRepository implements ProgressRepository {

    private final UserDAO userDAO = new UserDAO();
    private final OracleContentRepository contentRepository = new OracleContentRepository();

    @Override
    public Optional<LessonProgress> findByUserAndLesson(int userId, int lessonId) {
        String sql = "SELECT id_usuario, id_leccion, estado, fecha_completado "
                + "FROM Progreso_Leccion WHERE id_usuario = ? AND id_leccion = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, lessonId);
            try (ResultSet rs = stmt.executeQuery()) {
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
    public LessonProgress saveOrUpdate(LessonProgress progress) {
        int userId = progress.getUser().getId();
        int lessonId = progress.getLesson().getId();
        String dbState = mapStatusToDb(progress.getStatus());
        Timestamp completedTs = progress.getApprovedAt() != null
                ? Timestamp.valueOf(progress.getApprovedAt())
                : null;

        String mergeSql = "MERGE INTO Progreso_Leccion dst "
                + "USING (SELECT ? AS id_usuario, ? AS id_leccion FROM dual) src "
                + "ON (dst.id_usuario = src.id_usuario AND dst.id_leccion = src.id_leccion) "
                + "WHEN MATCHED THEN UPDATE SET dst.estado = ?, dst.fecha_completado = ? "
                + "WHEN NOT MATCHED THEN INSERT (id_usuario, id_leccion, estado, fecha_completado) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(mergeSql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, lessonId);
            stmt.setString(3, dbState);
            if (completedTs != null) {
                stmt.setTimestamp(4, completedTs);
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }
            stmt.setInt(5, userId);
            stmt.setInt(6, lessonId);
            stmt.setString(7, dbState);
            if (completedTs != null) {
                stmt.setTimestamp(8, completedTs);
            } else {
                stmt.setNull(8, Types.TIMESTAMP);
            }
            stmt.executeUpdate();
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
        if (status == ProgressStatus.COMPLETED) {
            return "COMPLETADA";
        }
        if (status == ProgressStatus.IN_PROGRESS) {
            return "EN_CURSO";
        }
        return "PENDIENTE";
    }
}
