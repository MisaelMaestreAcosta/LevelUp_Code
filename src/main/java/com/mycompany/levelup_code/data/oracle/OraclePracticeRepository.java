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
             var statement = connection.prepareStatement("SELECT id_reto,TRUNC(MAX(fecha_intento)) FROM Intentos WHERE id_usuario=? GROUP BY id_reto")) {
            statement.setInt(1, userId);
            try (var rows = statement.executeQuery()) {
                while (rows.next()) attempts.put(rows.getInt(1), rows.getDate(2).toLocalDate());
            }
        }
        return attempts;
    }
}
