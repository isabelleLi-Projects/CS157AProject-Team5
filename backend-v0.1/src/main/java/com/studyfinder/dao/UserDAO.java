package com.studyfinder.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.studyfinder.database.DatabaseConnection;
import com.studyfinder.model.User;

/**
 * Database access for the Users and Roles tables.
 * Every query uses a PreparedStatement, so user input is never pasted into SQL.
 */
public class UserDAO {

    /** MySQL error code for a duplicate value in a UNIQUE column. */
    private static final int DUPLICATE_ENTRY = 1062;

    private static final String FIND_BY_EMAIL =
            "SELECT u.user_id, u.full_name, u.email, u.password_hash, "
            + "u.email_verified, u.created_at, "
            + "u.account_status AS status, "
            + "CASE WHEN a.user_id IS NOT NULL THEN 'admin' "
            + "WHEN s.user_id IS NOT NULL THEN 'student' "
            + "ELSE 'student' END AS role_name "
            + "FROM users u "
            + "LEFT JOIN students s ON u.user_id = s.user_id "
            + "LEFT JOIN administrators a ON u.user_id = a.user_id "
            + "WHERE u.email = ?";

    private static final String EMAIL_EXISTS =
            "SELECT 1 FROM users WHERE email = ?";

    private static final String CREATE_USER =
            "INSERT INTO users (full_name, email, password_hash, email_verified, account_status) "
            + "VALUES (?, ?, ?, 1, 'active')";

    private static final String CREATE_STUDENT =
            "INSERT INTO students (user_id, student_id) VALUES (?, ?)";

    /** Returns the user with this email, or null if there is none. Email must already be lowercase. */
    public User findByEmail(String email) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(FIND_BY_EMAIL)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setEmailVerified(rs.getBoolean("email_verified"));
                user.setStatus(rs.getString("status"));
                user.setRoleName(rs.getString("role_name"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                return user;
            }
        }
    }

    public boolean emailExists(String email) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(EMAIL_EXISTS)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Creates the users and students rows together as one transaction. */
    public int createStudent(String fullName, String email, String passwordHash) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement userStmt = con.prepareStatement(CREATE_USER, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement studentStmt = con.prepareStatement(CREATE_STUDENT)) {

            con.setAutoCommit(false);
            try {
                userStmt.setString(1, fullName);
                userStmt.setString(2, email);
                userStmt.setString(3, passwordHash);
                userStmt.executeUpdate();

                int userId;
                try (ResultSet keys = userStmt.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("MySQL did not return the new user_id.");
                    userId = keys.getInt(1);
                }

                studentStmt.setInt(1, userId);
                // The current signup form does not collect student_id.
                // Use the generated user_id as a temporary numeric student_id.
                studentStmt.setString(2, String.valueOf(userId));
                studentStmt.executeUpdate();
                con.commit();
                return userId;
            } catch (SQLException exception) {
                con.rollback();
                throw exception;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // The current users table has no last_login_at column.
    public void recordLogin(int userId) {
        // Intentionally empty until last-login tracking is added to the schema.
    }

    /** True when an INSERT failed because the email is already taken. */
    public static boolean isDuplicateEntry(SQLException e) {
        return e.getErrorCode() == DUPLICATE_ENTRY;
    }
}
