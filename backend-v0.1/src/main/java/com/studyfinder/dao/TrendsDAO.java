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
 * Historical trends for the admin dashboard and time-of-day conditions for the spot page.
 * Read-only queries over reports, study_spot_views, reviews, and redeems.
 */
public class TrendsDAO {

    /** SQL that turns a datetime column into a time-of-day block. Used by trends and the spot page. */
    public static String timeBlock(String column) {
        return "CASE WHEN HOUR(" + column + ") BETWEEN 6 AND 11 THEN 'Morning' "
                + "WHEN HOUR(" + column + ") BETWEEN 12 AND 16 THEN 'Afternoon' "
                + "WHEN HOUR(" + column + ") BETWEEN 17 AND 21 THEN 'Evening' ELSE 'Night' END";
    }

    /** Number of reports per day, for the 30 most recent days that had reports, oldest first. */
    public List<Map<String, Object>> getReportsPerDay() throws SQLException {
        // One derived table groups by the plain date; the outer query formats it. Valid under ONLY_FULL_GROUP_BY.
        return rows("SELECT DATE_FORMAT(d.day, '%m-%d') AS label, d.value FROM ("
                + " SELECT DATE(created_at) AS day, COUNT(*) AS value"
                + " FROM reports GROUP BY DATE(created_at) ORDER BY DATE(created_at) DESC LIMIT 30"
                + ") d ORDER BY d.day");
    }

    /** For each time-of-day block: how many reports said Available, Moderate, or Crowded. */
    public List<Map<String, Object>> getCrowdednessByTime() throws SQLException {
        // Label each report with its time block in a derived table, then count per block.
        return rows("SELECT t.label, "
                + "SUM(t.crowdedness = 'Available') AS available, SUM(t.crowdedness = 'Moderate') AS moderate, "
                + "SUM(t.crowdedness = 'Crowded') AS crowded, COUNT(*) AS total FROM ("
                + " SELECT " + timeBlock("created_at") + " AS label, crowdedness FROM reports"
                + ") t GROUP BY t.label "
                + "ORDER BY FIELD(t.label, 'Morning', 'Afternoon', 'Evening', 'Night')");
    }

    /** The 8 active spots with the most visits + reports + reviews. */
    public List<Map<String, Object>> getMostActiveSpots() throws SQLException {
        return rows("SELECT s.spot_name AS label, "
                + "(SELECT COUNT(*) FROM study_spot_views v WHERE v.spot_id = s.spot_id) AS visits, "
                + "(SELECT COUNT(*) FROM reports r WHERE r.spot_id = s.spot_id) AS reports, "
                + "(SELECT COUNT(*) FROM reviews rv WHERE rv.spot_id = s.spot_id) AS reviews "
                + "FROM study_spots s WHERE s.is_active = 1 "
                + "ORDER BY visits + reports + reviews DESC, s.spot_name LIMIT 8");
    }

    /** Rewards redeemed and points spent per week (week starting Monday), last 12 weeks with redemptions. */
    public List<Map<String, Object>> getRedemptionsPerWeek() throws SQLException {
        return rows("SELECT DATE_FORMAT(w.week, '%m-%d') AS label, w.value, w.points FROM ("
                + " SELECT x.week, COUNT(*) AS value, SUM(x.points_spent) AS points FROM ("
                + "  SELECT DATE_SUB(DATE(redeemed_at), INTERVAL WEEKDAY(redeemed_at) DAY) AS week, points_spent"
                + "  FROM redeems WHERE redeemed_at IS NOT NULL"
                + " ) x GROUP BY x.week ORDER BY x.week DESC LIMIT 12"
                + ") w ORDER BY w.week");
    }

    private List<Map<String, Object>> rows(String sql) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                }
                result.add(row);
            }
        }
        return result;
    }
}
