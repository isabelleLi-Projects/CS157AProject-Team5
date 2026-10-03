package com.studyfinder.dao;

import com.studyfinder.database.DatabaseConnection;
import com.studyfinder.model.Reward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

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
    public List<Map<String, Object>> getActivityLogs() throws SQLException { return rows("SELECT user_id, action_type, target_type, target_id FROM activitylogs ORDER BY created_at DESC LIMIT 100"); }
    public List<Map<String, Object>> getVerifications() throws SQLException { return rows("SELECT report_id, user_id, result FROM verifies ORDER BY verified_at DESC"); }

    public List<Reward> getActiveRewards() throws SQLException {
        List<Reward> rewards = new ArrayList<>();
        String sql = "SELECT reward_id, reward_name, description, point_cost, stock_quantity "
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

    public void redeemReward(int userId, int rewardId) throws SQLException {
        String rewardSql = "SELECT point_cost, stock_quantity FROM rewards "
                + "WHERE reward_id = ? AND is_active = 1 FOR UPDATE";
        String pointsSql = "SELECT points FROM students WHERE user_id = ? FOR UPDATE";
        String updatePoints = "UPDATE students SET points = points - ? WHERE user_id = ?";
        String updateStock = "UPDATE rewards SET stock_quantity = stock_quantity - 1 "
                + "WHERE reward_id = ? AND stock_quantity > 0";
        String insertRedemption = "INSERT INTO redeems "
                + "(user_id, reward_id, points_spent, status, redeemed_at) "
                + "VALUES (?, ?, ?, 'completed', CURRENT_TIMESTAMP)";

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement rewardStmt = con.prepareStatement(rewardSql);
                 PreparedStatement pointsStmt = con.prepareStatement(pointsSql);
                 PreparedStatement updatePointsStmt = con.prepareStatement(updatePoints);
                 PreparedStatement updateStockStmt = con.prepareStatement(updateStock);
                 PreparedStatement redemptionStmt = con.prepareStatement(insertRedemption)) {
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
                    con.commit();
                }
            } catch (SQLException exception) {
                con.rollback();
                throw exception;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}
