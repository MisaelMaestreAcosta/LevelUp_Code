package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.domain.model.Challenge;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

final class OracleModuleFinalRepository {
    private OracleModuleFinalRepository() { }

    static void load(Connection connection, Collection<Challenge> challenges) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT COUNT(*) FROM user_tables WHERE table_name='FINAL_MODULO'");
             var rows = statement.executeQuery()) {
            rows.next();
            if (rows.getInt(1) == 0) return;
        }
        Set<Integer> finals = new HashSet<>();
        try (var statement = connection.prepareStatement("SELECT f.id_reto FROM Final_Modulo f "
                + "JOIN Retos r ON r.id_reto=f.id_reto JOIN Lecciones l ON l.id_leccion=r.id_leccion WHERE l.id_modulo=f.id_modulo");
             var rows = statement.executeQuery()) {
            while (rows.next()) finals.add(rows.getInt(1));
        }
        challenges.forEach(challenge -> challenge.setModuleFinal(finals.contains(challenge.getId())));
    }
}
