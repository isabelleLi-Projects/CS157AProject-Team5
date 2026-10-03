package com.studyfinder.model;

import java.util.ArrayList;
import java.util.List;

public class StudySpot {
    private int spotId;
    private String name;
    private String building;
    private String location;
    private int capacity;
    private double latitude;
    private double longitude;
    private boolean active;
    private String accessType;
    private String photoPath;
    private String noise;
    private String crowdedness;
    private String outlets;
    private String updated;
    private final List<String> amenities = new ArrayList<>();

    public int getSpotId() {
        return spotId;
    }

    public void setSpotId(int spotId) {
        this.spotId = spotId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public String getNoise() { return noise; }
    public void setNoise(String noise) { this.noise = noise; }
    public String getCrowdedness() { return crowdedness; }
    public void setCrowdedness(String crowdedness) { this.crowdedness = crowdedness; }
    public String getOutlets() { return outlets; }
    public void setOutlets(String outlets) { this.outlets = outlets; }
    public String getUpdated() { return updated; }
    public void setUpdated(String updated) { this.updated = updated; }
    public List<String> getAmenities() { return amenities; }
    public void addAmenity(String amenity) { if (amenity != null && !amenity.isBlank()) amenities.add(amenity); }
}
