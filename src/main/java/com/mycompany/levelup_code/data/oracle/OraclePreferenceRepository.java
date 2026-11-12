package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.domain.model.EditorPreferences;
import java.sql.*;

public final class OraclePreferenceRepository {
    public EditorPreferences load(int userId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT lenguaje_preferido,tamano_editor,"
                     + "espacios_indentacion,reducir_animaciones FROM Preferencia_Usuario WHERE id_usuario=?")) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? new EditorPreferences(result.getString(1), result.getInt(2),
                        result.getInt(3), result.getInt(4) == 1) : EditorPreferences.DEFAULT;
            }
        }
    }

    public void save(int userId, EditorPreferences preferences) throws SQLException {
        String sql = "MERGE INTO Preferencia_Usuario p USING (SELECT ? id_usuario FROM dual) v "
                + "ON (p.id_usuario=v.id_usuario) WHEN MATCHED THEN UPDATE SET lenguaje_preferido=?,"
                + "tamano_editor=?,espacios_indentacion=?,reducir_animaciones=? "
                + "WHEN NOT MATCHED THEN INSERT(id_usuario,lenguaje_preferido,tamano_editor,espacios_indentacion,reducir_animaciones) "
                + "VALUES(v.id_usuario,?,?,?,?)";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            for (int offset : new int[]{2, 6}) {
                statement.setString(offset, preferences.language());
                statement.setInt(offset + 1, preferences.fontSize());
                statement.setInt(offset + 2, preferences.indentSpaces());
                statement.setInt(offset + 3, preferences.reducedMotion() ? 1 : 0);
            }
            statement.executeUpdate();
        }
    }
}
