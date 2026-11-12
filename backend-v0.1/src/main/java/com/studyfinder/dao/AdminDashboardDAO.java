package com.studyfinder.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.studyfinder.database.DatabaseConnection;

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

    public void addReward(String name, String description, int points, int stock, String photoPath) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement stmt = con.prepareStatement(
                "INSERT INTO rewards (reward_name, description, points_required, stock_quantity, is_active, photo_path) VALUES (?, ?, ?, ?, 1, ?)")) {
            stmt.setString(1, name); 
            stmt.setString(2, description); 
            stmt.setInt(3, points); 
            stmt.setInt(4, stock); 
            stmt.setString(5, photoPath); 
            stmt.executeUpdate();
        }
    }

    public void addStudySpot(String name, Integer buildingId, String location, String accessType, int capacity, double latitude, double longitude, String photoPath) throws SQLException {
    String sql =
        "INSERT INTO study_spots "
        + "(building_id, spot_name, location_details, "
        + "access_type, capacity, is_active, created_at, "
        + "latitude, longitude, photo_path) "
        + "VALUES (?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP, ?, ?, ?)";

    try (Connection con = DatabaseConnection.getConnection();
         PreparedStatement stmt = con.prepareStatement(sql)) {

        con.setAutoCommit(false);

        try {
            // Stores NULL when the spot is outdoors
            stmt.setObject(
                1,
                buildingId,
                java.sql.Types.INTEGER
            );

            stmt.setString(2, name);
            stmt.setString(3, location);
            stmt.setString(4, accessType);
            stmt.setInt(5, capacity);
            stmt.setDouble(6, latitude);
            stmt.setDouble(7, longitude);
            stmt.setString(8, photoPath);

            stmt.executeUpdate();

            con.commit();

        } catch (SQLException exception) {
            con.rollback();
            throw exception;

        } finally {
            con.setAutoCommit(true);
        }
    }
}
    public List<Map<String, Object>> getBuildings() throws SQLException {
        List<Map<String, Object>> buildings = new ArrayList<>();

        // photo_path: the newest photo of an existing spot in the same building, used to
        // autofill the photo on the Add Study Spot form (buildings has no photo column).
        String sql =
            "SELECT b.building_id, b.acronym, " +
            "(SELECT s.photo_path FROM study_spots s " +
            " WHERE s.building_id = b.building_id AND s.photo_path IS NOT NULL AND s.photo_path <> '' " +
            " ORDER BY s.created_at DESC, s.spot_id DESC LIMIT 1) AS photo_path " +
            "FROM buildings b ORDER BY b.acronym";

        try (Connection con = DatabaseConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> building = new HashMap<>();
                building.put("id", rs.getInt("building_id"));
                building.put("name", rs.getString("acronym"));
                building.put("photoPath", rs.getString("photo_path"));
                buildings.add(building);
            }
        }
        return buildings;
    }

    /**
     * Saved opening/closing times for every building, used to autofill the hours on the
     * Add Study Spot form. Each row: buildingId, day ("Monday"...), opening/closing as "HH:mm".
     */
    public List<Map<String, Object>> getAllBuildingHours() throws SQLException {
        return rows("SELECT building_id AS buildingId, day_of_week AS day, "
                + "TIME_FORMAT(opening_time, '%H:%i') AS opening, TIME_FORMAT(closing_time, '%H:%i') AS closing "
                + "FROM building_hours");
    }

    /**
     * Newest photo of an existing spot in the building, or null if none. The server looks
     * this up itself instead of trusting a file name sent by the browser.
     */
    public String getBuildingPhoto(int buildingId) throws SQLException {
        String sql = "SELECT photo_path FROM study_spots "
                + "WHERE building_id = ? AND photo_path IS NOT NULL AND photo_path <> '' "
                + "ORDER BY created_at DESC, spot_id DESC LIMIT 1";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, buildingId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("photo_path") : null;
            }
        }
    }
}
