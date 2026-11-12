package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.domain.enums.PublicationState;
import com.mycompany.levelup_code.domain.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class OracleAuthoringRepository {
    public record Entry(int id, String title, PublicationState state, int revision) {
        @Override public String toString() { return title + " · " + state + " · v" + revision; }
    }

    static void requireAdministrator(Connection connection, int administratorId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM Usuarios u "
                + "JOIN Roles r ON r.id_rol=u.id_rol WHERE u.id_usuario=? AND UPPER(TRIM(r.nombre_rol))='ADMINISTRADOR'")) {
            statement.setInt(1, administratorId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new SecurityException("Se requiere una cuenta de administrador vigente.");
            }
        }
    }

    public void checkAccess(int administratorId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            requireAdministrator(connection, administratorId);
        }
    }

    public List<Entry> list(int administratorId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            requireAdministrator(connection, administratorId);
            List<Entry> entries = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT id_reto,titulo_reto,estado,revision FROM Retos ORDER BY id_reto DESC");
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) entries.add(new Entry(result.getInt(1), result.getString(2),
                        PublicationState.valueOf(result.getString(3)), result.getInt(4)));
            }
            return entries;
        }
    }

    public AuthoredChallenge load(int administratorId, int challengeId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            requireAdministrator(connection, administratorId);
            connection.setAutoCommit(false);
            try {
                PublicationState state;
                int revision;
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT estado,revision FROM Retos WHERE id_reto=? FOR UPDATE")) {
                    statement.setInt(1, challengeId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) throw new IllegalArgumentException("El reto ya no existe.");
                        state = PublicationState.valueOf(result.getString(1));
                        revision = result.getInt(2);
                    }
                }
                Challenge challenge = new OracleChallengeRepository().findForAuthoring(connection, challengeId);
                String reference = "";
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT codigo_solucion FROM Solucion_Referencia WHERE id_reto=?")) {
                    statement.setInt(1, challengeId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) reference = result.getString(1);
                    }
                }
                connection.rollback();
                return new AuthoredChallenge(challenge, reference, state, revision);
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    public AuthoredChallenge save(int administratorId, AuthoredChallenge draft) throws SQLException {
        Challenge challenge = draft.challenge();
        int originalId = challenge.getId();
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                requireAdministrator(connection, administratorId);
                OracleContentRepository content = new OracleContentRepository();
                if (originalId > 0) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "SELECT revision,id_leccion,tipo_reto FROM Retos WHERE id_reto=? FOR UPDATE")) {
                        statement.setInt(1, originalId);
                        try (ResultSet result = statement.executeQuery()) {
                            if (!result.next() || result.getInt(1) != draft.revision()) {
                                throw new IllegalStateException("El reto cambió. Actualiza la lista y vuelve a cargarlo.");
                            }
                            if (result.getInt(2) != challenge.getLessonId()
                                    || !result.getString(3).equals(challenge.getType().databaseValue())) {
                                throw new IllegalArgumentException("Conserva la lección y el tipo del reto existente.");
                            }
                        }
                    }
                    content.updateChallenge(connection, challenge);
                } else {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "SELECT id_leccion FROM Lecciones WHERE id_leccion=? FOR UPDATE")) {
                        statement.setInt(1, challenge.getLessonId());
                        try (ResultSet result = statement.executeQuery()) {
                            if (!result.next()) throw new IllegalArgumentException("Selecciona una lección existente.");
                        }
                    }
                    content.saveChallenge(connection, challenge);
                }
                int revision = originalId == 0 ? 1 : draft.revision() + 1;
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE Retos SET estado=?,revision=?,fecha_actualizacion=SYSTIMESTAMP WHERE id_reto=?")) {
                    statement.setString(1, draft.state().name());
                    statement.setInt(2, revision);
                    statement.setInt(3, challenge.getId());
                    statement.executeUpdate();
                }
                if (challenge instanceof CodeChallenge) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM Solucion_Referencia WHERE id_reto=?")) {
                        statement.setInt(1, challenge.getId());
                        statement.executeUpdate();
                    }
                    if (draft.referenceSolution() != null && !draft.referenceSolution().isBlank()) {
                        try (PreparedStatement statement = connection.prepareStatement(
                                "INSERT INTO Solucion_Referencia(id_reto,codigo_solucion) VALUES(?,?)")) {
                            statement.setInt(1, challenge.getId());
                            statement.setString(2, draft.referenceSolution());
                            statement.executeUpdate();
                        }
                    }
                }
                OracleChallengeVersionRepository.snapshot(connection, challenge.getId(), administratorId);
                connection.commit();
                challenge.setRevision(revision);
                return new AuthoredChallenge(challenge, draft.referenceSolution(), draft.state(), revision);
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                challenge.setId(originalId);
                throw ex;
            }
        }
    }
}
