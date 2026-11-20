package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.domain.model.EditorPreferences;
import java.sql.*;

public final class OraclePreferenceRepository {
    public EditorPreferences load(int userId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             var statement = OracleCalls.cursor(connection, "PKG_ESPACIO_ESTUDIANTE.preferencias_usuario", userId)) {
            try (ResultSet result = OracleCalls.rows(statement)) {
                return result.next() ? new EditorPreferences(result.getString(1), result.getInt(2),
                        result.getInt(3), result.getInt(4) == 1) : EditorPreferences.DEFAULT;
            }
        }
    }

    public void save(int userId, EditorPreferences preferences) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            OracleCalls.execute(connection, "PKG_ESPACIO_ESTUDIANTE.guardar_preferencias", userId, preferences.language(), preferences.fontSize(),
                    preferences.indentSpaces(), preferences.reducedMotion() ? 1 : 0);
        }
    }
}
