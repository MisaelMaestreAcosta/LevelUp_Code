package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.data.dao.GenericDAO;
import com.mycompany.levelup_code.domain.model.Role;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.exception.BusinessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Implementación Oracle JDBC para la entidad {@link User} sobre las tablas
 * {@code Usuarios} y {@code Roles} del Diccionario de Datos.
 */
public class UserDAO implements GenericDAO<User, Integer> {

    private static final String SELECT_BASE =
            "SELECT u.id_usuario, u.id_rol, u.primer_nombre, u.segundo_nombre, "
            + "u.primer_apellido, u.segundo_apellido, u.correo_electronico, u.password_hash, "
            + "u.exp_total, u.nivel, u.racha_actual, u.racha_maxima, u.vidas, "
            + "u.fecha_registro, u.ultima_actividad, r.nombre_rol, r.descripcion AS rol_descripcion "
            + "FROM Usuarios u "
            + "JOIN Roles r ON r.id_rol = u.id_rol ";

    @Override
    public User insert(User user) {
        String sql = "INSERT INTO Usuarios ("
                + "id_rol, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, "
                + "correo_electronico, password_hash, exp_total, nivel, racha_actual, racha_maxima, "
                + "vidas, fecha_registro, ultima_actividad"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, SYSTIMESTAMP)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID_USUARIO"})) {

            int roleId = (user.getRole() != null && user.getRole().getId() > 0) ? user.getRole().getId() : 1;
            stmt.setInt(1, roleId);
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getMiddleName());
            stmt.setString(4, user.getLastName());
            stmt.setString(5, user.getSecondLastName());
            stmt.setString(6, user.getEmail().trim().toLowerCase(Locale.ROOT));
            stmt.setString(7, user.getPasswordHash());
            stmt.setInt(8, user.getTotalExp());
            stmt.setInt(9, Math.max(1, user.getLevel()));
            stmt.setInt(10, user.getCurrentStreak());
            stmt.setInt(11, user.getMaxStreak());
            stmt.setInt(12, user.getLives() > 0 ? user.getLives() : 5);

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new BusinessException("Error al registrar el usuario en Oracle: " + e.getMessage(), e);
        }
    }

    public User save(User user) {
        if (user.getId() > 0) {
            update(user);
            return user;
        }
        return insert(user);
    }

    @Override
    public Optional<User> findById(Integer id) {
        String sql = SELECT_BASE + "WHERE u.id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al consultar usuario por ID en Oracle: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        String sql = SELECT_BASE + "WHERE LOWER(u.correo_electronico) = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim().toLowerCase(Locale.ROOT));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al buscar usuario por correo en Oracle: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY u.exp_total DESC, u.id_usuario ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al listar usuarios en Oracle: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE Usuarios SET "
                + "primer_nombre = ?, segundo_nombre = ?, primer_apellido = ?, segundo_apellido = ?, "
                + "exp_total = ?, nivel = ?, racha_actual = ?, racha_maxima = ?, vidas = ?, "
                + "ultima_actividad = SYSTIMESTAMP "
                + "WHERE id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getMiddleName());
            stmt.setString(3, user.getLastName());
            stmt.setString(4, user.getSecondLastName());
            stmt.setInt(5, user.getTotalExp());
            stmt.setInt(6, Math.max(1, user.getLevel()));
            stmt.setInt(7, user.getCurrentStreak());
            stmt.setInt(8, user.getMaxStreak());
            stmt.setInt(9, user.getLives());
            stmt.setInt(10, user.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new BusinessException("Error al actualizar progreso del usuario en Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM Usuarios WHERE id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new BusinessException("Error al eliminar usuario en Oracle: " + e.getMessage(), e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id_usuario"));
        u.setFirstName(rs.getString("primer_nombre"));
        u.setMiddleName(rs.getString("segundo_nombre"));
        u.setLastName(rs.getString("primer_apellido"));
        u.setSecondLastName(rs.getString("segundo_apellido"));

        StringBuilder fullName = new StringBuilder();
        if (u.getFirstName() != null) {
            fullName.append(u.getFirstName());
        }
        if (u.getLastName() != null && !"Usuario".equalsIgnoreCase(u.getLastName())
                && !"Demo".equalsIgnoreCase(u.getLastName())
                && !"Sistema".equalsIgnoreCase(u.getLastName())) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(u.getLastName());
        }
        u.setUsername(fullName.toString());

        u.setEmail(rs.getString("correo_electronico"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setTotalExp(rs.getInt("exp_total"));
        u.setLevel(rs.getInt("nivel"));
        u.setCurrentStreak(rs.getInt("racha_actual"));
        u.setMaxStreak(rs.getInt("racha_maxima"));
        u.setLives(rs.getInt("vidas"));

        Timestamp reg = rs.getTimestamp("fecha_registro");
        if (reg != null) {
            u.setRegistrationDate(reg.toLocalDateTime());
        }
        Timestamp last = rs.getTimestamp("ultima_actividad");
        if (last != null) {
            u.setLastActivity(last.toLocalDateTime());
        }

        Role role = new Role(
                rs.getInt("id_rol"),
                rs.getString("nombre_rol"),
                rs.getString("rol_descripcion")
        );
        u.setRole(role);
        return u;
    }
}
