package com.studyfinder.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.studyfinder.dao.SpotSuggestionDAO;

/** Business rules for students suggesting study spots and administrators reviewing them. */
public class SpotSuggestionService {

    private static final List<String> ACCESS_TYPES = List.of("Public access", "Student access");

    private final SpotSuggestionDAO dao = new SpotSuggestionDAO();

    public List<Map<String, Object>> getBuildings() throws SQLException { return dao.getBuildings(); }

    public List<Map<String, Object>> getPendingSuggestions() throws SQLException { return dao.getPendingSuggestions(); }

    /**
     * Checks the student's suggestion and saves it for an administrator to review.
     *
     * @return null on success, otherwise a message to show the student
     */
    public String suggest(int studentId, String name, String buildingText, String location, String accessType,
            String capacityText, String coordinates, String description, String[] amenities) throws SQLException {
        String spotName = name == null ? "" : name.trim();
        String spotLocation = location == null ? "" : location.trim();
        String spotDescription = description == null ? "" : description.trim();
        if (spotName.isEmpty() || spotName.length() > 100) return "Enter a spot name (up to 100 characters).";
        if (spotLocation.isEmpty() || spotLocation.length() > 100) return "Enter where the spot is (up to 100 characters).";
        if (spotDescription.length() > 255) return "Description can be at most 255 characters.";
        if (!ACCESS_TYPES.contains(accessType)) return "Choose an access type.";

        int buildingId;
        int capacity;
        try {
            buildingId = Integer.parseInt(buildingText);
            capacity = Integer.parseInt(capacityText);
        } catch (NumberFormatException exception) {
            return "Choose a building and enter how many seats there are.";
        }
        if (capacity < 1 || capacity > 1000) return "Seats must be between 1 and 1000.";

        double[] point;
        try {
            // Same check as the admin form: at most 6 decimal places and on the SJSU main campus.
            point = new AdminDashboardService().parseCoordinates(coordinates);
        } catch (IllegalArgumentException exception) {
            return exception.getMessage();
        }

        return dao.createSuggestion(studentId, spotName, buildingId, spotLocation, accessType, capacity,
                point[0], point[1], spotDescription.isEmpty() ? null : spotDescription,
                amenities == null ? List.of() : List.of(amenities));
    }

    /**
     * Approves or rejects a pending suggestion.
     *
     * @throws IllegalArgumentException with a message to show the administrator
     */
    public void review(int adminId, String spotIdText, String decision) throws SQLException {
        if (!"approve".equals(decision) && !"reject".equals(decision)) {
            throw new IllegalArgumentException("Choose approve or reject.");
        }
        int spotId;
        try {
            spotId = Integer.parseInt(spotIdText);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Unknown study spot.");
        }
        if (!dao.reviewSuggestion(adminId, spotId, "approve".equals(decision))) {
            throw new IllegalArgumentException("That suggestion was already reviewed or does not exist.");
        }
    }
}
