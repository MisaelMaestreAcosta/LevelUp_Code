package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.domain.model.LearningAnalytics;
import java.sql.*;
import java.util.ArrayList;

/** Aggregates attempts separately from lessons to avoid multiplying counters by joins. */
public final class OracleLearningAnalyticsRepository {
    public LearningAnalytics load(int userId) throws SQLException {
        var results = new ArrayList<LearningAnalytics.LanguageResult>();
        String sql = "SELECT lg.nombre, "
                + "(SELECT COUNT(*) FROM Intentos i JOIN Retos r ON r.id_reto=i.id_reto "
                + "JOIN Lecciones le ON le.id_leccion=r.id_leccion JOIN Modulos m ON m.id_modulo=le.id_modulo "
                + "WHERE i.id_usuario=? AND m.id_lenguaje=lg.id_lenguaje) intentos, "
                + "(SELECT COUNT(*) FROM Intentos i JOIN Retos r ON r.id_reto=i.id_reto "
                + "JOIN Lecciones le ON le.id_leccion=r.id_leccion JOIN Modulos m ON m.id_modulo=le.id_modulo "
                + "WHERE i.id_usuario=? AND i.es_correcto=1 AND m.id_lenguaje=lg.id_lenguaje) aciertos, "
                + "(SELECT COUNT(DISTINCT i.id_reto) FROM Intentos i JOIN Retos r ON r.id_reto=i.id_reto "
                + "JOIN Lecciones le ON le.id_leccion=r.id_leccion JOIN Modulos m ON m.id_modulo=le.id_modulo "
                + "WHERE i.id_usuario=? AND i.es_correcto=1 AND m.id_lenguaje=lg.id_lenguaje) superados, "
                + "(SELECT COUNT(*) FROM Retos r JOIN Lecciones le ON le.id_leccion=r.id_leccion "
                + "JOIN Modulos m ON m.id_modulo=le.id_modulo WHERE m.id_lenguaje=lg.id_lenguaje) retos, "
                + "(SELECT COUNT(*) FROM Progreso_Leccion p JOIN Lecciones le ON le.id_leccion=p.id_leccion "
                + "JOIN Modulos m ON m.id_modulo=le.id_modulo WHERE p.id_usuario=? "
                + "AND p.estado='COMPLETADA' AND m.id_lenguaje=lg.id_lenguaje) lecturas, "
                + "(SELECT COUNT(*) FROM Lecciones le JOIN Modulos m ON m.id_modulo=le.id_modulo "
                + "WHERE m.id_lenguaje=lg.id_lenguaje) lecciones "
                + "FROM Lenguajes lg WHERE lg.activo=1 ORDER BY lg.id_lenguaje";
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 1; i <= 4; i++) stmt.setInt(i, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) results.add(new LearningAnalytics.LanguageResult(rs.getString(1),
                            rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getInt(7)));
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement("SELECT NVL(SUM(exp_ganada),0), "
                    + "COUNT(DISTINCT CASE WHEN exp_ganada>0 THEN TRUNC(fecha) END) FROM Actividad_Diaria "
                    + "WHERE id_usuario=? AND fecha>=TRUNC(SYSDATE)-6 AND fecha<TRUNC(SYSDATE)+1")) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    return new LearningAnalytics(results, rs.getInt(1), rs.getInt(2));
                }
            }
        }
    }
}
