package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import java.sql.*;
import java.util.*;

/** Administrative edits serialize on the parent and compare the loaded content before updating it. */
public final class OracleCurriculumRepository {
    public enum Kind {
        MODULE("Modulos", "id_modulo", "id_lenguaje", "nombre_modulo", "descripcion"),
        LESSON("Lecciones", "id_leccion", "id_modulo", "titulo", "contenido");
        final String table, id, parent, title, content;
        Kind(String table, String id, String parent, String title, String content) {
            this.table = table; this.id = id; this.parent = parent; this.title = title; this.content = content;
        }
    }

    public record Entry(int id, int parentId, String title, String content, int order, int xp) {
        public Entry { content = Objects.requireNonNullElse(content, ""); }
        @Override public String toString() { return order + " · " + title; }
    }
    public record Course(int id, String name) { @Override public String toString() { return name; } }

    public List<Course> courses(int administratorId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            OracleAuthoringRepository.requireAdministrator(connection, administratorId);
            try (var statement = OracleCalls.cursor(connection, "PKG_CATALOGO.cursos_editor");
                 var rows = OracleCalls.rows(statement)) {
                List<Course> courses = new ArrayList<>();
                while (rows.next()) courses.add(new Course(rows.getInt(1), rows.getString(2)));
                return List.copyOf(courses);
            }
        }
    }

    public List<Entry> list(int administratorId, Kind kind, int parentId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            OracleAuthoringRepository.requireAdministrator(connection, administratorId);
            return list(connection, kind, parentId, false);
        }
    }

    private List<Entry> list(Connection connection, Kind kind, int parentId, boolean lock) throws SQLException {
        try (var statement = OracleCalls.cursor(connection, kind == Kind.MODULE
                ? "PKG_CATALOGO.orden_modulos" : "PKG_CATALOGO.orden_lecciones", parentId, lock ? 1 : 0)) {
            try (var rows = OracleCalls.rows(statement)) {
                List<Entry> entries = new ArrayList<>();
                while (rows.next()) entries.add(new Entry(rows.getInt(1), parentId, rows.getString(2), rows.getString(3), rows.getInt(4), rows.getInt(5)));
                return entries;
            }
        }
    }

    public static void validate(Kind kind, Entry entry) {
        int titleLimit = kind == Kind.MODULE ? 80 : 100;
        if (entry.id() < 0 || entry.parentId() <= 0 || entry.title() == null || entry.title().isBlank() || entry.title().length() > titleLimit
                || entry.order() < 1 || entry.xp() < 0 || entry.xp() > 10000) {
            throw new IllegalArgumentException("Completa el título y usa una posición válida y una recompensa entre 0 y 10000 XP.");
        }
        if (kind == Kind.MODULE && entry.content().length() > 200) throw new IllegalArgumentException("La descripción del módulo admite hasta 200 caracteres.");
        if (kind == Kind.LESSON && (entry.content().isBlank() || entry.content().length() > 1_000_000)) {
            throw new IllegalArgumentException("Escribe el contenido de la lección, hasta un millón de caracteres.");
        }
    }

    public Entry save(int administratorId, Kind kind, Entry draft, Entry original) throws SQLException {
        validate(kind, draft);
        if (original != null && (draft.id() != original.id() || draft.parentId() != original.parentId())) {
            throw new IllegalArgumentException("Conserva el curso o módulo del contenido que estás editando.");
        }
        if (draft.id() > 0 && original == null) throw new IllegalArgumentException("Carga el contenido antes de editarlo.");
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                OracleAuthoringRepository.requireAdministrator(connection, administratorId);
                lockParent(connection, kind, draft.parentId());
                List<Entry> siblings = list(connection, kind, draft.parentId(), true);
                if (original != null && !siblings.contains(original)) {
                    throw new IllegalStateException("El contenido cambió. Actualiza la lista y vuelve a cargarlo antes de guardar.");
                }
                siblings.removeIf(entry -> entry.id() == draft.id());
                int position = Math.min(draft.order(), siblings.size() + 1);
                int temporary = siblings.stream().mapToInt(Entry::order).max().orElse(0) + siblings.size() + 2;
                if (original != null) temporary = Math.max(temporary, original.order() + siblings.size() + 2);
                // Move existing positions out of the final range before applying the unique order values.
                try (var statement = connection.prepareStatement("UPDATE " + kind.table + " SET orden_secuencia=orden_secuencia+? WHERE " + kind.parent + "=?")) {
                    statement.setInt(1, temporary); statement.setInt(2, draft.parentId()); statement.executeUpdate();
                }
                int id = write(connection, kind, draft, position);
                siblings.add(position - 1, new Entry(id, draft.parentId(), draft.title().strip(), draft.content(), position, draft.xp()));
                try (var statement = connection.prepareStatement("UPDATE " + kind.table + " SET orden_secuencia=? WHERE " + kind.id + "=?")) {
                    for (int i = 0; i < siblings.size(); i++) {
                        statement.setInt(1, i + 1); statement.setInt(2, siblings.get(i).id()); statement.addBatch();
                    }
                    statement.executeBatch();
                }
                connection.commit();
                return new Entry(id, draft.parentId(), draft.title().strip(), draft.content(), position, draft.xp());
            } catch (SQLException | RuntimeException ex) {
                connection.rollback(); throw ex;
            }
        }
    }

    private void lockParent(Connection connection, Kind kind, int parentId) throws SQLException {
        try (var statement = OracleCalls.cursor(connection, kind == Kind.MODULE
                ? "PKG_CATALOGO.bloquear_curso_activo" : "PKG_CATALOGO.bloquear_modulo_activo", parentId)) {
            try (var result = OracleCalls.rows(statement)) {
                if (!result.next()) throw new IllegalArgumentException("Selecciona un curso o módulo activo y existente.");
            }
        }
    }

    private int write(Connection connection, Kind kind, Entry entry, int position) throws SQLException {
        boolean fresh = entry.id() == 0;
        String sql = fresh
                ? "INSERT INTO " + kind.table + "(" + kind.title + "," + kind.content + ",orden_secuencia,"
                        + (kind == Kind.LESSON ? "exp_recompensa," : "") + kind.parent + ") VALUES(?,?,?," + (kind == Kind.LESSON ? "?," : "") + "?)"
                : "UPDATE " + kind.table + " SET " + kind.title + "=?," + kind.content + "=?,orden_secuencia=?"
                        + (kind == Kind.LESSON ? ",exp_recompensa=?" : "") + " WHERE " + kind.id + "=?";
        try (var statement = connection.prepareStatement(sql, new String[] { kind.id.toUpperCase(Locale.ROOT) })) {
            statement.setString(1, entry.title().strip()); statement.setString(2, entry.content()); statement.setInt(3, position);
            int index = 4;
            if (kind == Kind.LESSON) statement.setInt(index++, entry.xp());
            statement.setInt(index, fresh ? entry.parentId() : entry.id());
            if (statement.executeUpdate() != 1) throw new SQLException("El contenido ya no existe.");
            if (!fresh) return entry.id();
            try (var keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No se pudo identificar el contenido guardado.");
                return keys.getInt(1);
            }
        }
    }
}
