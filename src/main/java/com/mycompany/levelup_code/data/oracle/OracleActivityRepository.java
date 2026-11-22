package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.data.dao.ActivityRepository;
import com.mycompany.levelup_code.domain.model.DailyActivity;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.exception.BusinessException;
import java.sql.Connection;
import java.sql.Date;
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
        try (Connection conn = DatabaseConnection.getConnection();
             var stmt = OracleCalls.cursor(conn,
                     "PKG_SEGUIMIENTO.actividad_en_fecha", userId, Date.valueOf(date))) {
            try (ResultSet rs = OracleCalls.rows(stmt)) {
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

        try (Connection conn = DatabaseConnection.getConnection()) {
            OracleCalls.execute(conn, "PKG_SEGUIMIENTO.guardar_actividad", userId, Date.valueOf(date), exp);
            return activity;
        } catch (SQLException e) {
            throw new BusinessException("Error al registrar actividad diaria en Oracle: " + e.getMessage(), e);
        }
    }

    public List<DailyActivity> findRecentByUser(int userId, int days) {
        List<DailyActivity> result = new ArrayList<>();
        User user = userDAO.findById(userId).orElse(null);
        try (Connection conn = DatabaseConnection.getConnection();
             var stmt = OracleCalls.cursor(conn,
                     "PKG_SEGUIMIENTO.actividad_reciente", userId, Math.max(1, days))) {
            try (ResultSet rs = OracleCalls.rows(stmt)) {
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

