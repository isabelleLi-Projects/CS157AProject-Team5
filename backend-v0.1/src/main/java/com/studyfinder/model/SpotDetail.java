package com.studyfinder.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything the Study Spot page (study-spot.jsp) shows for one spot.
 * Filled by SpotDetailDAO, written into window.spotDetail by the JSP.
 */
public class SpotDetail {

    /** One row from provides JOIN amenities. */
    public record Amenity(String name, String description, String notes) {
    }

    /** One row from reports, with its confirmed/disputed counts from verifies. */
    public record Report(int reportId, int authorId, String authorFirstName, String noise,
                         String crowdedness, Integer outlets, String smell, String wifi, String notes,
                         String createdAt, long minutesAgo, int confirmed, int disputed) {
    }

    /** One visible row from reviews JOIN users. */
    public record Review(String firstName, String text, String createdAt) {
    }

    private int spotId;
    private int buildingId;
    private String name;
    private String buildingName;
    private String buildingAcronym;
    private String locationDetails;
    private String description;
    private int capacity;
    private String accessType;
    private String photoPath;
    private double latitude;
    private double longitude;

    // Today's hours. All null when building_hours has no row for today.
    private String today;
    private String openingTime;
    private String closingTime;
    private Boolean openNow;

    private final List<Amenity> amenities = new ArrayList<>();
    private final List<Report> recentReports = new ArrayList<>();
    private final List<Review> reviews = new ArrayList<>();

    // The current student's rating of the latest report ("confirmed"/"disputed"), or null.
    private String currentUserRating;

    public int getSpotId() { return spotId; }
    public void setSpotId(int spotId) { this.spotId = spotId; }
    public int getBuildingId() { return buildingId; }
    public void setBuildingId(int buildingId) { this.buildingId = buildingId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
    public String getBuildingAcronym() { return buildingAcronym; }
    public void setBuildingAcronym(String buildingAcronym) { this.buildingAcronym = buildingAcronym; }
    public String getLocationDetails() { return locationDetails; }
    public void setLocationDetails(String locationDetails) { this.locationDetails = locationDetails; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getAccessType() { return accessType; }
    public void setAccessType(String accessType) { this.accessType = accessType; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getToday() { return today; }
    public void setToday(String today) { this.today = today; }
    public String getOpeningTime() { return openingTime; }
    public void setOpeningTime(String openingTime) { this.openingTime = openingTime; }
    public String getClosingTime() { return closingTime; }
    public void setClosingTime(String closingTime) { this.closingTime = closingTime; }
    public Boolean getOpenNow() { return openNow; }
    public void setOpenNow(Boolean openNow) { this.openNow = openNow; }
    public List<Amenity> getAmenities() { return amenities; }
    public List<Report> getRecentReports() { return recentReports; }
    public List<Review> getReviews() { return reviews; }
    public String getCurrentUserRating() { return currentUserRating; }
    public void setCurrentUserRating(String currentUserRating) { this.currentUserRating = currentUserRating; }

    public boolean isPublicAccess() { return "Public access".equalsIgnoreCase(accessType); }

    /** The newest report for this spot, or null when nobody has reported yet. */
    public Report getLatestReport() { return recentReports.isEmpty() ? null : recentReports.get(0); }
}
