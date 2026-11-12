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
        String sql = "INSERT INTO Version_Reto(id_reto,revision,titulo_reto,enunciado,tipo_reto,dificultad,"
                + "exp_recompensa,codigo_inicial,limite_tiempo_ms,opciones,respuesta_correcta,solucion_referencia,estado,id_administrador) "
                + "SELECT r.id_reto,r.revision,r.titulo_reto,r.enunciado,r.tipo_reto,r.dificultad,r.exp_recompensa,"
                + "c.codigo_inicial,c.limite_tiempo_ms,q.opciones,q.respuesta_correcta,s.codigo_solucion,r.estado,? "
                + "FROM Retos r LEFT JOIN Reto_Codigo c ON c.id_reto=r.id_reto "
                + "LEFT JOIN Reto_Quiz q ON q.id_reto=r.id_reto LEFT JOIN Solucion_Referencia s ON s.id_reto=r.id_reto WHERE r.id_reto=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (administratorId == null) statement.setNull(1, Types.NUMERIC); else statement.setInt(1, administratorId);
            statement.setInt(2, challengeId);
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement("UPDATE Version_Reto v SET "
                + "codigo_inicial=(SELECT codigo_ejemplo FROM Reto_Prediccion WHERE id_reto=v.id_reto) "
                + "WHERE v.id_reto=? AND v.revision=(SELECT revision FROM Retos WHERE id_reto=?) AND tipo_reto='PREDICCION'")) {
            statement.setInt(1, challengeId); statement.setInt(2, challengeId); statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement("UPDATE Version_Reto v SET "
                + "opciones=(SELECT fragmentos FROM Reto_Ordenamiento WHERE id_reto=v.id_reto),"
                + "respuesta_correcta=(SELECT orden_correcto FROM Reto_Ordenamiento WHERE id_reto=v.id_reto) "
                + "WHERE v.id_reto=? AND v.revision=(SELECT revision FROM Retos WHERE id_reto=?) AND tipo_reto='ORDENAR'")) {
            statement.setInt(1, challengeId); statement.setInt(2, challengeId); statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO Version_Caso_Prueba "
                + "(id_version,numero,entrada,salida_esperada,es_oculto) "
                + "SELECT v.id_version,ROW_NUMBER() OVER(ORDER BY c.id_caso),c.entrada,c.salida_esperada,c.es_oculto "
                + "FROM Version_Reto v JOIN Retos r ON r.id_reto=v.id_reto AND r.revision=v.revision "
                + "JOIN Casos_Prueba c ON c.id_reto=r.id_reto WHERE r.id_reto=?")) {
            statement.setInt(1, challengeId);
            statement.executeUpdate();
        }
    }

    public List<Version> list(int administratorId, int challengeId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            OracleAuthoringRepository.requireAdministrator(connection, administratorId);
            List<Version> versions = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM Version_Reto WHERE id_reto=? ORDER BY revision DESC")) {
                statement.setInt(1, challengeId);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        StringBuilder details = new StringBuilder(result.getString("titulo_reto"))
                                .append("\n").append(result.getString("estado")).append(" · ").append(result.getTimestamp("fecha_guardado"))
                                .append("\n\n").append(result.getString("enunciado"))
                                .append("\n\nPlantilla\n").append(result.getString("codigo_inicial"))
                                .append("\n\nReferencia\n").append(result.getString("solucion_referencia"));
                        String options = result.getString("opciones");
                        if (options != null) details.append("\n\nOpciones\n").append(String.join("\n", QuizOptionCodec.decode(options)))
                                .append("\nRespuesta\n").append(result.getString("respuesta_correcta"));
                        try (PreparedStatement cases = connection.prepareStatement(
                                "SELECT numero,entrada,salida_esperada,es_oculto FROM Version_Caso_Prueba WHERE id_version=? ORDER BY numero")) {
                            cases.setInt(1, result.getInt("id_version"));
                            try (ResultSet test = cases.executeQuery()) {
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
