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
    private static final List<String> DAYS = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    private static final Pattern TIME = Pattern.compile("([01]\\d|2[0-3]):[0-5]\\d");

    /**
     * Adds a spot with its amenities and building hours.
     * openingTimes/closingTimes: day name -> "HH:mm"; a day missing from either map is Closed.
     *
     * @throws IllegalArgumentException with a message to show the administrator
     */
    public void addStudySpot(String name, Integer buildingId, String location, String accessType, int capacity,
            double latitude, double longitude, String photoPath, String description, String[] amenities,
            Map<String, String> openingTimes, Map<String, String> closingTimes) throws SQLException {
        // study_spots.description is varchar(255); empty means no description.
        String trimmedDescription = description == null ? "" : description.trim();
        if (trimmedDescription.length() > 255) {
            throw new IllegalArgumentException("Description can be at most 255 characters.");
        }
        Map<String, String[]> hours = new LinkedHashMap<>();
        for (String day : DAYS) {
            String open = openingTimes.get(day);
            String close = closingTimes.get(day);
            if (open == null || open.isBlank() || close == null || close.isBlank()) {
                hours.put(day, null); // Closed
                continue;
            }
            if (!TIME.matcher(open).matches() || !TIME.matcher(close).matches()) {
                throw new IllegalArgumentException("Times for " + day + " must look like 08:00.");
            }
            if (open.equals(close)) {
                throw new IllegalArgumentException(day + " opening and closing times can't be the same.");
            }
            hours.put(day, new String[] { open + ":00", close + ":00" });
        }
        List<String> amenityNames = amenities == null ? List.of() : List.of(amenities);
        dao.addStudySpot(name, buildingId, location, accessType, capacity, latitude, longitude, photoPath,
                trimmedDescription.isEmpty() ? null : trimmedDescription, amenityNames, buildingId == null ? null : hours);
    }

    public List<Map<String, Object>> getAllSpots() throws SQLException { return dao.getAllSpots(); }

    private static final List<String> ACTIVE_TYPES = List.of("student", "spot", "review", "report");

    /**
     * Makes a student, study spot, review, or report active or inactive.
     *
     * @throws IllegalArgumentException with a message to show the administrator
     */
    public void setActive(int adminId, String type, String idText, String activeText) throws SQLException {
        if (!ACTIVE_TYPES.contains(type)) throw new IllegalArgumentException("Unknown item type.");
        int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Unknown item.");
        }
        boolean active = "1".equals(activeText);
        if (!dao.setActive(adminId, type, id, active)) {
            throw new IllegalArgumentException("That " + type + " was not found.");
        }
    }

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
        if (!isOnCampus(latitude.doubleValue(), longitude.doubleValue())) {
            throw new IllegalArgumentException("Coordinates must be on the SJSU main campus.");
        }
        return new double[] { latitude.doubleValue(), longitude.doubleValue() };
    }

    // Outline of the SJSU main campus as {latitude, longitude} corners, going around the edge.
    // Taken from OpenStreetMap (c) OpenStreetMap contributors. Same points as SJSU_CAMPUS in AdminSpotForm.tsx.
    private static final double[][] SJSU_CAMPUS = {
        { 37.335805, -121.885877 }, // 4th St & San Fernando St (King Library corner)
        { 37.333161, -121.883917 },
        { 37.331614, -121.882799 }, // 4th St & San Salvador St
        { 37.334576, -121.876568 }, // 10th St & San Salvador St
        { 37.338646, -121.879621 }, // 10th St & San Fernando St
        { 37.337289, -121.882765 },
    };

    /** True when the point is inside the campus outline (ray casting test). */
    public static boolean isOnCampus(double latitude, double longitude) {
        boolean inside = false;
        for (int i = 0, j = SJSU_CAMPUS.length - 1; i < SJSU_CAMPUS.length; j = i++) {
            double latI = SJSU_CAMPUS[i][0], lngI = SJSU_CAMPUS[i][1];
            double latJ = SJSU_CAMPUS[j][0], lngJ = SJSU_CAMPUS[j][1];
            if ((latI > latitude) != (latJ > latitude)
                    && longitude < (lngJ - lngI) * (latitude - latI) / (latJ - latI) + lngI) {
                inside = !inside;
            }
        }
        return inside;
    }
}
