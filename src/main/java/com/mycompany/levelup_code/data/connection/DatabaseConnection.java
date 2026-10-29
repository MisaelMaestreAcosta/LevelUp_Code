package com.mycompany.levelup_code.data.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestiona la obtención de conexiones JDBC hacia la base de datos Oracle.
 */
public final class DatabaseConnection {

    private static final String DEFAULT_URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String DEFAULT_USER = "levelup_code";
    private static final String DEFAULT_PASSWORD = "password";

    private DatabaseConnection() {
    }

    /**
     * Abre y devuelve una conexión activa a la base de datos configurada.
     *
     * @return Conexión JDBC
     * @throws SQLException Si ocurre un error al establecer la conexión
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DEFAULT_URL, DEFAULT_USER, DEFAULT_PASSWORD);
    }
}

