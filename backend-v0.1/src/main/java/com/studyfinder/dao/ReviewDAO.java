package com.studyfinder.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.studyfinder.database.DatabaseConnection;

/** Students editing or deleting their own review. Each change is logged in activity_logs. */
public class ReviewDAO {

    /** Points a review earns; taken back when the review is deleted. */
    public static final int REVIEW_POINTS = 5;

    private static final String LOG_SQL =
            "INSERT INTO activity_logs (user_id, action_type, target_type, target_id, details, created_at) "
            + "VALUES (?, ?, 'review', ?, ?, NOW())";

    /** @return false when the review does not exist or belongs to someone else */
    public boolean updateReview(int userId, int reviewId, int rating, String text) throws SQLException {
        // user_id = ? makes sure students can only change their own review.
        String sql = "UPDATE reviews SET rating = ?, review_text = ?, created_at = NOW() "
                + "WHERE review_id = ? AND user_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement stmt = con.prepareStatement(sql);
                PreparedStatement logStmt = con.prepareStatement(LOG_SQL)) {
            con.setAutoCommit(false);
            try {
                stmt.setInt(1, rating);
                stmt.setString(2, text);
                stmt.setInt(3, reviewId);
                stmt.setInt(4, userId);
                if (stmt.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
                logStmt.setInt(1, userId);
                logStmt.setString(2, "edit_review");
                logStmt.setInt(3, reviewId);
                logStmt.setString(4, "Edited review " + reviewId + " (" + rating + " stars).");
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

    /**
     * Deletes the student's own review and takes back the points it earned (never below 0),
     * so deleting and re-posting can't be used to farm points.
     *
     * @return false when the review does not exist or belongs to someone else
     */
    public boolean deleteReview(int userId, int reviewId) throws SQLException {
        String deleteSql = "DELETE FROM reviews WHERE review_id = ? AND user_id = ?";
        String pointsSql = "UPDATE students SET points = GREATEST(points - ?, 0) WHERE user_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement deleteStmt = con.prepareStatement(deleteSql);
                PreparedStatement pointsStmt = con.prepareStatement(pointsSql);
                PreparedStatement logStmt = con.prepareStatement(LOG_SQL)) {
            con.setAutoCommit(false);
            try {
                deleteStmt.setInt(1, reviewId);
                deleteStmt.setInt(2, userId);
                if (deleteStmt.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
                pointsStmt.setInt(1, REVIEW_POINTS);
                pointsStmt.setInt(2, userId);
                pointsStmt.executeUpdate();
                logStmt.setInt(1, userId);
                logStmt.setString(2, "delete_review");
                logStmt.setInt(3, reviewId);
                logStmt.setString(4, "Deleted review " + reviewId + " (-" + REVIEW_POINTS + " points).");
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
