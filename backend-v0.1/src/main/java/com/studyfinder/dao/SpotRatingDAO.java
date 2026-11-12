package com.studyfinder.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.studyfinder.database.DatabaseConnection;

/**
 * Data for a spot's combined rating (review stars + report accuracy) and its typical
 * conditions by time of day. Hidden reviews and reports an admin made inactive are skipped.
 */
public class SpotRatingDAO {

    /** Review stars (1-5) only; reviews.rating is varchar and can be empty in old sample data. */
    private static final String VALID_STARS = "rv.rating REGEXP '^[1-5]$'";

    /** {avgStars, reviewCount, confirmed, disputed} for one spot. avgStars is 0 when there are no reviews. */
    public double[] getRatingData(int spotId) throws SQLException {
        Map<Integer, double[]> all = query("WHERE s.spot_id = ?", spotId);
        return all.getOrDefault(spotId, new double[] { 0, 0, 0, 0 });
    }

    /** spot_id -> {avgStars, reviewCount, confirmed, disputed} for every active spot. */
    public Map<Integer, double[]> getAllRatingData() throws SQLException {
        return query("WHERE s.is_active = 1", null);
    }

    private Map<Integer, double[]> query(String where, Integer spotId) throws SQLException {
        String sql = "SELECT s.spot_id, "
                + "(SELECT AVG(CAST(rv.rating AS UNSIGNED)) FROM reviews rv "
                + "  WHERE rv.spot_id = s.spot_id AND rv.is_visible = 1 AND " + VALID_STARS + ") AS avg_stars, "
                + "(SELECT COUNT(*) FROM reviews rv "
                + "  WHERE rv.spot_id = s.spot_id AND rv.is_visible = 1 AND " + VALID_STARS + ") AS review_count, "
                + "(SELECT COUNT(*) FROM verifies v JOIN reports r ON r.report_id = v.report_id "
                + "  WHERE r.spot_id = s.spot_id AND v.result = 'confirmed' AND NOT " + AdminDashboardDAO.REPORT_HIDDEN + ") AS confirmed, "
                + "(SELECT COUNT(*) FROM verifies v JOIN reports r ON r.report_id = v.report_id "
                + "  WHERE r.spot_id = s.spot_id AND v.result = 'disputed' AND NOT " + AdminDashboardDAO.REPORT_HIDDEN + ") AS disputed "
                + "FROM study_spots s " + where;
        Map<Integer, double[]> result = new HashMap<>();
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement stmt = con.prepareStatement(sql)) {
            if (spotId != null) stmt.setInt(1, spotId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt("spot_id"), new double[] {
                            rs.getDouble("avg_stars"), rs.getInt("review_count"),
                            rs.getInt("confirmed"), rs.getInt("disputed") });
                }
            }
        }
        return result;
    }

    /** Each visible report for the spot as {time block, noise_level, crowdedness}. */
    public List<String[]> getReportsByTimeOfDay(int spotId) throws SQLException {
        String sql = "SELECT " + TrendsDAO.timeBlock("r.created_at") + " AS block, r.noise_level, r.crowdedness "
                + "FROM reports r WHERE r.spot_id = ? AND NOT " + AdminDashboardDAO.REPORT_HIDDEN;
        List<String[]> rows = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, spotId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[] { rs.getString("block"), rs.getString("noise_level"), rs.getString("crowdedness") });
                }
            }
        }
        return rows;
    }
}
