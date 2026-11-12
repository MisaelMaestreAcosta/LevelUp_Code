package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.domain.model.*;
import java.sql.*;
import java.util.UUID;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OracleContentVersionIntegrationTest {
    @Test void snapshotsKeepOriginalAttemptContentAndRejectArchivedSubmissions() throws Exception {
        Assumptions.assumeTrue(DatabaseConnection.isDatabaseReady(), "Oracle local no disponible.");
        try (Connection connection = DatabaseConnection.getConnection()) {
            try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(
                    "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME IN ('VERSION_RETO','VERSION_CASO_PRUEBA','RETO_PREDICCION','RETO_ORDENAMIENTO')")) {
                result.next();
                Assumptions.assumeTrue(result.getInt(1) == 4, "Instala las migraciones 08, 09 y 10 para probar el versionado en Oracle.");
            }
            connection.setAutoCommit(false);
            try {
                int lessonId;
                int userId;
                try (Statement statement = connection.createStatement();
                     ResultSet result = statement.executeQuery("SELECT (SELECT MIN(id_leccion) FROM Lecciones), (SELECT MIN(id_usuario) FROM Usuarios) FROM dual")) {
                    result.next(); lessonId = result.getInt(1); userId = result.getInt(2);
                }
                Assumptions.assumeTrue(lessonId > 0 && userId > 0, "Se necesitan una cuenta y una lección de prueba.");
                Lesson lesson = new Lesson(); lesson.setId(lessonId);
                CodeChallenge challenge = new CodeChallenge(0, "Original integración", "Enunciado inicial", lesson, 0,
                        "public class Solver { public static String solve(String s) { return s; } }", 2);
                challenge.addTestCase(new TestCase(0, challenge, "", "", false));
                OracleContentRepository repository = new OracleContentRepository();
                repository.saveChallenge(connection, challenge);
                OracleChallengeVersionRepository.snapshot(connection, challenge.getId(), null);
                insertAttempt(connection, userId, challenge.getId());
                challenge.setDescription("Enunciado editado");
                repository.updateChallenge(connection, challenge);
                try (PreparedStatement statement = connection.prepareStatement("UPDATE Retos SET revision=2 WHERE id_reto=?")) {
                    statement.setInt(1, challenge.getId()); statement.executeUpdate();
                }
                OracleChallengeVersionRepository.snapshot(connection, challenge.getId(), null);
                try (PreparedStatement statement = connection.prepareStatement("SELECT v.revision,v.enunciado FROM Intentos i "
                        + "JOIN Version_Reto v ON v.id_version=i.id_version_reto WHERE i.id_reto=?")) {
                    statement.setInt(1, challenge.getId());
                    try (ResultSet result = statement.executeQuery()) {
                        assertTrue(result.next()); assertEquals(1, result.getInt(1));
                        assertEquals("Enunciado inicial", result.getString(2));
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement("UPDATE Retos SET estado='ARCHIVADO' WHERE id_reto=?")) {
                    statement.setInt(1, challenge.getId()); statement.executeUpdate();
                }
                assertThrows(SQLException.class, () -> insertAttempt(connection, userId, challenge.getId()));
            } finally { connection.rollback(); }
        }
    }

    @Test void preservesEmptyInputAndOutputAsNonNullClobs() throws Exception {
        Assumptions.assumeTrue(DatabaseConnection.isDatabaseReady(), "Oracle local no disponible.");
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int lessonId;
                try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("SELECT MIN(id_leccion) FROM Lecciones")) {
                    result.next(); lessonId = result.getInt(1);
                }
                Assumptions.assumeTrue(lessonId > 0, "Se necesita una lección de prueba.");
                Lesson lesson = new Lesson(); lesson.setId(lessonId);
                CodeChallenge challenge = new CodeChallenge(0, "Caso vacío integración", "Acepta entrada vacía", lesson, 0, "class Solver {}", 2);
                challenge.addTestCase(new TestCase(0, challenge, "", "", false));
                new OracleContentRepository().saveChallenge(connection, challenge);
                try (PreparedStatement statement = connection.prepareStatement("SELECT DBMS_LOB.GETLENGTH(entrada),DBMS_LOB.GETLENGTH(salida_esperada) FROM Casos_Prueba WHERE id_reto=?")) {
                    statement.setInt(1, challenge.getId());
                    try (ResultSet result = statement.executeQuery()) {
                        assertTrue(result.next()); assertEquals(0, result.getInt(1)); assertFalse(result.wasNull());
                        assertEquals(0, result.getInt(2)); assertFalse(result.wasNull());
                    }
                }
            } finally { connection.rollback(); }
        }
    }

    private void insertAttempt(Connection connection, int userId, int challengeId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO Intentos(id_usuario,id_reto,codigo_enviado,id_solicitud) VALUES(?,?,?,?)")) {
            statement.setInt(1, userId); statement.setInt(2, challengeId);
            statement.setString(3, "Respuesta de integración"); statement.setString(4, UUID.randomUUID().toString());
            statement.executeUpdate();
        }
    }
}
