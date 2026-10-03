package com.studyfinder.service;

import com.studyfinder.dao.StudyFinderDAO;
import com.studyfinder.dao.StudySpotDAO;
import com.studyfinder.model.Reward;
import com.studyfinder.model.StudySpot;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class StudyFinderPageService {
    private final StudySpotDAO studySpotDAO = new StudySpotDAO();
    private final StudyFinderDAO studyFinderDAO = new StudyFinderDAO();

    public List<StudySpot> getStudySpots() throws SQLException { return studySpotDAO.getAllStudySpots(); }
    public List<Reward> getRewards() throws SQLException { return studyFinderDAO.getActiveRewards(); }
    public int getStudentPoints(int userId) throws SQLException { return studyFinderDAO.getStudentPoints(userId); }
    public void redeemReward(int userId, int rewardId) throws SQLException { studyFinderDAO.redeemReward(userId, rewardId); }
    public List<Map<String, Object>> getReviews() throws SQLException { return studyFinderDAO.getReviews(); }
    public List<Map<String, Object>> getActivityLogs() throws SQLException { return studyFinderDAO.getActivityLogs(); }
    public List<Map<String, Object>> getVerifications() throws SQLException { return studyFinderDAO.getVerifications(); }
}
