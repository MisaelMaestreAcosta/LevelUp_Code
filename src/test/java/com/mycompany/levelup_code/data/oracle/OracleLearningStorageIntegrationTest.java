package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.domain.model.EditorPreferences;
import com.mycompany.levelup_code.domain.model.LearningGoal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Exercises the repositories' own connections using disposable accounts. */
class OracleLearningStorageIntegrationTest {
    private final String email = "learning-storage-" + UUID.randomUUID() + "@example.invalid";
    private final String otherEmail = "learning-storage-" + UUID.randomUUID() + "@example.invalid";
    private int userId;
    private int otherUserId;

    @BeforeEach
    void createDisposableAccounts() throws SQLException {
        Assumptions.assumeTrue(DatabaseConnection.isDatabaseReady(), "Oracle local no disponible.");
        try (Connection connection = DatabaseConnection.getConnection()) {
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM user_tables WHERE table_name IN "
                         + "('RETO_FAVORITO','NOTA_ESTUDIO','META_USUARIO','PREFERENCIA_USUARIO')")) {
                result.next();
                Assumptions.assumeTrue(result.getInt(1) == 4, "Instala las migraciones 05, 06 y 07.");
            }
            connection.setAutoCommit(false);
            try {
                userId = insertUser(connection, email);
                otherUserId = insertUser(connection, otherEmail);
                connection.commit();
            } finally {
                connection.rollback();
            }
        }
    }

    @AfterEach
    void removeDisposableAccountsAndTheirData() throws SQLException {
        if (userId == 0 && otherUserId == 0) {
            return;
        }
        // Foreign keys cascade only these accounts' notes, favorites, goals and activity.
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM Usuarios WHERE correo_electronico IN (?,?)")) {
            statement.setString(1, email);
            statement.setString(2, otherEmail);
            statement.executeUpdate();
        }
    }

    @Test
    void notesAndFavoritesPersistAndStayPrivate() throws SQLException {
        int challengeId = firstId("SELECT MIN(id_reto) FROM Retos");
        OracleNotebookRepository repository = new OracleNotebookRepository();
        String content = "Apuntes de programación\nSystem.out.println(\"¡Hola!\");\n".repeat(300);

        repository.save(userId, new OracleNotebookRepository.Note(0, challengeId, "  Mi primera nota  ", content));
        var notebook = repository.load(userId);
        assertEquals(1, notebook.notes().size());
        var note = notebook.notes().getFirst();
        assertTrue(note.id() > 0);
        assertEquals("Mi primera nota", note.title());
        assertEquals(content, note.content());
        assertEquals(Integer.valueOf(challengeId), note.challengeId());
        assertTrue(repository.load(otherUserId).notes().isEmpty());

        assertThrows(SQLException.class, () -> repository.save(otherUserId,
                new OracleNotebookRepository.Note(note.id(), null, "Otra cuenta", "Contenido")));
        repository.delete(otherUserId, note.id());
        assertEquals(note, repository.load(userId).notes().getFirst());

        repository.save(userId, new OracleNotebookRepository.Note(note.id(), null, "Nota editada", "Contenido nuevo"));
        var updated = repository.load(userId).notes().getFirst();
        assertEquals("Nota editada", updated.title());
        assertEquals("Contenido nuevo", updated.content());
        assertNull(updated.challengeId());

        repository.favorite(userId, challengeId, true);
        repository.favorite(userId, challengeId, true);
        assertEquals(Set.of(challengeId), repository.load(userId).favorites());
        assertTrue(repository.load(otherUserId).favorites().isEmpty());
        repository.favorite(otherUserId, challengeId, true);
        repository.favorite(userId, challengeId, false);
        assertTrue(repository.load(userId).favorites().isEmpty());
        assertEquals(Set.of(challengeId), repository.load(otherUserId).favorites());

        repository.delete(userId, note.id());
        assertTrue(repository.load(userId).notes().isEmpty());
    }

    @Test
    void goalsReadActivityWithinTheirPeriodAndStayPrivate() throws SQLException {
        int challengeId = firstId("SELECT MIN(r.id_reto) FROM Retos r JOIN Version_Reto v "
                + "ON v.id_reto=r.id_reto AND v.revision=r.revision WHERE r.estado='PUBLICADO'");
        int lessonId = firstId("SELECT MIN(id_leccion) FROM Lecciones");
        OracleGoalRepository repository = new OracleGoalRepository();
        for (LearningGoal.Type type : LearningGoal.Type.values()) {
            repository.create(userId, type, type == LearningGoal.Type.XP ? 100 : 3, 7);
        }
        List<LearningGoal> goals = repository.load(userId);
        assertEquals(4, goals.size());
        assertTrue(goals.stream().allMatch(goal -> goal.current() == 0));
        assertTrue(goals.stream().allMatch(goal -> goal.end().equals(goal.start().plusDays(6))));
        assertTrue(repository.load(otherUserId).isEmpty());

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                execute(connection, "INSERT INTO Actividad_Diaria(id_usuario,fecha,exp_ganada) "
                        + "VALUES(?,TRUNC(SYSDATE),25)", userId);
                execute(connection, "INSERT INTO Actividad_Diaria(id_usuario,fecha,exp_ganada) "
                        + "VALUES(?,TRUNC(SYSDATE)+1,0)", userId);
                execute(connection, "INSERT INTO Actividad_Diaria(id_usuario,fecha,exp_ganada) "
                        + "VALUES(?,TRUNC(SYSDATE)-1,900)", userId);
                execute(connection, "INSERT INTO Progreso_Leccion(id_usuario,id_leccion,estado,fecha_completado) "
                        + "VALUES(?,?,'COMPLETADA',SYSTIMESTAMP)", userId, lessonId);
                for (int attempt = 0; attempt < 2; attempt++) {
                    execute(connection, "INSERT INTO Intentos(id_usuario,id_reto,codigo_enviado,es_correcto) "
                            + "VALUES(?,?,'Prueba de metas',1)", userId, challengeId);
                }
                connection.commit();
            } finally {
                connection.rollback();
            }
        }
        for (LearningGoal goal : repository.load(userId)) {
            assertEquals(goal.type() == LearningGoal.Type.XP ? 25 : 1, goal.current(), goal.type().name());
            repository.delete(otherUserId, goal.id());
        }
        assertEquals(4, repository.load(userId).size());
        for (LearningGoal goal : goals) {
            repository.delete(userId, goal.id());
        }
        assertTrue(repository.load(userId).isEmpty());

        repository.create(userId, LearningGoal.Type.DIAS, 1, 1);
        LearningGoal daily = repository.load(userId).getFirst();
        assertEquals(daily.start(), daily.end());
        assertEquals(1, daily.current());
        assertTrue(daily.completed());
    }

    @Test
    void preferencesInsertUpdateAndKeepIndependentDefaults() throws SQLException {
        OraclePreferenceRepository repository = new OraclePreferenceRepository();
        assertEquals(EditorPreferences.DEFAULT, repository.load(userId));
        assertEquals(EditorPreferences.DEFAULT, repository.load(otherUserId));

        var python = new EditorPreferences("PYTHON", 24, 2, true);
        repository.save(userId, python);
        assertEquals(python, repository.load(userId));
        assertEquals(EditorPreferences.DEFAULT, repository.load(otherUserId));

        var cpp = new EditorPreferences("CPP", 12, 8, false);
        repository.save(userId, cpp);
        repository.save(otherUserId, python);
        assertEquals(cpp, repository.load(userId));
        assertEquals(python, repository.load(otherUserId));

        repository.save(userId, EditorPreferences.DEFAULT);
        assertEquals(EditorPreferences.DEFAULT, repository.load(userId));
        assertEquals(python, repository.load(otherUserId));
    }

    private static int insertUser(Connection connection, String email) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO Usuarios(id_rol,primer_nombre,primer_apellido,correo_electronico,password_hash) "
                        + "VALUES((SELECT MIN(id_rol) FROM Roles),'Integración','Almacenamiento',?,?)")) {
            statement.setString(1, email);
            statement.setString(2, UUID.randomUUID().toString());
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id_usuario FROM Usuarios WHERE correo_electronico=?")) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("No se pudo crear la cuenta de integración.");
                }
                return result.getInt(1);
            }
        }
    }

    private static int firstId(String sql) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            result.next();
            int id = result.getInt(1);
            Assumptions.assumeTrue(id > 0, "Se necesita contenido de prueba en Oracle.");
            return id;
        }
    }

    private static void execute(Connection connection, String sql, int... values) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < values.length; index++) {
                statement.setInt(index + 1, values[index]);
            }
            statement.executeUpdate();
        }
    }
}
