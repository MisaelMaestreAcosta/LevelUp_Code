package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.data.oracle.OracleCurriculumRepository.*;
import java.sql.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import static org.junit.jupiter.api.Assertions.*;

class OracleCurriculumIntegrationTest {
    @Test void preservesIdsAndChildrenWhenReorderingAndRejectsStaleEdits() throws Exception {
        Assumptions.assumeTrue(DatabaseConnection.isDatabaseReady(), "Oracle local no disponible.");
        int administrator;
        try (var connection = DatabaseConnection.getConnection(); var statement = connection.prepareStatement(
                "SELECT MIN(u.id_usuario) FROM Usuarios u JOIN Roles r ON r.id_rol=u.id_rol WHERE UPPER(TRIM(r.nombre_rol))='ADMINISTRADOR'");
             var rows = statement.executeQuery()) {
            rows.next(); administrator = rows.getInt(1);
        }
        Assumptions.assumeTrue(administrator > 0, "Se requiere un administrador para verificar permisos.");
        int courseId;
        try (var connection = DatabaseConnection.getConnection(); var statement = connection.prepareStatement(
                "INSERT INTO Lenguajes(nombre,descripcion,activo) VALUES(?,'Integración de currículo',1)", new String[] { "ID_LENGUAJE" })) {
            statement.setString(1, "Test-" + UUID.randomUUID().toString().substring(0, 8)); statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) { assertTrue(keys.next()); courseId = keys.getInt(1); }
        }
        try {
            var repository = new OracleCurriculumRepository();
            final int parentId = courseId;
            assertThrows(SecurityException.class, () -> repository.save(-1, Kind.MODULE, new Entry(0, parentId, "Sin permiso", "", 1, 0), null));
            Entry first = repository.save(administrator, Kind.MODULE, new Entry(0, courseId, "Primero", "Descripción", 1, 0), null);
            Entry second = repository.save(administrator, Kind.MODULE, new Entry(0, courseId, "Segundo", "Descripción", 2, 0), null);
            Entry lesson = repository.save(administrator, Kind.LESSON, new Entry(0, first.id(), "Lección", "Contenido original ñ", 1, 60), null);
            Entry moved = repository.save(administrator, Kind.MODULE, new Entry(second.id(), courseId, "Segundo", "Descripción", 1, 0), second);
            var ordered = repository.list(administrator, Kind.MODULE, courseId);
            assertEquals(second.id(), ordered.getFirst().id());
            assertEquals(first.id(), ordered.getLast().id());
            assertEquals(2, ordered.getLast().order());
            assertEquals(lesson, repository.list(administrator, Kind.LESSON, first.id()).getFirst());
            assertThrows(IllegalStateException.class, () -> repository.save(administrator, Kind.MODULE, new Entry(first.id(), parentId, "Obsoleto", "", 1, 0), first));
            Entry edited = repository.save(administrator, Kind.LESSON,
                    new Entry(lesson.id(), lesson.parentId(), "Lección editada", "Texto nuevo", 1, 70), lesson);
            assertEquals(lesson.id(), edited.id());
            assertEquals("Texto nuevo", repository.list(administrator, Kind.LESSON, first.id()).getFirst().content());
            assertEquals(moved.id(), repository.list(administrator, Kind.MODULE, courseId).getFirst().id());
        } finally {
            try (var connection = DatabaseConnection.getConnection(); var statement = connection.prepareStatement("DELETE FROM Lenguajes WHERE id_lenguaje=?")) {
                statement.setInt(1, courseId); statement.executeUpdate();
            }
        }
    }
}
