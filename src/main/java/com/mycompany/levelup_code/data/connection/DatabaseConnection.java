package com.mycompany.levelup_code.data.connection;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.Properties;

/**
 * Gestor centralizado de conexiones JDBC hacia Oracle Database.
 * Lee los parámetros desde {@code db.properties} o variables de entorno,
 * soportando tanto servicio PDB ({@code XEPDB1}) como SID ({@code xe}).
 */
public final class DatabaseConnection {

    private static final String DEFAULT_URL = "jdbc:oracle:thin:@//localhost:1521/XEPDB1";
    private static final String FALLBACK_URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String PLACEHOLDER_PASSWORD = "tu_contraseña_aqui";

    private static final String URL;
    private static final String ALT_URL;
    private static final String USER;
    private static final String PASSWORD;
    private static final int CONNECT_TIMEOUT_MS;
    private static final int READ_TIMEOUT_MS;
    private static final Executor NETWORK_TIMEOUT_EXECUTOR = command -> {
        Thread thread = new Thread(command, "oracle-network-timeout");
        thread.setDaemon(true);
        thread.start();
    };

    static {
        Properties props = new Properties();
        try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {
            // Si no existe db.properties se consultan las variables de entorno
        }

        URL = resolveSetting("DB_URL", props.getProperty("db.url"), DEFAULT_URL);
        ALT_URL = props.getProperty("db.fallback.url", FALLBACK_URL);
        USER = resolveSetting("DB_USER", props.getProperty("db.user"), "");
        PASSWORD = resolveSetting("DB_PASSWORD", props.getProperty("db.password"), "");
        CONNECT_TIMEOUT_MS = resolvePositiveInt("DB_CONNECT_TIMEOUT_MS",
                props.getProperty("db.connect.timeout.ms"), 3_000);
        READ_TIMEOUT_MS = resolvePositiveInt("DB_READ_TIMEOUT_MS",
                props.getProperty("db.read.timeout.ms"), 10_000);

        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e1) {
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
            } catch (ClassNotFoundException e2) {
                // El driver se reportará al intentar abrir conexión si no está en el classpath
            }
        }
        DriverManager.setLoginTimeout(Math.max(1, (CONNECT_TIMEOUT_MS + 999) / 1_000));
    }

    private DatabaseConnection() {
    }

    private static String resolveSetting(String envKey, String propValue, String defaultValue) {
        String env = System.getenv(envKey);
        if (env != null && !env.trim().isEmpty()) {
            return env.trim();
        }
        if (propValue != null && !propValue.trim().isEmpty()) {
            return propValue.trim();
        }
        return defaultValue;
    }

    private static int resolvePositiveInt(String envKey, String propValue, int defaultValue) {
        String raw = resolveSetting(envKey, propValue, String.valueOf(defaultValue));
        try {
            int value = Integer.parseInt(raw);
            return value > 0 ? value : defaultValue;
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static Connection openConnection(String url) throws SQLException {
        Properties connectionProperties = new Properties();
        connectionProperties.setProperty("user", USER);
        connectionProperties.setProperty("password", PASSWORD);
        connectionProperties.setProperty("oracle.net.CONNECT_TIMEOUT", String.valueOf(CONNECT_TIMEOUT_MS));
        connectionProperties.setProperty("oracle.jdbc.ReadTimeout", String.valueOf(READ_TIMEOUT_MS));
        Connection connection = DriverManager.getConnection(url, connectionProperties);
        try {
            connection.setNetworkTimeout(NETWORK_TIMEOUT_EXECUTOR, READ_TIMEOUT_MS);
        } catch (SQLException ignored) {
            // Oracle's ReadTimeout property still protects drivers that do not
            // implement the standard JDBC network timeout operation.
        }
        return connection;
    }

    /**
     * Abre una nueva conexión JDBC a Oracle intentando la URL principal (XEPDB1)
     * y, en caso de fallo de servicio, la URL alternativa (SID xe).
     */
    public static Connection getConnection() throws SQLException {
        if (USER.isEmpty() || PASSWORD.isEmpty() || PLACEHOLDER_PASSWORD.equals(PASSWORD)) {
            throw new SQLException(
                    "Credenciales de Oracle no configuradas. Cree src/main/resources/db.properties "
                    + "a partir de db.properties.example o defina DB_USER y DB_PASSWORD.");
        }
        try {
            return openConnection(URL);
        } catch (SQLException primaryEx) {
            if (ALT_URL != null && !ALT_URL.equalsIgnoreCase(URL)) {
                try {
                    return openConnection(ALT_URL);
                } catch (SQLException ignored) {
                    // Se relanza la excepción original
                }
            }
            throw primaryEx;
        }
    }

    /**
     * Verifica de forma rápida si la base de datos Oracle responde y tiene la tabla Usuarios creada.
     */
    public static boolean isDatabaseReady() {
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM Usuarios FETCH FIRST 1 ROWS ONLY")) {
            ps.setQueryTimeout(Math.max(1, (READ_TIMEOUT_MS + 999) / 1_000));
            ps.executeQuery();
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }
}
