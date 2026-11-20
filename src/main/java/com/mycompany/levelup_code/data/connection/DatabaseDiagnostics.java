package com.mycompany.levelup_code.data.connection;

import com.mycompany.levelup_code.data.oracle.OracleCalls;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Read-only checks against the schema used by the application's connection. */
public final class DatabaseDiagnostics {
    public record Report(boolean connected, List<String> scripts) {
        public Report { scripts = List.copyOf(scripts); }

        public String message() {
            if (!connected) return "No pudimos conectar con Oracle. Comprueba el servicio y la configuración de la conexión.";
            if (scripts.isEmpty()) return "Oracle responde y las actualizaciones de la base de datos están disponibles. Vuelve a intentar la operación.";
            return "Oracle responde, pero hay objetos ausentes o inválidos en la base de datos. "
                    + "Pide al administrador que revise estos scripts en database: " + String.join(", ", scripts) + ".";
        }
    }

    private record Requirement(String script, List<String> objects, List<String> columns) { }

    private static final List<Requirement> REQUIREMENTS = List.of(
            new Requirement("01_schema.sql", List.of("ROLES/TABLE", "USUARIOS/TABLE", "ACTIVIDAD_DIARIA/TABLE",
                    "LENGUAJES/TABLE", "INSCRIPCIONES/TABLE", "MODULOS/TABLE", "LECCIONES/TABLE",
                    "PROGRESO_LECCION/TABLE", "RETOS/TABLE", "RETO_CODIGO/TABLE", "CASOS_PRUEBA/TABLE",
                    "RETO_QUIZ/TABLE", "INTENTOS/TABLE", "LOGROS/TABLE", "LOGRO_RACHA/TABLE",
                    "LOGRO_EXP/TABLE", "LOGRO_RETOS/TABLE", "USUARIO_LOGRO/TABLE"), List.of("RETOS.TITULO_RETO")),
            new Requirement("05_student_notebook.sql", List.of("NOTA_ESTUDIO/TABLE", "RETO_FAVORITO/TABLE"), List.of()),
            new Requirement("06_learning_goals.sql", List.of("META_USUARIO/TABLE"), List.of()),
            new Requirement("07_user_preferences.sql", List.of("PREFERENCIA_USUARIO/TABLE"), List.of()),
            new Requirement("08_content_publication.sql", List.of("SOLUCION_REFERENCIA/TABLE"),
                    List.of("RETOS.ESTADO", "RETOS.REVISION", "RETOS.FECHA_ACTUALIZACION")),
            new Requirement("09_content_versions.sql", List.of("VERSION_RETO/TABLE", "VERSION_CASO_PRUEBA/TABLE",
                    "TRG_INTENTO_VERSION/TRIGGER"), List.of("INTENTOS.ID_VERSION_RETO")),
            new Requirement("10_challenge_types.sql", List.of("RETO_PREDICCION/TABLE", "RETO_ORDENAMIENTO/TABLE"), List.of()),
            new Requirement("13_challenge_guidance.sql", List.of(), List.of("RETOS.GUIA_DIDACTICA", "VERSION_RETO.GUIA_DIDACTICA")),
            new Requirement("14_module_finals.sql", List.of("FINAL_MODULO/TABLE"), List.of()),
            new Requirement("15_professional_missions.sql", List.of("MISION_PROFESIONAL/TABLE",
                    "REQUISITO_MISION/TABLE", "REQUISITO_CASO_PRUEBA/TABLE"), List.of()),
            new Requirement("03_plsql_gamificacion.sql", List.of("PKG_GAMIFICACION/PACKAGE",
                    "PKG_GAMIFICACION/PACKAGE BODY", "TRG_AUDIT_USUARIOS/TRIGGER"), List.of("INTENTOS.ID_SOLICITUD")),
            new Requirement("17_data_access.sql", List.of("PKG_DIAGNOSTICO/PACKAGE", "PKG_DIAGNOSTICO/PACKAGE BODY",
                    "PKG_USUARIOS/PACKAGE", "PKG_USUARIOS/PACKAGE BODY",
                    "PKG_CATALOGO/PACKAGE", "PKG_CATALOGO/PACKAGE BODY",
                    "PKG_RETOS/PACKAGE", "PKG_RETOS/PACKAGE BODY",
                    "PKG_SEGUIMIENTO/PACKAGE", "PKG_SEGUIMIENTO/PACKAGE BODY",
                    "PKG_ESPACIO_ESTUDIANTE/PACKAGE", "PKG_ESPACIO_ESTUDIANTE/PACKAGE BODY"), List.of())
    );

    private DatabaseDiagnostics() { }

    public static Report inspect() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            Set<String> objects = new HashSet<>();
            Set<String> columns = new HashSet<>();
            try (var statement = OracleCalls.cursor(connection, "PKG_DIAGNOSTICO.objetos_validos")) {
                statement.setQueryTimeout(10);
                try (var rows = OracleCalls.rows(statement)) {
                    while (rows.next()) objects.add(rows.getString(1) + "/" + rows.getString(2));
                }
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 6550 || ex.getErrorCode() == 4063 || ex.getErrorCode() == 6508) {
                    return new Report(true, List.of("17_data_access.sql"));
                }
                throw ex;
            }
            try (var statement = OracleCalls.cursor(connection, "PKG_DIAGNOSTICO.columnas_instaladas")) {
                statement.setQueryTimeout(10);
                try (var rows = OracleCalls.rows(statement)) {
                    while (rows.next()) columns.add(rows.getString(1) + "." + rows.getString(2));
                }
            }
            return evaluate(objects, columns);
        } catch (SQLException ex) {
            return new Report(false, List.of());
        }
    }

    static Report evaluate(Set<String> objects, Set<String> columns) {
        List<String> scripts = new ArrayList<>();
        for (Requirement requirement : REQUIREMENTS) {
            if (!objects.containsAll(requirement.objects()) || !columns.containsAll(requirement.columns())) {
                scripts.add(requirement.script());
            }
        }
        return new Report(true, scripts);
    }
}
