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
import com.studyfinder.model.Reward;

/** Database access for rewards and student dashboard data. */
public class StudyFinderDAO {
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

    public List<Map<String, Object>> getReviews() throws SQLException { return rows("SELECT user_id, spot_id, review_text, is_visible FROM reviews ORDER BY created_at DESC"); }
    public List<Map<String, Object>> getActivityLogs() throws SQLException { return rows("SELECT user_id, action_type, target_type, target_id FROM activity_logs ORDER BY created_at DESC LIMIT 100"); }
    public List<Map<String, Object>> getVerifications() throws SQLException { return rows("SELECT report_id, user_id, result FROM verifies ORDER BY verified_at DESC"); }

    public List<Reward> getActiveRewards() throws SQLException {
        List<Reward> rewards = new ArrayList<>();
        String sql = "SELECT reward_id, reward_name, description, points_required AS point_cost, stock_quantity, photo_path "
                + "FROM rewards WHERE is_active = 1 ORDER BY point_cost";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Reward reward = new Reward();
                reward.setRewardId(rs.getInt("reward_id"));
                reward.setName(rs.getString("reward_name"));
                reward.setDescription(rs.getString("description"));
                reward.setPointCost(rs.getInt("point_cost"));
                reward.setStockQuantity(rs.getInt("stock_quantity"));
                reward.setPhotoPath(rs.getString("photo_path"));
                rewards.add(reward);
            }
        }
        return rewards;
    }

    public int getStudentPoints(int userId) throws SQLException {
        String sql = "SELECT points FROM students WHERE user_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("points") : 0;
            }
        }
    }

    /**
     * Redeems a reward in one transaction and logs the gift code in activity_logs.
     *
     * @return the reward's name and the points spent, for the gift email
     */
    public Object[] redeemReward(int userId, int rewardId, String giftCode) throws SQLException {
        String rewardSql = "SELECT reward_name, points_required AS point_cost, stock_quantity FROM rewards "
                + "WHERE reward_id = ? AND is_active = 1 FOR UPDATE";
        String pointsSql = "SELECT points FROM students WHERE user_id = ? FOR UPDATE";
        String updatePoints = "UPDATE students SET points = points - ? WHERE user_id = ?";
        String updateStock = "UPDATE rewards SET stock_quantity = stock_quantity - 1 "
                + "WHERE reward_id = ? AND stock_quantity > 0";
        String insertRedemption = "INSERT INTO redeems "
                + "(user_id, reward_id, points_spent, status, redeemed_at) "
                + "VALUES (?, ?, ?, 'completed', CURRENT_TIMESTAMP)";
        // Record the redemption and its gift code (the gift is emailed to the student).
        String insertLog = "INSERT INTO activity_logs (user_id, action_type, target_type, target_id, details, created_at) "
                + "VALUES (?, 'redeem_reward', 'reward', ?, ?, NOW())";

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement rewardStmt = con.prepareStatement(rewardSql);
                 PreparedStatement pointsStmt = con.prepareStatement(pointsSql);
                 PreparedStatement updatePointsStmt = con.prepareStatement(updatePoints);
                 PreparedStatement updateStockStmt = con.prepareStatement(updateStock);
                 PreparedStatement redemptionStmt = con.prepareStatement(insertRedemption);
                 PreparedStatement logStmt = con.prepareStatement(insertLog)) {
                rewardStmt.setInt(1, rewardId);
                pointsStmt.setInt(1, userId);
                try (ResultSet reward = rewardStmt.executeQuery(); ResultSet points = pointsStmt.executeQuery()) {
                    if (!reward.next() || !points.next()) throw new SQLException("Reward or student not found.");
                    int cost = reward.getInt("point_cost");
                    if (reward.getInt("stock_quantity") <= 0 || points.getInt("points") < cost) {
                        throw new SQLException("You do not have enough points or the reward is out of stock.");
                    }
                    updatePointsStmt.setInt(1, cost);
                    updatePointsStmt.setInt(2, userId);
                    updatePointsStmt.executeUpdate();
                    updateStockStmt.setInt(1, rewardId);
                    if (updateStockStmt.executeUpdate() != 1) throw new SQLException("Reward is out of stock.");
                    redemptionStmt.setInt(1, userId);
                    redemptionStmt.setInt(2, rewardId);
                    redemptionStmt.setInt(3, cost);
                    redemptionStmt.executeUpdate();
                    String rewardName = reward.getString("reward_name");
                    logStmt.setInt(1, userId);
                    logStmt.setInt(2, rewardId);
                    logStmt.setString(3, "Redeemed " + rewardName + " for " + cost + " points. Gift code " + giftCode + ".");
                    logStmt.executeUpdate();
                    con.commit();
                    return new Object[] { rewardName, cost };
                }
            } catch (SQLException exception) {
                con.rollback();
                throw exception;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
    
    // insert into study spot views when a user clicks on the study spot
    public void recordStudySpotView(int spotId, Integer userId) throws SQLException {
        String sql =
            "INSERT INTO study_spot_views (spot_id, user_id) "
          + "VALUES (?, ?)";
    
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
    
            stmt.setInt(1, spotId);
    
            if (userId == null) {
                stmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(2, userId);
            }
    
            stmt.executeUpdate();
        }
    }

}
