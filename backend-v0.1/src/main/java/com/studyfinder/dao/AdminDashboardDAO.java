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
        addStudySpot(name, buildingId, location, accessType, capacity, latitude, longitude, photoPath, List.of(), null);
    }

    /**
     * Adds a study spot, its amenities (provides) and its building's hours (building_hours)
     * in one transaction, so either everything is saved or nothing is.
     *
     * @param amenityNames names from the form's checkboxes; names not in amenities are skipped
     * @param hours day name -> {opening "HH:mm", closing "HH:mm"}, or null value for a closed day.
     *              Pass null to leave building_hours unchanged (for example an outdoor spot).
     */
    public void addStudySpot(String name, Integer buildingId, String location, String accessType, int capacity,
            double latitude, double longitude, String photoPath, List<String> amenityNames,
            Map<String, String[]> hours) throws SQLException {
        String insertSpot =
            "INSERT INTO study_spots "
            + "(building_id, spot_name, location_details, "
            + "access_type, capacity, is_active, created_at, "
            + "latitude, longitude, photo_path) "
            + "VALUES (?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP, ?, ?, ?)";
        // Link the new spot to each checked amenity (looked up by name).
        String insertProvides =
            "INSERT INTO provides (spot_id, amenity_id) "
            + "SELECT ?, amenity_id FROM amenities WHERE amenity_name = ? AND is_active = 1";
        // Hours belong to the building: add the day, or update it if it already exists.
        String upsertHours =
            "INSERT INTO building_hours (building_id, day_of_week, opening_time, closing_time) "
            + "VALUES (?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE opening_time = VALUES(opening_time), closing_time = VALUES(closing_time)";
        // A day marked Closed has no row.
        String deleteHours = "DELETE FROM building_hours WHERE building_id = ? AND day_of_week = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement spotStmt = con.prepareStatement(insertSpot, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement providesStmt = con.prepareStatement(insertProvides);
             PreparedStatement upsertStmt = con.prepareStatement(upsertHours);
             PreparedStatement deleteStmt = con.prepareStatement(deleteHours)) {

            con.setAutoCommit(false);

            try {
                // Stores NULL when the spot is outdoors
                spotStmt.setObject(1, buildingId, java.sql.Types.INTEGER);
                spotStmt.setString(2, name);
                spotStmt.setString(3, location);
                spotStmt.setString(4, accessType);
                spotStmt.setInt(5, capacity);
                spotStmt.setDouble(6, latitude);
                spotStmt.setDouble(7, longitude);
                spotStmt.setString(8, photoPath);
                spotStmt.executeUpdate();

                int spotId;
                try (ResultSet keys = spotStmt.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("New study spot id was not returned");
                    spotId = keys.getInt(1);
                }

                for (String amenity : amenityNames) {
                    providesStmt.setInt(1, spotId);
                    providesStmt.setString(2, amenity);
                    providesStmt.executeUpdate();
                }

                if (buildingId != null && hours != null) {
                    for (Map.Entry<String, String[]> day : hours.entrySet()) {
                        if (day.getValue() == null) {
                            deleteStmt.setInt(1, buildingId);
                            deleteStmt.setString(2, day.getKey());
                            deleteStmt.executeUpdate();
                        } else {
                            upsertStmt.setInt(1, buildingId);
                            upsertStmt.setString(2, day.getKey());
                            upsertStmt.setString(3, day.getValue()[0]);
                            upsertStmt.setString(4, day.getValue()[1]);
                            upsertStmt.executeUpdate();
                        }
                    }
                }

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
