package com.studyfinder.service;

import com.studyfinder.dao.StudySpotDAO;
import com.studyfinder.model.StudySpot;

import java.sql.SQLException;
import java.util.List;

/**
 * Application/business layer for study spots.
 * The JSP talks to this class instead of talking to JDBC directly.
 */
public class StudySpotService {

    private final StudySpotDAO studySpotDAO;

    public StudySpotService() {
        this.studySpotDAO = new StudySpotDAO();
    }

    public List<StudySpot> getActiveStudySpots() throws SQLException {
        return studySpotDAO.getAllStudySpots();
    }
}
