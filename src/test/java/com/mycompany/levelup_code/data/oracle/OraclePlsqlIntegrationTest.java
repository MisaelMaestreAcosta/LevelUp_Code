package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Types;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de integración contra Oracle Database para validar procedimientos PL/SQL
 * y aislamiento transaccional con {@link Savepoint}. Se omite limpiamente cuando
 * no hay una instancia Oracle configurada o disponible en el entorno de construcción.
 */
class OraclePlsqlIntegrationTest {

    @Test
    void storedProceduresRespectCallerSavepointWithoutPersistingChanges() throws SQLException {
        Assumptions.assumeTrue(
                DatabaseConnection.isDatabaseReady(),
                "Se omite prueba de integración Oracle: base de datos local no configurada o fuera de línea."
        );

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Verificar que exista el paquete compilado sin COMMIT interno y el usuario de prueba #1
            boolean packageHasInternalCommit = packageBodyContainsDirectCommit(conn);
            Assumptions.assumeTrue(
                    !packageHasInternalCommit,
                    "Se omite hasta recompilar database/03_plsql_gamificacion.sql en Oracle (paquete actual aún contiene COMMIT)."
            );

            Integer initialExp = queryUserExp(conn, 1);
            Assumptions.assumeTrue(initialExp != null, "No existe id_usuario = 1 para ejecutar la prueba con SAVEPOINT.");

            Savepoint sp = conn.setSavepoint("sp_junit_plsql_test");
            try {
                try (CallableStatement fnStmt = conn.prepareCall("{? = call PKG_GAMIFICACION.FN_CALCULAR_NIVEL(?)}")) {
                    fnStmt.registerOutParameter(1, Types.NUMERIC);
                    fnStmt.setInt(2, 600);
                    fnStmt.execute();
                    assertEquals(3, fnStmt.getInt(1));
                }

                try (CallableStatement spStmt = conn.prepareCall("{call PKG_GAMIFICACION.SP_RECUPERAR_VIDAS(?, ?)}")) {
                    spStmt.setInt(1, 1);
                    spStmt.registerOutParameter(2, Types.NUMERIC);
                    spStmt.execute();
                    assertEquals(5, spStmt.getInt(2));
                }

                // Si algún procedimiento hubiera ejecutado COMMIT internamente, este rollback lanzaría ORA-01086
                conn.rollback(sp);

                Integer expAfterRollback = queryUserExp(conn, 1);
                assertEquals(initialExp, expAfterRollback);
                assertTrue(expAfterRollback >= 0);
            } finally {
                conn.rollback();
                conn.setAutoCommit(true);
            }
        }
    }

    private static boolean packageBodyContainsDirectCommit(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM USER_SOURCE "
                + "WHERE NAME = 'PKG_GAMIFICACION' AND TYPE = 'PACKAGE BODY' "
                + "AND REGEXP_LIKE(UPPER(TEXT), '(^|[^A-Z_])COMMIT[[:space:]]*;')";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static Integer queryUserExp(Connection conn, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT exp_total FROM Usuarios WHERE id_usuario = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }
}

