package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.data.dao.UserRepository;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.exception.BusinessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Adaptador de persistencia que implementa {@link UserRepository} delegando
 * las operaciones JDBC sobre la tabla {@code Usuarios} a {@link UserDAO}.
 */
public class OracleUserRepository implements UserRepository {

    private final UserDAO userDAO;

    public OracleUserRepository() {
        this(new UserDAO());
    }

    public OracleUserRepository(UserDAO userDAO) {
        this.userDAO = userDAO != null ? userDAO : new UserDAO();
    }

    @Override
    public Optional<User> findById(int id) {
        return userDAO.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    @Override
    public Optional<User> refreshStreakStats(int userId) {
        String streakSql = "WITH dias AS ("
                + "SELECT DISTINCT TRUNC(fecha) AS dia FROM Actividad_Diaria WHERE id_usuario=?), "
                + "numerados AS (SELECT dia, dia-ROW_NUMBER() OVER (ORDER BY dia) AS grupo FROM dias), "
                + "rachas AS (SELECT MAX(dia) AS fin, COUNT(*) AS longitud FROM numerados GROUP BY grupo) "
                + "SELECT NVL(MAX(CASE WHEN fin IN (TRUNC(SYSDATE), TRUNC(SYSDATE)-1) "
                + "THEN longitud ELSE 0 END),0), NVL(MAX(longitud),0) FROM rachas";
        String updateSql = "UPDATE Usuarios SET racha_actual=?, racha_maxima=? WHERE id_usuario=? "
                + "AND (racha_actual<>? OR racha_maxima<>?)";
        try (Connection connection = DatabaseConnection.getConnection();
             var streaks = connection.prepareStatement(streakSql);
             var update = connection.prepareStatement(updateSql)) {
            streaks.setInt(1, userId);
            try (var result = streaks.executeQuery()) {
                if (result.next()) {
                    int currentStreak = result.getInt(1);
                    int maxStreak = result.getInt(2);
                    update.setInt(1, currentStreak);
                    update.setInt(2, maxStreak);
                    update.setInt(3, userId);
                    update.setInt(4, currentStreak);
                    update.setInt(5, maxStreak);
                    update.executeUpdate();
                }
            }
            return userDAO.findById(userId);
        } catch (SQLException ex) {
            throw new BusinessException("No se pudo recalcular la racha desde la actividad real.", ex);
        }
    }

    @Override
    public User save(User user) {
        return userDAO.save(user);
    }

    @Override
    public User update(User user) {
        userDAO.update(user);
        return user;
    }

    @Override
    public void updateProfile(int userId, String firstName, String lastName) {
        try (Connection connection = DatabaseConnection.getConnection();
             var statement = connection.prepareStatement(
                     "UPDATE Usuarios SET primer_nombre=?,primer_apellido=? WHERE id_usuario=?")) {
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setInt(3, userId);
            if (statement.executeUpdate() != 1) throw new SQLException("La cuenta no existe.");
        } catch (SQLException ex) { throw new BusinessException("No se pudo guardar el perfil.", ex); }
    }

    @Override
    public boolean compareAndSetPassword(int userId, String expectedHash, String replacementHash) {
        try (Connection connection = DatabaseConnection.getConnection();
             var statement = connection.prepareStatement(
                     "UPDATE Usuarios SET password_hash=? WHERE id_usuario=? AND password_hash=?")) {
            statement.setString(1, replacementHash);
            statement.setInt(2, userId);
            statement.setString(3, expectedHash);
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) { throw new BusinessException("No se pudo cambiar la contraseña.", ex); }
    }

    @Override
    public int recoverLivesIfEligible(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            String refreshSql = "UPDATE Usuarios u SET vidas=5 WHERE u.id_usuario=? AND u.vidas<5 "
                    + "AND NOT EXISTS (SELECT 1 FROM Intentos i WHERE i.id_usuario=u.id_usuario "
                    + "AND i.fecha_intento>=TRUNC(SYSDATE))";
            try (var refresh = conn.prepareStatement(refreshSql);
                 var select = conn.prepareStatement("SELECT vidas FROM Usuarios WHERE id_usuario=?")) {
                refresh.setInt(1, userId);
                refresh.executeUpdate();
                select.setInt(1, userId);
                try (var result = select.executeQuery()) {
                    if (!result.next()) throw new SQLException("Usuario #" + userId + " no encontrado.");
                    int restored = result.getInt(1);
                    conn.commit();
                    return restored;
                }
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new BusinessException("No se pudieron recuperar las vidas en Oracle: " + ex.getMessage(), ex);
        }
    }
}

