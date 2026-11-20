package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class OracleChallengeVersionRepository {
    public record Version(int revision, String details) {
        @Override public String toString() { return "Versión " + revision; }
    }

    static void snapshot(Connection connection, int challengeId, Integer administratorId) throws SQLException {
        OracleCalls.execute(connection, "PKG_RETOS.guardar_version", challengeId, administratorId);
    }

    public List<Version> list(int administratorId, int challengeId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            OracleAuthoringRepository.requireAdministrator(connection, administratorId);
            List<Version> versions = new ArrayList<>();
            boolean guidanceInstalled = OracleGuidanceRepository.installed(connection);
            try (var statement = OracleCalls.cursor(connection, "PKG_RETOS.versiones_reto", challengeId)) {
                try (ResultSet result = OracleCalls.rows(statement)) {
                    while (result.next()) {
                        StringBuilder details = new StringBuilder(result.getString("titulo_reto"))
                                .append("\n").append(result.getString("estado")).append(" · ").append(result.getTimestamp("fecha_guardado"))
                                .append("\n\n").append(result.getString("enunciado"))
                                .append("\n\nPlantilla\n").append(result.getString("codigo_inicial"))
                                .append("\n\nReferencia\n").append(result.getString("solucion_referencia"));
                        if (guidanceInstalled) {
                            var guidance = ChallengeGuidanceCodec.decode(result.getString("guia_didactica"));
                            for (int i = 0; i < guidance.hints().size(); i++) details.append("\n\nPista ").append(i + 1).append("\n").append(guidance.hints().get(i));
                            details.append("\n\nExplicación\n").append(guidance.explanation());
                            for (int i = 0; i < guidance.optionFeedback().size(); i++) details.append("\n\nExplicación de opción ").append(i + 1).append("\n").append(guidance.optionFeedback().get(i));
                        }
                        String options = result.getString("opciones");
                        if (options != null) details.append("\n\nOpciones\n").append(String.join("\n", QuizOptionCodec.decode(options)))
                                .append("\nRespuesta\n").append(result.getString("respuesta_correcta"));
                        try (var cases = OracleCalls.cursor(connection,
                                "PKG_RETOS.casos_version", result.getInt("id_version"))) {
                            try (ResultSet test = OracleCalls.rows(cases)) {
                                while (test.next()) details.append("\n\nCaso ").append(test.getInt(1))
                                        .append(test.getInt(4) == 1 ? " (oculto)" : " (visible)")
                                        .append("\nEntrada\n").append(test.getString(2)).append("\nSalida\n").append(test.getString(3));
                            }
                        }
                        versions.add(new Version(result.getInt("revision"), details.toString()));
                    }
                }
            }
            return versions;
        }
    }
}
