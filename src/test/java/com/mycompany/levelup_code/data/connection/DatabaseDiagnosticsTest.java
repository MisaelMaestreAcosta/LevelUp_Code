package com.mycompany.levelup_code.data.connection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseDiagnosticsTest {
    @Test
    void identifiesIncompleteMigrationsAndInvalidPackageBodies() {
        Set<String> objects = new HashSet<>(List.of("SOLUCION_REFERENCIA/TABLE", "VERSION_RETO/TABLE",
                "VERSION_CASO_PRUEBA/TABLE", "TRG_INTENTO_VERSION/TRIGGER", "RETO_PREDICCION/TABLE",
                "RETO_ORDENAMIENTO/TABLE", "PKG_GAMIFICACION/PACKAGE", "TRG_AUDIT_USUARIOS/TRIGGER"));
        Set<String> columns = Set.of("RETOS.ESTADO", "RETOS.REVISION", "RETOS.FECHA_ACTUALIZACION",
                "INTENTOS.ID_VERSION_RETO", "INTENTOS.ID_SOLICITUD");
        var report = DatabaseDiagnostics.evaluate(objects, columns);
        assertTrue(report.connected());
        assertTrue(report.scripts().contains("03_plsql_gamificacion.sql"));
        assertTrue(report.scripts().contains("05_student_notebook.sql"));
        assertFalse(report.scripts().contains("08_content_publication.sql"));
        assertFalse(report.scripts().contains("09_content_versions.sql"));
        assertFalse(report.scripts().contains("10_challenge_types.sql"));

        objects.add("PKG_GAMIFICACION/PACKAGE BODY");
        assertFalse(DatabaseDiagnostics.evaluate(objects, columns).scripts().contains("03_plsql_gamificacion.sql"));
        assertTrue(DatabaseDiagnostics.evaluate(objects, Set.of()).scripts().contains("08_content_publication.sql"));
    }

    @Test
    void distinguishesConnectionFailureFromMissingScripts() {
        assertTrue(new DatabaseDiagnostics.Report(false, List.of()).message().contains("configuración"));
        assertTrue(DatabaseDiagnostics.evaluate(Set.of(), Set.of()).message().contains("01_schema.sql"));
    }
}
