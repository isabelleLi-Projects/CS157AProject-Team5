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
    /**
     * Redeems a reward, then emails the gift code to the student's SJSU email.
     *
     * @return true if the email was sent; false if the redemption saved but the email failed
     */
    public boolean redeemReward(int userId, String email, String name, int rewardId) throws SQLException {
        String giftCode = newGiftCode();
        Object[] reward = studyFinderDAO.redeemReward(userId, rewardId, giftCode);
        try {
            new EmailVerificationService().sendRewardGift(email, name, (String) reward[0], (Integer) reward[1], giftCode);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final java.security.SecureRandom RANDOM = new java.security.SecureRandom();

    /** A gift code like SJSU-7K2Q-9MXD. */
    private static String newGiftCode() {
        StringBuilder code = new StringBuilder("SJSU-");
        for (int i = 0; i < 8; i++) {
            if (i == 4) code.append('-');
            code.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return code.toString();
    }
    public List<Map<String, Object>> getReviews() throws SQLException { return studyFinderDAO.getReviews(); }
    public List<Map<String, Object>> getActivityLogs() throws SQLException { return studyFinderDAO.getActivityLogs(); }
    public List<Map<String, Object>> getVerifications() throws SQLException { return studyFinderDAO.getVerifications(); }
}
