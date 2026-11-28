package com.studyfinder.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.studyfinder.dao.StudentHistoryDAO;
import com.studyfinder.dao.StudyFinderDAO;

/** Business layer for the My history page (Functional Requirement #8). */
public class StudentHistoryService {

    private final StudentHistoryDAO historyDAO = new StudentHistoryDAO();
    private final StudyFinderDAO studyFinderDAO = new StudyFinderDAO();

    public List<Map<String, Object>> getPointsActivity(int userId) throws SQLException { return historyDAO.getPointsActivity(userId); }
    public List<Map<String, Object>> getSpotHistory(int userId) throws SQLException { return historyDAO.getSpotHistory(userId); }
    public List<Map<String, Object>> getSuggestions(int userId) throws SQLException { return historyDAO.getSuggestions(userId); }

    /** The student's current balance (students.points). */
    public int getCurrentPoints(int userId) throws SQLException { return studyFinderDAO.getStudentPoints(userId); }

    /** {earned, spent} added up from the points activity. */
    public int[] totals(List<Map<String, Object>> activity) {
        int earned = 0;
        int spent = 0;
        for (Map<String, Object> row : activity) {
            int points = ((Number) row.get("points")).intValue();
            if (points >= 0) earned += points;
            else spent -= points;
        }
        return new int[] { earned, spent };
    }
}
