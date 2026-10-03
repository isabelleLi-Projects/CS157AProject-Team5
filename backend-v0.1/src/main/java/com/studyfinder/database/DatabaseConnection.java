package com.studyfinder.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = System.getenv().getOrDefault(
        "STUDYFINDER_DB_URL",
        "jdbc:mysql://127.0.0.1:3306/studyfinder"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    );

    private static final String USERNAME = System.getenv().getOrDefault(
        "STUDYFINDER_DB_USER", "root"
    );

    private static final String PASSWORD = System.getenv().getOrDefault(
        "STUDYFINDER_DB_PASSWORD", ""
    );

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("MySQL Connector/J is not on the web application's classpath.", exception);
        }
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
