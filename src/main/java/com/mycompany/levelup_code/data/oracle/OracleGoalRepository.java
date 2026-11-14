package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.domain.model.LearningGoal;
import java.sql.*;
import java.util.*;

public final class OracleGoalRepository {
    public void create(int userId, LearningGoal.Type type, int target, int days) throws SQLException {
        LearningGoal.validate(type,target,days);
        try (Connection conn=DatabaseConnection.getConnection(); PreparedStatement stmt=conn.prepareStatement(
                "INSERT INTO Meta_Usuario(id_usuario,tipo_meta,valor_objetivo,fecha_inicio,fecha_fin) "
                + "VALUES(?,?,?,TRUNC(SYSDATE),TRUNC(SYSDATE)+?)")) {
            stmt.setInt(1,userId); stmt.setString(2,type.name()); stmt.setInt(3,target); stmt.setInt(4,days-1);
            stmt.executeUpdate();
        }
    }
    public List<LearningGoal> load(int userId) throws SQLException {
        String sql="SELECT g.id_meta,g.tipo_meta,g.valor_objetivo,g.fecha_inicio,g.fecha_fin, "
                + "CASE g.tipo_meta "
                + "WHEN 'RETOS' THEN (SELECT COUNT(DISTINCT i.id_reto) FROM Intentos i "
                + "WHERE i.id_usuario=g.id_usuario AND i.es_correcto=1 "
                + "AND i.fecha_intento>=g.fecha_inicio AND i.fecha_intento<g.fecha_fin+1) "
                + "WHEN 'LECCIONES' THEN (SELECT COUNT(*) FROM Progreso_Leccion p "
                + "WHERE p.id_usuario=g.id_usuario AND p.estado='COMPLETADA' "
                + "AND p.fecha_completado>=g.fecha_inicio AND p.fecha_completado<g.fecha_fin+1) "
                + "WHEN 'XP' THEN (SELECT NVL(SUM(a.exp_ganada),0) FROM Actividad_Diaria a "
                + "WHERE a.id_usuario=g.id_usuario AND a.fecha>=g.fecha_inicio AND a.fecha<g.fecha_fin+1) "
                + "WHEN 'DIAS' THEN (SELECT COUNT(DISTINCT TRUNC(a.fecha)) FROM Actividad_Diaria a "
                + "WHERE a.id_usuario=g.id_usuario AND a.exp_ganada>0 "
                + "AND a.fecha>=g.fecha_inicio AND a.fecha<g.fecha_fin+1) END avance "
                + "FROM Meta_Usuario g WHERE g.id_usuario=? ORDER BY g.fecha_fin DESC,g.id_meta DESC";
        var goals=new ArrayList<LearningGoal>();
        try (Connection conn=DatabaseConnection.getConnection(); PreparedStatement stmt=conn.prepareStatement(sql)) {
            stmt.setInt(1,userId);
            try (ResultSet rs=stmt.executeQuery()) {
                while(rs.next()) goals.add(new LearningGoal(rs.getInt(1),LearningGoal.Type.valueOf(rs.getString(2)),
                        rs.getInt(3),rs.getDate(4).toLocalDate(),rs.getDate(5).toLocalDate(),rs.getInt(6)));
            }
        }
        return goals;
    }
    public void delete(int userId,int id) throws SQLException {
        try (Connection conn=DatabaseConnection.getConnection(); PreparedStatement stmt=conn.prepareStatement(
                "DELETE FROM Meta_Usuario WHERE id_usuario=? AND id_meta=?")) {
            stmt.setInt(1,userId); stmt.setInt(2,id); stmt.executeUpdate();
        }
    }
}
