package com.mycompany.levelup_code.data.oracle;

import java.io.Reader;
import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;

/** JDBC calls to stored routines. The caller owns the connection and transaction. */
public final class OracleCalls {
    private OracleCalls() { }

    public static CallableStatement cursor(Connection connection, String procedure, Object... inputs) throws SQLException {
        return prepare(connection, procedure, Types.REF_CURSOR, 1, inputs);
    }

    public static ResultSet rows(CallableStatement statement) throws SQLException {
        statement.execute();
        return (ResultSet) statement.getObject(1);
    }

    static void execute(Connection connection, String procedure, Object... inputs) throws SQLException {
        try (var statement = prepare(connection, procedure, null, 0, inputs)) {
            statement.execute();
        }
    }

    static int number(Connection connection, String procedure, Object... inputs) throws SQLException {
        int outputPosition = inputs.length + 1;
        try (var statement = prepare(connection, procedure, Types.NUMERIC, outputPosition, inputs)) {
            statement.execute();
            return statement.getInt(outputPosition);
        }
    }

    static Reader clob(String value) {
        return value == null ? null : new StringReader(value);
    }

    private static CallableStatement prepare(Connection connection, String procedure, Integer outputType,
            int outputPosition, Object[] inputs) throws SQLException {
        int count = inputs.length + (outputType == null ? 0 : 1);
        String placeholders = String.join(", ", Collections.nCopies(count, "?"));
        CallableStatement statement = connection.prepareCall("{call " + procedure + "(" + placeholders + ")}");
        try {
            if (outputType != null) {
                statement.registerOutParameter(outputPosition, outputType);
            }
            for (int i = 0; i < inputs.length; i++) {
                int position = i + (outputPosition == 1 ? 2 : 1);
                if (inputs[i] instanceof Reader reader) {
                    statement.setClob(position, reader);
                } else {
                    statement.setObject(position, inputs[i]);
                }
            }
            return statement;
        } catch (SQLException | RuntimeException failure) {
            try {
                statement.close();
            } catch (SQLException closeError) {
                failure.addSuppressed(closeError);
            }
            throw failure;
        }
    }
}
