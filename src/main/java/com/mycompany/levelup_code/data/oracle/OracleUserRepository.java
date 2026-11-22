package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.data.dao.UserRepository;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.exception.BusinessException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
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
        try (Connection connection = DatabaseConnection.getConnection();
             CallableStatement statement = connection.prepareCall(
                     "{call PKG_GAMIFICACION.SP_SINCRONIZAR_RACHA(?, ?, ?)}")) {
            statement.setInt(1, userId);
            statement.registerOutParameter(2, Types.NUMERIC);
            statement.registerOutParameter(3, Types.NUMERIC);
            statement.execute();
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
        try (Connection connection = DatabaseConnection.getConnection();
             CallableStatement statement = connection.prepareCall(
                     "{call PKG_GAMIFICACION.SP_RECUPERAR_VIDAS(?, ?)}")) {
            statement.setInt(1, userId);
            statement.registerOutParameter(2, Types.NUMERIC);
            statement.execute();
            return statement.getInt(2);
        } catch (SQLException ex) {
            throw new BusinessException("No se pudieron recuperar las vidas en Oracle: " + ex.getMessage(), ex);
        }
    }
}

