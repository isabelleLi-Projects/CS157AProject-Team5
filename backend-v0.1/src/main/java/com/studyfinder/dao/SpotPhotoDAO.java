package com.studyfinder.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.studyfinder.database.DatabaseConnection;

/** Photos students add to study spots (table spot_photos, sql/migrations/add_spot_photos.sql). */
public class SpotPhotoDAO {

    /** A student can have at most this many visible photos on one spot. */
    public static final int MAX_PHOTOS_PER_STUDENT_PER_SPOT = 5;

    /**
     * Saves a photo row after checking the rules, in one transaction with its activity log row.
     *
     * @return null on success, otherwise a message to show the student
     */
    public String addPhoto(int userId, int spotId, double latitude, double longitude, String fileName)
            throws SQLException {
        String spotSql = "SELECT spot_id FROM study_spots WHERE spot_id = ? AND is_active = 1";
        String studentSql = "SELECT user_id FROM students WHERE user_id = ?";
        String countSql = "SELECT COUNT(*) AS photos FROM spot_photos WHERE user_id = ? AND spot_id = ? AND is_visible = 1";
        String insertSql = "INSERT INTO spot_photos (spot_id, user_id, photo_path, uploaded_at, is_visible) "
                + "VALUES (?, ?, ?, NOW(), 1)";
        String logSql = "INSERT INTO activity_logs (user_id, action_type, target_type, target_id, details, created_at) "
                + "VALUES (?, 'add_photo', 'spot', ?, ?, NOW())";

        try (Connection con = DatabaseConnection.getConnection()) {
            if (!new SpotDetailDAO().isWithinActionDistance(con, spotId, latitude, longitude)) {
                return "You must be within 0.3 miles of the study spot to add a photo.";
            }
            con.setAutoCommit(false);
            try (PreparedStatement spotStmt = con.prepareStatement(spotSql);
                    PreparedStatement studentStmt = con.prepareStatement(studentSql);
                    PreparedStatement countStmt = con.prepareStatement(countSql);
                    PreparedStatement insertStmt = con.prepareStatement(insertSql);
                    PreparedStatement logStmt = con.prepareStatement(logSql)) {
                spotStmt.setInt(1, spotId);
                try (ResultSet rs = spotStmt.executeQuery()) {
                    if (!rs.next()) { con.rollback(); return "Study spot not found."; }
                }
                studentStmt.setInt(1, userId);
                try (ResultSet rs = studentStmt.executeQuery()) {
                    if (!rs.next()) { con.rollback(); return "Only students can add photos."; }
                }
                countStmt.setInt(1, userId);
                countStmt.setInt(2, spotId);
                try (ResultSet rs = countStmt.executeQuery()) {
                    if (rs.next() && rs.getInt("photos") >= MAX_PHOTOS_PER_STUDENT_PER_SPOT) {
                        con.rollback();
                        return "You can add up to " + MAX_PHOTOS_PER_STUDENT_PER_SPOT + " photos per spot.";
                    }
                }
                insertStmt.setInt(1, spotId);
                insertStmt.setInt(2, userId);
                insertStmt.setString(3, fileName);
                insertStmt.executeUpdate();
                logStmt.setInt(1, userId);
                logStmt.setInt(2, spotId);
                logStmt.setString(3, "Added a photo to study spot " + spotId + ".");
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

    /** Visible photos for a spot, newest first: photoPath, firstName, uploadedAt. */
    public List<Map<String, Object>> getVisiblePhotos(int spotId) throws SQLException {
        String sql = "SELECT p.photo_path AS photoPath, SUBSTRING_INDEX(u.full_name, ' ', 1) AS firstName, "
                + "DATE_FORMAT(p.uploaded_at, '%Y-%m-%d') AS uploadedAt "
                + "FROM spot_photos p JOIN users u ON u.user_id = p.user_id "
                + "WHERE p.spot_id = ? AND p.is_visible = 1 ORDER BY p.uploaded_at DESC, p.photo_id DESC LIMIT 24";
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, spotId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("photoPath", rs.getString("photoPath"));
                    row.put("firstName", rs.getString("firstName"));
                    row.put("uploadedAt", rs.getString("uploadedAt"));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    /** Recent photos (visible and hidden) for the admin's Student Photos tab. */
    public List<Map<String, Object>> getRecentPhotosForAdmin() throws SQLException {
        String sql = "SELECT p.photo_id AS id, s.spot_name AS label, CONCAT('Added by ', u.full_name) AS secondary, "
                + "DATE_FORMAT(p.uploaded_at, '%Y-%m-%d %H:%i') AS meta, "
                + "CASE WHEN p.is_visible = 1 THEN 'Visible' ELSE 'Hidden' END AS status, p.is_visible AS active, "
                + "p.photo_path AS img "
                + "FROM spot_photos p JOIN study_spots s ON s.spot_id = p.spot_id JOIN users u ON u.user_id = p.user_id "
                + "ORDER BY p.uploaded_at DESC, p.photo_id DESC LIMIT 100";
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
}
