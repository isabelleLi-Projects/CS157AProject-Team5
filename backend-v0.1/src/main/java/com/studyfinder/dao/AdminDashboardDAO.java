package com.studyfinder.dao;

import com.studyfinder.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Read/write database operations used only by the administrator dashboard. */
public class AdminDashboardDAO {
    private List<Map<String, Object>> rows(String sql) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement stmt = con.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                result.add(row);
            }
        }
        return result;
    }

    public Map<String, Integer> getCounts() throws SQLException {
        Map<String, Integer> counts = new LinkedHashMap<>();
        try (Connection con = DatabaseConnection.getConnection(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(
                "SELECT (SELECT COUNT(*) FROM study_spots WHERE is_active = 1) active_spots, "
              + "(SELECT COUNT(*) FROM reports) reports, "
              + "(SELECT COUNT(*) FROM reviews WHERE is_visible = 0) reviews, "
              + "(SELECT COUNT(*) FROM users) users, "
              + "(SELECT COUNT(*) FROM redeems) redemptions")) {
            if (rs.next()) {
                counts.put("activeSpots", rs.getInt("active_spots"));
                counts.put("reports", rs.getInt("reports"));
                counts.put("reviews", rs.getInt("reviews"));
                counts.put("users", rs.getInt("users"));
                counts.put("redemptions", rs.getInt("redemptions"));
            }
        }
        return counts;
    }

    public List<Map<String, Object>> getReports() throws SQLException {
        return rows("SELECT r.report_id id, CONCAT('Report on ', s.spot_name) label, u.full_name secondary, "
                + "DATE_FORMAT(r.created_at, '%Y-%m-%d %H:%i') meta, 'Review' status "
                + "FROM reports r JOIN study_spots s ON s.spot_id = r.spot_id JOIN users u ON u.user_id = r.user_id "
                + "ORDER BY r.created_at DESC LIMIT 100");
    }

    public List<Map<String, Object>> getReviews() throws SQLException {
        return rows("SELECT rv.review_id id, CONCAT('Review of ', s.spot_name) label, u.full_name secondary, "
                + "DATE_FORMAT(rv.created_at, '%Y-%m-%d %H:%i') meta, "
                + "CASE WHEN rv.is_visible = 1 THEN 'Visible' ELSE 'Flagged' END status "
                + "FROM reviews rv JOIN study_spots s ON s.spot_id = rv.spot_id JOIN users u ON u.user_id = rv.user_id "
                + "ORDER BY rv.created_at DESC LIMIT 100");
    }

    public List<Map<String, Object>> getUsers() throws SQLException {
        return rows("SELECT user_id id, full_name label, email secondary, account_status meta, 'User' status "
                + "FROM users ORDER BY created_at DESC LIMIT 100");
    }

    public List<Map<String, Object>> getActivityLogs() throws SQLException {
        return rows("SELECT l.log_id id, l.action_type label, u.full_name secondary, "
                + "DATE_FORMAT(l.created_at, '%Y-%m-%d %H:%i') meta, 'Logged' status "
                + "FROM activity_logs l JOIN users u ON u.user_id = l.user_id ORDER BY l.created_at DESC LIMIT 100");
    }

    public List<Map<String, Object>> getRecentRedemptions() throws SQLException {
        return rows("SELECT r.redemption_id id, rw.reward_name label, u.full_name secondary, "
                + "DATE_FORMAT(r.redeemed_at, '%Y-%m-%d %H:%i') meta, r.status status "
                + "FROM redeems r JOIN rewards rw ON rw.reward_id = r.reward_id JOIN users u ON u.user_id = r.user_id "
                + "ORDER BY r.redeemed_at DESC LIMIT 10");
    }

    public List<Map<String, Object>> getSpotClicks() throws SQLException {
        return rows("SELECT s.spot_name spot, COUNT(v.view_id) clicks FROM study_spots s "
                + "LEFT JOIN study_spot_views v ON v.spot_id = s.spot_id WHERE s.is_active = 1 "
                + "GROUP BY s.spot_id, s.spot_name ORDER BY clicks DESC LIMIT 10");
    }

    public void addReward(String name, String description, int points, int stock) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement stmt = con.prepareStatement(
                "INSERT INTO rewards (reward_name, description, points_required, stock_quantity, is_active) VALUES (?, ?, ?, ?, 1)")) {
            stmt.setString(1, name); stmt.setString(2, description); stmt.setInt(3, points); stmt.setInt(4, stock); stmt.executeUpdate();
        }
    }

    public void addStudySpot(String name, String building, String location, String accessType, int capacity, double latitude, double longitude) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement buildingStmt = con.prepareStatement(
                    "INSERT INTO buildings (acronym, building_name) VALUES (?, ?) ON DUPLICATE KEY UPDATE building_id = LAST_INSERT_ID(building_id)", Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement spotStmt = con.prepareStatement(
                    "INSERT INTO study_spots (building_id, spot_name, location_details, access_type, capacity, is_active, created_at, latitude, longitude) "
                  + "VALUES (LAST_INSERT_ID(), ?, ?, ?, ?, 1, CURRENT_TIMESTAMP, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                buildingStmt.setString(1, building); buildingStmt.setString(2, building); buildingStmt.executeUpdate();
                spotStmt.setString(1, name); spotStmt.setString(2, location); spotStmt.setString(3, accessType); spotStmt.setInt(4, capacity); spotStmt.setDouble(5, latitude); spotStmt.setDouble(6, longitude); spotStmt.executeUpdate();
                con.commit();
            } catch (SQLException e) { con.rollback(); throw e; }
            finally { con.setAutoCommit(true); }
        }
    }
}
