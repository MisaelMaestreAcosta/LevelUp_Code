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
                + "WHERE i.id_usuario=? AND i.es_correcto=1 AND r.estado='PUBLICADO' AND m.id_lenguaje=lg.id_lenguaje) superados, "
                + "(SELECT COUNT(*) FROM Retos r JOIN Lecciones le ON le.id_leccion=r.id_leccion "
                + "JOIN Modulos m ON m.id_modulo=le.id_modulo WHERE r.estado='PUBLICADO' AND m.id_lenguaje=lg.id_lenguaje) retos, "
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
                    return new LearningAnalytics(results, rs.getInt(1), rs.getInt(2),
                            loadDailyActivity(conn, userId), loadReviewChallenges(conn, userId));
                }
            }
        }
    }

    private java.util.List<LearningAnalytics.DayActivity> loadDailyActivity(Connection connection, int userId) throws SQLException {
        var days = new ArrayList<LearningAnalytics.DayActivity>();
        String sql = "WITH dias AS (SELECT TRUNC(SYSDATE)-LEVEL+1 fecha FROM dual CONNECT BY LEVEL<=28) "
                + "SELECT d.fecha,NVL(SUM(a.exp_ganada),0) FROM dias d LEFT JOIN Actividad_Diaria a "
                + "ON a.id_usuario=? AND a.fecha>=d.fecha AND a.fecha<d.fecha+1 "
                + "GROUP BY d.fecha ORDER BY d.fecha";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) days.add(new LearningAnalytics.DayActivity(rows.getDate(1).toLocalDate(), rows.getInt(2)));
            }
        }
        return days;
    }

    private java.util.List<LearningAnalytics.ReviewChallenge> loadReviewChallenges(Connection connection, int userId) throws SQLException {
        var challenges = new ArrayList<LearningAnalytics.ReviewChallenge>();
        String sql = "SELECT r.id_reto,r.titulo,lg.nombre FROM "
                + "(SELECT i.*,ROW_NUMBER() OVER (PARTITION BY i.id_reto ORDER BY i.fecha_intento DESC,i.id_intento DESC) posicion "
                + "FROM Intentos i WHERE i.id_usuario=?) ultimo "
                + "JOIN Retos r ON r.id_reto=ultimo.id_reto JOIN Lecciones le ON le.id_leccion=r.id_leccion "
                + "JOIN Modulos m ON m.id_modulo=le.id_modulo JOIN Lenguajes lg ON lg.id_lenguaje=m.id_lenguaje "
                + "WHERE ultimo.posicion=1 AND ultimo.es_correcto=0 AND r.estado='PUBLICADO' AND lg.activo=1 "
                + "AND NOT EXISTS (SELECT 1 FROM Intentos aprobado WHERE aprobado.id_usuario=? "
                + "AND aprobado.id_reto=r.id_reto AND aprobado.es_correcto=1) "
                + "ORDER BY ultimo.fecha_intento DESC,ultimo.id_intento DESC FETCH FIRST 5 ROWS ONLY";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, userId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) challenges.add(new LearningAnalytics.ReviewChallenge(rows.getInt(1), rows.getString(2), rows.getString(3)));
            }
        }
        return challenges;
    }
}
