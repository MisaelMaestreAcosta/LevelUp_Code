package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.domain.model.LearningAnalytics;
import java.sql.*;
import java.util.ArrayList;

/** Aggregates attempts separately from lessons to avoid multiplying counters by joins. */
public final class OracleLearningAnalyticsRepository {
    public LearningAnalytics load(int userId) throws SQLException {
        var results = new ArrayList<LearningAnalytics.LanguageResult>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (var stmt = OracleCalls.cursor(conn, "PKG_SEGUIMIENTO.resumen_lenguajes", userId)) {
                try (ResultSet rs = OracleCalls.rows(stmt)) {
                    while (rs.next()) results.add(new LearningAnalytics.LanguageResult(rs.getString(1),
                            rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getInt(7)));
                }
            }
            try (var stmt = OracleCalls.cursor(conn, "PKG_SEGUIMIENTO.xp_semanal", userId)) {
                try (ResultSet rs = OracleCalls.rows(stmt)) {
                    rs.next();
                    return new LearningAnalytics(results, rs.getInt(1), rs.getInt(2),
                            loadDailyActivity(conn, userId), loadReviewChallenges(conn, userId));
                }
            }
        }
    }

    private java.util.List<LearningAnalytics.DayActivity> loadDailyActivity(Connection connection, int userId) throws SQLException {
        var days = new ArrayList<LearningAnalytics.DayActivity>();
        try (var statement = OracleCalls.cursor(connection, "PKG_SEGUIMIENTO.xp_diaria", userId)) {
            try (ResultSet rows = OracleCalls.rows(statement)) {
                while (rows.next()) days.add(new LearningAnalytics.DayActivity(rows.getDate(1).toLocalDate(), rows.getInt(2)));
            }
        }
        return days;
    }

    private java.util.List<LearningAnalytics.ReviewChallenge> loadReviewChallenges(Connection connection, int userId) throws SQLException {
        var challenges = new ArrayList<LearningAnalytics.ReviewChallenge>();
        try (var statement = OracleCalls.cursor(connection, "PKG_SEGUIMIENTO.retos_para_repasar", userId)) {
            try (ResultSet rows = OracleCalls.rows(statement)) {
                while (rows.next()) challenges.add(new LearningAnalytics.ReviewChallenge(rows.getInt(1), rows.getString(2), rows.getString(3)));
            }
        }
        return challenges;
    }
}
