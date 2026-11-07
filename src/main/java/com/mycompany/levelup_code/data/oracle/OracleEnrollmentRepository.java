package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.data.dao.EnrollmentRepository;
import com.mycompany.levelup_code.exception.BusinessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Implementación Oracle JDBC de {@link EnrollmentRepository} sobre la tabla {@code Inscripciones}.
 */
public class OracleEnrollmentRepository implements EnrollmentRepository {

    @Override
    public Set<Integer> findEnrolledLanguageIds(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("El identificador de usuario debe ser válido.");
        }
        Set<Integer> ids = new LinkedHashSet<>();
        String sql = "SELECT id_lenguaje FROM Inscripciones WHERE id_usuario = ? ORDER BY fecha_inscripcion ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id_lenguaje"));
                }
            }
            return ids;
        } catch (SQLException e) {
            throw new BusinessException("Error al consultar inscripciones del usuario en Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isEnrolled(int userId, int languageId) {
        if (userId <= 0 || languageId <= 0) {
            return false;
        }
        String sql = "SELECT 1 FROM Inscripciones WHERE id_usuario = ? AND id_lenguaje = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, languageId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al verificar inscripción en Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public void enroll(int userId, int languageId) {
        if (userId <= 0 || languageId <= 0) {
            throw new IllegalArgumentException("El usuario y el lenguaje deben ser válidos para inscribirse.");
        }
        String mergeSql = "MERGE INTO Inscripciones dst "
                + "USING (SELECT ? AS id_usuario, ? AS id_lenguaje FROM dual) src "
                + "ON (dst.id_usuario = src.id_usuario AND dst.id_lenguaje = src.id_lenguaje) "
                + "WHEN NOT MATCHED THEN "
                + "INSERT (id_usuario, id_lenguaje, fecha_inscripcion) VALUES (?, ?, SYSDATE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(mergeSql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, languageId);
            stmt.setInt(3, userId);
            stmt.setInt(4, languageId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new BusinessException("Error al registrar inscripción en Oracle: " + e.getMessage(), e);
        }
    }
}
