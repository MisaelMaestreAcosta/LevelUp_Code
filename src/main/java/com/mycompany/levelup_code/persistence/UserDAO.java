package com.mycompany.levelup_code.persistence;

import com.mycompany.levelup_code.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO implements GenericDAO<User, Integer> {

    @Override
    public User insert(User user) {
        String sql = "INSERT INTO USERS (username, email, password_hash, role_id, total_exp, level_num, current_streak, max_streak, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setInt(4, 1);
            stmt.setInt(5, user.getTotalExp());
            stmt.setInt(6, user.getLevel());
            stmt.setInt(7, user.getCurrentStreak());
            stmt.setInt(8, user.getMaxStreak());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public Optional<User> findById(Integer id) {
        String sql = "SELECT * FROM USERS WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM USERS";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE USERS SET total_exp = ?, level_num = ?, current_streak = ?, max_streak = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user.getTotalExp());
            stmt.setInt(2, user.getLevel());
            stmt.setInt(3, user.getCurrentStreak());
            stmt.setInt(4, user.getMaxStreak());
            stmt.setInt(5, user.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM USERS WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setTotalExp(rs.getInt("total_exp"));
        u.setLevel(rs.getInt("level_num"));
        u.setCurrentStreak(rs.getInt("current_streak"));
        u.setMaxStreak(rs.getInt("max_streak"));
        return u;
    }
}