package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.data.dao.ActivityRepository;
import com.mycompany.levelup_code.domain.model.DailyActivity;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.exception.BusinessException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación Oracle JDBC de {@link ActivityRepository} sobre la tabla {@code Actividad_Diaria}.
 */
public class OracleActivityRepository implements ActivityRepository {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public Optional<DailyActivity> findByUserAndDate(int userId, LocalDate date) {
        String sql = "SELECT id_actividad, id_usuario, fecha, exp_ganada "
                + "FROM Actividad_Diaria WHERE id_usuario = ? AND TRUNC(fecha) = TRUNC(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = userDAO.findById(userId).orElse(null);
                    Date dbDate = rs.getDate("fecha");
                    DailyActivity activity = new DailyActivity(
                            rs.getInt("id_actividad"),
                            user,
                            dbDate != null ? dbDate.toLocalDate() : date,
                            rs.getInt("exp_ganada")
                    );
                    return Optional.of(activity);
                }
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al consultar actividad diaria en Oracle: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public DailyActivity saveOrUpdate(DailyActivity activity) {
        int userId = activity.getUser().getId();
        LocalDate date = activity.getActivityDate() != null ? activity.getActivityDate() : LocalDate.now();
        int exp = Math.max(0, activity.getExperienceGained());

        String mergeSql = "MERGE INTO Actividad_Diaria dst "
                + "USING (SELECT ? AS id_usuario, TRUNC(?) AS fecha FROM dual) src "
                + "ON (dst.id_usuario = src.id_usuario AND TRUNC(dst.fecha) = src.fecha) "
                + "WHEN MATCHED THEN UPDATE SET dst.exp_ganada = ? "
                + "WHEN NOT MATCHED THEN INSERT (id_usuario, fecha, exp_ganada) "
                + "VALUES (?, TRUNC(?), ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(mergeSql)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setInt(3, exp);
            stmt.setInt(4, userId);
            stmt.setDate(5, Date.valueOf(date));
            stmt.setInt(6, exp);
            stmt.executeUpdate();
            return activity;
        } catch (SQLException e) {
            throw new BusinessException("Error al registrar actividad diaria en Oracle: " + e.getMessage(), e);
        }
    }

    public List<DailyActivity> findRecentByUser(int userId, int days) {
        List<DailyActivity> result = new ArrayList<>();
        String sql = "SELECT id_actividad, id_usuario, fecha, exp_ganada "
                + "FROM Actividad_Diaria WHERE id_usuario = ? AND fecha >= TRUNC(SYSDATE) - ? "
                + "ORDER BY fecha ASC";
        User user = userDAO.findById(userId).orElse(null);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, Math.max(1, days));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date d = rs.getDate("fecha");
                    result.add(new DailyActivity(
                            rs.getInt("id_actividad"),
                            user,
                            d != null ? d.toLocalDate() : LocalDate.now(),
                            rs.getInt("exp_ganada")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al listar actividad reciente en Oracle: " + e.getMessage(), e);
        }
        return result;
    }
}
