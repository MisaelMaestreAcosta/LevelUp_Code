package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public final class OraclePracticeRepository {
    public Map<Integer, LocalDate> lastAttempts(int userId) throws SQLException {
        Map<Integer, LocalDate> attempts = new HashMap<>();
        try (var connection = DatabaseConnection.getConnection();
             var statement = OracleCalls.cursor(connection, "PKG_SEGUIMIENTO.ultimas_practicas", userId)) {
            try (var rows = OracleCalls.rows(statement)) {
                while (rows.next()) attempts.put(rows.getInt(1), rows.getDate(2).toLocalDate());
            }
        }
        return attempts;
    }
}
