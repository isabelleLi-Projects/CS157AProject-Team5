package com.studyfinder.service;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

    /**
     * Buildings for the Add Study Spot form. Each building also gets:
     * "hours": day name -> [opening "HH:mm", closing "HH:mm"] from building_hours, and
     * "photoPath": newest photo of another spot in the building (may be null).
     */
    public List<Map<String, Object>> getBuildings() throws SQLException {
        List<Map<String, Object>> buildings = dao.getBuildings();
        Map<Object, Map<String, String[]>> hoursByBuilding = new LinkedHashMap<>();
        for (Map<String, Object> row : dao.getAllBuildingHours()) {
            hoursByBuilding.computeIfAbsent(row.get("buildingId"), id -> new LinkedHashMap<>())
                    .put(String.valueOf(row.get("day")),
                            new String[] { String.valueOf(row.get("opening")), String.valueOf(row.get("closing")) });
        }
        for (Map<String, Object> building : buildings) {
            building.put("hours", hoursByBuilding.getOrDefault(building.get("id"), Map.of()));
        }
        return buildings;
    }

    /** Photo to reuse for a new spot in this building, or null when the building has none. */
    public String getBuildingPhoto(Integer buildingId) throws SQLException {
        return buildingId == null ? null : dao.getBuildingPhoto(buildingId);
    }

    // study_spots.latitude/longitude are decimal(9,6): at most 6 digits after the decimal point.
    private static final Pattern COORDINATE = Pattern.compile("-?\\d+(\\.\\d{1,6})?");

    /**
     * Parses "latitude, longitude" typed by an administrator.
     *
     * @return {latitude, longitude}
     * @throws IllegalArgumentException with a message to show the administrator
     */
    public double[] parseCoordinates(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Coordinates are required.");
        }
        String[] parts = text.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Enter coordinates as: latitude, longitude");
        }
        String lat = parts[0].trim();
        String lng = parts[1].trim();
        if (!COORDINATE.matcher(lat).matches() || !COORDINATE.matcher(lng).matches()) {
            throw new IllegalArgumentException("Latitude and longitude must be numbers with at most 6 decimal places.");
        }
        BigDecimal latitude = new BigDecimal(lat);
        BigDecimal longitude = new BigDecimal(lng);
        if (latitude.abs().compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90.");
        }
        if (longitude.abs().compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180.");
        }
        return new double[] { latitude.doubleValue(), longitude.doubleValue() };
    }
}
