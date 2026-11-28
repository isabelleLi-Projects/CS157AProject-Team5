package com.studyfinder.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.studyfinder.database.DatabaseConnection;

/**
 * Students suggest study spots; administrators approve or reject them.
 * Uses study_spots.suggested_by and study_spots.suggestion_status
 * (sql/migrations/add_spot_suggestions.sql).
 */
public class SpotSuggestionDAO {

    /** Points a student earns when an administrator approves their spot. */
    public static final int APPROVAL_POINTS = 15;

    /** A student can have at most this many suggestions waiting for review. */
    public static final int MAX_PENDING_PER_STUDENT = 3;

    /** Buildings for the suggestion form's dropdown. */
    public List<Map<String, Object>> getBuildings() throws SQLException {
        List<Map<String, Object>> buildings = new ArrayList<>();
        String sql = "SELECT building_id, acronym, building_name FROM buildings ORDER BY acronym";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> building = new LinkedHashMap<>();
                building.put("id", rs.getInt("building_id"));
                building.put("acronym", rs.getString("acronym"));
                building.put("name", rs.getString("building_name"));
                buildings.add(building);
            }
        }
        return buildings;
    }

    /**
     * Saves a student's suggestion as a hidden spot (is_active = 0, status 'pending'),
     * its amenities, and an activity log row, in one transaction.
     *
     * @return null on success, otherwise a message to show the student
     */
    public String createSuggestion(int studentId, String name, int buildingId, String location, String accessType,
            int capacity, double latitude, double longitude, String description, List<String> amenityNames)
            throws SQLException {
        String studentSql = "SELECT user_id FROM students WHERE user_id = ?";
        String pendingSql = "SELECT COUNT(*) AS pending FROM study_spots "
                + "WHERE suggested_by = ? AND suggestion_status = 'pending'";
        String insertSpot = "INSERT INTO study_spots (building_id, spot_name, location_details, access_type, capacity, "
                + "description, is_active, created_at, latitude, longitude, suggested_by, suggestion_status) "
                + "VALUES (?, ?, ?, ?, ?, ?, 0, NOW(), ?, ?, ?, 'pending')";
        String insertProvides = "INSERT INTO provides (spot_id, amenity_id) "
                + "SELECT ?, amenity_id FROM amenities WHERE amenity_name = ? AND is_active = 1";
        String insertLog = "INSERT INTO activity_logs (user_id, action_type, target_type, target_id, details, created_at) "
                + "VALUES (?, 'suggest_spot', 'spot', ?, ?, NOW())";

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement studentStmt = con.prepareStatement(studentSql);
                    PreparedStatement pendingStmt = con.prepareStatement(pendingSql);
                    PreparedStatement spotStmt = con.prepareStatement(insertSpot, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement providesStmt = con.prepareStatement(insertProvides);
                    PreparedStatement logStmt = con.prepareStatement(insertLog)) {

                studentStmt.setInt(1, studentId);
                try (ResultSet rs = studentStmt.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return "Only students can suggest study spots.";
                    }
                }

                pendingStmt.setInt(1, studentId);
                try (ResultSet rs = pendingStmt.executeQuery()) {
                    if (rs.next() && rs.getInt("pending") >= MAX_PENDING_PER_STUDENT) {
                        con.rollback();
                        return "You already have " + MAX_PENDING_PER_STUDENT
                                + " spots waiting for review. Please wait for an admin to review them.";
                    }
                }

                spotStmt.setInt(1, buildingId);
                spotStmt.setString(2, name);
                spotStmt.setString(3, location);
                spotStmt.setString(4, accessType);
                spotStmt.setInt(5, capacity);
                spotStmt.setString(6, description);
                spotStmt.setDouble(7, latitude);
                spotStmt.setDouble(8, longitude);
                spotStmt.setInt(9, studentId);
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

                logStmt.setInt(1, studentId);
                logStmt.setInt(2, spotId);
                logStmt.setString(3, "Suggested a new study spot: " + name + ".");
                logStmt.executeUpdate();

                con.commit();
                return null;
            } catch (SQLException exception) {
                con.rollback();
                throw exception;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /** Suggestions waiting for an administrator, newest first, with who suggested them and their amenities. */
    public List<Map<String, Object>> getPendingSuggestions() throws SQLException {
        String sql = "SELECT s.spot_id id, s.spot_name label, "
                + "CONCAT(COALESCE(b.acronym, 'Outdoor'), ' · ', s.location_details) secondary, "
                + "CONCAT('Suggested by ', u.full_name, ' on ', DATE_FORMAT(s.created_at, '%Y-%m-%d %H:%i')) meta, "
                + "CONCAT_WS(' | ', s.access_type, CONCAT('Seats ', s.capacity), "
                + "CONCAT(s.latitude, ', ', s.longitude), s.description, "
                + "(SELECT GROUP_CONCAT(a.amenity_name ORDER BY a.amenity_name SEPARATOR ', ') "
                + " FROM provides p JOIN amenities a ON a.amenity_id = p.amenity_id WHERE p.spot_id = s.spot_id)) details "
                + "FROM study_spots s "
                + "JOIN users u ON u.user_id = s.suggested_by "
                + "LEFT JOIN buildings b ON b.building_id = s.building_id "
                + "WHERE s.suggestion_status = 'pending' "
                + "ORDER BY s.created_at DESC, s.spot_id DESC";
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    /**
     * Approves or rejects a pending suggestion in one transaction.
     * Approve: the spot goes live, reuses its building's photo if it has none, and the
     * student earns APPROVAL_POINTS. Reject: the spot stays hidden.
     *
     * @return false when the spot is not a pending suggestion
     */
    public boolean reviewSuggestion(int adminId, int spotId, boolean approve) throws SQLException {
        String pendingSql = "SELECT suggested_by, building_id, photo_path FROM study_spots "
                + "WHERE spot_id = ? AND suggestion_status = 'pending' FOR UPDATE";
        String buildingPhotoSql = "SELECT photo_path FROM study_spots "
                + "WHERE building_id = ? AND is_active = 1 AND photo_path IS NOT NULL AND photo_path <> '' "
                + "ORDER BY created_at DESC, spot_id DESC LIMIT 1";
        String approveSql = "UPDATE study_spots SET is_active = 1, suggestion_status = 'approved', photo_path = ? "
                + "WHERE spot_id = ?";
        String rejectSql = "UPDATE study_spots SET suggestion_status = 'rejected' WHERE spot_id = ?";
        String pointsSql = "UPDATE students SET points = points + ? WHERE user_id = ?";
        String logSql = "INSERT INTO activity_logs (user_id, action_type, target_type, target_id, details, created_at) "
                + "VALUES (?, ?, 'spot', ?, ?, NOW())";

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement pendingStmt = con.prepareStatement(pendingSql);
                    PreparedStatement photoStmt = con.prepareStatement(buildingPhotoSql);
                    PreparedStatement approveStmt = con.prepareStatement(approveSql);
                    PreparedStatement rejectStmt = con.prepareStatement(rejectSql);
                    PreparedStatement pointsStmt = con.prepareStatement(pointsSql);
                    PreparedStatement logStmt = con.prepareStatement(logSql)) {

                int studentId;
                Integer buildingId;
                String photoPath;
                pendingStmt.setInt(1, spotId);
                try (ResultSet rs = pendingStmt.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }
                    studentId = rs.getInt("suggested_by");
                    buildingId = (Integer) rs.getObject("building_id");
                    photoPath = rs.getString("photo_path");
                }

                if (approve) {
                    if ((photoPath == null || photoPath.isBlank()) && buildingId != null) {
                        photoStmt.setInt(1, buildingId);
                        try (ResultSet rs = photoStmt.executeQuery()) {
                            if (rs.next()) photoPath = rs.getString("photo_path");
                        }
                    }
                    approveStmt.setString(1, photoPath);
                    approveStmt.setInt(2, spotId);
                    approveStmt.executeUpdate();

                    pointsStmt.setInt(1, APPROVAL_POINTS);
                    pointsStmt.setInt(2, studentId);
                    pointsStmt.executeUpdate();
                } else {
                    rejectStmt.setInt(1, spotId);
                    rejectStmt.executeUpdate();
                }

                logStmt.setInt(1, adminId);
                logStmt.setString(2, approve ? "approve_spot" : "reject_spot");
                logStmt.setInt(3, spotId);
                logStmt.setString(4, (approve ? "Approved" : "Rejected") + " study spot " + spotId
                        + " suggested by student " + studentId
                        + (approve ? " (+" + APPROVAL_POINTS + " points)." : "."));
                logStmt.executeUpdate();

                con.commit();
                return true;
            } catch (SQLException exception) {
                con.rollback();
                throw exception;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}
