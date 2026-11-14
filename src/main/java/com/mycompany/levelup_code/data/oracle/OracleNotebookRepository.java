package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import java.sql.*;
import java.util.*;

/** Every mutation is scoped to the signed-in user. */
public final class OracleNotebookRepository {
    public record Note(int id, Integer challengeId, String title, String content) {
        @Override public String toString() { return title; }
    }
    public record Notebook(List<Note> notes, Set<Integer> favorites) { }

    public Notebook load(int userId) throws SQLException {
        var notes = new ArrayList<Note>();
        var favorites = new HashSet<Integer>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id_nota,id_reto,titulo,contenido "
                    + "FROM Nota_Estudio WHERE id_usuario=? ORDER BY fecha_edicion DESC")) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int challenge = rs.getInt(2);
                        Integer challengeId = rs.wasNull() ? null : challenge;
                        notes.add(new Note(rs.getInt(1), challengeId, rs.getString(3), rs.getString(4)));
                    }
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id_reto FROM Reto_Favorito WHERE id_usuario=?")) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) { while (rs.next()) favorites.add(rs.getInt(1)); }
            }
        }
        return new Notebook(notes, favorites);
    }

    public void save(int userId, Note note) throws SQLException {
        if (note.title() == null || note.title().isBlank() || note.title().length()>100
                || note.content() == null || note.content().isBlank()) {
            throw new IllegalArgumentException("Escribe un título de hasta 100 caracteres y el contenido de la nota.");
        }
        boolean updating = note.id()>0;
        String sql = updating
                ? "UPDATE Nota_Estudio SET id_reto=?,titulo=?,contenido=?,fecha_edicion=SYSTIMESTAMP WHERE id_usuario=? AND id_nota=?"
                : "INSERT INTO Nota_Estudio(id_reto,titulo,contenido,id_usuario) VALUES(?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (note.challengeId()==null) stmt.setNull(1, Types.NUMERIC); else stmt.setInt(1,note.challengeId());
            stmt.setString(2,note.title().strip()); stmt.setString(3,note.content()); stmt.setInt(4,userId);
            if (updating) stmt.setInt(5,note.id());
            if (stmt.executeUpdate()!=1) throw new SQLException("La nota ya no existe.");
        }
    }

    public void delete(int userId, int noteId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM Nota_Estudio WHERE id_usuario=? AND id_nota=?")) {
            stmt.setInt(1,userId); stmt.setInt(2,noteId); stmt.executeUpdate();
        }
    }

    public void favorite(int userId, int challengeId, boolean saved) throws SQLException {
        String sql = saved
                ? "MERGE INTO Reto_Favorito f USING (SELECT ? id_usuario, ? id_reto FROM dual) v "
                    + "ON (f.id_usuario=v.id_usuario AND f.id_reto=v.id_reto) "
                    + "WHEN NOT MATCHED THEN INSERT(id_usuario,id_reto) VALUES(v.id_usuario,v.id_reto)"
                : "DELETE FROM Reto_Favorito WHERE id_usuario=? AND id_reto=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1,userId); stmt.setInt(2,challengeId); stmt.executeUpdate();
        }
    }
}
