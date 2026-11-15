package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.domain.model.Challenge;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Older schemas keep their catalog available until migration 13 is installed. */
final class OracleGuidanceRepository {
    private OracleGuidanceRepository() { }

    static boolean installed(Connection connection) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT COUNT(*) FROM user_tab_columns "
                + "WHERE table_name IN ('RETOS','VERSION_RETO') AND column_name='GUIA_DIDACTICA'");
             var rows = statement.executeQuery()) {
            rows.next();
            return rows.getInt(1) == 2;
        }
    }

    static void load(Connection connection, Collection<Challenge> challenges) throws SQLException {
        if (challenges.isEmpty() || !installed(connection)) return;
        Map<Integer, Challenge> byId = challenges.stream().collect(Collectors.toMap(Challenge::getId, Function.identity(), (a, b) -> a));
        try (var statement = connection.prepareStatement("SELECT id_reto,guia_didactica FROM Retos WHERE guia_didactica IS NOT NULL");
             var rows = statement.executeQuery()) {
            while (rows.next()) {
                Challenge challenge = byId.get(rows.getInt(1));
                if (challenge != null) challenge.setGuidance(ChallengeGuidanceCodec.decode(rows.getString(2)));
            }
        }
    }

    static void save(Connection connection, Challenge challenge) throws SQLException {
        if (!installed(connection)) {
            if (challenge.getGuidance().isEmpty()) return;
            throw new IllegalStateException("Instala database/13_challenge_guidance.sql para guardar las pistas y explicaciones.");
        }
        try (var statement = connection.prepareStatement("UPDATE Retos SET guia_didactica=? WHERE id_reto=?")) {
            statement.setString(1, ChallengeGuidanceCodec.encode(challenge.getGuidance()));
            statement.setInt(2, challenge.getId());
            statement.executeUpdate();
        }
    }
}
