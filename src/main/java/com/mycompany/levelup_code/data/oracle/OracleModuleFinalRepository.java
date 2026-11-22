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
        try (var statement = OracleCalls.cursor(connection, "PKG_DIAGNOSTICO.tabla_final_modulo");
             var rows = OracleCalls.rows(statement)) {
            rows.next();
            if (rows.getInt(1) == 0) return;
        }
        Set<Integer> finals = new HashSet<>();
        try (var statement = OracleCalls.cursor(connection, "PKG_RETOS.retos_finales");
             var rows = OracleCalls.rows(statement)) {
            while (rows.next()) finals.add(rows.getInt(1));
        }
        challenges.forEach(challenge -> challenge.setModuleFinal(finals.contains(challenge.getId())));
    }
}
