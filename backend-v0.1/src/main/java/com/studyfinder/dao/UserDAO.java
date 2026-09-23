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
            "SELECT u.user_id, u.full_name, u.email, u.password_hash, u.status, r.role_name "
            + "FROM Users u JOIN Roles r ON u.role_id = r.role_id "
            + "WHERE u.email = ?";

    private static final String EMAIL_EXISTS =
            "SELECT 1 FROM Users WHERE email = ?";

    // INSERT ... SELECT inserts nothing if the 'student' role is missing,
    // which gives a clear error instead of a NULL role_id.
    // Accounts start as 'active' until email verification is built.
    private static final String CREATE_STUDENT =
            "INSERT INTO Users (role_id, full_name, email, password_hash, status) "
            + "SELECT role_id, ?, ?, ?, 'active' FROM Roles WHERE role_name = 'student'";

    private static final String RECORD_LOGIN =
            "UPDATE Users SET last_login_at = CURRENT_TIMESTAMP WHERE user_id = ?";

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
                user.setStatus(rs.getString("status"));
                user.setRoleName(rs.getString("role_name"));
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

    /** Creates a student account and returns its new user_id. */
    public int createStudent(String fullName, String email, String passwordHash) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(CREATE_STUDENT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, passwordHash);

            if (stmt.executeUpdate() != 1) {
                throw new SQLException("The 'student' role is missing from the Roles table. Run sql/auth_schema.sql.");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("MySQL did not return the new user_id.");
        }
    }

    public void recordLogin(int userId) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(RECORD_LOGIN)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    /** True when an INSERT failed because the email is already taken. */
    public static boolean isDuplicateEntry(SQLException e) {
        return e.getErrorCode() == DUPLICATE_ENTRY;
    }
}
