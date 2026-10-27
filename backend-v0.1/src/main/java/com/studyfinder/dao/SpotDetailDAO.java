package com.studyfinder.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.studyfinder.database.DatabaseConnection;
import com.studyfinder.model.SpotDetail;

/** Database access for the Study Spot page (Functional Requirement #4). */
public class SpotDetailDAO {

    /** MySQL error code for a duplicate primary/unique key. */
    private static final int DUPLICATE_KEY = 1062;

    /**
     * Loads one active spot with its building, amenities, last 5 reports and visible reviews.
     * Returns null when the spot does not exist or is inactive.
     */
    public SpotDetail getSpotDetail(int spotId) throws SQLException {
        // Spot info joined with its building (full name + acronym). Only active spots.
        String spotSql = "SELECT s.spot_id, s.building_id, s.spot_name, s.location_details, "
                + "s.description, s.capacity, s.access_type, s.photo_path, s.latitude, s.longitude, "
                + "b.building_name, b.acronym "
                + "FROM study_spots s "
                + "LEFT JOIN buildings b ON b.building_id = s.building_id "
                + "WHERE s.spot_id = ? AND s.is_active = 1";

        // Active amenities this spot provides, with the spot-specific note from provides.
        String amenitySql = "SELECT a.amenity_name, a.description, p.notes "
                + "FROM provides p "
                + "JOIN amenities a ON a.amenity_id = p.amenity_id "
                + "WHERE p.spot_id = ? AND a.is_active = 1 "
                + "ORDER BY a.amenity_name";

        // The 5 newest reports for this spot, each with how many students confirmed/disputed it.
        // TIMESTAMPDIFF uses the database clock so "X min ago" matches when the row was written.
        String reportSql = "SELECT r.report_id, r.user_id, "
                + "SUBSTRING_INDEX(u.full_name, ' ', 1) AS first_name, "
                + "r.noise_level, r.crowdedness, r.outlets_available, r.smell_level, r.wifi_quality, r.notes, "
                + "DATE_FORMAT(r.created_at, '%Y-%m-%d %H:%i') AS created_at, "
                + "TIMESTAMPDIFF(MINUTE, r.created_at, NOW()) AS minutes_ago, "
                + "SUM(CASE WHEN v.result = 'confirmed' THEN 1 ELSE 0 END) AS confirmed, "
                + "SUM(CASE WHEN v.result = 'disputed' THEN 1 ELSE 0 END) AS disputed "
                + "FROM reports r "
                + "JOIN users u ON u.user_id = r.user_id "
                + "LEFT JOIN verifies v ON v.report_id = r.report_id "
                + "WHERE r.spot_id = ? "
                + "GROUP BY r.report_id, r.user_id, u.full_name, r.noise_level, r.crowdedness, "
                + "r.outlets_available, r.smell_level, r.wifi_quality, r.notes, r.created_at "
                + "ORDER BY r.created_at DESC, r.report_id DESC "
                + "LIMIT 5";

        // Visible reviews for this spot with the reviewer's first name (read-only here).
        String reviewSql = "SELECT SUBSTRING_INDEX(u.full_name, ' ', 1) AS first_name, "
                + "rv.review_text, DATE_FORMAT(rv.created_at, '%Y-%m-%d') AS created_at "
                + "FROM reviews rv "
                + "JOIN users u ON u.user_id = rv.user_id "
                + "WHERE rv.spot_id = ? AND rv.is_visible = 1 "
                + "ORDER BY rv.created_at DESC";

        try (Connection con = DatabaseConnection.getConnection()) {
            SpotDetail spot;
            try (PreparedStatement stmt = con.prepareStatement(spotSql)) {
                stmt.setInt(1, spotId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) return null;
                    spot = new SpotDetail();
                    spot.setSpotId(rs.getInt("spot_id"));
                    spot.setBuildingId(rs.getInt("building_id"));
                    spot.setName(rs.getString("spot_name"));
                    spot.setLocationDetails(rs.getString("location_details"));
                    spot.setDescription(rs.getString("description"));
                    spot.setCapacity(rs.getInt("capacity"));
                    spot.setAccessType(rs.getString("access_type"));
                    spot.setPhotoPath(rs.getString("photo_path"));
                    spot.setLatitude(rs.getDouble("latitude"));
                    spot.setLongitude(rs.getDouble("longitude"));
                    spot.setBuildingName(rs.getString("building_name"));
                    spot.setBuildingAcronym(rs.getString("acronym"));
                }
            }

            try (PreparedStatement stmt = con.prepareStatement(amenitySql)) {
                stmt.setInt(1, spotId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        spot.getAmenities().add(new SpotDetail.Amenity(
                                rs.getString("amenity_name"), rs.getString("description"), rs.getString("notes")));
                    }
                }
            }

            try (PreparedStatement stmt = con.prepareStatement(reportSql)) {
                stmt.setInt(1, spotId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int outlets = rs.getInt("outlets_available");
                        spot.getRecentReports().add(new SpotDetail.Report(
                                rs.getInt("report_id"),
                                rs.getInt("user_id"),
                                rs.getString("first_name"),
                                rs.getString("noise_level"),
                                rs.getString("crowdedness"),
                                rs.wasNull() ? null : outlets,
                                rs.getString("smell_level"),
                                rs.getString("wifi_quality"),
                                rs.getString("notes"),
                                rs.getString("created_at"),
                                rs.getLong("minutes_ago"),
                                rs.getInt("confirmed"),
                                rs.getInt("disputed")));
                    }
                }
            }

            try (PreparedStatement stmt = con.prepareStatement(reviewSql)) {
                stmt.setInt(1, spotId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        spot.getReviews().add(new SpotDetail.Review(
                                rs.getString("first_name"), rs.getString("review_text"), rs.getString("created_at")));
                    }
                }
            }

            return spot;
        }
    }

    /**
     * Opening/closing times for a building on the given days (e.g. today and yesterday),
     * keyed by day name. Each value is {opening_time, closing_time}.
     */
    public Map<String, LocalTime[]> getBuildingHours(int buildingId, List<String> days) throws SQLException {
        // Hours for one building on two specific days. Yesterday is needed because
        // a closing_time after midnight (e.g. 07:00 -> 01:00) runs into today.
        String sql = "SELECT day_of_week, opening_time, closing_time "
                + "FROM building_hours "
                + "WHERE building_id = ? AND day_of_week IN (?, ?)";
        Map<String, LocalTime[]> hours = new LinkedHashMap<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, buildingId);
            stmt.setString(2, days.get(0));
            stmt.setString(3, days.get(1));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    hours.put(rs.getString("day_of_week"), new LocalTime[] {
                            rs.getTime("opening_time").toLocalTime(),
                            rs.getTime("closing_time").toLocalTime() });
                }
            }
        }
        return hours;
    }

    /** The current student's rating ("confirmed"/"disputed") of a report, or null if they haven't rated it. */
    public String getUserRating(int reportId, int userId) throws SQLException {
        // Has this student already verified this report? (verifies has one row per student per report.)
        String sql = "SELECT result FROM verifies WHERE report_id = ? AND user_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, reportId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("result") : null;
            }
        }
    }

    /**
     * Saves a new conditions report for a spot and logs it, in one transaction.
     * outlets is 1 (available) or 0 (none). Values are already validated by SpotService.
     *
     * @return null on success, otherwise a message to show the student
     */
    public String submitReport(int userId, int spotId, String noise, String crowdedness, int outlets,
                               String smell, String wifi, String notes) throws SQLException {
        // The spot must exist and be active.
        String spotSql = "SELECT spot_id FROM study_spots WHERE spot_id = ? AND is_active = 1";
        // Only students can report (reports.user_id references students).
        String studentSql = "SELECT user_id FROM students WHERE user_id = ?";
        // Has this student already reported this spot in the last 10 minutes? Stops spam reports.
        String recentSql = "SELECT COUNT(*) AS recent FROM reports "
                + "WHERE user_id = ? AND spot_id = ? AND created_at > NOW() - INTERVAL 10 MINUTE";
        // Save the report. It becomes the spot's "current conditions" because it is the newest.
        String insertReport = "INSERT INTO reports "
                + "(user_id, spot_id, noise_level, crowdedness, outlets_available, smell_level, "
                + "wifi_quality, notes, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        // Audit trail of the new report.
        String insertLog = "INSERT INTO activity_logs "
                + "(user_id, action_type, target_type, target_id, details, created_at) "
                + "VALUES (?, 'submit_report', 'report', ?, ?, NOW())";

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement spotStmt = con.prepareStatement(spotSql);
                 PreparedStatement studentStmt = con.prepareStatement(studentSql);
                 PreparedStatement recentStmt = con.prepareStatement(recentSql);
                 PreparedStatement reportStmt = con.prepareStatement(insertReport, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement logStmt = con.prepareStatement(insertLog)) {

                spotStmt.setInt(1, spotId);
                try (ResultSet rs = spotStmt.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return "Spot not found.";
                    }
                }

                studentStmt.setInt(1, userId);
                try (ResultSet rs = studentStmt.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return "Only students can report conditions.";
                    }
                }

                recentStmt.setInt(1, userId);
                recentStmt.setInt(2, spotId);
                try (ResultSet rs = recentStmt.executeQuery()) {
                    if (rs.next() && rs.getInt("recent") > 0) {
                        con.rollback();
                        return "You already reported this spot in the last 10 minutes.";
                    }
                }

                reportStmt.setInt(1, userId);
                reportStmt.setInt(2, spotId);
                reportStmt.setString(3, noise);
                reportStmt.setString(4, crowdedness);
                reportStmt.setInt(5, outlets);
                reportStmt.setString(6, smell);
                reportStmt.setString(7, wifi);
                reportStmt.setString(8, notes);
                reportStmt.executeUpdate();

                int reportId;
                try (ResultSet keys = reportStmt.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Report id was not returned.");
                    reportId = keys.getInt(1);
                }

                logStmt.setInt(1, userId);
                logStmt.setInt(2, reportId);
                logStmt.setString(3, "Reported conditions for spot " + spotId + ".");
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

    /**
     * Records a student's accurate/inaccurate rating of a spot's latest report, all in one transaction:
     * inserts into verifies, gives the rater +2 points, moves the author's trust score +1/-1,
     * and writes an activity log row.
     *
     * @return null on success, otherwise a message to show the student
     */
    public String rateLatestReport(int userId, int spotId, int reportId, String result) throws SQLException {
        // The newest report for the spot. Locked so the check and the insert see the same row.
        String latestSql = "SELECT report_id, user_id FROM reports "
                + "WHERE spot_id = ? "
                + "ORDER BY created_at DESC, report_id DESC LIMIT 1 FOR UPDATE";
        // Only students can rate (verifies.user_id references students).
        String studentSql = "SELECT user_id FROM students WHERE user_id = ?";
        // Save the rating: 'confirmed' = accurate, 'disputed' = inaccurate.
        String insertVerify = "INSERT INTO verifies (report_id, user_id, result, verified_at) "
                + "VALUES (?, ?, ?, NOW())";
        // Reward the rater with 2 points.
        String addPoints = "UPDATE students SET points = points + 2 WHERE user_id = ?";
        // Report author's trust score: +1 if confirmed, -1 if disputed, never below 0.
        String trustUp = "UPDATE students SET trust_score = trust_score + 1 WHERE user_id = ?";
        String trustDown = "UPDATE students SET trust_score = GREATEST(trust_score - 1, 0) WHERE user_id = ?";
        // Audit trail of the rating.
        String insertLog = "INSERT INTO activity_logs "
                + "(user_id, action_type, target_type, target_id, details, created_at) "
                + "VALUES (?, 'verify_report', 'report', ?, ?, NOW())";

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement latestStmt = con.prepareStatement(latestSql);
                 PreparedStatement studentStmt = con.prepareStatement(studentSql);
                 PreparedStatement verifyStmt = con.prepareStatement(insertVerify);
                 PreparedStatement pointsStmt = con.prepareStatement(addPoints);
                 PreparedStatement trustStmt = con.prepareStatement("confirmed".equals(result) ? trustUp : trustDown);
                 PreparedStatement logStmt = con.prepareStatement(insertLog)) {

                int authorId;
                latestStmt.setInt(1, spotId);
                try (ResultSet rs = latestStmt.executeQuery()) {
                    if (!rs.next() || rs.getInt("report_id") != reportId) {
                        con.rollback();
                        return "Conditions changed. Please rate the latest report.";
                    }
                    authorId = rs.getInt("user_id");
                }

                studentStmt.setInt(1, userId);
                try (ResultSet rs = studentStmt.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return "Only students can rate spots.";
                    }
                }

                if (authorId == userId) {
                    con.rollback();
                    return "You can't rate your own report";
                }

                verifyStmt.setInt(1, reportId);
                verifyStmt.setInt(2, userId);
                verifyStmt.setString(3, result);
                try {
                    verifyStmt.executeUpdate();
                } catch (SQLIntegrityConstraintViolationException exception) {
                    if (exception.getErrorCode() != DUPLICATE_KEY) throw exception;
                    con.rollback();
                    return "You already rated this report";
                }

                pointsStmt.setInt(1, userId);
                pointsStmt.executeUpdate();

                trustStmt.setInt(1, authorId);
                trustStmt.executeUpdate();

                logStmt.setInt(1, userId);
                logStmt.setInt(2, reportId);
                logStmt.setString(3, ("confirmed".equals(result) ? "Rated accurate" : "Rated inaccurate")
                        + " the latest report for spot " + spotId + ".");
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
}
