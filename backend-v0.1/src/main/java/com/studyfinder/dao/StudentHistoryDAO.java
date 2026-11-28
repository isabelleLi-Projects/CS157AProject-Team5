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

/**
 * Functional Requirement #8, Student history. Read-only queries over tables that already
 * exist: study_spot_views (visited), reports (reported), verifies (rated), reviews
 * (reviewed), redeems (points spent), and study_spots (suggested).
 */
public class StudentHistoryDAO {

    /**
     * Every points event for the student, newest first. Earned points follow the app's
     * rules: +2 per rating, +5 per review, +15 per approved spot suggestion. Spent points
     * come from redeems.points_spent.
     * Each row: at ("YYYY-MM-DD HH:MM"), what, item, points (negative when spent).
     */
    public List<Map<String, Object>> getPointsActivity(int userId) throws SQLException {
        String sql = "SELECT DATE_FORMAT(e.at, '%Y-%m-%d %H:%i') AS at, e.what, e.item, e.points FROM ("
                + " SELECT v.verified_at AS at, 'Rated a report' AS what, s.spot_name AS item, 2 AS points"
                + " FROM verifies v JOIN reports r ON r.report_id = v.report_id"
                + " JOIN study_spots s ON s.spot_id = r.spot_id WHERE v.user_id = ?"
                + " UNION ALL"
                + " SELECT rv.created_at, 'Wrote a review', s.spot_name, 5"
                + " FROM reviews rv JOIN study_spots s ON s.spot_id = rv.spot_id WHERE rv.user_id = ?"
                + " UNION ALL"
                // The approval time is in activity_logs (approve_spot); fall back to when it was suggested.
                + " SELECT COALESCE(l.created_at, s.created_at), 'Spot suggestion approved', s.spot_name, 15"
                + " FROM study_spots s LEFT JOIN activity_logs l"
                + " ON l.action_type = 'approve_spot' AND l.target_type = 'spot' AND l.target_id = s.spot_id"
                + " WHERE s.suggested_by = ? AND s.suggestion_status = 'approved'"
                + " UNION ALL"
                + " SELECT rd.redeemed_at, 'Redeemed a reward', rw.reward_name, -rd.points_spent"
                + " FROM redeems rd JOIN rewards rw ON rw.reward_id = rd.reward_id WHERE rd.user_id = ?"
                + ") e ORDER BY e.at DESC LIMIT 200";
        return rows(sql, userId, 4);
    }

    /**
     * Every spot the student visited, reported, rated, or reviewed, with how many times
     * and when they last did something there, most recent first.
     */
    public List<Map<String, Object>> getSpotHistory(int userId) throws SQLException {
        String sql = "SELECT s.spot_id AS spotId, s.spot_name AS name, COALESCE(b.acronym, 'Outdoor') AS building, "
                + "s.is_active AS active, "
                + "SUM(e.kind = 'visit') AS visits, SUM(e.kind = 'report') AS reports, "
                + "SUM(e.kind = 'rate') AS ratings, SUM(e.kind = 'review') AS reviews, "
                + "DATE_FORMAT(MAX(e.at), '%Y-%m-%d %H:%i') AS lastAt "
                + "FROM ("
                + " SELECT spot_id, 'visit' AS kind, viewed_at AS at FROM study_spot_views WHERE user_id = ?"
                + " UNION ALL SELECT spot_id, 'report', created_at FROM reports WHERE user_id = ?"
                + " UNION ALL SELECT r.spot_id, 'rate', v.verified_at FROM verifies v"
                + "   JOIN reports r ON r.report_id = v.report_id WHERE v.user_id = ?"
                + " UNION ALL SELECT spot_id, 'review', created_at FROM reviews WHERE user_id = ?"
                + ") e "
                + "JOIN study_spots s ON s.spot_id = e.spot_id "
                + "LEFT JOIN buildings b ON b.building_id = s.building_id "
                + "GROUP BY s.spot_id, s.spot_name, b.acronym, s.is_active "
                + "ORDER BY MAX(e.at) DESC";
        return rows(sql, userId, 4);
    }

    /** Spots the student suggested and whether they are waiting, approved, or rejected. */
    public List<Map<String, Object>> getSuggestions(int userId) throws SQLException {
        String sql = "SELECT s.spot_name AS name, COALESCE(b.acronym, 'Outdoor') AS building, "
                + "s.suggestion_status AS status, DATE_FORMAT(s.created_at, '%Y-%m-%d %H:%i') AS at "
                + "FROM study_spots s LEFT JOIN buildings b ON b.building_id = s.building_id "
                + "WHERE s.suggested_by = ? ORDER BY s.created_at DESC";
        return rows(sql, userId, 1);
    }

    /** Runs a query whose only parameters are the same user id, repeated `times` times. */
    private List<Map<String, Object>> rows(String sql, int userId, int times) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement stmt = con.prepareStatement(sql)) {
            for (int i = 1; i <= times; i++) stmt.setInt(i, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                    }
                    result.add(row);
                }
            }
        }
        return result;
    }
}
