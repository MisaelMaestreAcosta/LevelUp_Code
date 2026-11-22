package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.data.dao.EnrollmentRepository;
import com.mycompany.levelup_code.exception.BusinessException;
import java.sql.Connection;
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
        try (Connection conn = DatabaseConnection.getConnection();
             var stmt = OracleCalls.cursor(conn, "PKG_USUARIOS.inscripciones_usuario", userId)) {
            try (ResultSet rs = OracleCalls.rows(stmt)) {
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
        try (Connection conn = DatabaseConnection.getConnection();
             var stmt = OracleCalls.cursor(conn, "PKG_USUARIOS.esta_inscrito", userId, languageId)) {
            try (ResultSet rs = OracleCalls.rows(stmt)) {
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
        try (Connection conn = DatabaseConnection.getConnection()) {
            OracleCalls.execute(conn, "PKG_USUARIOS.inscribir", userId, languageId);
        } catch (SQLException e) {
            throw new BusinessException("Error al registrar inscripción en Oracle: " + e.getMessage(), e);
        }
    }
}
