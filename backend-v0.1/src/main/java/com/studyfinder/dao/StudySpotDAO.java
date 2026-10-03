package com.studyfinder.dao;

import com.studyfinder.database.DatabaseConnection;
import com.studyfinder.model.StudySpot;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudySpotDAO {

    public List<StudySpot> getAllStudySpots() throws SQLException {

        List<StudySpot> studySpots = new ArrayList<>();

        String sql =
            "SELECT s.spot_id, s.name, s.building, s.location, s.capacity, "
            + "s.latitude, s.longitude, s.is_active, s.access_type, s.photo_path, "
            + "COALESCE(r.noise_level, 'Unknown') AS noise_level, "
            + "COALESCE(r.crowdedness, 'Unknown') AS crowdedness, "
            + "COALESCE(CAST(r.outlet_avaiable AS CHAR), 'Unknown') AS outlets, "
            + "COALESCE(DATE_FORMAT(r.created_at, '%Y-%m-%d %H:%i'), 'No reports') AS report_time, "
            + "GROUP_CONCAT(DISTINCT a.amentity_name SEPARATOR '||') AS amenities "
            + "FROM studyspots s "
            + "LEFT JOIN reports r ON r.report_id = ("
            + "SELECT r2.report_id FROM reports r2 WHERE r2.spot_id = s.spot_id "
            + "ORDER BY r2.created_at DESC, r2.report_id DESC LIMIT 1) "
            + "LEFT JOIN provides p ON p.spot_id = s.spot_id "
            + "LEFT JOIN amenities a ON a.amenity_id = p.amenity_id AND a.is_active = 1 "
            + "WHERE s.is_active = 1 "
            + "GROUP BY s.spot_id, s.name, s.building, s.location, s.capacity, s.latitude, "
            + "s.longitude, s.is_active, s.access_type, s.photo_path, r.noise_level, "
            + "r.crowdedness, r.outlet_avaiable, r.created_at "
            + "ORDER BY s.name";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                StudySpot spot = new StudySpot();
            
                spot.setSpotId(rs.getInt("spot_id"));
                spot.setName(rs.getString("name"));
                spot.setBuilding(rs.getString("building"));
                spot.setLocation(rs.getString("location"));
                spot.setCapacity(rs.getInt("capacity"));
                spot.setLatitude(rs.getDouble("latitude"));
                spot.setLongitude(rs.getDouble("longitude"));
                spot.setActive(rs.getInt("is_active") == 1);
                spot.setAccessType(rs.getString("access_type"));
                spot.setPhotoPath(rs.getString("photo_path"));
                spot.setNoise(rs.getString("noise_level"));
                spot.setCrowdedness(rs.getString("crowdedness"));
                spot.setOutlets(rs.getString("outlets"));
                spot.setUpdated(rs.getString("report_time"));

                String amenities = rs.getString("amenities");
                if (amenities != null) {
                    for (String amenity : amenities.split("\\|\\|")) spot.addAmenity(amenity);
                }
            
                studySpots.add(spot);
            }

        }

        return studySpots;
    }
}
