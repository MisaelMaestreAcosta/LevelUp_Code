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
        var goals=new ArrayList<LearningGoal>();
        try (Connection conn=DatabaseConnection.getConnection(); var stmt=OracleCalls.cursor(conn,
                "PKG_ESPACIO_ESTUDIANTE.metas_usuario", userId)) {
            try (ResultSet rs=OracleCalls.rows(stmt)) {
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
