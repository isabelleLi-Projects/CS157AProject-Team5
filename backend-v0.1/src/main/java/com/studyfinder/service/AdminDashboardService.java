package com.studyfinder.service;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.studyfinder.dao.AdminDashboardDAO;


public class AdminDashboardService {
    private final AdminDashboardDAO dao = new AdminDashboardDAO();
    public Map<String, Integer> getCounts() throws SQLException { return dao.getCounts(); }
    public List<Map<String, Object>> getReports() throws SQLException { return dao.getReports(); }
    public List<Map<String, Object>> getReviews() throws SQLException { return dao.getReviews(); }
    public List<Map<String, Object>> getUsers() throws SQLException { return dao.getUsers(); }
    public List<Map<String, Object>> getActivityLogs() throws SQLException { return dao.getActivityLogs(); }
    public List<Map<String, Object>> getRecentRedemptions() throws SQLException { return dao.getRecentRedemptions(); }
    public List<Map<String, Object>> getSpotClicks() throws SQLException { return dao.getSpotClicks(); }
    public void addReward(String name, String description, int points, int stock, String photoPath) throws SQLException { dao.addReward(name, description, points, stock, photoPath); }
    public void addStudySpot(String name, Integer buildingId, String location, String accessType, int capacity, double latitude, double longitude, String photoPath) throws SQLException { dao.addStudySpot(name, buildingId, location, accessType, capacity, latitude, longitude, photoPath); }
    public List<Map<String, Object>> getBuildings() throws SQLException {return dao.getBuildings();}
}
