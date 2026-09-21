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

    public List<StudySpot> getAllStudySpots() {

        List<StudySpot> studySpots = new ArrayList<>();

        String sql =
            "SELECT * "
            + "FROM StudySpots "
            + "WHERE is_active = 1 "
            + "ORDER BY name";
        
            try {
            
            Connection con =
                DatabaseConnection.getConnection();

            PreparedStatement stmt =
                con.prepareStatement(sql);

            ResultSet rs =
                stmt.executeQuery();
            

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
            
                studySpots.add(spot);
            }

            rs.close();
            stmt.close();
            con.close();

        } catch (SQLException e) {
            System.out.println(
                "SQLException caught: "
                + e.getMessage()
            );
        }

        return studySpots;
    }
}